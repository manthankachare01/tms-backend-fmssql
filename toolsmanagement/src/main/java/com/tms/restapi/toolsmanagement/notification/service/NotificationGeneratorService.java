package com.tms.restapi.toolsmanagement.notification.service;

import com.tms.restapi.toolsmanagement.notification.model.Notification;
import com.tms.restapi.toolsmanagement.notification.repository.NotificationRepository;
import com.tms.restapi.toolsmanagement.tools.model.Tool;
import com.tms.restapi.toolsmanagement.issuance.model.Issuance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
public class NotificationGeneratorService {

    @Autowired
    private NotificationRepository notificationRepository;

    /**
     * Create a notification for a missing tool
     */
    public void createToolMissingNotification(Tool tool) {
        Notification notification = new Notification();
        notification.setType("TOOL_MISSING");
        notification.setSeverity("CRITICAL");
        notification.setTitle("Tool Missing");
        notification.setMessage(String.format("Tool '%s' (SI No: %s) is missing from location '%s'",
                tool.getDescription(), tool.getSiNo(), tool.getLocation()));
        notification.setToolId(tool.getId());
        notification.setLocation(tool.getLocation());
        notification.setTargetRole("SUPERADMIN");

        notificationRepository.save(notification);
    }

    /**
     * Create a notification for an obsolete tool
     */
    public void createToolObsoleteNotification(Tool tool) {
        Notification notification = new Notification();
        notification.setType("TOOL_OBSOLETE");
        notification.setSeverity("HIGH");
        notification.setTitle("Tool Obsolete");
        notification.setMessage(String.format("Tool '%s' (SI No: %s) is marked as obsolete",
                tool.getDescription(), tool.getSiNo()));
        notification.setToolId(tool.getId());
        notification.setLocation(tool.getLocation());
        notification.setTargetRole("SUPERADMIN");

        notificationRepository.save(notification);
    }

    /**
     * Create a notification for a damaged tool
     */
    public void createToolDamagedNotification(Tool tool) {
        Notification notification = new Notification();
        notification.setType("TOOL_DAMAGED");
        notification.setSeverity("HIGH");
        notification.setTitle("Tool Damaged");
        notification.setMessage(String.format("Tool '%s' (SI No: %s) at location '%s' is reported as damaged",
                tool.getDescription(), tool.getSiNo(), tool.getLocation()));
        notification.setToolId(tool.getId());
        notification.setLocation(tool.getLocation());
        notification.setTargetRole("SUPERADMIN");

        notificationRepository.save(notification);
    }

    /**
     * Create a notification for an overdue tool return
     */
    public void createReturnOverdueNotification(Issuance issuance) {
        Notification notification = new Notification();
        notification.setType("RETURN_OVERDUE");
        notification.setSeverity("CRITICAL");
        notification.setTitle("Tool Return Overdue");
        notification.setMessage(String.format("Trainer '%s' has not returned tools from '%s' training. " +
                "Due date was %s, days overdue: %d",
                issuance.getTrainerName(), issuance.getTrainingName(), issuance.getReturnDate(),
                ChronoUnit.DAYS.between(issuance.getReturnDate(), LocalDate.now())));
        notification.setIssuanceId(issuance.getId());
        notification.setTrainerId(issuance.getTrainerId());
        notification.setLocation(issuance.getLocation());
        notification.setTargetRole("SUPERADMIN");

        notificationRepository.save(notification);
    }

    /**
     * Create a notification for a return due tomorrow
     */
    public void createReturnDueTomorrowNotification(Issuance issuance) {
        Notification notification = new Notification();
        notification.setType("RETURN_DUE_TOMORROW");
        notification.setSeverity("MEDIUM");
        notification.setTitle("Tool Return Due Tomorrow");
        notification.setMessage(String.format("Tools from '%s' training are due to be returned tomorrow (%s). " +
                "Please prepare for return.",
                issuance.getTrainingName(), issuance.getReturnDate()));
        notification.setIssuanceId(issuance.getId());
        notification.setTrainerId(issuance.getTrainerId());
        notification.setLocation(issuance.getLocation());
        notification.setTargetRole("TRAINER");

        notificationRepository.save(notification);
    }

    /**
     * Create a notification for an issuance reminder
     */
    public void createIssuanceReminderNotification(Issuance issuance) {
        Notification notification = new Notification();
        notification.setType("ISSUANCE_REMINDER");
        notification.setSeverity("MEDIUM");
        notification.setTitle("Tool Issuance Reminder");
        notification.setMessage(String.format("You have been issued tools for '%s' training. " +
                "Return date: %s", issuance.getTrainingName(), issuance.getReturnDate()));
        notification.setIssuanceId(issuance.getId());
        notification.setTrainerId(issuance.getTrainerId());
        notification.setLocation(issuance.getLocation());
        notification.setTargetRole("TRAINER");

        notificationRepository.save(notification);
    }

    /**
     * Create a notification for calibration reminder
     */
    public void createCalibrationReminderNotification(Tool tool, int daysUntilDue) {
        Notification notification = new Notification();
        notification.setType("CALIBRATION");
        notification.setSeverity(daysUntilDue <= 7 ? "HIGH" : "MEDIUM");
        notification.setTitle("Tool Calibration Due");
        notification.setMessage(String.format("Tool '%s' (SI No: %s) at location '%s' requires calibration. " +
                "Due date: %s (in %d days)", tool.getDescription(), tool.getSiNo(),
                tool.getLocation(), tool.getNextCalibrationDate(), daysUntilDue));
        notification.setToolId(tool.getId());
        notification.setLocation(tool.getLocation());
        notification.setTargetRole("ADMIN");

        notificationRepository.save(notification);
    }
}
