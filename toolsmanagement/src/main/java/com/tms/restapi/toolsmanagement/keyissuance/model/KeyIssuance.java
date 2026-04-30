package com.tms.restapi.toolsmanagement.keyissuance.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

@Entity
@Table(name = "key_issuance")
public class KeyIssuance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;           // DB primary key

    @Column(name = "issuance_id", unique = true, nullable = false)
    private String issuanceId; // e.g. KI-001

    @Column(name = "security_id", nullable = false)
    private String securityId;

    @Column(name = "security_name", nullable = false)
    private String securityName;

    @Column(name = "trainer_id", nullable = false)
    private String trainerId;

    @Column(name = "trainer_name", nullable = false)
    private String trainerName;

    @Column(name = "location", nullable = false)
    private String location;

    // "issued" or "returned"
    @Column(name = "status", nullable = false)
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "date_of_issuance", nullable = false)
    private LocalDateTime dateOfIssuance;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "date_of_return")
    private LocalDateTime dateOfReturn;

    // Getters and setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIssuanceId() {
        return issuanceId;
    }

    public void setIssuanceId(String issuanceId) {
        this.issuanceId = issuanceId;
    }

    public String getSecurityId() {
        return securityId;
    }

    public void setSecurityId(String securityId) {
        this.securityId = securityId;
    }

    public String getSecurityName() {
        return securityName;
    }

    public void setSecurityName(String securityName) {
        this.securityName = securityName;
    }

    public String getTrainerId() {
        return trainerId;
    }

    public void setTrainerId(String trainerId) {
        this.trainerId = trainerId;
    }

    public String getTrainerName() {
        return trainerName;
    }

    public void setTrainerName(String trainerName) {
        this.trainerName = trainerName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getDateOfIssuance() {
        return dateOfIssuance;
    }

    public void setDateOfIssuance(LocalDateTime dateOfIssuance) {
        this.dateOfIssuance = dateOfIssuance;
    }

    public LocalDateTime getDateOfReturn() {
        return dateOfReturn;
    }

    public void setDateOfReturn(LocalDateTime dateOfReturn) {
        this.dateOfReturn = dateOfReturn;
    }
}
