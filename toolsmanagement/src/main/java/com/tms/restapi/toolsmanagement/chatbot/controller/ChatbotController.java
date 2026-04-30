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

@RestController
@RequestMapping("/api/chatbot")
@CrossOrigin(origins = "*")
public class ChatbotController {

    @Autowired
    private ChatbotService chatbotService;

    /**
     * Process user query and get answer from chatbot
     * Supports both predefined and dynamic database queries
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
     * Get all predefined Q&A
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
     * Add new predefined Q&A
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
