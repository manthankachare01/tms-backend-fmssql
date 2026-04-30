package com.tms.restapi.toolsmanagement.reports.dto;

public class ToolStatisticsDTO {
    private Long totalTools;
    private Long availableTools;
    private Long unavailableTools;
    private Double availabilityPercentage;
    private Long toolsNeedingCalibration;
    private Long damagedTools;
    private Long missingTools;
    private Long obsoleteTools;

    public ToolStatisticsDTO() {
    }

    public ToolStatisticsDTO(Long totalTools, Long availableTools, Long unavailableTools,
                            Double availabilityPercentage, Long toolsNeedingCalibration, Long damagedTools,
                            Long missingTools, Long obsoleteTools) {
        this.totalTools = totalTools;
        this.availableTools = availableTools;
        this.unavailableTools = unavailableTools;
        this.availabilityPercentage = availabilityPercentage;
        this.toolsNeedingCalibration = toolsNeedingCalibration;
        this.damagedTools = damagedTools;
        this.missingTools = missingTools;
        this.obsoleteTools = obsoleteTools;
    }

    // Getters and Setters
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

    public Long getToolsNeedingCalibration() {
        return toolsNeedingCalibration;
    }

    public void setToolsNeedingCalibration(Long toolsNeedingCalibration) {
        this.toolsNeedingCalibration = toolsNeedingCalibration;
    }

    public Long getDamagedTools() {
        return damagedTools;
    }

    public void setDamagedTools(Long damagedTools) {
        this.damagedTools = damagedTools;
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
}
