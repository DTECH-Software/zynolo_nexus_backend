package com.zynolo_nexus.setting_service.dto.request;

import com.zynolo_nexus.contracts.modules.ModuleStatus;
import lombok.Data;

@Data
public class ModuleUpdateRequest {

    private String channel;
    private String ip;
    private String message;
    private String userAgent;
    private String username;

    private String code;
    private String name;
    private String description;
    private String url;
    private ModuleStatus status;
    private Integer sortOrder;
}
