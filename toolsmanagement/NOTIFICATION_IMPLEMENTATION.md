# Notification System Implementation Summary

## Overview
A comprehensive notification system has been implemented for the Tools Management REST API to support three different user roles: SUPERADMIN, ADMIN, and TRAINER.

## Implemented Endpoints

### 1. SUPERADMIN Notifications
**Endpoint:** `GET /api/notifications/superadmin`

**Description:** Fetch ALL critical system notifications

**Includes:**
- TOOL_MISSING - Tools that are missing from inventory
- TOOL_OBSOLETE - Outdated/obsolete tools
- TOOL_DAMAGED - Tools that are damaged
- RETURN_OVERDUE - All overdue tool returns

**Response:** Ordered by `createdAt DESC` (newest first)

**Response Format:**
```json
{
  "success": true,
  "count": 5,
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

### 2. ADMIN Notifications (Location-Based)
**Endpoint:** `GET /api/notifications/admin?location={location}`

**Description:** Fetch notifications ONLY for given location

**Parameters:**
- `location` (required): The location to fetch notifications for (e.g., "Pune", "Mumbai")

**Includes:**
- Calibration reminders - Tools requiring calibration
- Trainer overdue returns - Trainers with overdue tool returns
- Damaged tools in that location - Tools damaged at the specific location

**Response:** Ordered by `createdAt DESC` (newest first)

**Response Format:**
```json
{
  "success": true,
  "location": "Pune",
  "count": 3,
  "data": [
    {
      "id": 2,
      "type": "CALIBRATION",
      "severity": "HIGH",
      "title": "Tool Calibration Due",
      "message": "Tool 'Pressure Gauge' (SI No: SI-015) at location 'Pune' requires calibration. Due date: 2025-12-25 (in 2 days)",
      "toolId": 15,
      "trainerId": null,
      "issuanceId": null,
      "location": "Pune",
      "targetRole": "ADMIN",
      "status": "UNREAD",
      "createdAt": "2025-12-23T09:15:00",
      "readAt": null
    }
  ]
}
```

---

### 3. TRAINER Notifications (Trainer-Specific)
**Endpoint:** `GET /api/notifications/trainer?trainerId={trainerId}`

**Description:** Fetch notifications ONLY for that trainer

**Parameters:**
- `trainerId` (required): The trainer ID to fetch notifications for

**Includes:**
- Tool issuance reminders - Tools issued to the trainer
- Return due tomorrow - Tools due for return tomorrow
- Overdue return alerts - Tools past their return date
- Training schedule reminders - Training-related reminders

**Response:** Ordered by `createdAt DESC` (newest first)

**Response Format:**
```json
{
  "success": true,
  "trainerId": 5,
  "count": 2,
  "data": [
    {
      "id": 3,
      "type": "RETURN_OVERDUE",
      "severity": "CRITICAL",
      "title": "Tool Return Overdue",
      "message": "Trainer 'John Doe' has not returned tools from 'Advanced Python' training. Due date was 2025-12-20, days overdue: 3",
      "toolId": null,
      "trainerId": 5,
      "issuanceId": 12,
      "location": "Pune",
      "targetRole": "TRAINER",
      "status": "UNREAD",
      "createdAt": "2025-12-23T08:00:00",
      "readAt": null
    }
  ]
}
```

---

## Additional Endpoints

### 4. Get All Critical Notifications
**Endpoint:** `GET /api/notifications/critical`

**Description:** Fetch all critical notifications across the system (severity = CRITICAL)

**Response Format:** Same as above, includes all critical notifications

---

### 5. Mark Notification as Read
**Endpoint:** `PUT /api/notifications/{notificationId}/read`

**Description:** Mark a specific notification as read

**Parameters:**
- `notificationId` (path): The notification ID to mark as read

**Response Format:**
```json
{
  "success": true,
  "message": "Notification marked as read",
  "data": {
    "id": 1,
    "type": "TOOL_MISSING",
    "status": "READ",
    "readAt": "2025-12-23T10:45:00",
    ...
  }
}
```

---

## Error Handling

All endpoints include comprehensive error handling:

**400 Bad Request:** When required parameters are missing or invalid
```json
{
  "success": false,
  "error": "Invalid trainerId parameter",
  "message": "Valid trainerId is required"
}
```

**404 Not Found:** When a resource doesn't exist
```json
{
  "success": false,
  "error": "Notification not found",
  "message": "Notification not found with id: 999"
}
```

**500 Internal Server Error:** For server-side errors
```json
{
  "success": false,
  "error": "Failed to fetch superadmin notifications",
  "message": "[Error details]"
}
```

---

## Data Models

### Notification Entity
```
- id (Long): Auto-generated ID
- type (String): Notification type (TOOL_MISSING, TOOL_OBSOLETE, TOOL_DAMAGED, CALIBRATION, RETURN_OVERDUE, RETURN_DUE_TOMORROW, ISSUANCE_REMINDER)
- severity (String): CRITICAL, HIGH, MEDIUM, LOW
- title (String): Notification title
- message (String): Detailed notification message
- toolId (Long): Referenced tool ID (nullable)
- trainerId (Long): Referenced trainer ID (nullable)
- issuanceId (Long): Referenced issuance ID (nullable)
- location (String): Location for location-based notifications
- targetRole (String): SUPERADMIN, ADMIN, TRAINER, or ALL
- status (String): UNREAD or READ
- createdAt (LocalDateTime): Auto-set on creation
- readAt (LocalDateTime): Set when notification is marked as read
```

### NotificationDto (Transfer Object)
Same structure as the entity for API responses.

---

## Database Schema

```sql
CREATE TABLE notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    type VARCHAR(50) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT,
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
);
```

---

## Helper Service: NotificationGeneratorService

A utility service for creating notifications based on system events:

**Methods:**
- `createToolMissingNotification(Tool)` - Create notification for missing tool
- `createToolObsoleteNotification(Tool)` - Create notification for obsolete tool
- `createToolDamagedNotification(Tool)` - Create notification for damaged tool
- `createReturnOverdueNotification(Issuance)` - Create notification for overdue returns
- `createReturnDueTomorrowNotification(Issuance)` - Create notification for return due tomorrow
- `createIssuanceReminderNotification(Issuance)` - Create notification for tool issuance
- `createCalibrationReminderNotification(Tool, int)` - Create notification for calibration due

---

## Implementation Details

### File Structure
```
notification/
├── model/
│   └── Notification.java (JPA Entity)
├── dto/
│   └── NotificationDto.java (Data Transfer Object)
├── repository/
│   └── NotificationRepository.java (JPA Repository with custom queries)
├── service/
│   ├── NotificationService.java (Business logic)
│   └── NotificationGeneratorService.java (Helper for creating notifications)
├── controller/
│   └── NotificationController.java (REST Endpoints)
└── CalibrationNotificationService.java (Existing service)
```

### Key Features
1. **Role-Based Filtering** - Notifications filtered by user role
2. **Location-Based Filtering** - Admin notifications filtered by location
3. **Trainer-Specific Filtering** - Trainer notifications filtered by trainer ID
4. **Read/Unread Status** - Track notification status with timestamps
5. **Severity Levels** - Notifications prioritized by severity (CRITICAL, HIGH, MEDIUM, LOW)
6. **Ordering** - All results ordered by creation date (newest first)
7. **Error Handling** - Comprehensive error handling with meaningful messages
8. **CORS Support** - Endpoints support cross-origin requests

---

## Usage Examples

### Get Superadmin Notifications
```bash
curl -X GET "http://localhost:8080/api/notifications/superadmin"
```

### Get Admin Notifications for Pune Location
```bash
curl -X GET "http://localhost:8080/api/notifications/admin?location=Pune"
```

### Get Trainer Notifications
```bash
curl -X GET "http://localhost:8080/api/notifications/trainer?trainerId=5"
```

### Mark Notification as Read
```bash
curl -X PUT "http://localhost:8080/api/notifications/1/read"
```

### Get Critical Notifications
```bash
curl -X GET "http://localhost:8080/api/notifications/critical"
```

---

## Integration Points

The notification system can be integrated with existing services:

1. **Tool Management** - When tools are marked as missing, obsolete, or damaged
2. **Issuance System** - When tools are issued or return dates approach
3. **Calibration Service** - When calibration reminders are due
4. **Email Service** - To send email notifications alongside database records

---

## Future Enhancements

1. Notification preferences/settings per user
2. Email/SMS delivery integration
3. Real-time notifications via WebSockets
4. Notification expiration/archival
5. Bulk operations (mark multiple as read)
6. Notification categories/tags
7. Notification templates
8. Delivery tracking (delivered, failed, etc.)
