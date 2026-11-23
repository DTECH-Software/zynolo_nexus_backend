package com.zynolo_nexus.setting_service.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class RolePageTaskAccessUpdateByIdRequest {

    private String channel;
    private String ip;
    private String message;
    private String userAgent;
    private String username;

    private Long roleId;
    private String roleCode;
    private List<PageTaskPermission> tasks;

    @Data
    public static class PageTaskPermission {
        private Long pageId;
        private String pageCode;
        private Long taskId;
        private String taskCode;
        private boolean canAccess;
    }
}
