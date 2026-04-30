package com.tms.restapi.toolsmanagement.keyissuance.service.impl;

import com.tms.restapi.toolsmanagement.keyissuance.dto.KeyIssuanceRequest;
import com.tms.restapi.toolsmanagement.keyissuance.model.KeyIssuance;
import com.tms.restapi.toolsmanagement.keyissuance.repository.KeyIssuanceRepository;
import com.tms.restapi.toolsmanagement.keyissuance.service.KeyIssuanceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class KeyIssuanceServiceImpl implements KeyIssuanceService {

    private final KeyIssuanceRepository keyIssuanceRepository;

    public KeyIssuanceServiceImpl(KeyIssuanceRepository keyIssuanceRepository) {
        this.keyIssuanceRepository = keyIssuanceRepository;
    }

    @Override
    public KeyIssuance createIssuance(KeyIssuanceRequest request) {
        KeyIssuance issuance = new KeyIssuance();

        // Generate simple issuanceId like KI-001, KI-002, ...
        long count = keyIssuanceRepository.count() + 1;
        String issuanceId = String.format("KI-%03d", count);

        issuance.setIssuanceId(issuanceId);
        issuance.setSecurityId(request.getSecurityId());
        issuance.setSecurityName(request.getSecurityName());
        issuance.setTrainerId(request.getTrainerId());
        issuance.setTrainerName(request.getTrainerName());
        issuance.setLocation(request.getLocation());

        issuance.setStatus("issued");
        issuance.setDateOfIssuance(LocalDateTime.now());
        issuance.setDateOfReturn(null);

        return keyIssuanceRepository.save(issuance);
    }

    @Override
    public List<KeyIssuance> getAllIssuances() {
        return keyIssuanceRepository.findAll();
    }

    @Override
    public List<KeyIssuance> getIssuancesByLocation(String location) {
        return keyIssuanceRepository.findByLocationIgnoreCase(location);
    }

    @Override
    public List<KeyIssuance> getIssuancesByLocationAndStatus(String location, String status) {
        return keyIssuanceRepository.findByLocationIgnoreCaseAndStatusIgnoreCase(location, status);
    }

    @Override
    public List<KeyIssuance> getIssuancesByStatus(String status) {
        return keyIssuanceRepository.findByStatusIgnoreCase(status);
    }

    @Override
    public KeyIssuance returnKey(String issuanceId) {
        KeyIssuance issuance = keyIssuanceRepository.findByIssuanceId(issuanceId)
                .orElseThrow(() -> new RuntimeException("Issuance not found with id: " + issuanceId));

        issuance.setStatus("returned");
        issuance.setDateOfReturn(LocalDateTime.now());

        return keyIssuanceRepository.save(issuance);
    }
}
