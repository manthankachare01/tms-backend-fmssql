package com.tms.restapi.toolsmanagement.reports.dto;

public class DashboardOverviewDTO {
    private Long totalTools;
    private Long totalIssuances;
    private Long totalTrainers;
    private Long totalAdmins;
    private Double toolAvailabilityPercentage;
    private Long pendingApprovals;
    private Long toolsNeedingMaintenance;

    public DashboardOverviewDTO() {
    }

    public DashboardOverviewDTO(Long totalTools, Long totalIssuances, Long totalTrainers,
                               Long totalAdmins, Double toolAvailabilityPercentage,
                               Long pendingApprovals, Long toolsNeedingMaintenance) {
        this.totalTools = totalTools;
        this.totalIssuances = totalIssuances;
        this.totalTrainers = totalTrainers;
        this.totalAdmins = totalAdmins;
        this.toolAvailabilityPercentage = toolAvailabilityPercentage;
        this.pendingApprovals = pendingApprovals;
        this.toolsNeedingMaintenance = toolsNeedingMaintenance;
    }

    // Getters and Setters
    public Long getTotalTools() {
        return totalTools;
    }

    public void setTotalTools(Long totalTools) {
        this.totalTools = totalTools;
    }

    public Long getTotalIssuances() {
        return totalIssuances;
    }

    public void setTotalIssuances(Long totalIssuances) {
        this.totalIssuances = totalIssuances;
    }

    public Long getTotalTrainers() {
        return totalTrainers;
    }

    public void setTotalTrainers(Long totalTrainers) {
        this.totalTrainers = totalTrainers;
    }

    public Long getTotalAdmins() {
        return totalAdmins;
    }

    public void setTotalAdmins(Long totalAdmins) {
        this.totalAdmins = totalAdmins;
    }

    public Double getToolAvailabilityPercentage() {
        return toolAvailabilityPercentage;
    }

    public void setToolAvailabilityPercentage(Double toolAvailabilityPercentage) {
        this.toolAvailabilityPercentage = toolAvailabilityPercentage;
    }

    public Long getPendingApprovals() {
        return pendingApprovals;
    }

    public void setPendingApprovals(Long pendingApprovals) {
        this.pendingApprovals = pendingApprovals;
    }

    public Long getToolsNeedingMaintenance() {
        return toolsNeedingMaintenance;
    }

    public void setToolsNeedingMaintenance(Long toolsNeedingMaintenance) {
        this.toolsNeedingMaintenance = toolsNeedingMaintenance;
    }
}
