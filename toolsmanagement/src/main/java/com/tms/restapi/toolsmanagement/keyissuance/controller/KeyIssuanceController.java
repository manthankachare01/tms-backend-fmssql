package com.tms.restapi.toolsmanagement.keyissuance.controller;

import com.tms.restapi.toolsmanagement.keyissuance.dto.KeyIssuanceRequest;
import com.tms.restapi.toolsmanagement.keyissuance.model.KeyIssuance;
import com.tms.restapi.toolsmanagement.keyissuance.service.KeyIssuanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/key-issuance")
@CrossOrigin(origins = "*")
public class KeyIssuanceController {

    private final KeyIssuanceService keyIssuanceService;

    public KeyIssuanceController(KeyIssuanceService keyIssuanceService) {
        this.keyIssuanceService = keyIssuanceService;
    }

    // 1) Create issuance (Issued)
    @PostMapping("/create")
    public ResponseEntity<KeyIssuance> createIssuance(@RequestBody KeyIssuanceRequest request) {
        KeyIssuance created = keyIssuanceService.createIssuance(request);
        return ResponseEntity.ok(created);
    }

    // 2) Get all issuance details
    @GetMapping("/all")
    public ResponseEntity<List<KeyIssuance>> getAllIssuances() {
        return ResponseEntity.ok(keyIssuanceService.getAllIssuances());
    }

    // 3) Get all issuance details by location
    @GetMapping("/location/{location}")
    public ResponseEntity<List<KeyIssuance>> getByLocation(@PathVariable String location) {
        return ResponseEntity.ok(keyIssuanceService.getIssuancesByLocation(location));
    }

    // 4) Get all issuance details by location and status (issued/returned)
    @GetMapping("/location/{location}/status/{status}")
    public ResponseEntity<List<KeyIssuance>> getByLocationAndStatus(@PathVariable String location,
                                                                    @PathVariable String status) {
        return ResponseEntity.ok(
                keyIssuanceService.getIssuancesByLocationAndStatus(location, status)
        );
    }

    // 5) Get issuance details by status only (all issued or all returned)
    @GetMapping("/status/{status}")
    public ResponseEntity<List<KeyIssuance>> getByStatus(@PathVariable String status) {
        return ResponseEntity.ok(keyIssuanceService.getIssuancesByStatus(status));
    }

    // 6) Return key (update status to returned and set return date)
    @PostMapping("/{issuanceId}/return")
    public ResponseEntity<KeyIssuance> returnKey(@PathVariable String issuanceId) {
        KeyIssuance updated = keyIssuanceService.returnKey(issuanceId);
        return ResponseEntity.ok(updated);
    }
}
