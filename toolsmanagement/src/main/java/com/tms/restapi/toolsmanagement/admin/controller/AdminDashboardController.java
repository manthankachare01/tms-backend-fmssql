package com.tms.restapi.toolsmanagement.admin.controller;

import com.tms.restapi.toolsmanagement.admin.dto.AdminDashboardResponse;
import com.tms.restapi.toolsmanagement.admin.service.AdminDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Admin Dashboard Controller for providing location-specific dashboard metrics.
 *
 * This controller retrieves summary data for admin dashboards, such as
 * tool availability, issuance counts, and location-level performance metrics.
 */
@RestController
@RequestMapping("/api/admins/dashboard")
@CrossOrigin(origins = "*")
public class AdminDashboardController {

    @Autowired
    private AdminDashboardService dashboardService;

    /**
     * Retrieve dashboard data for a specific location.
     *
     * API Endpoint: GET /api/admins/dashboard?location={location}
     *
     * Query Parameters:
     * - location: Location name for dashboard metrics (required)
     *
     * Response Sample:
     * {
     *   "totalTools": 120,
     *   "availableTools": 95,
     *   "issuedTools": 15,
     *   "pendingRequests": 4,
     *   "overdueReturns": 2
     * }
     *
     * @param location Location to retrieve dashboard metrics for
     * @return ResponseEntity containing dashboard summary data
     */
    @GetMapping
    public ResponseEntity<AdminDashboardResponse> getDashboard(@RequestParam String location) {
        AdminDashboardResponse resp = dashboardService.getDashboardByLocation(location);
        return ResponseEntity.ok(resp);
    }
}
