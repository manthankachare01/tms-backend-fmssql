package com.tms.restapi.toolsmanagement.admin.service;

import com.tms.restapi.toolsmanagement.admin.dto.ActivityDto;
import com.tms.restapi.toolsmanagement.admin.dto.AdminDashboardResponse;
import com.tms.restapi.toolsmanagement.issuance.model.Issuance;
import com.tms.restapi.toolsmanagement.issuance.model.ReturnRecord;
import com.tms.restapi.toolsmanagement.issuance.model.ReturnItem;
import com.tms.restapi.toolsmanagement.issuance.repository.IssuanceRepository;
import com.tms.restapi.toolsmanagement.issuance.repository.ReturnRepository;
import com.tms.restapi.toolsmanagement.kit.repository.KitRepository;
import com.tms.restapi.toolsmanagement.tools.model.Tool;
import com.tms.restapi.toolsmanagement.tools.repository.ToolRepository;
import com.tms.restapi.toolsmanagement.kit.model.Kit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminDashboardService {

    @Autowired
    private ToolRepository toolRepository;

    @Autowired
    private KitRepository kitRepository;

    @Autowired
    private IssuanceRepository issuanceRepository;

    @Autowired
    private ReturnRepository returnRepository;

    @Autowired
    private com.tms.restapi.toolsmanagement.issuance.service.IssuanceService issuanceService;

    public AdminDashboardResponse getDashboardByLocation(String location) {
        AdminDashboardResponse resp = new AdminDashboardResponse();
        if (location == null) return resp;

        // Update overdue statuses first
        issuanceService.updateOverdueStatuses();

        LocalDate today = LocalDate.now();
        LocalDateTime startOfToday = today.atStartOfDay();
        LocalDateTime endOfToday = today.plusDays(1).atStartOfDay();

        List<Tool> tools = toolRepository.findByLocation(location);
        List<Kit> kits = kitRepository.findByLocationIgnoreCase(location);
        List<Issuance> issuances = issuanceRepository.findByLocation(location);
        List<ReturnRecord> returns = returnRepository.findByIssuance_Location(location);

        resp.setTotalTools(tools == null ? 0 : tools.size());
        resp.setTotalKits(kits == null ? 0 : kits.size());

        int issuanceToday = 0;
        int overdueCount = 0;
        if (issuances != null) {
            for (Issuance i : issuances) {
                // Only count issuanceToday if approved by admin (approvalDate not null or status not PENDING/REJECTED)
                if (i.getIssuanceDate() != null && i.getIssuanceDate().isAfter(startOfToday) && i.getIssuanceDate().isBefore(endOfToday) && 
                    (i.getApprovalDate() != null && i.getStatus() != null && !i.getStatus().equalsIgnoreCase("PENDING") && !i.getStatus().equalsIgnoreCase("REJECTED"))) {
                    issuanceToday++;
                }
                if (i.getStatus() != null && i.getStatus().equalsIgnoreCase("OVERDUE")) overdueCount++;
            }
        }
        resp.setIssuanceToday(issuanceToday);
        resp.setOverdueIssuance(overdueCount);

        int returnsToday = 0;
        if (returns != null) {
            for (ReturnRecord rr : returns) {
                if (rr.getActualReturnDate() != null && rr.getActualReturnDate().isAfter(startOfToday) && rr.getActualReturnDate().isBefore(endOfToday)) returnsToday++;
            }
        }
        
        // Count tools with damaged/missing/obsolete condition in the location (from tools table only)
        int damagedCount = 0;
        if (tools != null) {
            for (Tool t : tools) {
                String cond = t.getCondition();
                if (cond != null && (cond.equalsIgnoreCase("damaged") || cond.equalsIgnoreCase("missing") || cond.equalsIgnoreCase("obsolete"))) {
                    damagedCount++;
                }
            }
        }
        
        resp.setReturnsToday(returnsToday);
        resp.setDamagedCount(damagedCount);

        // calibration required: only count tools that have calibration required flag set to true
        int calibCount = 0;
        if (tools != null) {
            for (Tool t : tools) {
                if (t.isCalibrationRequired()) {
                    calibCount++;
                }
            }
        }
        resp.setCalibrationRequiredCount(calibCount);

        // Recent activities: gather issuances (issued), returns, added tools/kits (best-effort using id desc)
        List<ActivityDto> activities = new ArrayList<>();

        if (issuances != null) {
            for (Issuance i : issuances) {
                // Only show approved issuances in activity feed (status=ISSUED or OVERDUE or RETURNED)
                if (i.getStatus() != null && !i.getStatus().equals("PENDING")) {
                    String names = buildItemList(i.getToolIds(), i.getKitIds());
                    String type = i.getToolIds() != null && !i.getToolIds().isEmpty() ? "Tool" : "Kit";
                    
                    // Show approval info in activity if this was approved by an admin
                    String activity = i.getApprovedBy() != null ? "Tool Issued" : "Tool Issued";
                    ActivityDto act = new ActivityDto(activity, i.getTrainerName(), type, names, i.getIssuanceDate());
                    
                    LocalDateTime ts = i.getApprovalDate() != null ? i.getApprovalDate() : i.getIssuanceDate();
                    act.setTimestamp(ts);
                    act.setTimeAgo(formatTimeAgo(ts));
                    activities.add(act);
                }
            }
        }

        if (returns != null) {
            for (ReturnRecord rr : returns) {
                String names = buildReturnItemList(rr);
                ActivityDto act = new ActivityDto("Tool Returned", rr.getIssuance() != null ? rr.getIssuance().getTrainerName() : "", "Mixed", names, rr.getActualReturnDate());
                LocalDateTime ts = rr.getActualReturnDate(); // Use accurate timestamp directly
                act.setTimestamp(ts);
                act.setTimeAgo(formatTimeAgo(ts));
                activities.add(act);
            }
        }

        // Add latest tools and kits as "Added Tool/Kit" if there is space
        List<Tool> latestTools = tools == null ? List.of() : tools.stream()
                .sorted(Comparator.comparingLong(t -> t.getId() == null ? 0L : -t.getId()))
                .limit(8).collect(Collectors.toList());
        for (Tool t : latestTools) {
            ActivityDto act = new ActivityDto("Added Tool", t.getCreatedBy(), "Tool", t.getDescription(), t.getLocation());
            LocalDateTime ts = t.getCreatedAt();
            act.setTimestamp(ts);
            act.setTimeAgo(formatTimeAgo(ts));
            act.setDate(t.getCreatedAt() != null ? t.getCreatedAt().toLocalDate() : null);
            activities.add(act);
        }

        List<Kit> latestKits = kits == null ? List.of() : kits.stream()
                .sorted(Comparator.comparingLong(k -> k.getId() == null ? 0L : -k.getId()))
                .limit(8).collect(Collectors.toList());
        for (Kit k : latestKits) {
            ActivityDto act = new ActivityDto("Added Kit", k.getCreatedBy(), "Kit", k.getKitName(), k.getLocation());
            LocalDateTime ts = k.getCreatedAt();
            act.setTimestamp(ts);
            act.setTimeAgo(formatTimeAgo(ts));
            act.setDate(k.getCreatedAt() != null ? k.getCreatedAt().toLocalDate() : null);
            activities.add(act);
        }

        // sort activities by date (null dates last), keep newest first; if date null, preserve insertion order after dated ones
        List<ActivityDto> sorted = activities.stream()
                .sorted((a,b) -> {
                    if (a.getDate() == null && b.getDate() == null) return 0;
                    if (a.getDate() == null) return 1;
                    if (b.getDate() == null) return -1;
                    return b.getDate().compareTo(a.getDate());
                })
                .limit(8)
                .collect(Collectors.toList());

        resp.setRecentActivities(sorted);

        return resp;
    }

    private String buildItemList(List<Long> toolIds, List<Long> kitIds) {
        List<String> parts = new ArrayList<>();
        if (toolIds != null) {
            for (Long id : toolIds) {
                Tool t = toolRepository.findById(id).orElse(null);
                parts.add(t != null ? t.getDescription() : "Tool " + id);
            }
        }
        if (kitIds != null) {
            for (Long id : kitIds) {
                Kit k = kitRepository.findById(id).orElse(null);
                parts.add(k != null ? k.getKitName() : "Kit " + id);
            }
        }
        return String.join(", ", parts);
    }

    private String buildReturnItemList(ReturnRecord rr) {
        List<String> parts = new ArrayList<>();
        if (rr.getItems() != null) {
            for (ReturnItem ri : rr.getItems()) {
                if (ri.getToolId() != null) {
                    Tool t = toolRepository.findById(ri.getToolId()).orElse(null);
                    parts.add(t != null ? t.getDescription() : "Tool " + ri.getToolId());
                } else if (ri.getKitId() != null) {
                    Kit k = kitRepository.findById(ri.getKitId()).orElse(null);
                    parts.add(k != null ? k.getKitName() : "Kit " + ri.getKitId());
                }
            }
        }
        return String.join(", ", parts);
    }

    private String formatTimeAgo(LocalDateTime ts) {
        if (ts == null) return null;
        Duration d = Duration.between(ts, LocalDateTime.now());
        if (d.isNegative()) return "just now";
        long secs = d.getSeconds();
        if (secs < 60) return secs + " sec" + (secs != 1 ? "s" : "") + " ago";
        long mins = secs / 60;
        if (mins < 60) return mins + " min" + (mins != 1 ? "s" : "") + " ago";
        long hours = mins / 60;
        if (hours < 24) return hours + " hour" + (hours != 1 ? "s" : "") + " ago";
        long days = hours / 24;
        if (days < 30) return days + " day" + (days != 1 ? "s" : "") + " ago";
        long months = days / 30;
        if (months < 12) return months + " month" + (months != 1 ? "s" : "") + " ago";
        long years = months / 12;
        return years + " year" + (years != 1 ? "s" : "") + " ago";
    }
}
