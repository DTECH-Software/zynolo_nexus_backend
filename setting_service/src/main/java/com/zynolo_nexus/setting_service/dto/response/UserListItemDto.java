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
public class UserListItemDto {

    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
    private String createdBy;
    private String lastModifiedBy;
    private Long id;
    private String username;
    private String roleCode;
    private String roleDescription;
    private String nic;
    private String email;
    private String mobile;
    private String firstName;
    private String lastName;
    private String status;
    private String statusDescription;
    private String loginStatus;
    private String loginStatusDescription;
}
