package com.example.beacon;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class EventDetailsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        TextView nameView = findViewById(R.id.eventName);
        TextView descView = findViewById(R.id.eventDesc);
        TextView dateView = findViewById(R.id.eventDate);
        TextView timeView = findViewById(R.id.eventTime);
        TextView locationView = findViewById(R.id.eventLocation);
        TextView volunteersView = findViewById(R.id.eventVolunteers);

        // Get extras from intent
        String name = getIntent().getStringExtra("name");
        String desc = getIntent().getStringExtra("description");
        String date = getIntent().getStringExtra("date");
        String time = getIntent().getStringExtra("time");
        String location = getIntent().getStringExtra("location");
        int volunteers = getIntent().getIntExtra("volunteersNeeded", 0);

        // Set text values
        nameView.setText(name);
        descView.setText(desc);
        dateView.setText("Date: " + date);
        timeView.setText("Time: " + time);
        locationView.setText("Location: " + location);

        // Conditionally show volunteers needed
        if (volunteers > 0) {
            volunteersView.setText("Volunteers Needed: " + volunteers);
        } else {
            volunteersView.setVisibility(View.GONE);
        }
    }
}
