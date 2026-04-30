package com.tms.restapi.toolsmanagement.kit.dto;

/**
 * Request payload for adding or updating a kit aggregate entry.
 */
public class KitAggregateRequest {
    private String name;
    private String remark;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
