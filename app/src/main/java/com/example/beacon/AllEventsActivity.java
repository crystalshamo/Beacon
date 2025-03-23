package com.example.beacon;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.beacon.databinding.ActivityAllEventsBinding;
import com.example.beacon.models.Event;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AllEventsActivity extends AppCompatActivity {

    private ActivityAllEventsBinding binding;
    private Calendar currentCalendar;
    private CalendarAdapter calendarAdapter;
    private List<Event> events = new ArrayList<>();
    private Map<String, List<Event>> eventMap = new HashMap<>();
    private List<Event> currentEventList;
    private int currentEventIndex = 0;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAllEventsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        currentCalendar = Calendar.getInstance();

        // Set up calendar UI
        GridView calendarGridView = binding.calendarGridView;
        Button prevMonthButton = binding.prevMonthButton;
        Button nextMonthButton = binding.nextMonthButton;
        Button prevEventButton = binding.prevEventButton;
        Button nextEventButton = binding.nextEventButton;

        calendarAdapter = new CalendarAdapter();
        calendarGridView.setAdapter(calendarAdapter);

        updateMonthYearHeader();
        fetchEventsFromFirestore();

        calendarGridView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedDate = calendarAdapter.getDateAtPosition(position);
            List<Event> eventsForDate = eventMap.get(selectedDate);

            if (eventsForDate == null || eventsForDate.isEmpty()) {
                binding.eventDetailsTextView.setText("No events for this date");
                prevEventButton.setVisibility(View.GONE);
                nextEventButton.setVisibility(View.GONE);
            } else {
                currentEventIndex = 0;
                currentEventList = eventsForDate;
                showEventDetails(currentEventIndex);
                prevEventButton.setVisibility(currentEventList.size() > 1 ? View.VISIBLE : View.GONE);
                nextEventButton.setVisibility(currentEventList.size() > 1 ? View.VISIBLE : View.GONE);
            }
        });

        prevEventButton.setOnClickListener(v -> {
            if (currentEventIndex > 0) {
                currentEventIndex--;
                showEventDetails(currentEventIndex);
            }
        });

        nextEventButton.setOnClickListener(v -> {
            if (currentEventIndex < currentEventList.size() - 1) {
                currentEventIndex++;
                showEventDetails(currentEventIndex);
            }
        });

        prevMonthButton.setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, -1);
            updateMonthYearHeader();
            calendarAdapter.updateCalendar();
        });

        nextMonthButton.setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, 1);
            updateMonthYearHeader();
            calendarAdapter.updateCalendar();
        });
    }

    // Show event details and add volunteer button
    private void showEventDetails(int index) {
        if (currentEventList != null && !currentEventList.isEmpty() && index >= 0 && index < currentEventList.size()) {
            Event event = currentEventList.get(index);

            binding.eventDetailsTextView.setText("Organization: " + event.getOrgName() + "\n"
                    + "Event: " + event.getName() + "\n"
                    + "Date: " + event.getDate() + "\n"
                    + "Time: " + event.getTime() + "\n"
                    + "Location: " + event.getLocation() + "\n"
                    + "Description: " + event.getDescription() + "\n"
                    + "Volunteers Needed: " + event.getVolunteersNeeded());

            // Remove previous volunteer button if any
            ViewGroup parent = (ViewGroup) binding.eventDetailsTextView.getParent();
            for (int i = 0; i < parent.getChildCount(); i++) {
                View child = parent.getChildAt(i);
                if (child instanceof Button && ((Button) child).getText().equals("Volunteer")) {
                    parent.removeView(child);
                    break;
                }
            }

            // If volunteers needed, add the button
            if (event.getVolunteersNeeded() > 0) {
                Button volunteerButton = new Button(this);
                volunteerButton.setText("Volunteer");
                parent.addView(volunteerButton);

                volunteerButton.setOnClickListener(v -> showVolunteerDialog(event));
            }
        }
    }

    // Show dialog to sign up as volunteer
    private void showVolunteerDialog(Event event) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Volunteer for " + event.getName());

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_volunteer, null);
        EditText nameInput = view.findViewById(R.id.inputName);
        EditText phoneInput = view.findViewById(R.id.inputPhone);
        builder.setView(view);

        builder.setPositiveButton("Submit", (dialog, which) -> {
            String name = nameInput.getText().toString().trim();
            String phone = phoneInput.getText().toString().trim();

            if (!name.isEmpty() && !phone.isEmpty()) {
                String volunteerEntry = name + " - " + phone;

                db.collection("events")
                        .whereEqualTo("name", event.getName())
                        .get()
                        .addOnSuccessListener(snapshot -> {
                            if (!snapshot.isEmpty()) {
                                DocumentReference ref = snapshot.getDocuments().get(0).getReference();
                                ref.update("volunteers", FieldValue.arrayUnion(volunteerEntry));
                                ref.update("volunteersNeeded", event.getVolunteersNeeded() - 1);
                                Toast.makeText(this, "Thanks for signing up!", Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                Toast.makeText(this, "Please enter both fields", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    // Update header to show month and year
    private void updateMonthYearHeader() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        binding.monthYearTextView.setText(sdf.format(currentCalendar.getTime()));
    }

    // Load events from Firestore and map by date
    private void fetchEventsFromFirestore() {
        db.collection("events")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        events.clear();
                        eventMap.clear();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Event event = document.toObject(Event.class);
                            events.add(event);

                            String eventDate = formatDateForCalendar(event.getDate());

                            if (!eventMap.containsKey(eventDate)) {
                                eventMap.put(eventDate, new ArrayList<>());
                            }
                            eventMap.get(eventDate).add(event);
                        }

                        calendarAdapter.notifyDataSetChanged();
                    } else {
                        Log.e("Firestore", "Error fetching events", task.getException());
                    }
                });
    }

    private String formatDateForCalendar(String date) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("M/d/yyyy", Locale.getDefault());
            Date parsedDate = sdf.parse(date);
            SimpleDateFormat outputFormat = new SimpleDateFormat("MM-dd-yyyy", Locale.getDefault());
            return outputFormat.format(parsedDate);
        } catch (Exception e) {
            Log.e("DateParse", "Error parsing date", e);
            return null;
        }
    }

    // Calendar view adapter
    private class CalendarAdapter extends BaseAdapter {
        private final List<String> dates = new ArrayList<>();

        public CalendarAdapter() {
            updateCalendar();
        }

        public void updateCalendar() {
            dates.clear();
            Calendar calendar = (Calendar) currentCalendar.clone();
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            int firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            for (int i = 1; i < firstDayOfWeek; i++) {
                dates.add("");
            }
            int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
            for (int i = 1; i <= daysInMonth; i++) {
                dates.add(String.valueOf(i));
            }
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return dates.size();
        }

        @Override
        public String getItem(int position) {
            return dates.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.calendar_day_layout, parent, false);
            }

            TextView calendarDayText = convertView.findViewById(R.id.calendarDayText);
            String date = getItem(position);
            calendarDayText.setText(date);

            if (!date.isEmpty()) {
                String formattedDate = String.format(Locale.getDefault(), "%02d-%02d-%04d",
                        currentCalendar.get(Calendar.MONTH) + 1, Integer.parseInt(date), currentCalendar.get(Calendar.YEAR));
                if (eventMap.containsKey(formattedDate)) {
                    convertView.setBackgroundResource(R.drawable.date_background_event);
                    calendarDayText.setTextColor(Color.WHITE);
                } else {
                    convertView.setBackgroundResource(R.drawable.date_background);
                    calendarDayText.setTextColor(Color.BLACK);
                }
            } else {
                convertView.setBackgroundResource(R.drawable.date_background);
                calendarDayText.setTextColor(Color.BLACK);
            }

            return convertView;
        }

        public String getDateAtPosition(int position) {
            String date = getItem(position);
            if (date.isEmpty()) return null;
            return String.format(Locale.getDefault(), "%02d-%02d-%04d",
                    currentCalendar.get(Calendar.MONTH) + 1, Integer.parseInt(date), currentCalendar.get(Calendar.YEAR));
        }
    }
}
