package com.zynolo_nexus.setting_service.service.impl;

import com.zynolo_nexus.contracts.modules.RoleModuleAccessDto;
import com.zynolo_nexus.contracts.modules.RoleModuleAccessUpdateRequest;
import com.zynolo_nexus.setting_service.client.AuthModuleClient;
import com.zynolo_nexus.setting_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.setting_service.service.RoleModuleAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RoleModuleAccessServiceImpl implements RoleModuleAccessService {

    private final AuthModuleClient authModuleClient;

    @Override
    public MessageResponseDTO<RoleModuleAccessDto> getRoleModuleAccess(String roleCode) {

        RoleModuleAccessDto dto = authModuleClient.getRoleModuleAccess(roleCode);

        return MessageResponseDTO.<RoleModuleAccessDto>builder()
                .success(true)
                .message("Role module access loaded successfully")
                .data(dto)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    public MessageResponseDTO<RoleModuleAccessDto> updateRoleModuleAccess(RoleModuleAccessUpdateRequest request) {

        RoleModuleAccessDto dto = authModuleClient.updateRoleModuleAccess(request);

        return MessageResponseDTO.<RoleModuleAccessDto>builder()
                .success(true)
                .message("Role module access updated successfully")
                .data(dto)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }
}
