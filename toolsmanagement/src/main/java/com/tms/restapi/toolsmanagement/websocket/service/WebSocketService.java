package com.tms.restapi.toolsmanagement.websocket.service;

import com.tms.restapi.toolsmanagement.websocket.controller.WebSocketController;
import com.tms.restapi.toolsmanagement.websocket.controller.WebSocketDashboardController;
import com.tms.restapi.toolsmanagement.websocket.dto.DataMessage;
import com.tms.restapi.toolsmanagement.admin.dto.AdminDashboardResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service for handling WebSocket operations across the application
 * Use this to send real-time notifications from any service layer
 */
@Service
public class WebSocketService {

    @Autowired
    private WebSocketController wsController;

    @Autowired
    private WebSocketDashboardController dashboardController;

    /**
     * Broadcast tool update to all subscribers
     */
    public void broadcastToolUpdate(Object toolData) {
        DataMessage message = new DataMessage(
                "TOOL_UPDATE",
                "SYSTEM",
                toolData,
                "SUCCESS"
        );
        wsController.broadcastMessage("tools/updates", message);
    }

    /**
     * Broadcast kit status update
     */
    public void broadcastKitStatusUpdate(Object kitData) {
        DataMessage message = new DataMessage(
                "KIT_STATUS",
                "SYSTEM",
                kitData,
                "SUCCESS"
        );
        wsController.broadcastMessage("kit/status", message);
    }

    /**
     * Broadcast issuance notification
     */
    public void broadcastIssuanceNotification(Object issuanceData) {
        DataMessage message = new DataMessage(
                "ISSUANCE_NOTIFICATION",
                "SYSTEM",
                issuanceData,
                "SUCCESS"
        );
        wsController.broadcastMessage("issuance/notifications", message);
    }

    /**
     * Send private notification to a specific user
     */
    public void sendUserNotification(String userId, String messageType, Object data) {
        DataMessage message = new DataMessage(
                messageType,
                "SYSTEM",
                data,
                "SUCCESS"
        );
        wsController.sendPrivateNotification(userId, message);
    }

    /**
     * Send error notification to a specific user
     */
    public void sendErrorNotification(String userId, String errorMessage) {
        DataMessage message = new DataMessage(
                "ERROR",
                "SYSTEM",
                errorMessage,
                "ERROR"
        );
        wsController.sendPrivateNotification(userId, message);
    }

    /**
     * Broadcast custom message to a specific topic
     */
    public void broadcastCustomMessage(String topic, String messageType, Object data) {
        DataMessage message = new DataMessage(
                messageType,
                "SYSTEM",
                data,
                "SUCCESS"
        );
        wsController.broadcastMessage(topic, message);
    }

    /**
     * Send custom message to specific user
     */
    public void sendCustomUserMessage(String userId, String messageType, Object data) {
        DataMessage message = new DataMessage(
                messageType,
                "SYSTEM",
                data,
                "SUCCESS"
        );
        wsController.sendPrivateNotification(userId, message);
    }

    // ===== DASHBOARD OPERATIONS =====

    /**
     * Send real-time dashboard update to specific user
     */
    public void sendDashboardUpdate(String userId, AdminDashboardResponse dashboardData) {
        dashboardController.sendDashboardUpdate(userId, dashboardData);
    }

    /**
     * Broadcast dashboard update to all connected users
     */
    public void broadcastDashboardUpdate(AdminDashboardResponse dashboardData) {
        dashboardController.broadcastDashboardUpdate(dashboardData);
    }

    /**
     * Broadcast stats update (tools, kits, issuances, returns, overdue, damaged)
     */
    public void broadcastStatsUpdate(String statType, Object statData) {
        dashboardController.broadcastStatsUpdate(statType, statData);
    }

    /**
     * Broadcast new activity update (issuance, return, etc.)
     */
    public void broadcastActivityUpdate(Object activity) {
        dashboardController.broadcastActivityUpdate(activity);
    }
}
