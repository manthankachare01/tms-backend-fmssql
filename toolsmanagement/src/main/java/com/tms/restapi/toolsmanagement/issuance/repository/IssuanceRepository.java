package com.tms.restapi.toolsmanagement.issuance.repository;

import com.tms.restapi.toolsmanagement.issuance.model.Issuance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IssuanceRepository extends JpaRepository<Issuance, Long> {
    List<Issuance> findByTrainerId(Long trainerId);
    List<Issuance> findByLocation(String location);
    List<Issuance> findByStatus(String status);
}