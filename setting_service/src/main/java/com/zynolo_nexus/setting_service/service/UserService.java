package com.zynolo_nexus.setting_service.service;

import com.zynolo_nexus.setting_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.setting_service.dto.request.CreateUserRequest;
import com.zynolo_nexus.setting_service.dto.request.UpdateUserRequest;
import com.zynolo_nexus.setting_service.dto.response.ProfileDetails;

public interface UserService {

    MessageResponseDTO<ProfileDetails> createUser(CreateUserRequest request);

    MessageResponseDTO<ProfileDetails> getUser(String username);

    MessageResponseDTO<ProfileDetails> updateUser(String username, UpdateUserRequest request);

    MessageResponseDTO<String> deactivateUser(String username);
}
