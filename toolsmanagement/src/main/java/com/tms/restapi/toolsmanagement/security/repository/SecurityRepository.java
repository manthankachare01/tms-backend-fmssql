package com.tms.restapi.toolsmanagement.security.repository;

import com.tms.restapi.toolsmanagement.security.model.Security;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SecurityRepository extends JpaRepository<Security, Long> {

    boolean existsByEmail(String email);

    Security findByEmail(String email);

    List<Security> findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(String nameKeyword, String emailKeyword);

    List<Security> findByLocation(String location);
}
