# JWT Implementation - Quick Reference & Testing Guide

## ✅ Implementation Complete

Your Spring Boot backend now generates and returns JWT tokens on login for **SuperAdmin, Admin, Trainer, and Security** roles.

## What Changed

### Before (❌ Broken)
```json
{
  "message": "Admin login successful",
  "token": null,
  "user": { /* user data */ }
}
```

### After (✅ Fixed)
```json
{
  "message": "Admin login successful",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJyb2xlIjoiYWRtaW4iLCJlbWFpbCI6ImFkbWluQGV4YW1wbGUuY29tIiwic3ViIjoiQUQtMDAxIiwiaWF0IjoxNjAxNTEzNjAwLCJleHAiOjE2MDE2MDAwMDB9...",
  "user": { /* user data */ },
  "role": "admin"
}
```

## Quick Test

### 1. Login and Get Token
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "role": "admin",
    "email": "admin@example.com",
    "password": "admin123"
  }'
```

### 2. Use Token in WebSocket Connection (JavaScript)
```javascript
// Step 1: Store token from login response
const loginResponse = await fetch('/api/auth/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    role: 'admin',
    email: 'admin@example.com',
    password: 'admin123'
  })
});

const { token } = await loginResponse.json();

// Step 2: Connect to WebSocket with token
const socket = new SockJS('/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({
  'Authorization': 'Bearer ' + token
}, (frame) => {
  console.log('Connected!');
  
  // Now subscribe to topics
  stompClient.subscribe('/topic/tools/updates', (msg) => {
    console.log('Tool Update:', msg.body);
  });
});
```

### 3. Use Token for Protected API Calls
```bash
# Get the token from login response
TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

# Use token in protected endpoints
curl -X GET http://localhost:8080/api/protected-endpoint \
  -H "Authorization: Bearer $TOKEN"
```

## Configuration

### Environment Variables (Production)
```bash
# Set a strong secret key (minimum 32 characters recommended)
export JWT_SECRET="your-super-secret-key-with-minimum-32-characters-recommended"

# Token expiration in milliseconds (86400000 = 24 hours)
export JWT_EXPIRATION="86400000"
```

### Default Values (Development)
If not set, defaults are:
- Secret: `MySecretKeyForJWTTokenGenerationAndValidation12345`
- Expiration: `86400000` (24 hours)

## User Roles & Login

| Role | Email Example | ID Type | Endpoint |
|------|----------|---------|----------|
| **superadmin** | admin@system.com | Long (auto) | /api/auth/login |
| **admin** | admin@org.com | String | /api/auth/login |
| **trainer** | trainer@org.com | Long (auto) | /api/auth/login |
| **security** | security@org.com | Long (auto) | /api/auth/login |

## Response Format

All login responses include:
```json
{
  "message": "Login successful",
  "token": "JWT_TOKEN_HERE",
  "user": {
    "id/adminId": "user_identifier",
    "email": "user@example.com",
    "name": "User Name",
    "role": "admin/trainer/superadmin/security",
    "status": "active"
  },
  "role": "admin/trainer/superadmin/security"
}
```

## Token Header Format
All authenticated requests must include:
```
Authorization: Bearer <JWT_TOKEN>
```

## Files Modified/Created

### ✅ Created (New Files)
1. **JwtTokenProvider.java** - Token generation & validation
2. **JwtAuthenticationFilter.java** - Request interceptor
3. **WebSecurityConfig.java** - Security configuration
4. **JWT_IMPLEMENTATION.md** - Full documentation

### ✅ Modified
1. **pom.xml** - Added JWT & Spring Security dependencies
2. **AuthController.java** - Generate & return tokens on login
3. **application.properties** - JWT configuration

## Token Lifespan
- **Default Expiration:** 24 hours (86400000ms)
- **After Expiration:** User must login again to get new token
- **No Refresh Token:** For now, login again for new token
  
*(Optional: Implement refresh token endpoint for better UX)*

## Testing Checklist

- [ ] Build project: `mvnw clean compile`
- [ ] Start server: `mvnw spring-boot:run`
- [ ] Test SuperAdmin login → Get token
- [ ] Test Admin login → Get token
- [ ] Test Trainer login → Get token
- [ ] Test Security login → Get token
- [ ] Connect WebSocket with token
- [ ] Subscribe to `/topic/tools/updates`
- [ ] Send message to `/app/tool-update`
- [ ] Verify message is received

## Common Issues & Solutions

### ❌ "Invalid token" error
**Solution:** Token may be expired. Login again to get fresh token.

### ❌ WebSocket connects but no messages
**Solution:** Make sure Authorization header is correct: `Bearer <token>`

### ❌ 401 Unauthorized on protected endpoint
**Solution:** 
1. Get a token by logging in
2. Use header: `Authorization: Bearer <token>`

### ❌ CORS error
**Solution:** CORS is enabled for all origins in development. For production, update:
```java
// In WebSecurityConfig.java
configuration.setAllowedOrigins(Arrays.asList("https://yourdomain.com"));
```

## Decode & Debug JWT Tokens

Visit [jwt.io](https://jwt.io) and paste your token to see:
- User ID (sub)
- Role
- Email
- Expiration time (exp)
- Issued at (iat)

## Next Steps

1. **Frontend Integration:** Update login handler to store and use token
2. **Token Refresh:** (Optional) Implement `/api/auth/refresh` endpoint
3. **Role-Based Access:** (Optional) Add `@PreAuthorize` annotations
4. **Production:** Change `jwt.secret` to strong value via env variable

## Support Files

- Full Documentation: [JWT_IMPLEMENTATION.md](JWT_IMPLEMENTATION.md)
- Source Files:
  - [AuthController.java](src/main/java/com/tms/restapi/toolsmanagement/auth/controller/AuthController.java)
  - [JwtTokenProvider.java](src/main/java/com/tms/restapi/toolsmanagement/security/jwt/JwtTokenProvider.java)
  - [WebSecurityConfig.java](src/main/java/com/tms/restapi/toolsmanagement/config/WebSecurityConfig.java)
