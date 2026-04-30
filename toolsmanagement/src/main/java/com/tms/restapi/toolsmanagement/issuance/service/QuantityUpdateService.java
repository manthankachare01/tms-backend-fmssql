package com.tms.restapi.toolsmanagement.issuance.service;

import com.tms.restapi.toolsmanagement.kit.model.Kit;
import com.tms.restapi.toolsmanagement.kit.repository.KitRepository;
import com.tms.restapi.toolsmanagement.tools.model.Tool;
import com.tms.restapi.toolsmanagement.tools.repository.ToolRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuantityUpdateService {

	@Autowired
	private ToolRepository toolRepository;

	@Autowired
	private KitRepository kitRepository;

	public void reduceQuantities(List<Long> toolIds, List<Long> kitIds, String borrowerName) {
		if (toolIds != null) {
			for (Long toolId : toolIds) {
				Tool tool = toolRepository.findById(toolId)
						.orElseThrow(() -> new com.tms.restapi.toolsmanagement.exception.ResourceNotFoundException("Tool not found: id=" + toolId));
				if (tool.getAvailability() > 0) {
					tool.setAvailability(tool.getAvailability() - 1);
					tool.setLastBorrowedBy(borrowerName);
					toolRepository.save(tool);
				} else {
					throw new com.tms.restapi.toolsmanagement.exception.BadRequestException("Tool unavailable: " + tool.getToolNo());
				}
			}
		}

		if (kitIds != null) {
			for (Long kitId : kitIds) {
				Kit kit = kitRepository.findById(kitId)
					.orElseThrow(() -> new com.tms.restapi.toolsmanagement.exception.ResourceNotFoundException("Kit not found: id=" + kitId));
				if (kit.getAvailability() > 0) {
				    kit.setAvailability(kit.getAvailability() - 1);
				    kit.setLastBorrowedBy(borrowerName);
				    kitRepository.save(kit);

					// also decrement each tool inside the kit
					if (kit.getTools() != null) {
						for (Tool t : kit.getTools()) {
							if (t.getAvailability() > 0) {
								t.setAvailability(t.getAvailability() - 1);
								t.setLastBorrowedBy(borrowerName);
								toolRepository.save(t);
							} else {
								throw new com.tms.restapi.toolsmanagement.exception.BadRequestException("Kit contains unavailable tool: " + t.getToolNo());
							}
						}
					}
				} else {
					throw new com.tms.restapi.toolsmanagement.exception.BadRequestException("Kit unavailable: " + kit.getKitName());
				}
			}
		}
	}

	public void increaseQuantities(List<Long> toolIds, List<Long> kitIds) {
		if (toolIds != null) {
			for (Long toolId : toolIds) {
				toolRepository.findById(toolId).ifPresent(tool -> {
					tool.setAvailability(tool.getAvailability() + 1);
					toolRepository.save(tool);
				});
			}
		}

		if (kitIds != null) {
			for (Long kitId : kitIds) {
				kitRepository.findById(kitId).ifPresent(kit -> {
					kit.setAvailability(kit.getAvailability() + 1);
					kitRepository.save(kit);

					// also increment each tool inside the kit
					if (kit.getTools() != null) {
						for (Tool t : kit.getTools()) {
							t.setAvailability(t.getAvailability() + 1);
							toolRepository.save(t);
						}
					}
				});
			}
		}
	}
}