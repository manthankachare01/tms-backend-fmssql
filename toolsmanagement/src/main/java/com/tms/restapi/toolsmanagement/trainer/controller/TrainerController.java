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

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/trainers")
public class TrainerController {

    @Autowired
    private TrainerService trainerService;

    @Autowired
    private EmailService emailService;

    // 1) Create trainer (location from adminLocation, not from form)
    // POST: /api/trainers/create?adminLocation=Pune
    @PostMapping("/create")
    public ResponseEntity<Trainer> createTrainer(
            @RequestParam String adminLocation,
            @RequestBody Trainer trainer
    ) {
        // capture raw password before encoding
        String rawPassword = trainer.getPassword();
        Trainer created = trainerService.createTrainer(trainer, adminLocation);

        // send credentials email (best-effort)
        try { emailService.sendCredentials(created.getEmail(), rawPassword == null ? "" : rawPassword, created.getRole() == null ? "Trainer" : created.getRole()); } catch (Exception ignored) {}

        // do not send password back
        created.setPassword(null);
        return ResponseEntity.ok(created);
    }

    // 2) Get all trainers
    // GET: /api/trainers/all
    @GetMapping("/all")
    public ResponseEntity<List<Trainer>> getAllTrainers() {
        List<Trainer> trainers = trainerService.getAllTrainers();
        trainers.forEach(t -> t.setPassword(null));
        return ResponseEntity.ok(trainers);
    }

    // 3) Get trainers by location
    // GET: /api/trainers/by-location?location=Pune
    @GetMapping("/by-location")
    public ResponseEntity<List<Trainer>> getTrainersByLocation(
            @RequestParam String location
    ) {
        List<Trainer> trainers = trainerService.getAllTrainersByLocation(location);
        trainers.forEach(t -> t.setPassword(null));
        return ResponseEntity.ok(trainers);
    }

    // 4) Get trainer by id
    // GET: /api/trainers/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Trainer> getTrainerById(@PathVariable Long id) {
        return trainerService.getTrainerById(id)
                .map(trainer -> {
                    trainer.setPassword(null);
                    return ResponseEntity.ok(trainer);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // 5) Update trainer by id
    // PUT: /api/trainers/update/{id}
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

    // 6) Delete trainer by id
    // DELETE: /api/trainers/delete/{id}
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Map<String, String>> deleteTrainer(
            @PathVariable Long id
    ) {
        String msg = trainerService.deleteTrainer(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", msg);
        return ResponseEntity.ok(response);
    }

    // optional: search
    // GET: /api/trainers/search?keyword=rahul
    @GetMapping("/search")
    public ResponseEntity<List<Trainer>> searchTrainers(@RequestParam String keyword) {
        List<Trainer> trainers = trainerService.searchTrainers(keyword);
        trainers.forEach(t -> t.setPassword(null));
        return ResponseEntity.ok(trainers);
    }
}
