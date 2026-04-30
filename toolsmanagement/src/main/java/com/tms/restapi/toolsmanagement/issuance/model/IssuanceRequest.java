package com.tms.restapi.toolsmanagement.issuance.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "issuance_requests_pending")
public class IssuanceRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long trainerId;
    private String trainerName;
    private String trainingName;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime requestDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime returnDate;
    
    private String status; // PENDING, APPROVED, REJECTED
    private String location;
    private String comment;
    // issuance type: TOOL or KIT
    private String issuanceType;

    // remarks for issuance
    private String remarks;
    
    @ElementCollection
    private List<Long> toolIds;

    @ElementCollection
    private List<Long> kitIds;

    // Admin approval fields
    private String approvedBy;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime approvalDate;
    
    private String approvalRemark;
    
    // Reference to created issuance (when approved)
    private Long issuanceId;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getTrainerId() { return trainerId; }
    public void setTrainerId(Long trainerId) { this.trainerId = trainerId; }

    public String getTrainerName() { return trainerName; }
    public void setTrainerName(String trainerName) { this.trainerName = trainerName; }

    public String getTrainingName() { return trainingName; }
    public void setTrainingName(String trainingName) { this.trainingName = trainingName; }

    public LocalDateTime getRequestDate() { return requestDate; }
    public void setRequestDate(LocalDateTime requestDate) { this.requestDate = requestDate; }

    public LocalDateTime getReturnDate() { return returnDate; }
    public void setReturnDate(LocalDateTime returnDate) { this.returnDate = returnDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public List<Long> getToolIds() { return toolIds; }
    public void setToolIds(List<Long> toolIds) { this.toolIds = toolIds; }

    public List<Long> getKitIds() { return kitIds; }
    public void setKitIds(List<Long> kitIds) { this.kitIds = kitIds; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public String getIssuanceType() { return issuanceType; }
    public void setIssuanceType(String issuanceType) { this.issuanceType = issuanceType; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }

    public LocalDateTime getApprovalDate() { return approvalDate; }
    public void setApprovalDate(LocalDateTime approvalDate) { this.approvalDate = approvalDate; }

    public String getApprovalRemark() { return approvalRemark; }
    public void setApprovalRemark(String approvalRemark) { this.approvalRemark = approvalRemark; }

    public Long getIssuanceId() { return issuanceId; }
    public void setIssuanceId(Long issuanceId) { this.issuanceId = issuanceId; }
}
