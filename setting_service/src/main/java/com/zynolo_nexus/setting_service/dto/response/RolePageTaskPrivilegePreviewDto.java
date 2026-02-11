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
public class RolePageTaskPrivilegePreviewDto {

    private Long roleId;
    private String roleCode;
    private Long pageId;
    private String pageCode;
    private List<RolePageTaskPrivilegePreviewTaskDto> tasks;
}
