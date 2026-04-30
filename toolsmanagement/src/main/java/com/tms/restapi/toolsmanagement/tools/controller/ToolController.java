package com.tms.restapi.toolsmanagement.tools.controller;

import com.tms.restapi.toolsmanagement.tools.model.Tool;
import com.tms.restapi.toolsmanagement.tools.service.ToolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/tools")
public class ToolController {

    @Autowired
    private ToolService toolService;

    // 1) Create tool
    // POST: /api/tools/create?adminLocation=Pune&createdBy=AdminName
    @PostMapping("/create")
    public ResponseEntity<Tool> createTool(
            @RequestParam String adminLocation,
            @RequestParam(defaultValue = "System") String createdBy,
            @RequestBody Tool tool
    ) {
        Tool created = toolService.createTool(tool, adminLocation, createdBy);
        return ResponseEntity.ok(created);
    }

    // 2) Get all tools
    // GET: /api/tools/all
    @GetMapping("/all")
    public ResponseEntity<List<Tool>> getAllTools() {
        List<Tool> tools = toolService.getAllTools();
        return ResponseEntity.ok(tools);
    }

    // 3) Get tools by location
    // GET: /api/tools/by-location?location=Pune
    @GetMapping("/by-location")
    public ResponseEntity<List<Tool>> getToolsByLocation(
            @RequestParam String location
    ) {
        List<Tool> tools = toolService.getToolsByLocation(location);
        return ResponseEntity.ok(tools);
    }

    // 4) Get tool by id
    // GET: /api/tools/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Tool> getToolById(@PathVariable Long id) {
        return toolService.getToolById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 5) Update tool
    // PUT: /api/tools/update/{id}
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

    // 6) Delete tool
    // DELETE: /api/tools/delete/{id}
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Map<String, String>> deleteTool(
            @PathVariable Long id
    ) {
        String msg = toolService.deleteTool(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", msg);
        return ResponseEntity.ok(response);
    }

    // 7) Search tools by description or toolNo
    // GET: /api/tools/search?keyword=spanner
    @GetMapping("/search")
    public ResponseEntity<List<Tool>> searchTools(@RequestParam String keyword) {
        List<Tool> tools = toolService.searchTools(keyword);
        return ResponseEntity.ok(tools);
    }

    // 8) Search tools by keyword within a specific location
    // GET: /api/tools/search-by-location?keyword=spanner&location=Pune
    @GetMapping("/search-by-location")
    public ResponseEntity<List<Tool>> searchToolsByLocation(
            @RequestParam String keyword,
            @RequestParam String location
    ) {
        List<Tool> tools = toolService.searchToolsByLocation(keyword, location);
        return ResponseEntity.ok(tools);
    }
}
