package com.tms.restapi.toolsmanagement.websocket.dto;

import java.time.LocalDateTime;

/**
 * Message class for real-time data transmission via WebSocket
 */
public class DataMessage {

    private String messageType;      // Type of message (e.g., "TOOL_UPDATE", "KIT_STATUS", "NOTIFICATION")
    private String sender;           // Who sent the message
    private Object data;             // Actual payload data
    private LocalDateTime timestamp; // When the message was created
    private String status;           // Status of the operation (SUCCESS, ERROR, etc.)

    public DataMessage() {
        this.timestamp = LocalDateTime.now();
    }

    public DataMessage(String messageType, String sender, Object data) {
        this.messageType = messageType;
        this.sender = sender;
        this.data = data;
        this.timestamp = LocalDateTime.now();
        this.status = "SUCCESS";
    }

    public DataMessage(String messageType, String sender, Object data, String status) {
        this.messageType = messageType;
        this.sender = sender;
        this.data = data;
        this.timestamp = LocalDateTime.now();
        this.status = status;
    }

    // Getters and Setters
    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "DataMessage{" +
                "messageType='" + messageType + '\'' +
                ", sender='" + sender + '\'' +
                ", data=" + data +
                ", timestamp=" + timestamp +
                ", status='" + status + '\'' +
                '}';
    }
}
