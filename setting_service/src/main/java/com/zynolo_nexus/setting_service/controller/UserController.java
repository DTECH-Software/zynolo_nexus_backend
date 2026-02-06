package com.zynolo_nexus.setting_service.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zynolo_nexus.setting_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.setting_service.dto.request.CreateUserRequest;
import com.zynolo_nexus.setting_service.dto.request.UpdateUserRequest;
import com.zynolo_nexus.setting_service.dto.response.ProfileDetails;
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

    @PostMapping("/{username}/update")
    public MessageResponseDTO<ProfileDetails> updateUser(@PathVariable String username,
                                                         @RequestBody UpdateUserRequest request) {
        return userService.updateUser(username, request);
    }

    @PostMapping("/{username}/deactivate")
    public MessageResponseDTO<String> deactivateUser(@PathVariable String username) {
        return userService.deactivateUser(username);
    }

    @PostMapping("/reference-data")
    public MessageResponseDTO<UserReferenceDataDto> referenceData() {
        return userService.getReferenceData();
    }
}
