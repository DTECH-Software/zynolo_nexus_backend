package com.zynolo_nexus.setting_service.dto.request;

import lombok.Data;

@Data
public class PasswordPolicyReferenceDataRequest {

    private String channel;
    private String ip;
    private String message;
    private String userAgent;
    private String username;
}
