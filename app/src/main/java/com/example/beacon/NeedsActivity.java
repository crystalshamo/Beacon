package com.example.beacon;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class NeedsActivity extends AppCompatActivity {

    private String orgId;
    private String orgName;
    private ArrayList<String> needsList = new ArrayList<>();
    private NeedsAdapter adapter;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DocumentReference orgRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_needs);

        // Get data from intent
        orgId = getIntent().getStringExtra("orgId");
        orgName = getIntent().getStringExtra("orgName");

        if (orgId == null || orgId.isEmpty()) {
            Toast.makeText(this, "Organization ID is missing!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        orgRef = db.collection("organizations").document(orgId);

        // UI References
        TextView title = findViewById(R.id.needsTitle);
        EditText inputNeed = findViewById(R.id.inputNeed);
        Button btnAddNeed = findViewById(R.id.btnAddNeed);
        ListView listView = findViewById(R.id.needsListView);

        title.setText(orgName + " Needs");

        adapter = new NeedsAdapter(this, needsList, this::removeNeed);
        listView.setAdapter(adapter);

        // Load needs from Firestore
        loadNeeds();

        // Add button click
        btnAddNeed.setOnClickListener(v -> {
            String newNeed = inputNeed.getText().toString().trim();
            if (!newNeed.isEmpty()) {
                addNeed(newNeed);
                inputNeed.setText("");
            } else {
                Toast.makeText(this, "Need cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadNeeds() {
        orgRef.get().addOnSuccessListener(doc -> {
            needsList.clear();
            needsList.addAll((ArrayList<String>) doc.get("needs"));
            adapter.notifyDataSetChanged();
        });
    }

    private void removeNeed(String need) {
        orgRef.update("needs", FieldValue.arrayRemove(need))
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Removed: " + need, Toast.LENGTH_SHORT).show();
                    loadNeeds();
                });
    }

    private void addNeed(String newNeed) {
        orgRef.update("needs", FieldValue.arrayUnion(newNeed))
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Need added!", Toast.LENGTH_SHORT).show();
                    loadNeeds();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to add need.", Toast.LENGTH_SHORT).show());
    }
}
