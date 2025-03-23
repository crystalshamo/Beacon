package com.example.beacon.ui.account;

import android.app.AlertDialog;
import android.content.Intent;
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
import androidx.fragment.app.Fragment;

import com.example.beacon.AllEventsActivity;
import com.example.beacon.MyOrganizationActivity;
import com.example.beacon.OrganizationAdapter;
import com.example.beacon.R;
import com.example.beacon.databinding.FragmentAccountBinding;
import com.example.beacon.models.Event;
import com.google.firebase.auth.FirebaseAuth;
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

public class AccountFragment extends Fragment {

    private FragmentAccountBinding binding;
    private Calendar currentCalendar;
    private CalendarAdapter calendarAdapter;
    private List<Event> events = new ArrayList<>();
    private Map<String, List<Event>> eventMap = new HashMap<>();
    private List<Event> currentEventList;
    private int currentEventIndex = 0;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth auth = FirebaseAuth.getInstance();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAccountBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        currentCalendar = Calendar.getInstance();

        // Set up calendar UI
        GridView calendarGridView = binding.calendarGridView;
        Button prevMonthButton = binding.prevMonthButton;
        Button nextMonthButton = binding.nextMonthButton;
        Button prevEventButton = binding.prevEventButton;
        Button nextEventButton = binding.nextEventButton;

        Button btnMyOrganization = binding.btnMyOrganization;
        btnMyOrganization.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MyOrganizationActivity.class);
            startActivity(intent);
        });


        calendarAdapter = new CalendarAdapter();
        calendarGridView.setAdapter(calendarAdapter);

        updateMonthYearHeader();
        loadOrganizations();

        calendarGridView.setOnItemClickListener((parent, view1, position, id) -> {
            String selectedDate = calendarAdapter.getDateAtPosition(position);
            Log.d("CalendarClick", "Selected date: " + selectedDate);

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
    private void showEventDetails(int index) {
        if (currentEventList != null && !currentEventList.isEmpty() && index >= 0 && index < currentEventList.size()) {
            Event event = currentEventList.get(index);

            binding.eventDetailsTextView.setText("Organization: " + event.getOrgName() + "\n"
                    + "Event: " + event.getName() + "\n"
                    + "Date: " + event.getDate() + "\n"
                    + "Time: " + event.getTime() + "\n"
                    + "Location: " + event.getLocation() + "\n"
                    + "Description: " + event.getDescription() + "\n"
                    );
        }
    }


    // Update header to show month and year
    private void updateMonthYearHeader() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        binding.monthYearTextView.setText(sdf.format(currentCalendar.getTime()));
    }

    // Load organizations and filter events based on savedOrgs
    private void loadOrganizations() {
        String currentUserId = auth.getCurrentUser().getUid();

        db.collection("users").document(currentUserId)
                .get()
                .addOnSuccessListener(userDocument -> {
                    if (userDocument.exists()) {
                        // Get the savedOrgs array from the user's document
                        List<String> savedOrgs = (List<String>) userDocument.get("savedOrgs");
                        if (savedOrgs == null || savedOrgs.isEmpty()) {
                            // Handle the case where savedOrgs is empty or null
                            events.clear();
                            eventMap.clear();
                            calendarAdapter.notifyDataSetChanged();
                            Toast.makeText(getContext(), "No organizations saved", Toast.LENGTH_SHORT).show();
                            return; // Exit the method early
                        }

                        // Fetch organizations based on savedOrgs
                        db.collection("organizations")
                                .whereIn("__name__", savedOrgs) // Fetch organizations with IDs in savedOrgs
                                .get()
                                .addOnCompleteListener(orgTask -> {
                                    if (orgTask.isSuccessful() && orgTask.getResult() != null) {
                                        events.clear();
                                        eventMap.clear();

                                        // Iterate through each organization
                                        for (QueryDocumentSnapshot orgDocument : orgTask.getResult()) {
                                            // Get the events array for the organization
                                            List<String> eventIds = (List<String>) orgDocument.get("events");

                                            if (eventIds != null && !eventIds.isEmpty()) {
                                                // Fetch events for each event ID
                                                for (String eventId : eventIds) {
                                                    db.collection("events").document(eventId)
                                                            .get()
                                                            .addOnSuccessListener(eventDocument -> {
                                                                if (eventDocument.exists()) {
                                                                    // Convert the event document to an Event object
                                                                    Event event = eventDocument.toObject(Event.class);
                                                                    if (event != null) {
                                                                        events.add(event);

                                                                        // Format the event date for the calendar
                                                                        String eventDate = formatDateForCalendar(event.getDate());

                                                                        // Map the event to its date
                                                                        if (!eventMap.containsKey(eventDate)) {
                                                                            eventMap.put(eventDate, new ArrayList<>());
                                                                        }
                                                                        eventMap.get(eventDate).add(event);

                                                                        // Log the event and its date
                                                                        Log.d("EventMapping", "Event: " + event.getName() + " mapped to date: " + eventDate);
                                                                    }
                                                                }

                                                                // Notify the adapter after all events are loaded
                                                                calendarAdapter.notifyDataSetChanged();
                                                            })
                                                            .addOnFailureListener(e -> {
                                                                Log.e("AccountFragment", "Error fetching event details", e);
                                                            });
                                                }
                                            }
                                        }
                                    } else {
                                        Log.e("AccountFragment", "Error fetching organizations", orgTask.getException());
                                        Toast.makeText(getContext(), "Failed to load organizations", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Log.e("AccountFragment", "User document does not exist");
                        Toast.makeText(getContext(), "User data not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("AccountFragment", "Error fetching user data", e);
                    Toast.makeText(getContext(), "Failed to fetch user data", Toast.LENGTH_SHORT).show();
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