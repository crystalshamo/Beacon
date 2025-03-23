package com.example.beacon;

import static java.security.AccessController.getContext;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.beacon.R;
import com.example.beacon.models.Organization;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class OrganizationAdapter extends RecyclerView.Adapter<OrganizationAdapter.ViewHolder> {

    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    private List<Organization> organizations; // Full list of organizations
    private List<Organization> filteredOrganizations; // Filtered list of organizations
    private Context context;

    public OrganizationAdapter(Context context, List<Organization> organizations) {
        this.context = context;
        this.organizations = organizations;
        this.filteredOrganizations = new ArrayList<>(organizations); // Initialize filtered list with full list
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_organization2, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Organization org = filteredOrganizations.get(position); // Use filtered list for binding
        holder.orgName.setText(org.getName());
        holder.orgAddress.setText(org.getAddress());
        holder.orgDescription.setText(org.getDescription());
        // Get the organization ID
        String orgId = org.getId(); // Ensure this is not null
        if (orgId == null) {
            Log.e("OrganizationAdapter", "Organization ID is null for: " + org.getName());
        }

        holder.btnViewDetails.setOnClickListener(v -> {
            Intent intent = new Intent(context, OrganizationDetailActivity.class);
            intent.putExtra("name", org.getName());
            intent.putExtra("address", org.getAddress());
            intent.putExtra("description", org.getDescription());
            intent.putExtra("website", org.getWebsite());
            context.startActivity(intent);
        });

        holder.btnSave.setOnClickListener(v -> {
            // Get the current user

            if (user != null) {
                String userId = user.getUid(); // Get the user's UID

                // Reference to the user's document in Firestore
                DocumentReference userRef = FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(userId);

                // Organization ID to add
                // Ensure this is not null
                if (orgId == null) {
                    Log.e("Firestore", "Organization ID is null");
                    Toast.makeText(context, "Invalid organization ID", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Update the savedOrgs array
                userRef.update("savedOrgs", FieldValue.arrayUnion(orgId))
                        .addOnSuccessListener(aVoid -> {
                            Log.d("Firestore", "Organization added to savedOrgs: " + orgId);
                            Toast.makeText(context, "Organization saved!", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Log.e("Firestore", "Error adding organization to savedOrgs", e);
                            Toast.makeText(context, "Failed to save organization", Toast.LENGTH_SHORT).show();
                        });
            } else {
                Log.e("Firestore", "User is not logged in");
                Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public int getItemCount() {
        return filteredOrganizations.size(); // Return size of filtered list
    }

    // Method to update the list of organizations
    public void setOrganizations(List<Organization> organizations) {
        this.filteredOrganizations.clear();
        this.filteredOrganizations.addAll(organizations);
        notifyDataSetChanged(); // Notify the adapter that the data has changed
    }

    // Method to filter organizations by name
    public void filterOrganizations(String query) {
        filteredOrganizations.clear(); // Clear the filtered list

        if (query.isEmpty()) {
            // If the query is empty, show the full list
            filteredOrganizations.addAll(organizations);
        } else {
            // Filter the list based on the query
            for (Organization org : organizations) {
                if (org.getName().toLowerCase().contains(query.toLowerCase())) {
                    filteredOrganizations.add(org);
                }
            }
        }

        notifyDataSetChanged(); // Notify the adapter that the data has changed
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView orgName, orgAddress, orgDescription;
        Button btnViewDetails, btnSave;

        public ViewHolder(View itemView) {
            super(itemView);
            orgName = itemView.findViewById(R.id.orgName);
            orgAddress = itemView.findViewById(R.id.orgAddress);
            orgDescription = itemView.findViewById(R.id.orgDescription);
            btnViewDetails = itemView.findViewById(R.id.btnViewDetails);
            btnSave = itemView.findViewById(R.id.btnSave);
        }
    }
}
