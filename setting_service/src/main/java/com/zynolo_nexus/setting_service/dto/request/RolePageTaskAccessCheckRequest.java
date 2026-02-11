package com.zynolo_nexus.setting_service.dto.request;

import lombok.Data;

@Data
public class RolePageTaskAccessCheckRequest {

    private String channel;
    private String ip;
    private String message;
    private String userAgent;
    private String username;

    private Long roleId;
    private String roleCode;
    private Long pageId;
    private String pageCode;
    private Long taskId;
    private String taskCode;
}
