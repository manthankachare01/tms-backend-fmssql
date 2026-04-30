package com.tms.restapi.toolsmanagement.auth.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import com.tms.restapi.toolsmanagement.issuance.model.Issuance;
import com.tms.restapi.toolsmanagement.issuance.model.IssuanceRequest;
import com.tms.restapi.toolsmanagement.issuance.model.ReturnRecord;
import com.tms.restapi.toolsmanagement.issuance.model.ReturnItem;
import com.tms.restapi.toolsmanagement.tools.model.Tool;
import com.tms.restapi.toolsmanagement.kit.model.Kit;
import com.tms.restapi.toolsmanagement.tools.repository.ToolRepository;
import com.tms.restapi.toolsmanagement.kit.repository.KitRepository;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired(required = false)
    private JavaMailSender mailSender;

    // Brevo config (set as env vars or in application.properties)
    @Value("${brevo.api.key:}")
    private String brevoApiKey;

    @Value("${brevo.sender.email:no-reply@example.com}")
    private String brevoSenderEmail;

    @Value("${brevo.sender.name:ToolsManagement}")
    private String brevoSenderName;

    private final RestTemplate rest = new RestTemplate();

    @Autowired(required = false)
    private ToolRepository toolRepository;

    @Autowired(required = false)
    private KitRepository kitRepository;

    public void sendOtp(String to, String otp, String role) {
        // Prefer Brevo API if key provided (useful on platforms where SMTP is blocked)
        if (brevoApiKey != null && !brevoApiKey.isBlank()) {
            try {
                String url = "https://api.brevo.com/v3/smtp/email";
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("api-key", brevoApiKey);

                Map<String, Object> payload = new HashMap<>();
                Map<String, String> sender = new HashMap<>();
                sender.put("email", brevoSenderEmail);
                sender.put("name", brevoSenderName);
                payload.put("sender", sender);

                Map<String, String> toMap = new HashMap<>();
                toMap.put("email", to);
                payload.put("to", new Map[]{toMap});

                payload.put("subject", "Your password reset OTP");
                // HTML formatted content per design
                String salutation = displayRole(role);
                String html = "<p>Dear " + salutation + ",</p>"
                        + "<p>Greetings from Škoda Volkswagen India Pvt. Ltd.</p>"
                        + "<p>We received a request to reset the password for your registered account. To proceed with the password reset, please use the <strong>One-Time Password (OTP)</strong> provided below:</p>"
                        + "<h4 style=\"color:#333;\">OTP: " + otp + "</h4>"
                        + "<p>This OTP is valid for <strong>10 minutes</strong> only. Please do not share this OTP with anyone for security reasons.</p>"
                        + "<p>Warm regards,<br><strong>IT Support Team</strong><br>Škoda Volkswagen India Pvt. Ltd.</p>";
                payload.put("htmlContent", html);
                payload.put("textContent", "Your OTP for password reset is: " + otp + " (valid for 10 minutes)");

                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
                ResponseEntity<String> resp = rest.postForEntity(url, entity, String.class);
                logger.info("Brevo response: {}", resp.getStatusCode());
                return;
            } catch (Exception e) {
                logger.warn("Brevo send failed for {} otp {}: {}", to, otp, e.getMessage());
                throw new RuntimeException("Brevo send failed. Check logs for OTP: " + otp, e);
            }
        }

        // Fallback to JavaMailSender if available (send HTML)
        if (mailSender == null) {
            logger.warn("Mail sender not configured and Brevo API key not set. OTP for {}: {}", to, otp);
            throw new RuntimeException("Mail sender not configured and Brevo not configured. For dev, check logs for OTP.");
        }

        try {
            MimeMessage mime = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mime, "utf-8");
            helper.setTo(to);
            helper.setSubject("Your password reset OTP");
                String salutation = displayRole(role);
                String html = "<p>Dear " + salutation + ",</p>"
                    + "<p>Greetings from Škoda Volkswagen India Pvt. Ltd.</p>"
                    + "<p>We received a request to reset the password for your registered account. To proceed with the password reset, please use the <strong>One-Time Password (OTP)</strong> provided below:</p>"
                    + "<h2 style=\"color:#333;\">OTP: <strong>" + otp + "</strong></h2>"
                    + "<p>This OTP is valid for <strong>10 minutes</strong> only. Please do not share this OTP with anyone for security reasons.</p>"
                    + "<p>Warm regards,<br><strong>IT Support Team</strong><br>Škoda Volkswagen India Pvt. Ltd.</p>";
            helper.setText(html, true);
            mailSender.send(mime);
            logger.info("OTP email sent to: {}", to);
        } catch (Exception e) {
            logger.warn("Failed to send OTP email to {}. OTP: {}. Error: {}", to, otp, e.getMessage());
            throw new RuntimeException("Failed to send OTP email. For dev, check logs for OTP: " + otp, e);
        }
    }

    private String displayRole(String role) {
        if (role == null) return "User";
        String r = role.trim().toLowerCase();
        switch (r) {
            case "admin": return "Admin";
            case "trainer": return "Trainer";
            case "security": return "Security";
            case "superadmin": return "Superadmin";
            default:
                // Capitalize first letter
                if (r.length() == 0) return "User";
                return r.substring(0,1).toUpperCase() + r.substring(1);
        }
    }

    /**
     * Send welcome credentials email with username and password.
     * Message format:
     * Welcome to tools management system
     * username : <email>
     * password : <password>
     * Please reset your password
     */
    public void sendCredentials(String to, String plainPassword, String role) {
        String displayRole = displayRole(role);

        String html = "<p>Dear " + displayRole + ",</p>"
                + "<p>Greetings from Škoda Volkswagen India Pvt. Ltd.</p>"
                + "<p>We are pleased to inform you that your account has been <strong>successfully created</strong> on the Škoda Volkswagen India Pvt. Ltd. system.</p>"
                + "<p>You may now log in using your registered email ID and proceed with the available services.</p>"
                + "<p><strong>Role:</strong> " + (displayRole != null ? displayRole : "") + "<br/>"
                + "<strong>Username / Email:</strong> <a href=\"mailto:" + to + "\">" + to + "</a><br/>"
                + "<strong>Temporary Password:</strong> " + plainPassword + "</p>"
                + "<p><strong>Important Instructions:</strong></p>"
                + "<ul>"
                + "<li>Please log in using the above credentials.</li>"
                + "<li>You are required to <strong>reset your password upon your first login</strong> for security purposes.</li>"
                + "<li>Keep your credentials confidential and do not share them with anyone.</li>"
                + "</ul>"
                + "<p>We look forward to your association with Škoda Volkswagen India Pvt. Ltd.</p>"
                + "<p>Warm regards,<br/><strong>IT Support Team</strong><br/>Škoda Volkswagen India Pvt. Ltd.</p>";

        String textFallback = "Dear " + displayRole + ",\n\n"
                + "Your account has been successfully created.\n\n"
                + "Role: " + displayRole + "\n"
                + "Username / Email: " + to + "\n"
                + "Temporary Password: " + plainPassword + "\n\n"
                + "Please reset your password upon first login.";

        // Prefer Brevo if configured
        if (brevoApiKey != null && !brevoApiKey.isBlank()) {
            try {
                String url = "https://api.brevo.com/v3/smtp/email";
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("api-key", brevoApiKey);

                Map<String, Object> payload = new HashMap<>();
                Map<String, String> sender = new HashMap<>();
                sender.put("email", brevoSenderEmail);
                sender.put("name", brevoSenderName);
                payload.put("sender", sender);

                Map<String, String> toMap = new HashMap<>();
                toMap.put("email", to);
                payload.put("to", new Map[]{toMap});

                payload.put("subject", "Welcome to Tools Management System");
                payload.put("htmlContent", html);
                payload.put("textContent", textFallback);

                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
                ResponseEntity<String> resp = rest.postForEntity(url, entity, String.class);
                logger.info("Brevo credentials response: {}", resp.getStatusCode());
                return;
            } catch (Exception e) {
                logger.warn("Brevo sendCredentials failed for {}: {}", to, e.getMessage());
                throw new RuntimeException("Brevo send failed. Check logs for credentials for " + to, e);
            }
        }

        // Fallback to SMTP
        if (mailSender == null) {
            logger.warn("Mail sender not configured and Brevo not set. Credentials for {}: {}", to, plainPassword);
            throw new RuntimeException("Mail sender not configured and Brevo not configured. For dev, check logs for credentials.");
        }

        try {
            MimeMessage mime = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mime, "utf-8");
            helper.setTo(to);
            helper.setSubject("Welcome to Tools Management System");
            helper.setText(html, true);
            mailSender.send(mime);
            logger.info("Credentials email sent to: {}", to);
        } catch (Exception e) {
            logger.warn("Failed to send credentials email to {}. Error: {}", to, e.getMessage());
            throw new RuntimeException("Failed to send credentials email. For dev, check logs for credentials for " + to, e);
        }
    }

    // Send issuance email to trainer using the Issuance object
    public void sendIssuanceEmail(Issuance issuance, String trainerEmail) {
        StringBuilder sb = new StringBuilder();
        sb.append("Dear ").append(issuance.getTrainerName()).append(",\n\n");
        sb.append("Greetings from Škoda Volkswagen India Pvt. Ltd.\n\n");
        sb.append("This is to formally inform you that the following tools have been issued to you as part of your training responsibilities. Kindly find the issuance details below for your reference:\n\n");
        sb.append("Trainer Name: ").append(issuance.getTrainerName()).append("\n");
        sb.append("Issuance ID: ").append(issuance.getId()).append("\n");
        sb.append("Issue Date: ").append(issuance.getIssuanceDate()).append("\n");
        sb.append("Return Date: ").append(issuance.getReturnDate()).append("\n\n");
        sb.append("List of Issued Tools:\n\n");

        int counter = 1;
        if (issuance.getToolIds() != null) {
            for (Long toolId : issuance.getToolIds()) {
                Tool[] tArray = new Tool[1];
                if (toolRepository != null) toolRepository.findById(toolId).ifPresent(tt -> tArray[0] = tt);
                String name = tArray[0] != null ? tArray[0].getDescription() : "Tool " + toolId;
                sb.append(counter++).append(". ").append(name).append(" – [").append(toolId).append(" / 1]\n\n");
            }
        }
        if (issuance.getKitIds() != null) {
            for (Long kitId : issuance.getKitIds()) {
                Kit[] kArray = new Kit[1];
                if (kitRepository != null) kitRepository.findById(kitId).ifPresent(kk -> kArray[0] = kk);
                String name = kArray[0] != null ? kArray[0].getKitName() : "Kit " + kitId;
                sb.append(counter++).append(". ").append(name).append(" – [").append(kitId).append(" / 1]\n\n");
            }
        }

        sb.append("You are requested to ensure that all issued tools are used responsibly and returned in proper working condition on or before the mentioned return date. Any loss or damage should be reported immediately to the concerned department.\n\n");
        sb.append("For any clarification or assistance, feel free to contact the Tool Management Team.\n\n");
        sb.append("Thank you for your cooperation.\n\n");
        sb.append("Warm regards,\nTool Management Team\nŠkoda Volkswagen India Pvt. Ltd.");

        sendPlainTextEmail(trainerEmail, "Issuance Notification - Tools Issued", sb.toString());
    }

    /**
     * Send SuperAdmin credentials/notification email when credentials or email are updated.
     * If plainPassword is null, password line will be omitted.
     */
    public void sendSuperAdminUpdated(String to, String name, String plainPassword) {
        String salutation = name != null && !name.isBlank() ? name : "Super Admin";

        String html = "<p>Dear " + salutation + ",</p>"
                + "<p>Greetings from Škoda Volkswagen India Pvt. Ltd.</p>"
                + "<p>This is to inform you that your <strong>Super Admin account credentials</strong> have been <strong>successfully created</strong> and transferred to you. Kindly find your login details below:</p>"
                + "<p><strong>Role:</strong> Super Admin<br/>"
                + "<strong>Username / Email:</strong> <a href=\"mailto:" + to + "\">" + to + "</a>";

        if (plainPassword != null && !plainPassword.isBlank()) {
            html += "<br/><strong>Password:</strong> " + plainPassword;
        }

        html += "</p>"
                + "<p><strong>Important Instructions:</strong></p>"
                + "<ul>"
                + "<li>Please log in using the above credentials.</li>"
                + "<li>You are required to <strong>reset your password upon your first login</strong> for security purposes.</li>"
                + "<li>Keep your credentials confidential and do not share them with anyone.</li>"
                + "</ul>"
                + "<p>For any assistance or issues accessing your account, please contact the IT Support Team.</p>"
                + "<p>Warm regards,<br/><strong>IT Support Team</strong><br/>Škoda Volkswagen India Pvt. Ltd.</p>";

        String text = "Dear " + salutation + ",\n\n"
                + "This is to inform you that your Super Admin account credentials have been successfully created and transferred to you.\n\n"
                + "Role: Super Admin\n"
                + "Username / Email: " + to + "\n";

        if (plainPassword != null && !plainPassword.isBlank()) {
            text += "Password: " + plainPassword + "\n";
        }

        text += "\nImportant Instructions:\n"
                + "- Please log in using the above credentials.\n"
                + "- You are required to reset your password upon your first login for security purposes.\n"
                + "- Keep your credentials confidential and do not share them with anyone.\n\n"
                + "For any assistance or issues accessing your account, please contact the IT Support Team.\n\n"
                + "Warm regards,\nIT Support Team\nŠkoda Volkswagen India Pvt. Ltd.";

        // Try Brevo
        if (brevoApiKey != null && !brevoApiKey.isBlank()) {
            try {
                String url = "https://api.brevo.com/v3/smtp/email";
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("api-key", brevoApiKey);

                Map<String, Object> payload = new HashMap<>();
                Map<String, String> sender = new HashMap<>();
                sender.put("email", brevoSenderEmail);
                sender.put("name", brevoSenderName);
                payload.put("sender", sender);

                Map<String, String> toMap = new HashMap<>();
                toMap.put("email", to);
                payload.put("to", new Map[]{toMap});

                payload.put("subject", "Super Admin Account Credentials");
                payload.put("htmlContent", html);
                payload.put("textContent", text);

                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
                rest.postForEntity(url, entity, String.class);
                return;
            } catch (Exception e) {
                logger.warn("Brevo sendSuperAdminUpdated failed for {}: {}", to, e.getMessage());
            }
        }

        // Fallback to SMTP
        if (mailSender != null) {
            try {
                MimeMessage mime = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(mime, "utf-8");
                helper.setTo(to);
                helper.setSubject("Super Admin Account Credentials");
                helper.setText(html, true);
                mailSender.send(mime);
                return;
            } catch (Exception e) {
                logger.warn("Failed to send superadmin email to {}: {}", to, e.getMessage());
            }
        }

        // No provider configured -> log
        logger.info("SuperAdmin email not sent (no provider). To: {}. Subject: Super Admin Account Credentials. Body:\n{}", to, text);
    }

    // Send return email to trainer using the ReturnRecord
    public void sendReturnEmail(ReturnRecord rr, String trainerEmail) {
        Issuance issuance = rr.getIssuance();
        StringBuilder sb = new StringBuilder();
        sb.append("Dear ").append(issuance.getTrainerName()).append(",\n\n");
        sb.append("Greetings from Škoda Volkswagen India Pvt. Ltd.\n\n");
        sb.append("This is to formally confirm the return of the tools issued to you for training purposes. The details of the tool return are mentioned below for your reference:\n\n");
        sb.append("Trainer Name: ").append(issuance.getTrainerName()).append("\n");
        sb.append("Issuance ID: ").append(issuance.getId()).append("\n");
        sb.append("Issue Date: ").append(issuance.getIssuanceDate()).append("\n");
        sb.append("Return Date: ").append(rr.getActualReturnDate()).append("\n\n");
        sb.append("List of Returned Tools:\n\n");

        int counter = 1;
        if (rr.getItems() != null && !rr.getItems().isEmpty()) {
            for (var ri : rr.getItems()) {
                String name = "";
                if (ri.getToolId() != null && toolRepository != null) {
                    Tool t = toolRepository.findById(ri.getToolId()).orElse(null);
                    name = t != null ? t.getDescription() : "Tool " + ri.getToolId();
                    sb.append(counter++).append(". ").append(name).append(" – [").append(ri.getToolId()).append(" / ").append(ri.getQuantityReturned()).append("]\n\n");
                } else if (ri.getKitId() != null && kitRepository != null) {
                    Kit k = kitRepository.findById(ri.getKitId()).orElse(null);
                    name = k != null ? k.getKitName() : "Kit " + ri.getKitId();
                    sb.append(counter++).append(". ").append(name).append(" – [").append(ri.getKitId()).append(" / ").append(ri.getQuantityReturned()).append("]\n\n");
                }
            }
        } else {
            // no items in return record -> list issuance's tool/kit ids
            if (issuance.getToolIds() != null) {
                for (Long toolId : issuance.getToolIds()) {
                    Tool t = toolRepository != null ? toolRepository.findById(toolId).orElse(null) : null;
                    String name = t != null ? t.getDescription() : "Tool " + toolId;
                    sb.append(counter++).append(". ").append(name).append(" – [").append(toolId).append(" / 1]\n\n");
                }
            }
            if (issuance.getKitIds() != null) {
                for (Long kitId : issuance.getKitIds()) {
                    Kit k = kitRepository != null ? kitRepository.findById(kitId).orElse(null) : null;
                    String name = k != null ? k.getKitName() : "Kit " + kitId;
                    sb.append(counter++).append(". ").append(name).append(" – [").append(kitId).append(" / 1]\n\n");
                }
            }
        }

        sb.append("For any further assistance, please feel free to reach out to the Tool Management Team.\n\n");
        sb.append("Warm regards,\nTool Management Team\nŠkoda Volkswagen India Pvt. Ltd.");

        sendPlainTextEmail(trainerEmail, "Return Confirmation - Tools Returned", sb.toString());
    }

    // helper to send plain text email using existing Brevo/SMTP logic
    private void sendPlainTextEmail(String to, String subject, String body) {
        // Try Brevo first
        if (brevoApiKey != null && !brevoApiKey.isBlank()) {
            try {
                String url = "https://api.brevo.com/v3/smtp/email";
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("api-key", brevoApiKey);

                Map<String, Object> payload = new HashMap<>();
                Map<String, String> sender = new HashMap<>();
                sender.put("email", brevoSenderEmail);
                sender.put("name", brevoSenderName);
                payload.put("sender", sender);

                Map<String, String> toMap = new HashMap<>();
                toMap.put("email", to);
                payload.put("to", new Map[]{toMap});

                payload.put("subject", subject);
                payload.put("textContent", body);

                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
                rest.postForEntity(url, entity, String.class);
                return;
            } catch (Exception e) {
                logger.warn("Brevo send failed: {}", e.getMessage());
            }
        }

        // Fallback to SMTP
        if (mailSender != null) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(to);
                message.setSubject(subject);
                message.setText(body);
                mailSender.send(message);
            } catch (Exception e) {
                logger.warn("Failed to send email to {}: {}", to, e.getMessage());
            }
        } else {
            // nothing configured, log the message
            logger.info("Email to {} not sent (no provider configured). Subject: {}. Body:\n{}", to, subject, body);
        }
    }

    /**
     * Send calibration notification email to admin for a specific tool.
     * scheduledDate: the nextCalibrationDate
     * expectedCompletionDate: approximate expected completion (can be same day or later)
     */
    public void sendCalibrationEmail(String to, String adminName, Tool tool, java.time.LocalDate scheduledDate, java.time.LocalDate expectedCompletionDate) {
        String salutation = adminName != null && !adminName.isBlank() ? adminName : "Admin";

        String html = "<p>Dear " + salutation + ",</p>"
                + "<p>Greetings from Škoda Volkswagen India Pvt. Ltd.</p>"
                + "<p>The calibration of the tool(s) has been <strong>scheduled</strong> in accordance with standard maintenance and quality requirements. The details are provided below for your reference:</p>"
                + "<p><strong>" + escapeHtml(tool.getDescription()) + "</strong> – [" + escapeHtml(tool.getToolNo()) + "]</p>"
                + "<p><strong>Scheduled Calibration Date:</strong> " + (scheduledDate != null ? scheduledDate.toString() : "N/A") + "<br/>"
                + "<strong>Expected Completion Date:</strong> " + (expectedCompletionDate != null ? expectedCompletionDate.toString() : "N/A") + "</p>"
                + "<p>Thank you for your cooperation.</p>"
                + "<p>Warm regards,<br/><strong>Tool Management & Calibration Team</strong><br/>Škoda Volkswagen India Pvt. Ltd.</p>";

        String text = "Dear " + salutation + ",\n\n"
                + "The calibration of the tool(s) has been scheduled. Details:\n\n"
                + tool.getDescription() + " – [" + tool.getToolNo() + "]\n"
                + "Scheduled Calibration Date: " + (scheduledDate != null ? scheduledDate.toString() : "N/A") + "\n"
                + "Expected Completion Date: " + (expectedCompletionDate != null ? expectedCompletionDate.toString() : "N/A") + "\n\n"
                + "Thank you for your cooperation.\n\n"
                + "Warm regards,\nTool Management & Calibration Team\nŠkoda Volkswagen India Pvt. Ltd.";

        // Use Brevo or SMTP via helper
        // Try Brevo first
        if (brevoApiKey != null && !brevoApiKey.isBlank()) {
            try {
                String url = "https://api.brevo.com/v3/smtp/email";
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("api-key", brevoApiKey);

                Map<String, Object> payload = new HashMap<>();
                Map<String, String> sender = new HashMap<>();
                sender.put("email", brevoSenderEmail);
                sender.put("name", brevoSenderName);
                payload.put("sender", sender);

                Map<String, String> toMap = new HashMap<>();
                toMap.put("email", to);
                payload.put("to", new Map[]{toMap});

                payload.put("subject", "Calibration Scheduled: " + (tool.getDescription() == null ? "Tool" : tool.getDescription()));
                payload.put("htmlContent", html);
                payload.put("textContent", text);

                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
                rest.postForEntity(url, entity, String.class);
                return;
            } catch (Exception e) {
                logger.warn("Brevo sendCalibrationEmail failed for {}: {}", to, e.getMessage());
            }
        }

        // Fallback to SMTP / plain text helper
        sendPlainTextEmail(to, "Calibration Scheduled - " + (tool.getDescription() == null ? "Tool" : tool.getDescription()), text);
    }

    // minimal HTML-escaping for values inserted into HTML
    private String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }

    /**
     * Send email to admin about tools/kits that were returned in damaged/missing/obsolete condition.
     * items: list of ReturnItem objects with problematic condition
     * issuance: the related Issuance object (contains trainerName, location, etc.)
     * adminEmail: email of the admin to notify (from the tool/kit's location)
     * adminName: name of the admin (for salutation)
     */
    public void sendDamagedItemNotification(java.util.List<ReturnItem> items, Issuance issuance, String adminEmail, String adminName) {
        if (items == null || items.isEmpty()) return;
        if (adminEmail == null || adminEmail.isBlank()) return;

        String salutation = adminName != null && !adminName.isBlank() ? adminName : "Admin";

        // Build tool list for HTML
        StringBuilder htmlItems = new StringBuilder();
        StringBuilder textItems = new StringBuilder();
        int counter = 1;
        for (ReturnItem ri : items) {
            String itemName = "";
            String itemId = "";

            if (ri.getToolId() != null && toolRepository != null) {
                Tool t = toolRepository.findById(ri.getToolId()).orElse(null);
                itemName = t != null ? t.getDescription() : "Tool " + ri.getToolId();
                itemId = t != null ? t.getToolNo() : ri.getToolId().toString();
            } else if (ri.getKitId() != null && kitRepository != null) {
                Kit k = kitRepository.findById(ri.getKitId()).orElse(null);
                itemName = k != null ? k.getKitName() : "Kit " + ri.getKitId();
                itemId = ri.getKitId().toString();
            }

            htmlItems.append("<p>").append(counter).append(". ").append(escapeHtml(itemName))
                    .append(" – [").append(escapeHtml(itemId)).append("]</p>");
            textItems.append(counter).append(". ").append(itemName).append(" – [").append(itemId).append("]\n");
            counter++;
        }

        String issuanceId = issuance != null && issuance.getId() != null ? issuance.getId().toString() : "N/A";
        String issueDate = issuance != null && issuance.getIssuanceDate() != null ? issuance.getIssuanceDate().toString() : "N/A";

        String html = "<p>Dear " + escapeHtml(salutation) + ",</p>"
                + "<p>Greetings from Škoda Volkswagen India Pvt. Ltd.</p>"
                + "<p>This is to formally notify you that the following tool(s) issued have been identified as <strong>missing/damaged/obsolete</strong>.The details are mentioned below for your reference:</p>"
                + "<p><strong>Trainer Name:</strong> " + escapeHtml(issuance != null && issuance.getTrainerName() != null ? issuance.getTrainerName() : "") + "</p>"
                + htmlItems.toString()
                + "<p><strong>Issuance ID:</strong> " + escapeHtml(issuanceId) + "<br/>"
                + "<strong>Issue Date:</strong> " + escapeHtml(issueDate) + "</p>"
                + "<p>Thank you for your cooperation.</p>"
                + "<p>Warm regards,<br/><strong>Tool Management Team</strong><br/>Škoda Volkswagen India Pvt. Ltd.</p>";

        String text = "Dear " + salutation + ",\n\n"
                + "Greetings from Škoda Volkswagen India Pvt. Ltd.\n\n"
                + "This is to formally notify you that the following tool(s) issued have been identified as missing/damaged/obsolete. The details are mentioned below for your reference:\n\n"
                + "Trainer Name: " + (issuance != null && issuance.getTrainerName() != null ? issuance.getTrainerName() : "") + "\n\n"
                + textItems.toString()
                + "Issuance ID: " + issuanceId + "\n"
                + "Issue Date: " + issueDate + "\n\n"
                + "Thank you for your cooperation.\n\n"
                + "Warm regards,\nTool Management Team\nŠkoda Volkswagen India Pvt. Ltd.";

        // Try Brevo first
        if (brevoApiKey != null && !brevoApiKey.isBlank()) {
            try {
                String url = "https://api.brevo.com/v3/smtp/email";
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("api-key", brevoApiKey);

                Map<String, Object> payload = new HashMap<>();
                Map<String, String> sender = new HashMap<>();
                sender.put("email", brevoSenderEmail);
                sender.put("name", brevoSenderName);
                payload.put("sender", sender);

                Map<String, String> toMap = new HashMap<>();
                toMap.put("email", adminEmail);
                payload.put("to", new Map[]{toMap});

                payload.put("subject", "Alert: Missing/Damaged/Obsolete Tools Reported");
                payload.put("htmlContent", html);
                payload.put("textContent", text);

                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
                rest.postForEntity(url, entity, String.class);
                logger.info("Brevo damaged item notification sent to {}", adminEmail);
                return;
            } catch (Exception e) {
                logger.warn("Brevo sendDamagedItemNotification failed for {}: {}", adminEmail, e.getMessage());
            }
        }

        // Fallback to SMTP / plain text helper
        sendPlainTextEmail(adminEmail, "Alert: Missing/Damaged/Obsolete Tools Reported", text);
    }

    /**
     * Send overdue notification email to trainer when tools are returned late.
     * issuance: the Issuance object (contains tool/kit names, dates, etc.)
     * trainerEmail: email to send to (trainer's email)
     * trainerName: trainer name for salutation
     */
    public void sendOverdueEmailToTrainer(Issuance issuance, String trainerEmail, String trainerName) {
        if (trainerEmail == null || trainerEmail.isBlank()) return;
        if (issuance == null) return;

        String salutation = trainerName != null && !trainerName.isBlank() ? trainerName : "Trainer";
        String issuanceId = issuance.getId() != null ? issuance.getId().toString() : "N/A";
        String issueDate = issuance.getIssuanceDate() != null ? issuance.getIssuanceDate().toString() : "N/A";
        String returnDueDate = issuance.getReturnDate() != null ? issuance.getReturnDate().toString() : "N/A";

        // Build tool list
        StringBuilder htmlItems = new StringBuilder();
        StringBuilder textItems = new StringBuilder();
        int counter = 1;

        if (issuance.getToolIds() != null) {
            for (Long toolId : issuance.getToolIds()) {
                Tool t = toolRepository != null ? toolRepository.findById(toolId).orElse(null) : null;
                String name = t != null ? t.getDescription() : "Tool " + toolId;
                String id = t != null ? t.getToolNo() : toolId.toString();
                htmlItems.append("<p>").append(counter).append(". ").append(escapeHtml(name))
                        .append(" – [").append(escapeHtml(id)).append("]</p>");
                textItems.append(counter).append(". ").append(name).append(" – [").append(id).append("]\n");
                counter++;
            }
        }

        if (issuance.getKitIds() != null) {
            for (Long kitId : issuance.getKitIds()) {
                Kit k = kitRepository != null ? kitRepository.findById(kitId).orElse(null) : null;
                String name = k != null ? k.getKitName() : "Kit " + kitId;
                htmlItems.append("<p>").append(counter).append(". ").append(escapeHtml(name))
                        .append(" – [").append(escapeHtml(kitId.toString())).append("]</p>");
                textItems.append(counter).append(". ").append(name).append(" – [").append(kitId).append("]\n");
                counter++;
            }
        }

        String html = "<p>Dear " + escapeHtml(salutation) + ",</p>"
                + "<p>Greetings from Škoda Volkswagen India Pvt. Ltd.</p>"
                + "<p>This is a reminder that the return date for the tool(s) issued to you has <strong>passed</strong>, and the tools are currently <strong>overdue</strong>. Kindly return the tools immediately or inform the Tool Management Team in case of any operational dependency.</p>"
                + htmlItems.toString()
                + "<p><strong>Issuance ID:</strong> " + escapeHtml(issuanceId) + "<br/>"
                + "<strong>Issue Date:</strong> " + escapeHtml(issueDate) + "<br/>"
                + "<strong>Return Due Date:</strong> " + escapeHtml(returnDueDate) + "</p>"
                + "<p>Warm regards,<br/><strong>Tool Management Team</strong><br/>Škoda Volkswagen India Pvt. Ltd.</p>";

        String text = "Dear " + salutation + ",\n\n"
                + "Greetings from Škoda Volkswagen India Pvt. Ltd.\n\n"
                + "This is a reminder that the return date for the tool(s) issued to you has passed, and the tools are currently overdue. Kindly return the tools immediately or inform the Tool Management Team in case of any operational dependency.\n\n"
                + textItems.toString()
                + "Issuance ID: " + issuanceId + "\n"
                + "Issue Date: " + issueDate + "\n"
                + "Return Due Date: " + returnDueDate + "\n\n"
                + "Warm regards,\nTool Management Team\nŠkoda Volkswagen India Pvt. Ltd.";

        // Try Brevo
        if (brevoApiKey != null && !brevoApiKey.isBlank()) {
            try {
                String url = "https://api.brevo.com/v3/smtp/email";
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("api-key", brevoApiKey);

                Map<String, Object> payload = new HashMap<>();
                Map<String, String> sender = new HashMap<>();
                sender.put("email", brevoSenderEmail);
                sender.put("name", brevoSenderName);
                payload.put("sender", sender);

                Map<String, String> toMap = new HashMap<>();
                toMap.put("email", trainerEmail);
                payload.put("to", new Map[]{toMap});

                payload.put("subject", "Overdue Tool Return - Action Required");
                payload.put("htmlContent", html);
                payload.put("textContent", text);

                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
                rest.postForEntity(url, entity, String.class);
                logger.info("Brevo overdue trainer notification sent to {}", trainerEmail);
                return;
            } catch (Exception e) {
                logger.warn("Brevo sendOverdueEmailToTrainer failed for {}: {}", trainerEmail, e.getMessage());
            }
        }

        // Fallback to SMTP / plain text helper
        sendPlainTextEmail(trainerEmail, "Overdue Tool Return - Action Required", text);
    }

    /**
     * Send overdue notification email to admin when tools issued to a trainer are overdue.
     * issuance: the Issuance object
     * adminEmail: email to send to (admin's email)
     * adminName: admin name for salutation
     */
    public void sendOverdueEmailToAdmin(Issuance issuance, String adminEmail, String adminName) {
        if (adminEmail == null || adminEmail.isBlank()) return;
        if (issuance == null) return;

        String salutation = adminName != null && !adminName.isBlank() ? adminName : "Admin";
        String trainerName = issuance.getTrainerName() != null ? issuance.getTrainerName() : "Unknown Trainer";
        String issuanceId = issuance.getId() != null ? issuance.getId().toString() : "N/A";
        String issueDate = issuance.getIssuanceDate() != null ? issuance.getIssuanceDate().toString() : "N/A";
        String returnDueDate = issuance.getReturnDate() != null ? issuance.getReturnDate().toString() : "N/A";

        // Build tool list
        StringBuilder htmlItems = new StringBuilder();
        StringBuilder textItems = new StringBuilder();
        int counter = 1;

        if (issuance.getToolIds() != null) {
            for (Long toolId : issuance.getToolIds()) {
                Tool t = toolRepository != null ? toolRepository.findById(toolId).orElse(null) : null;
                String name = t != null ? t.getDescription() : "Tool " + toolId;
                String id = t != null ? t.getToolNo() : toolId.toString();
                htmlItems.append("<p>").append(counter).append(". ").append(escapeHtml(name))
                        .append(" – [").append(escapeHtml(id)).append("]</p>");
                textItems.append(counter).append(". ").append(name).append(" – [").append(id).append("]\n");
                counter++;
            }
        }

        if (issuance.getKitIds() != null) {
            for (Long kitId : issuance.getKitIds()) {
                Kit k = kitRepository != null ? kitRepository.findById(kitId).orElse(null) : null;
                String name = k != null ? k.getKitName() : "Kit " + kitId;
                htmlItems.append("<p>").append(counter).append(". ").append(escapeHtml(name))
                        .append(" – [").append(escapeHtml(kitId.toString())).append("]</p>");
                textItems.append(counter).append(". ").append(name).append(" – [").append(kitId).append("]\n");
                counter++;
            }
        }

        String html = "<p>Dear " + escapeHtml(salutation) + ",</p>"
                + "<p>Greetings from Škoda Volkswagen India Pvt. Ltd.</p>"
                + "<p>This is to inform you that the following tool(s) issued to " + escapeHtml(trainerName) + " are currently <strong>overdue</strong>. Please review and take necessary action as per company policy.</p>"
                + "<p><strong>Trainer Name:</strong> " + escapeHtml(trainerName) + "</p>"
                + htmlItems.toString()
                + "<p><strong>Issuance ID:</strong> " + escapeHtml(issuanceId) + "<br/>"
                + "<strong>Issue Date:</strong> " + escapeHtml(issueDate) + "<br/>"
                + "<strong>Return Due Date:</strong> " + escapeHtml(returnDueDate) + "</p>"
                + "<p>Thank you.</p>"
                + "<p>Warm regards,<br/><strong>Tool Management Team</strong><br/>Škoda Volkswagen India Pvt. Ltd.</p>";

        String text = "Dear " + salutation + ",\n\n"
                + "Greetings from Škoda Volkswagen India Pvt. Ltd.\n\n"
                + "This is to inform you that the following tool(s) issued to " + trainerName + " are currently overdue. Please review and take necessary action as per company policy.\n\n"
                + "Trainer Name: " + trainerName + "\n\n"
                + textItems.toString()
                + "Issuance ID: " + issuanceId + "\n"
                + "Issue Date: " + issueDate + "\n"
                + "Return Due Date: " + returnDueDate + "\n\n"
                + "Thank you.\n\n"
                + "Warm regards,\nTool Management Team\nŠkoda Volkswagen India Pvt. Ltd.";

        // Try Brevo
        if (brevoApiKey != null && !brevoApiKey.isBlank()) {
            try {
                String url = "https://api.brevo.com/v3/smtp/email";
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("api-key", brevoApiKey);

                Map<String, Object> payload = new HashMap<>();
                Map<String, String> sender = new HashMap<>();
                sender.put("email", brevoSenderEmail);
                sender.put("name", brevoSenderName);
                payload.put("sender", sender);

                Map<String, String> toMap = new HashMap<>();
                toMap.put("email", adminEmail);
                payload.put("to", new Map[]{toMap});

                payload.put("subject", "Overdue Tool Alert - " + trainerName);
                payload.put("htmlContent", html);
                payload.put("textContent", text);

                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
                rest.postForEntity(url, entity, String.class);
                logger.info("Brevo overdue admin notification sent to {}", adminEmail);
                return;
            } catch (Exception e) {
                logger.warn("Brevo sendOverdueEmailToAdmin failed for {}: {}", adminEmail, e.getMessage());
            }
        }

        // Fallback to SMTP / plain text helper
        sendPlainTextEmail(adminEmail, "Overdue Tool Alert - " + trainerName, text);
    }

    /**
     * Send notification to admin about pending issuance request for approval
     */
    public void sendIssuanceRequestNotification(IssuanceRequest request, String adminEmail, String adminName) {
        if (adminEmail == null || adminEmail.isBlank()) return;
        if (request == null) return;

        String salutation = adminName != null && !adminName.isBlank() ? adminName : "Admin";
        String trainerName = request.getTrainerName() != null ? request.getTrainerName() : "Unknown Trainer";
        String requestId = request.getId() != null ? request.getId().toString() : "N/A";
        String requestDate = request.getRequestDate() != null ? request.getRequestDate().toString() : "N/A";

        // Build tool/kit list
        StringBuilder htmlItems = new StringBuilder();
        StringBuilder textItems = new StringBuilder();
        int counter = 1;

        if (request.getToolIds() != null) {
            for (Long toolId : request.getToolIds()) {
                Tool t = toolRepository != null ? toolRepository.findById(toolId).orElse(null) : null;
                String name = t != null ? t.getDescription() : "Tool " + toolId;
                String id = t != null ? t.getToolNo() : toolId.toString();
                htmlItems.append("<p>").append(counter).append(". ").append(escapeHtml(name))
                        .append(" – [").append(escapeHtml(id)).append("]</p>");
                textItems.append(counter).append(". ").append(name).append(" – [").append(id).append("]\n");
                counter++;
            }
        }

        if (request.getKitIds() != null) {
            for (Long kitId : request.getKitIds()) {
                Kit k = kitRepository != null ? kitRepository.findById(kitId).orElse(null) : null;
                String name = k != null ? k.getKitName() : "Kit " + kitId;
                htmlItems.append("<p>").append(counter).append(". ").append(escapeHtml(name))
                        .append(" – [").append(escapeHtml(kitId.toString())).append("]</p>");
                textItems.append(counter).append(". ").append(name).append(" – [").append(kitId).append("]\n");
                counter++;
            }
        }

        String html = "<p>Dear " + escapeHtml(salutation) + ",</p>"
                + "<p>Greetings from Škoda Volkswagen India Pvt. Ltd.</p>"
                + "<p>A new tool issuance request awaits your approval:</p>"
                + "<p><strong>Trainer Name:</strong> " + escapeHtml(trainerName) + "<br/>"
                + "<strong>Request ID:</strong> " + escapeHtml(requestId) + "<br/>"
                + "<strong>Request Date:</strong> " + escapeHtml(requestDate) + "</p>"
                + "<p><strong>Items Requested:</strong></p>"
                + htmlItems.toString()
                + "<p>Please review and approve or reject this request at your earliest convenience.</p>"
                + "<p>Warm regards,<br/><strong>Tool Management Team</strong><br/>Škoda Volkswagen India Pvt. Ltd.</p>";

        String text = "Dear " + salutation + ",\n\n"
                + "Greetings from Škoda Volkswagen India Pvt. Ltd.\n\n"
                + "A new tool issuance request awaits your approval:\n\n"
                + "Trainer Name: " + trainerName + "\n"
                + "Request ID: " + requestId + "\n"
                + "Request Date: " + requestDate + "\n\n"
                + "Items Requested:\n"
                + textItems.toString()
                + "\nPlease review and approve or reject this request at your earliest convenience.\n\n"
                + "Warm regards,\nTool Management Team\nŠkoda Volkswagen India Pvt. Ltd.";

        // Try Brevo
        if (brevoApiKey != null && !brevoApiKey.isBlank()) {
            try {
                String url = "https://api.brevo.com/v3/smtp/email";
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("api-key", brevoApiKey);

                Map<String, Object> payload = new HashMap<>();
                Map<String, String> sender = new HashMap<>();
                sender.put("email", brevoSenderEmail);
                sender.put("name", brevoSenderName);
                payload.put("sender", sender);

                Map<String, String> toMap = new HashMap<>();
                toMap.put("email", adminEmail);
                payload.put("to", new Map[]{toMap});

                payload.put("subject", "New Issuance Request - Approval Needed");
                payload.put("htmlContent", html);
                payload.put("textContent", text);

                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
                rest.postForEntity(url, entity, String.class);
                logger.info("Brevo issuance request notification sent to admin {}", adminEmail);
                return;
            } catch (Exception e) {
                logger.warn("Brevo sendIssuanceRequestNotification failed for {}: {}", adminEmail, e.getMessage());
            }
        }

        // Fallback to SMTP
        sendPlainTextEmail(adminEmail, "New Issuance Request - Approval Needed", text);
    }

    /**
     * Send approval notification to trainer when admin approves issuance request
     */
    public void sendIssuanceApprovalEmail(Issuance issuance, String trainerEmail, String trainerName) {
        if (trainerEmail == null || trainerEmail.isBlank()) return;
        if (issuance == null) return;

        String salutation = trainerName != null && !trainerName.isBlank() ? trainerName : "Trainer";
        String issuanceId = issuance.getId() != null ? issuance.getId().toString() : "N/A";
        String issuanceDate = issuance.getIssuanceDate() != null ? issuance.getIssuanceDate().toString() : "N/A";
        String returnDate = issuance.getReturnDate() != null ? issuance.getReturnDate().toString() : "N/A";
        String approvedBy = issuance.getApprovedBy() != null ? issuance.getApprovedBy() : "Admin";

        // Build tool/kit list
        StringBuilder htmlItems = new StringBuilder();
        StringBuilder textItems = new StringBuilder();
        int counter = 1;

        if (issuance.getToolIds() != null) {
            for (Long toolId : issuance.getToolIds()) {
                Tool t = toolRepository != null ? toolRepository.findById(toolId).orElse(null) : null;
                String name = t != null ? t.getDescription() : "Tool " + toolId;
                String id = t != null ? t.getToolNo() : toolId.toString();
                htmlItems.append("<p>").append(counter).append(". ").append(escapeHtml(name))
                        .append(" – [").append(escapeHtml(id)).append("]</p>");
                textItems.append(counter).append(". ").append(name).append(" – [").append(id).append("]\n");
                counter++;
            }
        }

        if (issuance.getKitIds() != null) {
            for (Long kitId : issuance.getKitIds()) {
                Kit k = kitRepository != null ? kitRepository.findById(kitId).orElse(null) : null;
                String name = k != null ? k.getKitName() : "Kit " + kitId;
                htmlItems.append("<p>").append(counter).append(". ").append(escapeHtml(name))
                        .append(" – [").append(escapeHtml(kitId.toString())).append("]</p>");
                textItems.append(counter).append(". ").append(name).append(" – [").append(kitId).append("]\n");
                counter++;
            }
        }

        String html = "<p>Dear " + escapeHtml(salutation) + ",</p>"
                + "<p>Greetings from Škoda Volkswagen India Pvt. Ltd.</p>"
                + "<p>Your tool issuance request has been <strong>APPROVED</strong> by " + escapeHtml(approvedBy) + ".</p>"
                + "<p><strong>Issuance Details:</strong><br/>"
                + "<strong>Issuance ID:</strong> " + escapeHtml(issuanceId) + "<br/>"
                + "<strong>Issuance Date:</strong> " + escapeHtml(issuanceDate) + "<br/>"
                + "<strong>Expected Return Date:</strong> " + escapeHtml(returnDate) + "</p>"
                + "<p><strong>Items Approved:</strong></p>"
                + htmlItems.toString()
                + "<p>Please ensure the tools/kits are returned by the expected return date.</p>"
                + "<p>Warm regards,<br/><strong>Tool Management Team</strong><br/>Škoda Volkswagen India Pvt. Ltd.</p>";

        String text = "Dear " + salutation + ",\n\n"
                + "Greetings from Škoda Volkswagen India Pvt. Ltd.\n\n"
                + "Your tool issuance request has been APPROVED by " + approvedBy + ".\n\n"
                + "Issuance Details:\n"
                + "Issuance ID: " + issuanceId + "\n"
                + "Issuance Date: " + issuanceDate + "\n"
                + "Expected Return Date: " + returnDate + "\n\n"
                + "Items Approved:\n"
                + textItems.toString()
                + "\nPlease ensure the tools/kits are returned by the expected return date.\n\n"
                + "Warm regards,\nTool Management Team\nŠkoda Volkswagen India Pvt. Ltd.";

        // Try Brevo
        if (brevoApiKey != null && !brevoApiKey.isBlank()) {
            try {
                String url = "https://api.brevo.com/v3/smtp/email";
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("api-key", brevoApiKey);

                Map<String, Object> payload = new HashMap<>();
                Map<String, String> sender = new HashMap<>();
                sender.put("email", brevoSenderEmail);
                sender.put("name", brevoSenderName);
                payload.put("sender", sender);

                Map<String, String> toMap = new HashMap<>();
                toMap.put("email", trainerEmail);
                payload.put("to", new Map[]{toMap});

                payload.put("subject", "Your Issuance Request Approved");
                payload.put("htmlContent", html);
                payload.put("textContent", text);

                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
                rest.postForEntity(url, entity, String.class);
                logger.info("Brevo issuance approval notification sent to trainer {}", trainerEmail);
                return;
            } catch (Exception e) {
                logger.warn("Brevo sendIssuanceApprovalEmail failed for {}: {}", trainerEmail, e.getMessage());
            }
        }

        // Fallback to SMTP
        sendPlainTextEmail(trainerEmail, "Your Issuance Request Approved", text);
    }

    /**
     * Send rejection notification to trainer when admin rejects issuance request
     */
    public void sendIssuanceRejectionEmail(IssuanceRequest request, String trainerEmail, String trainerName) {
        if (trainerEmail == null || trainerEmail.isBlank()) return;
        if (request == null) return;

        String salutation = trainerName != null && !trainerName.isBlank() ? trainerName : "Trainer";
        String requestId = request.getId() != null ? request.getId().toString() : "N/A";
        String rejectedBy = request.getApprovedBy() != null ? request.getApprovedBy() : "Admin";
        String rejectionReason = request.getApprovalRemark() != null ? request.getApprovalRemark() : "No reason provided";

        String html = "<p>Dear " + escapeHtml(salutation) + ",</p>"
                + "<p>Greetings from Škoda Volkswagen India Pvt. Ltd.</p>"
                + "<p>Unfortunately, your tool issuance request has been <strong>REJECTED</strong>.</p>"
                + "<p><strong>Request ID:</strong> " + escapeHtml(requestId) + "<br/>"
                + "<strong>Rejected By:</strong> " + escapeHtml(rejectedBy) + "<br/>"
                + "<strong>Reason:</strong> " + escapeHtml(rejectionReason) + "</p>"
                + "<p>Please contact the admin if you have any questions or need further assistance.</p>"
                + "<p>Warm regards,<br/><strong>Tool Management Team</strong><br/>Škoda Volkswagen India Pvt. Ltd.</p>";

        String text = "Dear " + salutation + ",\n\n"
                + "Greetings from Škoda Volkswagen India Pvt. Ltd.\n\n"
                + "Unfortunately, your tool issuance request has been REJECTED.\n\n"
                + "Request ID: " + requestId + "\n"
                + "Rejected By: " + rejectedBy + "\n"
                + "Reason: " + rejectionReason + "\n\n"
                + "Please contact the admin if you have any questions or need further assistance.\n\n"
                + "Warm regards,\nTool Management Team\nŠkoda Volkswagen India Pvt. Ltd.";

        // Try Brevo
        if (brevoApiKey != null && !brevoApiKey.isBlank()) {
            try {
                String url = "https://api.brevo.com/v3/smtp/email";
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("api-key", brevoApiKey);

                Map<String, Object> payload = new HashMap<>();
                Map<String, String> sender = new HashMap<>();
                sender.put("email", brevoSenderEmail);
                sender.put("name", brevoSenderName);
                payload.put("sender", sender);

                Map<String, String> toMap = new HashMap<>();
                toMap.put("email", trainerEmail);
                payload.put("to", new Map[]{toMap});

                payload.put("subject", "Your Issuance Request Rejected");
                payload.put("htmlContent", html);
                payload.put("textContent", text);

                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
                rest.postForEntity(url, entity, String.class);
                logger.info("Brevo issuance rejection notification sent to trainer {}", trainerEmail);
                return;
            } catch (Exception e) {
                logger.warn("Brevo sendIssuanceRejectionEmail failed for {}: {}", trainerEmail, e.getMessage());
            }
        }

        // Fallback to SMTP
        sendPlainTextEmail(trainerEmail, "Your Issuance Request Rejected", text);
    }
}
