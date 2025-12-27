package com.zynolo_nexus.setting_service.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.zynolo_nexus.setting_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.setting_service.dto.request.CreateUserRequest;
import com.zynolo_nexus.setting_service.dto.request.UpdateUserRequest;
import com.zynolo_nexus.setting_service.dto.request.validator.CreateUserRequestValidator;
import com.zynolo_nexus.setting_service.dto.request.validator.UpdateUserRequestValidator;
import com.zynolo_nexus.setting_service.dto.response.ProfileDetails;
import com.zynolo_nexus.setting_service.enums.LoginStatus;
import com.zynolo_nexus.setting_service.enums.RoleCode;
import com.zynolo_nexus.setting_service.enums.UserStatus;
import com.zynolo_nexus.setting_service.exception.BadRequestException;
import com.zynolo_nexus.setting_service.exception.NotFoundException;
import com.zynolo_nexus.setting_service.mapper.entityToDto.UserEntityToDtoMapper;
import com.zynolo_nexus.setting_service.model.Role;
import com.zynolo_nexus.setting_service.model.User;
import com.zynolo_nexus.setting_service.repository.RoleRepository;
import com.zynolo_nexus.setting_service.repository.UserRepository;
import com.zynolo_nexus.setting_service.service.UserService;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private static final int PASSWORD_EXPIRY_DAYS = 30;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final MessageSource messageSource;
    private final UserEntityToDtoMapper userMapper;
    private final CreateUserRequestValidator createValidator;
    private final UpdateUserRequestValidator updateValidator;

    public UserServiceImpl(UserRepository userRepository,
                           RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder,
                           MessageSource messageSource,
                           UserEntityToDtoMapper userMapper,
                           CreateUserRequestValidator createValidator,
                           UpdateUserRequestValidator updateValidator) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.messageSource = messageSource;
        this.userMapper = userMapper;
        this.createValidator = createValidator;
        this.updateValidator = updateValidator;
    }

    @Override
    public MessageResponseDTO<ProfileDetails> createUser(CreateUserRequest request) {
        createValidator.validate(request);

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("user.create.username.exists");
        }
        if (StringUtils.hasText(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("user.create.email.exists");
        }

        Role role = resolveRole(request.getRoleCode());

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .mobile(request.getMobile())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .nic(request.getNic())
                .company(request.getCompany())
                .status(UserStatus.ACTIVE)
                .loginStatus(LoginStatus.ACTIVE)
                .passwordExpiredDate(LocalDate.now().plusDays(PASSWORD_EXPIRY_DAYS))
                .expectingFirstTimeLogging(true)
                .reset(false)
                .role(role)
                .build();

        user.setCreatedDate(LocalDateTime.now());
        user.setLastModifiedDate(LocalDateTime.now());

        user = userRepository.save(user);

        return buildProfileResponse(userMapper.toProfileDetails(user), "user.create.success");
    }

    @Override
    @Transactional(readOnly = true)
    public MessageResponseDTO<ProfileDetails> getUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("user.fetch.notfound"));
        return buildProfileResponse(userMapper.toProfileDetails(user), "user.fetch.success");
    }

    @Override
    public MessageResponseDTO<ProfileDetails> updateUser(String username, UpdateUserRequest request) {
        updateValidator.validate(request);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("user.update.notfound"));

        if (StringUtils.hasText(request.getEmail()) && !request.getEmail().equalsIgnoreCase(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new BadRequestException("user.create.email.exists");
            }
            user.setEmail(request.getEmail());
        }

        if (StringUtils.hasText(request.getMobile())) {
            user.setMobile(request.getMobile());
        }
        if (StringUtils.hasText(request.getFirstName())) {
            user.setFirstName(request.getFirstName());
        }
        if (StringUtils.hasText(request.getLastName())) {
            user.setLastName(request.getLastName());
        }
        if (StringUtils.hasText(request.getNic())) {
            user.setNic(request.getNic());
        }
        if (StringUtils.hasText(request.getCompany())) {
            user.setCompany(request.getCompany());
        }

        if (StringUtils.hasText(request.getRoleCode())) {
            user.setRole(resolveRole(request.getRoleCode()));
        }
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }
        if (request.getLoginStatus() != null) {
            user.setLoginStatus(request.getLoginStatus());
        }

        user.setLastModifiedDate(LocalDateTime.now());
        user = userRepository.save(user);

        return buildProfileResponse(userMapper.toProfileDetails(user), "user.update.success");
    }

    @Override
    public MessageResponseDTO<String> deactivateUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("user.update.notfound"));

        user.setStatus(UserStatus.INACTIVE);
        user.setLoginStatus(LoginStatus.DISABLED);
        userRepository.save(user);

        return buildSimpleResponse("user.deactivate.success");
    }

    private Role resolveRole(String roleCodeText) {
        if (!StringUtils.hasText(roleCodeText)) {
            throw new BadRequestException("user.create.role.notfound");
        }
        RoleCode roleCode;
        try {
            roleCode = RoleCode.valueOf(roleCodeText);
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("user.create.role.notfound");
        }
        return roleRepository.findByCode(roleCode)
                .orElseThrow(() -> new BadRequestException("user.create.role.notfound"));
    }

    private MessageResponseDTO<ProfileDetails> buildProfileResponse(ProfileDetails profile, String messageKey) {
        String message = messageSource.getMessage(messageKey, null, LocaleContextHolder.getLocale());
        return MessageResponseDTO.<ProfileDetails>builder()
                .success(true)
                .message(message)
                .data(profile)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    private MessageResponseDTO<String> buildSimpleResponse(String messageKey) {
        String message = messageSource.getMessage(messageKey, null, LocaleContextHolder.getLocale());
        return MessageResponseDTO.<String>builder()
                .success(true)
                .message(message)
                .data(null)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }
}
