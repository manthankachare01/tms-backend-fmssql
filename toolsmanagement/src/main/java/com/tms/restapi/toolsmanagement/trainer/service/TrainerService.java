package com.tms.restapi.toolsmanagement.trainer.service;

import com.tms.restapi.toolsmanagement.trainer.model.Trainer;
import com.tms.restapi.toolsmanagement.trainer.repository.TrainerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TrainerService {

    @Autowired
    private TrainerRepository trainerRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // create trainer with location from adminLocation and counters set to 0
    public Trainer createTrainer(Trainer trainer, String adminLocation) {
        trainer.setId(null); // ensure a new entity

        // force location from admin
        trainer.setLocation(adminLocation);

        // encode password
        trainer.setPassword(passwordEncoder.encode(trainer.getPassword()));

        // initial counters
        trainer.setToolsIssued(0);
        trainer.setToolsReturned(0);
        trainer.setActiveIssuance(0);
        trainer.setOverdueIssuance(0);

        return trainerRepository.save(trainer);
    }

    public List<Trainer> getAllTrainers() {
        return trainerRepository.findAll();
    }

    public List<Trainer> getAllTrainersByLocation(String location) {
        return trainerRepository.findByLocation(location);
    }

    public Optional<Trainer> getTrainerById(Long id) {
        return trainerRepository.findById(id);
    }

    // update using id
    public Trainer updateTrainer(Long id, Trainer trainerDetails) {
        Optional<Trainer> existingOpt = trainerRepository.findById(id);
        if (existingOpt.isEmpty()) {
            return null;
        }

        Trainer trainer = existingOpt.get();

        trainer.setName(trainerDetails.getName());
        trainer.setEmail(trainerDetails.getEmail());
        trainer.setContact(trainerDetails.getContact());
        trainer.setRole(trainerDetails.getRole());
        trainer.setStatus(trainerDetails.getStatus());
        trainer.setDob(trainerDetails.getDob());
        trainer.setDoj(trainerDetails.getDoj());

        // update counters from backend or frontend as per your logic
        trainer.setToolsIssued(trainerDetails.getToolsIssued());
        trainer.setToolsReturned(trainerDetails.getToolsReturned());
        trainer.setActiveIssuance(trainerDetails.getActiveIssuance());
        trainer.setOverdueIssuance(trainerDetails.getOverdueIssuance());

        // usually location should not change from frontend
        // trainer.setLocation(...) only from admin if needed

        return trainerRepository.save(trainer);
    }

    public List<Trainer> searchTrainers(String keyword) {
        return trainerRepository
                .findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(keyword, keyword);
    }

    public Trainer findByEmail(String email) {
        return trainerRepository.findByEmail(email);
    }

    public boolean verifyPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    public String deleteTrainer(Long id) {
        if (!trainerRepository.existsById(id)) {
            return "Trainer not found.";
        }
        trainerRepository.deleteById(id);
        return "Trainer deleted successfully.";
    }

    public void resetPassword(String email, String newPassword) {
        Trainer trainer = trainerRepository.findByEmail(email);
        if (trainer == null) {
            throw new RuntimeException("Trainer not found with this email");
        }

        trainer.setPassword(passwordEncoder.encode(newPassword));
        trainerRepository.save(trainer);
    }
}
