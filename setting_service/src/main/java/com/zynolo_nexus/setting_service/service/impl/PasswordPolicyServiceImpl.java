package com.zynolo_nexus.setting_service.service.impl;

import com.zynolo_nexus.setting_service.client.AuthModuleClient;
import com.zynolo_nexus.setting_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.setting_service.dto.request.PasswordPolicyReferenceDataRequest;
import com.zynolo_nexus.setting_service.dto.request.PasswordPolicyResetRequest;
import com.zynolo_nexus.setting_service.dto.request.PasswordPolicyUpdateRequest;
import com.zynolo_nexus.setting_service.dto.request.PasswordPolicyViewRequest;
import com.zynolo_nexus.setting_service.dto.response.PasswordPolicyDto;
import com.zynolo_nexus.setting_service.dto.response.PasswordPolicyPrivilegesDto;
import com.zynolo_nexus.setting_service.dto.response.PasswordPolicyReferenceDataDto;
import com.zynolo_nexus.setting_service.exception.BadRequestException;
import com.zynolo_nexus.setting_service.exception.NotFoundException;
import com.zynolo_nexus.setting_service.model.PasswordPolicy;
import com.zynolo_nexus.setting_service.model.User;
import com.zynolo_nexus.setting_service.repository.PasswordPolicyRepository;
import com.zynolo_nexus.setting_service.repository.UserRepository;
import com.zynolo_nexus.setting_service.service.PasswordPolicyService;
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
public class PasswordPolicyServiceImpl implements PasswordPolicyService {

    private static final String PASSWORD_POLICY_PAGE_CODE = "PSPM";

    private final PasswordPolicyRepository passwordPolicyRepository;
    private final MessageSource messageSource;
    private final UserRepository userRepository;
    private final AuthModuleClient authModuleClient;

    @Value("${password.policy.default.minUpperCase:1}")
    private int defaultMinUpperCase;
    @Value("${password.policy.default.minLowerCase:1}")
    private int defaultMinLowerCase;
    @Value("${password.policy.default.minNumbers:1}")
    private int defaultMinNumbers;
    @Value("${password.policy.default.minSpecialCharacters:1}")
    private int defaultMinSpecialCharacters;
    @Value("${password.policy.default.minLength:3}")
    private int defaultMinLength;
    @Value("${password.policy.default.maxLength:10}")
    private int defaultMaxLength;
    @Value("${password.policy.default.passwordHistory:3}")
    private int defaultPasswordHistory;
    @Value("${password.policy.default.attemptExceedCount:1}")
    private int defaultAttemptExceedCount;
    @Value("${password.policy.default.otpExceedCount:1}")
    private int defaultOtpExceedCount;

    @Override
    public MessageResponseDTO<PasswordPolicyReferenceDataDto> getReferenceData(PasswordPolicyReferenceDataRequest request) {
        PasswordPolicyPrivilegesDto privileges = resolvePrivileges(request);
        PasswordPolicyReferenceDataDto data = PasswordPolicyReferenceDataDto.builder()
                .privileges(privileges)
                .build();

        String message = messageSource.getMessage("password.policy.reference.success", null, LocaleContextHolder.getLocale());
        return MessageResponseDTO.<PasswordPolicyReferenceDataDto>builder()
                .success(true)
                .message(message)
                .data(data)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    public MessageResponseDTO<PasswordPolicyDto> viewPolicy(PasswordPolicyViewRequest request) {
        PasswordPolicy policy = getOrCreatePolicy();
        return buildResponse(toDto(policy), "password.policy.view.success");
    }

    @Override
    public MessageResponseDTO<PasswordPolicyDto> updatePolicy(PasswordPolicyUpdateRequest request) {
        if (request == null) {
            throw new BadRequestException("password.policy.invalid");
        }

        PasswordPolicy policy = getOrCreatePolicy();

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
        if (request.getPasswordHistory() != null) {
            policy.setPasswordHistory(request.getPasswordHistory());
        }
        if (request.getAttemptExceedCount() != null) {
            policy.setAttemptExceedCount(request.getAttemptExceedCount());
        }
        if (request.getOtpExceedCount() != null) {
            policy.setOtpExceedCount(request.getOtpExceedCount());
        }

        validatePolicy(policy);
        PasswordPolicy saved = passwordPolicyRepository.save(policy);
        return buildResponse(toDto(saved), "password.policy.update.success");
    }

    @Override
    public MessageResponseDTO<PasswordPolicyDto> resetPolicy(PasswordPolicyResetRequest request) {
        PasswordPolicy policy = getOrCreatePolicy();
        applyDefaults(policy);
        PasswordPolicy saved = passwordPolicyRepository.save(policy);
        return buildResponse(toDto(saved), "password.policy.reset.success");
    }

    private PasswordPolicy getOrCreatePolicy() {
        return passwordPolicyRepository.findTopByOrderByIdAsc()
                .orElseGet(() -> passwordPolicyRepository.save(buildDefaultPolicy()));
    }

    private PasswordPolicy buildDefaultPolicy() {
        return PasswordPolicy.builder()
                .minUpperCase(defaultMinUpperCase)
                .minLowerCase(defaultMinLowerCase)
                .minNumbers(defaultMinNumbers)
                .minSpecialCharacters(defaultMinSpecialCharacters)
                .minLength(defaultMinLength)
                .maxLength(defaultMaxLength)
                .passwordHistory(defaultPasswordHistory)
                .attemptExceedCount(defaultAttemptExceedCount)
                .otpExceedCount(defaultOtpExceedCount)
                .build();
    }

    private void applyDefaults(PasswordPolicy policy) {
        policy.setMinUpperCase(defaultMinUpperCase);
        policy.setMinLowerCase(defaultMinLowerCase);
        policy.setMinNumbers(defaultMinNumbers);
        policy.setMinSpecialCharacters(defaultMinSpecialCharacters);
        policy.setMinLength(defaultMinLength);
        policy.setMaxLength(defaultMaxLength);
        policy.setPasswordHistory(defaultPasswordHistory);
        policy.setAttemptExceedCount(defaultAttemptExceedCount);
        policy.setOtpExceedCount(defaultOtpExceedCount);
    }

    private void validatePolicy(PasswordPolicy policy) {
        if (policy == null) {
            throw new BadRequestException("password.policy.invalid");
        }

        if (isNegative(policy.getMinUpperCase())
                || isNegative(policy.getMinLowerCase())
                || isNegative(policy.getMinNumbers())
                || isNegative(policy.getMinSpecialCharacters())
                || isNegative(policy.getMinLength())
                || isNegative(policy.getMaxLength())
                || isNegative(policy.getPasswordHistory())
                || isNegative(policy.getAttemptExceedCount())
                || isNegative(policy.getOtpExceedCount())) {
            throw new BadRequestException("password.policy.invalid");
        }

        if (policy.getMinLength() != null && policy.getMaxLength() != null
                && policy.getMinLength() > policy.getMaxLength()) {
            throw new BadRequestException("password.policy.invalid");
        }
    }

    private boolean isNegative(Integer value) {
        return value != null && value < 0;
    }

    private MessageResponseDTO<PasswordPolicyDto> buildResponse(PasswordPolicyDto dto, String messageKey) {
        String message = messageSource.getMessage(messageKey, null, LocaleContextHolder.getLocale());
        return MessageResponseDTO.<PasswordPolicyDto>builder()
                .success(true)
                .message(message)
                .data(dto)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    private PasswordPolicyDto toDto(PasswordPolicy policy) {
        return PasswordPolicyDto.builder()
                .id(policy.getId())
                .minUpperCase(policy.getMinUpperCase())
                .minLowerCase(policy.getMinLowerCase())
                .minNumbers(policy.getMinNumbers())
                .minSpecialCharacters(policy.getMinSpecialCharacters())
                .minLength(policy.getMinLength())
                .maxLength(policy.getMaxLength())
                .passwordHistory(policy.getPasswordHistory())
                .attemptExceedCount(policy.getAttemptExceedCount())
                .otpExceedCount(policy.getOtpExceedCount())
                .build();
    }

    private PasswordPolicyPrivilegesDto resolvePrivileges(PasswordPolicyReferenceDataRequest request) {
        String username = request != null ? request.getUsername() : null;
        if (!StringUtils.hasText(username)) {
            username = getAuthenticatedUsername();
        }

        if (!StringUtils.hasText(username)) {
            return PasswordPolicyPrivilegesDto.builder()
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
            return PasswordPolicyPrivilegesDto.builder()
                    .view(false)
                    .update(false)
                    .reset(false)
                    .build();
        }

        var access = authModuleClient.getRolePageTaskAccess(roleCode);
        if (access == null || access.getPages() == null) {
            return PasswordPolicyPrivilegesDto.builder()
                    .view(false)
                    .update(false)
                    .reset(false)
                    .build();
        }

        Map<String, Boolean> taskAccess = new HashMap<>();
        access.getPages().stream()
                .filter(page -> PASSWORD_POLICY_PAGE_CODE.equalsIgnoreCase(page.getPageCode()))
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

        return PasswordPolicyPrivilegesDto.builder()
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
