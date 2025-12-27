package com.zynolo_nexus.contracts.modules;

import lombok.Data;

import java.util.List;

@Data
public class RoleModuleAccessUpdateRequest {

    private String roleCode;

    private List<ModulePermission> modules;

    @Data
    public static class ModulePermission {
        private String moduleCode;
        private boolean canView;
    }
}
