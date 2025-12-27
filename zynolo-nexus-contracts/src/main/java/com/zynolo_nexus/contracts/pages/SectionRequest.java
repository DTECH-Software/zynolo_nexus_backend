package com.zynolo_nexus.contracts.pages;

import lombok.Data;

@Data
public class SectionRequest {

    private String moduleCode;
    private String code;
    private String name;
    private String description;
    private Integer sortOrder;
}
