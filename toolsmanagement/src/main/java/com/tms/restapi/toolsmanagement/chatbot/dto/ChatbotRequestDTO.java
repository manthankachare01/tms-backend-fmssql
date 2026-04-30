package com.tms.restapi.toolsmanagement.chatbot.dto;

public class ChatbotRequestDTO {
    private String query;

    public ChatbotRequestDTO() {
    }

    public ChatbotRequestDTO(String query) {
        this.query = query;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }
}
