package com.zynolo_nexus.setting_service.dto.request.validator;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.zynolo_nexus.setting_service.dto.request.CreateUserRequest;
import com.zynolo_nexus.setting_service.exception.BadRequestException;

@Component
public class CreateUserRequestValidator {

    public void validate(CreateUserRequest request) {
        if (request == null) {
            throw new BadRequestException("user.create.invalid");
        }
        if (!StringUtils.hasText(request.getUsername())) {
            throw new BadRequestException("user.create.username.required");
        }
        if (!StringUtils.hasText(request.getPassword())) {
            throw new BadRequestException("user.create.password.required");
        }
        if (!StringUtils.hasText(request.getRoleCode())) {
            throw new BadRequestException("user.create.role.required");
        }
        if (StringUtils.hasText(request.getEmail()) && !request.getEmail().contains("@")) {
            throw new BadRequestException("user.create.email.invalid");
        }
    }
}
