package com.tms.restapi.toolsmanagement.notification.repository;

import com.tms.restapi.toolsmanagement.notification.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Superadmin notifications - all critical system notifications
    @Query("SELECT n FROM Notification n WHERE n.targetRole IN ('SUPERADMIN', 'ALL') " +
           "AND n.type IN ('TOOL_MISSING', 'TOOL_OBSOLETE', 'TOOL_DAMAGED', 'RETURN_OVERDUE') " +
           "ORDER BY n.createdAt DESC")
    List<Notification> findSuperadminNotifications();

    // Admin notifications - location-based
    @Query("SELECT n FROM Notification n WHERE (n.targetRole IN ('ADMIN', 'ALL') OR n.location = :location) " +
           "AND n.type IN ('CALIBRATION', 'RETURN_OVERDUE', 'TOOL_DAMAGED') " +
           "AND (n.location = :location OR n.location IS NULL) " +
           "ORDER BY n.createdAt DESC")
    List<Notification> findAdminNotificationsByLocation(@Param("location") String location);

    // Trainer notifications - trainer-specific
    @Query("SELECT n FROM Notification n WHERE n.targetRole IN ('TRAINER', 'ALL') " +
           "AND n.trainerId = :trainerId " +
           "AND n.type IN ('ISSUANCE_REMINDER', 'RETURN_DUE_TOMORROW', 'RETURN_OVERDUE') " +
           "ORDER BY n.createdAt DESC")
    List<Notification> findTrainerNotifications(@Param("trainerId") Long trainerId);

    // Find notifications by type and severity
    List<Notification> findByTypeAndSeverity(String type, String severity);

    // Find all critical notifications
    @Query("SELECT n FROM Notification n WHERE n.severity = 'CRITICAL' ORDER BY n.createdAt DESC")
    List<Notification> findCriticalNotifications();

    // Find unread notifications for a role
    @Query("SELECT n FROM Notification n WHERE n.targetRole = :role AND n.status = 'UNREAD' ORDER BY n.createdAt DESC")
    List<Notification> findUnreadByRole(@Param("role") String role);

    // Find notifications by location
    List<Notification> findByLocation(String location);

    // Find notifications by trainer ID
    List<Notification> findByTrainerId(Long trainerId);

    // Find notifications created after a specific date
    List<Notification> findByCreatedAtAfter(LocalDateTime dateTime);
}
