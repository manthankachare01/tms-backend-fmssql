package com.tms.restapi.toolsmanagement.issuance.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "return_items")
public class ReturnItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "return_record_id")
    @JsonBackReference
    private ReturnRecord returnRecord;

    private Long toolId;
    private Long kitId;
    private Integer quantityReturned;
    @Column(name = "item_condition")
    private String condition;
    private String remark;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ReturnRecord getReturnRecord() { return returnRecord; }
    public void setReturnRecord(ReturnRecord returnRecord) { this.returnRecord = returnRecord; }

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
