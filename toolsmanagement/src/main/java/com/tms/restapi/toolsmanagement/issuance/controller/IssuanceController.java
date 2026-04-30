package com.tms.restapi.toolsmanagement.issuance.controller;

import com.tms.restapi.toolsmanagement.issuance.dto.ApprovalRequestDto;
import com.tms.restapi.toolsmanagement.issuance.dto.RejectionRequestDto;
import com.tms.restapi.toolsmanagement.issuance.dto.ReturnRequestDto;
import com.tms.restapi.toolsmanagement.issuance.model.IssuanceRequest;
import com.tms.restapi.toolsmanagement.issuance.model.ReturnRecord;
import com.tms.restapi.toolsmanagement.issuance.model.Issuance;
import com.tms.restapi.toolsmanagement.issuance.service.IssuanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/issuance")
public class IssuanceController {

	/*
	 * Issuance API endpoints
	 * ---------------------
	 * POST   /api/issuance/request                -> create a new issuance request (PENDING status)
	 * GET    /api/issuance/requests/trainer/{trainerId}    -> get issuance requests for a trainer
	 * GET    /api/issuance/requests/location?location={loc}   -> get issuance requests filtered by location
	 * GET    /api/issuance/requests/pending?location={loc}   -> get PENDING issuance requests by location
	 * GET    /api/issuance/requests/all              -> get all issuance requests
	 * POST   /api/issuance/approve                -> approve an issuance request (admin)
	 * POST   /api/issuance/reject                 -> reject an issuance request (admin)
	 * GET    /api/issuance/issued-items           -> get currently issued items (status=ISSUED)
	 * PUT    /api/issuance/process-return         -> process a return for an issuance
	 * GET    /api/issuance/returns                -> get return records (optional query: location, trainerId)
	 */

	@Autowired
	private IssuanceService issuanceService;

	@PostMapping("/request")
	// POST /api/issuance/request - Create a new issuance request (PENDING)
	public ResponseEntity<Issuance> createRequest(@RequestBody Issuance issuance) {
		Issuance created = issuanceService.createIssuanceRequest(issuance);
		return ResponseEntity.ok(created);
	}

	@GetMapping("/requests/trainer/{trainerId}")
	// GET /api/issuance/requests/trainer/{trainerId}
	public ResponseEntity<List<IssuanceRequest>> getRequestsByTrainer(@PathVariable Long trainerId) {
		return ResponseEntity.ok(issuanceService.getIssuanceRequestsByTrainer(trainerId));
	}

	@GetMapping("/requests/location")
	// GET /api/issuance/requests/location?location={location}
	public ResponseEntity<List<IssuanceRequest>> getRequestsByLocation(@RequestParam String location) {
		return ResponseEntity.ok(issuanceService.getAllRequestsByLocation(location));
	}

	@GetMapping("/requests/pending")
	// GET /api/issuance/requests/pending?location={location}
	public ResponseEntity<List<IssuanceRequest>> getPendingRequestsByLocation(@RequestParam String location) {
		return ResponseEntity.ok(issuanceService.getPendingRequestsByLocation(location));
	}

	@GetMapping("/requests/all")
	// GET /api/issuance/requests/all
	public ResponseEntity<List<IssuanceRequest>> getAllIssuanceRequests() {
		return ResponseEntity.ok(issuanceService.getAllIssuanceRequests());
	}

	@PostMapping("/approve")
	// POST /api/issuance/approve - Approve an issuance request
	public ResponseEntity<Issuance> approveIssuanceRequest(@RequestBody ApprovalRequestDto body) {
		if (body.getRequestId() == null) {
			return ResponseEntity.badRequest().build();
		}
		Issuance approved = issuanceService.approveIssuanceRequest(
				body.getRequestId(),
				body.getApprovedBy(),
				body.getApprovalRemark()
		);
		return ResponseEntity.ok(approved);
	}

	@PostMapping("/reject")
	// POST /api/issuance/reject - Reject an issuance request
	public ResponseEntity<String> rejectIssuanceRequest(@RequestBody RejectionRequestDto body) {
		if (body.getRequestId() == null) {
			return ResponseEntity.badRequest().build();
		}
		issuanceService.rejectIssuanceRequest(
				body.getRequestId(),
				body.getRejectedBy(),
				body.getRejectionReason()
		);
		return ResponseEntity.ok("Issuance request rejected successfully");
	}

	@GetMapping("/issued-items")
	// GET /api/issuance/issued-items
	public ResponseEntity<List<Issuance>> getCurrentIssuedItems() {
		return ResponseEntity.ok(issuanceService.getCurrentIssuedItems());
	}

	@PutMapping("/process-return")
	public ResponseEntity<Issuance> processReturn(@RequestBody ReturnRequestDto body) {
		Issuance updated = issuanceService.processReturn(body);
		if (updated == null) return ResponseEntity.notFound().build();
		return ResponseEntity.ok(updated);
	}

    // Return records endpoints
    // GET /api/issuance/returns?location={location}&trainerId={trainerId}
    // - if both provided -> filtered by both
    // - if only location -> filtered by location
    // - if only trainerId -> filtered by trainer
    // - if none -> all return records
    @GetMapping("/returns")
	public ResponseEntity<List<ReturnRecord>> getReturnRecords(
			@RequestParam(required = false) String location,
			@RequestParam(required = false) Long trainerId
	) {
		if (location != null && trainerId != null) {
			return ResponseEntity.ok(issuanceService.getReturnRecordsByLocationAndTrainer(location, trainerId));
		}
		if (location != null) {
			return ResponseEntity.ok(issuanceService.getReturnRecordsByLocation(location));
		}
		if (trainerId != null) {
			return ResponseEntity.ok(issuanceService.getReturnRecordsByTrainer(trainerId));
		}
		return ResponseEntity.ok(issuanceService.getAllReturnRecords());
	}
}