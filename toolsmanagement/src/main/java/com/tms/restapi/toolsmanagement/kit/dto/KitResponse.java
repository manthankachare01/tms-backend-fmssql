package com.tms.restapi.toolsmanagement.kit.dto;

import com.tms.restapi.toolsmanagement.tools.model.Tool;

import java.time.LocalDateTime;
import java.util.List;

public class KitResponse {

    private Long id;
    private String kitId;
    private String kitName;
    private String qualificationLevel;
    private String trainingName;
    private String location;
    private Integer availability;
    private LocalDateTime issuanceDate;
    private LocalDateTime returnDate;
    private String currentBorrowedBy;
    private String lastBorrowedBy;
    private String remark;
    private String condition;
    private List<Tool> tools;                      // full tool objects
    private List<KitAggregateResponse> aggregates;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getKitId() {
        return kitId;
    }

    public void setKitId(String kitId) {
        this.kitId = kitId;
    }

    public String getKitName() {
        return kitName;
    }

    public void setKitName(String kitName) {
        this.kitName = kitName;
    }

    public String getQualificationLevel() {
        return qualificationLevel;
    }

    public void setQualificationLevel(String qualificationLevel) {
        this.qualificationLevel = qualificationLevel;
    }

    public String getTrainingName() {
        return trainingName;
    }

    public void setTrainingName(String trainingName) {
        this.trainingName = trainingName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Integer getAvailability() {
        return availability;
    }

    public void setAvailability(Integer availability) {
        this.availability = availability;
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

    public String getCurrentBorrowedBy() {
        return currentBorrowedBy;
    }

    public void setCurrentBorrowedBy(String currentBorrowedBy) {
        this.currentBorrowedBy = currentBorrowedBy;
    }

    public String getLastBorrowedBy() {
        return lastBorrowedBy;
    }

    public void setLastBorrowedBy(String lastBorrowedBy) {
        this.lastBorrowedBy = lastBorrowedBy;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public List<Tool> getTools() {
        return tools;
    }

    public void setTools(List<Tool> tools) {
        this.tools = tools;
    }

    public List<KitAggregateResponse> getAggregates() {
        return aggregates;
    }

    public void setAggregates(List<KitAggregateResponse> aggregates) {
        this.aggregates = aggregates;
    }
}
