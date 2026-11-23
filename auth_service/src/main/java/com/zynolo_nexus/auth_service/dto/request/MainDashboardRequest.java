package com.zynolo_nexus.auth_service.dto.request;

import lombok.Data;

@Data
public class MainDashboardRequest {

    private String channel;
    private String ip;
    private String message;
    private String userAgent;
    private String username;
}
