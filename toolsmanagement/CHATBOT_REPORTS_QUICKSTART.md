# Chatbot & Reports API - Quick Start Guide

## Overview
This guide helps you quickly get started with the new Chatbot and Reports APIs for the Tools Management System.

## What's New?

### Chatbot API
- Ask dynamic questions about tools, issuances, trainers, and admins
- Get answers from predefined Q&A database
- Automatically search database for relevant information
- Manage predefined questions and answers

### Reports API
- Get statistical data for dashboard visualization
- Create charts and analytics
- Monitor tool availability and issuance trends
- View location-wise and monthly trends

## Quick Start

### 1. Start Your Application
```bash
# Navigate to project directory
cd toolsmanagement

# Run the application
./mvnw spring-boot:run

# Or use Maven directly
mvn spring-boot:run
```

The application will start at `http://localhost:8080`

### 2. Test Chatbot API

#### Ask a Question
```bash
curl -X POST http://localhost:8080/api/chatbot/ask \
  -H "Content-Type: application/json" \
  -d '{"query":"What is system name?"}'
```

**Expected Response:**
```json
{
  "query": "What is system name?",
  "answer": "Tools Management System",
  "type": "predefined",
  "responseTime": 45,
  "success": true
}
```

#### Get All Q&A
```bash
curl http://localhost:8080/api/chatbot/qa/all
```

#### Add New Q&A
```bash
curl -X POST http://localhost:8080/api/chatbot/qa/add \
  -H "Content-Type: application/json" \
  -d '{
    "question": "How do I issue a tool?",
    "answer": "To issue a tool: 1. Select tool, 2. Choose trainee, 3. Set return date, 4. Submit for approval"
  }'
```

### 3. Test Reports API

#### Get Tool Statistics
```bash
curl http://localhost:8080/api/reports/tools/statistics
```

**Expected Response:**
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

#### Get Issuance Statistics
```bash
curl http://localhost:8080/api/reports/issuance/statistics
```

#### Get Dashboard Overview
```bash
curl http://localhost:8080/api/reports/dashboard/overview
```

#### Get Location Statistics
```bash
curl http://localhost:8080/api/reports/location/statistics
```

#### Get All Analytics at Once
```bash
curl http://localhost:8080/api/reports/comprehensive
```

## Project Structure

```
toolsmanagement/
├── src/main/java/com/tms/restapi/toolsmanagement/
│   ├── chatbot/
│   │   ├── controller/
│   │   │   └── ChatbotController.java
│   │   ├── service/
│   │   │   └── ChatbotService.java
│   │   ├── repository/
│   │   │   └── ChatbotQARepository.java
│   │   ├── model/
│   │   │   └── ChatbotQA.java
│   │   └── dto/
│   │       ├── ChatbotRequestDTO.java
│   │       ├── ChatbotResponseDTO.java
│   │       └── ChatbotQADTO.java
│   ├── reports/
│   │   ├── controller/
│   │   │   └── ReportsController.java
│   │   ├── service/
│   │   │   └── ReportsService.java
│   │   └── dto/
│   │       ├── ToolStatisticsDTO.java
│   │       ├── IssuanceStatisticsDTO.java
│   │       ├── LocationStatisticsDTO.java
│   │       └── DashboardOverviewDTO.java
│   └── [other modules...]
├── src/main/resources/
│   ├── db/migration/
│   │   └── V4__create_chatbot_qa_table.sql
│   └── application.properties
└── [documentation...]
```

## Database Setup

The database migration automatically creates the `chatbot_qa` table with the following structure:

```sql
CREATE TABLE chatbot_qa (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    question VARCHAR(500) NOT NULL,
    answer LONGTEXT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_question (question),
    INDEX idx_is_active (is_active)
);
```

A sample Q&A is automatically inserted:
- **Question:** "What is system name?"
- **Answer:** "Tools Management System"

## Common Chatbot Queries to Try

1. System Information:
   - "What is system name?"
   - "Tell me about the system"

2. Tool Related:
   - "What tools are available?"
   - "Show me tool information"
   - "Tell me about T001"

3. Issuance Related:
   - "Show issuance details"
   - "Tell me about issuances"

4. Trainer Related:
   - "Who are the trainers?"
   - "Show trainer information"

## Common Reports to Generate

1. **Tool Availability Dashboard:**
   ```bash
   curl http://localhost:8080/api/reports/tools/statistics
   ```

2. **Issuance Pipeline:**
   ```bash
   curl http://localhost:8080/api/reports/issuance/statistics
   ```

3. **Location-wise Analysis:**
   ```bash
   curl http://localhost:8080/api/reports/location/statistics
   ```

4. **Monthly Trends:**
   ```bash
   curl http://localhost:8080/api/reports/monthly-trend
   ```

5. **Top Used Tools:**
   ```bash
   curl http://localhost:8080/api/reports/top-issued-tools?limit=10
   ```

## Key Features

### Chatbot
✅ Intelligent query processing
✅ Dynamic database search
✅ Predefined Q&A management
✅ Case-insensitive matching
✅ Response time tracking
✅ Soft delete for Q&A

### Reports
✅ Real-time statistics
✅ Multiple analysis views
✅ Location-wise grouping
✅ Monthly trends
✅ Top tools ranking
✅ Comprehensive overview
✅ Dashboard optimization

## Next Steps

1. **Add More Q&A:**
   - Use the `/qa/add` endpoint to add domain-specific Q&As
   - Update Q&As as needed using `/qa/update/{id}`

2. **Customize Reports:**
   - Extend ReportsService to add new report types
   - Implement caching for better performance

3. **Frontend Integration:**
   - Connect the APIs to your Angular/React dashboard
   - Create charts using Chart.js, D3.js, or similar libraries
   - Implement chatbot UI widget

4. **Extend Chatbot:**
   - Add more predefined Q&As
   - Implement sentiment analysis
   - Add conversation history tracking
   - Implement feedback mechanism

## Troubleshooting

### Chatbot Returns "No Match Found"
- Check if the database has the required tables
- Verify the query syntax
- Try more specific keywords

### Reports Showing Zero Values
- Ensure database has data in required tables
- Check table names match the SQL queries
- Verify user has appropriate database permissions

### Application Won't Start
- Check if port 8080 is available
- Verify database connection properties
- Check for any SQL migration issues

## Documentation

- See [CHATBOT_API.md](./CHATBOT_API.md) for detailed API documentation
- See [REPORTS_API.md](./REPORTS_API.md) for reports endpoints

## Support

For issues or questions:
1. Check the detailed API documentation
2. Review error messages in application logs
3. Verify database connectivity
4. Check if all required tables exist

---

**Happy coding! 🚀**
