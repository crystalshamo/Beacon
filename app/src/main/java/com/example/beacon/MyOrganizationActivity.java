package com.example.beacon;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.beacon.models.Event;
import com.example.beacon.models.Organization;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MyOrganizationActivity extends AppCompatActivity {

    private ListView listView;
    private List<Organization> orgList = new ArrayList<>();
    private OrganizationAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_organization);

        FloatingActionButton fab = findViewById(R.id.fabAddOrganization);

        listView = findViewById(R.id.listOrganizations);
        adapter = new OrganizationAdapter(this, orgList);
        listView.setAdapter(adapter);

        fab.setOnClickListener(view -> showAddOrganizationDialog());

        // Load organizations on activity start
        loadOrganizations();
    }
    private void showAddOrganizationDialog() {
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

            FirebaseFirestore db = FirebaseFirestore.getInstance();

            // Step 1: Create an Event object from the same org info
            Event defaultEvent = new Event(
                    "Welcome Event",
                    "Kickoff for " + name,
                    "TBD", "TBD", address
            );

            // Step 2: Save the default event to the "events" collection and get its ID
            db.collection("events")
                    .add(defaultEvent)
                    .addOnSuccessListener(eventRef -> {
                        String eventId = eventRef.getId(); // Get the event ID

                        // Step 3: Create organization and attach the event ID
                        Organization org = new Organization(name, address, website, description);
                        List<String> events = new ArrayList<>();
                        events.add(eventId); // Store the event ID
                        org.setEvents(events);
                        org.setNeeds(new ArrayList<>());

                        // Step 4: Save organization to Firestore
                        db.collection("organizations")
                                .add(org)
                                .addOnSuccessListener(documentReference -> {
                                    String newOrgId = documentReference.getId();
                                    org.setId(newOrgId);
                                    orgList.add(org);
                                    adapter.notifyDataSetChanged();
                                    Toast.makeText(this, "Organization added!", Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Failed to create organization", Toast.LENGTH_SHORT).show();
                                });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to create default event.", Toast.LENGTH_SHORT).show();
                    });
        });
    }
    private void loadOrganizations() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("organizations")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    orgList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Organization org = doc.toObject(Organization.class);
                        org.setId(doc.getId()); // Store the Firestore document ID
                        orgList.add(org);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load organizations.", Toast.LENGTH_SHORT).show();
                });
    }
}
