# Issuance Approval Workflow - Quick Reference

## Files Created/Modified

### Created Files:
1. ✅ `issuance/model/IssuanceRequest.java` - New request tracking entity
2. ✅ `issuance/repository/IssuanceRequestRepository.java` - Request queries
3. ✅ `issuance/dto/ApprovalRequestDto.java` - Approve request DTO
4. ✅ `issuance/dto/RejectionRequestDto.java` - Reject request DTO

### Modified Files:
1. ✅ `issuance/model/Issuance.java` - Added approval tracking fields
2. ✅ `issuance/service/IssuanceService.java` - Major: Rewritten issuance flow + new methods
3. ✅ `issuance/controller/IssuanceController.java` - Added approval endpoints
4. ✅ `admin/service/AdminService.java` - Added approval methods
5. ✅ `admin/controller/AdminController.java` - Added approval endpoints
6. ✅ `admin/service/AdminDashboardService.java` - Activity filter updated

---

## Database Migrations Required

### New Table: `issuance_requests_pending`
```sql
CREATE TABLE issuance_requests_pending (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  trainer_id BIGINT NOT NULL,
  trainer_name VARCHAR(255),
  training_name VARCHAR(255),
  request_date DATETIME,
  return_date DATETIME,
  status VARCHAR(50),
  location VARCHAR(255),
  comment TEXT,
  issuance_type VARCHAR(50),
  remarks TEXT,
  approved_by VARCHAR(255),
  approval_date DATETIME,
  approval_remark TEXT,
  issuance_id BIGINT
);
```

### Update Table: `issuance_requests` (Issuance)
```sql
ALTER TABLE issuance_requests ADD COLUMN approved_by VARCHAR(255);
ALTER TABLE issuance_requests ADD COLUMN approval_date DATETIME;
ALTER TABLE issuance_requests ADD COLUMN approval_remark TEXT;
```

### ElementCollection Tables
```sql
-- Already handled by JPA for toolIds and kitIds
-- Tables: issuance_request_pending_tool_ids, issuance_request_pending_kit_ids
```

---

## Complete API Flow

### 1️⃣ Trainer Creates Request
```
POST /api/issuance/request
→ Status: PENDING
→ Admins notified
```

### 2️⃣ Admin Views Pending
```
GET /api/issuance/requests/pending?location=Location%20A
→ List of pending requests
```

### 3️⃣ Admin Approves (Deducts Stock)
```
POST /api/issuance/approve
→ IssuanceRequest marked APPROVED
→ Issuance created with ISSUED status
→ Quantities deducted
→ Trainer notified
```

### 4️⃣ OR Admin Rejects
```
POST /api/issuance/reject
→ IssuanceRequest marked REJECTED
→ No stock deducted
→ Trainer notified
```

### 5️⃣ Dashboard Shows Approved Activity
```
GET /api/admin/dashboard?location=X
→ "Tool Issued" activity shows approval timestamp
→ Only approved items shown (not pending)
```

### 6️⃣ Return Process (Same as Before)
```
PUT /api/issuance/process-return
→ Returns deduct from issued items
```

---

## Key Changes Summary

### Before:
- Trainer creates request → Immediate issuance → Stock deducted immediately

### After:
- Trainer creates request → **PENDING status** → Stock NOT deducted yet
- Admin approves → **ISSUED status** → Stock deducted when approved
- Admin rejects → **REJECTED status** → No stock impact

---

## Endpoint Reference

### Trainer Endpoints
| Method | Endpoint | Purpose |
|--------|----------|---------|
| POST | `/api/issuance/request` | Create issuance request (PENDING) |
| GET | `/api/issuance/requests/trainer/{id}` | Get trainer's requests |

### Admin Endpoints (IssuanceController)
| Method | Endpoint | Purpose |
|--------|----------|---------|
| GET | `/api/issuance/requests/pending?location=X` | Get pending requests |
| GET | `/api/issuance/requests/location?location=X` | Get all location requests |
| GET | `/api/issuance/requests/all` | Get all requests |
| POST | `/api/issuance/approve` | Approve request |
| POST | `/api/issuance/reject` | Reject request |

### Admin Endpoints (AdminController)
| Method | Endpoint | Purpose |
|--------|----------|---------|
| POST | `/api/admins/issuance/approve` | Approve request |
| POST | `/api/admins/issuance/reject` | Reject request |

---

## Email Methods Required

Add these to `EmailService`:
```java
// Notify admin of pending issuance request
void sendIssuanceRequestNotification(IssuanceRequest req, String adminEmail, String adminName)

// Notify trainer of approval
void sendIssuanceApprovalEmail(Issuance issuance, String trainerEmail, String trainerName)

// Notify trainer of rejection
void sendIssuanceRejectionEmail(IssuanceRequest req, String trainerEmail, String trainerName)
```

---

## Testing Scenarios

### Scenario 1: Happy Path (Approve)
1. Trainer calls: `POST /api/issuance/request`
2. Admin calls: `GET /api/issuance/requests/pending?location=X`
3. Admin calls: `POST /api/issuance/approve`
4. Verify: Issuance status = ISSUED, quantities reduced, activity shown

### Scenario 2: Rejection Path
1. Trainer calls: `POST /api/issuance/request`
2. Admin calls: `POST /api/issuance/reject`
3. Verify: Request status = REJECTED, quantities unchanged

### Scenario 3: Dashboard Activity
1. Approve an issuance
2. Call: `GET /api/admin/dashboard?location=X`
3. Verify: Activity shows "Tool Issued" with approval timestamp

---

## Notes

- **Return process unchanged**: Existing return workflow works as before
- **Backward compatibility**: Old issuance endpoints still work for direct issuance
- **Location-based**: Requests route to location-specific admins
- **Audit trail**: Tracks who approved/rejected and when
