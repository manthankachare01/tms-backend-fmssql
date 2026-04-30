# Issuance Status Workflow - Updated

## Summary
Updated the Issuance entity to track all status changes throughout the lifecycle:
- **PENDING** → When trainer creates request
- **ISSUED** → When admin approves
- **RETURNED** or **OVERDUE** → When items are returned
- **REJECTED** → When admin rejects

---

## Complete Issuance Lifecycle

```
┌─────────────────────────────────────────────────────────────────┐
│ 1. TRAINER CREATES REQUEST                                      │
│    POST /api/issuance/request                                   │
│                                                                 │
│    Creates TWO records:                                         │
│    • Issuance (status: PENDING)    ← Main record                │
│    • IssuanceRequest (status: PENDING)  ← Tracking record       │
│                                                                 │
│    Return: Issuance object with status="PENDING"                │
└─────────────────────────────────────────────────────────────────┘
                            ↓
                    ┌───────────────┐
                    │ ADMIN REVIEWS │
                    └───────────────┘
                    /               \
                YES /                 \ NO
                  /                     \
                 ↓                       ↓
    ┌──────────────────────┐   ┌──────────────────────┐
    │ 2. ADMIN APPROVES    │   │ 2. ADMIN REJECTS     │
    │ POST /approve        │   │ POST /reject         │
    │                      │   │                      │
    │ Updates Issuance:    │   │ Updates Issuance:    │
    │ • Status: PENDING→   │   │ • Status: PENDING→   │
    │   ISSUED             │   │   REJECTED           │
    │ • approvedBy         │   │ • approvedBy         │
    │ • approvalDate       │   │ • approvalDate       │
    │ • approvalRemark     │   │ • approvalRemark     │
    │                      │   │                      │
    │ Deducts Availability:│   │ NO Deduction         │
    │ • Each tool: -1      │   │                      │
    │ • Each kit: -1       │   │ Trainer notified     │
    │                      │   │                      │
    │ Updates IssuanceReq: │   │ Updates IssuanceReq: │
    │ • Status: APPROVED   │   │ • Status: REJECTED   │
    │                      │   │                      │
    │ Trainer notified     │   │ WORKFLOW ENDS        │
    └──────────────────────┘   └──────────────────────┘
              ↓
    ┌──────────────────────────────────────────┐
    │ 3. ITEMS ARE ISSUED TO TRAINER           │
    │    (Issuance status: ISSUED)             │
    └──────────────────────────────────────────┘
              ↓
    ┌──────────────────────────────────────────┐
    │ 4. TRAINER RETURNS ITEMS                 │
    │    PUT /api/issuance/process-return      │
    │                                          │
    │    Updates Issuance:                     │
    │    • Status: ISSUED → RETURNED           │
    │      OR ISSUED → OVERDUE (if late)       │
    │    • returnDate: actual return date      │
    │                                          │
    │    Restores Availability:                │
    │    • Each tool: +1 (or +quantity)        │
    │    • Each kit: +1 (or +quantity)         │
    │                                          │
    │    Creates ReturnRecord                  │
    │                                          │
    │    WORKFLOW COMPLETE                     │
    └──────────────────────────────────────────┘
```

---

## Issuance Status States

| Status | When | Availability | Notes |
|--------|------|--------------|-------|
| **PENDING** | Trainer creates request | No change | Waiting for admin approval |
| **ISSUED** | Admin approves | Deducted | Ready for trainer use |
| **RETURNED** | Trainer returns on time | Restored | Complete - on schedule |
| **OVERDUE** | Trainer returns late | Restored | Complete - but late |
| **REJECTED** | Admin rejects | No change | Request denied |

---

## API Response Examples

### 1. Trainer Creates Request
```
POST /api/issuance/request
{
  "trainerId": 1,
  "trainerName": "John Doe",
  "toolIds": [1, 2],
  "kitIds": [5],
  "location": "Location A"
}

RESPONSE (201 Created):
{
  "id": 101,
  "trainerId": 1,
  "trainerName": "John Doe",
  "status": "PENDING",                    ← PENDING STATUS
  "issuanceDate": "2026-01-22T10:00:00",
  "toolIds": [1, 2],
  "kitIds": [5],
  "approvedBy": null,                     ← Not yet approved
  "approvalDate": null,
  "approvalRemark": null
}
```

---

### 2. Admin Approves Request
```
POST /api/issuance/approve
{
  "requestId": 1,
  "approvedBy": "admin_name",
  "approvalRemark": "Approved"
}

RESPONSE (200 OK):
{
  "id": 101,
  "trainerId": 1,
  "status": "ISSUED",                     ← ISSUED STATUS
  "issuanceDate": "2026-01-22T10:00:00",
  "approvedBy": "admin_name",             ← Admin info
  "approvalDate": "2026-01-22T10:15:00",
  "approvalRemark": "Approved"
}

Effects:
- Tool 1 availability: 10 → 9 (deducted)
- Tool 2 availability: 8 → 7 (deducted)
- Kit 5 availability: 3 → 2 (deducted)
```

---

### 3. Admin Rejects Request
```
POST /api/issuance/reject
{
  "requestId": 1,
  "rejectedBy": "admin_name",
  "rejectionReason": "Insufficient stock"
}

RESPONSE (200 OK):
"Issuance request rejected successfully"

Issuance now has:
{
  "id": 101,
  "status": "REJECTED",                   ← REJECTED STATUS
  "approvedBy": "admin_name",
  "approvalDate": "2026-01-22T10:15:00",
  "approvalRemark": "Insufficient stock"
}

Effects:
- Tool 1 availability: 10 → 10 (NO change)
- Tool 2 availability: 8 → 8 (NO change)
- Kit 5 availability: 3 → 3 (NO change)
```

---

### 4. Trainer Returns Items
```
PUT /api/issuance/process-return
{
  "issuanceId": 101,
  "actualReturnDate": "2026-02-15T14:00:00",
  "processedBy": "admin_name",
  "remarks": "All returned in good condition",
  "items": [
    { "toolId": 1, "quantityReturned": 1, "condition": "Good" },
    { "toolId": 2, "quantityReturned": 1, "condition": "Good" }
  ]
}

RESPONSE (200 OK):
{
  "id": 101,
  "trainerId": 1,
  "status": "RETURNED",                   ← RETURNED STATUS (on time)
  "returnDate": "2026-02-15T14:00:00",
  "approvedBy": "admin_name",
  "approvalDate": "2026-01-22T10:15:00"
}

Effects:
- Tool 1 availability: 9 → 10 (restored)
- Tool 2 availability: 7 → 8 (restored)
```

---

### 5. Trainer Returns Items (Late)
```
PUT /api/issuance/process-return
{
  "issuanceId": 101,
  "actualReturnDate": "2026-02-28T14:00:00",  ← After returnDate
  ...
}

RESPONSE (200 OK):
{
  "id": 101,
  "status": "OVERDUE",                    ← OVERDUE STATUS (late return)
  "returnDate": "2026-02-28T14:00:00"
}
```

---

## Database Changes

### Issuance Record Flow
```
STEP 1: Create (Trainer)
INSERT INTO issuance_requests (
  trainer_id, trainer_name, status, issuance_date, location, ...
) VALUES (
  1, 'John Doe', 'PENDING', NOW(), 'Location A', ...
)
→ Issuance ID: 101, Status: PENDING

STEP 2: Link (Trainer)
INSERT INTO issuance_requests_pending (
  trainer_id, status, issuance_id, ...
) VALUES (
  1, 'PENDING', 101, ...
)

STEP 3: Update (Admin Approval)
UPDATE issuance_requests 
SET status='ISSUED', approved_by='admin', approval_date=NOW()
WHERE id=101

STEP 4: Availability Deduction (Admin Approval)
UPDATE tools SET availability=availability-1 WHERE id IN (1, 2)
UPDATE kits SET availability=availability-1 WHERE id=5

STEP 5: Update (Return)
UPDATE issuance_requests
SET status='RETURNED', return_date=NOW()
WHERE id=101

STEP 6: Availability Restoration (Return)
UPDATE tools SET availability=availability+1 WHERE id IN (1, 2)
UPDATE kits SET availability=availability+1 WHERE id=5
```

---

## Code Implementation

### Trainer Creates Request
```java
public Issuance createIssuanceRequest(Issuance issuance) {
    // Create Issuance with PENDING status
    Issuance pendingIssuance = new Issuance();
    pendingIssuance.setStatus("PENDING");           ← Status: PENDING
    pendingIssuance.setIssuanceDate(LocalDateTime.now());
    
    Issuance savedIssuance = issuanceRepository.save(pendingIssuance);
    
    // Create IssuanceRequest for admin tracking
    IssuanceRequest request = new IssuanceRequest();
    request.setIssuanceId(savedIssuance.getId());   ← Link to Issuance
    
    return savedIssuance;                           ← Return PENDING Issuance
}
```

### Admin Approves Request
```java
public Issuance approveIssuanceRequest(Long requestId, String approvedBy, String approvalRemark) {
    // Find existing PENDING Issuance
    Issuance existingIssuance = issuanceRepository.findById(request.getIssuanceId());
    
    // Validate availability
    for each tool:
        if (tool.getAvailability() <= 0) throw error
    
    // Deduct availability
    for each tool:
        tool.setAvailability(tool.getAvailability() - 1);
    
    // Update Issuance status
    existingIssuance.setStatus("ISSUED");           ← Status: ISSUED
    existingIssuance.setApprovedBy(approvedBy);
    existingIssuance.setApprovalDate(LocalDateTime.now());
    
    return issuanceRepository.save(existingIssuance);
}
```

### Admin Rejects Request
```java
public void rejectIssuanceRequest(Long requestId, String rejectedBy, String rejectionReason) {
    Issuance issuance = issuanceRepository.findById(request.getIssuanceId());
    
    // Update Issuance status
    issuance.setStatus("REJECTED");                 ← Status: REJECTED
    issuance.setApprovedBy(rejectedBy);
    issuance.setApprovalRemark(rejectionReason);
    
    // NO availability deduction
    
    issuanceRepository.save(issuance);
}
```

### Return Processing
```java
public Issuance processReturn(ReturnRequestDto body) {
    Issuance req = issuanceRepository.findById(body.getIssuanceId());
    
    // Determine status
    if (actualReturnDate.isAfter(plannedReturnDate)) {
        req.setStatus("OVERDUE");                   ← Status: OVERDUE (late)
    } else {
        req.setStatus("RETURNED");                  ← Status: RETURNED (on time)
    }
    
    // Restore availability
    for each tool:
        tool.setAvailability(tool.getAvailability() + 1);
    
    return issuanceRepository.save(req);
}
```

---

## Possible Issuance Statuses

| Status | Approval | Availability | Return | Notes |
|--------|----------|--------------|--------|-------|
| PENDING | ❌ No | No change | No | Waiting for admin |
| ISSUED | ✅ Yes | Deducted | Yes | Active issuance |
| RETURNED | ✅ Yes | Restored | N/A | Returned on time |
| OVERDUE | ✅ Yes | Restored | N/A | Returned late |
| REJECTED | ❌ No | No change | No | Request denied |

---

## Testing Checklist

- [ ] Create request → Issuance status PENDING
- [ ] Issuance ID in database with PENDING status
- [ ] Approve request → Issuance status ISSUED
- [ ] Availability deducted on approval
- [ ] Reject request → Issuance status REJECTED
- [ ] NO availability deducted on rejection
- [ ] Return items (on time) → Issuance status RETURNED
- [ ] Return items (late) → Issuance status OVERDUE
- [ ] Availability restored on return
- [ ] Get pending requests only returns PENDING issuances
- [ ] Get issued items only returns ISSUED issuances
- [ ] Activity feed shows ISSUED issuances (not PENDING/REJECTED)

---

## Compilation Status

✅ **No Errors** - All code compiles successfully

---

## Summary of Changes

| Operation | Before | After |
|-----------|--------|-------|
| Trainer creates request | Response with PENDING status | Issuance record created with PENDING status |
| Admin approves | Creates new Issuance | Updates existing Issuance PENDING→ISSUED |
| Admin rejects | Only marks request rejected | Updates Issuance PENDING→REJECTED |
| Available for activity feed | All issuances | Only ISSUED issuances |
| Availability deduction | On approval | On approval |
| Availability restoration | On return | On return |
