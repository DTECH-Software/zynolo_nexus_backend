package com.zynolo_nexus.setting_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyModuleBulkItemDto {

    private Long subscriptionId;
    private Long moduleId;
    private String moduleCode;
    private String moduleDescription;
    private boolean allowed;
    private String status;
    private String statusDescription;
}

