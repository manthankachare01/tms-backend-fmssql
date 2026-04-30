package com.tms.restapi.toolsmanagement.reports.controller;

import com.tms.restapi.toolsmanagement.reports.dto.DashboardOverviewDTO;
import com.tms.restapi.toolsmanagement.reports.dto.IssuanceStatisticsDTO;
import com.tms.restapi.toolsmanagement.reports.dto.LocationStatisticsDTO;
import com.tms.restapi.toolsmanagement.reports.dto.ToolStatisticsDTO;
import com.tms.restapi.toolsmanagement.reports.dto.KitStatisticsDTO;
import com.tms.restapi.toolsmanagement.reports.dto.KitLocationStatisticsDTO;
import com.tms.restapi.toolsmanagement.reports.service.ReportsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
public class ReportsController {

    @Autowired
    private ReportsService reportsService;

    /**
     * Get overall tool statistics
     * Returns: total tools, available, unavailable, availability %, needing calibration, damaged
     */
    @GetMapping("/tools/statistics")
    public ResponseEntity<?> getToolStatistics() {
        try {
            ToolStatisticsDTO stats = reportsService.getToolStatistics();
            return ResponseEntity.ok(new HashMap<String, Object>() {{
                put("success", true);
                put("data", stats);
            }});

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new HashMap<String, String>() {{
                put("error", "Failed to retrieve tool statistics: " + e.getMessage());
            }});
        }
    }

    /**
     * Get issuance statistics
     * Returns: total issuances, issued, returned, pending, approved, pending approvals, rejected
     */
    @GetMapping("/issuance/statistics")
    public ResponseEntity<?> getIssuanceStatistics() {
        try {
            IssuanceStatisticsDTO stats = reportsService.getIssuanceStatistics();
            return ResponseEntity.ok(new HashMap<String, Object>() {{
                put("success", true);
                put("data", stats);
            }});

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new HashMap<String, String>() {{
                put("error", "Failed to retrieve issuance statistics: " + e.getMessage());
            }});
        }
    }

    /**
     * Get statistics grouped by location
     * Returns: location-wise tool counts and availability
     */
    @GetMapping("/location/statistics")
    public ResponseEntity<?> getLocationStatistics(@RequestParam(required = false) String location) {
        try {
            List<LocationStatisticsDTO> stats = reportsService.getLocationStatistics(location);
            return ResponseEntity.ok(new HashMap<String, Object>() {{
                put("success", true);
                put("data", stats);
                put("total", stats.size());
            }});

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new HashMap<String, String>() {{
                put("error", "Failed to retrieve location statistics: " + e.getMessage());
            }});
        }
    }

    /**
     * Get dashboard overview with key metrics
     * Returns: total tools, issuances, trainers, admins, availability %, pending approvals, tools needing maintenance
     */
    @GetMapping("/dashboard/overview")
    public ResponseEntity<?> getDashboardOverview() {
        try {
            DashboardOverviewDTO overview = reportsService.getDashboardOverview();
            return ResponseEntity.ok(new HashMap<String, Object>() {{
                put("success", true);
                put("data", overview);
            }});

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new HashMap<String, String>() {{
                put("error", "Failed to retrieve dashboard overview: " + e.getMessage());
            }});
        }
    }

    /**
     * Get top issued tools (most frequently issued)
     * @param limit number of top tools to return (default: 10)
     */
    @GetMapping("/top-issued-tools")
    public ResponseEntity<?> getTopIssuedTools(
            @RequestParam(required = false) String location,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            // Validate limit - must be between 1 and 50
            if (limit <= 0 || limit > 50) {
                limit = 10;
            }

            List<Map<String, Object>> topTools = reportsService.getTopIssuedTools(limit, location);
            return ResponseEntity.ok(new HashMap<String, Object>() {{
                put("success", true);
                put("data", topTools);
                put("total", topTools.size());
                if (location != null && !location.trim().isEmpty()) {
                    put("location", location);
                }
            }});

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new HashMap<String, String>() {{
                put("error", "Failed to retrieve top issued tools: " + e.getMessage());
            }});
        }
    }

    /**
     * Get monthly issuance trend for last 12 months
     * Returns: monthly issue counts and return counts for chart visualization
     */
    @GetMapping("/monthly-trend")
    public ResponseEntity<?> getMonthlyTrend() {
        try {
            List<Map<String, Object>> trend = reportsService.getMonthlyIssuanceTrend();
            return ResponseEntity.ok(new HashMap<String, Object>() {{
                put("success", true);
                put("data", trend);
                put("total", trend.size());
            }});

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new HashMap<String, String>() {{
                put("error", "Failed to retrieve monthly trend: " + e.getMessage());
            }});
        }
    }

    /**
     * Get monthly issuance trend for a specific location (last 12 months)
     */
    @GetMapping("/monthly-trend/location")
    public ResponseEntity<?> getMonthlyTrendByLocation(@RequestParam String location) {
        try {
            if (location == null || location.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(new HashMap<String, String>() {{
                    put("error", "location parameter is required");
                }});
            }

            List<Map<String, Object>> trend = reportsService.getMonthlyIssuanceTrend(location);
            return ResponseEntity.ok(new HashMap<String, Object>() {{
                put("success", true);
                put("data", trend);
                put("total", trend.size());
                put("location", location);
            }});

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new HashMap<String, String>() {{
                put("error", "Failed to retrieve monthly trend: " + e.getMessage());
            }});
        }
    }

    /**
     * Get overall staff report across all locations
     */
    @GetMapping("/staff")
    public ResponseEntity<?> getStaffReport() {
        try {
            com.tms.restapi.toolsmanagement.reports.dto.StaffReportDTO dto = reportsService.getStaffReportOverall();
            return ResponseEntity.ok(new HashMap<String, Object>() {{
                put("success", true);
                put("data", dto);
            }});
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new HashMap<String, String>() {{
                put("error", "Failed to retrieve staff report: " + e.getMessage());
            }});
        }
    }

    /**
     * Get locationwise staff report (trainers + security only)
     */
    @GetMapping("/staff/location")
    public ResponseEntity<?> getStaffReportByLocation(@RequestParam String location) {
        try {
            if (location == null || location.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(new HashMap<String, String>() {{ put("error", "location parameter is required"); }});
            }
            com.tms.restapi.toolsmanagement.reports.dto.StaffLocationReportDTO dto = reportsService.getStaffReportByLocation(location);
            return ResponseEntity.ok(new HashMap<String, Object>() {{
                put("success", true);
                put("data", dto);
                put("location", location);
            }});
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new HashMap<String, String>() {{
                put("error", "Failed to retrieve staff report by location: " + e.getMessage());
            }});
        }
    }

    /**
     * Get overall kit statistics
     * Returns: total kits, available, unavailable, availability %
     */
    @GetMapping("/kits/statistics")
    public ResponseEntity<?> getKitStatistics() {
        try {
            KitStatisticsDTO stats = reportsService.getKitStatistics();
            return ResponseEntity.ok(new HashMap<String, Object>() {{
                put("success", true);
                put("data", stats);
            }});

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new HashMap<String, String>() {{
                put("error", "Failed to retrieve kit statistics: " + e.getMessage());
            }});
        }
    }

    /**
     * Get kit statistics grouped by location
     */
    @GetMapping("/kits/location/statistics")
    public ResponseEntity<?> getKitLocationStatistics(@RequestParam(required = false) String location) {
        try {
            List<KitLocationStatisticsDTO> stats = reportsService.getKitLocationStatistics(location);
            return ResponseEntity.ok(new HashMap<String, Object>() {{
                put("success", true);
                put("data", stats);
                put("total", stats.size());
            }});

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new HashMap<String, String>() {{
                put("error", "Failed to retrieve kit location statistics: " + e.getMessage());
            }});
        }
    }

    /**
     * Get tool statistics grouped by location (accepts optional `location` query param)
     * Example: /api/reports/tools/location/statistics?location=Pune
     */
    @GetMapping("/tools/location/statistics")
    public ResponseEntity<?> getToolsLocationStatistics(@RequestParam(required = false) String location) {
        try {
            List<LocationStatisticsDTO> stats = reportsService.getLocationStatistics(location);
            return ResponseEntity.ok(new HashMap<String, Object>() {{
                put("success", true);
                put("data", stats);
                put("total", stats.size());
            }});

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new HashMap<String, String>() {{
                put("error", "Failed to retrieve tool location statistics: " + e.getMessage());
            }});
        }
    }

    /**
     * Export tool usage report to Excel.
     * Optional filter values: high, less, none
     * Optional location filter for location-wise report
     */
    @GetMapping("/tools/usage/export")
    public ResponseEntity<?> exportToolUsageReport(@RequestParam(required = false) String filter,
                                                    @RequestParam(required = false) String location) {
        try {
            byte[] excelBytes = reportsService.exportToolUsageReportExcel(filter, location);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDisposition(ContentDisposition.builder("attachment").filename("tool-usage-report.xlsx").build());

            return ResponseEntity.ok().headers(headers).body(excelBytes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new HashMap<String, String>() {{
                put("error", "Failed to export tool usage report: " + e.getMessage());
            }});
        }
    }

    /**
     * Get comprehensive analytical data for dashboard
     * Combines all statistics in one endpoint for efficient loading
     */
    @GetMapping("/comprehensive")
    public ResponseEntity<?> getComprehensiveReport() {
        try {
            ToolStatisticsDTO toolStats = reportsService.getToolStatistics();
            IssuanceStatisticsDTO issuanceStats = reportsService.getIssuanceStatistics();
            DashboardOverviewDTO overview = reportsService.getDashboardOverview();
            List<LocationStatisticsDTO> locationStats = reportsService.getLocationStatistics(null);

            return ResponseEntity.ok(new HashMap<String, Object>() {{
                put("success", true);
                put("toolStatistics", toolStats);
                put("issuanceStatistics", issuanceStats);
                put("dashboardOverview", overview);
                put("locationStatistics", locationStats);
            }});

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new HashMap<String, String>() {{
                put("error", "Failed to retrieve comprehensive report: " + e.getMessage());
            }});
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(new HashMap<String, String>() {{
            put("status", "Reports service is running");
        }});
    }
}
