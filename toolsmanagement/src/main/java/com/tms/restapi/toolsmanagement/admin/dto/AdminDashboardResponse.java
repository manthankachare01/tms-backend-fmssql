package com.tms.restapi.toolsmanagement.admin.dto;

import java.util.List;

public class AdminDashboardResponse {
    private int totalTools;
    private int totalKits;
    private int issuanceToday;
    private int returnsToday;
    private int overdueIssuance;
    private int damagedCount;
    private int calibrationRequiredCount;
    private List<ActivityDto> recentActivities;

    public AdminDashboardResponse() {}

    public int getTotalTools() { return totalTools; }
    public void setTotalTools(int totalTools) { this.totalTools = totalTools; }

    public int getTotalKits() { return totalKits; }
    public void setTotalKits(int totalKits) { this.totalKits = totalKits; }

    public int getIssuanceToday() { return issuanceToday; }
    public void setIssuanceToday(int issuanceToday) { this.issuanceToday = issuanceToday; }

    public int getReturnsToday() { return returnsToday; }
    public void setReturnsToday(int returnsToday) { this.returnsToday = returnsToday; }

    public int getOverdueIssuance() { return overdueIssuance; }
    public void setOverdueIssuance(int overdueIssuance) { this.overdueIssuance = overdueIssuance; }

    public int getDamagedCount() { return damagedCount; }
    public void setDamagedCount(int damagedCount) { this.damagedCount = damagedCount; }

    public int getCalibrationRequiredCount() { return calibrationRequiredCount; }
    public void setCalibrationRequiredCount(int calibrationRequiredCount) { this.calibrationRequiredCount = calibrationRequiredCount; }

    public List<ActivityDto> getRecentActivities() { return recentActivities; }
    public void setRecentActivities(List<ActivityDto> recentActivities) { this.recentActivities = recentActivities; }
}
