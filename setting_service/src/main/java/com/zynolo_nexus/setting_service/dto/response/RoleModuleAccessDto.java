package com.zynolo_nexus.setting_service.dto.response;

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

    private String roleCode; // e.g. SPADMIN, ADMIN, PO_USER

    private List<RoleModulePermissionItem> modules;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoleModulePermissionItem {
        private String moduleCode;    // e.g. PO
        private String moduleName;    // e.g. PO Module
        private boolean canView;
    }
}
