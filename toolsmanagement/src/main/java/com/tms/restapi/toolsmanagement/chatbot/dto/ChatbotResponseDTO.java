package com.tms.restapi.toolsmanagement.chatbot.dto;

public class ChatbotResponseDTO {
    private String query;
    private String answer;
    private String type; // "predefined" or "dynamic"
    private Long responseTime; // in milliseconds
    private Boolean success;
    private String message;

    public ChatbotResponseDTO() {
    }

    public ChatbotResponseDTO(String query, String answer, String type, Long responseTime, Boolean success) {
        this.query = query;
        this.answer = answer;
        this.type = type;
        this.responseTime = responseTime;
        this.success = success;
    }

    public ChatbotResponseDTO(String query, Boolean success, String message) {
        this.query = query;
        this.success = success;
        this.message = message;
    }

    // Getters and Setters
    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(Long responseTime) {
        this.responseTime = responseTime;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
