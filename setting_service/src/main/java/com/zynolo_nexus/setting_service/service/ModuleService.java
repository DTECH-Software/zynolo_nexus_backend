package com.zynolo_nexus.setting_service.service;

import java.util.List;

import com.zynolo_nexus.contracts.modules.ModuleDto;
import com.zynolo_nexus.contracts.modules.ModuleRequest;
import com.zynolo_nexus.setting_service.dto.api.MessageResponseDTO;

public interface ModuleService {

    MessageResponseDTO<ModuleDto> createModule(ModuleRequest request);

    MessageResponseDTO<ModuleDto> updateModule(String code, ModuleRequest request);

    MessageResponseDTO<List<ModuleDto>> getAllModules();

    MessageResponseDTO<String> deactivateModule(String code);
}
