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
public class RolePageTaskAccessReferenceDataDto {

    private List<RolePageTaskAccessReferenceRoleDto> roles;
    private List<RolePageTaskAccessReferencePageDto> pages;
    private List<RolePageTaskAccessReferenceTaskDto> tasks;
    private RolePageTaskAccessPrivilegesDto privileges;
}
