package com.zynolo_nexus.contracts.pages;

import lombok.Data;

@Data
public class PageRequest {

    private String sectionCode;
    private String code;
    private String name;
    private String description;
    private Integer sortOrder;
}
