package com.tms.restapi.toolsmanagement.tools.service;

import com.tms.restapi.toolsmanagement.tools.model.Tool;
import com.tms.restapi.toolsmanagement.tools.repository.ToolRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service layer for tool inventory operations.
 *
 * Handles creation, retrieval, update, deletion, search, and issuance-related updates
 * for tool entities.
 */
@Service
public class ToolService {

    @Autowired
    private ToolRepository toolRepository;

    // Helper: calculate and assign the next calibration date based on the last calibration date and period
    private void updateNextCalibrationDate(Tool tool) {
        if (tool.isCalibrationRequired()
                && tool.getLastCalibrationDate() != null
                && tool.getCalibrationPeriodMonths() != null) {

            LocalDate next = tool.getLastCalibrationDate()
                    .plusMonths(tool.getCalibrationPeriodMonths());
            tool.setNextCalibrationDate(next);
        } else {
            // If not required, or missing data, clear nextCalibrationDate
            tool.setNextCalibrationDate(null);

            if (!tool.isCalibrationRequired()) {
                // Calibration not required, we also clear these
                tool.setCalibrationPeriodMonths(null);
                tool.setLastCalibrationDate(null);
            }
        }
    }

    // Create tool (location from adminLocation, availability = quantity initially)
    public Tool createTool(Tool tool, String adminLocation, String createdBy) {
        tool.setId(null); // new entity

        // Force location from admin
        tool.setLocation(adminLocation);

        // availability is remaining quantity; at start all are available
        tool.setAvailability(tool.getQuantity());

        // lastBorrowedBy and currentBorrowedBy start as null
        tool.setLastBorrowedBy(null);
        tool.setCurrentBorrowedBy(null);

        // Set creator info
        tool.setCreatedBy(createdBy);
        tool.setCreatedAt(LocalDateTime.now());

        // calibration logic
        updateNextCalibrationDate(tool);

        return toolRepository.save(tool);
    }

    public List<Tool> getAllTools() {
        return toolRepository.findAll();
    }

    public List<Tool> getToolsByLocation(String location) {
        return toolRepository.findByLocation(location);
    }

    public Optional<Tool> getToolById(Long id) {
        return toolRepository.findById(id);
    }

    public Tool updateTool(Long id, Tool toolDetails) {
        Optional<Tool> existingOpt = toolRepository.findById(id);
        if (existingOpt.isEmpty()) {
            return null;
        }

        Tool tool = existingOpt.get();

        // Fields allowed to update from form
        tool.setDescription(toolDetails.getDescription());
        tool.setSiNo(toolDetails.getSiNo());
        tool.setToolNo(toolDetails.getToolNo());
        tool.setToolLocation(toolDetails.getToolLocation());
        tool.setCondition(toolDetails.getCondition());
        tool.setRemark(toolDetails.getRemark());

        // Fixed quantity can be updated by admin
        tool.setQuantity(toolDetails.getQuantity());

        // Do NOT change availability here (this will be handled by issuance)
        // Do NOT change location from frontend
        // Do NOT overwrite lastBorrowedBy (issuance will update it)

        // Calibration fields from form
        tool.setCalibrationRequired(toolDetails.isCalibrationRequired());
        tool.setCalibrationPeriodMonths(toolDetails.getCalibrationPeriodMonths());
        tool.setLastCalibrationDate(toolDetails.getLastCalibrationDate());

        updateNextCalibrationDate(tool);

        return toolRepository.save(tool);
    }

    public String deleteTool(Long id) {
        if (!toolRepository.existsById(id)) {
            return "Tool not found.";
        }
        toolRepository.deleteById(id);
        return "Tool deleted successfully.";
    }

    public List<Tool> searchTools(String keyword) {
        return toolRepository
                .findByDescriptionContainingIgnoreCaseOrToolNoContainingIgnoreCase(keyword, keyword);
    }

    public List<Tool> searchToolsByLocation(String keyword, String location) {
        return toolRepository.searchByLocationAndKeyword(location, keyword);
    }

    // Issuance logic can use this to update remaining quantity and lastBorrowedBy
    public Tool updateToolAfterIssuance(Long toolId, int newAvailability, String lastBorrowedBy) {
        Optional<Tool> existingOpt = toolRepository.findById(toolId);
        if (existingOpt.isEmpty()) {
            return null;
        }

        Tool tool = existingOpt.get();

        // Remaining quantity after this issuance/return
        tool.setAvailability(newAvailability);
        tool.setLastBorrowedBy(lastBorrowedBy);

        return toolRepository.save(tool);
    }
}
