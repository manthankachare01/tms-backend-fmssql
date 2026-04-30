package com.tms.restapi.toolsmanagement.auth.controller;

import com.tms.restapi.toolsmanagement.trainer.model.Trainer;
import com.tms.restapi.toolsmanagement.trainer.repository.TrainerRepository;
import com.tms.restapi.toolsmanagement.superadmin.service.SuperAdminService;
import com.tms.restapi.toolsmanagement.admin.model.Admin;
import com.tms.restapi.toolsmanagement.admin.repository.AdminRepository;
import com.tms.restapi.toolsmanagement.superadmin.model.SuperAdmin;
import com.tms.restapi.toolsmanagement.security.model.Security;
import com.tms.restapi.toolsmanagement.security.repository.SecurityRepository;
import com.tms.restapi.toolsmanagement.auth.jwt.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Authentication Controller for the Tools Management System.
 *
 * This controller handles user authentication and login for different user roles
 * in the system: Admin, Trainer, Security, and SuperAdmin. It provides JWT token
 * generation upon successful authentication and manages role-based access control.
 *
 * The controller uses BCrypt for password hashing and JWT tokens for session management.
 * Passwords are never returned in responses for security reasons.
 */
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private TrainerRepository trainerRepository;

    @Autowired
    private SecurityRepository securityRepository;

    @Autowired
    private SuperAdminService superAdminService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    // Password encoder for secure password verification
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * User Login Endpoint
     *
     * Authenticates users based on their role, email, and password. Supports multiple
     * user roles with role-based authentication logic.
     *
     * API Endpoint: POST /api/auth/login
     *
     * Request Payload Sample:
     * {
     *   "role": "admin",
     *   "email": "admin@example.com",
     *   "password": "password123"
     * }
     *
     * Supported Roles: "admin", "trainer", "security", "superadmin"
     *
     * Response Sample (Success):
     * {
     *   "message": "Admin login successful",
     *   "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
     *   "user": {
     *     "adminId": 1,
     *     "name": "John Doe",
     *     "email": "admin@example.com",
     *     "location": "Main Office"
     *   },
     *   "role": "admin"
     * }
     *
     * Response Sample (Error):
     * {
     *   "message": "Invalid admin credentials"
     * }
     *
     * @param loginData Map containing role, email, and password
     * @return ResponseEntity with authentication result and JWT token
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginData) {
        String role = loginData.get("role");
        String email = loginData.get("email");
        String password = loginData.get("password");

        // Validate required fields
        if (role == null || email == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Missing login fields"));
        }

        Map<String, Object> response = new HashMap<>();

        // Handle Admin login
        if (role.equalsIgnoreCase("admin")) {
            Admin admin = adminRepository.findByEmail(email);

            // Verify credentials
            if (admin == null || !passwordEncoder.matches(password, admin.getPassword())) {
                return ResponseEntity.status(401).body(Map.of("message", "Invalid admin credentials"));
            }

            // Generate JWT token for admin
            String token = jwtTokenProvider.generateToken(admin.getAdminId(), admin.getEmail(), "admin");

            // Remove password from response for security
            admin.setPassword(null);
            response.put("message", "Admin login successful");
            response.put("token", token);
            response.put("user", admin);
            response.put("role", "admin");
            return ResponseEntity.ok(response);
        }

        // Handle Trainer login
        if (role.equalsIgnoreCase("trainer")) {
            Trainer trainer = trainerRepository.findByEmail(email);

            // Verify credentials
            if (trainer == null || !passwordEncoder.matches(password, trainer.getPassword())) {
                return ResponseEntity.status(401).body(Map.of("message", "Invalid trainer credentials"));
            }

            // Generate JWT token for trainer
            String token = jwtTokenProvider.generateToken(String.valueOf(trainer.getId()), trainer.getEmail(), "trainer");

            // Remove password from response for security
            trainer.setPassword(null);
            response.put("message", "Trainer login successful");
            response.put("token", token);
            response.put("user", trainer);
            response.put("role", "trainer");
            return ResponseEntity.ok(response);
        }

        // Handle Security login
        if (role.equalsIgnoreCase("security")) {
            Security security = securityRepository.findByEmail(email);

            // Verify credentials
            if (security == null || !passwordEncoder.matches(password, security.getPassword())) {
                return ResponseEntity.status(401).body(Map.of("message", "Invalid security credentials"));
            }

            // Generate JWT token for security
            String token = jwtTokenProvider.generateToken(String.valueOf(security.getId()), security.getEmail(), "security");

            // Remove password from response for security
            security.setPassword(null);
            response.put("message", "Security login successful");
            response.put("token", token);
            response.put("user", security);
            response.put("role", "security");
            return ResponseEntity.ok(response);
        }

        // Handle SuperAdmin login
        if (role.equalsIgnoreCase("superadmin")) {
            SuperAdmin internal = superAdminService.findInternal(email);

            // Verify credentials
            if (internal == null || !passwordEncoder.matches(password, internal.getPassword())) {
                return ResponseEntity.status(401).body(Map.of("message", "Invalid superadmin credentials"));
            }

            // Generate JWT token for superadmin
            String token = jwtTokenProvider.generateToken(String.valueOf(internal.getId()), internal.getEmail(), "superadmin");

            // Remove password from response for security
            internal.setPassword(null);
            response.put("message", "Superadmin login successful");
            response.put("token", token);
            response.put("user", internal);
            response.put("role", "superadmin");
            return ResponseEntity.ok(response);
        }

        // Invalid role provided
        return ResponseEntity.badRequest().body(Map.of("message", "Invalid role"));
    }
}
