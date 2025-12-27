package com.zynolo_nexus.setting_service.mapper.entityToDto;

import org.springframework.stereotype.Component;

import com.zynolo_nexus.setting_service.dto.response.ProfileDetails;
import com.zynolo_nexus.setting_service.dto.response.UserRoleDto;
import com.zynolo_nexus.setting_service.model.User;

@Component
public class UserEntityToDtoMapper {

    public ProfileDetails toProfileDetails(User user) {
        UserRoleDto roleDto = new UserRoleDto(
                user.getRole().getId(),
                user.getRole().getCode().name(),
                user.getRole().getDescription()
        );

        return ProfileDetails.builder()
                .createdDate(user.getCreatedDate() != null ? user.getCreatedDate().toLocalDate() : null)
                .lastModifiedDate(user.getLastModifiedDate() != null ? user.getLastModifiedDate().toLocalDate() : null)
                .createdBy(user.getCreatedBy())
                .lastModifiedBy(user.getLastModifiedBy())
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .mobile(user.getMobile())
                .status(user.getStatus() != null ? user.getStatus().name() : null)
                .statusDescription("Active")
                .loginStatus(user.getLoginStatus() != null ? user.getLoginStatus().name() : null)
                .loginStatusDescription("Active")
                .userRole(roleDto)
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .nic(user.getNic())
                .company(user.getCompany())
                .lastLoggedDate(user.getLastLoggedDate())
                .expectingFirstTimeLogging(user.getExpectingFirstTimeLogging())
                .passwordExpiredDate(user.getPasswordExpiredDate())
                .proImg(null)
                .reset(user.getReset())
                .build();
    }
}
