package com.tms.restapi.toolsmanagement.issuance.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.util.List;

public class ReturnRequestDto {
    private Long issuanceId;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime actualReturnDate;
    
    private String processedBy;
    private String remarks;
    private List<ReturnItemDto> items;

    public Long getIssuanceId() { return issuanceId; }
    public void setIssuanceId(Long issuanceId) { this.issuanceId = issuanceId; }

    public LocalDateTime getActualReturnDate() { return actualReturnDate; }
    public void setActualReturnDate(LocalDateTime actualReturnDate) { this.actualReturnDate = actualReturnDate; }

    public String getProcessedBy() { return processedBy; }
    public void setProcessedBy(String processedBy) { this.processedBy = processedBy; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public List<ReturnItemDto> getItems() { return items; }
    public void setItems(List<ReturnItemDto> items) { this.items = items; }
}
