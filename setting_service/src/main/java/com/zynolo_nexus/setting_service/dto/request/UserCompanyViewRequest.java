package com.zynolo_nexus.setting_service.dto.request;

import lombok.Data;

@Data
public class UserCompanyViewRequest {

    private String channel;
    private String ip;
    private String message;
    private String userAgent;
    private String username;
    private Long id;
}
