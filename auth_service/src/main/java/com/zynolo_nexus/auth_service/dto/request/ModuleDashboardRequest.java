package com.zynolo_nexus.auth_service.dto.request;

import lombok.Data;

@Data
public class ModuleDashboardRequest {

    private String channel;
    private String ip;
    private String message;
    private String userAgent;
    private String username;
    private String moduleCode;
}
