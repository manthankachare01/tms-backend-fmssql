package com.tms.restapi.toolsmanagement.keyissuance.controller;

import com.tms.restapi.toolsmanagement.keyissuance.dto.KeyIssuanceRequest;
import com.tms.restapi.toolsmanagement.keyissuance.model.KeyIssuance;
import com.tms.restapi.toolsmanagement.keyissuance.service.KeyIssuanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Key Issuance Controller for managing key and access card issuance.
 *
 * This controller handles the lifecycle of key issuance records, including
 * creation, retrieval, filtering by location/status, and return processing.
 * It supports separate workflows from tool and kit issuance.
 */
@RestController
@RequestMapping("/api/key-issuance")
@CrossOrigin(origins = "*")
public class KeyIssuanceController {

    private final KeyIssuanceService keyIssuanceService;

    public KeyIssuanceController(KeyIssuanceService keyIssuanceService) {
        this.keyIssuanceService = keyIssuanceService;
    }

    /**
     * Create a new key issuance record.
     *
     * API Endpoint: POST /api/key-issuance/create
     *
     * Request Payload Sample:
     * {
     *   "keyId": "KEY001",
     *   "issuedTo": "John Doe",
     *   "location": "Pune",
     *   "issuedBy": "Admin",
     *   "issueDate": "2024-01-10",
     *   "status": "ISSUED"
     * }
     *
     * @param request KeyIssuanceRequest containing issuance details
     * @return ResponseEntity containing created key issuance record
     */
    @PostMapping("/create")
    public ResponseEntity<KeyIssuance> createIssuance(@RequestBody KeyIssuanceRequest request) {
        KeyIssuance created = keyIssuanceService.createIssuance(request);
        return ResponseEntity.ok(created);
    }

    /**
     * Retrieve all key issuance records.
     *
     * API Endpoint: GET /api/key-issuance/all
     *
     * @return ResponseEntity containing list of key issuance records
     */
    @GetMapping("/all")
    public ResponseEntity<List<KeyIssuance>> getAllIssuances() {
        return ResponseEntity.ok(keyIssuanceService.getAllIssuances());
    }

    /**
     * Retrieve issuance records for a specific location.
     *
     * API Endpoint: GET /api/key-issuance/location/{location}
     *
     * @param location Location to filter by
     * @return ResponseEntity containing issuance records for the location
     */
    @GetMapping("/location/{location}")
    public ResponseEntity<List<KeyIssuance>> getByLocation(@PathVariable String location) {
        return ResponseEntity.ok(keyIssuanceService.getIssuancesByLocation(location));
    }

    /**
     * Retrieve issuance records by location and status.
     *
     * API Endpoint: GET /api/key-issuance/location/{location}/status/{status}
     *
     * @param location Location to filter by
     * @param status Issuance status to filter by (ISSUED/RETURNED)
     * @return ResponseEntity containing matching issuance records
     */
    @GetMapping("/location/{location}/status/{status}")
    public ResponseEntity<List<KeyIssuance>> getByLocationAndStatus(@PathVariable String location,
                                                                    @PathVariable String status) {
        return ResponseEntity.ok(
                keyIssuanceService.getIssuancesByLocationAndStatus(location, status)
        );
    }

    /**
     * Retrieve issuance records by status only.
     *
     * API Endpoint: GET /api/key-issuance/status/{status}
     *
     * @param status Issuance status to filter by (ISSUED/RETURNED)
     * @return ResponseEntity containing matching issuance records
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<KeyIssuance>> getByStatus(@PathVariable String status) {
        return ResponseEntity.ok(keyIssuanceService.getIssuancesByStatus(status));
    }

    /**
     * Mark a key as returned.
     *
     * API Endpoint: POST /api/key-issuance/{issuanceId}/return
     *
     * @param issuanceId Issuance identifier to mark as returned
     * @return ResponseEntity containing updated key issuance record
     */
    @PostMapping("/{issuanceId}/return")
    public ResponseEntity<KeyIssuance> returnKey(@PathVariable String issuanceId) {
        KeyIssuance updated = keyIssuanceService.returnKey(issuanceId);
        return ResponseEntity.ok(updated);
    }
}
