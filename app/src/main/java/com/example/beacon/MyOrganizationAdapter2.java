package com.example.beacon;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.beacon.models.Event;
import com.example.beacon.models.Organization;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MyOrganizationAdapter2 extends ArrayAdapter<Organization> {

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    public MyOrganizationAdapter2(Context context, List<Organization> orgs) {
        super(context, 0, orgs);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Organization org = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_organization, parent, false);
        }

        ((TextView) convertView.findViewById(R.id.orgName)).setText(org.getName());
        ((TextView) convertView.findViewById(R.id.orgAddress)).setText(org.getAddress());
        ((TextView) convertView.findViewById(R.id.orgWebsite)).setText(org.getWebsite());
        ((TextView) convertView.findViewById(R.id.orgDescription)).setText(org.getDescription());

        Button btnEdit = convertView.findViewById(R.id.btnViewDetails);
        Button btnNeeds = convertView.findViewById(R.id.btnNeeds);
        Button btnEvents = convertView.findViewById(R.id.btnEvents);

        btnEdit.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_organization, null);
            builder.setView(dialogView);
            AlertDialog dialog = builder.create();
            dialog.show();

            EditText editName = dialogView.findViewById(R.id.editName);
            EditText editAddress = dialogView.findViewById(R.id.editAddress);
            EditText editWebsite = dialogView.findViewById(R.id.editWebsite);
            EditText editDescription = dialogView.findViewById(R.id.editDescription);
            Button btnSubmit = dialogView.findViewById(R.id.btnSubmitOrg);

            editName.setText(org.getName());
            editAddress.setText(org.getAddress());
            editWebsite.setText(org.getWebsite());
            editDescription.setText(org.getDescription());

            btnSubmit.setOnClickListener(submitView -> {
                String newName = editName.getText().toString().trim();
                String newAddress = editAddress.getText().toString().trim();
                String newWebsite = editWebsite.getText().toString().trim();
                String newDescription = editDescription.getText().toString().trim();

                if (newName.isEmpty() || newAddress.isEmpty() || newWebsite.isEmpty() || newDescription.isEmpty()) {
                    Toast.makeText(getContext(), "All fields are required.", Toast.LENGTH_SHORT).show();
                    return;
                }

                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("organizations")
                        .whereEqualTo("name", org.getName())
                        .get()
                        .addOnSuccessListener(snapshot -> {
                            if (!snapshot.isEmpty()) {
                                snapshot.getDocuments().get(0).getReference()
                                        .update("name", newName,
                                                "address", newAddress,
                                                "website", newWebsite,
                                                "description", newDescription)
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(getContext(), "Organization updated!", Toast.LENGTH_SHORT).show();
                                            dialog.dismiss();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(getContext(), "Update failed.", Toast.LENGTH_SHORT).show();
                                        });
                            }
                        });
            });
        });

        btnNeeds.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), NeedsActivity.class);
            intent.putExtra("orgId", org.getId());
            intent.putExtra("orgName", org.getName());
            getContext().startActivity(intent);
        });

        btnEvents.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_events, null);
            builder.setView(dialogView);
            AlertDialog dialog = builder.create();
            dialog.show();

            String orgId = org.getId();
            ListView eventListView = dialogView.findViewById(R.id.eventListView);
            EditText nameInput = dialogView.findViewById(R.id.inputEventName);
            EditText descInput = dialogView.findViewById(R.id.inputEventDesc);
            EditText timeInput = dialogView.findViewById(R.id.inputEventTime);
            EditText locInput = dialogView.findViewById(R.id.inputEventLocation);
            EditText inputEventDate = dialogView.findViewById(R.id.inputEventDate);
            Switch switchVolunteers = dialogView.findViewById(R.id.switchVolunteers);
            Button btnAdd = dialogView.findViewById(R.id.btnAddEvent);

            final int[] volunteersNeeded = {0};

            inputEventDate.setFocusable(false);
            inputEventDate.setClickable(true);

            inputEventDate.setOnClickListener(view -> {
                final Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                        (view1, selectedYear, selectedMonth, selectedDay) -> {
                            String date = (selectedMonth + 1) + "/" + selectedDay + "/" + selectedYear;
                            inputEventDate.setText(date);
                        }, year, month, day);

                datePickerDialog.show();
            });

            switchVolunteers.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    AlertDialog.Builder inputBuilder = new AlertDialog.Builder(getContext());
                    inputBuilder.setTitle("Number of Volunteers Needed");

                    EditText input = new EditText(getContext());
                    input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
                    input.setHint("Enter a number");

                    inputBuilder.setView(input);
                    inputBuilder.setPositiveButton("OK", (dialogInterface, i) -> {
                        String value = input.getText().toString().trim();
                        if (!value.isEmpty()) {
                            try {
                                volunteersNeeded[0] = Integer.parseInt(value);
                                Toast.makeText(getContext(), "Volunteers Needed: " + volunteersNeeded[0], Toast.LENGTH_SHORT).show();
                            } catch (NumberFormatException e) {
                                Toast.makeText(getContext(), "Invalid number entered", Toast.LENGTH_SHORT).show();
                                switchVolunteers.setChecked(false);
                            }
                        } else {
                            switchVolunteers.setChecked(false);
                        }
                    });
                    inputBuilder.setNegativeButton("Cancel", (dialogInterface, i) -> switchVolunteers.setChecked(false));
                    inputBuilder.show();
                } else {
                    volunteersNeeded[0] = 0;
                }
            });

            ArrayList<Event> events = new ArrayList<>();
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, new ArrayList<>());
            eventListView.setAdapter(adapter);

            db.collection("organizations")
                    .document(org.getId())
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        List<String> eventIds = (List<String>) snapshot.get("events");
                        if (eventIds != null) {
                            for (String eventId : eventIds) {
                                db.collection("events").document(eventId).get()
                                        .addOnSuccessListener(eventDoc -> {
                                            if (eventDoc.exists()) {
                                                String eventName = eventDoc.getString("name");
                                                String description = eventDoc.getString("description");
                                                String date = eventDoc.getString("date");
                                                String time = eventDoc.getString("time");
                                                String location = eventDoc.getString("location");

                                                Event event = new Event(eventName, description, date, time, location);
                                                // Optional: Load volunteer count from Firestore if stored
                                                events.add(event);
                                                adapter.add(eventName);
                                                adapter.notifyDataSetChanged();
                                            }
                                        });
                            }
                        }
                    });

            btnAdd.setOnClickListener(view1 -> {
                String name = nameInput.getText().toString().trim();
                String desc = descInput.getText().toString().trim();
                String date = inputEventDate.getText().toString().trim();
                String time = timeInput.getText().toString().trim();
                String loc = locInput.getText().toString().trim();

                if (name.isEmpty() || desc.isEmpty() || date.isEmpty() || time.isEmpty() || loc.isEmpty()) {
                    Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                Event event = new Event(name, desc, date, time, loc);
                event.setVolunteersNeeded(volunteersNeeded[0]);

                db.collection("events")
                        .add(event)
                        .addOnSuccessListener(documentReference -> {
                            String eventId = documentReference.getId();

                            db.collection("organizations")
                                    .document(orgId)
                                    .update("events", FieldValue.arrayUnion(eventId))
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(getContext(), "Event added to organization!", Toast.LENGTH_SHORT).show();
                                        events.add(event);
                                        adapter.add(name);
                                        adapter.notifyDataSetChanged();
                                        nameInput.setText("");
                                        descInput.setText("");
                                        inputEventDate.setText("");
                                        timeInput.setText("");
                                        locInput.setText("");
                                        switchVolunteers.setChecked(false);
                                        volunteersNeeded[0] = 0;
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(getContext(), "Failed to update organization events.", Toast.LENGTH_SHORT).show();
                                    });
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(getContext(), "Failed to add event.", Toast.LENGTH_SHORT).show();
                        });
            });

            eventListView.setOnItemClickListener((adapterView, view1, eventPosition, id) -> {
                Event selectedEvent = events.get(eventPosition);
                Intent intent = new Intent(getContext(), EventDetailsActivity.class);
                intent.putExtra("name", selectedEvent.getName());
                intent.putExtra("description", selectedEvent.getDescription());
                intent.putExtra("date", selectedEvent.getDate());
                intent.putExtra("time", selectedEvent.getTime());
                intent.putExtra("location", selectedEvent.getLocation());
                intent.putExtra("volunteersNeeded", selectedEvent.getVolunteersNeeded());
                getContext().startActivity(intent);
            });
        });

        return convertView;
    }
}