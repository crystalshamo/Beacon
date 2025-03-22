package com.example.beacon;

import android.os.Bundle;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.example.beacon.databinding.ActivityMainBinding;
import java.util.List;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Arrays;
import java.util.List;
public class MainActivity extends AppCompatActivity {


    private ActivityMainBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        insertOrganizationData();


        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);
    }






    private void insertOrganizationData() {
        // Get a reference to Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();


        // Create an organization object with empty arrays
        // Organization org = new Organization(
        //                "Detroit Community Fridge",
        //                "1812 Field St, Detroit, MI 48214",
        //                "https://detfridge.com/",
        //                "Helping fight hunger in the community."
        //        );
        //
        //
        //        // Insert the organization into Firestore
        //        db.collection("organizations")
        //                .document("org1")
        //                .set(org)
        //                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Document added successfully"))
        //                .addOnFailureListener(e -> Log.e("Firestore", "Error adding document", e));
        //    }
    }
}
