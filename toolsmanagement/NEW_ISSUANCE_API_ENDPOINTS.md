# New Issuance API Endpoints - Complete Reference

## Overview
All endpoints for the new issuance request approval workflow. Organized by role (Trainer/Admin).

---

## 1️⃣ TRAINER ENDPOINTS

### Create Issuance Request (PENDING)
Creates a new issuance request that requires admin approval before processing.

```
POST /api/issuance/request
Content-Type: application/json

REQUEST PAYLOAD:
{
  "trainerId": 1,
  "trainerName": "John Doe",
  "trainingName": "Advanced Tool Management",
  "toolIds": [1, 2, 3],
  "kitIds": [5, 6],
  "returnDate": "2026-02-21T14:00:00",
  "location": "Location A",
  "comment": "Tools needed for advanced training program",
  "remarks": "Priority - urgent",
  "issuanceType": "TOOL"
}

RESPONSE (201 Created):
{
  "id": null,
  "trainerId": 1,
  "trainerName": "John Doe",
  "trainingName": "Advanced Tool Management",
  "toolIds": [1, 2, 3],
  "kitIds": [5, 6],
  "returnDate": "2026-02-21T14:00:00",
  "location": "Location A",
  "comment": "Tools needed for advanced training program",
  "remarks": "Priority - urgent",
  "issuanceType": "TOOL",
  "status": "PENDING",
  "issuanceDate": "2026-01-21T10:30:45",
  "approvedBy": null,
  "approvalDate": null,
  "approvalRemark": null
}
```

---

### Get Trainer's Issuance Requests
Retrieve all issuance requests created by a specific trainer.

```
GET /api/issuance/requests/trainer/{trainerId}

Example: GET /api/issuance/requests/trainer/1

RESPONSE (200 OK):
[
  {
    "id": 1,
    "trainerId": 1,
    "trainerName": "John Doe",
    "trainingName": "Advanced Tool Management",
    "toolIds": [1, 2, 3],
    "kitIds": [5, 6],
    "status": "PENDING",
    "location": "Location A",
    "requestDate": "2026-01-21T10:30:45",
    "returnDate": "2026-02-21T14:00:00",
    "comment": "Tools needed for advanced training program",
    "approvedBy": null,
    "approvalDate": null,
    "approvalRemark": null
  },
  {
    "id": 2,
    "trainerId": 1,
    "trainerName": "John Doe",
    "status": "APPROVED",
    "approvedBy": "admin_name",
    "approvalDate": "2026-01-21T11:00:00"
  }
]
```

---

## 2️⃣ ADMIN ENDPOINTS (IssuanceController)

### Get Pending Requests for Location
Retrieve all PENDING issuance requests that need approval for a specific location.

```
GET /api/issuance/requests/pending?location={location}

Example: GET /api/issuance/requests/pending?location=Location%20A

RESPONSE (200 OK):
[
  {
    "id": 1,
    "trainerId": 1,
    "trainerName": "John Doe",
    "trainingName": "Advanced Tool Management",
    "toolIds": [1, 2, 3],
    "kitIds": [5, 6],
    "status": "PENDING",
    "location": "Location A",
    "requestDate": "2026-01-21T10:30:45",
    "returnDate": "2026-02-21T14:00:00",
    "comment": "Tools needed for training",
    "issuanceType": "TOOL",
    "remarks": "Priority",
    "approvedBy": null,
    "approvalDate": null,
    "approvalRemark": null
  },
  {
    "id": 3,
    "trainerId": 2,
    "trainerName": "Jane Smith",
    "status": "PENDING",
    "location": "Location A",
    ...
  }
]
```

---

### Get All Requests for Location (Any Status)
Retrieve all issuance requests for a location (PENDING, APPROVED, REJECTED).

```
GET /api/issuance/requests/location?location={location}

Example: GET /api/issuance/requests/location?location=Location%20A

RESPONSE (200 OK):
[
  {
    "id": 1,
    "status": "PENDING",
    "trainerId": 1,
    "trainerName": "John Doe",
    ...
  },
  {
    "id": 2,
    "status": "APPROVED",
    "trainerId": 1,
    "approvedBy": "admin_name",
    "approvalDate": "2026-01-21T11:00:00",
    ...
  },
  {
    "id": 3,
    "status": "REJECTED",
    "trainerId": 3,
    "approvedBy": "admin_name",
    "approvalDate": "2026-01-21T11:15:00",
    "approvalRemark": "Insufficient stock",
    ...
  }
]
```

---

### Get All Issuance Requests (System-wide)
Retrieve all issuance requests across all locations and trainers.

```
GET /api/issuance/requests/all

RESPONSE (200 OK):
[
  { "id": 1, "status": "PENDING", "location": "Location A", ... },
  { "id": 2, "status": "APPROVED", "location": "Location B", ... },
  { "id": 3, "status": "REJECTED", "location": "Location A", ... },
  ...
]
```

---

### Approve Issuance Request
Admin approves a PENDING issuance request. This triggers:
- ✅ Quantities deducted from tools/kits
- ✅ Issuance record created with ISSUED status
- ✅ Trainer stats updated
- ✅ Trainer receives approval email

```
POST /api/issuance/approve
Content-Type: application/json

REQUEST PAYLOAD:
{
  "requestId": 1,
  "approvedBy": "admin_name",
  "approvalRemark": "Approved - sufficient stock available"
}

RESPONSE (200 OK):
{
  "id": 101,
  "trainerId": 1,
  "trainerName": "John Doe",
  "trainingName": "Advanced Tool Management",
  "toolIds": [1, 2, 3],
  "kitIds": [5, 6],
  "returnDate": "2026-02-21T14:00:00",
  "location": "Location A",
  "comment": "Tools needed for training",
  "status": "ISSUED",
  "issuanceDate": "2026-01-21T11:00:30",
  "approvedBy": "admin_name",
  "approvalDate": "2026-01-21T11:00:30",
  "approvalRemark": "Approved - sufficient stock available"
}
```

---

### Reject Issuance Request
Admin rejects a PENDING issuance request. This:
- ❌ Does NOT deduct quantities
- ❌ Does NOT create issuance record
- ✅ Marks request as REJECTED
- ✅ Trainer receives rejection email

```
POST /api/issuance/reject
Content-Type: application/json

REQUEST PAYLOAD:
{
  "requestId": 1,
  "rejectedBy": "admin_name",
  "rejectionReason": "Insufficient stock of requested tools"
}

RESPONSE (200 OK):
"Issuance request rejected successfully"

After rejection, check request status:
GET /api/issuance/requests/pending?location=Location%20A
→ RequestId 1 will NOT appear (no longer PENDING)
```

---

## 3️⃣ ADMIN ENDPOINTS (AdminController)

### Approve Issuance Request (Alternative)
Alternative endpoint for admins to approve requests (same as IssuanceController).

```
POST /api/admins/issuance/approve
Content-Type: application/json

REQUEST PAYLOAD:
{
  "requestId": 1,
  "approvedBy": "admin_name",
  "approvalRemark": "Approved"
}

RESPONSE (200 OK):
{
  "id": 101,
  "trainerId": 1,
  "status": "ISSUED",
  "approvedBy": "admin_name",
  "approvalDate": "2026-01-21T11:00:30",
  ...
}
```

---

### Reject Issuance Request (Alternative)
Alternative endpoint for admins to reject requests (same as IssuanceController).

```
POST /api/admins/issuance/reject
Content-Type: application/json

REQUEST PAYLOAD:
{
  "requestId": 1,
  "rejectedBy": "admin_name",
  "rejectionReason": "Reason for rejection"
}

RESPONSE (200 OK):
"Issuance request rejected successfully"
```

---

## 4️⃣ ADDITIONAL EXISTING ENDPOINTS (Updated)

### Get Currently Issued Items
Returns only ISSUED issuances (not PENDING or REJECTED).

```
GET /api/issuance/issued-items

RESPONSE (200 OK):
[
  {
    "id": 101,
    "trainerId": 1,
    "trainerName": "John Doe",
    "status": "ISSUED",
    "issuanceDate": "2026-01-21T11:00:30",
    "approvedBy": "admin_name",
    ...
  }
]
```

---

### Process Return
Return issued items (unchanged from before).

```
PUT /api/issuance/process-return
Content-Type: application/json

REQUEST PAYLOAD:
{
  "issuanceId": 101,
  "actualReturnDate": "2026-02-15T10:00:00",
  "processedBy": "admin_name",
  "remarks": "All items returned in good condition",
  "items": [
    {
      "toolId": 1,
      "quantityReturned": 1,
      "condition": "Good",
      "remark": "No issues"
    },
    {
      "kitId": 5,
      "quantityReturned": 1,
      "condition": "Good",
      "remark": "Complete"
    }
  ]
}

RESPONSE (200 OK):
{
  "id": 101,
  "trainerId": 1,
  "status": "RETURNED",
  "returnDate": "2026-02-15T10:00:00",
  ...
}
```

---

### Get Return Records
Retrieve return records with optional filters.

```
GET /api/issuance/returns
GET /api/issuance/returns?location=Location%20A
GET /api/issuance/returns?trainerId=1
GET /api/issuance/returns?location=Location%20A&trainerId=1

RESPONSE (200 OK):
[
  {
    "id": 1,
    "issuanceId": 101,
    "trainerId": 1,
    "trainerName": "John Doe",
    "location": "Location A",
    "actualReturnDate": "2026-02-15T10:00:00",
    "processedBy": "admin_name",
    "remarks": "All items returned",
    "items": [
      {
        "toolId": 1,
        "condition": "Good",
        "quantityReturned": 1
      }
    ]
  }
]
```

---

## 5️⃣ COMPLETE REQUEST/RESPONSE EXAMPLE

### Scenario: Full Approval Workflow

#### Step 1: Trainer Creates Request
```
POST /api/issuance/request
{
  "trainerId": 1,
  "trainerName": "John Doe",
  "trainingName": "Advanced Training",
  "toolIds": [1, 2],
  "kitIds": [5],
  "returnDate": "2026-02-21T14:00:00",
  "location": "Location A",
  "comment": "Tools for training",
  "remarks": "Standard issuance"
}

Response: Status PENDING ✓
```

#### Step 2: Admin Views Pending Requests
```
GET /api/issuance/requests/pending?location=Location%20A

Response: Shows request with id=1, status=PENDING ✓
```

#### Step 3: Admin Approves
```
POST /api/issuance/approve
{
  "requestId": 1,
  "approvedBy": "admin_john",
  "approvalRemark": "Approved"
}

Response: 
- IssuanceRequest id=1 marked APPROVED ✓
- Issuance id=101 created with ISSUED ✓
- Quantities deducted ✓
- Trainer notified ✓
```

#### Step 4: Check Dashboard Activity
```
GET /api/admin/dashboard?location=Location%20A

Response: Shows "Tool Issued" activity with approval timestamp ✓
```

#### Step 5: Trainer Returns Items
```
PUT /api/issuance/process-return
{
  "issuanceId": 101,
  "actualReturnDate": "2026-02-15T10:00:00",
  "processedBy": "admin_john",
  "remarks": "Returned in good condition",
  "items": [
    { "toolId": 1, "quantityReturned": 1, "condition": "Good" },
    { "toolId": 2, "quantityReturned": 1, "condition": "Good" }
  ]
}

Response: Status RETURNED ✓
```

---

## 6️⃣ ERROR RESPONSES

### 400 Bad Request
Missing required fields in payload:
```
{
  "status": 400,
  "error": "Bad Request",
  "message": "requestId is required"
}
```

### 404 Not Found
Request/Issuance not found:
```
{
  "status": 404,
  "error": "Not Found",
  "message": "Issuance request not found: id=999"
}
```

### 400 Invalid Status
Trying to approve non-PENDING request:
```
{
  "status": 400,
  "error": "Bad Request",
  "message": "Issuance request is not in PENDING status. Current status: APPROVED"
}
```

---

## 7️⃣ PAYLOAD FIELD REFERENCE

### IssuanceRequest/Issuance Common Fields
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `trainerId` | Long | Yes | Trainer ID |
| `trainerName` | String | Yes | Trainer name |
| `trainingName` | String | No | Training program name |
| `toolIds` | Long[] | Conditional | Tool IDs (at least one of toolIds/kitIds) |
| `kitIds` | Long[] | Conditional | Kit IDs (at least one of toolIds/kitIds) |
| `returnDate` | DateTime | No | Expected return date (yyyy-MM-dd HH:mm:ss) |
| `location` | String | No | Location code |
| `comment` | String | No | Additional comments |
| `remarks` | String | No | Remarks |
| `issuanceType` | String | No | TOOL or KIT |

### Approval/Rejection DTO Fields
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `requestId` | Long | Yes | Request ID to approve/reject |
| `approvedBy` | String | Yes | Admin name approving |
| `approvalRemark` | String | No | Approval/rejection reason |
| `rejectedBy` | String | Yes | Admin name rejecting |
| `rejectionReason` | String | No | Rejection reason |

---

## Summary of Endpoints

| Method | Endpoint | Purpose |
|--------|----------|---------|
| POST | `/api/issuance/request` | Create request (PENDING) |
| GET | `/api/issuance/requests/trainer/{id}` | Trainer's requests |
| GET | `/api/issuance/requests/pending?location=X` | Pending requests for location |
| GET | `/api/issuance/requests/location?location=X` | All requests for location |
| GET | `/api/issuance/requests/all` | All requests |
| POST | `/api/issuance/approve` | Approve request |
| POST | `/api/issuance/reject` | Reject request |
| POST | `/api/admins/issuance/approve` | Admin approve |
| POST | `/api/admins/issuance/reject` | Admin reject |
| GET | `/api/issuance/issued-items` | Get ISSUED items |
| PUT | `/api/issuance/process-return` | Process return |
| GET | `/api/issuance/returns` | Get return records |
