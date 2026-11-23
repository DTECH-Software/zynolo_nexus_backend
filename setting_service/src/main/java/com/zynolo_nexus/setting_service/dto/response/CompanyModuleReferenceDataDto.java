package com.zynolo_nexus.setting_service.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyModuleReferenceDataDto {

    private List<ReferenceCompanyDto> companies;
    private List<CompanyModuleReferenceModuleDto> modules;
    private List<ReferenceStatusDto> defaultStatus;
    private CompanyModulePrivilegesDto privileges;
}
