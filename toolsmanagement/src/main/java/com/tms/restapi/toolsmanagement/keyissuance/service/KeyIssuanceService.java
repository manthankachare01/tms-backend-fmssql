package com.tms.restapi.toolsmanagement.keyissuance.service;

import com.tms.restapi.toolsmanagement.keyissuance.dto.KeyIssuanceRequest;
import com.tms.restapi.toolsmanagement.keyissuance.model.KeyIssuance;

import java.util.List;

public interface KeyIssuanceService {

    KeyIssuance createIssuance(KeyIssuanceRequest request);

    List<KeyIssuance> getAllIssuances();

    List<KeyIssuance> getIssuancesByLocation(String location);

    List<KeyIssuance> getIssuancesByLocationAndStatus(String location, String status);

    List<KeyIssuance> getIssuancesByStatus(String status);

    KeyIssuance returnKey(String issuanceId);
}
