package com.zynolo_nexus.setting_service.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class RoleModuleAccessUpdateRequest {

    private String roleCode; // SPADMIN, ADMIN, etc.

    private List<ModulePermission> modules;

    @Data
    public static class ModulePermission {
        private String moduleCode;
        private boolean canView;
    }
}
