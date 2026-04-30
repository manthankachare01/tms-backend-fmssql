package com.tms.restapi.toolsmanagement.excel.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.tms.restapi.toolsmanagement.excel.dto.ExcelResponse;
import com.tms.restapi.toolsmanagement.excel.service.ToolExcelService;

/**
 * Tool Excel Controller for bulk tool import operations.
 *
 * This controller accepts Excel uploads to create or update tool inventory
 * records in bulk and returns a summary of processing results.
 */
@RestController
@RequestMapping("/api/tools")
public class ToolExcelController {

    @Autowired
    private ToolExcelService toolExcelService;

    /**
     * Upload Excel file to import tool inventory records in bulk.
     *
     * API Endpoint: POST /api/tools/upload-excel
     * Request payload: multipart/form-data with file field named "file"
     * Response payload includes total, success, failed and duplicate counts.
     */
    @PostMapping("/upload-excel")
    public ResponseEntity<ExcelResponse> uploadExcel(
            @RequestParam("file") MultipartFile file) {

        ExcelResponse response = toolExcelService.uploadTools(file);
        return ResponseEntity.ok(response);
    }
} 