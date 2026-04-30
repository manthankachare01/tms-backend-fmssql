package com.tms.restapi.toolsmanagement.admin.repository;

import com.tms.restapi.toolsmanagement.admin.model.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdminRepository extends JpaRepository<Admin, String> {
    Admin findByEmail(String email);
    List<Admin> findByLocation(String location);
    List<Admin> findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(String name, String email);
    boolean existsByEmail(String email);
}
