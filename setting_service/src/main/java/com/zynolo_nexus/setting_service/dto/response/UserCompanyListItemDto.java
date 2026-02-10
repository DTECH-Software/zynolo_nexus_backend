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
public class UserCompanyListItemDto {

    private Long id;
    private Long userId;
    private String username;
    private Long companyId;
    private String companyCode;
    private Long roleId;
    private String roleCode;
    private String status;
    private String statusDescription;
    private boolean isDefault;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
    private String createdBy;
    private String lastModifiedBy;
}
