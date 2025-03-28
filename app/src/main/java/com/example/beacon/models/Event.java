package com.example.beacon.models;

import java.util.ArrayList;
import java.util.List;

public class Event {
    private String name;
    private String description;
    private String date;
    private String time;
    private String location;
    private String orgName;
    private List<String> volunteers;
    private int volunteersNeeded;

    // Default constructor required for Firestore
    public Event() {
        this.volunteers = new ArrayList<>();
    }

    // Constructor with organizationName
    public Event(String name, String description, String date, String time, String location, String orgName) {
        this.name = name;
        this.description = description;
        this.date = date;
        this.time = time;
        this.location = location;
        this.orgName = orgName;
        this.volunteers = new ArrayList<>();
    }

    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getOrgName() {
        return orgName;
    }


    public int getVolunteersNeeded() { return volunteersNeeded; }
    public void setVolunteersNeeded(int volunteersNeeded) { this.volunteersNeeded = volunteersNeeded; }

    public List<String> getVolunteers() { return volunteers; }
    public void setVolunteers(List<String> volunteers) { this.volunteers = volunteers; }
}
