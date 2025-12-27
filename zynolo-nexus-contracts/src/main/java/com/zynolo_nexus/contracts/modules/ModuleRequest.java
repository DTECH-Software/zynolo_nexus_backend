package com.zynolo_nexus.contracts.modules;

import lombok.Data;

@Data
public class ModuleRequest {

    private String code;
    private String name;
    private String description;
    private Integer sortOrder;
}
