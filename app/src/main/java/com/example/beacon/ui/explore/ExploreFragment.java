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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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

        // Initialize UI components
        initializeUI();

        // Load organizations from Firestore
        loadOrganizations();

        // Initialize map fragment
        initializeMap();

        // Set up button click listeners
        setupButtonListeners();

        // Set up search functionality
        setupSearch();

        return root;
    }

    // Initialize UI components
    private void initializeUI() {
        recyclerView = binding.recyclerViewOrganizations;
        organizationList = new ArrayList<>();

        // Initialize the adapter
        adapter = new OrganizationAdapter(getContext(), organizationList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    // Load organizations from Firestore
    private void loadOrganizations() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("organizations")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        organizationList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String name = document.getString("name");
                            String address = document.getString("address");
                            String website = document.getString("website");
                            String description = document.getString("description");
                            Double latitude = document.getDouble("latitude");
                            Double longitude = document.getDouble("longitude");

                            if (latitude == null || longitude == null) {
                                latitude = 0.0;
                                longitude = 0.0;
                            }

                            Organization organization = new Organization(name, address, website, description, latitude, longitude);
                            organizationList.add(organization);

                            addMarkerToMap(organization);
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Log.w("ExploreFragment", "Error getting documents.", task.getException());
                    }
                });
    }

    // Initialize the map
    private void initializeMap() {
        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    ExploreFragment.this.googleMap = googleMap;
                    centerMapOnDetroit();

                    if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        googleMap.setMyLocationEnabled(true);
                        getCurrentLocation();
                    } else {
                        Log.e("ExploreFragment", "Location permission not granted");
                    }
                }
            });
        }
    }

    // Center the map on Detroit
    private void centerMapOnDetroit() {
        LatLng detroit = new LatLng(42.3314, -83.0458);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(detroit, 10));
    }

    // Get the current location
    private void getCurrentLocation() {
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(getActivity(), location -> {
                    if (location != null) {
                        LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
                    } else {
                        Log.e("ExploreFragment", "Location is null");
                    }
                });
    }

    // Add a marker to the map for an organization
    private void addMarkerToMap(Organization organization) {
        if (googleMap != null) {
            LatLng orgLocation = new LatLng(organization.getLatitude(), organization.getLongitude());
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(orgLocation)
                    .title(organization.getName())
                    .snippet(organization.getAddress());
            googleMap.addMarker(markerOptions);

            googleMap.setOnMarkerClickListener(marker -> {
                showOrganizationInfoDialog(marker.getTitle(), marker.getSnippet());
                return true;
            });
        }
    }

    // Set up button click listeners
    private void setupButtonListeners() {
        binding.btnMap.setOnClickListener(v -> showMapInputDialog());

        binding.toggleView.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                showMapAndHideRecyclerView();
            } else {
                showRecyclerViewAndHideMap();
            }
        });
    }

    // Show map and hide RecyclerView
    private void showMapAndHideRecyclerView() {
        binding.recyclerViewOrganizations.animate().alpha(0f).setDuration(300).withEndAction(() -> {
            binding.recyclerViewOrganizations.setVisibility(View.GONE);
        }).start();
        binding.mapContainer.setVisibility(View.VISIBLE);
        binding.mapContainer.setAlpha(0f);
        binding.mapContainer.animate().alpha(1f).setDuration(300).start();
    }

    // Show RecyclerView and hide map
    private void showRecyclerViewAndHideMap() {
        binding.mapContainer.animate().alpha(0f).setDuration(300).withEndAction(() -> {
            binding.mapContainer.setVisibility(View.GONE);
        }).start();
        binding.recyclerViewOrganizations.setVisibility(View.VISIBLE);
        binding.recyclerViewOrganizations.setAlpha(0f);
        binding.recyclerViewOrganizations.animate().alpha(1f).setDuration(300).start();
    }

    // Set up search functionality
    private void setupSearch() {
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
    }

    // Filter organizations based on the search query
    private void filterOrganizations(String query) {
        List<Organization> filteredList = new ArrayList<>();
        for (Organization org : organizationList) {
            if (org.getName().toLowerCase().contains(query.toLowerCase()) ||
                    org.getAddress().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(org);
            }
        }
        adapter.setOrganizations(filteredList);
    }

    // Show organization info dialog
    private void showOrganizationInfoDialog(String name, String address) {
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        DialogMapInputBinding dialogBinding = DialogMapInputBinding.inflate(getLayoutInflater());
        dialog.setContentView(dialogBinding.getRoot());

        dialogBinding.editPopupName.setText(name);
        dialogBinding.editPopupAddress.setText(address);

        dialog.show();
    }

    // Show map input dialog
    private void showMapInputDialog() {
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        DialogMapInputBinding dialogBinding = DialogMapInputBinding.inflate(getLayoutInflater());
        dialog.setContentView(dialogBinding.getRoot());

        dialogBinding.btnPopupGoToMap.setOnClickListener(v -> {
            String name = dialogBinding.editPopupName.getText().toString().trim();
            String address = dialogBinding.editPopupAddress.getText().toString().trim();

            if (!name.isEmpty() && !address.isEmpty()) {
                Intent intent = new Intent(getActivity(), MapsActivity.class);
                intent.putExtra("name", name);
                intent.putExtra("address", address);
                startActivity(intent);
                dialog.dismiss();
            } else {
                Toast.makeText(getContext(), "Please enter both name and address.", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }
}