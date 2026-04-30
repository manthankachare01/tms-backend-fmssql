package com.tms.restapi.toolsmanagement.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * Configure message broker for handling subscriptions and broadcasting messages
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable simple memory-based message broker
        config.enableSimpleBroker("/topic", "/queue");
        // Set the prefix for messages sent from client to server
        config.setApplicationDestinationPrefixes("/app");
    }

    /**
     * Configure WebSocket endpoints that clients can connect to
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Register WebSocket endpoint - clients connect to ws://localhost:8080/ws
        // Allow the frontend origin(s) explicitly because SockJS may use credentials
        registry.addEndpoint("/ws")
            .setAllowedOrigins("http://localhost:5173", "https://tools-management-xi.vercel.app/" ,"https://toolsmanagement-backend.onrender.com")
            .withSockJS(); // Enable SockJS fallback for browsers that don't support WebSocket
    }
}
