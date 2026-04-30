package com.tms.restapi.toolsmanagement.kit.controller;

import com.tms.restapi.toolsmanagement.kit.dto.KitCreateRequest;
import com.tms.restapi.toolsmanagement.kit.dto.KitResponse;
import com.tms.restapi.toolsmanagement.kit.service.KitService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/kits")
public class KitController {

    private final KitService kitService;

    public KitController(KitService kitService) {
        this.kitService = kitService;
    }

    // POST http://localhost:8080/api/kits/create?createdBy=AdminName
    @PostMapping("/create")
    public ResponseEntity<KitResponse> createKit(
            @RequestParam(defaultValue = "System") String createdBy,
            @RequestBody KitCreateRequest request) {
        KitResponse response = kitService.createKit(request, createdBy);
        return ResponseEntity.ok(response);
    }

    // GET http://localhost:8080/api/kits/all
    @GetMapping("/all")
    public ResponseEntity<List<KitResponse>> getAllKits() {
        return ResponseEntity.ok(kitService.getAllKits());
    }

    // GET http://localhost:8080/api/kits/{id}
    @GetMapping("/{id}")
    public ResponseEntity<KitResponse> getKitById(@PathVariable Long id) {
        return kitService.getKitById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET http://localhost:8080/api/kits/search?location={location}&keyword={keyword}
    @GetMapping("/search")
    public ResponseEntity<List<KitResponse>> searchKitsByLocationAndKeyword(
            @RequestParam String location,
            @RequestParam String keyword) {
        List<KitResponse> kits = kitService.searchKitsByLocationAndKeyword(location, keyword);
        return ResponseEntity.ok(kits);
    }

    // PUT http://localhost:8080/api/kits/update/{id}
    @PutMapping("/update/{id}")
    public ResponseEntity<KitResponse> updateKit(@PathVariable Long id,
            @RequestBody KitCreateRequest request) {
        return kitService.updateKit(id, request)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET http://localhost:8080/api/kits/location/{location}
    @GetMapping("/location/{location}")
    public ResponseEntity<List<KitResponse>> getKitsByLocation(@PathVariable String location) {
        List<KitResponse> kits = kitService.getKitsByLocation(location);
        return ResponseEntity.ok(kits);
    }

    // DELETE http://localhost:8080/api/kits/delete/{id}
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteKit(@PathVariable Long id) {
        boolean deleted = kitService.deleteKit(id);
        if (deleted) {
            return ResponseEntity.ok("Kit deleted successfully.");
        }
        return ResponseEntity.status(404).body("Kit not found.");
    }
}
