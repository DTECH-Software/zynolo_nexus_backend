package com.zynolo_nexus.auth_service.service;

import com.zynolo_nexus.contracts.modules.ModuleDto;
import com.zynolo_nexus.contracts.modules.ModuleRequest;

import java.util.List;

public interface ModuleManagementService {

    ModuleDto createModule(ModuleRequest request);

    ModuleDto updateModule(String code, ModuleRequest request);

    List<ModuleDto> getAllModules();

    void deactivateModule(String code);
}
