package com.tms.restapi.toolsmanagement.auth.dto;

/**
 * Request payload for password reset OTP generation.
 *
 * Expected JSON fields:
 * - role: user role such as ADMIN, TRAINER, SECURITY, SUPERADMIN
 * - email: registered email address of the user
 */
public class OtpRequest {
    private String role;
    private String email;

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
