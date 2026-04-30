package com.tms.restapi.toolsmanagement.websocket.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller to serve WebSocket test UI
 */
@Controller
@RequestMapping("/websocket")
public class WebSocketUIController {

    /**
     * Serve the WebSocket test console
     * Access at: http://localhost:8080/websocket/test
     */
    @GetMapping("/test")
    public String testConsole() {
        return "websocket-test";
    }
}
