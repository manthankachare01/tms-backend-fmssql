# API Usage & Integration Guide

## 🎯 Integration Paths

### For Frontend Developers (Angular/React)

#### 1. Chatbot Widget Integration
```typescript
// Angular example
import { HttpClient } from '@angular/common/http';

@Injectable()
export class ChatbotService {
  constructor(private http: HttpClient) {}

  askQuestion(query: string) {
    return this.http.post('/api/chatbot/ask', { query });
  }

  getAllQAs() {
    return this.http.get('/api/chatbot/qa/all');
  }

  addQA(question: string, answer: string) {
    return this.http.post('/api/chatbot/qa/add', { question, answer });
  }
}

// Component usage
this.chatbotService.askQuestion("What tools are available?")
  .subscribe(response => {
    console.log(response.answer);
    console.log(response.responseTime);
  });
```

#### 2. Dashboard Integration
```typescript
import { HttpClient } from '@angular/common/http';

@Injectable()
export class ReportsService {
  constructor(private http: HttpClient) {}

  getToolStatistics() {
    return this.http.get('/api/reports/tools/statistics');
  }

  getDashboardOverview() {
    return this.http.get('/api/reports/dashboard/overview');
  }

  getComprehensiveReport() {
    return this.http.get('/api/reports/comprehensive');
  }
}

// Dashboard component
this.reportsService.getComprehensiveReport()
  .subscribe(data => {
    this.toolStats = data.toolStatistics;
    this.issuanceStats = data.issuanceStatistics;
    this.overview = data.dashboardOverview;
    // Bind data to charts
  });
```

---

## 📊 Chart Integration Examples

### 1. Tool Availability Pie Chart (Chart.js)
```typescript
// Using Chart.js library
import { Chart } from 'chart.js';

constructor(private reports: ReportsService) {}

initToolChart() {
  this.reports.getToolStatistics().subscribe(stats => {
    new Chart('toolChart', {
      type: 'pie',
      data: {
        labels: ['Available', 'Unavailable'],
        datasets: [{
          data: [stats.data.availableTools, stats.data.unavailableTools],
          backgroundColor: ['#4CAF50', '#F44336']
        }]
      }
    });
  });
}
```

### 2. Monthly Trend Line Chart
```typescript
initTrendChart() {
  this.reports.getMonthlyTrend().subscribe(trend => {
    const months = trend.data.map(t => t.month);
    const issues = trend.data.map(t => t.issue_count);
    const returns = trend.data.map(t => t.return_count);

    new Chart('trendChart', {
      type: 'line',
      data: {
        labels: months,
        datasets: [
          {
            label: 'Issues',
            data: issues,
            borderColor: '#2196F3',
            tension: 0.1
          },
          {
            label: 'Returns',
            data: returns,
            borderColor: '#4CAF50',
            tension: 0.1
          }
        ]
      }
    });
  });
}
```

### 3. Location Comparison Bar Chart
```typescript
initLocationChart() {
  this.reports.getLocationStatistics().subscribe(locations => {
    new Chart('locationChart', {
      type: 'bar',
      data: {
        labels: locations.data.map(l => l.location),
        datasets: [
          {
            label: 'Total Tools',
            data: locations.data.map(l => l.totalTools),
            backgroundColor: '#2196F3'
          },
          {
            label: 'Available',
            data: locations.data.map(l => l.availableTools),
            backgroundColor: '#4CAF50'
          }
        ]
      }
    });
  });
}
```

### 4. Top Tools Bar Chart
```typescript
initTopToolsChart() {
  this.reports.getTopIssuedTools(10).subscribe(data => {
    new Chart('topToolsChart', {
      type: 'horizontalBar',
      data: {
        labels: data.data.map(t => t.description),
        datasets: [{
          label: 'Issues Count',
          data: data.data.map(t => t.issue_count),
          backgroundColor: '#FF9800'
        }]
      }
    });
  });
}
```

---

## 🔌 API Consumption Patterns

### Pattern 1: Single Endpoint
```javascript
// Simple query
fetch('http://localhost:8080/api/chatbot/ask', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ query: "What is the system name?" })
})
.then(r => r.json())
.then(data => console.log(data.answer));
```

### Pattern 2: Bulk Data Loading
```javascript
// Load all analytics at once (optimized)
fetch('http://localhost:8080/api/reports/comprehensive')
  .then(r => r.json())
  .then(data => {
    updateToolCharts(data.toolStatistics);
    updateIssuanceCharts(data.issuanceStatistics);
    updateDashboard(data.dashboardOverview);
    updateLocationMap(data.locationStatistics);
  });
```

### Pattern 3: Polling for Updates
```javascript
// Refresh reports every 5 minutes
setInterval(() => {
  fetch('http://localhost:8080/api/reports/tools/statistics')
    .then(r => r.json())
    .then(data => updateToolChart(data));
}, 5 * 60 * 1000);
```

### Pattern 4: Error Handling
```javascript
fetch('http://localhost:8080/api/chatbot/ask', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ query: userInput })
})
.then(r => {
  if (!r.ok) throw new Error(`HTTP error! status: ${r.status}`);
  return r.json();
})
.then(data => {
  if (data.success) {
    displayAnswer(data.answer);
  } else {
    showError(data.message);
  }
})
.catch(error => {
  console.error('Failed to get response:', error);
  showError('Unable to process your request');
});
```

---

## 🎨 UI Component Examples

### Chatbot Widget
```html
<div class="chatbot-widget">
  <div class="chat-header">
    <h3>Tools Assistant</h3>
  </div>
  
  <div class="chat-messages">
    <div *ngFor="let msg of messages" [class]="msg.type">
      {{ msg.text }}
      <small *ngIf="msg.type === 'assistant' && msg.responseTime">
        {{ msg.responseTime }}ms
      </small>
    </div>
  </div>
  
  <div class="chat-input">
    <input 
      #query 
      type="text" 
      placeholder="Ask about tools, issuances, trainers..."
      (keyup.enter)="askQuestion(query.value)"
    />
    <button (click)="askQuestion(query.value)">Send</button>
  </div>
</div>
```

### Statistics Cards
```html
<div class="stats-container">
  <div class="stat-card">
    <h4>Total Tools</h4>
    <p class="big-number">{{ overview.totalTools }}</p>
  </div>
  
  <div class="stat-card">
    <h4>Available</h4>
    <p class="big-number" [style.color]="getAvailabilityColor()">
      {{ overview.toolAvailabilityPercentage }}%
    </p>
  </div>
  
  <div class="stat-card">
    <h4>Pending Approvals</h4>
    <p class="big-number alert" *ngIf="overview.pendingApprovals > 0">
      {{ overview.pendingApprovals }}
    </p>
  </div>
  
  <div class="stat-card">
    <h4>Maintenance Needed</h4>
    <p class="big-number warning">
      {{ overview.toolsNeedingMaintenance }}
    </p>
  </div>
</div>
```

---

## 🔄 Common Workflows

### Workflow 1: Daily Dashboard Refresh
```javascript
async function refreshDashboard() {
  try {
    // 1. Load comprehensive data
    const response = await fetch('/api/reports/comprehensive');
    const data = await response.json();

    // 2. Update all sections
    updateToolStats(data.toolStatistics);
    updateIssuanceStats(data.issuanceStatistics);
    updateLocationStats(data.locationStatistics);
    updateDashboard(data.dashboardOverview);

    // 3. Schedule next refresh
    setTimeout(refreshDashboard, 5 * 60 * 1000);
  } catch (error) {
    console.error('Dashboard refresh failed:', error);
    showNotification('Failed to refresh dashboard', 'error');
  }
}
```

### Workflow 2: Interactive Chatbot
```javascript
class ChatbotAssistant {
  constructor() {
    this.conversationHistory = [];
  }

  async processQuery(userInput) {
    try {
      // 1. Send query
      const response = await fetch('/api/chatbot/ask', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ query: userInput })
      });

      const data = await response.json();

      // 2. Store in history
      this.conversationHistory.push({
        user: userInput,
        assistant: data.answer,
        type: data.type,
        time: new Date()
      });

      // 3. Display response
      return {
        message: data.answer,
        type: data.type,
        responseTime: data.responseTime
      };
    } catch (error) {
      return {
        message: 'Sorry, I could not process your request.',
        type: 'error'
      };
    }
  }
}
```

### Workflow 3: Admin Q&A Management
```javascript
class QAManager {
  async loadAllQAs() {
    const response = await fetch('/api/chatbot/qa/all');
    return response.json();
  }

  async addQA(question, answer) {
    const response = await fetch('/api/chatbot/qa/add', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ question, answer })
    });
    return response.json();
  }

  async updateQA(id, question, answer) {
    const response = await fetch(`/api/chatbot/qa/update/${id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ question, answer })
    });
    return response.json();
  }

  async deleteQA(id) {
    const response = await fetch(`/api/chatbot/qa/delete/${id}`, {
      method: 'DELETE'
    });
    return response.json();
  }
}
```

---

## 📱 Mobile Integration

### React Native Example
```javascript
import { useEffect, useState } from 'react';
import { View, Text, TextInput, Button, ScrollView } from 'react-native';

export default function ChatbotScreen() {
  const [query, setQuery] = useState('');
  const [response, setResponse] = useState(null);
  const [loading, setLoading] = useState(false);

  const askQuestion = async () => {
    setLoading(true);
    try {
      const res = await fetch('http://api.example.com:8080/api/chatbot/ask', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ query })
      });
      const data = await res.json();
      setResponse(data);
    } catch (error) {
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <View>
      <TextInput
        placeholder="Ask a question..."
        value={query}
        onChangeText={setQuery}
      />
      <Button title="Send" onPress={askQuestion} disabled={loading} />
      {response && <Text>{response.answer}</Text>}
    </View>
  );
}
```

---

## 🔍 Monitoring & Logging

### Health Check Monitoring
```javascript
async function monitorServices() {
  const services = [
    { name: 'Chatbot', url: '/api/chatbot/health' },
    { name: 'Reports', url: '/api/reports/health' }
  ];

  for (const service of services) {
    try {
      const response = await fetch(service.url);
      const data = await response.json();
      console.log(`${service.name}: ${data.status}`);
    } catch (error) {
      console.error(`${service.name}: OFFLINE`);
    }
  }
}

// Run every minute
setInterval(monitorServices, 60 * 1000);
```

### Response Time Tracking
```javascript
async function trackPerformance(endpoint) {
  const start = performance.now();
  const response = await fetch(endpoint);
  const end = performance.now();

  const duration = end - start;
  console.log(`${endpoint}: ${duration.toFixed(2)}ms`);

  if (duration > 1000) {
    console.warn(`Slow API: ${endpoint} took ${duration}ms`);
  }

  return response;
}
```

---

## 🚨 Error Handling Best Practices

### Comprehensive Error Handler
```javascript
async function safeAPICall(url, options = {}) {
  try {
    const response = await fetch(url, options);

    // Handle HTTP errors
    if (!response.ok) {
      throw new Error(`HTTP ${response.status}: ${response.statusText}`);
    }

    const data = await response.json();

    // Handle API errors
    if (!data.success && data.error) {
      throw new Error(data.error || data.message);
    }

    return data;
  } catch (error) {
    // Log error
    console.error('API Error:', {
      url,
      message: error.message,
      timestamp: new Date()
    });

    // Show user-friendly message
    showError(error.message);

    // Re-throw for caller
    throw error;
  }
}
```

---

## 📊 Dashboard Layout Example

```html
<div class="dashboard">
  <!-- Header -->
  <header class="dashboard-header">
    <h1>Tools Management Dashboard</h1>
    <span class="last-updated">Last updated: {{ lastUpdate }}</span>
  </header>

  <!-- KPI Cards -->
  <section class="kpi-section">
    <div class="kpi-card">
      <h3>Total Tools</h3>
      <div class="value">{{ overview.totalTools }}</div>
    </div>
    <div class="kpi-card">
      <h3>Availability</h3>
      <div class="value">{{ overview.toolAvailabilityPercentage }}%</div>
    </div>
    <div class="kpi-card">
      <h3>Pending Approvals</h3>
      <div class="value alert">{{ overview.pendingApprovals }}</div>
    </div>
  </section>

  <!-- Charts -->
  <section class="charts-section">
    <div class="chart-container">
      <h3>Tool Availability</h3>
      <canvas id="toolChart"></canvas>
    </div>
    <div class="chart-container">
      <h3>Monthly Trend</h3>
      <canvas id="trendChart"></canvas>
    </div>
  </section>

  <!-- Chatbot -->
  <section class="chatbot-section">
    <div class="chatbot-widget">
      <h3>Ask Tools Assistant</h3>
      <!-- Chatbot UI -->
    </div>
  </section>
</div>
```

---

## ✅ Integration Checklist

- [ ] Backend API running on localhost:8080
- [ ] Database migrations completed
- [ ] API endpoints tested with Postman
- [ ] CORS configured for frontend domain
- [ ] Error handling implemented
- [ ] Loading states added
- [ ] Chart libraries installed
- [ ] API service created
- [ ] Dashboard components built
- [ ] Chatbot widget integrated
- [ ] Health monitoring active
- [ ] Performance tracking enabled

---

**Ready to integrate? Start with the Quick Start guide and use these examples!** 🚀
