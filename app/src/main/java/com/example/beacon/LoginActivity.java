package com.example.beacon;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

public class LoginActivity extends AppCompatActivity {

    EditText loginUsername, loginPassword;
    Button loginButton;
    TextView signupRedirectText, forgotPasswordText;
    FirebaseAuth auth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginUsername = findViewById(R.id.login_username);
        loginPassword = findViewById(R.id.login_password);
        loginButton = findViewById(R.id.login_button);
        signupRedirectText = findViewById(R.id.signupRedirectText);
        forgotPasswordText = findViewById(R.id.resetpassword);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        loginButton.setOnClickListener(view -> {
            if (!validateUsername() || !validatePassword()) {
                return;
            } else {
                loginUser();
            }
        });

        signupRedirectText.setOnClickListener(view -> {
            startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
        });

        forgotPasswordText.setOnClickListener(view -> {
            resetPassword();
        });
    }

    public Boolean validateUsername() {
        String val = loginUsername.getText().toString();
        if (val.isEmpty()) {
            loginUsername.setError("Username cannot be empty");
            return false;
        } else {
            loginUsername.setError(null);
            return true;
        }
    }

    public Boolean validatePassword() {
        String val = loginPassword.getText().toString();
        if (val.isEmpty()) {
            loginPassword.setError("Password cannot be empty");
            return false;
        } else {
            loginPassword.setError(null);
            return true;
        }
    }

    // 🔥 Secure authentication using FirebaseAuth
    public void loginUser() {
        String username = loginUsername.getText().toString().trim();
        String password = loginPassword.getText().toString().trim();

        // Fetch the user document using the username field, not as the document ID
        db.collection("users")
                .whereEqualTo("username", username)  // Query where the username matches
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Get the first document (assuming usernames are unique)
                        DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                        String email = documentSnapshot.getString("email");

                        if (email != null) {
                            // Authenticate using Firebase Authentication
                            auth.signInWithEmailAndPassword(email, password)
                                    .addOnSuccessListener(authResult -> {
                                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                        intent.putExtra("username", username);
                                        intent.putExtra("email", email);
                                        startActivity(intent);
                                    })
                                    .addOnFailureListener(e -> {
                                        loginPassword.setError("Invalid Credentials");
                                        loginPassword.requestFocus();
                                    });
                        } else {
                            loginUsername.setError("No email associated with this username");
                        }
                    } else {
                        loginUsername.setError("User does not exist");
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(LoginActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // 🔥 Reset Password Functionality
    public void resetPassword() {
        String username = loginUsername.getText().toString().trim();

        if (username.isEmpty()) {
            loginUsername.setError("Enter your username to reset password");
            loginUsername.requestFocus();
            return;
        }

        // Fetch email from Firestore based on the username field (not document ID)
        db.collection("users") // "users" is the collection where the user data is stored
                .whereEqualTo("username", username) // Query where the username matches
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Get the first document (assuming usernames are unique)
                        DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                        String email = documentSnapshot.getString("email");

                        if (email != null) {
                            // Directly send the password reset email here
                            sendPasswordResetEmail(email);
                        } else {
                            loginUsername.setError("No email associated with this username");
                        }
                    } else {
                        loginUsername.setError("User does not exist");
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(LoginActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void sendPasswordResetEmail(String email) {
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, "Password reset email sent", Toast.LENGTH_SHORT).show();
                    } else {
                        String errorMessage = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                        Toast.makeText(LoginActivity.this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
