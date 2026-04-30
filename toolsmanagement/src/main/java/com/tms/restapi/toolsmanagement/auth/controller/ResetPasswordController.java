package com.tms.restapi.toolsmanagement.auth.controller;

import com.tms.restapi.toolsmanagement.trainer.service.TrainerService;
import com.tms.restapi.toolsmanagement.superadmin.service.SuperAdminService;
import com.tms.restapi.toolsmanagement.admin.service.AdminService;
import com.tms.restapi.toolsmanagement.security.service.SecurityService;
import com.tms.restapi.toolsmanagement.auth.dto.ResetPasswordRequest;
import com.tms.restapi.toolsmanagement.auth.dto.OtpRequest;
import com.tms.restapi.toolsmanagement.auth.dto.OtpVerifyRequest;
import com.tms.restapi.toolsmanagement.auth.service.OtpService;
import com.tms.restapi.toolsmanagement.auth.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Reset Password Controller for password recovery workflows.
 *
 * Supports requesting OTP, verifying OTP, and resetting passwords for
 * ADMIN, TRAINER, SECURITY, and SUPERADMIN roles.
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class ResetPasswordController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private TrainerService trainerService;

    @Autowired
    private SuperAdminService superAdminService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private OtpService otpService;

    @Autowired
    private EmailService emailService;

    /**
     * Reset password after OTP verification.
     *
     * API Endpoint: POST /api/auth/reset-password
     *
     * Request Payload Sample:
     * {
     *   "role": "TRAINER",
     *   "email": "ravi@example.com",
     *   "newPassword": "NewPass123",
     *   "confirmPassword": "NewPass123"
     * }
     *
     * @param request ResetPasswordRequest containing role, email, and new password
     * @return ResponseEntity with status message
     */
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {

        if (request.getNewPassword() == null || request.getConfirmPassword() == null
                || !request.getNewPassword().equals(request.getConfirmPassword())) {
            return ResponseEntity.badRequest().body("New password and confirm password do not match");
        }

        if (request.getRole() == null || request.getEmail() == null) {
            return ResponseEntity.badRequest().body("Role and email are required");
        }

        String role = request.getRole().trim().toUpperCase();

        if (!otpService.isVerified(role, request.getEmail())) {
            return ResponseEntity.badRequest().body("OTP not verified or expired. Please verify OTP before resetting password.");
        }

        try {
            switch (role) {
                case "ADMIN":
                    adminService.resetPassword(request.getEmail(), request.getNewPassword());
                    break;
                case "TRAINER":
                    trainerService.resetPassword(request.getEmail(), request.getNewPassword());
                    break;
                case "SECURITY":
                    securityService.resetPassword(request.getEmail(), request.getNewPassword());
                    break;
                case "SUPERADMIN":
                    superAdminService.resetPassword(request.getEmail(), request.getNewPassword());
                    break;
                default:
                    return ResponseEntity.badRequest().body("Invalid role. Use ADMIN, TRAINER, SECURITY or SUPERADMIN");
            }
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

        otpService.clear(role, request.getEmail());
        return ResponseEntity.ok("Password reset successful");
    }

    /**
     * Request OTP for password reset.
     *
     * API Endpoint: POST /api/auth/request-reset-otp
     *
     * Request Payload Sample:
     * {
     *   "role": "SECURITY",
     *   "email": "raj@example.com"
     * }
     *
     * @param request OtpRequest containing role and email
     * @return ResponseEntity with status message
     */
    @PostMapping("/request-reset-otp")
    public ResponseEntity<?> requestResetOtp(@RequestBody OtpRequest request) {
        if (request.getRole() == null || request.getEmail() == null) {
            return ResponseEntity.badRequest().body("Role and email are required");
        }

        String role = request.getRole().trim().toUpperCase();

        boolean exists;
        switch (role) {
            case "ADMIN":
                exists = adminService.findByEmail(request.getEmail()) != null;
                break;
            case "TRAINER":
                exists = trainerService.findByEmail(request.getEmail()) != null;
                break;
            case "SECURITY":
                exists = securityService.findByEmail(request.getEmail()) != null;
                break;
            case "SUPERADMIN":
                exists = superAdminService.findInternal(request.getEmail()) != null;
                break;
            default:
                return ResponseEntity.badRequest().body("Invalid role. Use ADMIN, TRAINER, SECURITY or SUPERADMIN");
        }

        if (!exists) return ResponseEntity.badRequest().body("No user found with given email for role");

        String otp = otpService.generateOtp(role, request.getEmail());

        try {
            emailService.sendOtp(request.getEmail(), otp, role);
        } catch (RuntimeException e) {
            return ResponseEntity.ok("OTP (dev): " + otp + ". Mail sending failed: " + e.getMessage());
        }

        return ResponseEntity.ok("OTP sent to email if mail configured");
    }

    /**
     * Verify the OTP sent for password reset.
     *
     * API Endpoint: POST /api/auth/verify-reset-otp
     *
     * Request Payload Sample:
     * {
     *   "role": "ADMIN",
     *   "email": "alice@example.com",
     *   "otp": "123456"
     * }
     *
     * @param request OtpVerifyRequest containing role, email, and OTP
     * @return ResponseEntity with verification status
     */
    @PostMapping("/verify-reset-otp")
    public ResponseEntity<?> verifyResetOtp(@RequestBody OtpVerifyRequest request) {
        if (request.getRole() == null || request.getEmail() == null || request.getOtp() == null) {
            return ResponseEntity.badRequest().body("Role, email and otp are required");
        }

        String role = request.getRole().trim().toUpperCase();
        boolean ok = otpService.verifyOtp(role, request.getEmail(), request.getOtp());
        if (!ok) return ResponseEntity.badRequest().body("Invalid or expired OTP");
        return ResponseEntity.ok("OTP verified");
    }
}
