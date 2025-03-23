package com.example.beacon;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.beacon.models.Organization;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MyOrganizationActivity extends AppCompatActivity {
    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseUser user = auth.getCurrentUser();

    private ListView listView;
    private List<Organization> orgList = new ArrayList<>();
    private MyOrganizationAdapter2 adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_organization);

        FloatingActionButton fab = findViewById(R.id.fabAddOrganization);

        listView = findViewById(R.id.listOrganizations);
        adapter = new MyOrganizationAdapter2(this, orgList);
        listView.setAdapter(adapter);

        fab.setOnClickListener(view -> showAddOrganizationDialog());

        // Load organizations on activity start
        loadOrganizations();
    }

    private void showAddOrganizationDialog() {
        if (user != null) {
            String userId = user.getUid(); // Get the unique user ID
            Log.d("FirebaseAuth", "Logged-in user ID: " + userId);
        } else {
            Log.d("FirebaseAuth", "No user is logged in");
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_organization, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialog.show();

        EditText editName = dialogView.findViewById(R.id.editName);
        EditText editDescription = dialogView.findViewById(R.id.editDescription);
        EditText editAddress = dialogView.findViewById(R.id.editAddress);
        EditText editWebsite = dialogView.findViewById(R.id.editWebsite);
        Button btnSubmit = dialogView.findViewById(R.id.btnSubmitOrg);

        btnSubmit.setOnClickListener(v -> {
            String name = editName.getText().toString().trim();
            String description = editDescription.getText().toString().trim();
            String address = editAddress.getText().toString().trim();
            String website = editWebsite.getText().toString().trim();

            if (name.isEmpty() || description.isEmpty() || address.isEmpty() || website.isEmpty()) {
                Toast.makeText(this, "All fields are required.", Toast.LENGTH_SHORT).show();
                return;
            }

            Geocoder geocoder = new Geocoder(MyOrganizationActivity.this);
            try {
                List<Address> addresses = geocoder.getFromLocationName(address, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    Address location = addresses.get(0);
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();

                    FirebaseFirestore db = FirebaseFirestore.getInstance();

                    String orgId = db.collection("organizations").document().getId();
                    String userId = user.getUid();
                    Organization org = new Organization(name, address, website, description, latitude, longitude, userId, orgId);
                    org.setEvents(new ArrayList<>());
                    org.setNeeds(new ArrayList<>());

                    db.collection("organizations")
                            .document(orgId)
                            .set(org)
                            .addOnSuccessListener(documentRef -> {

                                orgList.add(org);
                                adapter.notifyDataSetChanged();
                                Toast.makeText(this, "Organization added!", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Failed to create organization", Toast.LENGTH_SHORT).show();
                            });
                } else {
                    Toast.makeText(this, "Address not found.", Toast.LENGTH_SHORT).show();
                }
            } catch (IOException e) {
                Toast.makeText(this, "Geocoding failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadOrganizations() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        if (user != null) {
            String userId = user.getUid();

            db.collection("organizations")
                    .whereEqualTo("userId", userId)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        orgList.clear();
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            Organization org = doc.toObject(Organization.class);
                            org.setId(doc.getId());
                            orgList.add(org);
                        }
                        adapter.notifyDataSetChanged();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to load your organizations.", Toast.LENGTH_SHORT).show();
                    });
        }
    }
}
