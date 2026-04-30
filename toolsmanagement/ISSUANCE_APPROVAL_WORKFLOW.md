# Issuance Approval Workflow Implementation

## Overview
Implemented a complete issuance request approval workflow where:
1. Trainers create issuance requests (status: PENDING)
2. Location admins review and approve/reject requests
3. Upon approval, tools/kits are deducted and issuance becomes ISSUED
4. Tool issued activity is shown in the admin dashboard after approval
5. Return process remains unchanged

---

## New Entities Created

### 1. IssuanceRequest Entity
**File**: `issuance/model/IssuanceRequest.java`

Tracks pending issuance requests with the following fields:
- `id`: Primary key
- `trainerId`, `trainerName`, `trainingName`: Trainer information
- `requestDate`: When the request was created
- `returnDate`: Expected return date
- `status`: PENDING, APPROVED, or REJECTED
- `location`: Location of the admin
- `toolIds`, `kitIds`: Items requested
- `comment`, `issuanceType`, `remarks`: Additional details
- `approvedBy`: Admin who approved/rejected
- `approvalDate`: When approval/rejection occurred
- `approvalRemark`: Admin's comments
- `issuanceId`: Reference to created Issuance (when approved)

### 2. Updated Issuance Entity
**File**: `issuance/model/Issuance.java`

Added approval tracking fields:
- `approvedBy`: Admin who approved the request
- `approvalDate`: When approved
- `approvalRemark`: Admin's approval remarks

---

## New Repositories

### IssuanceRequestRepository
**File**: `issuance/repository/IssuanceRequestRepository.java`

Query methods:
- `findByLocation(location)`: Get all requests for a location
- `findByTrainerId(trainerId)`: Get requests for a trainer
- `findByStatus(status)`: Get requests by status
- `findByLocationAndStatus(location, status)`: Get pending requests for a location

---

## New DTOs

### 1. ApprovalRequestDto
**File**: `issuance/dto/ApprovalRequestDto.java`

Used when admin approves a request:
```json
{
  "requestId": 1,
  "approvedBy": "admin_name",
  "approvalRemark": "Approved"
}
```

### 2. RejectionRequestDto
**File**: `issuance/dto/RejectionRequestDto.java`

Used when admin rejects a request:
```json
{
  "requestId": 1,
  "rejectedBy": "admin_name",
  "rejectionReason": "Insufficient stock"
}
```

---

## Service Layer Updates

### IssuanceService Modifications
**File**: `issuance/service/IssuanceService.java`

**New Methods**:

1. **createIssuanceRequest(Issuance issuance)**
   - Creates PENDING issuance request (NOT ISSUED immediately)
   - Notifies location admins to review
   - Returns response with status: PENDING

2. **approveIssuanceRequest(requestId, approvedBy, approvalRemark)**
   - Validates request is PENDING
   - Deducts quantities from tools/kits
   - Creates actual Issuance record (status: ISSUED)
   - Updates trainer stats
   - Sends approval email to trainer
   - Returns created Issuance

3. **rejectIssuanceRequest(requestId, rejectedBy, rejectionReason)**
   - Marks request as REJECTED
   - Sends rejection email to trainer
   - No quantities deducted

4. **getPendingRequestsByLocation(location)**
   - Get all PENDING requests for a location

5. **getAllRequestsByLocation(location)**
   - Get all requests for a location (any status)

6. **getIssuanceRequestsByTrainer(trainerId)**
   - Get all requests for a specific trainer

7. **getAllIssuanceRequests()**
   - Get all issuance requests

### AdminService Updates
**File**: `admin/service/AdminService.java`

Added issuance approval delegation methods:
- `approveIssuanceRequest(requestId, approvedBy, approvalRemark)`
- `rejectIssuanceRequest(requestId, rejectedBy, rejectionReason)`

### AdminDashboardService Updates
**File**: `admin/service/AdminDashboardService.java`

**Changes**:
- Activity feed now filters to show only approved issuances (status != PENDING)
- Uses `approvalDate` instead of `issuanceDate` for activity timestamp
- Activity shows when admin approved the issuance

---

## API Endpoints

### Trainer Endpoints (IssuanceController)

**Create Issuance Request** (Creates PENDING request, NOT immediate issuance)
```
POST /api/issuance/request
Body: {
  "trainerId": 1,
  "trainerName": "John Doe",
  "trainingName": "Basic Training",
  "toolIds": [1, 2],
  "kitIds": [5],
  "returnDate": "2026-02-21T12:00:00",
  "location": "Location A",
  "comment": "Tools for training",
  "remarks": "Standard issuance"
}
Response: Issuance object with status "PENDING"
```

**Get Requests by Trainer**
```
GET /api/issuance/requests/trainer/{trainerId}
Response: List of IssuanceRequest objects
```

---

### Admin Endpoints

#### In IssuanceController

**Get Pending Requests for Location**
```
GET /api/issuance/requests/pending?location=Location%20A
Response: List of IssuanceRequest objects with status "PENDING"
```

**Get All Requests for Location**
```
GET /api/issuance/requests/location?location=Location%20A
Response: List of IssuanceRequest objects (all statuses)
```

**Get All Issuance Requests**
```
GET /api/issuance/requests/all
Response: List of all IssuanceRequest objects
```

**Approve Issuance Request**
```
POST /api/issuance/approve
Body: {
  "requestId": 1,
  "approvedBy": "admin_name",
  "approvalRemark": "Approved - sufficient stock"
}
Response: Created Issuance object with status "ISSUED"
```

**Reject Issuance Request**
```
POST /api/issuance/reject
Body: {
  "requestId": 1,
  "rejectedBy": "admin_name",
  "rejectionReason": "Insufficient stock"
}
Response: "Issuance request rejected successfully"
```

#### In AdminController

**Approve Issuance Request**
```
POST /api/admins/issuance/approve
Body: {
  "requestId": 1,
  "approvedBy": "admin_name",
  "approvalRemark": "Approved"
}
Response: Created Issuance object
```

**Reject Issuance Request**
```
POST /api/admins/issuance/reject
Body: {
  "requestId": 1,
  "rejectedBy": "admin_name",
  "rejectionReason": "Reason for rejection"
}
Response: "Issuance request rejected successfully"
```

---

## Workflow Flow

```
1. TRAINER CREATES REQUEST
   POST /api/issuance/request
   └─ IssuanceRequest created with status: PENDING
   └─ Admins notified via email
   └─ Return response with status: PENDING

2. ADMIN VIEWS PENDING REQUESTS
   GET /api/issuance/requests/pending?location=X
   └─ Returns list of PENDING requests for location

3. ADMIN APPROVES REQUEST
   POST /api/issuance/approve
   └─ Quantities deducted from tools/kits
   └─ Issuance record created (status: ISSUED)
   └─ Trainer stats updated
   └─ Trainer notified via email
   └─ Activity shown in admin dashboard

4. OR ADMIN REJECTS REQUEST
   POST /api/issuance/reject
   └─ IssuanceRequest marked REJECTED
   └─ Trainer notified via email
   └─ No quantities deducted

5. DASHBOARD ACTIVITY
   GET /api/admin/dashboard?location=X
   └─ Shows approved issuances with approval timestamp
   └─ Only approved issuances appear (not pending)

6. RETURN PROCESS (UNCHANGED)
   PUT /api/issuance/process-return
   └─ Works as before for ISSUED items
```

---

## Database Changes Required

1. Create new table: `issuance_requests_pending` (for IssuanceRequest entity)
2. Add columns to `issuance_requests`:
   - `approved_by` VARCHAR(255)
   - `approval_date` DATETIME
   - `approval_remark` TEXT

---

## Email Notifications

The system expects the following email methods in `EmailService`:
- `sendIssuanceRequestNotification(IssuanceRequest, adminEmail, adminName)` - Notify admin of pending request
- `sendIssuanceApprovalEmail(Issuance, trainerEmail, trainerName)` - Notify trainer of approval
- `sendIssuanceRejectionEmail(IssuanceRequest, trainerEmail, trainerName)` - Notify trainer of rejection

---

## Key Features

✅ **Request-Based Workflow**: Trainers create requests, admins approve  
✅ **Quantity Deduction on Approval**: Tools/kits only deducted when approved  
✅ **Admin Control**: Admins can approve or reject requests  
✅ **Approval Tracking**: Records who approved and when  
✅ **Activity Feed**: Shows approved issuances with approval timestamp  
✅ **Return Unchanged**: Return process works as before  
✅ **Email Notifications**: Admins notified of pending requests, trainers notified of approval/rejection  
✅ **Location-Based**: Requests routed to location-specific admins  

---

## Return Process Remains Unchanged

- Return workflow continues as before
- Return records still track returned items
- Quantities are restored upon return
- Damaged/missing/obsolete items still trigger notifications
- Return history displayed in activity feed
