package com.zynolo_nexus.setting_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RolePageTaskAccessReferencePageDto {

    private Long id;
    private String code;
    private String name;
    private Long sectionId;
    private String sectionCode;
    private String sectionName;
    private String moduleCode;
    private String moduleName;
}
