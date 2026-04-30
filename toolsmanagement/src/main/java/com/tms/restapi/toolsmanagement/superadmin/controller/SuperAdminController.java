package com.tms.restapi.toolsmanagement.superadmin.controller;

import com.tms.restapi.toolsmanagement.superadmin.model.SuperAdmin;
import com.tms.restapi.toolsmanagement.superadmin.service.SuperAdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * SuperAdmin Controller for managing superadmin user accounts.
 *
 * This controller allows creation and update of SuperAdmin users for high-level
 * system administration and oversight.
 */
@RestController
@RequestMapping("/api/superadmin")
@CrossOrigin("*")
public class SuperAdminController {

    @Autowired
    private SuperAdminService service;

    /**
     * Create a new superadmin user.
     *
     * API Endpoint: POST /api/superadmin/create
     *
     * Request Payload Sample:
     * {
     *   "name": "Sonal Super",
     *   "email": "sonal@example.com",
     *   "password": "SuperStrong123",
     *   "role": "SUPERADMIN"
     * }
     *
     * @param admin SuperAdmin object to create
     * @return Created SuperAdmin user
     */
    @PostMapping("/create")
    public SuperAdmin create(@RequestBody SuperAdmin admin) {
        return service.createSuperAdmin(admin);
    }

    /**
     * Update a superadmin user.
     *
     * API Endpoint: PUT /api/superadmin/update/{id}
     *
     * @param id SuperAdmin identifier
     * @param admin Updated SuperAdmin details
     * @return Updated SuperAdmin user
     */
    @PutMapping("/update/{id}")
    public SuperAdmin update(@PathVariable Long id, @RequestBody SuperAdmin admin) {
        return service.updateSuperAdmin(id, admin);
    }
}
