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

/**
 * Notification Controller for delivering system notifications across user roles.
 *
 * Provides endpoints for superadmin, admin, and trainer notifications, including
 * critical alerts, overdue returns, calibration reminders, and read status updates.
 */
@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    /**
     * Retrieve all critical notifications for the superadmin.
     *
     * API Endpoint: GET /api/notifications/superadmin
     *
     * Response Sample:
     * {
     *   "success": true,
     *   "count": 3,
     *   "data": [ ... ]
     * }
     *
     * @return ResponseEntity containing superadmin notifications
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
     * Retrieve notifications for a specific admin location.
     *
     * API Endpoint: GET /api/notifications/admin?location={location}
     *
     * @param location Location to filter notifications by
     * @return ResponseEntity containing admin notifications
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
     * Retrieve notifications for a specific trainer.
     *
     * API Endpoint: GET /api/notifications/trainer?trainerId={trainerId}
     *
     * @param trainerId Trainer identifier to filter notifications
     * @return ResponseEntity containing trainer notifications
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
     * Retrieve all critical notifications.
     *
     * API Endpoint: GET /api/notifications/critical
     *
     * @return ResponseEntity containing critical notifications
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
     * Mark a notification as read.
     *
     * API Endpoint: PUT /api/notifications/{notificationId}/read
     *
     * @param notificationId Notification identifier to mark read
     * @return ResponseEntity containing updated notification status
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
