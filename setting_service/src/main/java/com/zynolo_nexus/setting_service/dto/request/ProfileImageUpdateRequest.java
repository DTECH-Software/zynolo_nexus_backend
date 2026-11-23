package com.zynolo_nexus.setting_service.dto.request;

import lombok.Data;

@Data
public class ProfileImageUpdateRequest {

    private String channel;
    private String ip;
    private String message;
    private String userAgent;
    private String username;
    private String type;
    private String file;
    private String fileName;
    private String fileType;
}
