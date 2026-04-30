# Chatbot API Documentation

## Overview
The Chatbot API provides intelligent question-answering capabilities for the Tools Management System. It supports:
1. **Dynamic Database Queries** - Automatically searches all database tables based on user questions
2. **Predefined Q&A** - Returns answers to pre-configured questions
3. **Q&A Management** - Add, update, and manage predefined question-answer pairs

## Base URL
```
http://localhost:8080/api/chatbot
```

## Endpoints

### 1. Ask Chatbot (Process Query)
**Endpoint:** `POST /ask`

**Description:** Submit a question and receive an intelligent answer from the chatbot.

**Request Body:**
```json
{
  "query": "What is the availability of tools?"
}
```

**Response (Success):**
```json
{
  "query": "What is the availability of tools?",
  "answer": "Tools found: Tool Alpha (Tool No: T001, Availability: 5), Tool Beta (Tool No: T002, Availability: 3)",
  "type": "dynamic",
  "responseTime": 150,
  "success": true
}
```

**Response (Predefined Match):**
```json
{
  "query": "What is system name?",
  "answer": "Tools Management System",
  "type": "predefined",
  "responseTime": 45,
  "success": true
}
```

**Response (No Match):**
```json
{
  "query": "Unknown question",
  "success": false,
  "message": "I couldn't find relevant information for your query. Please try rephrasing your question or check the available data in the system."
}
```

**Query Examples:**
- "What tools are available?"
- "Tell me about tool T001"
- "Which trainers are available?"
- "What is system name?"
- "Show me issuance details"

---

### 2. Get All Q&A
**Endpoint:** `GET /qa/all`

**Description:** Retrieve all predefined question-answer pairs.

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "question": "What is system name?",
      "answer": "Tools Management System",
      "isActive": true,
      "createdAt": "2026-01-26T10:30:00",
      "updatedAt": "2026-01-26T10:30:00"
    }
  ],
  "total": 1
}
```

---

### 3. Add New Q&A
**Endpoint:** `POST /qa/add`

**Description:** Add a new predefined question-answer pair.

**Request Body:**
```json
{
  "question": "How do I issue a tool?",
  "answer": "To issue a tool: 1. Select the tool, 2. Choose trainer/employee, 3. Set return date, 4. Submit for approval, 5. Wait for admin approval"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Q&A added successfully",
  "data": {
    "id": 2,
    "question": "How do I issue a tool?",
    "answer": "To issue a tool: 1. Select the tool, 2. Choose trainer/employee, 3. Set return date, 4. Submit for approval, 5. Wait for admin approval",
    "isActive": true,
    "createdAt": "2026-01-26T10:30:00",
    "updatedAt": "2026-01-26T10:30:00"
  }
}
```

---

### 4. Update Q&A
**Endpoint:** `PUT /qa/update/{id}`

**Description:** Update an existing question-answer pair.

**Path Parameters:**
- `id`: ID of the Q&A to update

**Request Body:**
```json
{
  "question": "How do I issue a tool?",
  "answer": "Updated answer..."
}
```

**Response:**
```json
{
  "success": true,
  "message": "Q&A updated successfully",
  "data": {
    "id": 2,
    "question": "How do I issue a tool?",
    "answer": "Updated answer...",
    "isActive": true,
    "createdAt": "2026-01-26T10:30:00",
    "updatedAt": "2026-01-26T10:45:00"
  }
}
```

---

### 5. Delete Q&A
**Endpoint:** `DELETE /qa/delete/{id}`

**Description:** Delete a question-answer pair (soft delete - marks as inactive).

**Path Parameters:**
- `id`: ID of the Q&A to delete

**Response:**
```json
{
  "success": true,
  "message": "Q&A deleted successfully"
}
```

---

### 6. Health Check
**Endpoint:** `GET /health`

**Description:** Check if the chatbot service is running.

**Response:**
```json
{
  "status": "Chatbot service is running"
}
```

---

## How the Chatbot Works

### Query Processing Flow:
1. **Exact Match Check** - Searches for exact question match in predefined Q&A
2. **Partial Match Check** - Searches for partial keyword matches in predefined Q&A
3. **Dynamic Search** - If no predefined match, searches across database tables:
   - Tools table (description, tool_no, siNo)
   - Issuance table (issued_to, status)
   - Trainer table (name, specialization)
   - Admin table (name, location)
4. **Response** - Returns answer with metadata (type, response time, success status)

### Response Types:
- **predefined** - Answer from predefined Q&A database
- **dynamic** - Answer generated from dynamic database search
- **error** - No matching answer found with explanation

---

## Sample Queries to Try

1. **System Information:**
   - "What is system name?"
   - "Tell me about the system"

2. **Tool Related:**
   - "What tools are available?"
   - "Show me tool information"
   - "What is the status of T001?"

3. **Issuance Related:**
   - "Tell me about issuance details"
   - "Show issued tools"
   - "What is the status of issuances?"

4. **Trainer Related:**
   - "Who are the trainers?"
   - "Show trainer details"
   - "What specializations are available?"

---

## Error Codes

| Status Code | Meaning |
|---|---|
| 200 | Success |
| 201 | Created (for POST requests) |
| 400 | Bad Request (empty query, missing fields) |
| 404 | Not Found (Q&A with given ID) |
| 500 | Internal Server Error |

---

## Notes

- The chatbot is case-insensitive for question matching
- Dynamic searches are limited to 5 results per table to avoid large responses
- All timestamps are in ISO 8601 format
- Deleted Q&A are marked as inactive (soft delete), not permanently removed
- The chatbot can be extended by adding more predefined Q&As
- New database tables can be automatically searched by the dynamic search feature
