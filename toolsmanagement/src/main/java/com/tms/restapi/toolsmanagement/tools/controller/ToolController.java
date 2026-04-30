package com.tms.restapi.toolsmanagement.tools.controller;

import com.tms.restapi.toolsmanagement.tools.model.Tool;
import com.tms.restapi.toolsmanagement.tools.service.ToolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tool Controller for managing tool inventory in the Tools Management System.
 *
 * This controller provides REST endpoints for performing CRUD operations on tools,
 * including creation, retrieval, updating, deletion, and searching. It handles
 * tool inventory management with location-based filtering and search capabilities.
 *
 * All endpoints require appropriate authentication and authorization based on user roles.
 */
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/tools")
public class ToolController {

    @Autowired
    private ToolService toolService;

    /**
     * Create a new tool in the inventory.
     *
     * API Endpoint: POST /api/tools/create?adminLocation={location}&createdBy={creator}
     *
     * Query Parameters:
     * - adminLocation: Location where the admin is creating the tool (required)
     * - createdBy: Name of the admin creating the tool (optional, defaults to "System")
     *
     * Request Payload Sample:
     * {
     *   "toolNo": "T001",
     *   "description": "Adjustable Spanner 12\"",
     *   "siNo": "SI123456",
     *   "location": "Pune",
     *   "quantity": 10,
     *   "availableQuantity": 10,
     *   "condition": "Good",
     *   "calibrationDate": "2024-01-15",
     *   "calibrationPeriod": 12,
     *   "nextCalibrationDate": "2025-01-15",
     *   "remarks": "New tool added to inventory"
     * }
     *
     * Response Sample:
     * {
     *   "id": 1,
     *   "toolNo": "T001",
     *   "description": "Adjustable Spanner 12\"",
     *   ... (all tool fields)
     * }
     *
     * @param adminLocation Location of the admin creating the tool
     * @param createdBy Name of the creator (defaults to "System")
     * @param tool Tool object with details to be created
     * @return ResponseEntity containing the created tool
     */
    @PostMapping("/create")
    public ResponseEntity<Tool> createTool(
            @RequestParam String adminLocation,
            @RequestParam(defaultValue = "System") String createdBy,
            @RequestBody Tool tool
    ) {
        Tool created = toolService.createTool(tool, adminLocation, createdBy);
        return ResponseEntity.ok(created);
    }

    /**
     * Retrieve all tools in the inventory.
     *
     * API Endpoint: GET /api/tools/all
     *
     * Response Sample:
     * [
     *   {
     *     "id": 1,
     *     "toolNo": "T001",
     *     "description": "Adjustable Spanner 12\"",
     *     "location": "Pune",
     *     "quantity": 10,
     *     "availableQuantity": 8,
     *     ...
     *   },
     *   ... (more tools)
     * ]
     *
     * @return ResponseEntity containing list of all tools
     */
    @GetMapping("/all")
    public ResponseEntity<List<Tool>> getAllTools() {
        List<Tool> tools = toolService.getAllTools();
        return ResponseEntity.ok(tools);
    }

    /**
     * Retrieve tools filtered by location.
     *
     * API Endpoint: GET /api/tools/by-location?location={location}
     *
     * Query Parameters:
     * - location: Location to filter tools by (required)
     *
     * Response Sample: Same as /all endpoint but filtered by location
     *
     * @param location Location to filter tools
     * @return ResponseEntity containing list of tools in the specified location
     */
    @GetMapping("/by-location")
    public ResponseEntity<List<Tool>> getToolsByLocation(
            @RequestParam String location
    ) {
        List<Tool> tools = toolService.getToolsByLocation(location);
        return ResponseEntity.ok(tools);
    }

    /**
     * Retrieve a specific tool by its ID.
     *
     * API Endpoint: GET /api/tools/{id}
     *
     * Path Parameters:
     * - id: Unique identifier of the tool (required)
     *
     * Response Sample:
     * {
     *   "id": 1,
     *   "toolNo": "T001",
     *   "description": "Adjustable Spanner 12\"",
     *   ... (all tool fields)
     * }
     *
     * Returns 404 if tool not found.
     *
     * @param id Unique identifier of the tool
     * @return ResponseEntity containing the tool or 404 if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<Tool> getToolById(@PathVariable Long id) {
        return toolService.getToolById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Update an existing tool's information.
     *
     * API Endpoint: PUT /api/tools/update/{id}
     *
     * Path Parameters:
     * - id: Unique identifier of the tool to update (required)
     *
     * Request Payload Sample: Same as create endpoint, but only include fields to update
     *
     * Response Sample: Updated tool object
     *
     * Returns 404 if tool not found.
     *
     * @param id Unique identifier of the tool
     * @param toolDetails Updated tool information
     * @return ResponseEntity containing the updated tool or 404 if not found
     */
    @PutMapping("/update/{id}")
    public ResponseEntity<Tool> updateTool(
            @PathVariable Long id,
            @RequestBody Tool toolDetails
    ) {
        Tool updated = toolService.updateTool(id, toolDetails);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }

    /**
     * Delete a tool from the inventory.
     *
     * API Endpoint: DELETE /api/tools/delete/{id}
     *
     * Path Parameters:
     * - id: Unique identifier of the tool to delete (required)
     *
     * Response Sample:
     * {
     *   "message": "Tool deleted successfully"
     * }
     *
     * @param id Unique identifier of the tool to delete
     * @return ResponseEntity containing success message
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Map<String, String>> deleteTool(
            @PathVariable Long id
    ) {
        String msg = toolService.deleteTool(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", msg);
        return ResponseEntity.ok(response);
    }

    /**
     * Search tools by keyword in description or tool number.
     *
     * API Endpoint: GET /api/tools/search?keyword={keyword}
     *
     * Query Parameters:
     * - keyword: Search term to match against tool description or tool number (required)
     *
     * Response Sample: Array of tools matching the search criteria
     *
     * @param keyword Search keyword for tool description or tool number
     * @return ResponseEntity containing list of matching tools
     */
    @GetMapping("/search")
    public ResponseEntity<List<Tool>> searchTools(@RequestParam String keyword) {
        List<Tool> tools = toolService.searchTools(keyword);
        return ResponseEntity.ok(tools);
    }

    /**
     * Search tools by keyword within a specific location.
     *
     * API Endpoint: GET /api/tools/search-by-location?keyword={keyword}&location={location}
     *
     * Query Parameters:
     * - keyword: Search term to match against tool description or tool number (required)
     * - location: Location to filter the search results (required)
     *
     * Response Sample: Array of tools matching both keyword and location criteria
     *
     * @param keyword Search keyword for tool description or tool number
     * @param location Location to filter search results
     * @return ResponseEntity containing list of matching tools in the specified location
     */
    @GetMapping("/search-by-location")
    public ResponseEntity<List<Tool>> searchToolsByLocation(
            @RequestParam String keyword,
            @RequestParam String location
    ) {
        List<Tool> tools = toolService.searchToolsByLocation(keyword, location);
        return ResponseEntity.ok(tools);
    }
}
