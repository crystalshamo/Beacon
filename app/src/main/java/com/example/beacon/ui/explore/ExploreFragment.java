package com.example.beacon.ui.explore;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.beacon.AllEventsActivity;
import com.example.beacon.OrganizationAdapter;
import com.example.beacon.R;
import com.example.beacon.databinding.FragmentExploreBinding;
import com.example.beacon.databinding.DialogMapInputBinding;
import com.example.beacon.models.Organization;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ExploreFragment extends Fragment {

    private FragmentExploreBinding binding;
    private RecyclerView recyclerView;
    private OrganizationAdapter adapter;
    private List<Organization> organizationList;
    private GoogleMap googleMap;
    private SupportMapFragment mapFragment;
    private Dialog dialog;
    private boolean isFirstLocationUpdate = true;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private ToggleButton ToggleView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ExploreViewModel exploreViewModel =
                new ViewModelProvider(this).get(ExploreViewModel.class);

        binding = FragmentExploreBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        // Initialize RecyclerView
        recyclerView = binding.recyclerViewOrganizations;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize organization list and adapter
        organizationList = new ArrayList<>();
        adapter = new OrganizationAdapter(getContext(), organizationList, true, false);
        recyclerView.setAdapter(adapter);

        // Load organizations from Firestore
        loadOrganizations();

        // Initialize map fragment
        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this::initializeMap);
        }

        binding.btnEvents.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AllEventsActivity.class);
            startActivity(intent);
        });

        // Set initial view visibility
        binding.recyclerViewOrganizations.setVisibility(View.VISIBLE);
        binding.mapContainer.setVisibility(View.GONE);

        // Toggle between map and RecyclerView
        binding.toggleView.setOnCheckedChangeListener((buttonView, isChecked) -> toggleView(isChecked));

        // Initialize search input
        EditText searchInput = binding.searchInput;
        filterOrganizations(""); // Show all initially

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



    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Request location permission
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // Permission already granted, enable location features
            if (googleMap != null) {
                try {
                    googleMap.setMyLocationEnabled(true);
                    setCurrentLocation();
                } catch (SecurityException e) {
                    Log.e("ExploreFragment", "SecurityException: Location permission denied", e);
                    Toast.makeText(getContext(), "Location permission denied", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, enable location features
                if (googleMap != null) {
                    try {
                        googleMap.setMyLocationEnabled(true);
                        setCurrentLocation();
                    } catch (SecurityException e) {
                        Log.e("ExploreFragment", "SecurityException: Location permission denied", e);
                        Toast.makeText(getContext(), "Location permission denied", Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                // Permission denied, show a message to the user
                Toast.makeText(getContext(), "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initializeMap(GoogleMap googleMap) {
        this.googleMap = googleMap;

        // Set the camera to Detroit (42.3314, -83.0458)
        LatLng detroit = new LatLng(42.3314, -83.0458);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(detroit, 10)); // Zoom level 10

        // Enable location layer if permission is granted
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            try {
                googleMap.setMyLocationEnabled(true); // Optional: Enable the location layer
            } catch (SecurityException e) {
                Log.e("ExploreFragment", "SecurityException: Location permission denied", e);
                Toast.makeText(getContext(), "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Request location permission if not granted
            checkLocationPermission();
        }

        // Set marker click listener
        googleMap.setOnMarkerClickListener(marker -> {
            Organization org = (Organization) marker.getTag();
            if (org != null) {
                showOrganizationInfoDialog(org.getName(), org.getAddress(), org.getWebsite(), org.getDescription());
            }
            return true;
        });
    }

    private void setCurrentLocation() {
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // Check if location permission is granted
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            try {
                // Request the last known location
                fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(requireActivity(), location -> {
                            if (location != null && isFirstLocationUpdate) {
                                // Use the last known location
                                LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
                                isFirstLocationUpdate = false; // Prevent further updates
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e("ExploreFragment", "Failed to get location", e);
                            Toast.makeText(getContext(), "Failed to get location", Toast.LENGTH_SHORT).show();
                        });
            } catch (SecurityException e) {
                // Handle SecurityException (permission denied)
                Log.e("ExploreFragment", "SecurityException: Location permission denied", e);
                Toast.makeText(getContext(), "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Permission not granted, log an error or request permission
            Log.e("ExploreFragment", "Location permission not granted");
            Toast.makeText(getContext(), "Location permission not granted", Toast.LENGTH_SHORT).show();
        }
    }
    private void toggleView(boolean showMap) {
        if (showMap) {
            binding.recyclerViewOrganizations.animate().alpha(0f).setDuration(300).withEndAction(() -> {
                binding.recyclerViewOrganizations.setVisibility(View.GONE);
            }).start();
            binding.mapContainer.setVisibility(View.VISIBLE);
            binding.mapContainer.setAlpha(0f);
            binding.mapContainer.animate().alpha(1f).setDuration(300).start();
        } else {
            if (googleMap != null) {
                googleMap.clear(); // Clear markers when hiding the map view
            }
            binding.mapContainer.animate().alpha(0f).setDuration(300).withEndAction(() -> {
                binding.mapContainer.setVisibility(View.GONE);
            }).start();
            binding.recyclerViewOrganizations.setVisibility(View.VISIBLE);
            binding.recyclerViewOrganizations.setAlpha(0f);
            binding.recyclerViewOrganizations.animate().alpha(1f).setDuration(300).start();
        }
    }

    private void loadOrganizations() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("organizations")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        organizationList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String name = document.getString("name");
                            String address = document.getString("address");
                            String website = document.getString("website");
                            String description = document.getString("description");
                            String userId = document.getString("userId");
                            Double latitude = document.getDouble("latitude");
                            Double longitude = document.getDouble("longitude");
                            String id = document.getId();

                            if (latitude == null || longitude == null) {
                                latitude = 0.0;
                                longitude = 0.0;
                            }

                            Organization organization = new Organization(name, address, website, description, latitude, longitude, userId, id);
                            organizationList.add(organization);

                            if (googleMap != null) {
                                LatLng orgLocation = new LatLng(latitude, longitude);
                                Marker marker = googleMap.addMarker(new MarkerOptions()
                                        .position(orgLocation)
                                        .title(name));
                                marker.setTag(organization); // Attach organization to the marker
                            }
                        }
                        adapter.setOrganizations(organizationList);
                        adapter.notifyDataSetChanged();
                        filterOrganizations("");
                    } else {
                        Log.w("ExploreFragment", "Error getting documents.", task.getException());
                    }
                });
    }

    private void filterOrganizations(String query) {
        if (googleMap != null) {
            googleMap.clear(); // Clear existing markers

            List<Organization> filteredList = new ArrayList<>();
            for (Organization organization : organizationList) {
                if (organization.getName().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(organization);

                    // Add marker to map for filtered organization
                    LatLng orgLocation = new LatLng(organization.getLatitude(), organization.getLongitude());
                    Marker marker = googleMap.addMarker(new MarkerOptions()
                            .position(orgLocation)
                            .title(organization.getName()));
                    marker.setTag(organization); // Attach organization to the marker
                }
            }

            // Update the list for RecyclerView
            adapter.setOrganizations(filteredList);

            // If map is visible, center on the first filtered organization
            if (filteredList.size() > 0 && binding.toggleView.isChecked()) {
                Organization firstOrg = filteredList.get(0);
                LatLng firstOrgLocation = new LatLng(firstOrg.getLatitude(), firstOrg.getLongitude());
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(firstOrgLocation, 15)); // Move camera to first result
            }
        }
    }

    private void showOrganizationInfoDialog(String name, String address, String website, String description) {
        // Show organization details in a dialog
        DialogMapInputBinding dialogBinding = DialogMapInputBinding.inflate(getLayoutInflater());
        dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(dialogBinding.getRoot());

        dialogBinding.orgName.setText(name);
        dialogBinding.orgAddress.setText(address);
        dialogBinding.orgDescription.setText(description);
        dialogBinding.orgWebsite.setText(website);
        dialogBinding.orgWebsite.setMovementMethod(LinkMovementMethod.getInstance());

        dialogBinding.closeButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}