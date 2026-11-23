package com.zynolo_nexus.setting_service.dto.request;

import lombok.Data;

@Data
public class TaskFilterSearch {

    private String code;
    private String description;
    private String status;
}
