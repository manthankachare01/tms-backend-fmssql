package com.tms.restapi.toolsmanagement.reports.service;

import com.tms.restapi.toolsmanagement.reports.dto.DashboardOverviewDTO;
import com.tms.restapi.toolsmanagement.reports.dto.IssuanceStatisticsDTO;
import com.tms.restapi.toolsmanagement.reports.dto.LocationStatisticsDTO;
import com.tms.restapi.toolsmanagement.reports.dto.ToolStatisticsDTO;
import com.tms.restapi.toolsmanagement.reports.dto.KitStatisticsDTO;
import com.tms.restapi.toolsmanagement.reports.dto.KitLocationStatisticsDTO;
import com.tms.restapi.toolsmanagement.reports.dto.ToolUsageReportDTO;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class ReportsService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Get overall tool statistics for charts
     */
    public ToolStatisticsDTO getToolStatistics() {
        try {
            // Total tools
            Long totalTools = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM tools",
                Long.class
            );

            // Available tools (where availability > 0)
            Long availableTools = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM tools WHERE availability > 0",
                Long.class
            );

            // Unavailable tools (where availability = 0)
            Long unavailableTools = totalTools - availableTools;

            // Availability percentage
            Double availabilityPercentage = totalTools > 0 ?
                (double) (availableTools * 100) / totalTools : 0.0;

            // Tools needing calibration
            Long toolsNeedingCalibration = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM tools WHERE calibration_required = true",
                Long.class
            );

            // Damaged tools
            Long damagedTools = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM tools WHERE tool_condition = 'Damaged'",
                Long.class
            );

            // Missing tools
            Long missingTools = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM tools WHERE LOWER(tool_condition) = 'missing'",
                Long.class
            );

            // Obsolete tools
            Long obsoleteTools = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM tools WHERE LOWER(tool_condition) = 'obsolete'",
                Long.class
            );

            return new ToolStatisticsDTO(
                totalTools,
                availableTools,
                unavailableTools,
                Math.round(availabilityPercentage * 100.0) / 100.0,
                toolsNeedingCalibration,
                damagedTools,
                missingTools,
                obsoleteTools
            );

        } catch (Exception e) {
            // Return default values if query fails
            return new ToolStatisticsDTO(0L, 0L, 0L, 0.0, 0L, 0L, 0L, 0L);
        }
    }

    /**
     * Get issuance statistics for charts
     */
    public IssuanceStatisticsDTO getIssuanceStatistics() {
        try {
            // Total issuances
            Long totalIssuances = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM issuance",
                Long.class
            );

            // Issued tools (status = 'Issued')
            Long issuedTools = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM issuance WHERE status = 'Issued' OR status = 'issued'",
                Long.class
            );

            // Returned tools (status = 'Returned')
            Long returnedTools = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM issuance WHERE status = 'Returned' OR status = 'returned'",
                Long.class
            );

            // Pending returns (status = 'Issued' but return_date is null or in future)
            Long pendingReturns = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM issuance WHERE (status = 'Issued' OR status = 'issued') AND (return_date IS NULL OR return_date > GETDATE())",
                Long.class
            );

            // Approved issuances (approval_status = 'Approved')
            Long approvedIssuances = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM issuance WHERE approval_status = 'Approved' OR approval_status = 'approved'",
                Long.class
            );

            // Pending approvals (approval_status = 'Pending')
            Long pendingApprovals = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM issuance WHERE approval_status = 'Pending' OR approval_status = 'pending'",
                Long.class
            );

            // Rejected issuances (approval_status = 'Rejected')
            Long rejectedIssuances = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM issuance WHERE approval_status = 'Rejected' OR approval_status = 'rejected'",
                Long.class
            );

            return new IssuanceStatisticsDTO(
                totalIssuances,
                issuedTools,
                returnedTools,
                pendingReturns,
                approvedIssuances,
                pendingApprovals,
                rejectedIssuances
            );

        } catch (Exception e) {
            // Return default values if query fails
            return new IssuanceStatisticsDTO(0L, 0L, 0L, 0L, 0L, 0L, 0L);
        }
    }

    /**
     * Get statistics by location
     */
    public List<LocationStatisticsDTO> getLocationStatistics(String location) {
        try {
            List<Map<String, Object>> results;

            if (location == null || location.trim().isEmpty()) {
                String sql = "SELECT " +
                        "location, " +
                        "COUNT(*) as total_tools, " +
                        "SUM(CASE WHEN availability > 0 THEN 1 ELSE 0 END) as available_tools, " +
                        "SUM(CASE WHEN availability = 0 THEN 1 ELSE 0 END) as unavailable_tools, " +
                        "SUM(CASE WHEN LOWER(tool_condition) = 'missing' THEN 1 ELSE 0 END) as missing_tools, " +
                        "SUM(CASE WHEN LOWER(tool_condition) = 'obsolete' THEN 1 ELSE 0 END) as obsolete_tools, " +
                        "SUM(CASE WHEN LOWER(tool_condition) = 'damaged' THEN 1 ELSE 0 END) as damaged_tools, " +
                        "ROUND((SUM(CASE WHEN availability > 0 THEN 1 ELSE 0 END) * 100.0 / COUNT(*)), 2) as availability_percentage " +
                        "FROM tools " +
                        "GROUP BY location";

                results = jdbcTemplate.queryForList(sql);
            } else {
                location = location.trim();
                String sql = "SELECT " +
                        "location, " +
                        "COUNT(*) as total_tools, " +
                        "SUM(CASE WHEN availability > 0 THEN 1 ELSE 0 END) as available_tools, " +
                        "SUM(CASE WHEN availability = 0 THEN 1 ELSE 0 END) as unavailable_tools, " +
                        "SUM(CASE WHEN LOWER(tool_condition) = 'missing' THEN 1 ELSE 0 END) as missing_tools, " +
                        "SUM(CASE WHEN LOWER(tool_condition) = 'obsolete' THEN 1 ELSE 0 END) as obsolete_tools, " +
                        "SUM(CASE WHEN LOWER(tool_condition) = 'damaged' THEN 1 ELSE 0 END) as damaged_tools, " +
                        "ROUND((SUM(CASE WHEN availability > 0 THEN 1 ELSE 0 END) * 100.0 / COUNT(*)), 2) as availability_percentage " +
                        "FROM tools " +
                        "WHERE LOWER(TRIM(location)) = LOWER(TRIM(?)) " +
                        "GROUP BY location";

                results = jdbcTemplate.queryForList(sql, location);
            }

            System.out.println("LocationStatistics Query Results: " + results.size() + " rows");
            
            return results.stream().map(row ->
                new LocationStatisticsDTO(
                    (String) row.get("location"),
                    ((Number) row.get("total_tools")).longValue(),
                    ((Number) row.get("available_tools")).longValue(),
                    ((Number) row.get("unavailable_tools")).longValue(),
                    ((Number) row.get("availability_percentage")).doubleValue(),
                    ((Number) row.get("missing_tools")).longValue(),
                    ((Number) row.get("obsolete_tools")).longValue(),
                    ((Number) row.get("damaged_tools")).longValue()
                )
            ).toList();

        } catch (Exception e) {
            // Return empty list if query fails
            System.err.println("LocationStatistics Query Error: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    /**
     * Get tool usage report records filtered by usage category, optional location, and optional months.
     * Supported filters: high, less, none.
     * Months: 1, 2, 3, 6, 12 for last X months of issuances.
     */
    public List<ToolUsageReportDTO> getToolUsageReport(String usageFilter, String location, Integer months) {
        try {
            String filter = normalizeUsageFilter(usageFilter);
            StringBuilder sql = new StringBuilder(
                "SELECT t.id, t.description, t.tool_no, t.location, COUNT(ir.id) as issue_count, t.availability " +
                "FROM tools t " +
                "LEFT JOIN issuance_requests ir ON t.id = ir.tool_id"
            );
            StringBuilder whereClause = new StringBuilder();

            // Add date filter if months is specified
            if (months != null && months > 0) {
                sql.append(" AND ir.approval_date >= DATEADD(MONTH, -" + months + ", GETDATE())");
            }

            // Add location filter
            if (location != null && !location.trim().isEmpty()) {
                whereClause.append("LOWER(TRIM(t.location)) = LOWER(TRIM('" + location.trim() + "'))");
            }

            if (whereClause.length() > 0) {
                sql.append(" WHERE ").append(whereClause);
            }

            sql.append(" GROUP BY t.id, t.description, t.tool_no, t.location, t.availability");

            // Apply usage filter on the calculated issue_count
            if ("none".equals(filter)) {
                sql.append(" HAVING COUNT(i.id) = 0");
            } else if ("less".equals(filter)) {
                sql.append(" HAVING COUNT(i.id) > 0 AND COUNT(i.id) < 5");
            } else if ("high".equals(filter)) {
                sql.append(" HAVING COUNT(i.id) >= 5");
            }

            sql.append(" ORDER BY COUNT(i.id) DESC, t.description");

            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql.toString());

            return results.stream().map(row -> {
                int issueCount = row.get("issue_count") != null ? ((Number) row.get("issue_count")).intValue() : 0;
                return new ToolUsageReportDTO(
                    row.get("id") != null ? ((Number) row.get("id")).longValue() : null,
                    row.get("description") != null ? row.get("description").toString() : "",
                    row.get("tool_no") != null ? row.get("tool_no").toString() : "",
                    row.get("location") != null ? row.get("location").toString() : "",
                    issueCount,
                    row.get("availability") != null ? ((Number) row.get("availability")).intValue() : 0,
                    determineUsageCategory(issueCount)
                );
            }).toList();

        } catch (Exception e) {
            System.err.println("ToolUsageReport Query Error: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    public byte[] exportToolUsageReportExcel(String usageFilter, String location, Integer months) {
        List<ToolUsageReportDTO> rows = getToolUsageReport(usageFilter, location, months);
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Tool Usage Report");
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Tool ID", "Tool No", "Description", "Location", "Issue Count", "Availability", "Usage Category"};
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }

            int rowIndex = 1;
            for (ToolUsageReportDTO row : rows) {
                Row excelRow = sheet.createRow(rowIndex++);
                excelRow.createCell(0).setCellValue(row.getToolId() != null ? row.getToolId() : 0);
                excelRow.createCell(1).setCellValue(row.getToolNo() != null ? row.getToolNo() : "");
                excelRow.createCell(2).setCellValue(row.getDescription() != null ? row.getDescription() : "");
                excelRow.createCell(3).setCellValue(row.getLocation() != null ? row.getLocation() : "");
                excelRow.createCell(4).setCellValue(row.getIssueCount() != null ? row.getIssueCount() : 0);
                excelRow.createCell(5).setCellValue(row.getAvailability() != null ? row.getAvailability() : 0);
                excelRow.createCell(6).setCellValue(row.getUsageCategory() != null ? row.getUsageCategory() : "");
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            System.err.println("Excel export failed: " + e.getMessage());
            e.printStackTrace();
            return new byte[0];
        }
    }

    private String normalizeUsageFilter(String usageFilter) {
        if (usageFilter == null) {
            return "";
        }
        String filter = usageFilter.trim().toLowerCase();
        return switch (filter) {
            case "high", "high-use", "high use" -> "high";
            case "less", "low", "less-use", "less use", "low-use", "low use" -> "less";
            case "none", "no-use", "no use", "not used", "unused" -> "none";
            default -> "";
        };
    }

    private String determineUsageCategory(int issueCount) {
        if (issueCount == 0) {
            return "No use";
        }
        if (issueCount < 49) {
            return "Less use";
        }
        return "High use";
    }

    /**
     * Get overall kit statistics
     */
    public KitStatisticsDTO getKitStatistics() {
        try {
            Long totalKits = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM kits",
                Long.class
            );

            Long availableKits = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM kits WHERE availability > 0",
                Long.class
            );

            Long unavailableKits = totalKits - availableKits;

            Double availabilityPercentage = totalKits > 0 ?
                Math.round((double) (availableKits * 100) / totalKits * 100.0) / 100.0 : 0.0;

            return new KitStatisticsDTO(totalKits, availableKits, unavailableKits, availabilityPercentage);

        } catch (Exception e) {
            return new KitStatisticsDTO(0L, 0L, 0L, 0.0);
        }
    }

    /**
     * Get overall staff report across all locations
     */
    public com.tms.restapi.toolsmanagement.reports.dto.StaffReportDTO getStaffReportOverall() {
        com.tms.restapi.toolsmanagement.reports.dto.StaffReportDTO dto = new com.tms.restapi.toolsmanagement.reports.dto.StaffReportDTO();
        try {
            Long activeAdmins = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM admins WHERE LOWER(TRIM(status)) = 'active'",
                Long.class
            );
            Long inactiveAdmins = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM admins WHERE LOWER(TRIM(status)) <> 'active'",
                Long.class
            );

            Long activeTrainers = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM trainers WHERE LOWER(TRIM(status)) = 'active'",
                Long.class
            );
            Long inactiveTrainers = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM trainers WHERE LOWER(TRIM(status)) <> 'active'",
                Long.class
            );

            Long activeSecurity = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM security WHERE LOWER(TRIM(status)) = 'active'",
                Long.class
            );
            Long inactiveSecurity = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM security WHERE LOWER(TRIM(status)) <> 'active'",
                Long.class
            );

            long aAdmins = activeAdmins == null ? 0 : activeAdmins;
            long iaAdmins = inactiveAdmins == null ? 0 : inactiveAdmins;
            long aTrainers = activeTrainers == null ? 0 : activeTrainers;
            long iaTrainers = inactiveTrainers == null ? 0 : inactiveTrainers;
            long aSecurity = activeSecurity == null ? 0 : activeSecurity;
            long iaSecurity = inactiveSecurity == null ? 0 : inactiveSecurity;

            dto.setActiveAdmins(aAdmins);
            dto.setInactiveAdmins(iaAdmins);
            dto.setActiveTrainers(aTrainers);
            dto.setInactiveTrainers(iaTrainers);
            dto.setActiveSecurity(aSecurity);
            dto.setInactiveSecurity(iaSecurity);

            // percentages
            long totalAdmins = aAdmins + iaAdmins;
            if (totalAdmins > 0) {
                dto.setActiveAdminPercentage((aAdmins * 100.0) / totalAdmins);
                dto.setInactiveAdminPercentage((iaAdmins * 100.0) / totalAdmins);
            }

            long totalTrainers = aTrainers + iaTrainers;
            if (totalTrainers > 0) {
                dto.setActiveTrainerPercentage((aTrainers * 100.0) / totalTrainers);
                dto.setInactiveTrainerPercentage((iaTrainers * 100.0) / totalTrainers);
            }

            long totalSecurity = aSecurity + iaSecurity;
            if (totalSecurity > 0) {
                dto.setActiveSecurityPercentage((aSecurity * 100.0) / totalSecurity);
                dto.setInactiveSecurityPercentage((iaSecurity * 100.0) / totalSecurity);
            }

            return dto;

        } catch (Exception e) {
            return dto;
        }
    }

    /**
     * Get staff report filtered by location (trainer and security only)
     */
    public com.tms.restapi.toolsmanagement.reports.dto.StaffLocationReportDTO getStaffReportByLocation(String location) {
        com.tms.restapi.toolsmanagement.reports.dto.StaffLocationReportDTO dto = new com.tms.restapi.toolsmanagement.reports.dto.StaffLocationReportDTO();
        if (location == null || location.trim().isEmpty()) return dto;
        try {
            Long activeTrainers = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM trainers WHERE LOWER(TRIM(location)) = LOWER(TRIM(?)) AND LOWER(TRIM(status)) = 'active'",
                Long.class,
                location
            );
            Long inactiveTrainers = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM trainers WHERE LOWER(TRIM(location)) = LOWER(TRIM(?)) AND LOWER(TRIM(status)) <> 'active'",
                Long.class,
                location
            );

            Long activeSecurity = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM security WHERE LOWER(TRIM(location)) = LOWER(TRIM(?)) AND LOWER(TRIM(status)) = 'active'",
                Long.class,
                location
            );
            Long inactiveSecurity = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM security WHERE LOWER(TRIM(location)) = LOWER(TRIM(?)) AND LOWER(TRIM(status)) <> 'active'",
                Long.class,
                location
            );

            long aTrainers = activeTrainers == null ? 0 : activeTrainers;
            long iaTrainers = inactiveTrainers == null ? 0 : inactiveTrainers;
            long aSecurity = activeSecurity == null ? 0 : activeSecurity;
            long iaSecurity = inactiveSecurity == null ? 0 : inactiveSecurity;

            dto.setLocation(location);
            dto.setActiveTrainers(aTrainers);
            dto.setInactiveTrainers(iaTrainers);
            dto.setActiveSecurity(aSecurity);
            dto.setInactiveSecurity(iaSecurity);

            long totalTrainers = aTrainers + iaTrainers;
            if (totalTrainers > 0) {
                dto.setTrainerActivePercentage((aTrainers * 100.0) / totalTrainers);
                dto.setTrainerInactivePercentage((iaTrainers * 100.0) / totalTrainers);
            }

            long totalSecurity = aSecurity + iaSecurity;
            if (totalSecurity > 0) {
                dto.setSecurityActivePercentage((aSecurity * 100.0) / totalSecurity);
                dto.setSecurityInactivePercentage((iaSecurity * 100.0) / totalSecurity);
            }

            return dto;
        } catch (Exception e) {
            return dto;
        }
    }

    /**
     * Get kit statistics grouped by location
     */
    public List<KitLocationStatisticsDTO> getKitLocationStatistics(String location) {
        try {
            List<Map<String, Object>> results;

            if (location == null || location.trim().isEmpty()) {
                String sql = "SELECT location, COUNT(*) as total_kits, " +
                        "COUNT(CASE WHEN availability > 0 THEN 1 END) as available_kits, " +
                        "COUNT(CASE WHEN availability = 0 THEN 1 END) as unavailable_kits, " +
                        "ROUND((COUNT(CASE WHEN availability > 0 THEN 1 END) * 100.0 / COUNT(*)), 2) as availability_percentage " +
                        "FROM kits GROUP BY location";

                results = jdbcTemplate.queryForList(sql);
            } else {
                String sql = "SELECT location, COUNT(*) as total_kits, " +
                        "COUNT(CASE WHEN availability > 0 THEN 1 END) as available_kits, " +
                        "COUNT(CASE WHEN availability = 0 THEN 1 END) as unavailable_kits, " +
                        "ROUND((COUNT(CASE WHEN availability > 0 THEN 1 END) * 100.0 / COUNT(*)), 2) as availability_percentage " +
                        "FROM kits WHERE LOWER(location) = LOWER(?) GROUP BY location";

                results = jdbcTemplate.queryForList(sql, location);
            }

            return results.stream().map(row ->
                    new KitLocationStatisticsDTO(
                            (String) row.get("location"),
                            ((Number) row.get("total_kits")).longValue(),
                            ((Number) row.get("available_kits")).longValue(),
                            ((Number) row.get("unavailable_kits")).longValue(),
                            ((Number) row.get("availability_percentage")).doubleValue()
                    )
            ).toList();

        } catch (Exception e) {
            return List.of();
        }
    }

    /**
     * Get dashboard overview with key metrics
     */
    public DashboardOverviewDTO getDashboardOverview() {
        try {
            // Total tools
            Long totalTools = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM tools",
                Long.class
            );

            // Total issuances
            Long totalIssuances = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM issuance",
                Long.class
            );

            // Total trainers
            Long totalTrainers = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM trainer",
                Long.class
            );

            // Total admins
            Long totalAdmins = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM admin",
                Long.class
            );

            // Tool availability percentage
            Long availableTools = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM tools WHERE availability > 0",
                Long.class
            );
            Double toolAvailabilityPercentage = totalTools > 0 ?
                Math.round((double) (availableTools * 100) / totalTools * 100.0) / 100.0 : 0.0;

            // Pending approvals
            Long pendingApprovals = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM issuance WHERE approval_status = 'Pending' OR approval_status = 'pending'",
                Long.class
            );

            // Tools needing maintenance (calibration or damaged)
            Long toolsNeedingMaintenance = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM tools WHERE calibration_required = true OR tool_condition = 'Damaged'",
                Long.class
            );

            return new DashboardOverviewDTO(
                totalTools,
                totalIssuances,
                totalTrainers,
                totalAdmins,
                toolAvailabilityPercentage,
                pendingApprovals,
                toolsNeedingMaintenance
            );

        } catch (Exception e) {
            // Return default values if query fails
            return new DashboardOverviewDTO(0L, 0L, 0L, 0L, 0.0, 0L, 0L);
        }
    }

    /**
     * Get top issued tools (most frequently issued)
     */
    public List<Map<String, Object>> getTopIssuedTools(int limit, String location) {
        try {
            StringBuilder sql = new StringBuilder(
                "SELECT t.id, t.description, t.tool_no, t.location, t.issue_count " +
                "FROM tools t "
            );
            
            if (location != null && !location.trim().isEmpty()) {
                sql.append("WHERE LOWER(TRIM(t.location)) = LOWER(TRIM(?)) ");
            }
            
            sql.append("ORDER BY t.issue_count DESC " +
                      "OFFSET 0 ROWS FETCH NEXT ? ROWS ONLY");

            if (location != null && !location.trim().isEmpty()) {
                return jdbcTemplate.queryForList(sql.toString(), location, limit);
            } else {
                return jdbcTemplate.queryForList(sql.toString(), limit);
            }

        } catch (Exception e) {
            return List.of();
        }
    }

    /**
     * Get monthly issuance trend
     */
    public List<Map<String, Object>> getMonthlyIssuanceTrend() {
        return getMonthlyIssuanceTrend(null);
    }

    /**
     * Get monthly issuance trend for last 12 months. If location is provided, filter by location.
     */
    public List<Map<String, Object>> getMonthlyIssuanceTrend(String location) {
        try {
            // Determine start month (first day) 11 months ago so we have 12 months including current
            java.time.YearMonth nowYm = java.time.YearMonth.now();
            java.time.YearMonth startYm = nowYm.minusMonths(11);
            java.time.LocalDate startDate = startYm.atDay(1);

            // Query issuance counts from issuance_requests using approval_date
            StringBuilder issueSql = new StringBuilder();
            issueSql.append("SELECT CONVERT(VARCHAR(7), approval_date, 120) as month, COUNT(*) as issue_count ");
            issueSql.append("FROM issuance_requests ");
            issueSql.append("WHERE approval_date >= ? ");
            if (location != null && !location.trim().isEmpty()) {
                issueSql.append("AND LOWER(TRIM(location)) = LOWER(TRIM(?)) ");
            }
            issueSql.append("GROUP BY CONVERT(VARCHAR(7), approval_date, 120) ");
            issueSql.append("ORDER BY month ASC");

            List<Map<String, Object>> issueRows;
            if (location != null && !location.trim().isEmpty()) {
                issueRows = jdbcTemplate.queryForList(issueSql.toString(), java.sql.Timestamp.valueOf(startDate.atStartOfDay()), location);
            } else {
                issueRows = jdbcTemplate.queryForList(issueSql.toString(), java.sql.Timestamp.valueOf(startDate.atStartOfDay()));
            }

            // Query return counts from return_records using actual_return_date (join to issuance_requests for location)
            StringBuilder returnSql = new StringBuilder();
            returnSql.append("SELECT CONVERT(VARCHAR(7), rr.actual_return_date, 120) as month, COUNT(*) as return_count ");
            returnSql.append("FROM return_records rr ");
            returnSql.append("JOIN issuance_requests ir ON rr.issuance_id = ir.id ");
            returnSql.append("WHERE rr.actual_return_date >= ? ");
            if (location != null && !location.trim().isEmpty()) {
                returnSql.append("AND LOWER(TRIM(ir.location)) = LOWER(TRIM(?)) ");
            }
            returnSql.append("GROUP BY CONVERT(VARCHAR(7), rr.actual_return_date, 120) ");
            returnSql.append("ORDER BY month ASC");

            List<Map<String, Object>> returnRows;
            if (location != null && !location.trim().isEmpty()) {
                returnRows = jdbcTemplate.queryForList(returnSql.toString(), java.sql.Timestamp.valueOf(startDate.atStartOfDay()), location);
            } else {
                returnRows = jdbcTemplate.queryForList(returnSql.toString(), java.sql.Timestamp.valueOf(startDate.atStartOfDay()));
            }

            java.util.Map<String, Integer> issuesMap = new java.util.HashMap<>();
            if (issueRows != null) {
                for (Map<String, Object> r : issueRows) {
                    String m = r.get("month") == null ? null : r.get("month").toString();
                    if (m != null) {
                        issuesMap.put(m, ((Number) r.getOrDefault("issue_count", 0)).intValue());
                    }
                }
            }

            java.util.Map<String, Integer> returnsMap = new java.util.HashMap<>();
            if (returnRows != null) {
                for (Map<String, Object> r : returnRows) {
                    String m = r.get("month") == null ? null : r.get("month").toString();
                    if (m != null) {
                        returnsMap.put(m, ((Number) r.getOrDefault("return_count", 0)).intValue());
                    }
                }
            }

            // Build list for each month from startYm to nowYm
            List<Map<String, Object>> result = new java.util.ArrayList<>();
            java.time.YearMonth cursor = startYm;
            while (!cursor.isAfter(nowYm)) {
                String key = cursor.toString(); // YYYY-MM
                Map<String, Object> z = new java.util.HashMap<>();
                z.put("month", key);
                z.put("issue_count", issuesMap.getOrDefault(key, 0));
                z.put("return_count", returnsMap.getOrDefault(key, 0));
                result.add(z);
                cursor = cursor.plusMonths(1);
            }

            return result;

        } catch (Exception e) {
            return List.of();
        }
    }
}
