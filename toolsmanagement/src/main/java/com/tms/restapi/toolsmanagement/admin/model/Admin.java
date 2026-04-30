package com.tms.restapi.toolsmanagement.admin.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "admins")
public class Admin {

    @Id
    @Column(name = "admin_id", nullable = false, unique = true)
    private String adminId; // provided by frontend, e.g. "AD-001"

    private String name;

    private String role;    // e.g. "admin" provided by frontend

    private String location;
    private String contact;
    private String status;  // "active" / "inactive"

    private LocalDate dob;
    private LocalDate doj;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password; // stored encoded

    public Admin() {}

    // Getters and setters
    public String getAdminId() { return adminId; }
    public void setAdminId(String adminId) { this.adminId = adminId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getDob() { return dob; }
    public void setDob(LocalDate dob) { this.dob = dob; }

    public LocalDate getDoj() { return doj; }
    public void setDoj(LocalDate doj) { this.doj = doj; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
