package com.zynolo_nexus.setting_service.dto.request.validator;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.zynolo_nexus.setting_service.dto.request.UpdateUserRequest;
import com.zynolo_nexus.setting_service.exception.BadRequestException;

@Component
public class UpdateUserRequestValidator {

    public void validate(UpdateUserRequest request) {
        if (request == null) {
            throw new BadRequestException("user.update.invalid");
        }
        if (StringUtils.hasText(request.getEmail()) && !request.getEmail().contains("@")) {
            throw new BadRequestException("user.create.email.invalid");
        }
    }
}
