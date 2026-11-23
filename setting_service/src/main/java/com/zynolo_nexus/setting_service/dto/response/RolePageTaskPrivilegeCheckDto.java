package com.zynolo_nexus.setting_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RolePageTaskPrivilegeCheckDto {

    private Long roleId;
    private String roleCode;
    private Long pageId;
    private String pageCode;
    private Long taskId;
    private String taskCode;
    private boolean canAccess;
}
