# Notification System - Quick Reference

## Files Created/Modified

### Model Layer
1. **[notification/model/Notification.java](src/main/java/com/tms/restapi/toolsmanagement/notification/model/Notification.java)**
   - JPA Entity for storing notifications
   - Fields: type, severity, title, message, toolId, trainerId, issuanceId, location, targetRole, status, createdAt, readAt

### DTO Layer
2. **[notification/dto/NotificationDto.java](src/main/java/com/tms/restapi/toolsmanagement/notification/dto/NotificationDto.java)**
   - Data Transfer Object for API responses
   - Maps Notification entity to JSON

### Repository Layer
3. **[notification/repository/NotificationRepository.java](src/main/java/com/tms/restapi/toolsmanagement/notification/repository/NotificationRepository.java)**
   - JPA Repository with custom queries
   - Methods: findSuperadminNotifications(), findAdminNotificationsByLocation(), findTrainerNotifications()

### Service Layer
4. **[notification/service/NotificationService.java](src/main/java/com/tms/restapi/toolsmanagement/notification/service/NotificationService.java)**
   - Business logic for notification operations
   - Methods: getSuperadminNotifications(), getAdminNotificationsByLocation(), getTrainerNotifications(), markAsRead(), etc.

5. **[notification/service/NotificationGeneratorService.java](src/main/java/com/tms/restapi/toolsmanagement/notification/service/NotificationGeneratorService.java)**
   - Helper service for creating notifications
   - Methods: createToolMissingNotification(), createReturnOverdueNotification(), etc.

### Controller Layer
6. **[notification/controller/NotificationController.java](src/main/java/com/tms/restapi/toolsmanagement/notification/controller/NotificationController.java)**
   - REST API endpoints
   - Routes:
     - GET /api/notifications/superadmin
     - GET /api/notifications/admin?location={location}
     - GET /api/notifications/trainer?trainerId={trainerId}
     - GET /api/notifications/critical
     - PUT /api/notifications/{notificationId}/read

---

## API Endpoints Summary

### 1. Superadmin Notifications
```
GET /api/notifications/superadmin
```
Returns all critical system notifications (TOOL_MISSING, TOOL_OBSOLETE, TOOL_DAMAGED, RETURN_OVERDUE)

### 2. Admin Notifications
```
GET /api/notifications/admin?location=Pune
```
Returns location-specific notifications for admins (CALIBRATION, RETURN_OVERDUE, TOOL_DAMAGED)

### 3. Trainer Notifications
```
GET /api/notifications/trainer?trainerId=5
```
Returns trainer-specific notifications (ISSUANCE_REMINDER, RETURN_DUE_TOMORROW, RETURN_OVERDUE)

### 4. Critical Notifications
```
GET /api/notifications/critical
```
Returns all critical severity notifications across the system

### 5. Mark as Read
```
PUT /api/notifications/1/read
```
Marks a specific notification as read

---

## Notification Types

| Type | Severity | Target | Description |
|------|----------|--------|-------------|
| TOOL_MISSING | CRITICAL | SUPERADMIN | Tool is missing from inventory |
| TOOL_OBSOLETE | HIGH | SUPERADMIN | Tool is marked as obsolete |
| TOOL_DAMAGED | HIGH | SUPERADMIN | Tool is damaged |
| RETURN_OVERDUE | CRITICAL | SUPERADMIN/TRAINER | Tool return is overdue |
| CALIBRATION | HIGH/MEDIUM | ADMIN | Tool calibration is due |
| RETURN_DUE_TOMORROW | MEDIUM | TRAINER | Tool return is due tomorrow |
| ISSUANCE_REMINDER | MEDIUM | TRAINER | Tool has been issued |

---

## Sample Response

```json
{
  "success": true,
  "count": 2,
  "data": [
    {
      "id": 1,
      "type": "TOOL_MISSING",
      "severity": "CRITICAL",
      "title": "Tool Missing",
      "message": "Tool 'Multimeter' (SI No: SI-001) is missing from location 'Pune'",
      "toolId": 10,
      "trainerId": null,
      "issuanceId": null,
      "location": "Pune",
      "targetRole": "SUPERADMIN",
      "status": "UNREAD",
      "createdAt": "2025-12-23T10:30:00",
      "readAt": null
    }
  ]
}
```

---

## Integration Guide

### Using NotificationGeneratorService in Other Services

```java
@Service
public class ToolService {
    @Autowired
    private NotificationGeneratorService notificationGeneratorService;
    
    public void markToolAsMissing(Tool tool) {
        // ... mark tool as missing
        notificationGeneratorService.createToolMissingNotification(tool);
    }
}
```

### Calling from Controllers

```java
@RestController
public class IssuanceController {
    @Autowired
    private NotificationGeneratorService notificationGenerator;
    
    @PostMapping("/issuance")
    public ResponseEntity<?> createIssuance(@RequestBody Issuance issuance) {
        // ... create issuance
        notificationGenerator.createReturnOverdueNotification(issuance);
        return ResponseEntity.ok("Issuance created");
    }
}
```

---

## Database Migration

Run this SQL to create the notifications table:

```sql
CREATE TABLE notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    type VARCHAR(50) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    title VARCHAR(255) NOT NULL,
    message LONGTEXT,
    tool_id BIGINT,
    trainer_id BIGINT,
    issuance_id BIGINT,
    location VARCHAR(100),
    target_role VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at DATETIME NOT NULL,
    read_at DATETIME,
    INDEX idx_target_role (target_role),
    INDEX idx_created_at (created_at),
    INDEX idx_location (location),
    INDEX idx_trainer_id (trainer_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

---

## Testing

### Test Superadmin Endpoint
```bash
curl -X GET "http://localhost:8080/api/notifications/superadmin" \
  -H "Content-Type: application/json"
```

### Test Admin Endpoint
```bash
curl -X GET "http://localhost:8080/api/notifications/admin?location=Pune" \
  -H "Content-Type: application/json"
```

### Test Trainer Endpoint
```bash
curl -X GET "http://localhost:8080/api/notifications/trainer?trainerId=5" \
  -H "Content-Type: application/json"
```

### Mark Notification as Read
```bash
curl -X PUT "http://localhost:8080/api/notifications/1/read" \
  -H "Content-Type: application/json"
```

---

## Deployment Notes

1. Ensure the `notifications` table is created in the database before deployment
2. No additional dependencies were added (uses Spring Data JPA)
3. All new classes follow existing project conventions
4. Cross-origin requests are enabled for all notification endpoints
5. Comprehensive error handling is implemented for all endpoints

---

## Future Enhancement Possibilities

- [ ] WebSocket support for real-time notifications
- [ ] Email notification delivery
- [ ] SMS notification delivery
- [ ] Notification preferences per user
- [ ] Notification templates
- [ ] Bulk notification operations
- [ ] Notification expiration/archival
- [ ] Push notifications for mobile apps
