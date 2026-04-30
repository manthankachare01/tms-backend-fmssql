package com.tms.restapi.toolsmanagement.notification;

import com.tms.restapi.toolsmanagement.tools.model.Tool;
import com.tms.restapi.toolsmanagement.tools.repository.ToolRepository;
import com.tms.restapi.toolsmanagement.admin.repository.AdminRepository;
import com.tms.restapi.toolsmanagement.admin.model.Admin;
import com.tms.restapi.toolsmanagement.auth.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class CalibrationNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(CalibrationNotificationService.class);

    @Autowired
    private ToolRepository toolRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private EmailService emailService;

    // Run once a day at 08:00
    @Scheduled(cron = "0 0 8 * * *")
    public void dailyCalibrationCheck() {
        logger.info("Running daily calibration notification check");
        LocalDate today = LocalDate.now();

        List<Tool> all = toolRepository.findAll();
        for (Tool t : all) {
            if (!t.isCalibrationRequired()) continue;
            LocalDate next = t.getNextCalibrationDate();
            if (next == null) continue;

            long days = ChronoUnit.DAYS.between(today, next);
            if (days == 30 || days == 7 || days == 2 || days == 1) {
                String location = t.getLocation();
                List<Admin> admins = adminRepository.findByLocation(location);
                if (admins == null || admins.isEmpty()) {
                    logger.warn("No admins found for location {} to notify about calibration for tool {}", location, t.getId());
                    continue;
                }

                // Expected completion date: assume one week after scheduled for now
                LocalDate expected = next.plusDays(7);

                for (Admin a : admins) {
                    try {
                        String name = a.getName() == null ? "Admin" : a.getName();
                        emailService.sendCalibrationEmail(a.getEmail(), name, t, next, expected);
                        logger.info("Sent calibration notification to {} for tool {} (days={} )", a.getEmail(), t.getId(), days);
                    } catch (Exception e) {
                        logger.warn("Failed to send calibration email to {}: {}", a.getEmail(), e.getMessage());
                    }
                }
            }
        }
    }
}
