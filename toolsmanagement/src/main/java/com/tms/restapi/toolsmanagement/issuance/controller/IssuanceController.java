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

/**
 * Issuance Controller for managing tool and kit issuance requests in the Tools Management System.
 *
 * This controller handles the complete lifecycle of tool/kit issuance:
 * 1. Creating issuance requests (pending status)
 * 2. Admin approval/rejection workflow
 * 3. Tracking issued items
 * 4. Processing returns with condition updates
 *
 * The issuance process involves trainers requesting tools/kits, admins approving/rejecting
 * requests, and tracking the return process with timestamps and condition remarks.
 */
@RestController
@RequestMapping("/api/issuance")
public class IssuanceController {

	/**
	 * Issuance API Endpoints Overview:
	 * -------------------------------
	 * POST   /api/issuance/request                -> Create new issuance request (PENDING status)
	 * GET    /api/issuance/requests/trainer/{id}  -> Get requests for specific trainer
	 * GET    /api/issuance/requests/location      -> Get requests filtered by location
	 * GET    /api/issuance/requests/pending       -> Get PENDING requests by location
	 * GET    /api/issuance/requests/all           -> Get all issuance requests
	 * POST   /api/issuance/approve                -> Approve issuance request (admin only)
	 * POST   /api/issuance/reject                 -> Reject issuance request (admin only)
	 * GET    /api/issuance/issued-items           -> Get currently issued items (ISSUED status)
	 * PUT    /api/issuance/process-return         -> Process return for an issuance
	 * GET    /api/issuance/returns                -> Get return records with optional filters
	 */

	@Autowired
	private IssuanceService issuanceService;

	/**
	 * Create a new issuance request.
	 *
	 * API Endpoint: POST /api/issuance/request
	 *
	 * This endpoint allows trainers to request tools or kits for issuance.
	 * The request is created with PENDING status and requires admin approval.
	 *
	 * Request Payload Sample:
	 * {
	 *   "trainerId": 123,
	 *   "trainerName": "John Doe",
	 *   "location": "Pune",
	 *   "requestDate": "2024-01-15",
	 *   "items": [
	 *     {
	 *       "toolId": 456,
	 *       "toolNo": "T001",
	 *       "description": "Adjustable Spanner",
	 *       "quantity": 2,
	 *       "condition": "Good"
	 *     }
	 *   ],
	 *   "purpose": "Training session",
	 *   "expectedReturnDate": "2024-01-20"
	 * }
	 *
	 * Response Sample: Created issuance request object with generated ID and PENDING status
	 *
	 * @param issuance Issuance request object containing trainer and item details
	 * @return ResponseEntity containing the created issuance request
	 */
	@PostMapping("/request")
	public ResponseEntity<Issuance> createRequest(@RequestBody Issuance issuance) {
		Issuance created = issuanceService.createIssuanceRequest(issuance);
		return ResponseEntity.ok(created);
	}

	/**
	 * Get issuance requests for a specific trainer.
	 *
	 * API Endpoint: GET /api/issuance/requests/trainer/{trainerId}
	 *
	 * Path Parameters:
	 * - trainerId: Unique identifier of the trainer (required)
	 *
	 * Response Sample: Array of issuance requests for the specified trainer
	 *
	 * @param trainerId Unique identifier of the trainer
	 * @return ResponseEntity containing list of issuance requests for the trainer
	 */
	@GetMapping("/requests/trainer/{trainerId}")
	public ResponseEntity<List<IssuanceRequest>> getRequestsByTrainer(@PathVariable Long trainerId) {
		return ResponseEntity.ok(issuanceService.getIssuanceRequestsByTrainer(trainerId));
	}

	/**
	 * Get issuance requests filtered by location.
	 *
	 * API Endpoint: GET /api/issuance/requests/location?location={location}
	 *
	 * Query Parameters:
	 * - location: Location to filter requests by (required)
	 *
	 * Response Sample: Array of issuance requests for the specified location
	 *
	 * @param location Location to filter issuance requests
	 * @return ResponseEntity containing list of issuance requests in the location
	 */
	@GetMapping("/requests/location")
	public ResponseEntity<List<IssuanceRequest>> getRequestsByLocation(@RequestParam String location) {
		return ResponseEntity.ok(issuanceService.getAllRequestsByLocation(location));
	}

	/**
	 * Get pending issuance requests by location.
	 *
	 * API Endpoint: GET /api/issuance/requests/pending?location={location}
	 *
	 * Query Parameters:
	 * - location: Location to filter pending requests (required)
	 *
	 * Response Sample: Array of pending issuance requests requiring admin approval
	 *
	 * @param location Location to filter pending requests
	 * @return ResponseEntity containing list of pending issuance requests
	 */
	@GetMapping("/requests/pending")
	public ResponseEntity<List<IssuanceRequest>> getPendingRequestsByLocation(@RequestParam String location) {
		return ResponseEntity.ok(issuanceService.getPendingRequestsByLocation(location));
	}

	/**
	 * Get all issuance requests.
	 *
	 * API Endpoint: GET /api/issuance/requests/all
	 *
	 * Response Sample: Array of all issuance requests across all locations and statuses
	 *
	 * @return ResponseEntity containing list of all issuance requests
	 */
	@GetMapping("/requests/all")
	public ResponseEntity<List<IssuanceRequest>> getAllIssuanceRequests() {
		return ResponseEntity.ok(issuanceService.getAllIssuanceRequests());
	}

	/**
	 * Approve an issuance request.
	 *
	 * API Endpoint: POST /api/issuance/approve
	 *
	 * This endpoint allows admins to approve pending issuance requests.
	 * Upon approval, the request status changes to APPROVED and items are marked as issued.
	 *
	 * Request Payload Sample:
	 * {
	 *   "requestId": 789,
	 *   "approvedBy": "Admin Name",
	 *   "approvalRemark": "Approved for training session"
	 * }
	 *
	 * Response Sample: Updated issuance object with APPROVED status
	 *
	 * @param body Approval request containing request ID and approval details
	 * @return ResponseEntity containing the approved issuance
	 */
	@PostMapping("/approve")
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

	/**
	 * Reject an issuance request.
	 *
	 * API Endpoint: POST /api/issuance/reject
	 *
	 * This endpoint allows admins to reject pending issuance requests.
	 * The request status changes to REJECTED with rejection reason.
	 *
	 * Request Payload Sample:
	 * {
	 *   "requestId": 789,
	 *   "rejectedBy": "Admin Name",
	 *   "rejectionReason": "Items not available"
	 * }
	 *
	 * Response Sample:
	 * "Issuance request rejected successfully"
	 *
	 * @param body Rejection request containing request ID and rejection details
	 * @return ResponseEntity containing success message
	 */
	@PostMapping("/reject")
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

	/**
	 * Get currently issued items.
	 *
	 * API Endpoint: GET /api/issuance/issued-items
	 *
	 * Returns all items that are currently issued (status = ISSUED).
	 * These are items that have been approved and are out with trainers.
	 *
	 * Response Sample: Array of currently issued items with trainer and return details
	 *
	 * @return ResponseEntity containing list of currently issued items
	 */
	@GetMapping("/issued-items")
	public ResponseEntity<List<Issuance>> getCurrentIssuedItems() {
		return ResponseEntity.ok(issuanceService.getCurrentIssuedItems());
	}

	/**
	 * Process return for an issued item.
	 *
	 * API Endpoint: PUT /api/issuance/process-return
	 *
	 * This endpoint handles the return process when trainers return tools/kits.
	 * Updates the issuance status to RETURNED and records return condition and remarks.
	 *
	 * Request Payload Sample:
	 * {
	 *   "issuanceId": 123,
	 *   "returnDate": "2024-01-20",
	 *   "returnedBy": "John Doe",
	 *   "returnCondition": "Good",
	 *   "returnRemarks": "Returned in good condition",
	 *   "items": [
	 *     {
	 *       "toolId": 456,
	 *       "returnedQuantity": 2,
	 *       "condition": "Good",
	 *       "remarks": "No damage"
	 *     }
	 *   ]
	 * }
	 *
	 * Response Sample: Updated issuance object with RETURNED status
	 *
	 * @param body Return request containing return details and item conditions
	 * @return ResponseEntity containing the updated issuance or 404 if not found
	 */
	@PutMapping("/process-return")
	public ResponseEntity<Issuance> processReturn(@RequestBody ReturnRequestDto body) {
		Issuance updated = issuanceService.processReturn(body);
		if (updated == null) return ResponseEntity.notFound().build();
		return ResponseEntity.ok(updated);
	}

	/**
	 * Get return records with optional filtering.
	 *
	 * API Endpoint: GET /api/issuance/returns?location={location}&trainerId={trainerId}
	 *
	 * Query Parameters (all optional):
	 * - location: Filter by location
	 * - trainerId: Filter by trainer ID
	 *
	 * Filtering Logic:
	 * - Both location and trainerId: Filter by both criteria
	 * - Only location: Filter by location only
	 * - Only trainerId: Filter by trainer only
	 * - Neither: Return all return records
	 *
	 * Response Sample: Array of return records matching the filter criteria
	 *
	 * @param location Optional location filter
	 * @param trainerId Optional trainer ID filter
	 * @return ResponseEntity containing list of return records
	 */
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