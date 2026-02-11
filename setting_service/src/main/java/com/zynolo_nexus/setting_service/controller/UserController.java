package com.zynolo_nexus.setting_service.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zynolo_nexus.setting_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.setting_service.dto.request.CreateUserRequest;
import com.zynolo_nexus.setting_service.dto.request.UserFilterRequest;
import com.zynolo_nexus.setting_service.dto.request.UserReferenceDataRequest;
import com.zynolo_nexus.setting_service.dto.request.UserStatusUpdateRequest;
import com.zynolo_nexus.setting_service.dto.request.UpdateUserRequest;
import com.zynolo_nexus.setting_service.dto.request.UserUpdateByIdRequest;
import com.zynolo_nexus.setting_service.dto.request.UserViewRequest;
import com.zynolo_nexus.setting_service.dto.response.ProfileDetails;
import com.zynolo_nexus.setting_service.dto.response.UserFilterResultDto;
import com.zynolo_nexus.setting_service.dto.response.UserReferenceDataDto;
import com.zynolo_nexus.setting_service.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/setting/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public MessageResponseDTO<ProfileDetails> createUser(@RequestBody CreateUserRequest request) {
        return userService.createUser(request);
    }

    @PostMapping("/{username}/get")
    public MessageResponseDTO<ProfileDetails> getUser(@PathVariable String username) {
        return userService.getUser(username);
    }

    @PostMapping("/view")
    public MessageResponseDTO<ProfileDetails> getUserById(@RequestBody UserViewRequest request) {
        Long id = request != null ? request.getId() : null;
        return userService.getUser(id);
    }

    @PostMapping("/{username}/update")
    public MessageResponseDTO<ProfileDetails> updateUser(@PathVariable String username,
                                                         @RequestBody UpdateUserRequest request) {
        return userService.updateUser(username, request);
    }

    @PostMapping("/update")
    public MessageResponseDTO<ProfileDetails> updateUserById(@RequestBody UserUpdateByIdRequest request) {
        return userService.updateUser(request);
    }

    @PostMapping("/{username}/deactivate")
    public MessageResponseDTO<String> deactivateUser(@PathVariable String username) {
        return userService.deactivateUser(username);
    }

    @PostMapping("/status")
    public MessageResponseDTO<ProfileDetails> updateStatus(@RequestBody UserStatusUpdateRequest request) {
        return userService.updateStatus(request);
    }

    @PostMapping("/filter-list")
    public MessageResponseDTO<UserFilterResultDto> filterList(@RequestBody UserFilterRequest request) {
        return userService.filterList(request);
    }

    @PostMapping("/reference-data")
    public MessageResponseDTO<UserReferenceDataDto> referenceData(
            @RequestBody(required = false) UserReferenceDataRequest request) {
        return userService.getReferenceData(request);
    }
}
