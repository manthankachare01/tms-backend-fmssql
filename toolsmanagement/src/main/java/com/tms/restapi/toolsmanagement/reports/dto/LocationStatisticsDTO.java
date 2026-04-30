package com.tms.restapi.toolsmanagement.reports.dto;

public class LocationStatisticsDTO {
    private String location;
    private Long totalTools;
    private Long availableTools;
    private Long unavailableTools;
    private Double availabilityPercentage;
    private Long missingTools;
    private Long obsoleteTools;
    private Long damagedTools;

    public LocationStatisticsDTO() {
    }

    public LocationStatisticsDTO(String location, Long totalTools, Long availableTools,
                               Long unavailableTools, Double availabilityPercentage, Long missingTools, Long obsoleteTools, Long damagedTools) {
        this.location = location;
        this.totalTools = totalTools;
        this.availableTools = availableTools;
        this.unavailableTools = unavailableTools;
        this.availabilityPercentage = availabilityPercentage;
        this.missingTools = missingTools;
        this.obsoleteTools = obsoleteTools;
        this.damagedTools = damagedTools;
    }

    // Getters and Setters
    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Long getTotalTools() {
        return totalTools;
    }

    public void setTotalTools(Long totalTools) {
        this.totalTools = totalTools;
    }

    public Long getAvailableTools() {
        return availableTools;
    }

    public void setAvailableTools(Long availableTools) {
        this.availableTools = availableTools;
    }

    public Long getUnavailableTools() {
        return unavailableTools;
    }

    public void setUnavailableTools(Long unavailableTools) {
        this.unavailableTools = unavailableTools;
    }

    public Double getAvailabilityPercentage() {
        return availabilityPercentage;
    }

    public void setAvailabilityPercentage(Double availabilityPercentage) {
        this.availabilityPercentage = availabilityPercentage;
    }

    public Long getMissingTools() {
        return missingTools;
    }

    public void setMissingTools(Long missingTools) {
        this.missingTools = missingTools;
    }

    public Long getObsoleteTools() {
        return obsoleteTools;
    }

    public void setObsoleteTools(Long obsoleteTools) {
        this.obsoleteTools = obsoleteTools;
    }

    public Long getDamagedTools() {
        return damagedTools;
    }

    public void setDamagedTools(Long damagedTools) {
        this.damagedTools = damagedTools;
    }
}
