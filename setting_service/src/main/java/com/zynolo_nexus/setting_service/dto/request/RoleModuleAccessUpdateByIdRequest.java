package com.zynolo_nexus.setting_service.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class RoleModuleAccessUpdateByIdRequest {

    private String channel;
    private String ip;
    private String message;
    private String userAgent;
    private String username;

    private Long roleId;
    private String roleCode;
    private List<ModulePermission> modules;

    @Data
    public static class ModulePermission {
        private Long moduleId;
        private String moduleCode;
        private boolean canView;
    }
}
