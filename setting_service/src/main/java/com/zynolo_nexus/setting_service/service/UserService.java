package com.zynolo_nexus.setting_service.service;

import com.zynolo_nexus.setting_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.setting_service.dto.request.CreateUserRequest;
import com.zynolo_nexus.setting_service.dto.request.UpdateUserRequest;
import com.zynolo_nexus.setting_service.dto.request.ProfileImageUpdateRequest;
import com.zynolo_nexus.setting_service.dto.request.UserFilterRequest;
import com.zynolo_nexus.setting_service.dto.request.UserReferenceDataRequest;
import com.zynolo_nexus.setting_service.dto.request.UserStatusUpdateRequest;
import com.zynolo_nexus.setting_service.dto.request.UserUpdateByIdRequest;
import com.zynolo_nexus.setting_service.dto.response.ProfileDetails;
import com.zynolo_nexus.setting_service.dto.response.UserFilterResultDto;
import com.zynolo_nexus.setting_service.dto.response.UserReferenceDataDto;

public interface UserService {

    MessageResponseDTO<ProfileDetails> createUser(CreateUserRequest request);

    MessageResponseDTO<ProfileDetails> getUser(String username);

    MessageResponseDTO<ProfileDetails> updateUser(String username, UpdateUserRequest request);

    MessageResponseDTO<String> deactivateUser(String username);

    MessageResponseDTO<UserReferenceDataDto> getReferenceData();

    MessageResponseDTO<UserReferenceDataDto> getReferenceData(UserReferenceDataRequest request);

    MessageResponseDTO<UserFilterResultDto> filterList(UserFilterRequest request);

    MessageResponseDTO<ProfileDetails> getUser(Long id);

    MessageResponseDTO<ProfileDetails> updateUser(UserUpdateByIdRequest request);

    MessageResponseDTO<ProfileDetails> updateStatus(UserStatusUpdateRequest request);

    MessageResponseDTO<ProfileDetails> updateProfileImage(ProfileImageUpdateRequest request);
}
