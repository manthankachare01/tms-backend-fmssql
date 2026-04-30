package com.tms.restapi.toolsmanagement.issuance.service;

import com.tms.restapi.toolsmanagement.issuance.dto.ReturnItemDto;
import com.tms.restapi.toolsmanagement.issuance.dto.ReturnRequestDto;
import com.tms.restapi.toolsmanagement.issuance.model.Issuance;
import com.tms.restapi.toolsmanagement.issuance.model.IssuanceRequest;
import com.tms.restapi.toolsmanagement.issuance.model.ReturnItem;
import com.tms.restapi.toolsmanagement.issuance.model.ReturnRecord;
import com.tms.restapi.toolsmanagement.issuance.repository.IssuanceRepository;
import com.tms.restapi.toolsmanagement.issuance.repository.IssuanceRequestRepository;
import com.tms.restapi.toolsmanagement.issuance.repository.ReturnRepository;
import com.tms.restapi.toolsmanagement.kit.model.Kit;
import com.tms.restapi.toolsmanagement.kit.repository.KitRepository;
import com.tms.restapi.toolsmanagement.tools.model.Tool;
import com.tms.restapi.toolsmanagement.tools.repository.ToolRepository;
import com.tms.restapi.toolsmanagement.trainer.model.Trainer;
import com.tms.restapi.toolsmanagement.trainer.repository.TrainerRepository;
import com.tms.restapi.toolsmanagement.exception.BadRequestException;
import com.tms.restapi.toolsmanagement.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class IssuanceService {

    private static final Logger logger = LoggerFactory.getLogger(IssuanceService.class);

    @Autowired
    private IssuanceRepository issuanceRepository;

    @Autowired
    private IssuanceRequestRepository issuanceRequestRepository;

    @Autowired
    private QuantityUpdateService quantityService;

    @Autowired
    private ToolRepository toolRepository;

    @Autowired
    private KitRepository kitRepository;

    @Autowired
    private ReturnRepository returnRecordRepository;

    @Autowired
    private TrainerRepository trainerRepository;

    @Autowired
    private com.tms.restapi.toolsmanagement.auth.service.EmailService emailService;

    @Autowired
    private com.tms.restapi.toolsmanagement.admin.repository.AdminRepository adminRepository;

    // -------------------------------------------------------------------------
    // HELPERS
    // -------------------------------------------------------------------------

    /**
     * Apply issuance to a single tool row using its unique database id.
     * Never uses tool_no — always operates on the exact row.
     */
    private void applyIssuanceToTool(Tool tool, String trainerName, LocalDateTime returnDate) {
        if (tool.getCurrentBorrowedBy() != null) {
            tool.setLastBorrowedBy(tool.getCurrentBorrowedBy());
        }
        tool.setCurrentBorrowedBy(trainerName);
        tool.setIssueCount((tool.getIssueCount() == null ? 0 : tool.getIssueCount()) + 1);
        tool.setIssuanceDate(LocalDateTime.now());
        tool.setReturnDate(returnDate);
        tool.setAvailability(tool.getAvailability() - 1);
        toolRepository.save(tool);
    }

    /**
     * Apply return to a single tool row using its unique database id.
     * Never uses tool_no — always operates on the exact row.
     */
    private void applyReturnToTool(Tool tool, int quantityReturned) {
        tool.setAvailability(tool.getAvailability() + quantityReturned);
        tool.setIssuanceDate(null);
        tool.setReturnDate(null);
        if (tool.getCurrentBorrowedBy() != null) {
            tool.setLastBorrowedBy(tool.getCurrentBorrowedBy());
        }
        tool.setCurrentBorrowedBy(null);
        toolRepository.save(tool);
    }

    // -------------------------------------------------------------------------
    // OVERDUE
    // -------------------------------------------------------------------------

    public void updateOverdueStatuses() {
        LocalDateTime now = LocalDateTime.now();
        List<Issuance> issuances = issuanceRepository.findAll();

        for (Issuance i : issuances) {
            if ("ISSUED".equalsIgnoreCase(i.getStatus()) &&
                    i.getReturnDate() != null &&
                    i.getReturnDate().isBefore(now)) {

                i.setStatus("OVERDUE");
                issuanceRepository.save(i);

                try {
                    Trainer trainer = trainerRepository.findById(i.getTrainerId()).orElse(null);
                    if (trainer != null && trainer.getEmail() != null) {
                        emailService.sendOverdueEmailToTrainer(i, trainer.getEmail(), trainer.getName());
                    }
                } catch (Exception e) { /* ignore */ }

                if (i.getLocation() != null) {
                    try {
                        List<com.tms.restapi.toolsmanagement.admin.model.Admin> admins =
                                adminRepository.findByLocation(i.getLocation());
                        if (admins != null && !admins.isEmpty()) {
                            for (com.tms.restapi.toolsmanagement.admin.model.Admin admin : admins) {
                                try {
                                    emailService.sendOverdueEmailToAdmin(i, admin.getEmail(), admin.getName());
                                } catch (Exception e) { /* ignore */ }
                            }
                        }
                    } catch (Exception e) { /* ignore */ }
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // CREATE REQUEST
    // -------------------------------------------------------------------------

    public Issuance createIssuanceRequest(Issuance issuance) {
        if (issuance.getTrainerId() == null) {
            throw new BadRequestException("trainerId is required");
        }
        if (issuance.getTrainerName() == null || issuance.getTrainerName().isEmpty()) {
            throw new BadRequestException("trainerName is required");
        }
        if ((issuance.getToolIds() == null || issuance.getToolIds().isEmpty())
                && (issuance.getKitIds() == null || issuance.getKitIds().isEmpty())) {
            throw new BadRequestException("At least one toolId or kitId is required");
        }

        // Guard: reject duplicate tool ids in the same request
        if (issuance.getToolIds() != null) {
            Set<Long> seen = new HashSet<>();
            for (Long toolId : issuance.getToolIds()) {
                if (toolId == null) throw new BadRequestException("toolIds list contains a null value");
                if (!seen.add(toolId)) {
                    throw new BadRequestException(
                            "Duplicate toolId " + toolId + " in request. " +
                            "Each tool (identified by its unique si_no / database id) " +
                            "must appear at most once per issuance request.");
                }
            }
        }

        Issuance pendingIssuance = new Issuance();
        pendingIssuance.setTrainerId(issuance.getTrainerId());
        pendingIssuance.setTrainerName(issuance.getTrainerName());
        pendingIssuance.setTrainingName(issuance.getTrainingName());
        pendingIssuance.setToolIds(issuance.getToolIds());
        pendingIssuance.setKitIds(issuance.getKitIds());
        pendingIssuance.setReturnDate(issuance.getReturnDate());
        pendingIssuance.setLocation(issuance.getLocation());
        pendingIssuance.setComment(issuance.getComment());
        pendingIssuance.setIssuanceType(issuance.getIssuanceType());
        pendingIssuance.setRemarks(issuance.getRemarks());
        pendingIssuance.setStatus("PENDING");
        pendingIssuance.setIssuanceDate(LocalDateTime.now());

        Issuance savedIssuance = issuanceRepository.save(pendingIssuance);

        IssuanceRequest request = new IssuanceRequest();
        request.setTrainerId(issuance.getTrainerId());
        request.setTrainerName(issuance.getTrainerName());
        request.setTrainingName(issuance.getTrainingName());
        request.setToolIds(issuance.getToolIds());
        request.setKitIds(issuance.getKitIds());
        request.setReturnDate(issuance.getReturnDate());
        request.setLocation(issuance.getLocation());
        request.setComment(issuance.getComment());
        request.setIssuanceType(issuance.getIssuanceType());
        request.setRemarks(issuance.getRemarks());
        request.setStatus("PENDING");
        request.setRequestDate(LocalDateTime.now());
        request.setIssuanceId(savedIssuance.getId());

        IssuanceRequest savedRequest = issuanceRequestRepository.save(request);

        if (issuance.getLocation() != null) {
            try {
                List<com.tms.restapi.toolsmanagement.admin.model.Admin> admins =
                        adminRepository.findByLocation(issuance.getLocation());
                if (admins != null && !admins.isEmpty()) {
                    for (com.tms.restapi.toolsmanagement.admin.model.Admin admin : admins) {
                        try {
                            emailService.sendIssuanceRequestNotification(savedRequest, admin.getEmail(), admin.getName());
                        } catch (Exception e) { /* ignore */ }
                    }
                }
            } catch (Exception e) { /* ignore */ }
        }

        return savedIssuance;
    }

    // -------------------------------------------------------------------------
    // APPROVE
    // -------------------------------------------------------------------------

    public Issuance approveIssuanceRequest(Long requestId, String approvedBy, String approvalRemark) {
        IssuanceRequest request = issuanceRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Issuance request not found: id=" + requestId));

        if (!request.getStatus().equals("PENDING")) {
            throw new BadRequestException("Issuance request is not in PENDING status. Current status: " + request.getStatus());
        }

        // Track already-processed tool ids to avoid double-updating
        Set<Long> processedToolIds = new HashSet<>();

        // Update standalone tools (issued directly)
        if (request.getToolIds() != null) {
            for (Long toolId : request.getToolIds()) {
                if (toolId == null || processedToolIds.contains(toolId)) continue;
                Tool tool = toolRepository.findById(toolId)
                        .orElseThrow(() -> new ResourceNotFoundException("Tool not found: id=" + toolId));
                if (tool.getAvailability() <= 0) {
                    throw new BadRequestException("Tool not available: " + tool.getDescription()
                            + " (si_no=" + tool.getSiNo() + ", id=" + toolId + ")");
                }
                applyIssuanceToTool(tool, request.getTrainerName(), request.getReturnDate());
                processedToolIds.add(toolId);
            }
        }

        // Update kits and the tools inside each kit
        if (request.getKitIds() != null) {
            for (Long kitId : request.getKitIds()) {
                Kit kit = kitRepository.findById(kitId)
                        .orElseThrow(() -> new ResourceNotFoundException("Kit not found: id=" + kitId));
                if (kit.getAvailability() <= 0) {
                    throw new BadRequestException("Kit not available: " + kit.getKitName());
                }

                if (kit.getCurrentBorrowedBy() != null) {
                    kit.setLastBorrowedBy(kit.getCurrentBorrowedBy());
                }
                kit.setCurrentBorrowedBy(request.getTrainerName());
                kit.setAvailability(kit.getAvailability() - 1);
                kit.setIssuanceDate(LocalDateTime.now());
                kit.setReturnDate(request.getReturnDate());
                kitRepository.save(kit);

                // Update each tool inside this kit by its unique id
                if (kit.getTools() != null) {
                    for (Tool toolRef : kit.getTools()) {
                        if (toolRef == null) continue;
                        Long toolIdInKit = toolRef.getId();
                        if (toolIdInKit == null || processedToolIds.contains(toolIdInKit)) continue;
                        toolRepository.findById(toolIdInKit).ifPresent(managedTool -> {
                            applyIssuanceToTool(managedTool, request.getTrainerName(), request.getReturnDate());
                            processedToolIds.add(toolIdInKit);
                        });
                    }
                }
            }
        }

        Issuance existingIssuance = issuanceRepository.findById(request.getIssuanceId())
                .orElseThrow(() -> new ResourceNotFoundException("Issuance not found: id=" + request.getIssuanceId()));

        existingIssuance.setStatus("ISSUED");
        existingIssuance.setApprovedBy(approvedBy);
        existingIssuance.setApprovalDate(LocalDateTime.now());
        existingIssuance.setApprovalRemark(approvalRemark);

        Issuance savedIssuance = issuanceRepository.save(existingIssuance);

        Trainer trainer = trainerRepository.findById(request.getTrainerId()).orElse(null);
        if (trainer != null) {
            int issuedCount =
                    (request.getToolIds() != null ? request.getToolIds().size() : 0)
                            + (request.getKitIds() != null ? request.getKitIds().size() : 0);
            trainer.setToolsIssued(trainer.getToolsIssued() + issuedCount);
            trainer.setActiveIssuance(trainer.getActiveIssuance() + 1);
            trainerRepository.save(trainer);
        }

        request.setStatus("APPROVED");
        request.setApprovedBy(approvedBy);
        request.setApprovalDate(LocalDateTime.now());
        request.setApprovalRemark(approvalRemark);
        issuanceRequestRepository.save(request);

        try {
            Trainer t = trainerRepository.findById(savedIssuance.getTrainerId()).orElse(null);
            if (t != null && t.getEmail() != null) {
                emailService.sendIssuanceApprovalEmail(savedIssuance, t.getEmail(), t.getName());
            }
        } catch (Exception e) { /* ignore */ }

        return savedIssuance;
    }

    // -------------------------------------------------------------------------
    // REJECT
    // -------------------------------------------------------------------------

    public void rejectIssuanceRequest(Long requestId, String rejectedBy, String rejectionReason) {
        IssuanceRequest request = issuanceRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Issuance request not found: id=" + requestId));

        if (!request.getStatus().equals("PENDING")) {
            throw new BadRequestException("Issuance request is not in PENDING status. Current status: " + request.getStatus());
        }

        if (request.getIssuanceId() != null) {
            Issuance issuance = issuanceRepository.findById(request.getIssuanceId()).orElse(null);
            if (issuance != null) {
                issuance.setStatus("REJECTED");
                issuance.setApprovedBy(rejectedBy);
                issuance.setApprovalDate(LocalDateTime.now());
                issuance.setApprovalRemark(rejectionReason);
                issuanceRepository.save(issuance);
            }
        }

        request.setStatus("REJECTED");
        request.setApprovedBy(rejectedBy);
        request.setApprovalDate(LocalDateTime.now());
        request.setApprovalRemark(rejectionReason);
        issuanceRequestRepository.save(request);

        try {
            Trainer t = trainerRepository.findById(request.getTrainerId()).orElse(null);
            if (t != null && t.getEmail() != null) {
                emailService.sendIssuanceRejectionEmail(request, t.getEmail(), t.getName());
            }
        } catch (Exception e) { /* ignore */ }
    }

    // -------------------------------------------------------------------------
    // QUERIES
    // -------------------------------------------------------------------------

    public List<IssuanceRequest> getPendingRequestsByLocation(String location) {
        return issuanceRequestRepository.findByLocationAndStatus(location, "PENDING");
    }

    public List<IssuanceRequest> getAllRequestsByLocation(String location) {
        return issuanceRequestRepository.findByLocation(location);
    }

    public List<Issuance> getRequestsByTrainer(Long trainerId) {
        return issuanceRepository.findByTrainerId(trainerId);
    }

    public List<IssuanceRequest> getIssuanceRequestsByTrainer(Long trainerId) {
        return issuanceRequestRepository.findByTrainerId(trainerId);
    }

    public List<Issuance> getRequestsByLocation(String location) {
        return issuanceRepository.findByLocation(location);
    }

    public List<IssuanceRequest> getAllIssuanceRequests() {
        return issuanceRequestRepository.findAll();
    }

    // -------------------------------------------------------------------------
    // RETURN
    // -------------------------------------------------------------------------

    public Issuance processReturn(ReturnRequestDto body) {
        if (body.getIssuanceId() == null) {
            throw new BadRequestException("issuanceId is required");
        }

        return issuanceRepository.findById(body.getIssuanceId()).map(req -> {

            LocalDateTime actualReturnDate = body.getActualReturnDate() != null
                    ? body.getActualReturnDate()
                    : LocalDateTime.now();

            LocalDateTime plannedReturnDate = req.getReturnDate();

            if (plannedReturnDate != null && actualReturnDate.isAfter(plannedReturnDate)) {
                req.setStatus("OVERDUE");
            } else {
                req.setStatus("RETURNED");
            }

            ReturnRecord rr = new ReturnRecord();
            rr.setIssuance(req);
            rr.setActualReturnDate(actualReturnDate);
            rr.setProcessedBy(body.getProcessedBy());
            rr.setRemarks(body.getRemarks());

            // ----------------------------------------------------------------
            // KEY FIX: Build a Set of the exact tool ids that were issued in
            // this issuance. These are the ONLY ids whose availability should
            // be restored on return. We never use tool_no or kit.getTools()
            // to drive availability changes — only the ids recorded at
            // issuance time are trusted.
            // ----------------------------------------------------------------
            Set<Long> issuedToolIds = new HashSet<>();
            if (req.getToolIds() != null) {
                issuedToolIds.addAll(req.getToolIds());
            }
            // Include tools from issued kits
            if (req.getKitIds() != null) {
                for (Long kitId : req.getKitIds()) {
                    Kit kit = kitRepository.findById(kitId).orElse(null);
                    if (kit != null && kit.getTools() != null) {
                        for (Tool tool : kit.getTools()) {
                            if (tool.getId() != null) {
                                issuedToolIds.add(tool.getId());
                            }
                        }
                    }
                }
            }

            // Track which ids we have already processed (prevent double increment)
            Set<Long> processedReturnToolIds = new HashSet<>();

            boolean hasItems = body.getItems() != null && !body.getItems().isEmpty();

            if (hasItems) {
                // Per-item return: caller explicitly lists which tool/kit to return
                for (ReturnItemDto it : body.getItems()) {
                    ReturnItem ri = new ReturnItem();
                    ri.setReturnRecord(rr);
                    ri.setToolId(it.getToolId());
                    ri.setKitId(it.getKitId());
                    ri.setQuantityReturned(it.getQuantityReturned() == null ? 1 : it.getQuantityReturned());
                    ri.setCondition(it.getCondition());
                    ri.setRemark(it.getRemark());
                    rr.getItems().add(ri);

                    // --- Individual tool return ---
                    if (it.getToolId() != null) {
                        Long returnedToolId = it.getToolId();

                        // Only update if this exact tool was issued in this issuance
                        if (!issuedToolIds.contains(returnedToolId)) {
                            logger.warn("Return contains toolId {} which was not part of issuance {}. Skipping.",
                                    returnedToolId, req.getId());
                            continue;
                        }
                        if (processedReturnToolIds.contains(returnedToolId)) continue;

                        Tool t = toolRepository.findById(returnedToolId)
                                .orElseThrow(() -> new ResourceNotFoundException("Tool not found: id=" + returnedToolId));
                        applyReturnToTool(t, ri.getQuantityReturned());
                        // Update condition/remark only for individual tool returns
                        if (ri.getCondition() != null) t.setCondition(ri.getCondition());
                        if (ri.getRemark() != null) t.setRemark(ri.getRemark());
                        toolRepository.save(t);
                        processedReturnToolIds.add(returnedToolId);
                    }

                    // --- Kit return ---
                    if (it.getKitId() != null) {
                        Kit k = kitRepository.findById(it.getKitId())
                                .orElseThrow(() -> new ResourceNotFoundException("Kit not found: id=" + it.getKitId()));

                        k.setAvailability(k.getAvailability() + ri.getQuantityReturned());
                        k.setIssuanceDate(null);
                        k.setReturnDate(null);
                        if (k.getCurrentBorrowedBy() != null) {
                            k.setLastBorrowedBy(k.getCurrentBorrowedBy());
                        }
                        k.setCurrentBorrowedBy(null);
                        if (ri.getCondition() != null) {
                            try { k.getClass().getMethod("setCondition", String.class).invoke(k, ri.getCondition()); }
                            catch (Exception ignore) {}
                        }
                        if (ri.getRemark() != null) {
                            try { k.getClass().getMethod("setRemark", String.class).invoke(k, ri.getRemark()); }
                            catch (Exception ignore) {}
                        }
                        kitRepository.save(k);

                        // KEY FIX: Only restore tools that were recorded in issuedToolIds.
                        // kit.getTools() returns ALL tools in the kit — including tools with
                        // duplicate tool_no that were NOT part of this issuance. The
                        // issuedToolIds guard ensures we only touch the exact tools issued.
                        if (k.getTools() != null) {
                            for (Tool toolRef : k.getTools()) {
                                if (toolRef == null) continue;
                                Long toolIdInKit = toolRef.getId();
                                if (toolIdInKit == null) continue;
                                if (!issuedToolIds.contains(toolIdInKit)) continue; // <-- THE FIX
                                if (processedReturnToolIds.contains(toolIdInKit)) continue;
                                toolRepository.findById(toolIdInKit).ifPresent(managedTool -> {
                                    applyReturnToTool(managedTool, ri.getQuantityReturned());
                                    processedReturnToolIds.add(toolIdInKit);
                                });
                            }
                        }
                    }
                }

            } else {
                // Full return (no per-item breakdown)
                // Use ONLY the exact issuedToolIds — never iterate kit.getTools() blindly

                // Return standalone tools
                for (Long toolId : issuedToolIds) {
                    if (processedReturnToolIds.contains(toolId)) continue;
                    toolRepository.findById(toolId).ifPresent(tool -> {
                        applyReturnToTool(tool, 1);
                        processedReturnToolIds.add(toolId);
                    });
                }

                // Return kits
                if (req.getKitIds() != null) {
                    for (Long kitId : req.getKitIds()) {
                        Kit kit = kitRepository.findById(kitId).orElse(null);
                        if (kit == null) continue;

                        kit.setAvailability(kit.getAvailability() + 1);
                        kit.setIssuanceDate(null);
                        kit.setReturnDate(null);
                        if (kit.getCurrentBorrowedBy() != null) {
                            kit.setLastBorrowedBy(kit.getCurrentBorrowedBy());
                        }
                        kit.setCurrentBorrowedBy(null);
                        kitRepository.save(kit);

                        // KEY FIX: Only restore tools that were in issuedToolIds
                        if (kit.getTools() != null) {
                            for (Tool toolRef : kit.getTools()) {
                                if (toolRef == null) continue;
                                Long toolIdInKit = toolRef.getId();
                                if (toolIdInKit == null) continue;
                                if (!issuedToolIds.contains(toolIdInKit)) continue; // <-- THE FIX
                                if (processedReturnToolIds.contains(toolIdInKit)) continue;
                                toolRepository.findById(toolIdInKit).ifPresent(managedTool -> {
                                    applyReturnToTool(managedTool, 1);
                                    processedReturnToolIds.add(toolIdInKit);
                                });
                            }
                        }
                    }
                }
            }

            // Save return record
            returnRecordRepository.save(rr);

            // Update trainer stats
            Trainer trainer = trainerRepository.findById(req.getTrainerId()).orElse(null);
            if (trainer != null) {
                int returnCount =
                        (req.getToolIds() != null ? req.getToolIds().size() : 0)
                                + (req.getKitIds() != null ? req.getKitIds().size() : 0);
                trainer.setToolsReturned(trainer.getToolsReturned() + returnCount);
                trainer.setActiveIssuance(Math.max(0, trainer.getActiveIssuance() - 1));
                if (plannedReturnDate != null && actualReturnDate.isAfter(plannedReturnDate)) {
                    trainer.setOverdueIssuance(trainer.getOverdueIssuance() + 1);
                }
                trainerRepository.save(trainer);
            }

            req.setReturnDate(actualReturnDate);
            Issuance savedReq = issuanceRepository.save(req);

            // Sync IssuanceRequest status
            try {
                IssuanceRequest originalRequest = issuanceRequestRepository.findById(req.getId()).orElse(null);
                if (originalRequest != null) {
                    originalRequest.setStatus("OVERDUE".equalsIgnoreCase(savedReq.getStatus())
                            ? "RETURNED_OVERDUE" : "RETURNED");
                    issuanceRequestRepository.save(originalRequest);
                }
            } catch (Exception e) {
                logger.warn("Failed to update IssuanceRequest status to RETURNED: " + e.getMessage());
            }

            // Send return email to trainer
            try {
                Trainer tr = trainerRepository.findById(savedReq.getTrainerId()).orElse(null);
                if (tr != null && tr.getEmail() != null) {
                    emailService.sendReturnEmail(rr, tr.getEmail());
                }
            } catch (Exception e) { /* ignore */ }

            // Notify admin of damaged/missing/obsolete items
            List<ReturnItem> problematicItems = new java.util.ArrayList<>();
            if (rr.getItems() != null) {
                for (ReturnItem ri : rr.getItems()) {
                    String cond = ri.getCondition();
                    if (cond != null && (
                            cond.equalsIgnoreCase("damaged") ||
                            cond.equalsIgnoreCase("missing") ||
                            cond.equalsIgnoreCase("obsolete"))) {
                        problematicItems.add(ri);
                    }
                }
            }
            if (!problematicItems.isEmpty() && savedReq.getLocation() != null) {
                try {
                    List<com.tms.restapi.toolsmanagement.admin.model.Admin> admins =
                            adminRepository.findByLocation(savedReq.getLocation());
                    if (admins != null && !admins.isEmpty()) {
                        for (com.tms.restapi.toolsmanagement.admin.model.Admin admin : admins) {
                            try {
                                emailService.sendDamagedItemNotification(problematicItems, savedReq, admin.getEmail(), admin.getName());
                            } catch (Exception e) { /* ignore */ }
                        }
                    }
                } catch (Exception e) { /* ignore */ }
            }

            // Notify on overdue return
            if ("OVERDUE".equals(savedReq.getStatus())) {
                try {
                    Trainer tr = trainerRepository.findById(savedReq.getTrainerId()).orElse(null);
                    if (tr != null && tr.getEmail() != null) {
                        emailService.sendOverdueEmailToTrainer(savedReq, tr.getEmail(), tr.getName());
                    }
                } catch (Exception e) { /* ignore */ }

                if (savedReq.getLocation() != null) {
                    try {
                        List<com.tms.restapi.toolsmanagement.admin.model.Admin> admins =
                                adminRepository.findByLocation(savedReq.getLocation());
                        if (admins != null && !admins.isEmpty()) {
                            for (com.tms.restapi.toolsmanagement.admin.model.Admin admin : admins) {
                                try {
                                    emailService.sendOverdueEmailToAdmin(savedReq, admin.getEmail(), admin.getName());
                                } catch (Exception e) { /* ignore */ }
                            }
                        }
                    } catch (Exception e) { /* ignore */ }
                }
            }

            return savedReq;
        }).orElse(null);
    }

    // -------------------------------------------------------------------------
    // GETTERS
    // -------------------------------------------------------------------------

    public List<Issuance> getAllRequests() {
        return issuanceRepository.findAll();
    }

    public List<Issuance> getCurrentIssuedItems() {
        return issuanceRepository.findByStatus("ISSUED");
    }

    public List<ReturnRecord> getAllReturnRecords() {
        return returnRecordRepository.findAll();
    }

    public List<ReturnRecord> getReturnRecordsByLocation(String location) {
        return returnRecordRepository.findByIssuance_Location(location);
    }

    public List<ReturnRecord> getReturnRecordsByTrainer(Long trainerId) {
        return returnRecordRepository.findByIssuance_TrainerId(trainerId);
    }

    public List<ReturnRecord> getReturnRecordsByLocationAndTrainer(String location, Long trainerId) {
        return returnRecordRepository.findByIssuance_LocationAndIssuance_TrainerId(location, trainerId);
    }
}