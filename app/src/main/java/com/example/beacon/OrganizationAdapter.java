package com.example.beacon;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.beacon.R;
import com.example.beacon.models.Organization;

import java.util.ArrayList;
import java.util.List;

public class OrganizationAdapter extends RecyclerView.Adapter<OrganizationAdapter.ViewHolder> {

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
        View view = LayoutInflater.from(context).inflate(R.layout.item_organization, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Organization org = filteredOrganizations.get(position); // Use filtered list for binding
        holder.orgName.setText(org.getName());
        holder.orgAddress.setText(org.getAddress());
        holder.orgDescription.setText(org.getDescription());

        holder.btnViewDetails.setOnClickListener(v -> {
            Intent intent = new Intent(context, OrganizationDetailActivity.class);
            intent.putExtra("name", org.getName());
            intent.putExtra("address", org.getAddress());
            intent.putExtra("description", org.getDescription());
            intent.putExtra("website", org.getWebsite());
            context.startActivity(intent);
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
        Button btnViewDetails;

        public ViewHolder(View itemView) {
            super(itemView);
            orgName = itemView.findViewById(R.id.orgName);
            orgAddress = itemView.findViewById(R.id.orgAddress);
            orgDescription = itemView.findViewById(R.id.orgDescription);
            btnViewDetails = itemView.findViewById(R.id.btnViewDetails);
        }
    }
}