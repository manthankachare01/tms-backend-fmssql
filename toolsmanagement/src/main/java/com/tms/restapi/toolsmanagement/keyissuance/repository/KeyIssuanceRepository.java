package com.tms.restapi.toolsmanagement.keyissuance.repository;

import com.tms.restapi.toolsmanagement.keyissuance.model.KeyIssuance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface KeyIssuanceRepository extends JpaRepository<KeyIssuance, Long> {

    Optional<KeyIssuance> findByIssuanceId(String issuanceId);

    List<KeyIssuance> findByLocationIgnoreCase(String location);

    List<KeyIssuance> findByLocationIgnoreCaseAndStatusIgnoreCase(String location, String status);

    List<KeyIssuance> findByStatusIgnoreCase(String status);
}
