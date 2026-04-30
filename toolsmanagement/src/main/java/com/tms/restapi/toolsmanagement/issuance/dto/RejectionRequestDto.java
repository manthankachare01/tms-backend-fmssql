package com.tms.restapi.toolsmanagement.issuance.dto;

public class RejectionRequestDto {
    private Long requestId;
    private String rejectedBy;
    private String rejectionReason;

    public RejectionRequestDto() {}

    public RejectionRequestDto(Long requestId, String rejectedBy, String rejectionReason) {
        this.requestId = requestId;
        this.rejectedBy = rejectedBy;
        this.rejectionReason = rejectionReason;
    }

    public Long getRequestId() { return requestId; }
    public void setRequestId(Long requestId) { this.requestId = requestId; }

    public String getRejectedBy() { return rejectedBy; }
    public void setRejectedBy(String rejectedBy) { this.rejectedBy = rejectedBy; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
}
