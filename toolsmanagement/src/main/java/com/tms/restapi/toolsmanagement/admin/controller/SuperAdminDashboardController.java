package com.tms.restapi.toolsmanagement.admin.controller;

import com.tms.restapi.toolsmanagement.admin.dto.AdminDashboardResponse;
import com.tms.restapi.toolsmanagement.admin.service.SuperAdminDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * SuperAdmin Dashboard Controller for global dashboard metrics.
 *
 * This controller returns summary statistics for the entire application,
 * intended for superadmin monitoring and oversight.
 */
@RestController
@RequestMapping("/api/superadmin/dashboard")
@CrossOrigin(origins = "*")
public class SuperAdminDashboardController {

    @Autowired
    private SuperAdminDashboardService service;

    /**
     * Retrieve global dashboard data.
     *
     * API Endpoint: GET /api/superadmin/dashboard
     *
     * Response Sample:
     * {
     *   "totalTools": 500,
     *   "totalKits": 80,
     *   "totalIssuances": 320,
     *   "pendingApprovals": 12,
     *   "criticalNotifications": 5
     * }
     *
     * @return ResponseEntity containing global dashboard metrics
     */
    @GetMapping
    public ResponseEntity<AdminDashboardResponse> getGlobalDashboard() {
        AdminDashboardResponse resp = service.getGlobalDashboard();
        return ResponseEntity.ok(resp);
    }
}
