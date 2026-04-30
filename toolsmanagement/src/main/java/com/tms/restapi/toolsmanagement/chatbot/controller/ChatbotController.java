package com.tms.restapi.toolsmanagement.chatbot.controller;

import com.tms.restapi.toolsmanagement.chatbot.dto.ChatbotQADTO;
import com.tms.restapi.toolsmanagement.chatbot.dto.ChatbotRequestDTO;
import com.tms.restapi.toolsmanagement.chatbot.dto.ChatbotResponseDTO;
import com.tms.restapi.toolsmanagement.chatbot.service.ChatbotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for chatbot operations.
 *
 * Provides endpoints for querying the chatbot, managing predefined Q&A,
 * and checking service health.
 *
 * Base path: /api/chatbot
 */
@RestController
@RequestMapping("/api/chatbot")
@CrossOrigin(origins = "*")
public class ChatbotController {

    @Autowired
    private ChatbotService chatbotService;

    /**
     * Process user query and get answer from chatbot.
     * Supports both predefined Q&A and dynamic database query responses.
     *
     * API Endpoint: POST /api/chatbot/ask
     * Request Payload Sample:
     * {
     *   "query": "Is hammer available at Pune?"
     * }
     */
    @PostMapping("/ask")
    public ResponseEntity<?> askChatbot(@RequestBody ChatbotRequestDTO request) {
        try {
            if (request.getQuery() == null || request.getQuery().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(new HashMap<String, String>() {{
                    put("error", "Query cannot be empty");
                }});
            }

            ChatbotResponseDTO response = chatbotService.processQuery(request.getQuery());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new HashMap<String, String>() {{
                put("error", "An error occurred while processing your query: " + e.getMessage());
            }});
        }
    }

    /**
     * Get all predefined Q&A entries stored in the chatbot knowledge base.
     *
     * API Endpoint: GET /api/chatbot/qa/all
     * Response payload contains success flag, data list, and total count.
     */
    @GetMapping("/qa/all")
    public ResponseEntity<?> getAllQAs() {
        try {
            List<ChatbotQADTO> qaList = chatbotService.getAllQAs();
            return ResponseEntity.ok(new HashMap<String, Object>() {{
                put("success", true);
                put("data", qaList);
                put("total", qaList.size());
            }});

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new HashMap<String, String>() {{
                put("error", "Failed to retrieve Q&A: " + e.getMessage());
            }});
        }
    }

    /**
     * Add new predefined Q&A entry.
     *
     * API Endpoint: POST /api/chatbot/qa/add
     * Request Payload Sample:
     * {
     *   "question": "What is the process to return a tool?",
     *   "answer": "Please fill the return form and submit it to your admin."
     * }
     */
    @PostMapping("/qa/add")
    public ResponseEntity<?> addQA(@RequestBody ChatbotQADTO qaDTO) {
        try {
            if (qaDTO.getQuestion() == null || qaDTO.getQuestion().trim().isEmpty() ||
                qaDTO.getAnswer() == null || qaDTO.getAnswer().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(new HashMap<String, String>() {{
                    put("error", "Question and answer cannot be empty");
                }});
            }

            ChatbotQADTO saved = chatbotService.addQA(qaDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(new HashMap<String, Object>() {{
                put("success", true);
                put("message", "Q&A added successfully");
                put("data", saved);
            }});

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new HashMap<String, String>() {{
                put("error", "Failed to add Q&A: " + e.getMessage());
            }});
        }
    }

    /**
     * Update existing Q&A
     */
    @PutMapping("/qa/update/{id}")
    public ResponseEntity<?> updateQA(@PathVariable Long id, @RequestBody ChatbotQADTO qaDTO) {
        try {
            if (qaDTO.getQuestion() == null || qaDTO.getQuestion().trim().isEmpty() ||
                qaDTO.getAnswer() == null || qaDTO.getAnswer().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(new HashMap<String, String>() {{
                    put("error", "Question and answer cannot be empty");
                }});
            }

            ChatbotQADTO updated = chatbotService.updateQA(id, qaDTO);
            
            if (updated == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new HashMap<String, String>() {{
                    put("error", "Q&A with id " + id + " not found");
                }});
            }

            return ResponseEntity.ok(new HashMap<String, Object>() {{
                put("success", true);
                put("message", "Q&A updated successfully");
                put("data", updated);
            }});

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new HashMap<String, String>() {{
                put("error", "Failed to update Q&A: " + e.getMessage());
            }});
        }
    }

    /**
     * Delete Q&A
     */
    @DeleteMapping("/qa/delete/{id}")
    public ResponseEntity<?> deleteQA(@PathVariable Long id) {
        try {
            boolean deleted = chatbotService.deleteQA(id);
            
            if (!deleted) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new HashMap<String, String>() {{
                    put("error", "Q&A with id " + id + " not found");
                }});
            }

            return ResponseEntity.ok(new HashMap<String, Object>() {{
                put("success", true);
                put("message", "Q&A deleted successfully");
            }});

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new HashMap<String, String>() {{
                put("error", "Failed to delete Q&A: " + e.getMessage());
            }});
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(new HashMap<String, String>() {{
            put("status", "Chatbot service is running");
        }});
    }
}
