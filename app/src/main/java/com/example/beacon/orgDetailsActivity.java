package com.example.beacon;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.beacon.models.Event;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class orgDetailsActivity extends AppCompatActivity {

    private TextView orgName, orgAddress, orgWebsite, orgDescription, orgNeeds, orgVolunteerOpportunities;
    private FirebaseFirestore db;
    private LinearLayout eventsContainer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_org_details);

        // Initialize views
        orgName = findViewById(R.id.orgName);
        orgAddress = findViewById(R.id.orgAddress);
        orgWebsite = findViewById(R.id.orgWebsite);
        orgDescription = findViewById(R.id.orgDescription);
        orgNeeds = findViewById(R.id.orgNeeds);
        orgVolunteerOpportunities = findViewById(R.id.orgVolunteerOpportunities);
        eventsContainer = findViewById(R.id.eventsContainer);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Get organization ID from the intent
        String orgId = getIntent().getStringExtra("orgId");
        if (orgId == null) {
            Toast.makeText(this, "Organization ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Fetch organization details from Firestore
        fetchOrganizationDetails(orgId);
    }

    private void fetchOrganizationDetails(String orgId) {
        db.collection("organizations")
                .document(orgId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            // Set organization details
                            orgName.setText(document.getString("name"));
                            orgAddress.setText("Address: " + document.getString("address"));
                            orgWebsite.setText("Website: " + document.getString("website"));
                            orgDescription.setText("Description: " + document.getString("description"));

                            // Fetch and display events, needs, and volunteer opportunities
                            List<String> eventIds = (List<String>) document.get("events");  // Assuming "events" are stored as IDs
                            List<String> needs = (List<String>) document.get("needs");
                            List<String> volunteerOpportunities = (List<String>) document.get("volunteerOpportunities");

                            if (eventIds != null) {
                                fetchEventDetails(eventIds); // Function to fetch event details using the event IDs
                            }
                            if (needs != null) {
                                orgNeeds.setText("Needs: " + String.join(", ", needs));
                            }
                            if (volunteerOpportunities != null) {
                                orgVolunteerOpportunities.setText("Volunteer Opportunities: " + String.join(", ", volunteerOpportunities));
                            }
                        } else {
                            Toast.makeText(this, "Organization not found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e("OrgDetailsActivity", "Error fetching organization details", task.getException());
                        Toast.makeText(this, "Failed to load organization details", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchEventDetails(List<String> eventIds) {
        for (String eventId : eventIds) {
            db.collection("events")
                    .document(eventId)
                    .get()
                    .addOnCompleteListener(eventTask -> {
                        if (eventTask.isSuccessful()) {
                            DocumentSnapshot eventDocument = eventTask.getResult();
                            if (eventDocument.exists()) {
                                Event event = eventDocument.toObject(Event.class); // Convert Firestore document to Event object
                                if (event != null) {
                                    LayoutInflater inflater = LayoutInflater.from(this);
                                    View eventView = inflater.inflate(R.layout.event_item, eventsContainer, false);

                                    TextView eventName = eventView.findViewById(R.id.eventName);
                                    TextView eventDescription = eventView.findViewById(R.id.eventDescription);
                                    TextView eventDate = eventView.findViewById(R.id.eventDate);

                                    // Set event details
                                    eventName.setText(event.getName());
                                    eventDescription.setText(event.getDescription());
                                    eventDate.setText("Date: " + event.getDate());

                                    // Add the whole event layout to the container
                                    eventsContainer.addView(eventView);
                                }
                            }
                        } else {
                            Log.e("OrgDetailsActivity", "Error fetching event details", eventTask.getException());
                        }
                    });
        }
    }
}