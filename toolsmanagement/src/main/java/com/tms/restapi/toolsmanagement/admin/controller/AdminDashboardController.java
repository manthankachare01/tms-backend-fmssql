package com.tms.restapi.toolsmanagement.admin.controller;

import com.tms.restapi.toolsmanagement.admin.dto.AdminDashboardResponse;
import com.tms.restapi.toolsmanagement.admin.service.AdminDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.CrossOrigin;

@RestController
@RequestMapping("/api/admins/dashboard")
@CrossOrigin(origins = "*")
public class AdminDashboardController {

    @Autowired
    private AdminDashboardService dashboardService;

    // GET /api/admins/dashboard?location=Pune
    @GetMapping
    public ResponseEntity<AdminDashboardResponse> getDashboard(@RequestParam String location) {
        AdminDashboardResponse resp = dashboardService.getDashboardByLocation(location);
        return ResponseEntity.ok(resp);
    }
}
