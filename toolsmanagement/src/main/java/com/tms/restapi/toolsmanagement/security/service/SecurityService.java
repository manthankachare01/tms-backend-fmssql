package com.tms.restapi.toolsmanagement.security.service;

import com.tms.restapi.toolsmanagement.security.model.Security;
import com.tms.restapi.toolsmanagement.security.repository.SecurityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SecurityService {

    @Autowired
    private SecurityRepository securityRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // Create security
    public Security createSecurity(Security security) {
        if (security.getEmail() == null || security.getEmail().isBlank()) {
            throw new IllegalArgumentException("email is required");
        }
        if (securityRepository.existsByEmail(security.getEmail())) {
            throw new IllegalArgumentException("Email already in use");
        }

        if (security.getPassword() != null && !security.getPassword().isBlank()) {
            security.setPassword(passwordEncoder.encode(security.getPassword()));
        }

        // location should be provided by frontend (admin's location). If not provided, it's saved null.
        return securityRepository.save(security);
    }

    // Get all security users
    public List<Security> getAllSecurity() {
        return securityRepository.findAll();
    }

    // Get security by id
    public Optional<Security> getSecurityById(Long id) {
        return securityRepository.findById(id);
    }

    // Update security (id and name are NOT updatable)
    public Security updateSecurity(Long id, Security incoming) {
        return securityRepository.findById(id).map(existing -> {
            if (incoming.getLocation() != null) existing.setLocation(incoming.getLocation());
            if (incoming.getContact() != null) existing.setContact(incoming.getContact());
            if (incoming.getStatus() != null) existing.setStatus(incoming.getStatus());
            if (incoming.getDob() != null) existing.setDob(incoming.getDob());
            if (incoming.getDoj() != null) existing.setDoj(incoming.getDoj());
            if (incoming.getRole() != null) existing.setRole(incoming.getRole()); // optional

            if (incoming.getEmail() != null && !incoming.getEmail().equals(existing.getEmail())) {
                if (securityRepository.existsByEmail(incoming.getEmail())) {
                    throw new IllegalArgumentException("Email already in use");
                }
                existing.setEmail(incoming.getEmail());
            }

            if (incoming.getPassword() != null && !incoming.getPassword().isBlank()) {
                existing.setPassword(passwordEncoder.encode(incoming.getPassword()));
            }

            return securityRepository.save(existing);
        }).orElse(null);
    }

    // Search by name or email
    public List<Security> searchSecurity(String keyword) {
        return securityRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(keyword, keyword);
    }

    // Delete security
    public String deleteSecurity(Long id) {
        if (securityRepository.existsById(id)) {
            securityRepository.deleteById(id);
            return "Security deleted successfully.";
        } else {
            return "Security not found.";
        }
    }

    // Reset password (called by ResetPasswordController)
    public void resetPassword(String email, String newPassword) {
        Security security = securityRepository.findByEmail(email);
        if (security == null) {
            throw new RuntimeException("Security not found with this email");
        }
        security.setPassword(passwordEncoder.encode(newPassword));
        securityRepository.save(security);
    }

    // Find by email helper (used by AuthController)
    public Security findByEmail(String email) {
        return securityRepository.findByEmail(email);
    }

    // Find by location
    public List<Security> getSecurityByLocation(String location) {
        return securityRepository.findByLocation(location);
    }
}
