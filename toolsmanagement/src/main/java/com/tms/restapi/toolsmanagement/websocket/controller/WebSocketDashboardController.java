package com.tms.restapi.toolsmanagement.websocket.controller;

import com.tms.restapi.toolsmanagement.admin.dto.AdminDashboardResponse;
import com.tms.restapi.toolsmanagement.websocket.dto.DataMessage;
import com.tms.restapi.toolsmanagement.websocket.dto.WebSocketRequest;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * WebSocket Dashboard Controller for real-time dashboard updates
 * Handles dashboard data streaming and live notifications
 */
@Controller
public class WebSocketDashboardController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Handle dashboard subscription request
     * Client sends: /app/dashboard-subscribe
     * Subscribers receive: /topic/dashboard/{userId}/updates
     */
    @MessageMapping("/dashboard-subscribe")
    @SendTo("/topic/dashboard/updates")
    public DataMessage handleDashboardSubscribe(@Payload WebSocketRequest request) {
        System.out.println("Dashboard subscription received: " + request);
        
        return new DataMessage(
                "DASHBOARD_SUBSCRIPTION",
                request.getUserId(),
                "Subscribed to dashboard updates",
                "SUCCESS"
        );
    }

    /**
     * Broadcast dashboard update to specific user
     * Used internally to push real-time dashboard data
     */
    public void sendDashboardUpdate(String userId, AdminDashboardResponse dashboardData) {
        DataMessage message = new DataMessage(
                "DASHBOARD_UPDATE",
                "SYSTEM",
                dashboardData,
                "SUCCESS"
        );
        
        messagingTemplate.convertAndSendToUser(
                userId,
                "/queue/dashboard",
                message
        );
    }

    /**
     * Broadcast dashboard update to all subscribed users
     */
    public void broadcastDashboardUpdate(AdminDashboardResponse dashboardData) {
        DataMessage message = new DataMessage(
                "DASHBOARD_UPDATE",
                "SYSTEM",
                dashboardData,
                "SUCCESS"
        );
        
        messagingTemplate.convertAndSend("/topic/dashboard/updates", message);
    }

    /**
     * Broadcast stats update to all users
     * Used when tool/kit/issuance stats change
     */
    public void broadcastStatsUpdate(String statType, Object statData) {
        DataMessage message = new DataMessage(
                "STATS_UPDATE",
                "SYSTEM",
                statData,
                "SUCCESS"
        );
        
        messagingTemplate.convertAndSend("/topic/dashboard/stats/" + statType, message);
    }

    /**
     * Broadcast activity update (issuance/return activity)
     */
    public void broadcastActivityUpdate(Object activity) {
        DataMessage message = new DataMessage(
                "ACTIVITY_UPDATE",
                "SYSTEM",
                activity,
                "SUCCESS"
        );
        
        messagingTemplate.convertAndSend("/topic/dashboard/activities", message);
    }
}
