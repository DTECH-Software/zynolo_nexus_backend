package com.zynolo_nexus.contracts.pages;

import lombok.Data;

@Data
public class TaskRequest {

    private String code;
    private String name;
    private String description;
    private Integer sortOrder;
    private PageStatus status;
    private String username;
}
