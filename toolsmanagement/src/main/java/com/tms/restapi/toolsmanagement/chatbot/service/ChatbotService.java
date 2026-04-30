package com.tms.restapi.toolsmanagement.chatbot.service;

import com.tms.restapi.toolsmanagement.chatbot.dto.ChatbotQADTO;
import com.tms.restapi.toolsmanagement.chatbot.dto.ChatbotResponseDTO;
import com.tms.restapi.toolsmanagement.chatbot.model.ChatbotQA;
import com.tms.restapi.toolsmanagement.chatbot.repository.ChatbotQARepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * ChatbotService — Intent-Based Dynamic Query Engine
 *
 * Processing pipeline:
 *   1. Exact match  → chatbot_qa table
 *   2. Keyword match → chatbot_qa table  (LIKE on question)
 *   3. Intent detection → classify query type from keywords
 *   4. Entity extraction → pull location / tool name / trainer name / status
 *   5. Targeted SQL → run the right query with extracted entities
 *   6. Natural-language formatting → produce readable answer
 *
 * Tables accessed: tools, issuance_requests, trainers, admins, kits, kit_tools
 */
@Service
@Transactional
public class ChatbotService {

    private static final Logger logger = LoggerFactory.getLogger(ChatbotService.class);

    @Autowired
    private ChatbotQARepository chatbotQARepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // ─────────────────────────────────────────────────────────────
    //  PUBLIC ENTRY POINT
    // ─────────────────────────────────────────────────────────────

    /**
     * Main entry point — processes user query and returns an answer.
     */
    public ChatbotResponseDTO processQuery(String query) {
        long startTime = System.currentTimeMillis();

        if (query == null || query.trim().isEmpty()) {
            return new ChatbotResponseDTO(
                query, false,
                "Query cannot be empty. Please ask a question about tools, issuances, kits, or trainers."
            );
        }

        String trimmed = query.trim();

        // ── Step 1: Exact match in predefined Q&A ──────────────────
        Optional<ChatbotQA> exactMatch = chatbotQARepository.findByQuestionExact(trimmed);
        if (exactMatch.isPresent()) {
            return new ChatbotResponseDTO(
                trimmed,
                exactMatch.get().getAnswer(),
                "predefined",
                System.currentTimeMillis() - startTime,
                true
            );
        }

        // ── Step 2: Keyword/partial match in predefined Q&A ────────
        List<ChatbotQA> partialMatches = chatbotQARepository.findByQuestionKeyword(trimmed);
        if (!partialMatches.isEmpty()) {
            return new ChatbotResponseDTO(
                trimmed,
                partialMatches.get(0).getAnswer(),
                "predefined",
                System.currentTimeMillis() - startTime,
                true
            );
        }

        // ── Step 3: Intent-based dynamic database search ───────────
        String dynamicAnswer = performDynamicSearch(trimmed);
        if (dynamicAnswer != null && !dynamicAnswer.isEmpty()) {
            return new ChatbotResponseDTO(
                trimmed,
                dynamicAnswer,
                "dynamic",
                System.currentTimeMillis() - startTime,
                true
            );
        }

        // ── Step 4: Nothing found ───────────────────────────────────
        return new ChatbotResponseDTO(
            trimmed, false,
            "I couldn't find relevant information for: \"" + trimmed + "\". " +
            "Try asking about tool availability, issuance status, kits, trainers, " +
            "calibration, or counts — e.g. \"Is hammer available?\", " +
            "\"Show pending requests\", \"How many tools at Pune?\""
        );
    }

    // ─────────────────────────────────────────────────────────────
    //  INTENT ROUTER
    // ─────────────────────────────────────────────────────────────

    /**
     * Classifies the user query into an intent and dispatches
     * to the appropriate handler.  Order matters — more specific
     * intents must come before broad ones.
     */
    private String performDynamicSearch(String query) {
        String q = query.toLowerCase().trim();

        // ── Calibration intents ────────────────────────────────────
        if (matchesAny(q, "calibration due", "due for calibration", "calibration overdue",
                       "overdue calibration", "needs calibration", "calibration needed",
                       "which tools need calibration", "calibration expir")) {
            return handleCalibrationDue(q);
        }
        if (matchesAny(q, "calibration", "calibrate", "calibrated")) {
            return handleCalibrationQuery(q);
        }

        // ── Tool availability / stock intents ──────────────────────
        if (matchesAny(q, "available", "availability", "in stock", "out of stock",
                       "not available", "how many available")) {
            return handleToolAvailability(q);
        }

        // ── Tool condition intents ─────────────────────────────────
        if (matchesAny(q, "damaged", "good condition", "poor condition", "condition of",
                       "tool condition", "broken")) {
            return handleToolCondition(q);
        }

        // ── Tool location intents ──────────────────────────────────
        if (matchesAny(q, "where is", "location of", "where can i find", "rack of",
                       "stored at", "shelf of")) {
            return handleToolLocation(q);
        }

        // ── Most used / top tools ──────────────────────────────────
        if (matchesAny(q, "most used", "most issued", "top tool", "frequently used",
                       "highest issue", "popular tool")) {
            return handleMostUsedTools(q);
        }

        // ── Tool detail / info ─────────────────────────────────────
        if (matchesAny(q, "tell me about", "details of", "info about", "information about",
                       "describe", "what is", "show tool")) {
            return handleToolInfo(q);
        }

        // ── Kit intents ────────────────────────────────────────────
        if (matchesAny(q, "kit")) {
            return handleKitQuery(q);
        }

        // ── Issuance / request intents ─────────────────────────────
        if (matchesAny(q, "issuance", "issued to", "issued by", "pending request",
                       "approved request", "rejected request", "returned request",
                       "my request", "request status", "issue request",
                       "pending issuance", "approved issuance")) {
            return handleIssuanceQuery(q);
        }

        // ── Trainer intents ────────────────────────────────────────
        if (matchesAny(q, "trainer", "training")) {
            return handleTrainerQuery(q);
        }

        // ── Admin intents ──────────────────────────────────────────
        if (matchesAny(q, "admin", "administrator")) {
            return handleAdminQuery(q);
        }

        // ── Count / stats intents ──────────────────────────────────
        if (matchesAny(q, "how many", "total", "count", "number of", "statistics",
                       "summary", "overview")) {
            return handleCountQuery(q);
        }

        // ── Location-based tool list ───────────────────────────────
        if (matchesAny(q, "tools at", "tools in", "tools available at", "tools in location")) {
            return handleToolsByLocation(q);
        }

        // ── Last-resort: generic tool search by name ────────────────
        return handleGenericToolSearch(q);
    }

    // ─────────────────────────────────────────────────────────────
    //  INTENT HANDLERS
    // ─────────────────────────────────────────────────────────────

    /**
     * INTENT: Tool Availability
     * Examples: "Is hammer available?", "availability of vernier caliper",
     *           "how many drills are available at Pune?"
     */
    private String handleToolAvailability(String q) {
        try {
            String location = extractLocation(q);
            String toolKeyword = extractToolKeyword(q,
                "available", "availability", "in stock", "out of stock",
                "how many", "is", "are", "of", "at", "in", "the");

            // No specific tool — general availability stats
            if (toolKeyword.isEmpty()) {
                if (location != null) {
                    Map<String, Object> stats = jdbcTemplate.queryForMap(
                        "SELECT COUNT(*) AS total, " +
                        "SUM(CASE WHEN availability > 0 THEN 1 ELSE 0 END) AS avail " +
                        "FROM tools WHERE LOWER(location) = ?",
                        location.toLowerCase()
                    );
                    long total = toLong(stats.get("total"));
                    long avail = toLong(stats.get("avail"));
                    return String.format(
                        "Tools at %s — Total: %d | Available: %d | Not Available: %d",
                        capitalize(location), total, avail, total - avail
                    );
                }
                // Global stats
                Map<String, Object> stats = jdbcTemplate.queryForMap(
                    "SELECT COUNT(*) AS total, " +
                    "SUM(CASE WHEN availability > 0 THEN 1 ELSE 0 END) AS avail, " +
                    "SUM(CASE WHEN availability = 0 THEN 1 ELSE 0 END) AS not_avail " +
                    "FROM tools"
                );
                return String.format(
                    "Tool Availability Overview — Total: %d | Available: %d | Not Available: %d",
                    toLong(stats.get("total")),
                    toLong(stats.get("avail")),
                    toLong(stats.get("not_avail"))
                );
            }

            // Specific tool keyword supplied
            String locationClause = location != null ? " AND LOWER(location) = '" + location.toLowerCase() + "'" : "";
            List<Map<String, Object>> results = jdbcTemplate.queryForList(
                "SELECT description, tool_no, si_no, availability, quantity, location, tool_condition " +
                "FROM tools WHERE LOWER(description) LIKE ?" + locationClause + " LIMIT 8",
                "%" + toolKeyword + "%"
            );

            if (results.isEmpty()) {
                return "No tools found matching '" + toolKeyword + "'" +
                       (location != null ? " at location '" + capitalize(location) + "'" : "") +
                       ". Please verify the tool name.";
            }

            StringBuilder sb = new StringBuilder();
            sb.append("Availability for '").append(toolKeyword).append("'");
            if (location != null) sb.append(" at ").append(capitalize(location));
            sb.append(":\n");
            for (Map<String, Object> t : results) {
                int avail = toInt(t.get("availability"));
                int qty   = toInt(t.get("quantity"));
                String status = avail > 0
                    ? "✅ Available (" + avail + "/" + qty + " units)"
                    : "❌ Not Available";
                sb.append(String.format(
                    "• %s | Tool No: %s | SI No: %s | %s | Location: %s | Condition: %s\n",
                    t.get("description"), t.get("tool_no"), t.get("si_no"),
                    status, t.get("location"), t.get("tool_condition")
                ));
            }
            return sb.toString().trim();

        } catch (Exception e) {
            logger.warn("handleToolAvailability error: {}", e.getMessage());
            return null;
        }
    }

    /**
     * INTENT: Tool Information / Details
     * Examples: "Tell me about vernier caliper", "details of multimeter",
     *           "what is torque wrench"
     */
    private String handleToolInfo(String q) {
        try {
            String toolKeyword = extractToolKeyword(q,
                "tell me about", "details of", "info about", "information about",
                "describe", "what is", "show tool", "the", "a", "an");

            if (toolKeyword.isEmpty()) return null;

            List<Map<String, Object>> results = jdbcTemplate.queryForList(
                "SELECT description, tool_no, si_no, tool_location, location, quantity, " +
                "availability, tool_condition, calibration_required, last_calibration_date, " +
                "next_calibration_date, last_borrowed_by, issue_count, remark " +
                "FROM tools WHERE LOWER(description) LIKE ? LIMIT 5",
                "%" + toolKeyword + "%"
            );

            if (results.isEmpty()) {
                return "No tool found matching '" + toolKeyword + "'. Please check the name.";
            }

            StringBuilder sb = new StringBuilder();
            for (Map<String, Object> t : results) {
                int avail = toInt(t.get("availability"));
                sb.append("🔧 ").append(t.get("description")).append("\n");
                sb.append("   Tool No       : ").append(t.get("tool_no")).append("\n");
                sb.append("   SI No         : ").append(t.get("si_no")).append("\n");
                sb.append("   Plant Location: ").append(t.get("location")).append("\n");
                sb.append("   Rack/Shelf    : ").append(nvl(t.get("tool_location"))).append("\n");
                sb.append("   Total Qty     : ").append(t.get("quantity")).append("\n");
                sb.append("   Availability  : ").append(avail > 0 ? avail + " units available ✅" : "Not Available ❌").append("\n");
                sb.append("   Condition     : ").append(nvl(t.get("tool_condition"))).append("\n");
                sb.append("   Calibration   : ").append(boolStr(t.get("calibration_required"))).append("\n");
                if (t.get("next_calibration_date") != null) {
                    sb.append("   Next Calib.   : ").append(t.get("next_calibration_date")).append("\n");
                }
                sb.append("   Times Issued  : ").append(nvl(t.get("issue_count"), "0")).append("\n");
                if (t.get("last_borrowed_by") != null) {
                    sb.append("   Last Used By  : ").append(t.get("last_borrowed_by")).append("\n");
                }
                if (t.get("remark") != null) {
                    sb.append("   Remark        : ").append(t.get("remark")).append("\n");
                }
                sb.append("\n");
            }
            return sb.toString().trim();

        } catch (Exception e) {
            logger.warn("handleToolInfo error: {}", e.getMessage());
            return null;
        }
    }

    /**
     * INTENT: Tool Location
     * Examples: "Where is the hammer?", "location of vernier caliper"
     */
    private String handleToolLocation(String q) {
        try {
            String toolKeyword = extractToolKeyword(q,
                "where is", "location of", "where can i find",
                "rack of", "stored at", "shelf of", "the", "a", "an");

            if (toolKeyword.isEmpty()) return null;

            List<Map<String, Object>> results = jdbcTemplate.queryForList(
                "SELECT description, tool_no, tool_location, location, availability " +
                "FROM tools WHERE LOWER(description) LIKE ? LIMIT 5",
                "%" + toolKeyword + "%"
            );

            if (results.isEmpty()) {
                return "No tool found matching '" + toolKeyword + "'.";
            }

            StringBuilder sb = new StringBuilder("Location details for '" + toolKeyword + "':\n");
            for (Map<String, Object> t : results) {
                int avail = toInt(t.get("availability"));
                sb.append(String.format(
                    "• %s | Tool No: %s | Plant: %s | Rack/Shelf: %s | %s\n",
                    t.get("description"), t.get("tool_no"), t.get("location"),
                    nvl(t.get("tool_location"), "Not specified"),
                    avail > 0 ? "Available ✅" : "Currently Issued ❌"
                ));
            }
            return sb.toString().trim();

        } catch (Exception e) {
            logger.warn("handleToolLocation error: {}", e.getMessage());
            return null;
        }
    }

    /**
     * INTENT: Tool Condition
     * Examples: "show damaged tools", "tools in good condition", "condition of hammer"
     */
    private String handleToolCondition(String q) {
        try {
            String conditionFilter = null;
            if (q.contains("damaged") || q.contains("broken")) conditionFilter = "Damaged";
            else if (q.contains("good")) conditionFilter = "Good";
            else if (q.contains("poor")) conditionFilter = "Poor";
            else if (q.contains("fair")) conditionFilter = "Fair";

            String toolKeyword = extractToolKeyword(q,
                "condition of", "damaged", "good condition", "poor condition",
                "broken", "show", "list", "the", "a", "all");

            List<Map<String, Object>> results;
            StringBuilder sb = new StringBuilder();

            if (conditionFilter != null && toolKeyword.isEmpty()) {
                results = jdbcTemplate.queryForList(
                    "SELECT description, tool_no, tool_condition, location, availability " +
                    "FROM tools WHERE LOWER(tool_condition) = ? ORDER BY description LIMIT 10",
                    conditionFilter.toLowerCase()
                );
                sb.append("Tools with condition '").append(conditionFilter).append("':\n");
            } else if (!toolKeyword.isEmpty()) {
                results = jdbcTemplate.queryForList(
                    "SELECT description, tool_no, tool_condition, location, availability " +
                    "FROM tools WHERE LOWER(description) LIKE ? LIMIT 5",
                    "%" + toolKeyword + "%"
                );
                sb.append("Condition details for '").append(toolKeyword).append("':\n");
            } else {
                // Summary of conditions
                List<Map<String, Object>> summary = jdbcTemplate.queryForList(
                    "SELECT tool_condition, COUNT(*) AS cnt FROM tools " +
                    "GROUP BY tool_condition ORDER BY cnt DESC"
                );
                sb.append("Tool Condition Summary:\n");
                for (Map<String, Object> row : summary) {
                    sb.append(String.format("• %s: %d tools\n",
                        nvl(row.get("tool_condition"), "Unknown"), toLong(row.get("cnt"))));
                }
                return sb.toString().trim();
            }

            if (results.isEmpty()) return "No tools found for the requested condition.";

            for (Map<String, Object> t : results) {
                sb.append(String.format(
                    "• %s | Tool No: %s | Condition: %s | Location: %s | %s\n",
                    t.get("description"), t.get("tool_no"),
                    nvl(t.get("tool_condition"), "N/A"), t.get("location"),
                    toInt(t.get("availability")) > 0 ? "Available ✅" : "Not Available ❌"
                ));
            }
            return sb.toString().trim();

        } catch (Exception e) {
            logger.warn("handleToolCondition error: {}", e.getMessage());
            return null;
        }
    }

    /**
     * INTENT: Tools by Location
     * Examples: "tools at Pune", "list tools in Mumbai"
     */
    private String handleToolsByLocation(String q) {
        try {
            String location = extractLocation(q);
            if (location == null) {
                // List all unique locations
                List<Map<String, Object>> locs = jdbcTemplate.queryForList(
                    "SELECT location, COUNT(*) AS cnt FROM tools GROUP BY location ORDER BY cnt DESC"
                );
                StringBuilder sb = new StringBuilder("Available plant locations:\n");
                for (Map<String, Object> l : locs) {
                    sb.append(String.format("• %s — %d tools\n", l.get("location"), toLong(l.get("cnt"))));
                }
                return sb.toString().trim();
            }

            List<Map<String, Object>> results = jdbcTemplate.queryForList(
                "SELECT description, tool_no, availability, tool_condition " +
                "FROM tools WHERE LOWER(location) = ? ORDER BY description LIMIT 15",
                location.toLowerCase()
            );

            if (results.isEmpty()) {
                return "No tools found at location '" + capitalize(location) + "'.";
            }

            StringBuilder sb = new StringBuilder("Tools at " + capitalize(location) + ":\n");
            for (Map<String, Object> t : results) {
                sb.append(String.format(
                    "• %s | Tool No: %s | %s | Condition: %s\n",
                    t.get("description"), t.get("tool_no"),
                    toInt(t.get("availability")) > 0 ? "Available ✅" : "Not Available ❌",
                    nvl(t.get("tool_condition"))
                ));
            }
            return sb.toString().trim();

        } catch (Exception e) {
            logger.warn("handleToolsByLocation error: {}", e.getMessage());
            return null;
        }
    }

    /**
     * INTENT: Most Used / Top Tools
     * Examples: "most used tools", "top 5 most issued tools"
     */
    private String handleMostUsedTools(String q) {
        try {
            int limit = 5;
            if (q.contains("top 10") || q.contains("10 most")) limit = 10;
            else if (q.contains("top 3") || q.contains("3 most")) limit = 3;

            String location = extractLocation(q);
            String locationClause = location != null
                ? "WHERE LOWER(location) = '" + location.toLowerCase() + "' " : "";

            List<Map<String, Object>> results = jdbcTemplate.queryForList(
                "SELECT description, tool_no, issue_count, location, availability " +
                "FROM tools " + locationClause +
                "ORDER BY issue_count DESC LIMIT ?",
                limit
            );

            if (results.isEmpty()) return "No tool usage data found.";

            StringBuilder sb = new StringBuilder("Top " + limit + " Most Used Tools");
            if (location != null) sb.append(" at ").append(capitalize(location));
            sb.append(":\n");

            int rank = 1;
            for (Map<String, Object> t : results) {
                sb.append(String.format(
                    "%d. %s | Tool No: %s | Issued %s times | %s\n",
                    rank++, t.get("description"), t.get("tool_no"),
                    nvl(t.get("issue_count"), "0"),
                    toInt(t.get("availability")) > 0 ? "Available ✅" : "Currently Issued ❌"
                ));
            }
            return sb.toString().trim();

        } catch (Exception e) {
            logger.warn("handleMostUsedTools error: {}", e.getMessage());
            return null;
        }
    }

    /**
     * INTENT: Calibration Due / Overdue
     * Examples: "which tools need calibration?", "calibration overdue tools"
     */
    private String handleCalibrationDue(String q) {
        try {
            boolean overdue = matchesAny(q, "overdue", "expired", "past due", "missed");
            String location = extractLocation(q);

            String dateCondition = overdue
                ? "next_calibration_date < CURDATE()"
                : "next_calibration_date <= DATE_ADD(CURDATE(), INTERVAL 30 DAY)";

            String locationClause = location != null
                ? " AND LOWER(location) = '" + location.toLowerCase() + "'" : "";

            List<Map<String, Object>> results = jdbcTemplate.queryForList(
                "SELECT description, tool_no, location, last_calibration_date, " +
                "next_calibration_date, tool_condition " +
                "FROM tools WHERE calibration_required = true AND " + dateCondition +
                locationClause + " ORDER BY next_calibration_date ASC LIMIT 10"
            );

            if (results.isEmpty()) {
                return overdue
                    ? "Great news! No overdue calibrations found" + (location != null ? " at " + capitalize(location) : "") + "."
                    : "No calibrations due in the next 30 days" + (location != null ? " at " + capitalize(location) : "") + ".";
            }

            StringBuilder sb = new StringBuilder();
            sb.append(overdue ? "⚠️ Overdue Calibrations" : "🔔 Calibrations Due (Next 30 Days)");
            if (location != null) sb.append(" at ").append(capitalize(location));
            sb.append(":\n");

            for (Map<String, Object> t : results) {
                sb.append(String.format(
                    "• %s | Tool No: %s | Due: %s | Last: %s | Location: %s\n",
                    t.get("description"), t.get("tool_no"),
                    nvl(t.get("next_calibration_date"), "Not set"),
                    nvl(t.get("last_calibration_date"), "Never"),
                    t.get("location")
                ));
            }
            return sb.toString().trim();

        } catch (Exception e) {
            logger.warn("handleCalibrationDue error: {}", e.getMessage());
            return null;
        }
    }

    /**
     * INTENT: General Calibration Info
     * Examples: "calibration info for vernier", "is multimeter calibrated?"
     */
    private String handleCalibrationQuery(String q) {
        try {
            // Specific tool calibration info
            String toolKeyword = extractToolKeyword(q,
                "calibration", "calibrate", "calibrated", "of", "for", "is", "the");

            if (!toolKeyword.isEmpty()) {
                List<Map<String, Object>> results = jdbcTemplate.queryForList(
                    "SELECT description, tool_no, calibration_required, calibration_period_months, " +
                    "last_calibration_date, next_calibration_date " +
                    "FROM tools WHERE LOWER(description) LIKE ? LIMIT 5",
                    "%" + toolKeyword + "%"
                );

                if (results.isEmpty()) return "No tool found matching '" + toolKeyword + "' for calibration info.";

                StringBuilder sb = new StringBuilder("Calibration info for '" + toolKeyword + "':\n");
                for (Map<String, Object> t : results) {
                    boolean required = "1".equals(String.valueOf(t.get("calibration_required")))
                        || Boolean.TRUE.equals(t.get("calibration_required"));
                    sb.append(String.format(
                        "• %s | Tool No: %s | Calibration Required: %s | Period: %s months " +
                        "| Last: %s | Next: %s\n",
                        t.get("description"), t.get("tool_no"),
                        required ? "Yes" : "No",
                        nvl(t.get("calibration_period_months"), "N/A"),
                        nvl(t.get("last_calibration_date"), "Never"),
                        nvl(t.get("next_calibration_date"), "Not scheduled")
                    ));
                }
                return sb.toString().trim();
            }

            // Summary of all calibration-required tools
            Map<String, Object> summary = jdbcTemplate.queryForMap(
                "SELECT COUNT(*) AS total_requiring, " +
                "SUM(CASE WHEN next_calibration_date < CURDATE() THEN 1 ELSE 0 END) AS overdue, " +
                "SUM(CASE WHEN next_calibration_date BETWEEN CURDATE() " +
                "    AND DATE_ADD(CURDATE(), INTERVAL 30 DAY) THEN 1 ELSE 0 END) AS due_soon " +
                "FROM tools WHERE calibration_required = true"
            );
            return String.format(
                "Calibration Summary — Tools requiring calibration: %d | Overdue: %d | Due in 30 days: %d",
                toLong(summary.get("total_requiring")),
                toLong(summary.get("overdue")),
                toLong(summary.get("due_soon"))
            );

        } catch (Exception e) {
            logger.warn("handleCalibrationQuery error: {}", e.getMessage());
            return null;
        }
    }

    /**
     * INTENT: Issuance / Request Status
     * Examples: "show pending requests", "issued to Ravi", "approved issuances today"
     */
    private String handleIssuanceQuery(String q) {
        try {
            // Detect status filter
            String statusFilter = null;
            if (q.matches(".*\\bpending\\b.*")) {
                statusFilter = "Pending";
            } else if (q.matches(".*\\bapproved\\b.*") || q.matches(".*\\bapprove\\b.*")) {
                statusFilter = "Approved";
            } else if (q.matches(".*\\brejected\\b.*") || q.matches(".*\\breject\\b.*")) {
                statusFilter = "Rejected";
            } else if (q.matches(".*\\breturned\\b.*") || q.matches(".*\\breturn\\b.*")) {
                statusFilter = "Returned";
            }

            // Detect trainer name ("issued to <name>")
            String trainerKeyword = null;
            if (q.contains("issued to")) {
                trainerKeyword = q.substring(q.indexOf("issued to") + 9).trim()
                    .replaceAll("[?.,!]", "").trim();
            } else if (q.contains("for trainer")) {
                trainerKeyword = q.substring(q.indexOf("for trainer") + 11).trim()
                    .replaceAll("[?.,!]", "").trim();
            } else if (q.contains("by trainer")) {
                trainerKeyword = q.substring(q.indexOf("by trainer") + 10).trim()
                    .replaceAll("[?.,!]", "").trim();
            }

            String location = extractLocation(q);
            boolean isToday = matchesAny(q, "today");
            boolean isTypeKit = matchesAny(q, "kit issuance", "kit request");
            boolean isTypeTool = matchesAny(q, "tool issuance", "tool request");

            // Build dynamic query
            StringBuilder sql = new StringBuilder(
                "SELECT trainer_name, training_name, status, issuance_type, " +
                "issuance_date, return_date, location, approved_by " +
                "FROM issuance_requests WHERE 1=1"
            );

            if (statusFilter != null) sql.append(" AND LOWER(status) = '").append(statusFilter.toLowerCase()).append("'");
            if (trainerKeyword != null && !trainerKeyword.isEmpty())
                sql.append(" AND LOWER(trainer_name) LIKE '%").append(trainerKeyword.toLowerCase()).append("%'");
            if (location != null) sql.append(" AND LOWER(location) = '").append(location.toLowerCase()).append("'");
            if (isToday) sql.append(" AND DATE(issuance_date) = CURDATE()");
            if (isTypeKit) sql.append(" AND LOWER(issuance_type) = 'kit'");
            if (isTypeTool) sql.append(" AND LOWER(issuance_type) = 'tool'");

            sql.append(" ORDER BY issuance_date DESC LIMIT 10");

            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql.toString());

            if (results.isEmpty()) {
                String desc = statusFilter != null ? statusFilter.toLowerCase() : "matching";
                return "No " + desc + " issuance records found" +
                       (trainerKeyword != null ? " for trainer '" + trainerKeyword + "'" : "") +
                       (location != null ? " at " + capitalize(location) : "") + ".";
            }

            StringBuilder sb = new StringBuilder();
            sb.append("Issuance Records");
            if (statusFilter != null) sb.append(" (").append(statusFilter).append(")");
            if (trainerKeyword != null) sb.append(" — Trainer: ").append(trainerKeyword);
            if (location != null) sb.append(" at ").append(capitalize(location));
            sb.append(":\n");

            for (Map<String, Object> r : results) {
                sb.append(String.format(
                    "• Trainer: %s | Training: %s | Type: %s | Status: %s | Date: %s | Location: %s%s\n",
                    nvl(r.get("trainer_name")), nvl(r.get("training_name")),
                    nvl(r.get("issuance_type")), nvl(r.get("status")),
                    formatDate(r.get("issuance_date")),
                    nvl(r.get("location")),
                    r.get("approved_by") != null ? " | Approved by: " + r.get("approved_by") : ""
                ));
            }
            return sb.toString().trim();

        } catch (Exception e) {
            logger.warn("handleIssuanceQuery error: {}", e.getMessage());
            return null;
        }
    }

    /**
     * INTENT: Kit Queries
     * Examples: "list all kits", "is kit KIT-001 available?", "what tools are in kit A?"
     */
    private String handleKitQuery(String q) {
        try {

            // ── BOOLEAN: "any kit available?" ─────────────────────
            if (q.contains("any") && q.contains("available")) {
                String location = extractLocation(q);

                String sql = "SELECT COUNT(*) FROM kits WHERE availability > 0";

                if (location != null) {
                    sql += " AND LOWER(location) = ?";
                    Long count = jdbcTemplate.queryForObject(sql, Long.class, location.toLowerCase());

                    return count > 0
                            ? "Yes, kits are available at " + capitalize(location) + "."
                            : "No kits are currently available at " + capitalize(location) + ".";
                } else {
                    Long count = jdbcTemplate.queryForObject(sql, Long.class);

                    return count > 0
                            ? "Yes, kits are available."
                            : "No kits are currently available.";
                }
            }

            // ── KIT AVAILABILITY LIST ──────────────────────────────
            if (q.contains("available") || q.contains("availability")) {
                String location = extractLocation(q);

                String sql = "SELECT kit_id, kit_name, training_name, qualification_level, " +
                        "availability, location, kit_condition FROM kits WHERE 1=1";

                List<Map<String, Object>> results;

                if (location != null) {
                    sql += " AND LOWER(location) = ?";
                    sql += " LIMIT 10";
                    results = jdbcTemplate.queryForList(sql, location.toLowerCase());
                } else {
                    sql += " LIMIT 10";
                    results = jdbcTemplate.queryForList(sql);
                }

                if (results.isEmpty()) {
                    return "No kits found" +
                            (location != null ? " at " + capitalize(location) : "") + ".";
                }

                StringBuilder sb = new StringBuilder("Kit Availability:\n");

                for (Map<String, Object> k : results) {
                    int avail = toInt(k.get("availability"));

                    sb.append(String.format(
                            "• %s | Name: %s | Training: %s | %s | Location: %s | Condition: %s\n",
                            k.get("kit_id"),
                            nvl(k.get("kit_name")),
                            nvl(k.get("training_name")),
                            avail > 0 ? "✅ Available" : "❌ Not Available",
                            nvl(k.get("location")),
                            nvl(k.get("kit_condition"))));
                }

                return sb.toString().trim();
            }

            // ── KIT CONTENTS ───────────────────────────────────────
            if (matchesAny(q, "what is in", "contents of", "tools in kit", "what tools")) {

                String kitKeyword = extractToolKeyword(q,
                        "what is in", "contents of", "tools in kit", "tools in",
                        "what tools are in", "what tools in", "kit", "the", "a");

                if (!kitKeyword.isEmpty()) {

                    List<Map<String, Object>> results = jdbcTemplate.queryForList(
                            "SELECT t.description, t.tool_no, t.availability, t.tool_condition " +
                                    "FROM tools t " +
                                    "INNER JOIN kit_tools kt ON kt.tool_id_fk = t.id " +
                                    "INNER JOIN kits k ON k.id = kt.kit_id_fk " +
                                    "WHERE LOWER(k.kit_id) LIKE ? OR LOWER(k.kit_name) LIKE ? LIMIT 15",
                            "%" + kitKeyword.toLowerCase() + "%",
                            "%" + kitKeyword.toLowerCase() + "%");

                    if (results.isEmpty()) {
                        return "No kit found matching '" + kitKeyword + "' or the kit has no tools.";
                    }

                    StringBuilder sb = new StringBuilder("Tools in kit '" + kitKeyword + "':\n");

                    for (Map<String, Object> t : results) {
                        sb.append(String.format(
                                "• %s | Tool No: %s | %s | Condition: %s\n",
                                t.get("description"),
                                t.get("tool_no"),
                                toInt(t.get("availability")) > 0 ? "Available ✅" : "Not Available ❌",
                                nvl(t.get("tool_condition"))));
                    }

                    return sb.toString().trim();
                }
            }

            // ── KIT COUNT ──────────────────────────────────────────
            if (matchesAny(q, "how many kit", "total kit", "count kit", "number of kit")) {

                Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM kits", Long.class);
                Long avail = jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM kits WHERE availability > 0", Long.class);

                return String.format(
                        "Total kits: %d | Available: %d | Not available: %d",
                        count, avail, count - avail);
            }

            // ── DEFAULT: LIST ALL KITS ─────────────────────────────
            List<Map<String, Object>> kits = jdbcTemplate.queryForList(
                    "SELECT kit_id, kit_name, training_name, qualification_level, " +
                            "availability, location, kit_condition FROM kits LIMIT 10");

            if (kits.isEmpty())
                return "No kits found in the system.";

            StringBuilder sb = new StringBuilder("All Kits:\n");

            for (Map<String, Object> k : kits) {
                int avail = toInt(k.get("availability"));

                sb.append(String.format(
                        "• %s | %s | Training: %s | %s | Location: %s\n",
                        k.get("kit_id"),
                        nvl(k.get("kit_name")),
                        nvl(k.get("training_name")),
                        avail > 0 ? "✅ Available" : "❌ Not Available",
                        nvl(k.get("location"))));
            }

            return sb.toString().trim();

        } catch (Exception e) {
            logger.warn("handleKitQuery error: {}", e.getMessage());
            return null;
        }
    }

    /**
     * INTENT: Trainer Queries
     * Examples: "list trainers at Pune", "find trainer Ravi", "how many trainers?"
     */
    private String handleTrainerQuery(String q) {
        try {
            String location = extractLocation(q);

            // Specific trainer name search
            String trainerKeyword = extractToolKeyword(q,
                "find trainer", "search trainer", "trainer named", "trainer",
                "training", "who is", "list", "show", "at", "in", "the");

            if (!trainerKeyword.isEmpty() && trainerKeyword.length() > 2) {
                List<Map<String, Object>> results = jdbcTemplate.queryForList(
                    "SELECT name, email, contact, location, status, active_issuance, overdue_issuance " +
                    "FROM trainers WHERE LOWER(name) LIKE ? LIMIT 5",
                    "%" + trainerKeyword + "%"
                );

                if (results.isEmpty()) return "No trainer found matching '" + trainerKeyword + "'.";

                StringBuilder sb = new StringBuilder("Trainer Details:\n");
                for (Map<String, Object> t : results) {
                    sb.append(String.format(
                        "• %s | Email: %s | Contact: %s | Location: %s | Status: %s | " +
                        "Active Issuances: %s | Overdue: %s\n",
                        t.get("name"), nvl(t.get("email")), nvl(t.get("contact")),
                        nvl(t.get("location")), nvl(t.get("status")),
                        nvl(t.get("active_issuance"), "0"),
                        nvl(t.get("overdue_issuance"), "0")
                    ));
                }
                return sb.toString().trim();
            }

            // Count or list by location
            if (matchesAny(q, "how many", "count", "total")) {
                if (location != null) {
                    Long count = jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM trainers WHERE LOWER(location) = ?",
                        Long.class, location.toLowerCase()
                    );
                    return "There are " + count + " trainers at " + capitalize(location) + ".";
                }
                Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM trainers", Long.class);
                return "There are " + count + " trainers registered in the system.";
            }

            // List trainers by location or all
            String locationClause = location != null
                ? " WHERE LOWER(location) = '" + location.toLowerCase() + "'" : "";

            List<Map<String, Object>> trainers = jdbcTemplate.queryForList(
                "SELECT name, email, contact, location, status FROM trainers" +
                locationClause + " ORDER BY name LIMIT 10"
            );

            if (trainers.isEmpty()) {
                return "No trainers found" + (location != null ? " at " + capitalize(location) : "") + ".";
            }

            StringBuilder sb = new StringBuilder("Trainers");
            if (location != null) sb.append(" at ").append(capitalize(location));
            sb.append(":\n");
            for (Map<String, Object> t : trainers) {
                sb.append(String.format(
                    "• %s | Email: %s | Location: %s | Status: %s\n",
                    t.get("name"), nvl(t.get("email")), nvl(t.get("location")), nvl(t.get("status"))
                ));
            }
            return sb.toString().trim();

        } catch (Exception e) {
            logger.warn("handleTrainerQuery error: {}", e.getMessage());
            return null;
        }
    }

    /**
     * INTENT: Admin Queries
     * Examples: "list admins at Pune", "find admin John"
     */
    private String handleAdminQuery(String q) {
        try {
            String location = extractLocation(q);
            String adminKeyword = extractToolKeyword(q,
                "find admin", "search admin", "admin named", "admin",
                "administrator", "list", "show", "at", "in", "the");

            if (!adminKeyword.isEmpty() && adminKeyword.length() > 2) {
                List<Map<String, Object>> results = jdbcTemplate.queryForList(
                    "SELECT admin_id, name, email, contact, location, status " +
                    "FROM admins WHERE LOWER(name) LIKE ? LIMIT 5",
                    "%" + adminKeyword + "%"
                );

                if (results.isEmpty()) return "No admin found matching '" + adminKeyword + "'.";

                StringBuilder sb = new StringBuilder("Admin Details:\n");
                for (Map<String, Object> a : results) {
                    sb.append(String.format(
                        "• %s | %s | Email: %s | Location: %s | Status: %s\n",
                        a.get("admin_id"), a.get("name"),
                        nvl(a.get("email")), nvl(a.get("location")), nvl(a.get("status"))
                    ));
                }
                return sb.toString().trim();
            }

            String locationClause = location != null
                ? " WHERE LOWER(location) = '" + location.toLowerCase() + "'" : "";

            List<Map<String, Object>> admins = jdbcTemplate.queryForList(
                "SELECT admin_id, name, email, location, status FROM admins" +
                locationClause + " ORDER BY name LIMIT 10"
            );

            if (admins.isEmpty()) {
                return "No admins found" + (location != null ? " at " + capitalize(location) : "") + ".";
            }

            StringBuilder sb = new StringBuilder("Admins");
            if (location != null) sb.append(" at ").append(capitalize(location));
            sb.append(":\n");
            for (Map<String, Object> a : admins) {
                sb.append(String.format("• %s | %s | Location: %s | Status: %s\n",
                    a.get("admin_id"), a.get("name"), nvl(a.get("location")), nvl(a.get("status"))));
            }
            return sb.toString().trim();

        } catch (Exception e) {
            logger.warn("handleAdminQuery error: {}", e.getMessage());
            return null;
        }
    }

    /**
     * INTENT: Count / Statistics
     * Examples: "how many tools?", "total issuances today", "count available tools at Pune"
     */
    private String handleCountQuery(String q) {
        try {
            String location = extractLocation(q);
            String locClause = location != null
                ? " WHERE LOWER(location) = '" + location.toLowerCase() + "'" : "";
            String locAnd = location != null
                ? " AND LOWER(location) = '" + location.toLowerCase() + "'" : "";

            // Tools count
            if (matchesAny(q, "tool") && !matchesAny(q, "trainer", "kit", "issuance", "request")) {
                if (matchesAny(q, "available")) {
                    Long c = jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM tools WHERE availability > 0" + locAnd, Long.class);
                    return "Available tools" + (location != null ? " at " + capitalize(location) : "") + ": " + c;
                }
                if (matchesAny(q, "not available", "unavailable")) {
                    Long c = jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM tools WHERE availability = 0" + locAnd, Long.class);
                    return "Unavailable tools" + (location != null ? " at " + capitalize(location) : "") + ": " + c;
                }
                Long total = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM tools" + locClause, Long.class);
                Long avail = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM tools WHERE availability > 0" + locAnd, Long.class);
                return String.format("Tools%s — Total: %d | Available: %d | Not Available: %d",
                    location != null ? " at " + capitalize(location) : "", total, avail, total - avail);
            }

            // Trainer count
            if (matchesAny(q, "trainer")) {
                Long c = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM trainers" + locClause, Long.class);
                return "Trainers" + (location != null ? " at " + capitalize(location) : "") + ": " + c;
            }

            // Kit count
            if (matchesAny(q, "kit")) {
                Long total = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM kits", Long.class);
                Long avail = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM kits WHERE availability > 0", Long.class);
                return String.format("Kits — Total: %d | Available: %d | Not Available: %d",
                    total, avail, total - avail);
            }

            // Issuance count
            if (matchesAny(q, "issuance", "request")) {
                boolean isToday = matchesAny(q, "today");
                String dateClause = isToday ? " AND DATE(issuance_date) = CURDATE()" : "";
                Map<String, Object> stats = jdbcTemplate.queryForMap(
                    "SELECT " +
                    "COUNT(*) AS total, " +
                    "SUM(CASE WHEN LOWER(status)='pending' THEN 1 ELSE 0 END) AS pending, " +
                    "SUM(CASE WHEN LOWER(status)='approved' THEN 1 ELSE 0 END) AS approved, " +
                    "SUM(CASE WHEN LOWER(status)='returned' THEN 1 ELSE 0 END) AS returned, " +
                    "SUM(CASE WHEN LOWER(status)='rejected' THEN 1 ELSE 0 END) AS rejected " +
                    "FROM issuance_requests WHERE 1=1" + dateClause + locAnd
                );
                return String.format(
                    "Issuance Requests%s%s — Total: %d | Pending: %d | Approved: %d | Returned: %d | Rejected: %d",
                    isToday ? " (Today)" : "",
                    location != null ? " at " + capitalize(location) : "",
                    toLong(stats.get("total")), toLong(stats.get("pending")),
                    toLong(stats.get("approved")), toLong(stats.get("returned")),
                    toLong(stats.get("rejected"))
                );
            }

            // General overview
            Long tools = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM tools", Long.class);
            Long trainers = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM trainers", Long.class);
            Long kits = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM kits", Long.class);
            Long requests = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM issuance_requests", Long.class);
            Long pending = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM issuance_requests WHERE LOWER(status)='pending'", Long.class);

            return String.format(
                "System Overview — Tools: %d | Trainers: %d | Kits: %d | " +
                "Total Requests: %d | Pending Approvals: %d",
                tools, trainers, kits, requests, pending
            );

        } catch (Exception e) {
            logger.warn("handleCountQuery error: {}", e.getMessage());
            return null;
        }
    }

    /**
     * INTENT: Generic Tool Search (last resort)
     * Falls back to searching the tool description for any keyword in the query.
     */
    private String handleGenericToolSearch(String q) {
        try {
            // Remove very common stop words
            String keyword = q.replaceAll(
                "\\b(show|list|find|get|tell|give|what|where|which|how|is|are|the|a|an|me|" +
                "about|of|for|and|or|in|at|on|to|do|does|can|all|any|please|thanks|help)\\b", " ")
                .replaceAll("\\s+", " ").trim().replaceAll("[?.,!]", "");

            if (keyword.length() < 3) return null;

            List<Map<String, Object>> results = jdbcTemplate.queryForList(
                "SELECT description, tool_no, availability, location, tool_condition " +
                "FROM tools WHERE LOWER(description) LIKE ? LIMIT 5",
                "%" + keyword + "%"
            );

            if (results.isEmpty()) return null;

            StringBuilder sb = new StringBuilder("Search results for '" + keyword + "':\n");
            for (Map<String, Object> t : results) {
                int avail = toInt(t.get("availability"));
                sb.append(String.format(
                    "• %s | Tool No: %s | %s | Location: %s | Condition: %s\n",
                    t.get("description"), t.get("tool_no"),
                    avail > 0 ? "Available (" + avail + ") ✅" : "Not Available ❌",
                    nvl(t.get("location")), nvl(t.get("tool_condition"))
                ));
            }
            return sb.toString().trim();

        } catch (Exception e) {
            logger.warn("handleGenericToolSearch error: {}", e.getMessage());
            return null;
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  PREDEFINED Q&A CRUD METHODS
    // ─────────────────────────────────────────────────────────────

    public List<ChatbotQADTO> getAllQAs() {
        return chatbotQARepository.findAllActiveQAs().stream()
            .map(this::convertToDTO).toList();
    }

    public ChatbotQADTO addQA(ChatbotQADTO qaDTO) {
        ChatbotQA qa = new ChatbotQA(qaDTO.getQuestion(), qaDTO.getAnswer());
        return convertToDTO(chatbotQARepository.save(qa));
    }

    public ChatbotQADTO updateQA(Long id, ChatbotQADTO qaDTO) {
        Optional<ChatbotQA> existing = chatbotQARepository.findById(id);
        if (existing.isPresent()) {
            ChatbotQA qa = existing.get();
            qa.setQuestion(qaDTO.getQuestion());
            qa.setAnswer(qaDTO.getAnswer());
            return convertToDTO(chatbotQARepository.save(qa));
        }
        return null;
    }

    public boolean deleteQA(Long id) {
        Optional<ChatbotQA> existing = chatbotQARepository.findById(id);
        if (existing.isPresent()) {
            ChatbotQA qa = existing.get();
            qa.setIsActive(false);
            chatbotQARepository.save(qa);
            return true;
        }
        return false;
    }

    // ─────────────────────────────────────────────────────────────
    //  ENTITY EXTRACTORS
    // ─────────────────────────────────────────────────────────────

    /**
     * Extracts a plant location from the query.
     * Extend this list with your actual plant/city names.
     */
    private String extractLocation(String q) {
        String[] locations = {
                "pune",
                "bangalore",
                "bengaluru", // optional alias
                "ncr"
        };

        String lower = q.toLowerCase();

        for (String loc : locations) {
            if (lower.contains(loc)) {
                // normalize
                if (loc.equals("bengaluru"))
                    return "bangalore";
                return loc;
            }
        }
        return null;
    }

    /**
     * Strips noise words from the query to extract the core keyword
     * (tool name, trainer name, etc.).
     */
    private String extractToolKeyword(String q, String... stopWords) {
        String result = q.toLowerCase().replaceAll("[?.,!]", "");
        for (String w : stopWords) {
            result = result.replaceAll("\\b" + w.toLowerCase() + "\\b", " ");
        }
        // Remove common English stop words
        result = result.replaceAll(
            "\\b(show|list|find|get|tell|give|what|where|which|how|is|are|" +
            "me|please|thanks|help|currently|check|any|all)\\b", " ");

        // Remove known location words
        result = result.replaceAll(
                "\\b(pune|bangalore|bengaluru|ncr)\\b", " ");

        return result.replaceAll("\\s+", " ").trim();
    }

    // ─────────────────────────────────────────────────────────────
    //  UTILITY HELPERS
    // ─────────────────────────────────────────────────────────────

    /** Returns true if the query string contains ANY of the given keywords. */
    private boolean matchesAny(String q, String... keywords) {
        for (String k : keywords) {
            if (q.contains(k.toLowerCase())) return true;
        }
        return false;
    }

    private int toInt(Object val) {
        if (val == null) return 0;
        if (val instanceof Number) return ((Number) val).intValue();
        try { return Integer.parseInt(val.toString()); } catch (Exception e) { return 0; }
    }

    private long toLong(Object val) {
        if (val == null) return 0L;
        if (val instanceof Number) return ((Number) val).longValue();
        try { return Long.parseLong(val.toString()); } catch (Exception e) { return 0L; }
    }

    private String nvl(Object val) {
        return val != null ? val.toString() : "N/A";
    }

    private String nvl(Object val, String defaultVal) {
        return val != null ? val.toString() : defaultVal;
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1).toLowerCase();
    }

    private String boolStr(Object val) {
        if (val == null) return "No";
        String s = val.toString();
        return (s.equals("1") || s.equalsIgnoreCase("true")) ? "Yes" : "No";
    }

    private String formatDate(Object val) {
        if (val == null) return "N/A";
        String s = val.toString();
        // Trim time part if present: "2024-01-15 10:30:00" → "2024-01-15"
        return s.contains(" ") ? s.substring(0, s.indexOf(" ")) : s;
    }

    private ChatbotQADTO convertToDTO(ChatbotQA qa) {
        return new ChatbotQADTO(
            qa.getId(), qa.getQuestion(), qa.getAnswer(),
            qa.getCreatedAt(), qa.getUpdatedAt(), qa.getIsActive()
        );
    }
}