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
public class UserReferenceDataDto {

    private List<ReferenceCompanyDto> companies;
    private List<ReferenceRoleDto> roles;
    private List<ReferenceStatusDto> userStatus;
    private List<ReferenceStatusDto> loginStatus;
    private UsernamePolicyDto usernamePolicy;
    private PasswordPolicyDto passwordPolicy;
    private UserPrivilegesDto privileges;
}
