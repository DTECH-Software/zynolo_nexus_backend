package com.zynolo_nexus.setting_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleModulePrivilegeCheckDto {

    private Long roleId;
    private String roleCode;
    private Long moduleId;
    private String moduleCode;
    private boolean canView;
}
