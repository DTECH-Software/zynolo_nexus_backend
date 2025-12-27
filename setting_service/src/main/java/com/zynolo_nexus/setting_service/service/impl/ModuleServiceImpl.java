package com.zynolo_nexus.setting_service.service.impl;

import com.zynolo_nexus.contracts.modules.ModuleDto;
import com.zynolo_nexus.contracts.modules.ModuleRequest;
import com.zynolo_nexus.setting_service.client.AuthModuleClient;
import com.zynolo_nexus.setting_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.setting_service.service.ModuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ModuleServiceImpl implements ModuleService {

    private final AuthModuleClient authModuleClient;

    @Override
    public MessageResponseDTO<ModuleDto> createModule(ModuleRequest request) {
        ModuleDto module = authModuleClient.createModule(request);
        return MessageResponseDTO.<ModuleDto>builder()
                .success(true)
                .message("Module created successfully")
                .data(module)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    public MessageResponseDTO<ModuleDto> updateModule(String code, ModuleRequest request) {
        ModuleDto module = authModuleClient.updateModule(code, request);
        return MessageResponseDTO.<ModuleDto>builder()
                .success(true)
                .message("Module updated successfully")
                .data(module)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    public MessageResponseDTO<List<ModuleDto>> getAllModules() {
        List<ModuleDto> modules = authModuleClient.getAllModules();
        return MessageResponseDTO.<List<ModuleDto>>builder()
                .success(true)
                .message("Modules loaded successfully")
                .data(modules)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    public MessageResponseDTO<String> deactivateModule(String code) {
        authModuleClient.deactivateModule(code);
        return MessageResponseDTO.<String>builder()
                .success(true)
                .message("Module deactivated successfully")
                .data(code)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }
}
