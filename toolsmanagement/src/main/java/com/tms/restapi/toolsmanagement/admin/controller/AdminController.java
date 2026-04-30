package com.tms.restapi.toolsmanagement.admin.controller;

import com.tms.restapi.toolsmanagement.admin.model.Admin;
import com.tms.restapi.toolsmanagement.admin.service.AdminService;
import com.tms.restapi.toolsmanagement.auth.service.EmailService;
import com.tms.restapi.toolsmanagement.issuance.dto.ApprovalRequestDto;
import com.tms.restapi.toolsmanagement.issuance.dto.RejectionRequestDto;
import com.tms.restapi.toolsmanagement.issuance.model.Issuance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin Controller for managing admin users and admin-level issuance actions.
 *
 * This controller provides endpoints for creating, retrieving, updating, deleting,
 * and searching admin users. It also exposes admin approval/rejection workflows for
 * issuance requests.
 */
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/admins")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private EmailService emailService;

    /**
     * Create a new admin user.
     *
     * API Endpoint: POST /api/admins/create
     *
     * Request Payload Sample:
     * {
     *   "adminId": "ADM001",
     *   "name": "Alice Admin",
     *   "email": "alice@example.com",
     *   "password": "Password123",
     *   "role": "Admin",
     *   "location": "Pune"
     * }
     *
     * Response Sample: Created admin object without password field
     *
     * @param admin Admin object to create
     * @return ResponseEntity containing created admin user
     */
    @PostMapping("/create")
    public ResponseEntity<Admin> createAdmin(@RequestBody Admin admin) {
        String rawPassword = admin.getPassword();
        Admin created = adminService.createAdmin(admin);

        try {
            emailService.sendCredentials(created.getEmail(), rawPassword == null ? "" : rawPassword, created.getRole());
        } catch (Exception e) {
            // Email delivery is best-effort and should not block admin creation
        }

        created.setPassword(null);
        return ResponseEntity.status(201).body(created);
    }

    /**
     * Retrieve all admin users.
     *
     * API Endpoint: GET /api/admins/all
     *
     * @return ResponseEntity containing list of admins
     */
    @GetMapping("/all")
    public ResponseEntity<List<Admin>> getAllAdmins() {
        return ResponseEntity.ok(adminService.getAllAdmins());
    }

    /**
     * Retrieve a single admin by adminId.
     *
     * API Endpoint: GET /api/admins/{adminId}
     *
     * @param adminId Unique admin identifier
     * @return ResponseEntity containing admin or 404 if not found
     */
    @GetMapping("/{adminId}")
    public ResponseEntity<Admin> getAdminById(@PathVariable String adminId) {
        return adminService.getAdminById(adminId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Update an existing admin. Updatable fields exclude adminId, name, and role.
     *
     * API Endpoint: PUT /api/admins/update/{adminId}
     *
     * @param adminId Admin identifier to update
     * @param adminDetails Admin fields to update
     * @return ResponseEntity containing updated admin or 404 if not found
     */
    @PutMapping("/update/{adminId}")
    public ResponseEntity<Admin> updateAdmin(@PathVariable String adminId, @RequestBody Admin adminDetails) {
        Admin updated = adminService.updateAdmin(adminId, adminDetails);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    /**
     * Search admins by name or email.
     *
     * API Endpoint: GET /api/admins/search?keyword={keyword}
     *
     * @param keyword Search term for name or email
     * @return ResponseEntity containing matching admins
     */
    @GetMapping("/search")
    public ResponseEntity<List<Admin>> searchAdmins(@RequestParam String keyword) {
        return ResponseEntity.ok(adminService.searchAdmins(keyword));
    }

    /**
     * Delete an admin user.
     *
     * API Endpoint: DELETE /api/admins/delete/{adminId}
     *
     * @param adminId Admin identifier to delete
     * @return ResponseEntity containing deletion status message
     */
    @DeleteMapping("/delete/{adminId}")
    public ResponseEntity<String> deleteAdmin(@PathVariable String adminId) {
        return ResponseEntity.ok(adminService.deleteAdmin(adminId));
    }

    /**
     * Approve an issuance request as an admin.
     *
     * API Endpoint: POST /api/admins/issuance/approve
     *
     * Request Payload Sample:
     * {
     *   "requestId": 102,
     *   "approvedBy": "Alice Admin",
     *   "approvalRemark": "Approved for training"
     * }
     *
     * @param body Approval request details
     * @return ResponseEntity containing approved issuance
     */
    @PostMapping("/issuance/approve")
    public ResponseEntity<Issuance> approveIssuanceRequest(@RequestBody ApprovalRequestDto body) {
        if (body.getRequestId() == null || body.getApprovedBy() == null) {
            return ResponseEntity.badRequest().build();
        }
        Issuance approved = adminService.approveIssuanceRequest(
                body.getRequestId(),
                body.getApprovedBy(),
                body.getApprovalRemark()
        );
        return ResponseEntity.ok(approved);
    }

    /**
     * Reject an issuance request as an admin.
     *
     * API Endpoint: POST /api/admins/issuance/reject
     *
     * Request Payload Sample:
     * {
     *   "requestId": 102,
     *   "rejectedBy": "Alice Admin",
     *   "rejectionReason": "Item unavailable"
     * }
     *
     * @param body Rejection request details
     * @return ResponseEntity confirming rejection
     */
    @PostMapping("/issuance/reject")
    public ResponseEntity<String> rejectIssuanceRequest(@RequestBody RejectionRequestDto body) {
        if (body.getRequestId() == null || body.getRejectedBy() == null) {
            return ResponseEntity.badRequest().build();
        }
        adminService.rejectIssuanceRequest(
                body.getRequestId(),
                body.getRejectedBy(),
                body.getRejectionReason()
        );
        return ResponseEntity.ok("Issuance request rejected successfully");
    }
}
