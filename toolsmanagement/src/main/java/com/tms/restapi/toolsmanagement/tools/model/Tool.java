package com.tms.restapi.toolsmanagement.tools.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tools")
public class Tool {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Basic details
    @Column(nullable = false)
    private String description;

    @Column(name = "si_no", nullable = false)
    private String siNo;

    @Column(name = "tool_no", nullable = false)
    private String toolNo;

    // Physical location of the tool (rack, cupboard, etc.)
    @Column(name = "tool_location")
    private String toolLocation;

    // Plant location (Pune, etc.), forced from adminLocation
    @Column(name = "location", nullable = false)
    private String location;

    // Fixed total quantity for that tool
    @Column(nullable = false)
    private Integer quantity;

    // Remaining quantity (availability count)
    // 0 -> not available, >0 -> available
    @Column(nullable = false)
    private Integer availability;

    // Condition of tool (Good, Damaged, etc.)
    @Column(name = "tool_condition")
    private String condition;

    // Calibration
    @Column(name = "calibration_required", nullable = false)
    private boolean calibrationRequired;

    // Example: number of months between calibrations
    @Column(name = "calibration_period_months")
    private Integer calibrationPeriodMonths;

    @Column(name = "last_calibration_date")
    private LocalDate lastCalibrationDate;

    @Column(name = "next_calibration_date")
    private LocalDate nextCalibrationDate;

    // Other info
    private String remark;

    @Column(name = "last_borrowed_by")
    private String lastBorrowedBy;

    @Column(name = "current_borrowed_by")
    private String currentBorrowedBy;

    // Count of how many times this tool has been issued
    @Column(name = "issue_count", nullable = false)
    private Integer issueCount = 0;

    // Issuance and return dates
    @Column(name = "issuance_date")
    private LocalDateTime issuanceDate;

    @Column(name = "return_date")
    private LocalDateTime returnDate;

    // Flag: 0 = standalone tool, 1 = tool belongs to a kit
    @Column(name = "belongs_to_kit", nullable = false)
    private Integer belongsToKit = 0;

    // Track who created this tool and when
    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSiNo() {
        return siNo;
    }

    public void setSiNo(String siNo) {
        this.siNo = siNo;
    }

    public String getToolNo() {
        return toolNo;
    }

    public void setToolNo(String toolNo) {
        this.toolNo = toolNo;
    }

    public String getToolLocation() {
        return toolLocation;
    }

    public void setToolLocation(String toolLocation) {
        this.toolLocation = toolLocation;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    // fixed quantity
    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    // remaining quantity
    public Integer getAvailability() {
        return availability;
    }

    public void setAvailability(Integer availability) {
        this.availability = availability;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public boolean isCalibrationRequired() {
        return calibrationRequired;
    }

    public void setCalibrationRequired(boolean calibrationRequired) {
        this.calibrationRequired = calibrationRequired;
    }

    public Integer getCalibrationPeriodMonths() {
        return calibrationPeriodMonths;
    }

    public void setCalibrationPeriodMonths(Integer calibrationPeriodMonths) {
        this.calibrationPeriodMonths = calibrationPeriodMonths;
    }

    public LocalDate getLastCalibrationDate() {
        return lastCalibrationDate;
    }

    public void setLastCalibrationDate(LocalDate lastCalibrationDate) {
        this.lastCalibrationDate = lastCalibrationDate;
    }

    public LocalDate getNextCalibrationDate() {
        return nextCalibrationDate;
    }

    public void setNextCalibrationDate(LocalDate nextCalibrationDate) {
        this.nextCalibrationDate = nextCalibrationDate;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getLastBorrowedBy() {
        return lastBorrowedBy;
    }

    public void setLastBorrowedBy(String lastBorrowedBy) {
        this.lastBorrowedBy = lastBorrowedBy;
    }

    public String getCurrentBorrowedBy() {
        return currentBorrowedBy;
    }

    public void setCurrentBorrowedBy(String currentBorrowedBy) {
        this.currentBorrowedBy = currentBorrowedBy;
    }

    public Integer getIssueCount() {
        return issueCount;
    }

    public void setIssueCount(Integer issueCount) {
        this.issueCount = issueCount;
    }

    public LocalDateTime getIssuanceDate() {
        return issuanceDate;
    }

    public void setIssuanceDate(LocalDateTime issuanceDate) {
        this.issuanceDate = issuanceDate;
    }

    public LocalDateTime getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDateTime returnDate) {
        this.returnDate = returnDate;
    }

    public Integer getBelongsToKit() {
        return belongsToKit;
    }

    public void setBelongsToKit(Integer belongsToKit) {
        this.belongsToKit = belongsToKit;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
