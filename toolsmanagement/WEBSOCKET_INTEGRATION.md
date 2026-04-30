# WebSocket Integration Guide

## Overview
WebSocket support has been integrated into your Spring Boot REST API for real-time data communication. This enables bi-directional communication between server and clients.

## Architecture

### WebSocket Endpoint
- **URL**: `ws://localhost:8080/ws` (WebSocket) or `http://localhost:8080/ws` (SockJS fallback)
- **STOMP Protocol**: Supported for message routing
- **Message Broker**: In-memory Simple Broker

### Message Topics

#### 1. Tool Updates
- **Send To**: `/app/tool-update`
- **Subscribe To**: `/topic/tools/updates`
- **Message Type**: `TOOL_UPDATE`
- **Use Case**: Real-time tool availability changes, status updates

#### 2. Kit Status
- **Send To**: `/app/kit-status`
- **Subscribe To**: `/topic/kit/status`
- **Message Type**: `KIT_STATUS`
- **Use Case**: Kit assembly/disassembly status updates

#### 3. Issuance Notifications
- **Send To**: `/app/issuance-notify`
- **Subscribe To**: `/topic/issuance/notifications`
- **Message Type**: `ISSUANCE_NOTIFICATION`
- **Use Case**: Real-time notification when tools are issued

#### 4. Health Check (Ping/Pong)
- **Send To**: `/app/ping`
- **Subscribe To**: `/topic/pong`
- **Message Type**: `PONG`
- **Use Case**: Keep-alive check

### Private Messages (One-to-One)
- **Subscribe To**: `/user/queue/notifications`
- **Method**: `sendPrivateNotification(userId, message)`
- **Use Case**: Send notifications to specific users

## Installation

### 1. Dependencies Added
The following dependencies have been automatically added to `pom.xml`:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-websocket</artifactId>
</dependency>
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
</dependency>
```

### 2. Files Created

#### Configuration
- `config/WebSocketConfig.java` - WebSocket configuration with message broker setup

#### Controllers
- `websocket/controller/WebSocketController.java` - Main WebSocket handler

#### DTOs
- `websocket/dto/DataMessage.java` - Real-time message format
- `websocket/dto/WebSocketRequest.java` - Client request format

## Client-Side Implementation

### JavaScript/Web Client Example

```javascript
// 1. Install SockJS and Stomp.js
// <script src="https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js"></script>
// <script src="https://cdn.jsdelivr.net/npm/stompjs@2.3.3/lib/stomp.min.js"></script>

// 2. Connect and Subscribe
let stompClient = null;

function connect() {
    let socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);
    
    stompClient.connect({}, function(frame) {
        console.log('Connected: ' + frame.server);
        
        // Subscribe to tool updates
        stompClient.subscribe('/topic/tools/updates', function(message) {
            console.log('Tool Update:', JSON.parse(message.body));
        });
        
        // Subscribe to kit status
        stompClient.subscribe('/topic/kit/status', function(message) {
            console.log('Kit Status:', JSON.parse(message.body));
        });
        
        // Subscribe to issuance notifications
        stompClient.subscribe('/topic/issuance/notifications', function(message) {
            console.log('Issuance Notification:', JSON.parse(message.body));
        });
        
        // Subscribe to private messages (one-to-one)
        stompClient.subscribe('/user/queue/notifications', function(message) {
            console.log('Private Notification:', JSON.parse(message.body));
        });
    });
}

// 3. Send Messages
function sendToolUpdate(toolData) {
    const request = {
        action: "UPDATE_TOOL",
        userId: "user123",
        payload: toolData
    };
    stompClient.send("/app/tool-update", {}, JSON.stringify(request));
}

function sendKitStatus(kitData) {
    const request = {
        action: "UPDATE_KIT",
        userId: "user123",
        payload: kitData
    };
    stompClient.send("/app/kit-status", {}, JSON.stringify(request));
}

function sendIssuanceNotification(issuanceData) {
    const request = {
        action: "NOTIFY_ISSUANCE",
        userId: "user123",
        payload: issuanceData
    };
    stompClient.send("/app/issuance-notify", {}, JSON.stringify(request));
}

// 4. Health Check
function checkConnection() {
    stompClient.send("/app/ping", {}, JSON.stringify({action: "PING", userId: "user123"}));
}

// 5. Disconnect
function disconnect() {
    if (stompClient !== null) {
        stompClient.disconnect(() => {
            console.log("Disconnected");
        });
    }
}

// Example usage
window.addEventListener('load', () => {
    connect();
    
    // Send sample data
    setTimeout(() => {
        sendToolUpdate({toolId: 1, status: "AVAILABLE"});
    }, 1000);
});
```

### React Client Example

```javascript
import React, { useEffect, useState } from 'react';
import SockJS from 'sockjs-client';
import Stomp from 'stompjs';

function WebSocketComponent() {
    const [messages, setMessages] = useState([]);
    const [stompClient, setStompClient] = useState(null);

    useEffect(() => {
        const socket = new SockJS('/ws');
        const client = Stomp.over(socket);
        
        client.connect({}, (frame) => {
            console.log('Connected');
            setStompClient(client);
            
            // Subscribe to updates
            client.subscribe('/topic/tools/updates', (message) => {
                setMessages(prev => [...prev, JSON.parse(message.body)]);
            });
        });
        
        return () => {
            if (client && client.connected) {
                client.disconnect();
            }
        };
    }, []);

    const sendUpdate = () => {
        if (stompClient) {
            stompClient.send('/app/tool-update', {}, JSON.stringify({
                action: 'UPDATE',
                userId: 'user123',
                payload: { toolId: 1, status: 'AVAILABLE' }
            }));
        }
    };

    return (
        <div>
            <button onClick={sendUpdate}>Send Update</button>
            <ul>
                {messages.map((msg, idx) => (
                    <li key={idx}>{JSON.stringify(msg)}</li>
                ))}
            </ul>
        </div>
    );
}

export default WebSocketComponent;
```

## Java/Spring Client Example

```java
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

public class WebSocketClient {
    public static void main(String[] args) throws Exception {
        WebSocketClient webSocketClient = new StandardWebSocketClient();
        WebSocketStompClient stompClient = new WebSocketStompClient(webSocketClient);
        
        StompSession session = stompClient.connect("ws://localhost:8080/ws", new StompSessionHandlerAdapter() {
            @Override
            public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                session.subscribe("/topic/tools/updates", new StompFrameHandler() {
                    @Override
                    public Type getPayloadType(StompHeaders headers) {
                        return DataMessage.class;
                    }

                    @Override
                    public void handleFrame(StompHeaders headers, Object payload) {
                        System.out.println("Message received: " + payload);
                    }
                });
            }
        }).get();
        
        Thread.sleep(3000);
        session.disconnect();
    }
}
```

## Server-Side Integration

### Broadcasting to All Users

```java
@Autowired
private WebSocketController wsController;

// In your service class
public void notifyToolUpdate(Tool tool) {
    DataMessage msg = new DataMessage("TOOL_UPDATE", "SYSTEM", tool, "SUCCESS");
    wsController.broadcastMessage("tools/updates", msg);
}
```

### Sending Private Messages

```java
@Autowired
private WebSocketController wsController;

// Send notification to specific user
public void notifyUser(String userId, String message) {
    DataMessage msg = new DataMessage("NOTIFICATION", "SYSTEM", message, "SUCCESS");
    wsController.sendPrivateNotification(userId, msg);
}
```

## Message Format

### DataMessage
```json
{
    "messageType": "TOOL_UPDATE",
    "sender": "user123",
    "data": {
        "toolId": 1,
        "status": "AVAILABLE"
    },
    "timestamp": "2024-01-18T10:30:00",
    "status": "SUCCESS"
}
```

### WebSocketRequest
```json
{
    "action": "UPDATE_TOOL",
    "userId": "user123",
    "payload": {
        "toolId": 1,
        "status": "AVAILABLE"
    },
    "requestId": "req-12345"
}
```

## Configuration

### Update CORS for Production
Edit `WebSocketConfig.java`:
```java
registry.addEndpoint("/ws")
        .setAllowedOrigins("https://yourdomain.com")
        .withSockJS();
```

### Enable Advanced Message Broker (Optional)
For production with multiple server instances, use RabbitMQ:

Add dependency:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-amqp</artifactId>
</dependency>
```

Update `WebSocketConfig.java`:
```java
config.enableStompBrokerRelay("/topic", "/queue")
      .setRelayHost("localhost")
      .setRelayPort(61613);
```

## Troubleshooting

### Connection Issues
1. Check firewall - WebSocket uses port 8080
2. Verify SockJS fallback is enabled for older browsers
3. Check CORS settings if connecting from different origin

### Message Not Received
1. Ensure subscription path matches publisher path
2. Check message format matches DataMessage/WebSocketRequest structure
3. Verify user ID is correct for private messages

### Performance
- Monitor connected clients count
- Use message compression if handling large payloads
- Consider RabbitMQ broker for high-load scenarios

## Testing

Use WebSocket testing tools:
- **Browser DevTools**: Check WebSocket tab in Network
- **Postman**: WebSocket request feature
- **Insomnia**: WebSocket plugin
- **STOMP CLI**: For command-line testing

Example with STOMP CLI:
```bash
stomp -h localhost -p 8080 -u guest -p guest
> CONNECT
> SUBSCRIBE
destination:/topic/tools/updates
> SEND
destination:/app/tool-update
```

---

**Next Steps:**
1. Rebuild project: `mvn clean install`
2. Start application
3. Test with provided client examples
4. Integrate with your service layers
