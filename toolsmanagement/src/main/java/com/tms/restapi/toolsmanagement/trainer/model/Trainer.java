package com.tms.restapi.toolsmanagement.trainer.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "trainers")
public class Trainer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;   // auto-generated and unique

    private String name;
    private String role;
    private String contact;
    private String status;
    private LocalDate dob;
    private LocalDate doj;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    private String password;

    // location will be forced from adminLocation, not from form
    private String location;

    // counters / stats
    private int toolsIssued;
    private int toolsReturned;
    private int activeIssuance;
    private int overdueIssuance;

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getDob() {
        return dob;
    }

    public void setDob(LocalDate dob) {
        this.dob = dob;
    }

    public LocalDate getDoj() {
        return doj;
    }

    public void setDoj(LocalDate doj) {
        this.doj = doj;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getToolsIssued() {
        return toolsIssued;
    }

    public void setToolsIssued(int toolsIssued) {
        this.toolsIssued = toolsIssued;
    }

    public int getToolsReturned() {
        return toolsReturned;
    }

    public void setToolsReturned(int toolsReturned) {
        this.toolsReturned = toolsReturned;
    }

    public int getActiveIssuance() {
        return activeIssuance;
    }

    public void setActiveIssuance(int activeIssuance) {
        this.activeIssuance = activeIssuance;
    }

    public int getOverdueIssuance() {
        return overdueIssuance;
    }

    public void setOverdueIssuance(int overdueIssuance) {
        this.overdueIssuance = overdueIssuance;
    }
}
