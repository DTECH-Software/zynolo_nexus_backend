package com.zynolo_nexus.contracts.modules;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleModuleAccessDto {

    private String roleCode;

    private List<RoleModulePermissionItem> modules;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoleModulePermissionItem {
        private String moduleCode;
        private String moduleName;
        private boolean canView;
    }
}
