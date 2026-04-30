package com.tms.restapi.toolsmanagement.reports.dto;

public class StaffReportDTO {

    private long activeAdmins;
    private long inactiveAdmins;

    private long activeTrainers;
    private long inactiveTrainers;

    private long activeSecurity;
    private long inactiveSecurity;

    private double activeAdminPercentage;
    private double inactiveAdminPercentage;

    private double activeTrainerPercentage;
    private double inactiveTrainerPercentage;

    private double activeSecurityPercentage;
    private double inactiveSecurityPercentage;

    public StaffReportDTO() {}

    // Getters and setters
    public long getActiveAdmins() { return activeAdmins; }
    public void setActiveAdmins(long activeAdmins) { this.activeAdmins = activeAdmins; }

    public long getInactiveAdmins() { return inactiveAdmins; }
    public void setInactiveAdmins(long inactiveAdmins) { this.inactiveAdmins = inactiveAdmins; }

    public long getActiveTrainers() { return activeTrainers; }
    public void setActiveTrainers(long activeTrainers) { this.activeTrainers = activeTrainers; }

    public long getInactiveTrainers() { return inactiveTrainers; }
    public void setInactiveTrainers(long inactiveTrainers) { this.inactiveTrainers = inactiveTrainers; }

    public long getActiveSecurity() { return activeSecurity; }
    public void setActiveSecurity(long activeSecurity) { this.activeSecurity = activeSecurity; }

    public long getInactiveSecurity() { return inactiveSecurity; }
    public void setInactiveSecurity(long inactiveSecurity) { this.inactiveSecurity = inactiveSecurity; }

    public double getActiveAdminPercentage() { return activeAdminPercentage; }
    public void setActiveAdminPercentage(double activeAdminPercentage) { this.activeAdminPercentage = activeAdminPercentage; }

    public double getInactiveAdminPercentage() { return inactiveAdminPercentage; }
    public void setInactiveAdminPercentage(double inactiveAdminPercentage) { this.inactiveAdminPercentage = inactiveAdminPercentage; }

    public double getActiveTrainerPercentage() { return activeTrainerPercentage; }
    public void setActiveTrainerPercentage(double activeTrainerPercentage) { this.activeTrainerPercentage = activeTrainerPercentage; }

    public double getInactiveTrainerPercentage() { return inactiveTrainerPercentage; }
    public void setInactiveTrainerPercentage(double inactiveTrainerPercentage) { this.inactiveTrainerPercentage = inactiveTrainerPercentage; }

    public double getActiveSecurityPercentage() { return activeSecurityPercentage; }
    public void setActiveSecurityPercentage(double activeSecurityPercentage) { this.activeSecurityPercentage = activeSecurityPercentage; }

    public double getInactiveSecurityPercentage() { return inactiveSecurityPercentage; }
    public void setInactiveSecurityPercentage(double inactiveSecurityPercentage) { this.inactiveSecurityPercentage = inactiveSecurityPercentage; }
}
