package com.tms.restapi.toolsmanagement.trainer.repository;

import com.tms.restapi.toolsmanagement.trainer.model.Trainer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrainerRepository extends JpaRepository<Trainer, Long> {

    Trainer findByEmail(String email);

    List<Trainer> findByLocation(String location);

    List<Trainer> findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
            String name,
            String email
    );
}
