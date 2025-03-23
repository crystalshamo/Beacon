package com.example.beacon;
public class HelperClass {
    private String name, email, username, password;

    public HelperClass() {}  // Firestore requires an empty constructor

    public HelperClass(String name, String email, String username, String password) {
        this.name = name;
        this.email = email;
        this.username = username;
        this.password = password;
    }

    // Getters
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
}
