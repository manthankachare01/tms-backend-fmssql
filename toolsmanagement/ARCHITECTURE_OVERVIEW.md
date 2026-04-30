# Chatbot & Reports Architecture Overview

## System Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        CLIENT/FRONTEND                           │
│              (Angular/React/Web Dashboard)                        │
└────────────────────────┬────────────────────────────────────────┘
                         │
         ┌───────────────┼───────────────┐
         │               │               │
         ▼               ▼               ▼
    ┌─────────┐     ┌─────────┐    ┌──────────┐
    │ Chatbot │     │ Reports │    │  Other   │
    │  API    │     │   API   │    │ Modules  │
    │ Layer   │     │ Layer   │    │  (Exist) │
    └────┬────┘     └────┬────┘    └──────────┘
         │               │
         └───────┬───────┘
                 │
         ┌───────▼────────────────────────┐
         │   Spring Boot Application      │
         │   - Controllers                │
         │   - Services                   │
         │   - Repositories               │
         │   - Entities/Models            │
         └───────┬────────────────────────┘
                 │
         ┌───────▼────────────────────────┐
         │     JPA/Hibernate ORM          │
         │   (Database Abstraction)       │
         └───────┬────────────────────────┘
                 │
         ┌───────▼────────────────────────┐
         │    MySQL Database              │
         │  - Tools                       │
         │  - Issuance                    │
         │  - Trainers                    │
         │  - Admins                      │
         │  - Chatbot QA                  │
         │  - Other Tables                │
         └────────────────────────────────┘
```

---

## Chatbot Module Architecture

```
┌──────────────────────────────────────────────────────────┐
│           CHATBOT REQUEST FLOW                            │
└──────────────────────────────────────────────────────────┘

USER QUERY
   │
   ▼
┌─────────────────────────────────────────────┐
│  ChatbotController.ask(ChatbotRequestDTO)   │
└────────────────┬────────────────────────────┘
                 │
                 ▼
┌──────────────────────────────────────────────────┐
│  ChatbotService.processQuery(String)             │
│                                                  │
│  ┌─────────────────────────────────────────┐  │
│  │ 1. Check Exact Match in Q&A             │  │
│  │    ChatbotQARepository.findByExact()    │  │
│  └─────────────────────────────────────────┘  │
│    ↓ (if match) Return Predefined Answer      │
│    ↓ (if no match) Continue                   │
│                                                  │
│  ┌─────────────────────────────────────────┐  │
│  │ 2. Check Partial Match in Q&A           │  │
│  │    ChatbotQARepository.findByKeyword()  │  │
│  └─────────────────────────────────────────┘  │
│    ↓ (if match) Return Predefined Answer      │
│    ↓ (if no match) Continue                   │
│                                                  │
│  ┌─────────────────────────────────────────┐  │
│  │ 3. Dynamic Database Search              │  │
│  │    performDynamicSearch(String)         │  │
│  │                                         │  │
│  │  - Search Tools table                   │  │
│  │  - Search Issuance table                │  │
│  │  - Search Trainer table                 │  │
│  │  - Search Admin table                   │  │
│  └─────────────────────────────────────────┘  │
│    ↓ (if results) Return Dynamic Answer       │
│    ↓ (if no results) Continue                 │
│                                                  │
│  ┌─────────────────────────────────────────┐  │
│  │ 4. Return "No Match Found" Message      │  │
│  └─────────────────────────────────────────┘  │
│                                                  │
└────────────────┬──────────────────────────────┘
                 │
                 ▼
    ┌────────────────────────────┐
    │  ChatbotResponseDTO        │
    │  - query                   │
    │  - answer                  │
    │  - type (predefined/dynamic)│
    │  - responseTime            │
    │  - success                 │
    └────────────────────────────┘
                 │
                 ▼
            CLIENT/UI
```

---

## Reports Module Architecture

```
┌──────────────────────────────────────────────────────────┐
│           REPORTS REQUEST FLOW                            │
└──────────────────────────────────────────────────────────┘

ANALYTICS REQUEST
   │
   ▼
┌─────────────────────────────────────────────┐
│  ReportsController.get[X]Statistics()       │
└────────────────┬────────────────────────────┘
                 │
                 ▼
┌──────────────────────────────────────────────────────┐
│  ReportsService Methods:                             │
│                                                      │
│  ├─ getToolStatistics()                             │
│  │  └─ Query: COUNT(*), COUNT(availability > 0)    │
│  │           COUNT(calibration_required)           │
│  │                                                  │
│  ├─ getIssuanceStatistics()                         │
│  │  └─ Query: COUNT(*) GROUP BY status             │
│  │           approval_status                       │
│  │                                                  │
│  ├─ getLocationStatistics()                         │
│  │  └─ Query: COUNT(*) GROUP BY location           │
│  │                                                  │
│  ├─ getDashboardOverview()                          │
│  │  └─ Query: Multiple COUNT queries                │
│  │                                                  │
│  ├─ getTopIssuedTools(int limit)                    │
│  │  └─ Query: COUNT(*) GROUP BY tool_id            │
│  │           ORDER BY count DESC                   │
│  │                                                  │
│  └─ getMonthlyIssuanceTrend()                       │
│     └─ Query: COUNT(*) GROUP BY DATE_FORMAT        │
│              for last 12 months                     │
│                                                      │
└────────────────┬──────────────────────────────────────┘
                 │
                 ▼
    ┌────────────────────────────────┐
    │  Statistics DTOs               │
    │  - ToolStatisticsDTO           │
    │  - IssuanceStatisticsDTO       │
    │  - LocationStatisticsDTO       │
    │  - DashboardOverviewDTO        │
    │  - Map<String, Object>         │
    └────────────────────────────────┘
                 │
                 ▼
            CLIENT/UI (Charts)
```

---

## Database Schema Relationships

```
┌─────────────────┐
│    TOOLS        │
├─────────────────┤
│ id (PK)         │
│ description     │
│ tool_no         │
│ siNo            │
│ location        │
│ availability    │◄─────┐
│ condition       │      │
│ calibration_req │      │
│ quantity        │      │
│ created_at      │      │
└─────────────────┘      │
                         │
                         │ 1:N
                         │
┌─────────────────────┐  │
│    ISSUANCE         │──┘
├─────────────────────┤
│ id (PK)             │
│ tool_id (FK)        │
│ issued_to           │
│ issued_by           │
│ issued_date         │
│ return_date         │
│ status              │
│ approval_status     │
│ created_at          │
└─────────────────────┘


┌──────────────────┐
│  CHATBOT_QA      │
├──────────────────┤
│ id (PK)          │
│ question         │
│ answer           │
│ is_active        │
│ created_at       │
│ updated_at       │
└──────────────────┘


┌──────────────────┐
│    TRAINER       │
├──────────────────┤
│ id (PK)          │
│ name             │
│ email            │
│ specialization   │
│ created_at       │
└──────────────────┘


┌──────────────────┐
│     ADMIN        │
├──────────────────┤
│ id (PK)          │
│ name             │
│ email            │
│ location         │
│ created_at       │
└──────────────────┘
```

---

## Chatbot Module - File Structure

```
chatbot/
├── controller/
│   └── ChatbotController.java
│       ├── POST /ask - Process user query
│       ├── GET /qa/all - Get all Q&As
│       ├── POST /qa/add - Add new Q&A
│       ├── PUT /qa/update/{id} - Update Q&A
│       ├── DELETE /qa/delete/{id} - Delete Q&A
│       └── GET /health - Health check
│
├── service/
│   └── ChatbotService.java
│       ├── processQuery(String) - Main query processor
│       ├── performDynamicSearch(String) - Database search
│       ├── getAllQAs()
│       ├── addQA(ChatbotQADTO)
│       ├── updateQA(Long, ChatbotQADTO)
│       ├── deleteQA(Long)
│       └── convertToDTO()
│
├── repository/
│   └── ChatbotQARepository.java
│       ├── findByQuestionExact(String)
│       ├── findByQuestionKeyword(String)
│       ├── findAllActiveQAs()
│       └── findByIsActive(Boolean)
│
├── model/
│   └── ChatbotQA.java
│       ├── id, question, answer
│       ├── isActive, createdAt, updatedAt
│       └── @PrePersist, @PreUpdate
│
└── dto/
    ├── ChatbotRequestDTO.java (query)
    ├── ChatbotResponseDTO.java (answer, type, time)
    └── ChatbotQADTO.java (Q&A data)
```

---

## Reports Module - File Structure

```
reports/
├── controller/
│   └── ReportsController.java
│       ├── GET /tools/statistics
│       ├── GET /issuance/statistics
│       ├── GET /location/statistics
│       ├── GET /dashboard/overview
│       ├── GET /top-issued-tools
│       ├── GET /monthly-trend
│       ├── GET /comprehensive (all in one)
│       └── GET /health
│
├── service/
│   └── ReportsService.java
│       ├── getToolStatistics()
│       ├── getIssuanceStatistics()
│       ├── getLocationStatistics()
│       ├── getDashboardOverview()
│       ├── getTopIssuedTools(int)
│       └── getMonthlyIssuanceTrend()
│
└── dto/
    ├── ToolStatisticsDTO.java
    │   ├── totalTools, availableTools
    │   ├── availabilityPercentage
    │   ├── toolsNeedingCalibration, damagedTools
    │
    ├── IssuanceStatisticsDTO.java
    │   ├── totalIssuances, issuedTools, returnedTools
    │   ├── approvedIssuances, pendingApprovals
    │   └── rejectedIssuances
    │
    ├── LocationStatisticsDTO.java
    │   ├── location, totalTools, availableTools
    │   └── availabilityPercentage
    │
    └── DashboardOverviewDTO.java
        ├── totalTools, totalIssuances
        ├── totalTrainers, totalAdmins
        ├── toolAvailabilityPercentage
        └── pendingApprovals, toolsNeedingMaintenance
```

---

## API Response Flow Diagram

```
Request
   │
   ├─► @RestController
   │   (HTTP mapping)
   │
   ├─► @Service
   │   (Business logic)
   │   ├─► JdbcTemplate
   │   │   (SQL execution)
   │   │
   │   └─► Database
   │       (Data retrieval)
   │
   ├─► @Service
   │   (Data transformation)
   │
   ├─► DTO
   │   (Data packaging)
   │
   ├─► ResponseEntity
   │   (HTTP response)
   │
   └─► Client
       (JSON response)
```

---

## Data Flow Example: Chatbot Query

```
User Input: "What tools are available?"
   │
   ▼
ChatbotController.ask()
   │
   ▼
ChatbotService.processQuery()
   │
   ├─ Query: findByQuestionExact("What tools are available?")
   │  Result: NOT FOUND
   │  
   ├─ Query: findByQuestionKeyword("What tools are available?")
   │  Result: NOT FOUND
   │
   ├─ Dynamic Search: performDynamicSearch()
   │  │
   │  ├─ SQL: SELECT * FROM tools WHERE description LIKE '%available%'
   │  │  Result: Found 3 tools
   │  │
   │  └─ Format: "Tools found: Tool A, Tool B, Tool C"
   │
   ▼
ChatbotResponseDTO
{
  "query": "What tools are available?",
  "answer": "Tools found: Tool A...",
  "type": "dynamic",
  "responseTime": 150,
  "success": true
}
   │
   ▼
JSON Response to Client
```

---

## Data Flow Example: Reports Query

```
Client Request: GET /api/reports/tools/statistics
   │
   ▼
ReportsController.getToolStatistics()
   │
   ▼
ReportsService.getToolStatistics()
   │
   ├─ Query 1: SELECT COUNT(*) FROM tools
   │  Result: 150
   │
   ├─ Query 2: SELECT COUNT(*) FROM tools WHERE availability > 0
   │  Result: 120
   │
   ├─ Query 3: SELECT COUNT(*) FROM tools WHERE calibration_required = true
   │  Result: 15
   │
   ├─ Query 4: SELECT COUNT(*) FROM tools WHERE condition = 'Damaged'
   │  Result: 8
   │
   └─ Calculate: Availability % = (120 / 150) * 100 = 80%
   │
   ▼
ToolStatisticsDTO
{
  "totalTools": 150,
  "availableTools": 120,
  "unavailableTools": 30,
  "availabilityPercentage": 80.0,
  "toolsNeedingCalibration": 15,
  "damagedTools": 8
}
   │
   ▼
JSON Response to Client
```

---

## Integration Points

```
┌─────────────────────────────────────────────────────┐
│  Other Existing Modules                             │
├─────────────────────────────────────────────────────┤
│ - Tools Module                                      │
│ - Issuance Module                                   │
│ - Trainer Module                                    │
│ - Admin Module                                      │
│ - Security Module                                   │
│ - Notification Module                              │
│ - WebSocket Module                                 │
└────────────────┬──────────────────────────────────┘
                 │
                 │ Shares Database & Entities
                 │
       ┌─────────┴─────────┐
       │                   │
       ▼                   ▼
    CHATBOT            REPORTS
    (New)              (New)
       │                   │
       └─────────┬─────────┘
                 │
                 ▼
         Unified Data Access
         via JPA Repository
```

---

## Performance Considerations

```
Chatbot Performance:
├─ Query 1 (Exact Match): O(1) with indexed search
├─ Query 2 (Partial Match): O(n) on active Q&As
├─ Query 3 (Dynamic Search): O(n) on relevant tables
└─ Average Response Time: 100-300ms

Reports Performance:
├─ Single Table Query: 50-100ms
├─ Complex Query with JOINs: 100-200ms
├─ Comprehensive (all): 300-500ms
└─ Indexed queries prevent full table scans
```

---

**This architecture ensures scalability, maintainability, and clear separation of concerns.**
