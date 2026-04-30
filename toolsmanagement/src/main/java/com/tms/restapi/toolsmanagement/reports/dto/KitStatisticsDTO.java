package com.tms.restapi.toolsmanagement.reports.dto;

/**
 * Response payload for kit inventory statistics.
 *
 * Provides counts for available and unavailable kits,
 * total kit count, and availability percentage.
 */
public class KitStatisticsDTO {
    private Long totalKits;
    private Long availableKits;
    private Long unavailableKits;
    private Double availabilityPercentage;

    public KitStatisticsDTO() {}

    public KitStatisticsDTO(Long totalKits, Long availableKits, Long unavailableKits, Double availabilityPercentage) {
        this.totalKits = totalKits;
        this.availableKits = availableKits;
        this.unavailableKits = unavailableKits;
        this.availabilityPercentage = availabilityPercentage;
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
