package com.tms.restapi.toolsmanagement.issuance.dto;

public class ReturnItemDto {
    private Long toolId;
    private Long kitId;
    private Integer quantityReturned = 1;
    private String condition;
    private String remark;

    public Long getToolId() { return toolId; }
    public void setToolId(Long toolId) { this.toolId = toolId; }

    public Long getKitId() { return kitId; }
    public void setKitId(Long kitId) { this.kitId = kitId; }

    public Integer getQuantityReturned() { return quantityReturned; }
    public void setQuantityReturned(Integer quantityReturned) { this.quantityReturned = quantityReturned; }

    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
