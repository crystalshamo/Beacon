package com.example.beacon.ui.following;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.beacon.OrganizationAdapter;
import com.example.beacon.databinding.FragmentFollowingBinding;
import com.example.beacon.models.Organization;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class FollowingFragment extends Fragment {

    private FragmentFollowingBinding binding;
    private RecyclerView recyclerView;
    private OrganizationAdapter adapter;
    private List<Organization> organizationList;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        FollowingViewModel followingViewModel =
                new ViewModelProvider(this).get(FollowingViewModel.class);

        binding = FragmentFollowingBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        recyclerView = binding.recyclerViewOrganizations;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        organizationList = new ArrayList<>();
        adapter = new OrganizationAdapter(getContext(), organizationList, false, true);
        recyclerView.setAdapter(adapter);

        // Load organizations
        loadOrganizations();

        return root;
    }

    private void loadOrganizations() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Get the current user's ID
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Fetch the user's document to get the savedOrgs array
        db.collection("users").document(currentUserId)
                .get()
                .addOnSuccessListener(userDocument -> {
                    if (userDocument.exists()) {
                        // Get the savedOrgs array from the user's document
                        List<String> savedOrgs = (List<String>) userDocument.get("savedOrgs");
                        if (savedOrgs == null) {
                            savedOrgs = new ArrayList<>(); // Initialize an empty list if savedOrgs is null
                        }

                        // Fetch organizations from Firestore
                        List<String> finalSavedOrgs = savedOrgs;
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

                                            // Add the organization to the list only if its ID is in savedOrgs
                                            if (finalSavedOrgs.contains(id)) {
                                                organizationList.add(organization);
                                            }
                                        }

                                        // Update the adapter with the filtered list
                                        adapter.setOrganizations(organizationList);
                                        adapter.notifyDataSetChanged();
                                    } else {
                                        Log.e("FollowingFragment", "Error loading organizations", task.getException());
                                        Toast.makeText(getContext(), "Failed to load organizations", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Log.e("FollowingFragment", "User document does not exist");
                        Toast.makeText(getContext(), "User data not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FollowingFragment", "Error fetching user data", e);
                    Toast.makeText(getContext(), "Failed to fetch user data", Toast.LENGTH_SHORT).show();
                });
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}