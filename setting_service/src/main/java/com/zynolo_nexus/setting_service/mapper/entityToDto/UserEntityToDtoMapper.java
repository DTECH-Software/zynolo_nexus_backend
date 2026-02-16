package com.zynolo_nexus.setting_service.mapper.entityToDto;

import org.springframework.stereotype.Component;

import com.zynolo_nexus.setting_service.dto.response.ProfileDetails;
import com.zynolo_nexus.setting_service.dto.response.ProfileImageDto;
import com.zynolo_nexus.setting_service.dto.response.UserRoleDto;
import com.zynolo_nexus.setting_service.model.User;
import org.springframework.util.StringUtils;

@Component
public class UserEntityToDtoMapper {

    public ProfileDetails toProfileDetails(User user) {
        UserRoleDto roleDto = new UserRoleDto(
                user.getRole().getId(),
                user.getRole().getCode(),
                user.getRole().getDescription()
        );

        ProfileImageDto profileImage = null;
        if (StringUtils.hasText(user.getProfileImgDoc())
                || StringUtils.hasText(user.getProfileImgFileName())
                || StringUtils.hasText(user.getProfileImgFileType())) {
            profileImage = ProfileImageDto.builder()
                    .type(user.getProfileImgFileType())
                    .fileName(user.getProfileImgFileName())
                    .fileType(user.getProfileImgFileType())
                    .doc(user.getProfileImgDoc())
                    .build();
        }

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
                .statusDescription(statusDescription(user))
                .loginStatus(user.getLoginStatus() != null ? user.getLoginStatus().name() : null)
                .loginStatusDescription(loginStatusDescription(user))
                .userRole(roleDto)
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .nic(user.getNic())
                .company(user.getCompany())
                .lastLoggedDate(user.getLastLoggedDate())
                .expectingFirstTimeLogging(user.getExpectingFirstTimeLogging())
                .passwordExpiredDate(user.getPasswordExpiredDate())
                .profileImg(profileImage)
                .reset(user.getReset())
                .build();
    }

    private String statusDescription(User user) {
        if (user == null || user.getStatus() == null) {
            return null;
        }
        return switch (user.getStatus()) {
            case ACTIVE -> "Active";
            case INACTIVE -> "Inactive";
            case LOCKED -> "Locked";
        };
    }

    private String loginStatusDescription(User user) {
        if (user == null || user.getLoginStatus() == null) {
            return null;
        }
        return switch (user.getLoginStatus()) {
            case ACTIVE -> "Active";
            case DISABLED -> "Disabled";
            case PASSWORD_EXPIRED -> "Password Expired";
        };
    }
}
