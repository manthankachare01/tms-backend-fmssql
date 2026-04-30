package com.tms.restapi.toolsmanagement.issuance.repository;

import com.tms.restapi.toolsmanagement.issuance.model.IssuanceRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IssuanceRequestRepository extends JpaRepository<IssuanceRequest, Long> {
    List<IssuanceRequest> findByLocation(String location);
    List<IssuanceRequest> findByTrainerId(Long trainerId);
    List<IssuanceRequest> findByStatus(String status);
    List<IssuanceRequest> findByLocationAndStatus(String location, String status);
}
