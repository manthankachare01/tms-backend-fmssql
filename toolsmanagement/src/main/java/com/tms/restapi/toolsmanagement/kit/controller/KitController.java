package com.tms.restapi.toolsmanagement.kit.controller;

import com.tms.restapi.toolsmanagement.kit.dto.KitCreateRequest;
import com.tms.restapi.toolsmanagement.kit.dto.KitResponse;
import com.tms.restapi.toolsmanagement.kit.service.KitService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Kit Controller for managing tool kits in the Tools Management System.
 *
 * This controller provides REST endpoints for managing tool kits, which are
 * collections of tools grouped together for specific purposes (e.g., maintenance kits,
 * training kits). Kits can contain multiple tools and track their collective usage,
 * condition, and borrowing history.
 *
 * Kits are associated with locations and can be searched, updated, and tracked
 * throughout their lifecycle.
 */
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/kits")
public class KitController {

    private final KitService kitService;

    public KitController(KitService kitService) {
        this.kitService = kitService;
    }

    /**
     * Create a new tool kit.
     *
     * API Endpoint: POST /api/kits/create?createdBy={creator}
     *
     * Query Parameters:
     * - createdBy: Name of the person creating the kit (optional, defaults to "System")
     *
     * Request Payload Sample:
     * {
     *   "kitNo": "KIT001",
     *   "description": "Basic Maintenance Kit",
     *   "location": "Pune",
     *   "condition": "Excellent",
     *   "tools": [
     *     {
     *       "toolId": 123,
     *       "quantity": 1
     *     },
     *     {
     *       "toolId": 456,
     *       "quantity": 2
     *     }
     *   ],
     *   "remarks": "Contains essential maintenance tools"
     * }
     *
     * Response Sample:
     * {
     *   "id": 1,
     *   "kitNo": "KIT001",
     *   "description": "Basic Maintenance Kit",
     *   "location": "Pune",
     *   "condition": "Excellent",
     *   "tools": [...],
     *   "createdDate": "2024-01-15",
     *   "createdBy": "Admin Name"
     * }
     *
     * @param createdBy Name of the creator
     * @param request Kit creation request with details and tool list
     * @return ResponseEntity containing the created kit
     */
    @PostMapping("/create")
    public ResponseEntity<KitResponse> createKit(
            @RequestParam(defaultValue = "System") String createdBy,
            @RequestBody KitCreateRequest request) {
        KitResponse response = kitService.createKit(request, createdBy);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieve all tool kits.
     *
     * API Endpoint: GET /api/kits/all
     *
     * Response Sample: Array of all kits with their details and associated tools
     *
     * @return ResponseEntity containing list of all kits
     */
    @GetMapping("/all")
    public ResponseEntity<List<KitResponse>> getAllKits() {
        return ResponseEntity.ok(kitService.getAllKits());
    }

    /**
     * Retrieve a specific kit by its ID.
     *
     * API Endpoint: GET /api/kits/{id}
     *
     * Path Parameters:
     * - id: Unique identifier of the kit (required)
     *
     * Response Sample: Kit object with full details and tool list
     *
     * Returns 404 if kit not found.
     *
     * @param id Unique identifier of the kit
     * @return ResponseEntity containing the kit or 404 if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<KitResponse> getKitById(@PathVariable Long id) {
        return kitService.getKitById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Search kits by location and keyword.
     *
     * API Endpoint: GET /api/kits/search?location={location}&keyword={keyword}
     *
     * Query Parameters:
     * - location: Location to search in (required)
     * - keyword: Search term to match against kit description or kit number (required)
     *
     * Response Sample: Array of kits matching both location and keyword criteria
     *
     * @param location Location to filter search
     * @param keyword Search keyword for kit description or kit number
     * @return ResponseEntity containing list of matching kits
     */
    @GetMapping("/search")
    public ResponseEntity<List<KitResponse>> searchKitsByLocationAndKeyword(
            @RequestParam String location,
            @RequestParam String keyword) {
        List<KitResponse> kits = kitService.searchKitsByLocationAndKeyword(location, keyword);
        return ResponseEntity.ok(kits);
    }

    /**
     * Update an existing kit's information.
     *
     * API Endpoint: PUT /api/kits/update/{id}
     *
     * Path Parameters:
     * - id: Unique identifier of the kit to update (required)
     *
     * Request Payload Sample: Same as create endpoint, includes all updatable fields
     *
     * Response Sample: Updated kit object
     *
     * Returns 404 if kit not found.
     *
     * @param id Unique identifier of the kit
     * @param request Updated kit information
     * @return ResponseEntity containing the updated kit or 404 if not found
     */
    @PutMapping("/update/{id}")
    public ResponseEntity<KitResponse> updateKit(@PathVariable Long id,
            @RequestBody KitCreateRequest request) {
        return kitService.updateKit(id, request)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Retrieve kits filtered by location.
     *
     * API Endpoint: GET /api/kits/location/{location}
     *
     * Path Parameters:
     * - location: Location to filter kits by (required)
     *
     * Response Sample: Array of kits in the specified location
     *
     * @param location Location to filter kits
     * @return ResponseEntity containing list of kits in the location
     */
    @GetMapping("/location/{location}")
    public ResponseEntity<List<KitResponse>> getKitsByLocation(@PathVariable String location) {
        List<KitResponse> kits = kitService.getKitsByLocation(location);
        return ResponseEntity.ok(kits);
    }

    /**
     * Delete a kit from the system.
     *
     * API Endpoint: DELETE /api/kits/delete/{id}
     *
     * Path Parameters:
     * - id: Unique identifier of the kit to delete (required)
     *
     * Response Sample (Success):
     * "Kit deleted successfully."
     *
     * Response Sample (Not Found):
     * "Kit not found."
     *
     * @param id Unique identifier of the kit to delete
     * @return ResponseEntity containing success message or 404 if kit not found
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteKit(@PathVariable Long id) {
        boolean deleted = kitService.deleteKit(id);
        if (deleted) {
            return ResponseEntity.ok("Kit deleted successfully.");
        }
        return ResponseEntity.status(404).body("Kit not found.");
    }
}
