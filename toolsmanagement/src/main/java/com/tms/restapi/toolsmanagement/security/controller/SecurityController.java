package com.tms.restapi.toolsmanagement.security.controller;

import com.tms.restapi.toolsmanagement.security.model.Security;
import com.tms.restapi.toolsmanagement.security.service.SecurityService;
import com.tms.restapi.toolsmanagement.auth.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Security Controller for managing security staff accounts.
 *
 * This controller handles creation, update, deletion, and retrieval of security
 * personnel, and uses email notifications to deliver credentials on account creation.
 */
@RestController
@RequestMapping("/api/security")
@CrossOrigin(origins = "*")
public class SecurityController {

    @Autowired
    private SecurityService securityService;

    @Autowired
    private EmailService emailService;

    /**
     * Create a new security user.
     *
     * API Endpoint: POST /api/security/create
     *
     * Request Payload Sample:
     * {
     *   "name": "Raj Singh",
     *   "email": "raj@example.com",
     *   "password": "Secret123",
     *   "role": "Security",
     *   "location": "Pune"
     * }
     *
     * @param security Security object to create
     * @return ResponseEntity containing created security user (without password)
     */
    @PostMapping("/create")
    public ResponseEntity<?> createSecurity(@RequestBody Security security) {
        try {
            String rawPassword = security.getPassword();
            Security created = securityService.createSecurity(security);

            try {
                emailService.sendCredentials(created.getEmail(), rawPassword == null ? "" : rawPassword, created.getRole() == null ? "Security" : created.getRole());
            } catch (Exception ignored) {
                // Email sending is best-effort
            }

            created.setPassword(null);
            return ResponseEntity.ok(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Update an existing security user.
     *
     * API Endpoint: PUT /api/security/update/{id}
     *
     * @param id Security user identifier
     * @param security Updated security details
     * @return ResponseEntity containing updated security user or error
     */
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateSecurity(@PathVariable Long id, @RequestBody Security security) {
        try {
            Security updated = securityService.updateSecurity(id, security);
            if (updated == null) return ResponseEntity.badRequest().body("Security not found");
            updated.setPassword(null);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Delete a security user.
     *
     * API Endpoint: DELETE /api/security/delete/{id}
     *
     * @param id Security user identifier
     * @return ResponseEntity containing deletion result
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteSecurity(@PathVariable Long id) {
        String message = securityService.deleteSecurity(id);
        if (message.contains("not found")) {
            return ResponseEntity.badRequest().body(message);
        }
        return ResponseEntity.ok(message);
    }

    /**
     * Retrieve all security users.
     *
     * API Endpoint: GET /api/security/all
     *
     * @return ResponseEntity containing list of security users
     */
    @GetMapping("/all")
    public ResponseEntity<List<Security>> getAllSecurity() {
        List<Security> list = securityService.getAllSecurity();
        list.forEach(s -> s.setPassword(null));
        return ResponseEntity.ok(list);
    }

    /**
     * Retrieve a security user by ID.
     *
     * API Endpoint: GET /api/security/{id}
     *
     * @param id Security user identifier
     * @return ResponseEntity containing security user or error
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        Optional<Security> opt = securityService.getSecurityById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.badRequest().body("Security not found");
        }
        Security s = opt.get();
        s.setPassword(null);
        return ResponseEntity.ok(s);
    }

    /**
     * Retrieve all security users by location.
     *
     * API Endpoint: GET /api/security/all/location/{location}
     *
     * @param location Location to filter by
     * @return ResponseEntity containing security users in the location
     */
    @GetMapping("/all/location/{location}")
    public ResponseEntity<List<Security>> getByLocation(@PathVariable String location) {
        List<Security> list = securityService.getSecurityByLocation(location);
        list.forEach(s -> s.setPassword(null));
        return ResponseEntity.ok(list);
    }
}
