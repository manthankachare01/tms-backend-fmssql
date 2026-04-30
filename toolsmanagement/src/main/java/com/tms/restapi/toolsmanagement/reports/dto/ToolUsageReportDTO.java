package com.tms.restapi.toolsmanagement.reports.dto;

public class ToolUsageReportDTO {
    private Long toolId;
    private String description;
    private String toolNo;
    private String location;
    private Integer issueCount;
    private Integer availability;
    private String usageCategory;

    public ToolUsageReportDTO() {
    }

    public ToolUsageReportDTO(Long toolId, String description, String toolNo, String location,
                              Integer issueCount, Integer availability, String usageCategory) {
        this.toolId = toolId;
        this.description = description;
        this.toolNo = toolNo;
        this.location = location;
        this.issueCount = issueCount;
        this.availability = availability;
        this.usageCategory = usageCategory;
    }

    public Long getToolId() {
        return toolId;
    }

    public void setToolId(Long toolId) {
        this.toolId = toolId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getToolNo() {
        return toolNo;
    }

    public void setToolNo(String toolNo) {
        this.toolNo = toolNo;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Integer getIssueCount() {
        return issueCount;
    }

    public void setIssueCount(Integer issueCount) {
        this.issueCount = issueCount;
    }

    public Integer getAvailability() {
        return availability;
    }

    public void setAvailability(Integer availability) {
        this.availability = availability;
    }

    public String getUsageCategory() {
        return usageCategory;
    }

    public void setUsageCategory(String usageCategory) {
        this.usageCategory = usageCategory;
    }
}
