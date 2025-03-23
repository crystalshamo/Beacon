package com.example.beacon.ui.explore;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.beacon.MapsActivity;
import com.example.beacon.R;
import com.example.beacon.databinding.FragmentExploreBinding;
import com.example.beacon.databinding.DialogMapInputBinding;
import com.example.beacon.models.Organization;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

public class ExploreFragment extends Fragment {

    private FragmentExploreBinding binding;
    private RecyclerView recyclerView;
    private OrganizationAdapter adapter;
    private List<Organization> organizationList;
    private GoogleMap googleMap;
    private SupportMapFragment mapFragment;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ExploreViewModel exploreViewModel =
                new ViewModelProvider(this).get(ExploreViewModel.class);

        binding = FragmentExploreBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Initialize RecyclerView
        recyclerView = binding.recyclerViewOrganizations;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize the organization list
        organizationList = new ArrayList<>();

        // Set up the adapter
        adapter = new OrganizationAdapter(getContext(), organizationList);
        recyclerView.setAdapter(adapter);

        // Load organizations from Firestore
        loadOrganizations();

        // Initialize map fragment
        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    // Store the GoogleMap instance
                    ExploreFragment.this.googleMap = googleMap;

                    // Center the map on Detroit
                    LatLng detroit = new LatLng(42.3314, -83.0458); // Detroit's coordinates
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(detroit, 10)); // Zoom level can be adjusted

                    // Check if permission is granted to show the user's current location
                    if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        googleMap.setMyLocationEnabled(true);

                        // Get the current location
                        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
                        fusedLocationClient.getLastLocation()
                                .addOnSuccessListener(getActivity(), location -> {
                                    if (location != null) {
                                        // If the location is available, move the camera to the user's location
                                        LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));  // Adjust zoom level
                                    } else {
                                        // Handle case where location is null
                                        Log.e("ExploreFragment", "Location is null");
                                    }
                                });
                    } else {
                        // Handle permission not granted scenario
                        Log.e("ExploreFragment", "Location permission not granted");
                    }
                }
            });
        }

        // Set button click listener for opening the map input dialog
        binding.btnMap.setOnClickListener(v -> showMapInputDialog());

        // Set initial visibility when the app opens
        binding.recyclerViewOrganizations.setVisibility(View.VISIBLE);  // Show RecyclerView
        binding.mapContainer.setVisibility(View.GONE);  // Hide Map Container

        // Toggle the visibility of map and RecyclerView based on switch state
        binding.toggleView.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Show map and hide RecyclerView
                binding.recyclerViewOrganizations.animate().alpha(0f).setDuration(300).withEndAction(() -> {
                    binding.recyclerViewOrganizations.setVisibility(View.GONE);
                }).start();
                binding.mapContainer.setVisibility(View.VISIBLE);
                binding.mapContainer.setAlpha(0f);
                binding.mapContainer.animate().alpha(1f).setDuration(300).start();
            } else {
                // Hide map and show RecyclerView
                binding.mapContainer.animate().alpha(0f).setDuration(300).withEndAction(() -> {
                    binding.mapContainer.setVisibility(View.GONE);
                }).start();
                binding.recyclerViewOrganizations.setVisibility(View.VISIBLE);
                binding.recyclerViewOrganizations.setAlpha(0f);
                binding.recyclerViewOrganizations.animate().alpha(1f).setDuration(300).start();
            }
        });

        // Initialize search input and add TextWatcher
        EditText searchInput = binding.searchInput;
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterOrganizations(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        return root;
    }

    private void loadOrganizations() {
        // Get the Firestore instance
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Reference to the "organizations" collection in Firestore
        db.collection("organizations")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Clear the current list to avoid duplicates
                        organizationList.clear();

                        // Loop through each document in the collection
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Create a new Organization object for each document
                            String name = document.getString("name");
                            String address = document.getString("address");
                            String website = document.getString("website");
                            String description = document.getString("description");
                            Double latitude = document.getDouble("latitude");
                            Double longitude = document.getDouble("longitude");

                            // Handle null values in Firestore fields
                            if (latitude == null || longitude == null) {
                                latitude = 0.0;  // Default to a safe value
                                longitude = 0.0; // Default to a safe value
                            }

                            // Create Organization object and add it to the list
                            Organization organization = new Organization(name, address, website, description, latitude, longitude);
                            organizationList.add(organization);

                            // Add a marker on the map for each organization
                            if (googleMap != null) {
                                LatLng orgLocation = new LatLng(latitude, longitude);
                                MarkerOptions markerOptions = new MarkerOptions()
                                        .position(orgLocation)
                                        .title(name)
                                        .snippet(address); // Displaying address as a snippet
                                googleMap.addMarker(markerOptions);

                                // Set a click listener on the marker
                                googleMap.setOnMarkerClickListener(marker -> {
                                    // Show more information when a marker is clicked
                                    showOrganizationInfoDialog(marker.getTitle(), marker.getSnippet());
                                    return true;
                                });
                            }
                        }

                        // Notify the adapter that the data has changed
                        adapter.notifyDataSetChanged();
                    } else {
                        // Handle the error if the Firestore query fails
                        Log.w("ExploreFragment", "Error getting documents.", task.getException());
                    }
                });
    }

    private void showOrganizationInfoDialog(String name, String address) {
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        // Inflate the dialog layout
        DialogMapInputBinding dialogBinding = DialogMapInputBinding.inflate(getLayoutInflater());
        dialog.setContentView(dialogBinding.getRoot());

        // Set the text for the dialog (you can modify this part to add other details like website and description)
        dialogBinding.editPopupName.setText(name);
        dialogBinding.editPopupAddress.setText(address);

        // Show the dialog
        dialog.show();
    }

    private void showMapInputDialog() {
        // Create and display the map input dialog
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        // Inflate dialog layout using View Binding
        DialogMapInputBinding dialogBinding = DialogMapInputBinding.inflate(getLayoutInflater());
        dialog.setContentView(dialogBinding.getRoot());

        // Handle button click inside the dialog
        dialogBinding.btnPopupGoToMap.setOnClickListener(v -> {
            String name = dialogBinding.editPopupName.getText().toString().trim();
            String address = dialogBinding.editPopupAddress.getText().toString().trim();

            // Validate name and address fields
            if (!name.isEmpty() && !address.isEmpty() && address.length() > 5) {
                Intent intent = new Intent(getActivity(), MapsActivity.class);
                intent.putExtra("name", name);
                intent.putExtra("address", address);
                startActivity(intent);
                dialog.dismiss(); // Close the popup dialog
            } else {
                // Show a Toast or alert that the fields are invalid
                Toast.makeText(getContext(), "Please enter valid name and address.", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void filterOrganizations(String query) {
        List<Organization> filteredList = new ArrayList<>();

        if (query.isEmpty()) {
            // If the query is empty, show the full list
            filteredList.addAll(organizationList);
        } else {
            // Filter the list based on the query
            for (Organization organization : organizationList) {
                if (organization.getName().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(organization);
                }
            }
        }

        // Update the adapter with the filtered list
        adapter.setOrganizations(filteredList);
        adapter.notifyDataSetChanged();

        // Show a message if no results are found
        if (filteredList.isEmpty()) {
            Toast.makeText(getContext(), "No organizations found", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}