package com.example.beacon.ui.explore;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.beacon.EventDecorator;
import com.example.beacon.databinding.FragmentExploreBinding;
import com.example.beacon.models.Event;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ExploreFragment extends Fragment {

    private FragmentExploreBinding binding;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private List<Event> events = new ArrayList<>(); // List to hold all events

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentExploreBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        MaterialCalendarView calendarView = binding.calendarView;
        TextView eventDetailsTextView = binding.eventDetailsTextView;

        // Fetch events from Firestore
        fetchEventsFromFirestore();

        // Set a listener for date selection
        calendarView.setOnDateChangedListener((widget, date, selected) -> {
            String selectedDate = String.format(Locale.getDefault(), "%02d-%02d-%04d", date.getDay(), date.getMonth() + 1, date.getYear());

            // Filter events for the selected date
            List<String> eventNames = getEventsForDate(selectedDate);

            // Display event names
            if (eventNames.isEmpty()) {
                eventDetailsTextView.setText("No events for this date");
            } else {
                StringBuilder eventsText = new StringBuilder();
                for (String eventName : eventNames) {
                    eventsText.append(eventName).append("\n");
                }
                eventDetailsTextView.setText(eventsText.toString());
            }
        });

        return root;
    }

    private void fetchEventsFromFirestore() {
        db.collection("events")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        events.clear(); // Clear the list before adding new events
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Event event = document.toObject(Event.class);
                            events.add(event);
                            Log.d("Firestore", "Event fetched: " + event.getName());
                        }
                        // After fetching events, update the CalendarView
                        updateCalendarWithEvents();
                    } else {
                        Log.e("Firestore", "Error fetching events", task.getException());
                    }
                });
    }

    private void updateCalendarWithEvents() {
        MaterialCalendarView calendarView = binding.calendarView;

        // Clear previous decorations
        calendarView.removeDecorators();

        // Add a decorator for each event date
        for (Event event : events) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy", Locale.getDefault());
                Date eventDate = sdf.parse(event.getDate());
                if (eventDate != null) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(eventDate);
                    CalendarDay day = CalendarDay.from(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));

                    // Add a decorator to mark the date with a dot
                    calendarView.addDecorator(new EventDecorator(Color.RED, day));
                }
            } catch (ParseException e) {
                Log.e("DateParse", "Error parsing date", e);
            }
        }
    }

    private List<String> getEventsForDate(String selectedDate) {
        List<String> eventNames = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

        for (Event event : events) {
            try {
                Date eventDate = sdf.parse(event.getDate());
                Date selDate = sdf.parse(selectedDate);

                if (eventDate != null && eventDate.equals(selDate)) {
                    eventNames.add(event.getName());
                }
            } catch (ParseException e) {
                Log.e("DateParse", "Error parsing date", e);
            }
        }

        return eventNames;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}