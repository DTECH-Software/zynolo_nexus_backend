package com.zynolo_nexus.setting_service.dto.request;

import lombok.Data;

@Data
public class CompanyFilterSearch {

    private String code;
    private String description;
    private String status;
}
