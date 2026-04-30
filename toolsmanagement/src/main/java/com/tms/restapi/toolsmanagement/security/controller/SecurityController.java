package com.tms.restapi.toolsmanagement.security.controller;

import com.tms.restapi.toolsmanagement.security.model.Security;
import com.tms.restapi.toolsmanagement.security.service.SecurityService;
import com.tms.restapi.toolsmanagement.auth.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/security")
@CrossOrigin(origins = "*")
public class SecurityController {

    @Autowired
    private SecurityService securityService;

    @Autowired
    private EmailService emailService;

    @PostMapping("/create")
    public ResponseEntity<?> createSecurity(@RequestBody Security security) {
        try {
            String rawPassword = security.getPassword();
            Security created = securityService.createSecurity(security);

            // send credentials (best-effort)
            try { emailService.sendCredentials(created.getEmail(), rawPassword == null ? "" : rawPassword, created.getRole() == null ? "Security" : created.getRole()); } catch (Exception ignored) {}

            // do not return password to client
            created.setPassword(null);
            return ResponseEntity.ok(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

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

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteSecurity(@PathVariable Long id) {
        String message = securityService.deleteSecurity(id);
        if (message.contains("not found")) {
            return ResponseEntity.badRequest().body(message);
        }
        return ResponseEntity.ok(message);
    }

    @GetMapping("/all")
    public ResponseEntity<List<Security>> getAllSecurity() {
        List<Security> list = securityService.getAllSecurity();
        // remove passwords before sending
        list.forEach(s -> s.setPassword(null));
        return ResponseEntity.ok(list);
    }

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

    @GetMapping("/all/location/{location}")
    public ResponseEntity<List<Security>> getByLocation(@PathVariable String location) {
        List<Security> list = securityService.getSecurityByLocation(location);
        list.forEach(s -> s.setPassword(null));
        return ResponseEntity.ok(list);
    }
}
