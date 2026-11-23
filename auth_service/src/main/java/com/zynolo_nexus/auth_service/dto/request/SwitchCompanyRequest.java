package com.zynolo_nexus.auth_service.dto.request;

import lombok.Data;

@Data
public class SwitchCompanyRequest {

    private Long companyId;
    private String companyCode;
}
