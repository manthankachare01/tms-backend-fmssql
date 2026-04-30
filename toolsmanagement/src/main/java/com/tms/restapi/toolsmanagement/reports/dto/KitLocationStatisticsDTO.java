package com.tms.restapi.toolsmanagement.reports.dto;

public class KitLocationStatisticsDTO {
    private String location;
    private Long totalKits;
    private Long availableKits;
    private Long unavailableKits;
    private Double availabilityPercentage;

    public KitLocationStatisticsDTO() {}

    public KitLocationStatisticsDTO(String location, Long totalKits, Long availableKits, Long unavailableKits, Double availabilityPercentage) {
        this.location = location;
        this.totalKits = totalKits;
        this.availableKits = availableKits;
        this.unavailableKits = unavailableKits;
        this.availabilityPercentage = availabilityPercentage;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Long getTotalKits() {
        return totalKits;
    }

    public void setTotalKits(Long totalKits) {
        this.totalKits = totalKits;
    }

    public Long getAvailableKits() {
        return availableKits;
    }

    public void setAvailableKits(Long availableKits) {
        this.availableKits = availableKits;
    }

    public Long getUnavailableKits() {
        return unavailableKits;
    }

    public void setUnavailableKits(Long unavailableKits) {
        this.unavailableKits = unavailableKits;
    }

    public Double getAvailabilityPercentage() {
        return availabilityPercentage;
    }

    public void setAvailabilityPercentage(Double availabilityPercentage) {
        this.availabilityPercentage = availabilityPercentage;
    }
}
