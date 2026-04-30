# Reports API Documentation

## Overview
The Reports API provides statistical data and analytical insights for the Tools Management System. It offers various endpoints to retrieve data for creating charts, dashboards, and reports.

## Base URL
```
http://localhost:8080/api/reports
```

## Endpoints

### 1. Tool Statistics
**Endpoint:** `GET /tools/statistics`

**Description:** Get overall statistics about tools including availability, condition, and maintenance needs.

**Response:**
```json
{
  "success": true,
  "data": {
    "totalTools": 150,
    "availableTools": 120,
    "unavailableTools": 30,
    "availabilityPercentage": 80.0,
    "toolsNeedingCalibration": 15,
    "damagedTools": 8
  }
}
```

**Usage:** Create pie charts or bar charts for tool availability visualization.

---

### 2. Issuance Statistics
**Endpoint:** `GET /issuance/statistics`

**Description:** Get comprehensive statistics about tool issuances and their approval status.

**Response:**
```json
{
  "success": true,
  "data": {
    "totalIssuances": 250,
    "issuedTools": 180,
    "returnedTools": 70,
    "pendingReturns": 110,
    "approvedIssuances": 200,
    "pendingApprovals": 35,
    "rejectedIssuances": 15
  }
}
```

**Usage:** Create charts showing issuance status distribution and approval pipeline.

---

### 3. Location Statistics
**Endpoint:** `GET /location/statistics`

**Description:** Get statistics grouped by location to see tool distribution across different plants/locations.

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "location": "Pune",
      "totalTools": 75,
      "availableTools": 60,
      "issuedTools": 15,
      "availabilityPercentage": 80.0
    },
    {
      "location": "Mumbai",
      "totalTools": 50,
      "availableTools": 40,
      "issuedTools": 10,
      "availabilityPercentage": 80.0
    },
    {
      "location": "Bangalore",
      "totalTools": 25,
      "availableTools": 20,
      "issuedTools": 5,
      "availabilityPercentage": 80.0
    }
  ],
  "total": 3
}
```

**Usage:** Create location-wise comparison charts or heat maps.

---

### 4. Dashboard Overview
**Endpoint:** `GET /dashboard/overview`

**Description:** Get a comprehensive overview of key metrics for the main dashboard.

---

### 5. Tool Usage Export
**Endpoint:** `GET /tools/usage/export`

**Description:** Download an Excel report listing tools by usage count, with categories for high use, less use, and no use.

**Query Parameters:**
- `filter` (optional): `high`, `less`, `none`
- `location` (optional): Location name (e.g., Pune, Mumbai, Bangalore)

**Examples:**
- `/tools/usage/export?filter=high`
- `/tools/usage/export?location=Pune`
- `/tools/usage/export?filter=high&location=Pune`

**Response:** Binary `.xlsx` file download with columns: Tool ID, Tool No, Description, Location, Issue Count, Availability, Usage Category

**Response:**
```json
{
  "success": true,
  "data": {
    "totalTools": 150,
    "totalIssuances": 250,
    "totalTrainers": 25,
    "totalAdmins": 8,
    "toolAvailabilityPercentage": 80.0,
    "pendingApprovals": 35,
    "toolsNeedingMaintenance": 23
  }
}
```

**Usage:** Display key metrics cards on the main dashboard.

---

### 5. Top Issued Tools
**Endpoint:** `GET /top-issued-tools`

**Query Parameters:**
- `limit` (optional): Number of top tools to return (default: 10, max: 100)

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "description": "Digital Multimeter",
      "tool_no": "T001",
      "issue_count": 45
    },
    {
      "id": 2,
      "description": "Oscilloscope",
      "tool_no": "T002",
      "issue_count": 38
    },
    {
      "id": 3,
      "description": "Power Supply Unit",
      "tool_no": "T003",
      "issue_count": 32
    }
  ],
  "total": 3
}
```

**Usage:** Create bar charts showing most frequently used tools.

---

### 6. Monthly Issuance Trend
**Endpoint:** `GET /monthly-trend`

**Description:** Get monthly issuance and return trend for the last 12 months.

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "month": "2025-02",
      "issue_count": 15,
      "return_count": 8
    },
    {
      "month": "2025-03",
      "issue_count": 22,
      "return_count": 14
    },
    {
      "month": "2025-04",
      "issue_count": 18,
      "return_count": 12
    }
  ],
  "total": 12
}
```

**Usage:** Create line charts showing issuance trends over time.

---

### 7. Comprehensive Report
**Endpoint:** `GET /comprehensive`

**Description:** Get all statistics in one call for efficient dashboard loading. Combines tool statistics, issuance statistics, dashboard overview, and location statistics.

**Response:**
```json
{
  "success": true,
  "toolStatistics": {
    "totalTools": 150,
    "availableTools": 120,
    "unavailableTools": 30,
    "availabilityPercentage": 80.0,
    "toolsNeedingCalibration": 15,
    "damagedTools": 8
  },
  "issuanceStatistics": {
    "totalIssuances": 250,
    "issuedTools": 180,
    "returnedTools": 70,
    "pendingReturns": 110,
    "approvedIssuances": 200,
    "pendingApprovals": 35,
    "rejectedIssuances": 15
  },
  "dashboardOverview": {
    "totalTools": 150,
    "totalIssuances": 250,
    "totalTrainers": 25,
    "totalAdmins": 8,
    "toolAvailabilityPercentage": 80.0,
    "pendingApprovals": 35,
    "toolsNeedingMaintenance": 23
  },
  "locationStatistics": [
    {
      "location": "Pune",
      "totalTools": 75,
      "availableTools": 60,
      "issuedTools": 15,
      "availabilityPercentage": 80.0
    }
  ]
}
```

**Usage:** Load all analytics data at once for better performance.

---

### 8. Health Check
**Endpoint:** `GET /health`

**Description:** Check if the reports service is running.

**Response:**
```json
{
  "status": "Reports service is running"
}
```

---

## Chart Recommendations

### 1. Tool Availability
- **Chart Type:** Pie Chart or Donut Chart
- **Endpoint:** `/tools/statistics`
- **Data:** availableTools vs unavailableTools
- **Colors:** Green (available), Red (unavailable)

### 2. Tool Conditions
- **Chart Type:** Horizontal Bar Chart
- **Endpoint:** `/tools/statistics`
- **Data:** calibrationRequired, damagedTools, healthyTools
- **Colors:** Blue, Red, Green

### 3. Issuance Status Distribution
- **Chart Type:** Stacked Bar Chart or Pie Chart
- **Endpoint:** `/issuance/statistics`
- **Data:** issued, returned, pending
- **Colors:** Yellow, Green, Orange

### 4. Approval Pipeline
- **Chart Type:** Funnel Chart or Bar Chart
- **Endpoint:** `/issuance/statistics`
- **Data:** pendingApprovals, approvedIssuances, rejectedIssuances
- **Colors:** Orange, Green, Red

### 5. Location Comparison
- **Chart Type:** Grouped Bar Chart
- **Endpoint:** `/location/statistics`
- **Data:** totalTools, availableTools, issuedTools per location
- **Compare:** Multiple locations

### 6. Trend Analysis
- **Chart Type:** Line Chart
- **Endpoint:** `/monthly-trend`
- **Data:** issue_count and return_count over months
- **Legend:** Issues (Blue), Returns (Green)

### 7. Top Tools
- **Chart Type:** Horizontal Bar Chart
- **Endpoint:** `/top-issued-tools`
- **Data:** tool descriptions and issue counts
- **Sort:** Descending by count

### 8. Dashboard Cards
- **Card Type:** KPI Cards
- **Endpoint:** `/dashboard/overview`
- **Metrics:** Total Tools, Total Issuances, Availability %, Pending Approvals

---

## Error Codes

| Status Code | Meaning |
|---|---|
| 200 | Success |
| 400 | Bad Request (invalid parameters) |
| 500 | Internal Server Error |

---

## Query Parameters

### /top-issued-tools
- `limit` - Number of tools to return (default: 10, max: 100)

### Example:
```
GET /api/reports/top-issued-tools?limit=20
```

---

## Performance Notes

1. **Caching:** Consider caching these endpoints as data doesn't change frequently
2. **Comprehensive Endpoint:** Use `/comprehensive` for dashboard loading instead of multiple calls
3. **Pagination:** Not implemented for reports as data volumes are typically manageable
4. **Response Time:** Most queries return within 200-500ms

---

## Data Freshness

- Statistics are calculated in real-time from the database
- For large datasets, consider implementing a caching layer
- Monthly trends are calculated for the last 12 months
- Location statistics include null location values

---

## Expansion Opportunities

These endpoints can be extended to include:
- Trainer-wise issuance statistics
- Tool category-wise statistics
- Return delay analytics
- Approval time metrics
- Tool utilization rates
- Maintenance history trends
