package com.tms.restapi.toolsmanagement.admin.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ActivityDto {
    private String title; // e.g. "Tool Issued", "Tool Returned", "Added Tool", "Added Kit"
    private String actor; // trainer name or admin name
    private String itemType; // Tool or Kit
    private String itemNames; // comma separated names
    private String location; // location where activity happened (optional)
    private LocalDate date;
    private LocalDateTime timestamp;
    private String timeAgo;

    public ActivityDto() {}

    public ActivityDto(String title, String actor, String itemType, String itemNames, LocalDate date) {
        this.title = title;
        this.actor = actor;
        this.itemType = itemType;
        this.itemNames = itemNames;
        this.date = date;
    }

    public ActivityDto(String title, String actor, String itemType, String itemNames, LocalDate date, LocalDateTime timestamp, String timeAgo) {
        this.title = title;
        this.actor = actor;
        this.itemType = itemType;
        this.itemNames = itemNames;
        this.date = date;
        this.timestamp = timestamp;
        this.timeAgo = timeAgo;
    }

    public ActivityDto(String title, String actor, String itemType, String itemNames, LocalDate date, String location) {
        this.title = title;
        this.actor = actor;
        this.itemType = itemType;
        this.itemNames = itemNames;
        this.date = date;
        this.location = location;
    }

    public ActivityDto(String title, String actor, String itemType, String itemNames, LocalDate date, String location, LocalDateTime timestamp, String timeAgo) {
        this.title = title;
        this.actor = actor;
        this.itemType = itemType;
        this.itemNames = itemNames;
        this.date = date;
        this.location = location;
        this.timestamp = timestamp;
        this.timeAgo = timeAgo;
    }

    public ActivityDto(String title, String actor, String itemType, String itemNames, LocalDateTime timestamp, String location) {
        this.title = title;
        this.actor = actor;
        this.itemType = itemType;
        this.itemNames = itemNames;
        this.timestamp = timestamp;
        this.location = location;
        this.date = timestamp != null ? timestamp.toLocalDate() : null;
    }

    public ActivityDto(String title, String actor, String itemType, String itemNames, LocalDateTime timestamp) {
        this.title = title;
        this.actor = actor;
        this.itemType = itemType;
        this.itemNames = itemNames;
        this.timestamp = timestamp;
        this.date = timestamp != null ? timestamp.toLocalDate() : null;
    }

    public ActivityDto(String title, String actor, String itemType, String itemNames, String location) {
        this.title = title;
        this.actor = actor;
        this.itemType = itemType;
        this.itemNames = itemNames;
        this.location = location;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getActor() { return actor; }
    public void setActor(String actor) { this.actor = actor; }

    public String getItemType() { return itemType; }
    public void setItemType(String itemType) { this.itemType = itemType; }

    public String getItemNames() { return itemNames; }
    public void setItemNames(String itemNames) { this.itemNames = itemNames; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getTimeAgo() { return timeAgo; }
    public void setTimeAgo(String timeAgo) { this.timeAgo = timeAgo; }
}
