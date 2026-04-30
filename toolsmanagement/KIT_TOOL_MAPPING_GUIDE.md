# Kit Tool Mapping Guide - SI_NO Based Approach

## Overview
You can now add **specific tools to a kit** using their **SI_NO (Serial Number)** and **tool location** instead of just tool numbers. This ensures only exact tools you want are added, not all tools with the same tool number.

## Changes Made

### 1. **KitCreateRequest DTO** (Updated)
- Added new field: `toolItems` - List of tools specified by SI_NO and location
- Kept old field: `toolNos` - Deprecated, but still supported for backward compatibility

### 2. **ToolRepository** (Enhanced)
- Added new method: `findBySiNoAndToolLocationIgnoreCaseAndTrim(siNo, toolLocation)`
- Finds a specific tool by its SI_NO and tool location

### 3. **KitService** (Enhanced)
- `createKit()` method now supports:
  - New way: Using `toolItems` (SI_NO + location based)
  - Old way: Using `toolNos` (backward compatible)
- `updateKit()` method now supports both approaches
- Both methods validate that specified tools exist; if not, returns meaningful error

---

## Usage Examples

### Method 1: Add Tools by SI_NO (Recommended) ✅

**Request:**
```json
POST http://localhost:8080/api/kits/create?createdBy=AdminName

{
  "kitName": "Electrical Kit",
  "qualificationLevel": "Level 2",
  "trainingName": "Electrical Training",
  "location": "Pune",
  "toolItems": [
    {
      "siNo": "TOOL-001",
      "toolLocation": "Rack-A1"
    },
    {
      "siNo": "TOOL-002",
      "toolLocation": "Rack-B2"
    },
    {
      "siNo": "TOOL-003",
      "toolLocation": "Shelf-C1"
    }
  ],
  "aggregates": [],
  "remark": "Standard tools for electrical training",
  "condition": "Good"
}
```

**Benefits:**
- Adds **exact tools** you specify
- No duplicate tools if multiple tools have same tool number
- Includes physical location of each tool in the request
- Precise inventory management

---

### Method 2: Add Tools by Tool No (Legacy - Deprecated)

**Request:**
```json
POST http://localhost:8080/api/kits/create?createdBy=AdminName

{
  "kitName": "Mechanical Kit",
  "qualificationLevel": "Level 1",
  "trainingName": "Mechanical Training",
  "location": "Pune",
  "toolNos": ["TOOL-NO-01", "TOOL-NO-02", "TOOL-NO-03"],
  "aggregates": [],
  "remark": "Mechanical tools",
  "condition": "Good"
}
```

**Note:** This will add **ALL tools** with these tool numbers (if multiple exist)

---

## Update Kit Example

### Update Kit with New SI_NO Based Tools

**Request:**
```json
PUT http://localhost:8080/api/kits/update/1

{
  "kitName": "Advanced Electrical Kit",
  "qualificationLevel": "Level 3",
  "trainingName": "Advanced Electrical Training",
  "location": "Pune",
  "toolItems": [
    {
      "siNo": "TOOL-001",
      "toolLocation": "Rack-A1"
    },
    {
      "siNo": "TOOL-004",
      "toolLocation": "Rack-D3"
    }
  ],
  "aggregates": [],
  "remark": "Updated tools",
  "condition": "Good"
}
```

---

## API Contract

### KitCreateRequest Structure

```java
{
  "kitName": String,
  "qualificationLevel": String,
  "trainingName": String,
  "location": String,
  
  // NEW: Recommended approach
  "toolItems": [
    {
      "siNo": String (required),
      "toolLocation": String (required - physical location like "Rack-A1")
    }
  ],
  
  // OLD: Deprecated but still supported
  "toolNos": List<String>,
  
  "aggregates": List<KitAggregateRequest>,
  "remark": String,
  "condition": String
}
```

---

## Error Handling

If a specified tool is not found, the API returns:

```json
{
  "error": "Tool not found with SI_NO: TOOL-999 at location: Rack-Z9"
}
```

**Reasons for tool not found:**
1. SI_NO doesn't exist in the system
2. Tool location spelling is incorrect (case-insensitive matching is applied)
3. Tool was deleted from the system

---

## Best Practices

1. **Always use SI_NO approach** for new integrations
2. **Verify SI_NO and toolLocation** before sending request
3. **Include location in toolItems** - it's the physical storage location
4. **Use exact SI_NO format** (case-insensitive matching is applied)
5. **Don't mix approaches** - use either toolItems OR toolNos, not both

---

## Database Mapping

### Tool Entity Fields Used:
- `si_no` - Serial number of the tool
- `tool_location` - Physical location in warehouse (e.g., "Rack-A1", "Shelf-C1")
- `tool_no` - Tool number (different from SI_NO)
- `location` - Plant/facility location (e.g., "Pune", "Mumbai")

**Note:** 
- `tool_location` = Physical storage location within the facility
- `location` = Facility/plant name (set at kit level)

---

## Migration from Old to New Approach

If you're currently using `toolNos`:

**Old Request:**
```json
{
  "toolNos": ["HAMMER-001", "HAMMER-001", "SCREWDRIVER-001"]
}
```

**New Request (Recommended):**
```json
{
  "toolItems": [
    {"siNo": "TOOL-HAMMER-A1", "toolLocation": "Rack-A1"},
    {"siNo": "TOOL-HAMMER-A2", "toolLocation": "Rack-A2"},
    {"siNo": "TOOL-SCREW-B1", "toolLocation": "Rack-B1"}
  ]
}
```

---

## Summary

| Aspect | Old Approach (toolNos) | New Approach (toolItems) |
|--------|------------------------|--------------------------|
| Parameter | Tool Number | SI_NO + Tool Location |
| Specificity | Adds all tools with same number | Adds exact specified tools |
| Duplicates | Possible if multiple tools have same number | Not possible |
| Location Info | Not included | Included in request |
| Recommended | ❌ No | ✅ Yes |

