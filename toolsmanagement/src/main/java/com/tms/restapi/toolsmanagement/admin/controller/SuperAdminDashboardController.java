package com.tms.restapi.toolsmanagement.admin.controller;

import com.tms.restapi.toolsmanagement.admin.dto.AdminDashboardResponse;
import com.tms.restapi.toolsmanagement.admin.service.SuperAdminDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

@RestController
@RequestMapping("/api/superadmin/dashboard")
@CrossOrigin(origins = "*")
public class SuperAdminDashboardController {

    @Autowired
    private SuperAdminDashboardService service;

    // GET /api/superadmin/dashboard
    @GetMapping
    public ResponseEntity<AdminDashboardResponse> getGlobalDashboard() {
        AdminDashboardResponse resp = service.getGlobalDashboard();
        return ResponseEntity.ok(resp);
    }
}
