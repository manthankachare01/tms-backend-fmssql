package com.tms.restapi.toolsmanagement.superadmin.controller;

import com.tms.restapi.toolsmanagement.superadmin.model.SuperAdmin;
import com.tms.restapi.toolsmanagement.superadmin.service.SuperAdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/superadmin")
@CrossOrigin("*")
public class SuperAdminController {

    @Autowired
    private SuperAdminService service;

    @PostMapping("/create")
    public SuperAdmin create(@RequestBody SuperAdmin admin) {
        return service.createSuperAdmin(admin);
    }

    @PutMapping("/update/{id}")
    public SuperAdmin update(@PathVariable Long id, @RequestBody SuperAdmin admin) {
        return service.updateSuperAdmin(id, admin);
    }
}
