package com.zynolo_nexus.auth_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurrentUserDto {

    private String username;
    private String displayName;
    private String email;
    private String mobile;
    private String roleCode; // e.g. SPADMIN, ADMIN, USER
}
