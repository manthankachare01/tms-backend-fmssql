package com.tms.restapi.toolsmanagement.reports.dto;

public class StaffLocationReportDTO {
    private String location;

    private long activeTrainers;
    private long inactiveTrainers;

    private double trainerActivePercentage;
    private double trainerInactivePercentage;

    private long activeSecurity;
    private long inactiveSecurity;

    private double securityActivePercentage;
    private double securityInactivePercentage;

    public StaffLocationReportDTO() {}

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public long getActiveTrainers() { return activeTrainers; }
    public void setActiveTrainers(long activeTrainers) { this.activeTrainers = activeTrainers; }

    public long getInactiveTrainers() { return inactiveTrainers; }
    public void setInactiveTrainers(long inactiveTrainers) { this.inactiveTrainers = inactiveTrainers; }

    public double getTrainerActivePercentage() { return trainerActivePercentage; }
    public void setTrainerActivePercentage(double trainerActivePercentage) { this.trainerActivePercentage = trainerActivePercentage; }

    public double getTrainerInactivePercentage() { return trainerInactivePercentage; }
    public void setTrainerInactivePercentage(double trainerInactivePercentage) { this.trainerInactivePercentage = trainerInactivePercentage; }

    public long getActiveSecurity() { return activeSecurity; }
    public void setActiveSecurity(long activeSecurity) { this.activeSecurity = activeSecurity; }

    public long getInactiveSecurity() { return inactiveSecurity; }
    public void setInactiveSecurity(long inactiveSecurity) { this.inactiveSecurity = inactiveSecurity; }

    public double getSecurityActivePercentage() { return securityActivePercentage; }
    public void setSecurityActivePercentage(double securityActivePercentage) { this.securityActivePercentage = securityActivePercentage; }

    public double getSecurityInactivePercentage() { return securityInactivePercentage; }
    public void setSecurityInactivePercentage(double securityInactivePercentage) { this.securityInactivePercentage = securityInactivePercentage; }
}
