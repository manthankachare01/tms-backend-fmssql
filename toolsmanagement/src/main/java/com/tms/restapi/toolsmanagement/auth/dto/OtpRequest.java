package com.tms.restapi.toolsmanagement.auth.dto;

public class OtpRequest {
    private String role;
    private String email;

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
