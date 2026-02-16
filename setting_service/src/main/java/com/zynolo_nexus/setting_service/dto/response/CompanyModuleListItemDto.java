package com.zynolo_nexus.setting_service.dto.response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyModuleListItemDto {

    private Long id;
    private String companyCode;
    private String companyDescription;
    private String moduleCode;
    private String moduleDescription;
    private String status;
    private String statusDescription;
    private boolean enabled;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
    private String createdBy;
    private String lastModifiedBy;
}
