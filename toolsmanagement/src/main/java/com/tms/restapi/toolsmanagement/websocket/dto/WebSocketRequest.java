package com.tms.restapi.toolsmanagement.websocket.dto;

/**
 * Request message sent from client to server via WebSocket
 */
public class WebSocketRequest {

    private String action;     // Action to perform (e.g., "GET_TOOLS", "SUBSCRIBE_UPDATES")
    private String userId;     // User performing the action
    private Object payload;    // Additional data for the action
    private String requestId;  // Unique request identifier for tracking

    public WebSocketRequest() {
    }

    public WebSocketRequest(String action, String userId) {
        this.action = action;
        this.userId = userId;
    }

    public WebSocketRequest(String action, String userId, Object payload) {
        this.action = action;
        this.userId = userId;
        this.payload = payload;
    }

    // Getters and Setters
    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    @Override
    public String toString() {
        return "WebSocketRequest{" +
                "action='" + action + '\'' +
                ", userId='" + userId + '\'' +
                ", payload=" + payload +
                ", requestId='" + requestId + '\'' +
                '}';
    }
}
