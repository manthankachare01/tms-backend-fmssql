# JWT Token Implementation for WebSocket Authentication

## Overview
This document explains the JWT (JSON Web Token) implementation for authenticating users in the Tools Management System, enabling secure WebSocket connections for SuperAdmin, Admin, and Trainer roles.

## Problem Solved
**Before:** Login endpoint returned `token: null`, preventing WebSocket authentication.
**After:** Login endpoint generates and returns a valid JWT token for all user roles.

## Components Implemented

### 1. JWT Token Provider
**File:** `src/main/java/com/tms/restapi/toolsmanagement/security/jwt/JwtTokenProvider.java`

Handles JWT token generation and validation:
- `generateToken(userId, email, role)` - Creates a signed JWT token
- `getUserIdFromToken(token)` - Extracts user ID from token
- `getRoleFromToken(token)` - Extracts user role from token
- `getEmailFromToken(token)` - Extracts email from token
- `validateToken(token)` - Validates token signature and expiration

**Configuration:**
- Secret Key: `jwt.secret` (default: MySecretKeyForJWTTokenGenerationAndValidation12345)
- Expiration: `jwt.expiration` (default: 86400000ms = 24 hours)

### 2. JWT Authentication Filter
**File:** `src/main/java/com/tms/restapi/toolsmanagement/security/jwt/JwtAuthenticationFilter.java`

Intercepts incoming requests to:
- Extract JWT token from `Authorization: Bearer <token>` header
- Validate the token
- Set authentication context for secured endpoints
- Allow WebSocket connections with valid tokens

### 3. Web Security Configuration
**File:** `src/main/java/com/tms/restapi/toolsmanagement/config/WebSecurityConfig.java`

Configures Spring Security with:
- CORS enabled for all origins (configurable in production)
- CSRF disabled for REST API
- Stateless session management (JWT-based)
- JWT filter integrated into security chain
- Public endpoints: `/api/auth/**`, `/api/otp/**`, `/api/reset-password/**`, `/ws/**`
- BCrypt password encoder for secure password storage

### 4. Updated AuthController
**File:** `src/main/java/com/tms/restapi/toolsmanagement/auth/controller/AuthController.java`

Modified login endpoint to:
1. Authenticate user credentials (email + password)
2. Generate JWT token using JwtTokenProvider
3. Return token in response along with user details

**Login Response Format:**
```json
{
  "message": "Admin login successful",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "adminId": "AD-001",
    "name": "Admin Name",
    "email": "admin@example.com",
    "role": "admin",
    "status": "active"
  },
  "role": "admin"
}
```

### 5. Dependencies Added
**File:** `pom.xml`

```xml
<!-- Spring Security -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<!-- JWT (JJWT) Library -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.3</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.3</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.3</version>
    <scope>runtime</scope>
</dependency>
```

## Configuration
**File:** `src/main/resources/application.properties`

```properties
# JWT Configuration
jwt.secret=${JWT_SECRET:MySecretKeyForJWTTokenGenerationAndValidation12345}
jwt.expiration=${JWT_EXPIRATION:86400000}
```

**For Production:**
Set environment variables:
```bash
export JWT_SECRET=your-super-secret-key-min-32-chars
export JWT_EXPIRATION=86400000  # 24 hours in milliseconds
```

## User Roles Supported

### 1. SuperAdmin
- Model: `com.tms.restapi.toolsmanagement.superadmin.model.SuperAdmin`
- ID Type: Long (auto-generated)
- Token Role: `superadmin`

### 2. Admin
- Model: `com.tms.restapi.toolsmanagement.admin.model.Admin`
- ID Type: String (adminId)
- Token Role: `admin`

### 3. Trainer
- Model: `com.tms.restapi.toolsmanagement.trainer.model.Trainer`
- ID Type: Long (auto-generated)
- Token Role: `trainer`

### 4. Security
- Model: `com.tms.restapi.toolsmanagement.security.model.Security`
- ID Type: Long (auto-generated)
- Token Role: `security`

## Usage

### 1. Login Request
```bash
POST /api/auth/login
Content-Type: application/json

{
  "role": "admin",
  "email": "admin@example.com",
  "password": "password123"
}
```

### 2. WebSocket Connection with Token
```javascript
// Client-side (JavaScript)
const token = loginResponse.token;
const socket = new SockJS('/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({
  'Authorization': 'Bearer ' + token
}, (frame) => {
  // Connected
});
```

### 3. Protected API Requests
```bash
GET /api/protected-endpoint
Authorization: Bearer <JWT_TOKEN>
```

## Token Claims
Each JWT token contains:
- **subject** (sub): User ID
- **role**: User role (superadmin, admin, trainer, security)
- **email**: User email
- **iat**: Issued at timestamp
- **exp**: Expiration timestamp

## Token Validation Flow

1. Client sends Authorization header: `Authorization: Bearer <token>`
2. JwtAuthenticationFilter intercepts request
3. Filter extracts token from header
4. JwtTokenProvider validates token signature and expiration
5. If valid, authentication context is set
6. If invalid, request proceeds without authentication (401 for protected endpoints)

## Security Features

✅ **Password Encoding:** BCryptPasswordEncoder used for secure password storage
✅ **CORS Protection:** Configurable CORS for allowed origins
✅ **CSRF Protection:** Disabled for REST API (JWT provides security)
✅ **Token Expiration:** Automatic token expiration after 24 hours
✅ **Signature Validation:** HMAC-SHA256 ensures token integrity
✅ **Stateless:** No session storage required (scalable)

## Troubleshooting

### Token Not Generated
- Check if user exists in database
- Verify password matches
- Ensure JwtTokenProvider is autowired in controller

### WebSocket Connection Fails
- Verify JWT token is valid (not expired)
- Check Authorization header format: `Bearer <token>`
- Ensure token contains required claims (role, email)

### Invalid Token Error
- Token may be expired (24-hour default)
- Token signature may be invalid (different secret key)
- Token may be malformed

### Test the Implementation
```bash
# 1. Build the project
./mvnw clean compile

# 2. Start the application
./mvnw spring-boot:run

# 3. Test login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"role":"admin","email":"admin@example.com","password":"password123"}'

# 4. Use returned token for WebSocket or API requests
```

## File Changes Summary

| File | Change |
|------|--------|
| `pom.xml` | Added Spring Security & JJWT dependencies |
| `application.properties` | Added JWT configuration properties |
| `AuthController.java` | Updated to generate and return JWT tokens |
| `JwtTokenProvider.java` | NEW - JWT token generation & validation |
| `JwtAuthenticationFilter.java` | NEW - Request interceptor for JWT validation |
| `WebSecurityConfig.java` | NEW - Spring Security configuration |

## Next Steps

1. **Update Frontend:** Use the returned `token` from login response
2. **WebSocket Authentication:** Send token in Authorization header when connecting
3. **Test All Roles:** Verify login works for admin, trainer, superadmin, and security
4. **Production Setup:** Change `jwt.secret` to a strong, unique value
5. **Token Refresh:** Consider implementing token refresh endpoint for long sessions

## Token Example
```
Header: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9
Payload: eyJyb2xlIjoiYWRtaW4iLCJlbWFpbCI6ImFkbWluQGV4YW1wbGUuY29tIiwic3ViIjoiQUQtMDAxIiwiaWF0IjoxNjAxNTEzNjAwLCJleHAiOjE2MDE2MDAwMDB9
Signature: [HMAC-SHA256 signature]
```

## References
- [JJWT Documentation](https://github.com/jwtk/jjwt)
- [Spring Security Documentation](https://spring.io/projects/spring-security)
- [JWT.io](https://jwt.io) - JWT token decoder for debugging
