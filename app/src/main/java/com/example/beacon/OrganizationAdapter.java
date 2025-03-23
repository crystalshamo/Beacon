package com.example.beacon;

import static java.security.AccessController.getContext;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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
    private boolean showSaveButton; // Flag to control btnSave visibility
    private boolean showRequestButton;
    public OrganizationAdapter(Context context, List<Organization> organizations, boolean showSaveButton, boolean showRequestButton) {

        this.context = context;
        this.organizations = organizations;
        this.filteredOrganizations = new ArrayList<>(organizations); // Initialize filtered list with full list
        this.showSaveButton = showSaveButton; // Set the visibility flag
        this.showRequestButton = showRequestButton;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_organization2, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Organization org = filteredOrganizations.get(position);
        holder.orgName.setText(org.getName());
        holder.orgAddress.setText(org.getAddress());
        holder.orgDescription.setText(org.getDescription());

        String orgId = org.getId();
        if (orgId == null) {
            Log.e("OrganizationAdapter", "Organization ID is null for: " + org.getName());
            return;
        }
        holder.btnViewDetails.setOnClickListener(v -> {
            Intent intent = new Intent(context, orgDetailsActivity.class);

            // Pass organization data to OrgDetailsActivity
            intent.putExtra("orgId", org.getId()); // Pass the organization ID
            intent.putExtra("orgName", org.getName());
            intent.putExtra("orgAddress", org.getAddress());
            intent.putExtra("orgWebsite", org.getWebsite());
            intent.putExtra("orgDescription", org.getDescription());

            context.startActivity(intent);
        });
        // Set initial button state
        holder.btnSave.setSelected(isOrganizationSaved(orgId));
        holder.btnSave.setBackgroundResource(R.drawable.btn_save_selector);

        holder.btnSave.setOnClickListener(v -> {
            if (user != null) {
                String userId = user.getUid();
                DocumentReference userRef = FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(userId);

                if (holder.btnSave.isSelected()) {
                    // Remove from savedOrgs
                    userRef.update("savedOrgs", FieldValue.arrayRemove(orgId))
                            .addOnSuccessListener(aVoid -> {
                                Log.d("Firestore", "Organization removed from savedOrgs: " + orgId);
                                Toast.makeText(context, "Organization unsaved!", Toast.LENGTH_SHORT).show();
                                holder.btnSave.setSelected(false);
                            })
                            .addOnFailureListener(e -> Log.e("Firestore", "Error removing organization", e));
                } else {
                    // Add to savedOrgs
                    userRef.update("savedOrgs", FieldValue.arrayUnion(orgId))
                            .addOnSuccessListener(aVoid -> {
                                Log.d("Firestore", "Organization added to savedOrgs: " + orgId);
                                Toast.makeText(context, "Organization saved!", Toast.LENGTH_SHORT).show();
                                holder.btnSave.setSelected(true);
                            })
                            .addOnFailureListener(e -> Log.e("Firestore", "Error saving organization", e));
                }
            } else {
                Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show();
            }
        });
        if (showSaveButton) {
            holder.btnSave.setVisibility(View.VISIBLE);
        } else {
            holder.btnSave.setVisibility(View.GONE);
        }
        if (showRequestButton) {
            holder.btnRequest.setVisibility(View.VISIBLE);
        } else {
            holder.btnRequest.setVisibility(View.GONE);
        }

        holder.btnRequest.setOnClickListener(v -> {
            // Inflate the dialog layout
            View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_request, null);

            // Create the dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setView(dialogView);
            AlertDialog dialog = builder.create();

            // Initialize views in the dialog
            EditText editRequest = dialogView.findViewById(R.id.editRequest);
            Button btnSubmitRequest = dialogView.findViewById(R.id.btnSubmitRequest);

            // Handle submit button click
            btnSubmitRequest.setOnClickListener(submitView -> {
                String requestText = editRequest.getText().toString().trim();

                if (requestText.isEmpty()) {
                    Toast.makeText(context, "Request cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Submit the request to Firestore
                submitRequestToFirestore(org.getId(), requestText);

                // Dismiss the dialog
                dialog.dismiss();
            });

            // Show the dialog
            dialog.show();
        });

    }
    private boolean isOrganizationSaved(String orgId) {
        // Here, you should fetch the saved orgs list from Firestore and check
        // This is a placeholder – ideally, this should be updated when data is loaded
        return false;
    }


    @Override
    public int getItemCount() {
        return filteredOrganizations.size(); // Return size of filtered list
    }
    private void submitRequestToFirestore(String orgId, String requestText) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get the current user's ID
        String userId = user.getUid();

        // Reference to the organization document
        DocumentReference orgRef = FirebaseFirestore.getInstance()
                .collection("organizations")
                .document(orgId);

        // Add the request to the "needs" array in the organization document
        orgRef.update("needs", FieldValue.arrayUnion(requestText))
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Request added to organization needs", Toast.LENGTH_SHORT).show();
                    Log.d("Firestore", "Request added to needs array for orgId: " + orgId);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to add request to organization needs", Toast.LENGTH_SHORT).show();
                    Log.e("Firestore", "Error adding request to needs array", e);
                });
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
        Button btnRequest;

        public ViewHolder(View itemView) {
            super(itemView);
            orgName = itemView.findViewById(R.id.orgName);
            orgAddress = itemView.findViewById(R.id.orgAddress);
            orgDescription = itemView.findViewById(R.id.orgDescription);
            btnViewDetails = itemView.findViewById(R.id.btnViewDetails);
            btnSave = itemView.findViewById(R.id.btnSave);
            btnRequest = itemView.findViewById(R.id.btnRequest);
        }

    }}
