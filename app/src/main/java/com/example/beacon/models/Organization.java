package com.example.beacon.models;

import java.util.ArrayList;
import java.util.List;

public class Organization {
    private String id;
    private String name;
    private String address;
    private String website;
    private String description;
    private List<String> events;  // <-- ✅ FIXED
    private List<String> volunteer_opportunities;
    private List<String> needs;
    private double latitude; // Latitude field
    private double longitude;

    public Organization() {
        this.events = new ArrayList<>();
        this.volunteer_opportunities = new ArrayList<>();
        this.needs = new ArrayList<>();
    }

    public Organization(String name, String address, String website, String description) {
        this.name = name;
        this.address = address;
        this.website = website;
        this.description = description;
        this.events = new ArrayList<>();
        this.volunteer_opportunities = new ArrayList<>();
        this.needs = new ArrayList<>();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<String> getEvents() {
        return events;
    }

    public void setEvents(List<String> events) {
        this.events = events;
    }
    public List<String> getNeeds() { return needs; }
    public void setNeeds(List<String> needs) { this.needs = needs; }

    public List<String> getVolunteerOpportunities() { return volunteer_opportunities; }
    public void setVolunteerOpportunities(List<String> volunteer_opportunities) {
        this.volunteer_opportunities = volunteer_opportunities;
    }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
}

