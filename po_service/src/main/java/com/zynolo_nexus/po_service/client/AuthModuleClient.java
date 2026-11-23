package com.zynolo_nexus.po_service.client;

import com.zynolo_nexus.contracts.pages.RolePageTaskAccessDto;
import com.zynolo_nexus.po_service.config.FeignInternalAuthConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(
        name = "auth-service",
        url = "${auth.service.url}",
        configuration = FeignInternalAuthConfig.class
)
public interface AuthModuleClient {

    @PostMapping("/internal/role-page-tasks/{roleCode}/get")
    RolePageTaskAccessDto getRolePageTaskAccess(@PathVariable("roleCode") String roleCode);
}
