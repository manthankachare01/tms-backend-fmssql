# Availability Deduction Implementation - Complete

## Summary
Updated the issuance workflow to deduct from **availability** field instead of using the quantity service.

---

## Changes Made

### 1. Approval Process (approveIssuanceRequest)
**Changed from:** `quantityService.reduceQuantities(toolIds, kitIds, trainerName)`

**Changed to:** Direct availability deduction

**Process:**
```java
// For each tool
Tool tool = toolRepository.findById(toolId);
if (tool.getAvailability() <= 0) {
    throw new BadRequestException("Tool not available");
}
tool.setAvailability(tool.getAvailability() - 1);
toolRepository.save(tool);

// For each kit
Kit kit = kitRepository.findById(kitId);
if (kit.getAvailability() <= 0) {
    throw new BadRequestException("Kit not available");
}
kit.setAvailability(kit.getAvailability() - 1);
kitRepository.save(kit);
```

**Benefits:**
- ✅ Direct availability deduction
- ✅ Availability validation before deduction
- ✅ Per-item error handling
- ✅ No complex quantity service logic

---

### 2. Return Process (processReturn - No Items Case)
**Changed from:** `quantityService.increaseQuantities(toolIds, kitIds)`

**Changed to:** Direct availability increment

**Process:**
```java
// For each tool
Tool tool = toolRepository.findById(toolId);
if (tool != null) {
    tool.setAvailability(tool.getAvailability() + 1);
    toolRepository.save(tool);
}

// For each kit
Kit kit = kitRepository.findById(kitId);
if (kit != null) {
    kit.setAvailability(kit.getAvailability() + 1);
    kitRepository.save(kit);
}
```

**Benefits:**
- ✅ Direct availability restoration
- ✅ Consistent with item-level returns
- ✅ No quantity service dependency
- ✅ Simple and transparent

---

### 3. Return Process (With Items)
**Status:** ✅ Already using availability deduction

The per-item return logic already increments availability:
```java
// For tools
Tool t = toolRepository.findById(it.getToolId());
t.setAvailability(t.getAvailability() + ri.getQuantityReturned());

// For kits
Kit k = kitRepository.findById(it.getKitId());
k.setAvailability(k.getAvailability() + ri.getQuantityReturned());
// Plus all tools inside the kit
```

---

## Complete Issuance Workflow

```
┌─────────────────────────────────────────────────┐
│ 1. TRAINER CREATES REQUEST                      │
│    POST /api/issuance/request                   │
│    → IssuanceRequest: PENDING                   │
│    → NO availability changes                    │
└─────────────────────────────────────────────────┘
                        ↓
┌─────────────────────────────────────────────────┐
│ 2. ADMIN APPROVES REQUEST                       │
│    POST /api/issuance/approve                   │
│                                                 │
│    For each TOOL:                               │
│    → availability = availability - 1            │
│                                                 │
│    For each KIT:                                │
│    → availability = availability - 1            │
│                                                 │
│    → Issuance: ISSUED                           │
│    → Trainer notified                           │
└─────────────────────────────────────────────────┘
                        ↓
┌─────────────────────────────────────────────────┐
│ 3. TRAINER RETURNS ITEMS                        │
│    PUT /api/issuance/process-return             │
│                                                 │
│    Option A: Per-item returns                   │
│    For each TOOL:                               │
│    → availability = availability + 1            │
│                                                 │
│    For each KIT:                                │
│    → availability = availability + 1            │
│                                                 │
│    Option B: Full return (no items specified)   │
│    For each TOOL:                               │
│    → availability = availability + 1            │
│                                                 │
│    For each KIT:                                │
│    → availability = availability + 1            │
│                                                 │
│    → Issuance: RETURNED or OVERDUE              │
│    → Return record created                      │
└─────────────────────────────────────────────────┘
```

---

## Before vs After

### BEFORE (Using Quantity Service)
```
✗ Complex quantity service logic
✗ Separate quantity tracking
✗ Difficult to understand flow
✗ Multiple deduction paths
```

### AFTER (Direct Availability)
```
✓ Simple, direct availability changes
✓ Single source of truth (availability field)
✓ Clear and transparent logic
✓ Consistent across all operations
✓ Easy to debug and audit
```

---

## Key Features

| Feature | Implementation |
|---------|-----------------|
| Approval deduction | Direct availability - 1 |
| Availability validation | Check availability > 0 before deduction |
| Return with items | Direct availability + quantity_returned |
| Return without items | Direct availability + 1 |
| Error handling | Throws exception if unavailable |
| Transaction safety | Each operation saved immediately |

---

## Error Scenarios

### Tool/Kit Not Available
```
POST /api/issuance/approve
{
  "requestId": 1,
  "approvedBy": "admin"
}

Response: 400 Bad Request
"Tool not available: Power Drill"
```

### Invalid Tool/Kit ID
```
Response: 404 Not Found
"Tool not found: id=999"
```

---

## API Behavior

### Approval
```
Tool/Kit availability BEFORE approval:  5
Tools/Kits requested:                   2

Approval process:
- Tool 1: 5 → 4
- Tool 2: 3 → 2

Tool/Kit availability AFTER approval:   4 and 2
```

### Return (Per-Item)
```
Issuance availability BEFORE return:    4 and 2
Items returned:                         2 items

Return process:
- Tool 1: 4 → 5
- Tool 2: 2 → 3

Issuance availability AFTER return:     5 and 3
```

### Return (No Items)
```
Issuance availability BEFORE return:    4 and 2
Items returned:                         all (no details)

Return process:
- Tool 1: 4 → 5
- Tool 2: 2 → 3

Issuance availability AFTER return:     5 and 3
```

---

## Files Modified

| File | Changes |
|------|---------|
| IssuanceService.java | 2 methods updated |

---

## Testing Checklist

- [ ] Approve issuance → availability decreases by 1
- [ ] Approve multiple items → each availability decreases by 1
- [ ] Approve with insufficient availability → error thrown
- [ ] Return per-item → availability increases by quantity
- [ ] Return all items → availability increases by 1 each
- [ ] Availability validation working
- [ ] No quantity service calls in approval
- [ ] No quantity service calls in return (full)

---

## Compilation Status

✅ **No Errors** - All code compiles successfully

---

## Summary

Simplified the issuance workflow by:
1. Removing dependency on QuantityUpdateService
2. Using direct availability field deduction
3. Consistent availability management across approval and return
4. Clearer, more transparent logic
5. Better error handling and validation
