package com.tms.restapi.toolsmanagement.trainer.controller;

import com.tms.restapi.toolsmanagement.admin.dto.AdminDashboardResponse;
import com.tms.restapi.toolsmanagement.trainer.service.TrainerDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

@RestController
@RequestMapping("/api/trainers/dashboard")
@CrossOrigin(origins = "*")
public class TrainerDashboardController {

    @Autowired
    private TrainerDashboardService dashboardService;

    // GET /api/trainers/dashboard?trainerId=123
    @GetMapping
    public ResponseEntity<AdminDashboardResponse> getDashboard(@RequestParam Long trainerId) {
        AdminDashboardResponse resp = dashboardService.getDashboardForTrainer(trainerId);
        return ResponseEntity.ok(resp);
    }
}
