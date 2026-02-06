package com.zynolo_nexus.setting_service.client;

import com.zynolo_nexus.contracts.modules.RoleModuleAccessDto;
import com.zynolo_nexus.contracts.modules.RoleModuleAccessUpdateRequest;
import com.zynolo_nexus.setting_service.dto.api.MessageResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "auth-service-role-modules", url = "${auth.service.url}")
public interface RoleModuleAccessClient {

    @PostMapping("/api/v1/role-modules/{roleCode}/get")
    MessageResponseDTO<RoleModuleAccessDto> getRoleModuleAccess(@PathVariable("roleCode") String roleCode);

    @PostMapping("/api/v1/role-modules/update")
    MessageResponseDTO<RoleModuleAccessDto> updateRoleModuleAccess(@RequestBody RoleModuleAccessUpdateRequest request);
}
