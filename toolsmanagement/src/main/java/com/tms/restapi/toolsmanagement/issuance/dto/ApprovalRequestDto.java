package com.tms.restapi.toolsmanagement.issuance.dto;

public class ApprovalRequestDto {
    private Long requestId;
    private String approvedBy;
    private String approvalRemark;

    public ApprovalRequestDto() {}

    public ApprovalRequestDto(Long requestId, String approvedBy, String approvalRemark) {
        this.requestId = requestId;
        this.approvedBy = approvedBy;
        this.approvalRemark = approvalRemark;
    }

    public Long getRequestId() { return requestId; }
    public void setRequestId(Long requestId) { this.requestId = requestId; }

    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }

    public String getApprovalRemark() { return approvalRemark; }
    public void setApprovalRemark(String approvalRemark) { this.approvalRemark = approvalRemark; }
}
