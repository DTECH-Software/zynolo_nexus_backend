package com.zynolo_nexus.setting_service.service;

import java.util.List;

import com.zynolo_nexus.contracts.modules.ModuleDto;
import com.zynolo_nexus.contracts.modules.ModuleRequest;
import com.zynolo_nexus.setting_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.setting_service.dto.request.ModuleFilterRequest;
import com.zynolo_nexus.setting_service.dto.request.ModuleReferenceDataRequest;
import com.zynolo_nexus.setting_service.dto.request.ModuleUpdateRequest;
import com.zynolo_nexus.setting_service.dto.response.ModuleFilterResultDto;
import com.zynolo_nexus.setting_service.dto.response.ModuleReferenceDataDto;

public interface ModuleService {

    MessageResponseDTO<ModuleDto> createModule(ModuleRequest request);

    MessageResponseDTO<ModuleDto> updateModule(String code, ModuleRequest request);

    MessageResponseDTO<ModuleDto> updateModule(String code, ModuleUpdateRequest request);

    MessageResponseDTO<ModuleDto> getModule(Long id);

    MessageResponseDTO<ModuleDto> updateModuleStatus(Long id, com.zynolo_nexus.contracts.modules.ModuleStatus status);

    MessageResponseDTO<List<ModuleDto>> getAllModules();

    MessageResponseDTO<ModuleReferenceDataDto> getReferenceData(ModuleReferenceDataRequest request);

    MessageResponseDTO<ModuleFilterResultDto> filterList(ModuleFilterRequest request);

    MessageResponseDTO<String> deactivateModule(String code);
}
