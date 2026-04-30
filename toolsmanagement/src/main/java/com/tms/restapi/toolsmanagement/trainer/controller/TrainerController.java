package com.tms.restapi.toolsmanagement.trainer.controller;

import com.tms.restapi.toolsmanagement.trainer.model.Trainer;
import com.tms.restapi.toolsmanagement.trainer.service.TrainerService;
import com.tms.restapi.toolsmanagement.auth.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Trainer Controller for managing trainer user accounts.
 *
 * This controller handles trainer creation, retrieval, updates, deletion,
 * and search operations. It also sends credentials via email when a trainer
 * is created.
 */
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/trainers")
public class TrainerController {

    @Autowired
    private TrainerService trainerService;

    @Autowired
    private EmailService emailService;

    /**
     * Create a new trainer.
     *
     * API Endpoint: POST /api/trainers/create?adminLocation={location}
     *
     * Request Payload Sample:
     * {
     *   "name": "Ravi Kumar",
     *   "email": "ravi@example.com",
     *   "password": "Password123",
     *   "role": "Trainer"
     * }
     *
     * Response Sample: Created trainer object without password field
     *
     * @param adminLocation Location assigned by admin
     * @param trainer Trainer object to create
     * @return ResponseEntity containing created trainer
     */
    @PostMapping("/create")
    public ResponseEntity<Trainer> createTrainer(
            @RequestParam String adminLocation,
            @RequestBody Trainer trainer
    ) {
        String rawPassword = trainer.getPassword();
        Trainer created = trainerService.createTrainer(trainer, adminLocation);

        try {
            emailService.sendCredentials(created.getEmail(), rawPassword == null ? "" : rawPassword, created.getRole() == null ? "Trainer" : created.getRole());
        } catch (Exception ignored) {
            // Email sending is best-effort and should not block trainer creation
        }

        created.setPassword(null);
        return ResponseEntity.ok(created);
    }

    /**
     * Retrieve all trainers.
     *
     * API Endpoint: GET /api/trainers/all
     *
     * @return ResponseEntity containing list of trainers
     */
    @GetMapping("/all")
    public ResponseEntity<List<Trainer>> getAllTrainers() {
        List<Trainer> trainers = trainerService.getAllTrainers();
        trainers.forEach(t -> t.setPassword(null));
        return ResponseEntity.ok(trainers);
    }

    /**
     * Retrieve trainers by location.
     *
     * API Endpoint: GET /api/trainers/by-location?location={location}
     *
     * @param location Location filter
     * @return ResponseEntity containing trainers at the location
     */
    @GetMapping("/by-location")
    public ResponseEntity<List<Trainer>> getTrainersByLocation(
            @RequestParam String location
    ) {
        List<Trainer> trainers = trainerService.getAllTrainersByLocation(location);
        trainers.forEach(t -> t.setPassword(null));
        return ResponseEntity.ok(trainers);
    }

    /**
     * Retrieve a trainer by ID.
     *
     * API Endpoint: GET /api/trainers/{id}
     *
     * @param id Trainer identifier
     * @return ResponseEntity containing trainer or 404 if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<Trainer> getTrainerById(@PathVariable Long id) {
        return trainerService.getTrainerById(id)
                .map(trainer -> {
                    trainer.setPassword(null);
                    return ResponseEntity.ok(trainer);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Update a trainer's information.
     *
     * API Endpoint: PUT /api/trainers/update/{id}
     *
     * @param id Trainer identifier
     * @param trainerDetails Updated trainer details
     * @return ResponseEntity containing updated trainer or 404 if not found
     */
    @PutMapping("/update/{id}")
    public ResponseEntity<Trainer> updateTrainer(
            @PathVariable Long id,
            @RequestBody Trainer trainerDetails
    ) {
        Trainer updated = trainerService.updateTrainer(id, trainerDetails);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        updated.setPassword(null);
        return ResponseEntity.ok(updated);
    }

    /**
     * Delete a trainer account.
     *
     * API Endpoint: DELETE /api/trainers/delete/{id}
     *
     * @param id Trainer identifier
     * @return ResponseEntity containing deletion message
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Map<String, String>> deleteTrainer(
            @PathVariable Long id
    ) {
        String msg = trainerService.deleteTrainer(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", msg);
        return ResponseEntity.ok(response);
    }

    /**
     * Search trainers by name or email.
     *
     * API Endpoint: GET /api/trainers/search?keyword={keyword}
     *
     * @param keyword Search term
     * @return ResponseEntity containing matching trainers
     */
    @GetMapping("/search")
    public ResponseEntity<List<Trainer>> searchTrainers(@RequestParam String keyword) {
        List<Trainer> trainers = trainerService.searchTrainers(keyword);
        trainers.forEach(t -> t.setPassword(null));
        return ResponseEntity.ok(trainers);
    }
}
