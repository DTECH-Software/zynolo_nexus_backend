package com.zynolo_nexus.setting_service.dto.request;

import lombok.Data;

@Data
public class ModuleRequest {

    private String code;        // unique code
    private String name;        // display name
    private String description;
    private Integer sortOrder;
}
