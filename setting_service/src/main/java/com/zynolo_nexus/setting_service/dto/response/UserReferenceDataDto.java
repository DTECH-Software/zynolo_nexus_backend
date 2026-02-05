package com.zynolo_nexus.setting_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserReferenceDataDto {

    private List<ReferenceCompanyDto> companies;
    private List<ReferenceRoleDto> roles;
}
