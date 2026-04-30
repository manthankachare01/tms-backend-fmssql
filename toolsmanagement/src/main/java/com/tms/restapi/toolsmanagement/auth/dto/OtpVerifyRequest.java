package com.tms.restapi.toolsmanagement.auth.dto;

/**
 * Request payload for OTP verification during password reset.
 *
 * Expected JSON fields:
 * - role: user role such as ADMIN, TRAINER, SECURITY, SUPERADMIN
 * - email: registered email address of the user
 * - otp: numeric one-time password sent to the user email
 */
public class OtpVerifyRequest {
    private String role;
    private String email;
    private String otp;

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getOtp() { return otp; }
    public void setOtp(String otp) { this.otp = otp; }
}
