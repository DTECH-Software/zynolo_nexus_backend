package com.zynolo_nexus.setting_service.service.impl;

import com.zynolo_nexus.contracts.pages.RolePageTaskAccessDto;
import com.zynolo_nexus.contracts.pages.RolePageTaskAccessUpdateRequest;
import com.zynolo_nexus.setting_service.client.AuthModuleClient;
import com.zynolo_nexus.setting_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.setting_service.service.RolePageTaskAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RolePageTaskAccessServiceImpl implements RolePageTaskAccessService {

    private final AuthModuleClient authModuleClient;

    @Override
    public MessageResponseDTO<RolePageTaskAccessDto> getRolePageTaskAccess(String roleCode) {
        RolePageTaskAccessDto dto = authModuleClient.getRolePageTaskAccess(roleCode);
        return MessageResponseDTO.<RolePageTaskAccessDto>builder()
                .success(true)
                .message("Role page tasks loaded successfully")
                .data(dto)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    public MessageResponseDTO<RolePageTaskAccessDto> updateRolePageTaskAccess(RolePageTaskAccessUpdateRequest request) {
        RolePageTaskAccessDto dto = authModuleClient.updateRolePageTaskAccess(request);
        return MessageResponseDTO.<RolePageTaskAccessDto>builder()
                .success(true)
                .message("Role page tasks updated successfully")
                .data(dto)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }
}
