package com.tms.restapi.toolsmanagement.admin.service;

import com.tms.restapi.toolsmanagement.admin.model.Admin;
import com.tms.restapi.toolsmanagement.admin.repository.AdminRepository;
import com.tms.restapi.toolsmanagement.issuance.model.Issuance;
import com.tms.restapi.toolsmanagement.issuance.service.IssuanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AdminService {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired(required = false)
    private IssuanceService issuanceService;

    // keep same pattern as TrainerService
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // Create admin
    public Admin createAdmin(Admin admin) {
        if (admin.getAdminId() == null || admin.getAdminId().isBlank()) {
            throw new IllegalArgumentException("adminId is required and must be provided by frontend");
        }
        if (adminRepository.existsById(admin.getAdminId())) {
            throw new IllegalArgumentException("Admin with id " + admin.getAdminId() + " already exists");
        }
        if (admin.getEmail() == null || admin.getEmail().isBlank()) {
            throw new IllegalArgumentException("email is required");
        }
        if (adminRepository.existsByEmail(admin.getEmail())) {
            throw new IllegalArgumentException("Email already in use");
        }

        if (admin.getPassword() != null && !admin.getPassword().isBlank()) {
            admin.setPassword(passwordEncoder.encode(admin.getPassword()));
        }

        return adminRepository.save(admin);
    }

    // Get all admins
    public List<Admin> getAllAdmins() {
        return adminRepository.findAll();
    }

    // Get admin by adminId
    public Optional<Admin> getAdminById(String adminId) {
        return adminRepository.findById(adminId);
    }

    // Update admin (name, adminId, role are NOT updatable)
    public Admin updateAdmin(String adminId, Admin incoming) {
        return adminRepository.findById(adminId).map(existing -> {
            if (incoming.getLocation() != null) existing.setLocation(incoming.getLocation());
            if (incoming.getContact() != null) existing.setContact(incoming.getContact());
            if (incoming.getStatus() != null) existing.setStatus(incoming.getStatus());
            if (incoming.getDob() != null) existing.setDob(incoming.getDob());
            if (incoming.getDoj() != null) existing.setDoj(incoming.getDoj());

            if (incoming.getEmail() != null && !incoming.getEmail().equals(existing.getEmail())) {
                if (adminRepository.existsByEmail(incoming.getEmail())) {
                    throw new IllegalArgumentException("Email already in use");
                }
                existing.setEmail(incoming.getEmail());
            }

            if (incoming.getPassword() != null && !incoming.getPassword().isBlank()) {
                existing.setPassword(passwordEncoder.encode(incoming.getPassword()));
            }

            return adminRepository.save(existing);
        }).orElse(null);
    }

    // Search by name or email
    public List<Admin> searchAdmins(String keyword) {
        return adminRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(keyword, keyword);
    }

    // Delete admin
    public String deleteAdmin(String adminId) {
        if (adminRepository.existsById(adminId)) {
            adminRepository.deleteById(adminId);
            return "Admin deleted successfully.";
        } else {
            return "Admin not found.";
        }
    }

    // --- Reset password (added method) ---
    // Called by ResetPasswordController: adminService.resetPassword(email, newPassword)
    public void resetPassword(String email, String newPassword) {
        Admin admin = adminRepository.findByEmail(email);
        if (admin == null) {
            throw new RuntimeException("Admin not found with this email");
        }
        admin.setPassword(passwordEncoder.encode(newPassword));
        adminRepository.save(admin);
    }

    // helper to find admin by email
    public Admin findByEmail(String email) {
        return adminRepository.findByEmail(email);
    }

    /**
     * Approve an issuance request - delegates to IssuanceService
     */
    public Issuance approveIssuanceRequest(Long requestId, String approvedBy, String approvalRemark) {
        if (issuanceService == null) {
            throw new RuntimeException("IssuanceService not available");
        }
        return issuanceService.approveIssuanceRequest(requestId, approvedBy, approvalRemark);
    }

    /**
     * Reject an issuance request - delegates to IssuanceService
     */
    public void rejectIssuanceRequest(Long requestId, String rejectedBy, String rejectionReason) {
        if (issuanceService == null) {
            throw new RuntimeException("IssuanceService not available");
        }
        issuanceService.rejectIssuanceRequest(requestId, rejectedBy, rejectionReason);
    }
}

