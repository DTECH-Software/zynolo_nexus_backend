package com.zynolo_nexus.setting_service.service.impl;

import com.zynolo_nexus.setting_service.client.AuthModuleClient;
import com.zynolo_nexus.setting_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.setting_service.dto.request.UsernamePolicyReferenceDataRequest;
import com.zynolo_nexus.setting_service.dto.request.UsernamePolicyResetRequest;
import com.zynolo_nexus.setting_service.dto.request.UsernamePolicyUpdateRequest;
import com.zynolo_nexus.setting_service.dto.request.UsernamePolicyViewRequest;
import com.zynolo_nexus.setting_service.dto.response.UsernamePolicyDto;
import com.zynolo_nexus.setting_service.dto.response.UsernamePolicyPrivilegesDto;
import com.zynolo_nexus.setting_service.dto.response.UsernamePolicyReferenceDataDto;
import com.zynolo_nexus.setting_service.exception.BadRequestException;
import com.zynolo_nexus.setting_service.exception.NotFoundException;
import com.zynolo_nexus.setting_service.model.User;
import com.zynolo_nexus.setting_service.model.UsernamePolicy;
import com.zynolo_nexus.setting_service.repository.UserRepository;
import com.zynolo_nexus.setting_service.repository.UsernamePolicyRepository;
import com.zynolo_nexus.setting_service.service.UsernamePolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UsernamePolicyServiceImpl implements UsernamePolicyService {

    private static final String USERNAME_POLICY_PAGE_CODE = "UNPM";

    private final UsernamePolicyRepository usernamePolicyRepository;
    private final MessageSource messageSource;
    private final UserRepository userRepository;
    private final AuthModuleClient authModuleClient;

    @Value("${username.policy.default.minUpperCase:1}")
    private int defaultMinUpperCase;
    @Value("${username.policy.default.minLowerCase:4}")
    private int defaultMinLowerCase;
    @Value("${username.policy.default.minNumbers:1}")
    private int defaultMinNumbers;
    @Value("${username.policy.default.minSpecialCharacters:1}")
    private int defaultMinSpecialCharacters;
    @Value("${username.policy.default.minLength:1}")
    private int defaultMinLength;
    @Value("${username.policy.default.maxLength:15}")
    private int defaultMaxLength;

    @Override
    public MessageResponseDTO<UsernamePolicyReferenceDataDto> getReferenceData(UsernamePolicyReferenceDataRequest request) {
        UsernamePolicyPrivilegesDto privileges = resolvePrivileges(request);
        UsernamePolicyReferenceDataDto data = UsernamePolicyReferenceDataDto.builder()
                .privileges(privileges)
                .build();

        String message = messageSource.getMessage("username.policy.reference.success", null, LocaleContextHolder.getLocale());
        return MessageResponseDTO.<UsernamePolicyReferenceDataDto>builder()
                .success(true)
                .message(message)
                .data(data)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    public MessageResponseDTO<UsernamePolicyDto> viewPolicy(UsernamePolicyViewRequest request) {
        UsernamePolicy policy = getOrCreatePolicy();
        String message = messageSource.getMessage(
                "username.policy.view.success",
                new Object[]{policy.getId()},
                LocaleContextHolder.getLocale()
        );
        return MessageResponseDTO.<UsernamePolicyDto>builder()
                .success(true)
                .message(message)
                .data(toDto(policy))
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    public MessageResponseDTO<UsernamePolicyDto> updatePolicy(UsernamePolicyUpdateRequest request) {
        if (request == null) {
            throw new BadRequestException("username.policy.invalid");
        }

        UsernamePolicy policy = getOrCreatePolicy();

        if (request.getMinUpperCase() != null) {
            policy.setMinUpperCase(request.getMinUpperCase());
        }
        if (request.getMinLowerCase() != null) {
            policy.setMinLowerCase(request.getMinLowerCase());
        }
        if (request.getMinNumbers() != null) {
            policy.setMinNumbers(request.getMinNumbers());
        }
        if (request.getMinSpecialCharacters() != null) {
            policy.setMinSpecialCharacters(request.getMinSpecialCharacters());
        }
        if (request.getMinLength() != null) {
            policy.setMinLength(request.getMinLength());
        }
        if (request.getMaxLength() != null) {
            policy.setMaxLength(request.getMaxLength());
        }

        validatePolicy(policy);
        UsernamePolicy saved = usernamePolicyRepository.save(policy);
        String message = messageSource.getMessage("username.policy.update.success", null, LocaleContextHolder.getLocale());
        return MessageResponseDTO.<UsernamePolicyDto>builder()
                .success(true)
                .message(message)
                .data(toDto(saved))
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    public MessageResponseDTO<UsernamePolicyDto> resetPolicy(UsernamePolicyResetRequest request) {
        UsernamePolicy policy = getOrCreatePolicy();
        applyDefaults(policy);
        UsernamePolicy saved = usernamePolicyRepository.save(policy);
        String message = messageSource.getMessage("username.policy.reset.success", null, LocaleContextHolder.getLocale());
        return MessageResponseDTO.<UsernamePolicyDto>builder()
                .success(true)
                .message(message)
                .data(toDto(saved))
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    private UsernamePolicy getOrCreatePolicy() {
        return usernamePolicyRepository.findTopByOrderByIdAsc()
                .orElseGet(() -> usernamePolicyRepository.save(buildDefaultPolicy()));
    }

    private UsernamePolicy buildDefaultPolicy() {
        return UsernamePolicy.builder()
                .minUpperCase(defaultMinUpperCase)
                .minLowerCase(defaultMinLowerCase)
                .minNumbers(defaultMinNumbers)
                .minSpecialCharacters(defaultMinSpecialCharacters)
                .minLength(defaultMinLength)
                .maxLength(defaultMaxLength)
                .build();
    }

    private void applyDefaults(UsernamePolicy policy) {
        policy.setMinUpperCase(defaultMinUpperCase);
        policy.setMinLowerCase(defaultMinLowerCase);
        policy.setMinNumbers(defaultMinNumbers);
        policy.setMinSpecialCharacters(defaultMinSpecialCharacters);
        policy.setMinLength(defaultMinLength);
        policy.setMaxLength(defaultMaxLength);
    }

    private void validatePolicy(UsernamePolicy policy) {
        if (policy == null) {
            throw new BadRequestException("username.policy.invalid");
        }

        if (isNegative(policy.getMinUpperCase())
                || isNegative(policy.getMinLowerCase())
                || isNegative(policy.getMinNumbers())
                || isNegative(policy.getMinSpecialCharacters())
                || isNegative(policy.getMinLength())
                || isNegative(policy.getMaxLength())) {
            throw new BadRequestException("username.policy.invalid");
        }

        if (policy.getMinLength() != null && policy.getMaxLength() != null
                && policy.getMinLength() > policy.getMaxLength()) {
            throw new BadRequestException("username.policy.invalid");
        }
    }

    private boolean isNegative(Integer value) {
        return value != null && value < 0;
    }

    private UsernamePolicyDto toDto(UsernamePolicy policy) {
        return UsernamePolicyDto.builder()
                .id(policy.getId())
                .minUpperCase(policy.getMinUpperCase())
                .minLowerCase(policy.getMinLowerCase())
                .minNumbers(policy.getMinNumbers())
                .minSpecialCharacters(policy.getMinSpecialCharacters())
                .minLength(policy.getMinLength())
                .maxLength(policy.getMaxLength())
                .build();
    }

    private UsernamePolicyPrivilegesDto resolvePrivileges(UsernamePolicyReferenceDataRequest request) {
        String username = request != null ? request.getUsername() : null;
        if (!StringUtils.hasText(username)) {
            username = getAuthenticatedUsername();
        }

        if (!StringUtils.hasText(username)) {
            return UsernamePolicyPrivilegesDto.builder()
                    .view(false)
                    .update(false)
                    .reset(false)
                    .build();
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("user.fetch.notfound"));

        String roleCode = user.getRole() != null && user.getRole().getCode() != null
                ? user.getRole().getCode().name()
                : null;

        if (!StringUtils.hasText(roleCode)) {
            return UsernamePolicyPrivilegesDto.builder()
                    .view(false)
                    .update(false)
                    .reset(false)
                    .build();
        }

        var access = authModuleClient.getRolePageTaskAccess(roleCode);
        if (access == null || access.getPages() == null) {
            return UsernamePolicyPrivilegesDto.builder()
                    .view(false)
                    .update(false)
                    .reset(false)
                    .build();
        }

        Map<String, Boolean> taskAccess = new HashMap<>();
        access.getPages().stream()
                .filter(page -> USERNAME_POLICY_PAGE_CODE.equalsIgnoreCase(page.getPageCode()))
                .findFirst()
                .ifPresent(page -> {
                    if (page.getTasks() != null) {
                        page.getTasks().forEach(task -> {
                            String codeKey = normalizeTaskKey(task.getTaskCode());
                            if (StringUtils.hasText(codeKey)) {
                                taskAccess.put(codeKey, task.isCanAccess());
                            }
                            String nameKey = normalizeTaskKey(task.getTaskName());
                            if (StringUtils.hasText(nameKey)) {
                                taskAccess.putIfAbsent(nameKey, task.isCanAccess());
                            }
                        });
                    }
                });

        boolean view = hasTask(taskAccess, "VIEW", "READ");
        boolean update = hasTask(taskAccess, "UPDATE", "EDIT");
        boolean reset = hasTask(taskAccess, "RESET") || update;

        return UsernamePolicyPrivilegesDto.builder()
                .view(view)
                .update(update)
                .reset(reset)
                .build();
    }

    private String normalizeTaskKey(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.replaceAll("[^A-Za-z0-9]", "").toUpperCase(Locale.ROOT);
    }

    private boolean hasTask(Map<String, Boolean> taskAccess, String... tokens) {
        if (taskAccess == null || taskAccess.isEmpty() || tokens == null) {
            return false;
        }
        for (Map.Entry<String, Boolean> entry : taskAccess.entrySet()) {
            if (!Boolean.TRUE.equals(entry.getValue())) {
                continue;
            }
            String key = entry.getKey();
            if (!StringUtils.hasText(key)) {
                continue;
            }
            for (String token : tokens) {
                if (StringUtils.hasText(token) && key.contains(token)) {
                    return true;
                }
            }
        }
        return false;
    }

    private String getAuthenticatedUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal == null || "anonymousUser".equals(principal)) {
            return null;
        }
        return authentication.getName();
    }
}
