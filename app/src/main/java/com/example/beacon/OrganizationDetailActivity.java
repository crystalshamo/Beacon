package com.example.beacon;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class OrganizationDetailActivity extends AppCompatActivity {

    private TextView name, address, description, website;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organization_detail);

        name = findViewById(R.id.detailName);
        address = findViewById(R.id.detailAddress);
        description = findViewById(R.id.detailDescription);
        website = findViewById(R.id.detailWebsite);

        Intent intent = getIntent();
        name.setText(intent.getStringExtra("name"));
        address.setText(intent.getStringExtra("address"));
        description.setText(intent.getStringExtra("description"));
        website.setText(intent.getStringExtra("website"));
    }
}