package com.zynolo_nexus.setting_service.dto.request;

import com.zynolo_nexus.setting_service.enums.CompanyStatus;
import lombok.Data;

@Data
public class CompanyRequest {

    private String code;
    private String description;
    private CompanyStatus status;
}
