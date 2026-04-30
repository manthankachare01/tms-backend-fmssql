package com.tms.restapi.toolsmanagement.superadmin.service;

import com.tms.restapi.toolsmanagement.superadmin.model.SuperAdmin;
import com.tms.restapi.toolsmanagement.superadmin.repository.SuperAdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class SuperAdminService {

    @Autowired
    private SuperAdminRepository repository;

    @Autowired(required = false)
    private com.tms.restapi.toolsmanagement.auth.service.EmailService emailService;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public SuperAdmin createSuperAdmin(SuperAdmin admin) {
        admin.setPassword(encoder.encode(admin.getPassword()));
        admin.setRole("SUPERADMIN");
        return repository.save(admin);
    }

    public SuperAdmin updateSuperAdmin(Long id, SuperAdmin data) {
        SuperAdmin saved = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Superadmin not found"));

        String rawPassword = data.getPassword();
        boolean emailChanged = false;
        boolean passwordChanged = false;

        if (data.getEmail() != null && !data.getEmail().equals(saved.getEmail())) {
            saved.setEmail(data.getEmail());
            emailChanged = true;
        }
        if (data.getName() != null) saved.setName(data.getName());
        if (rawPassword != null) {
            saved.setPassword(encoder.encode(rawPassword));
            passwordChanged = true;
        }

        SuperAdmin out = repository.save(saved);

        // Notify SuperAdmin at new email if credentials changed
        if ((emailChanged || passwordChanged) && emailService != null) {
            try {
                // send to the saved (possibly new) email; include plain password only if it was changed
                emailService.sendSuperAdminUpdated(out.getEmail(), out.getName(), passwordChanged ? rawPassword : null);
            } catch (Exception e) {
                // best-effort - do not fail update
            }
        }

        return out;
    }

    public SuperAdmin findInternal(String email) {
        return repository.findByEmail(email).orElse(null);
    }

    public void resetPassword(String email, String newPassword) {
        SuperAdmin admin = repository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Superadmin not found"));

        admin.setPassword(encoder.encode(newPassword));
        repository.save(admin);
    }
}
