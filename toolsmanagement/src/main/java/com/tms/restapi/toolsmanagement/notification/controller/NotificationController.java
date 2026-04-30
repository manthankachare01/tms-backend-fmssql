package com.tms.restapi.toolsmanagement.notification.controller;

import com.tms.restapi.toolsmanagement.notification.dto.NotificationDto;
import com.tms.restapi.toolsmanagement.notification.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    /**
     * GET /api/notifications/superadmin
     * Fetch ALL critical system notifications for SUPERADMIN
     *
     * Includes:
     * - TOOL_MISSING
     * - TOOL_OBSOLETE
     * - TOOL_DAMAGED
     * - ALL OVERDUE RETURNS
     *
     * Response ordered by createdAt DESC
     */
    @GetMapping("/superadmin")
    public ResponseEntity<?> getSuperadminNotifications() {
        try {
            List<NotificationDto> notifications = notificationService.getSuperadminNotifications();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("count", notifications.size());
            response.put("data", notifications);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to fetch superadmin notifications");
            errorResponse.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * GET /api/notifications/admin?location={location}
     * Fetch notifications ONLY for given location (ADMIN role)
     *
     * Includes:
     * - Calibration reminders
     * - Trainer overdue returns
     * - Damaged tools in that location
     *
     * Response ordered by createdAt DESC
     *
     * @param location The location to fetch notifications for (required)
     */
    @GetMapping("/admin")
    public ResponseEntity<?> getAdminNotifications(
            @RequestParam(value = "location", required = true) String location) {
        try {
            List<NotificationDto> notifications = notificationService.getAdminNotificationsByLocation(location);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("location", location);
            response.put("count", notifications.size());
            response.put("data", notifications);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Invalid location parameter");
            errorResponse.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to fetch admin notifications");
            errorResponse.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * GET /api/notifications/trainer?trainerId={trainerId}
     * Fetch notifications ONLY for that trainer
     *
     * Includes:
     * - Tool issuance reminders
     * - Return due tomorrow
     * - Overdue return alerts
     * - Training schedule reminders
     *
     * Response ordered by createdAt DESC
     *
     * @param trainerId The trainer ID to fetch notifications for (required)
     */
    @GetMapping("/trainer")
    public ResponseEntity<?> getTrainerNotifications(
            @RequestParam(value = "trainerId", required = true) Long trainerId) {
        try {
            List<NotificationDto> notifications = notificationService.getTrainerNotifications(trainerId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("trainerId", trainerId);
            response.put("count", notifications.size());
            response.put("data", notifications);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Invalid trainerId parameter");
            errorResponse.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to fetch trainer notifications");
            errorResponse.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * GET /api/notifications/critical
     * Fetch all critical notifications across the system
     */
    @GetMapping("/critical")
    public ResponseEntity<?> getCriticalNotifications() {
        try {
            List<NotificationDto> notifications = notificationService.getCriticalNotifications();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("count", notifications.size());
            response.put("data", notifications);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to fetch critical notifications");
            errorResponse.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * PUT /api/notifications/{notificationId}/read
     * Mark a notification as read
     */
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<?> markNotificationAsRead(
            @PathVariable Long notificationId) {
        try {
            NotificationDto notification = notificationService.markAsRead(notificationId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Notification marked as read");
            response.put("data", notification);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Notification not found");
            errorResponse.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to mark notification as read");
            errorResponse.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
