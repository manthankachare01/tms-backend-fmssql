package com.tms.restapi.toolsmanagement.auth.dto;

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
