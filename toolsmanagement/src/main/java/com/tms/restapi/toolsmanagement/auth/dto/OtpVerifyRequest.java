package com.tms.restapi.toolsmanagement.auth.dto;

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
