package com.example.beacon;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.beacon.models.Organization;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class MyOrganizationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_organization);

        FloatingActionButton fab = findViewById(R.id.fabAddOrganization);
        fab.setOnClickListener(view -> showAddOrganizationDialog());
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
        Button btnSubmit = dialogView.findViewById(R.id.btnSubmitOrg);

        btnSubmit.setOnClickListener(v -> {
            String name = editName.getText().toString().trim();
            String description = editDescription.getText().toString().trim();
            String address = editAddress.getText().toString().trim();

            if (name.isEmpty() || description.isEmpty() || address.isEmpty()) {
                Toast.makeText(this, "All fields are required.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create org object with only name, address, and description
            Organization org = new Organization(name, address, null, description); // website is null
            org.setEvents(new ArrayList<>());
            org.setNeeds(new ArrayList<>());

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("organizations")
                    .add(org)
                    .addOnSuccessListener(doc -> {
                        Toast.makeText(this, "Organization added!", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to add.", Toast.LENGTH_SHORT).show();
                    });
        });
    }
}
