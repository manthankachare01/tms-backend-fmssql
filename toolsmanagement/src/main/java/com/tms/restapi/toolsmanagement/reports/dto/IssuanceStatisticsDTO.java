package com.tms.restapi.toolsmanagement.reports.dto;

public class IssuanceStatisticsDTO {
    private Long totalIssuances;
    private Long issuedTools;
    private Long returnedTools;
    private Long pendingReturns;
    private Long approvedIssuances;
    private Long pendingApprovals;
    private Long rejectedIssuances;

    public IssuanceStatisticsDTO() {
    }

    public IssuanceStatisticsDTO(Long totalIssuances, Long issuedTools, Long returnedTools,
                                Long pendingReturns, Long approvedIssuances, Long pendingApprovals,
                                Long rejectedIssuances) {
        this.totalIssuances = totalIssuances;
        this.issuedTools = issuedTools;
        this.returnedTools = returnedTools;
        this.pendingReturns = pendingReturns;
        this.approvedIssuances = approvedIssuances;
        this.pendingApprovals = pendingApprovals;
        this.rejectedIssuances = rejectedIssuances;
    }

    // Getters and Setters
    public Long getTotalIssuances() {
        return totalIssuances;
    }

    public void setTotalIssuances(Long totalIssuances) {
        this.totalIssuances = totalIssuances;
    }

    public Long getIssuedTools() {
        return issuedTools;
    }

    public void setIssuedTools(Long issuedTools) {
        this.issuedTools = issuedTools;
    }

    public Long getReturnedTools() {
        return returnedTools;
    }

    public void setReturnedTools(Long returnedTools) {
        this.returnedTools = returnedTools;
    }

    public Long getPendingReturns() {
        return pendingReturns;
    }

    public void setPendingReturns(Long pendingReturns) {
        this.pendingReturns = pendingReturns;
    }

    public Long getApprovedIssuances() {
        return approvedIssuances;
    }

    public void setApprovedIssuances(Long approvedIssuances) {
        this.approvedIssuances = approvedIssuances;
    }

    public Long getPendingApprovals() {
        return pendingApprovals;
    }

    public void setPendingApprovals(Long pendingApprovals) {
        this.pendingApprovals = pendingApprovals;
    }

    public Long getRejectedIssuances() {
        return rejectedIssuances;
    }

    public void setRejectedIssuances(Long rejectedIssuances) {
        this.rejectedIssuances = rejectedIssuances;
    }
}
