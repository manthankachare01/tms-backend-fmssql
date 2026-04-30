package com.tms.restapi.toolsmanagement.notification.service;

import com.tms.restapi.toolsmanagement.notification.model.Notification;
import com.tms.restapi.toolsmanagement.notification.dto.NotificationDto;
import com.tms.restapi.toolsmanagement.notification.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    /**
     * Get all critical system notifications for SUPERADMIN
     * Includes: TOOL_MISSING, TOOL_OBSOLETE, TOOL_DAMAGED, ALL OVERDUE RETURNS
     * Ordered by createdAt DESC
     */
    public List<NotificationDto> getSuperadminNotifications() {
        List<Notification> notifications = notificationRepository.findSuperadminNotifications();
        return notifications.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get location-based notifications for ADMIN
     * Includes: Calibration reminders, Trainer overdue returns, Damaged tools in that location
     * Ordered by createdAt DESC
     */
    public List<NotificationDto> getAdminNotificationsByLocation(String location) {
        if (location == null || location.trim().isEmpty()) {
            throw new IllegalArgumentException("Location parameter is required");
        }

        List<Notification> notifications = notificationRepository.findAdminNotificationsByLocation(location);
        return notifications.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get trainer-specific notifications
     * Includes: Tool issuance reminders, Return due tomorrow, Overdue return alerts, Training schedule reminders
     * Ordered by createdAt DESC
     */
    public List<NotificationDto> getTrainerNotifications(Long trainerId) {
        if (trainerId == null || trainerId <= 0) {
            throw new IllegalArgumentException("Valid trainerId is required");
        }

        List<Notification> notifications = notificationRepository.findTrainerNotifications(trainerId);
        return notifications.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Create a new notification
     */
    public NotificationDto createNotification(Notification notification) {
        Notification saved = notificationRepository.save(notification);
        return convertToDTO(saved);
    }

    /**
     * Mark notification as read
     */
    public NotificationDto markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found with id: " + notificationId));

        notification.setStatus("READ");
        notification.setReadAt(java.time.LocalDateTime.now());

        Notification updated = notificationRepository.save(notification);
        return convertToDTO(updated);
    }

    /**
     * Get all critical notifications
     */
    public List<NotificationDto> getCriticalNotifications() {
        List<Notification> notifications = notificationRepository.findCriticalNotifications();
        return notifications.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get unread notifications for a specific role
     */
    public List<NotificationDto> getUnreadNotificationsByRole(String role) {
        List<Notification> notifications = notificationRepository.findUnreadByRole(role);
        return notifications.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Convert Notification entity to DTO
     */
    private NotificationDto convertToDTO(Notification notification) {
        return new NotificationDto(
                notification.getId(),
                notification.getType(),
                notification.getSeverity(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getToolId(),
                notification.getTrainerId(),
                notification.getIssuanceId(),
                notification.getLocation(),
                notification.getTargetRole(),
                notification.getStatus(),
                notification.getCreatedAt(),
                notification.getReadAt()
        );
    }
}
