package com.tms.restapi.toolsmanagement.websocket.controller;

import com.tms.restapi.toolsmanagement.websocket.dto.DataMessage;
import com.tms.restapi.toolsmanagement.websocket.dto.WebSocketRequest;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * WebSocket Controller for handling real-time data communication
 * Clients connect to: ws://localhost:8080/ws
 */
@Controller
public class WebSocketController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Handle tool updates and broadcast to all subscribers
     * Client sends: /app/tool-update
     * Subscribers receive: /topic/tools/updates
     */
    @MessageMapping("/tool-update")
    @SendTo("/topic/tools/updates")
    public DataMessage handleToolUpdate(@Payload WebSocketRequest request) {
        System.out.println("Tool update received: " + request);
        
        return new DataMessage(
                "TOOL_UPDATE",
                request.getUserId(),
                request.getPayload(),
                "SUCCESS"
        );
    }

    /**
     * Handle kit status updates
     * Client sends: /app/kit-status
     * Subscribers receive: /topic/kit/status
     */
    @MessageMapping("/kit-status")
    @SendTo("/topic/kit/status")
    public DataMessage handleKitStatusUpdate(@Payload WebSocketRequest request) {
        System.out.println("Kit status update received: " + request);
        
        return new DataMessage(
                "KIT_STATUS",
                request.getUserId(),
                request.getPayload(),
                "SUCCESS"
        );
    }

    /**
     * Handle issuance notifications
     * Client sends: /app/issuance-notify
     * Subscribers receive: /topic/issuance/notifications
     */
    @MessageMapping("/issuance-notify")
    @SendTo("/topic/issuance/notifications")
    public DataMessage handleIssuanceNotification(@Payload WebSocketRequest request) {
        System.out.println("Issuance notification received: " + request);
        
        return new DataMessage(
                "ISSUANCE_NOTIFICATION",
                request.getUserId(),
                request.getPayload(),
                "SUCCESS"
        );
    }

    /**
     * Send real-time notification to specific user
     * This is called internally to push data to a specific user
     */
    public void sendPrivateNotification(String userId, DataMessage message) {
        messagingTemplate.convertAndSendToUser(
                userId,
                "/queue/notifications",
                message
        );
    }

    /**
     * Broadcast message to all connected clients on a topic
     */
    public void broadcastMessage(String topic, DataMessage message) {
        messagingTemplate.convertAndSend("/topic/" + topic, message);
    }

    /**
     * Handle connection status check (ping/pong)
     * Client sends: /app/ping
     * Returns: pong message
     */
    @MessageMapping("/ping")
    @SendTo("/topic/pong")
    public DataMessage handlePing(@Payload WebSocketRequest request) {
        return new DataMessage(
                "PONG",
                "SERVER",
                "Connection is active",
                "SUCCESS"
        );
    }
}
