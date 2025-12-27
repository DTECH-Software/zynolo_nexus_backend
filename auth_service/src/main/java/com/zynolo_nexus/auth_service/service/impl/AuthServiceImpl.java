package com.zynolo_nexus.auth_service.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.zynolo_nexus.auth_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.auth_service.dto.request.ForgotPasswordRequest;
import com.zynolo_nexus.auth_service.dto.request.LoginRequest;
import com.zynolo_nexus.auth_service.dto.request.LogoutRequest;
import com.zynolo_nexus.auth_service.dto.request.ResetPasswordRequest;
import com.zynolo_nexus.auth_service.dto.response.LoginData;
import com.zynolo_nexus.auth_service.dto.response.ModulePermissionDto;
import com.zynolo_nexus.auth_service.dto.response.PagePermissionDto;
import com.zynolo_nexus.auth_service.dto.response.PageTaskPermissionDto;
import com.zynolo_nexus.auth_service.dto.response.ProfileDetails;
import com.zynolo_nexus.auth_service.dto.response.CurrentUserDto;
import com.zynolo_nexus.auth_service.dto.response.ReferenceDataDto;
import com.zynolo_nexus.auth_service.dto.response.SectionPermissionDto;
import com.zynolo_nexus.auth_service.dto.response.TokenDetails;
import com.zynolo_nexus.auth_service.enums.UserStatus;
import com.zynolo_nexus.auth_service.exception.BadRequestException;
import com.zynolo_nexus.auth_service.exception.NotFoundException;
import com.zynolo_nexus.auth_service.exception.UnauthorizedException;
import com.zynolo_nexus.auth_service.mapper.entityToDto.UserEntityToDtoMapper;
import com.zynolo_nexus.auth_service.model.Module;
import com.zynolo_nexus.auth_service.model.PasswordResetToken;
import com.zynolo_nexus.auth_service.model.Page;
import com.zynolo_nexus.auth_service.model.PageTask;
import com.zynolo_nexus.auth_service.model.RefreshToken;
import com.zynolo_nexus.auth_service.model.RoleModuleAccess;
import com.zynolo_nexus.auth_service.model.Section;
import com.zynolo_nexus.auth_service.model.User;
import com.zynolo_nexus.auth_service.repository.ModuleRepository;
import com.zynolo_nexus.auth_service.repository.PageRepository;
import com.zynolo_nexus.auth_service.repository.PageTaskRepository;
import com.zynolo_nexus.auth_service.repository.PasswordResetTokenRepository;
import com.zynolo_nexus.auth_service.repository.RolePageTaskAccessRepository;
import com.zynolo_nexus.auth_service.repository.RoleModuleAccessRepository;
import com.zynolo_nexus.auth_service.repository.RefreshTokenRepository;
import com.zynolo_nexus.auth_service.repository.SectionRepository;
import com.zynolo_nexus.auth_service.repository.UserRepository;
import com.zynolo_nexus.auth_service.service.AuthService;
import com.zynolo_nexus.auth_service.service.EmailService;
import com.zynolo_nexus.auth_service.util.JwtUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@org.springframework.transaction.annotation.Transactional
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);
    private static final int PASSWORD_EXPIRY_DAYS = 30;
    private static final int OTP_EXPIRY_MINUTES = 10;

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final MessageSource messageSource;
    private final JwtUtil jwtUtil;
    private final UserEntityToDtoMapper userMapper;
    private final EmailService emailService;
    private final ModuleRepository moduleRepository;
    private final RoleModuleAccessRepository roleModuleAccessRepository;
    private final SectionRepository sectionRepository;
    private final PageRepository pageRepository;
    private final PageTaskRepository pageTaskRepository;
    private final RolePageTaskAccessRepository rolePageTaskAccessRepository;

    @Override
    public MessageResponseDTO<LoginData> login(LoginRequest request) {

        User user = userRepository
                .findByUsernameAndStatus(request.getUsername(), UserStatus.ACTIVE)
                .orElseThrow(() -> new UnauthorizedException("auth.login.failed"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("auth.login.failed");
        }

        LocalDate today = LocalDate.now();
        if (user.getPasswordExpiredDate() != null && !user.getPasswordExpiredDate().isAfter(today)) {
            throw new UnauthorizedException("auth.password.expired");
        }

        user.setLastLoggedDate(today);
        user.setPasswordExpiredDate(today.plusDays(PASSWORD_EXPIRY_DAYS));
        user.setExpectingFirstTimeLogging(false);
        userRepository.save(user);

        ProfileDetails profileDetails = userMapper.toProfileDetails(user);

        String accessToken = jwtUtil.generateAccessToken(user.getUsername());
        String refreshToken = jwtUtil.generateRefreshToken(user.getUsername());

        refreshTokenRepository.deleteByUsername(user.getUsername());
        RefreshToken refreshTokenEntity = new RefreshToken();
        refreshTokenEntity.setUsername(user.getUsername());
        refreshTokenEntity.setToken(refreshToken);
        refreshTokenEntity.setExpiresAt(
                LocalDateTime.now().plusSeconds(jwtUtil.getRefreshTokenValidityMs() / 1000));
        refreshTokenRepository.save(refreshTokenEntity);

        TokenDetails tokenDetails = TokenDetails.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();

        LoginData loginData = LoginData.builder()
                .profileDetails(profileDetails)
                .tokenDetails(tokenDetails)
                .build();

        String message = messageSource.getMessage(
                "auth.login.success",
                null,
                LocaleContextHolder.getLocale()
        );

        return MessageResponseDTO.<LoginData>builder()
                .success(true)
                .message(message)
                .data(loginData)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    public MessageResponseDTO<String> logout(LogoutRequest request) {
        if (request == null || !StringUtils.hasText(request.getRefreshToken())) {
            throw new BadRequestException("auth.logout.invalid");
        }

        Optional<RefreshToken> tokenOpt = refreshTokenRepository.findByToken(request.getRefreshToken());
        boolean success = false;
        String messageKey = "auth.logout.invalid";
        if (tokenOpt.isPresent()) {
            refreshTokenRepository.delete(tokenOpt.get());
            messageKey = "auth.logout.success";
            success = true;
        }

        return buildMessageResponse(messageKey, success);
    }

    @Override
    public MessageResponseDTO<String> forgotPassword(ForgotPasswordRequest request) {
        if (request == null || !StringUtils.hasText(request.getUsername())
                || !StringUtils.hasText(request.getEmail())) {
            throw new BadRequestException("auth.password.reset.invalid");
        }

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadRequestException("auth.password.reset.user.notfound"));

        if (!request.getEmail().equalsIgnoreCase(user.getEmail())) {
            throw new BadRequestException("auth.password.reset.email.mismatch");
        }

        String otp = generateOtp();
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setUsername(user.getUsername());
        resetToken.setOtp(otp);
        resetToken.setExpiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES));
        passwordResetTokenRepository.save(resetToken);

        log.info("Password reset OTP for {} sent to {}: {}", user.getUsername(), user.getEmail(), otp);
        String displayName = StringUtils.hasText(user.getFirstName()) ? user.getFirstName() : user.getUsername();
        emailService.sendPasswordResetOtp(user.getEmail(), displayName, otp);

        return buildMessageResponse("auth.password.reset.code.sent", true);
    }

    @Override
    public MessageResponseDTO<String> resetPassword(ResetPasswordRequest request) {
        if (request == null || !StringUtils.hasText(request.getUsername())
                || !StringUtils.hasText(request.getOtp())
                || !StringUtils.hasText(request.getNewPassword())) {
            throw new BadRequestException("auth.password.reset.invalid");
        }

        PasswordResetToken resetToken = passwordResetTokenRepository
                .findTopByUsernameAndOtpAndUsedFalseOrderByCreatedAtDesc(
                        request.getUsername(), request.getOtp())
                .orElseThrow(() -> new BadRequestException("auth.password.reset.invalid"));

        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("auth.password.reset.invalid");
        }

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadRequestException("auth.password.reset.user.notfound"));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordExpiredDate(LocalDate.now().plusDays(PASSWORD_EXPIRY_DAYS));
        user.setExpectingFirstTimeLogging(false);
        userRepository.save(user);

        resetToken.setUsed(true);
        resetToken.setUsedAt(LocalDateTime.now());
        passwordResetTokenRepository.save(resetToken);

        refreshTokenRepository.deleteByUsername(user.getUsername());

        return buildMessageResponse("auth.password.reset.success", true);
    }

    @Override
    public MessageResponseDTO<ReferenceDataDto> getReferenceData(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("auth.user.notfound"));

        var accesses = roleModuleAccessRepository.findByRole(user.getRole());
        var viewableIds = accesses.stream()
                .filter(RoleModuleAccess::getCanView)
                .map(a -> a.getModule().getId())
                .collect(java.util.stream.Collectors.toSet());

        var modules = moduleRepository.findAllByActiveTrueOrderBySortOrderAsc();

        var sections = sectionRepository.findAllByActiveTrueOrderBySortOrderAsc();
        var pages = pageRepository.findAllByActiveTrueOrderBySortOrderAsc();
        var tasks = pageTaskRepository.findAllByActiveTrueOrderBySortOrderAsc();

        var sectionsByModule = new java.util.HashMap<Long, java.util.List<Section>>();
        for (Section section : sections) {
            sectionsByModule.computeIfAbsent(section.getModule().getId(), k -> new java.util.ArrayList<>()).add(section);
        }

        var pagesBySection = new java.util.HashMap<Long, java.util.List<Page>>();
        for (Page page : pages) {
            pagesBySection.computeIfAbsent(page.getSection().getId(), k -> new java.util.ArrayList<>()).add(page);
        }

        var tasksByPage = new java.util.HashMap<Long, java.util.List<PageTask>>();
        for (PageTask task : tasks) {
            tasksByPage.computeIfAbsent(task.getPage().getId(), k -> new java.util.ArrayList<>()).add(task);
        }

        var taskAccessMap = rolePageTaskAccessRepository.findByRole(user.getRole()).stream()
                .collect(java.util.stream.Collectors.toMap(
                        a -> a.getPageTask().getId(),
                        a -> Boolean.TRUE.equals(a.getCanAccess()),
                        (left, right) -> left
                ));

        var moduleDtos = modules.stream()
                .map(m -> toPermissionDto(
                        m,
                        viewableIds.contains(m.getId()),
                        sectionsByModule,
                        pagesBySection,
                        tasksByPage,
                        taskAccessMap
                ))
                .toList();

        ReferenceDataDto data = ReferenceDataDto.builder()
                .user(CurrentUserDto.builder()
                        .username(user.getUsername())
                        .displayName(buildDisplayName(user))
                        .email(user.getEmail())
                        .mobile(user.getMobile())
                        .roleCode(user.getRole() != null ? user.getRole().getCode().name() : null)
                        .build())
                .modules(moduleDtos)
                .build();

        String message = messageSource.getMessage(
                "auth.reference.success",
                null,
                "Success",
                LocaleContextHolder.getLocale()
        );

        return MessageResponseDTO.<ReferenceDataDto>builder()
                .success(true)
                .message(message)
                .data(data)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    private ModulePermissionDto toPermissionDto(
            Module module,
            boolean canView,
            java.util.Map<Long, java.util.List<Section>> sectionsByModule,
            java.util.Map<Long, java.util.List<Page>> pagesBySection,
            java.util.Map<Long, java.util.List<PageTask>> tasksByPage,
            java.util.Map<Long, Boolean> taskAccessMap
    ) {
        java.util.List<SectionPermissionDto> sectionDtos = java.util.List.of();
        if (canView) {
            sectionDtos = sectionsByModule.getOrDefault(module.getId(), java.util.List.of()).stream()
                    .map(section -> {
                        var pageDtos = pagesBySection.getOrDefault(section.getId(), java.util.List.of()).stream()
                                .map(page -> {
                                    var taskDtos = tasksByPage.getOrDefault(page.getId(), java.util.List.of()).stream()
                                            .map(task -> PageTaskPermissionDto.builder()
                                                    .code(task.getCode())
                                                    .name(task.getName())
                                                    .canAccess(taskAccessMap.getOrDefault(task.getId(), Boolean.FALSE))
                                                    .build())
                                            .toList();
                                    return PagePermissionDto.builder()
                                            .code(page.getCode())
                                            .name(page.getName())
                                            .tasks(taskDtos)
                                            .build();
                                })
                                .toList();
                        return SectionPermissionDto.builder()
                                .code(section.getCode())
                                .name(section.getName())
                                .pages(pageDtos)
                                .build();
                    })
                    .toList();
        }

        return ModulePermissionDto.builder()
                .code(module.getCode())
                .name(module.getName())
                .canView(canView)
                .sections(sectionDtos)
                .build();
    }

    private String buildDisplayName(User user) {
        if (StringUtils.hasText(user.getFirstName()) || StringUtils.hasText(user.getLastName())) {
            return String.format("%s %s",
                    StringUtils.hasText(user.getFirstName()) ? user.getFirstName() : "",
                    StringUtils.hasText(user.getLastName()) ? user.getLastName() : "").trim();
        }
        return user.getUsername();
    }

    private MessageResponseDTO<String> buildMessageResponse(String key, boolean success) {
        String message = messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
        return MessageResponseDTO.<String>builder()
                .success(success)
                .message(message)
                .errors(null)
                .errorCode(0)
                .data(null)
                .responseTime(LocalDateTime.now())
                .build();
    }

    private String generateOtp() {
        return String.format("%06d", ThreadLocalRandom.current().nextInt(0, 1_000_000));
    }
}
