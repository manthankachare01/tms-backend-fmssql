package com.tms.restapi.toolsmanagement.superadmin.repository;

import com.tms.restapi.toolsmanagement.superadmin.model.SuperAdmin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SuperAdminRepository extends JpaRepository<SuperAdmin, Long> {
    Optional<SuperAdmin> findByEmail(String email);
}
