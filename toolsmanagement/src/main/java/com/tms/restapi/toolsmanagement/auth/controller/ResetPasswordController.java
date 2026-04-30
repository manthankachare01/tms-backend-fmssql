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

        // Ensure OTP was verified for this email+role
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

        // clear OTP after successful reset
        otpService.clear(role, request.getEmail());

        return ResponseEntity.ok("Password reset successful");
    }

    @PostMapping("/request-reset-otp")
    public ResponseEntity<?> requestResetOtp(@RequestBody OtpRequest request) {
        if (request.getRole() == null || request.getEmail() == null) {
            return ResponseEntity.badRequest().body("Role and email are required");
        }

        String role = request.getRole().trim().toUpperCase();

        // confirm user exists for role
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
            // If mail not configured, return success with OTP in body for development
            return ResponseEntity.ok("OTP (dev): " + otp + ". Mail sending failed: " + e.getMessage());
        }

        return ResponseEntity.ok("OTP sent to email if mail configured");
    }

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
