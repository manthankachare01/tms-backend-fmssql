package com.tms.restapi.toolsmanagement.auth.dto;

/**
 * Request payload for resetting a user password after successful OTP verification.
 *
 * Expected JSON fields:
 * - role: ADMIN, TRAINER, SECURITY, or SUPERADMIN
 * - email: registered email address
 * - newPassword: new password value
 * - confirmPassword: confirmation of the new password
 */
public class ResetPasswordRequest {

    private String role;           // e.g. "ADMIN" or "TRAINER"
    private String email;
    private String newPassword;
    private String confirmPassword;

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }

    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
}
