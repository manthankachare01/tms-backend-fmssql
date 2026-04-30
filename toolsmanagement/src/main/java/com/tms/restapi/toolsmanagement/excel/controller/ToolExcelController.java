package com.tms.restapi.toolsmanagement.excel.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.tms.restapi.toolsmanagement.excel.dto.ExcelResponse;
import com.tms.restapi.toolsmanagement.excel.service.ToolExcelService;

@RestController
@RequestMapping("/api/tools")
public class ToolExcelController {

    @Autowired
    private ToolExcelService toolExcelService;

    @PostMapping("/upload-excel")
    public ResponseEntity<ExcelResponse> uploadExcel(
            @RequestParam("file") MultipartFile file) {

        ExcelResponse response = toolExcelService.uploadTools(file);
        return ResponseEntity.ok(response);
    }
}