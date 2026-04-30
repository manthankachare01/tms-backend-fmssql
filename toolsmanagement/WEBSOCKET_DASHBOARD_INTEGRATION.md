# WebSocket Dashboard Integration Guide

## Overview
WebSocket has been integrated with your dashboard API to provide **real-time dashboard updates** without the need for constant polling.

## Dashboard WebSocket Topics

### 1. Personal Dashboard Updates (One-to-One)
- **Subscribe To**: `/user/queue/dashboard`
- **Message Type**: `DASHBOARD_UPDATE`
- **Use Case**: Send complete dashboard data to specific trainer/admin
- **Trigger**: When trainer makes issuance, return, or stats change

```javascript
stompClient.subscribe('/user/queue/dashboard', (message) => {
    const dashboardData = JSON.parse(message.body);
    console.log('Dashboard updated:', dashboardData.data);
    // Update UI with new dashboard data
});
```

### 2. Public Dashboard Updates (Broadcast)
- **Subscribe To**: `/topic/dashboard/updates`
- **Message Type**: `DASHBOARD_UPDATE`
- **Use Case**: Broadcast dashboard changes to all connected users
- **Trigger**: Global stats change

```javascript
stompClient.subscribe('/topic/dashboard/updates', (message) => {
    const update = JSON.parse(message.body);
    // Update public dashboard view
});
```

### 3. Stats Updates (Granular)
- **Subscribe To**: `/topic/dashboard/stats/{statType}`
- **Message Type**: `STATS_UPDATE`
- **Use Case**: Subscribe to specific metrics
- **Types**: `totalTools`, `totalKits`, `issuanceToday`, `overdue`, `damaged`

```javascript
// Subscribe to specific stat
stompClient.subscribe('/topic/dashboard/stats/overdue', (message) => {
    const overdueCount = JSON.parse(message.body).data;
    // Update only overdue count in UI
});

stompClient.subscribe('/topic/dashboard/stats/totalTools', (message) => {
    const totalTools = JSON.parse(message.body).data;
    // Update tool count
});
```

### 4. Activity Updates (Real-Time Activity Feed)
- **Subscribe To**: `/topic/dashboard/activities`
- **Message Type**: `ACTIVITY_UPDATE`
- **Use Case**: Real-time activity feed (issuances, returns)
- **Trigger**: Whenever issuance/return happens

```javascript
stompClient.subscribe('/topic/dashboard/activities', (message) => {
    const activity = JSON.parse(message.body);
    // Add to activity feed at top
    addActivityToFeed(activity.data);
});
```

## Integration Examples

### Example 1: Update Dashboard After Tool Issuance

In `IssuanceService.java`:

```java
@Autowired
private WebSocketService wsService;

@Autowired
private TrainerDashboardService dashboardService;

public void createIssuance(IssuanceRequest request) {
    // ... existing issuance creation logic ...
    
    Issuance issuance = issuanceRepository.save(new Issuance(...));
    
    // Send real-time dashboard update to trainer
    AdminDashboardResponse dashboard = dashboardService.getDashboardForTrainer(request.getTrainerId());
    wsService.sendDashboardUpdate(request.getTrainerId().toString(), dashboard);
    
    // Broadcast activity
    ActivityDto activity = new ActivityDto("Tool Issued", ..., issuance.getIssuanceDate());
    wsService.broadcastActivityUpdate(activity);
    
    // Update specific stats
    wsService.broadcastStatsUpdate("issuanceToday", 1);
}
```

### Example 2: Update Dashboard After Tool Return

In `ReturnService.java` (create if not exists):

```java
@Autowired
private WebSocketService wsService;

@Autowired
private AdminDashboardService adminDashboardService;

public void processReturn(ReturnRequest request) {
    // ... existing return logic ...
    
    ReturnRecord returnRecord = returnRepository.save(new ReturnRecord(...));
    
    // Send updated dashboard to trainer
    AdminDashboardResponse dashboard = dashboardService.getDashboardForTrainer(request.getTrainerId());
    wsService.sendDashboardUpdate(request.getTrainerId().toString(), dashboard);
    
    // Broadcast activity
    wsService.broadcastActivityUpdate(returnRecord);
    
    // Update stats
    wsService.broadcastStatsUpdate("returnsToday", 1);
    
    // Update damaged count if applicable
    int damagedCount = calculateDamagedCount(returnRecord);
    wsService.broadcastStatsUpdate("damaged", damagedCount);
}
```

### Example 3: Update Overdue Status in Real-Time

In `IssuanceService.java`:

```java
@Autowired
private WebSocketService wsService;

public void updateOverdueStatuses() {
    List<Issuance> overdueIssuances = // ... find overdue ...
    
    for (Issuance issuance : overdueIssuances) {
        issuance.setStatus("OVERDUE");
        issuanceRepository.save(issuance);
        
        // Push update to that trainer's dashboard
        AdminDashboardResponse dashboard = 
            dashboardService.getDashboardForTrainer(issuance.getTrainerId());
        wsService.sendDashboardUpdate(
            issuance.getTrainerId().toString(), 
            dashboard
        );
    }
    
    // Update global overdue count
    int totalOverdue = issuanceRepository.countByStatus("OVERDUE");
    wsService.broadcastStatsUpdate("overdue", totalOverdue);
}
```

## Client-Side Dashboard Implementation

### React Dashboard Component

```javascript
import React, { useEffect, useState } from 'react';
import SockJS from 'sockjs-client';
import Stomp from 'stompjs';

function TrainerDashboard({ trainerId, userId }) {
    const [dashboard, setDashboard] = useState({
        totalTools: 0,
        totalKits: 0,
        issuanceToday: 0,
        overdueIssuance: 0,
        damagedCount: 0,
        activities: []
    });

    const [stompClient, setStompClient] = useState(null);

    useEffect(() => {
        // Connect to WebSocket
        const socket = new SockJS('/ws');
        const client = Stomp.over(socket);

        client.connect({}, (frame) => {
            setStompClient(client);

            // Subscribe to personal dashboard updates
            client.subscribe(`/user/queue/dashboard`, (message) => {
                const update = JSON.parse(message.body);
                setDashboard(update.data);
            });

            // Subscribe to activity feed
            client.subscribe('/topic/dashboard/activities', (message) => {
                const activity = JSON.parse(message.body);
                setDashboard(prev => ({
                    ...prev,
                    activities: [activity.data, ...prev.activities]
                }));
            });

            // Subscribe to overdue updates
            client.subscribe('/topic/dashboard/stats/overdue', (message) => {
                const overdueCount = JSON.parse(message.body);
                setDashboard(prev => ({
                    ...prev,
                    overdueIssuance: overdueCount.data
                }));
            });

            // Subscribe to damaged count updates
            client.subscribe('/topic/dashboard/stats/damaged', (message) => {
                const damagedCount = JSON.parse(message.body);
                setDashboard(prev => ({
                    ...prev,
                    damagedCount: damagedCount.data
                }));
            });
        });

        return () => {
            if (client && client.connected) {
                client.disconnect();
            }
        };
    }, [userId]);

    return (
        <div className="dashboard">
            <div className="stats-grid">
                <StatCard 
                    title="Total Tools Issued" 
                    value={dashboard.totalTools}
                    icon="📦"
                />
                <StatCard 
                    title="Total Returns" 
                    value={dashboard.totalKits}
                    icon="🔄"
                />
                <StatCard 
                    title="Issuance Today" 
                    value={dashboard.issuanceToday}
                    icon="📬"
                />
                <StatCard 
                    title="Overdue" 
                    value={dashboard.overdueIssuance}
                    icon="⚠️"
                    color="red"
                />
                <StatCard 
                    title="Damaged/Missing" 
                    value={dashboard.damagedCount}
                    icon="❌"
                    color="orange"
                />
            </div>

            <div className="activity-feed">
                <h2>Recent Activity (Live Updates)</h2>
                <div className="activities">
                    {dashboard.activities.map((activity, idx) => (
                        <ActivityItem key={idx} activity={activity} />
                    ))}
                </div>
            </div>
        </div>
    );
}

function StatCard({ title, value, icon, color = 'blue' }) {
    return (
        <div className={`stat-card stat-${color}`}>
            <div className="stat-icon">{icon}</div>
            <div className="stat-title">{title}</div>
            <div className="stat-value">{value}</div>
        </div>
    );
}

function ActivityItem({ activity }) {
    return (
        <div className="activity-item">
            <span className="activity-type">{activity.type}</span>
            <span className="activity-user">{activity.userName}</span>
            <span className="activity-item-name">{activity.itemType}</span>
            <span className="activity-time">{activity.timeAgo}</span>
        </div>
    );
}

export default TrainerDashboard;
```

### Vanilla JavaScript Dashboard Example

```html
<div id="dashboard">
    <div class="stats">
        <div class="stat-card">
            <h3>Total Tools</h3>
            <p id="totalTools">0</p>
        </div>
        <div class="stat-card">
            <h3>Total Returns</h3>
            <p id="totalKits">0</p>
        </div>
        <div class="stat-card">
            <h3>Today's Issuance</h3>
            <p id="issuanceToday">0</p>
        </div>
        <div class="stat-card warning">
            <h3>Overdue</h3>
            <p id="overdue">0</p>
        </div>
    </div>

    <div class="activities">
        <h2>Recent Activities (Live)</h2>
        <ul id="activityList"></ul>
    </div>
</div>

<script>
let stompClient = null;

function connectDashboard(userId) {
    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);

    stompClient.connect({}, () => {
        // Subscribe to personal dashboard
        stompClient.subscribe('/user/queue/dashboard', (msg) => {
            const data = JSON.parse(msg.body).data;
            updateDashboard(data);
        });

        // Subscribe to activities
        stompClient.subscribe('/topic/dashboard/activities', (msg) => {
            const activity = JSON.parse(msg.body).data;
            addActivityToFeed(activity);
        });

        // Subscribe to specific stat updates
        stompClient.subscribe('/topic/dashboard/stats/overdue', (msg) => {
            document.getElementById('overdue').textContent = JSON.parse(msg.body).data;
        });
    });
}

function updateDashboard(data) {
    document.getElementById('totalTools').textContent = data.totalTools;
    document.getElementById('totalKits').textContent = data.totalKits;
    document.getElementById('issuanceToday').textContent = data.issuanceToday;
    document.getElementById('overdue').textContent = data.overdueIssuance;
}

function addActivityToFeed(activity) {
    const list = document.getElementById('activityList');
    const li = document.createElement('li');
    li.className = 'activity-item';
    li.innerHTML = `
        <span class="type">${activity.activityType}</span>
        <span class="user">${activity.userName}</span>
        <span class="time">${activity.timeAgo}</span>
    `;
    list.insertBefore(li, list.firstChild);
}

// Start connection
window.addEventListener('load', () => {
    connectDashboard('user123');
});
</script>
```

## Message Format

### Dashboard Update Message
```json
{
    "messageType": "DASHBOARD_UPDATE",
    "sender": "SYSTEM",
    "data": {
        "totalTools": 42,
        "totalKits": 28,
        "issuanceToday": 5,
        "returnsToday": 3,
        "overdueIssuance": 2,
        "damagedCount": 1,
        "activities": [...]
    },
    "timestamp": "2024-01-18T15:30:00",
    "status": "SUCCESS"
}
```

### Activity Update Message
```json
{
    "messageType": "ACTIVITY_UPDATE",
    "sender": "SYSTEM",
    "data": {
        "activityType": "Tool Issued",
        "userName": "Trainer John",
        "itemType": "Tool",
        "itemName": "Wrench Set",
        "timestamp": "2024-01-18T15:30:00",
        "timeAgo": "Just now"
    },
    "timestamp": "2024-01-18T15:30:00",
    "status": "SUCCESS"
}
```

## Benefits

✅ **Real-time Updates**: Dashboard updates instantly without page refresh  
✅ **Reduced Server Load**: No polling = fewer requests  
✅ **Better UX**: Smooth, live data updates  
✅ **Scalable**: Can handle multiple concurrent connections  
✅ **Granular Control**: Subscribe to specific metrics  
✅ **Personal & Broadcast**: Send to specific users or all users  

## Performance Tips

1. **Unsubscribe when not needed**: Reduce WebSocket overhead
2. **Batch updates**: Combine multiple changes into one message
3. **Throttle updates**: Limit frequency of high-frequency updates
4. **Compress data**: Send only changed fields instead of full object
5. **Use private messages**: One-to-one updates are more efficient than broadcast

## Testing

Open the WebSocket test console:
```
http://localhost:8080/websocket/test
```

1. Connect to WebSocket
2. Subscribe to `/topic/dashboard/updates`
3. Simulate issuance/return from your API
4. Watch real-time updates appear in the console

---

**Next Steps:**
1. Add WebSocket calls to IssuanceService, ReturnService
2. Update dashboard frontend to subscribe to WebSocket topics
3. Test with provided React/Vanilla JS examples
