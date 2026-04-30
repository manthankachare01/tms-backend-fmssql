package com.tms.restapi.toolsmanagement.notification.dto;

import java.time.LocalDateTime;

public class NotificationDto {

    private Long id;
    private String type;
    private String severity;
    private String title;
    private String message;
    private Long toolId;
    private Long trainerId;
    private Long issuanceId;
    private String location;
    private String targetRole;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;

    // Constructors
    public NotificationDto() {}

    public NotificationDto(Long id, String type, String severity, String title, String message,
                          Long toolId, Long trainerId, Long issuanceId, String location,
                          String targetRole, String status, LocalDateTime createdAt, LocalDateTime readAt) {
        this.id = id;
        this.type = type;
        this.severity = severity;
        this.title = title;
        this.message = message;
        this.toolId = toolId;
        this.trainerId = trainerId;
        this.issuanceId = issuanceId;
        this.location = location;
        this.targetRole = targetRole;
        this.status = status;
        this.createdAt = createdAt;
        this.readAt = readAt;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getToolId() {
        return toolId;
    }

    public void setToolId(Long toolId) {
        this.toolId = toolId;
    }

    public Long getTrainerId() {
        return trainerId;
    }

    public void setTrainerId(Long trainerId) {
        this.trainerId = trainerId;
    }

    public Long getIssuanceId() {
        return issuanceId;
    }

    public void setIssuanceId(Long issuanceId) {
        this.issuanceId = issuanceId;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getTargetRole() {
        return targetRole;
    }

    public void setTargetRole(String targetRole) {
        this.targetRole = targetRole;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getReadAt() {
        return readAt;
    }

    public void setReadAt(LocalDateTime readAt) {
        this.readAt = readAt;
    }
}
