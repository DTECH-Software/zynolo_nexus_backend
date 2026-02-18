package com.zynolo_nexus.auth_service.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.zynolo_nexus.auth_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.auth_service.dto.request.ForgotPasswordRequest;
import com.zynolo_nexus.auth_service.dto.request.ChangePasswordRequest;
import com.zynolo_nexus.auth_service.dto.request.LoginRequest;
import com.zynolo_nexus.auth_service.dto.request.LogoutRequest;
import com.zynolo_nexus.auth_service.dto.request.ResetPasswordRequest;
import com.zynolo_nexus.auth_service.dto.request.SwitchCompanyRequest;
import com.zynolo_nexus.auth_service.dto.request.VerifyResetOtpRequest;
import com.zynolo_nexus.auth_service.dto.response.LoginData;
import com.zynolo_nexus.auth_service.dto.response.ModuleDashboardPageDto;
import com.zynolo_nexus.auth_service.dto.response.ModuleDashboardSectionDto;
import com.zynolo_nexus.auth_service.dto.response.ModulePermissionDto;
import com.zynolo_nexus.auth_service.dto.response.ProfileDetails;
import com.zynolo_nexus.auth_service.dto.response.CurrentUserDto;
import com.zynolo_nexus.auth_service.dto.response.ReferenceDataDto;
import com.zynolo_nexus.auth_service.dto.response.ResetTokenResponse;
import com.zynolo_nexus.auth_service.dto.response.TokenDetails;
import com.zynolo_nexus.auth_service.dto.response.CompanySummaryDto;
import com.zynolo_nexus.auth_service.enums.CompanyModuleSubscriptionStatus;
import com.zynolo_nexus.auth_service.enums.ModuleStatus;
import com.zynolo_nexus.auth_service.enums.UserStatus;
import com.zynolo_nexus.auth_service.exception.BadRequestException;
import com.zynolo_nexus.auth_service.exception.NotFoundException;
import com.zynolo_nexus.auth_service.exception.UnauthorizedException;
import com.zynolo_nexus.auth_service.mapper.entityToDto.UserEntityToDtoMapper;
import com.zynolo_nexus.auth_service.model.Module;
import com.zynolo_nexus.auth_service.model.Page;
import com.zynolo_nexus.auth_service.model.PasswordResetToken;
import com.zynolo_nexus.auth_service.model.RefreshToken;
import com.zynolo_nexus.auth_service.model.RoleModuleAccess;
import com.zynolo_nexus.auth_service.model.Section;
import com.zynolo_nexus.auth_service.model.User;
import com.zynolo_nexus.auth_service.model.UserCompany;
import com.zynolo_nexus.auth_service.model.Company;
import com.zynolo_nexus.auth_service.repository.ModuleRepository;
import com.zynolo_nexus.auth_service.repository.PageRepository;
import com.zynolo_nexus.auth_service.repository.PasswordResetTokenRepository;
import com.zynolo_nexus.auth_service.repository.RoleModuleAccessRepository;
import com.zynolo_nexus.auth_service.repository.RefreshTokenRepository;
import com.zynolo_nexus.auth_service.repository.SectionRepository;
import com.zynolo_nexus.auth_service.repository.CompanyModuleSubscriptionRepository;
import com.zynolo_nexus.auth_service.repository.UserRepository;
import com.zynolo_nexus.auth_service.repository.UserCompanyRepository;
import com.zynolo_nexus.auth_service.repository.CompanyRepository;
import com.zynolo_nexus.auth_service.service.AuthService;
import com.zynolo_nexus.auth_service.service.EmailService;
import com.zynolo_nexus.auth_service.service.ReferenceDataCache;
import com.zynolo_nexus.auth_service.util.JwtUtil;
import com.zynolo_nexus.auth_service.context.CompanyContext;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@org.springframework.transaction.annotation.Transactional
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);
    private static final int PASSWORD_EXPIRY_DAYS = 30;
    private static final int OTP_EXPIRY_MINUTES = 1;
    private static final int RESET_TOKEN_EXPIRY_MINUTES = 10;

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final MessageSource messageSource;
    private final JwtUtil jwtUtil;
    private final UserEntityToDtoMapper userMapper;
    private final EmailService emailService;
    private final ModuleRepository moduleRepository;
    private final SectionRepository sectionRepository;
    private final PageRepository pageRepository;
    private final RoleModuleAccessRepository roleModuleAccessRepository;
    private final ReferenceDataCache referenceDataCache;
    private final UserCompanyRepository userCompanyRepository;
    private final CompanyRepository companyRepository;
    private final CompanyModuleSubscriptionRepository companyModuleSubscriptionRepository;

    @Value("${app.default.company-id:1}")
    private Long defaultCompanyId;

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

        var companyInfo = resolveLoginCompany(user);
        Long companyId = companyInfo.defaultCompanyId();
        String accessToken = jwtUtil.generateAccessToken(user.getUsername(), companyId);
        String refreshToken = jwtUtil.generateRefreshToken(user.getUsername(), companyId);

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
                .defaultCompanyId(companyId)
                .companies(companyInfo.companies())
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
        if (request == null || !StringUtils.hasText(request.getUsername())) {
            throw new BadRequestException("auth.password.reset.invalid");
        }

        Optional<User> userOpt = userRepository.findByUsername(request.getUsername());
        if (userOpt.isEmpty()) {
            throw new BadRequestException("auth.password.reset.user.notfound");
        }
        User user = userOpt.get();
        if (!StringUtils.hasText(user.getEmail())) {
            throw new BadRequestException("auth.password.reset.user.notfound");
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
    public MessageResponseDTO<ResetTokenResponse> verifyResetOtp(VerifyResetOtpRequest request) {
        if (request == null || !StringUtils.hasText(request.getUsername())
                || !StringUtils.hasText(request.getOtp())) {
            throw new BadRequestException("auth.password.reset.invalid");
        }

        PasswordResetToken resetToken = passwordResetTokenRepository
                .findTopByUsernameAndOtpAndUsedFalseOrderByCreatedAtDesc(
                        request.getUsername(), request.getOtp())
                .orElseThrow(() -> new BadRequestException("auth.password.reset.invalid"));

        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("auth.password.reset.invalid");
        }

        String resetTokenValue = java.util.UUID.randomUUID().toString();
        resetToken.setResetToken(resetTokenValue);
        resetToken.setOtpVerified(true);
        resetToken.setVerifiedAt(LocalDateTime.now());
        resetToken.setExpiresAt(LocalDateTime.now().plusMinutes(RESET_TOKEN_EXPIRY_MINUTES));
        passwordResetTokenRepository.save(resetToken);

        ResetTokenResponse response = ResetTokenResponse.builder()
                .resetToken(resetTokenValue)
                .build();

        String message = messageSource.getMessage(
                "auth.password.reset.otp.verified",
                null,
                LocaleContextHolder.getLocale()
        );
        return MessageResponseDTO.<ResetTokenResponse>builder()
                .success(true)
                .message(message)
                .data(response)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    public MessageResponseDTO<String> resetPassword(ResetPasswordRequest request) {
        if (request == null || !StringUtils.hasText(request.getUsername())
                || !StringUtils.hasText(request.getResetToken())
                || !StringUtils.hasText(request.getNewPassword())
                || !StringUtils.hasText(request.getConfirmPassword())) {
            throw new BadRequestException("auth.password.reset.invalid");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("auth.password.reset.invalid");
        }

        PasswordResetToken resetToken = passwordResetTokenRepository
                .findTopByUsernameAndResetTokenAndUsedFalseOrderByCreatedAtDesc(
                        request.getUsername(), request.getResetToken())
                .orElseThrow(() -> new BadRequestException("auth.password.reset.invalid"));

        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("auth.password.reset.invalid");
        }

        if (!resetToken.isOtpVerified()) {
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
    public MessageResponseDTO<String> changePassword(String username, ChangePasswordRequest request) {
        if (!StringUtils.hasText(username) || request == null
                || !StringUtils.hasText(request.getCurrentPassword())
                || !StringUtils.hasText(request.getNewPassword())
                || !StringUtils.hasText(request.getConfirmPassword())) {
            throw new BadRequestException("auth.password.change.invalid");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("auth.password.change.invalid");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BadRequestException("auth.password.change.user.notfound"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new UnauthorizedException("auth.password.change.invalid");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordExpiredDate(LocalDate.now().plusDays(PASSWORD_EXPIRY_DAYS));
        user.setExpectingFirstTimeLogging(false);
        userRepository.save(user);

        refreshTokenRepository.deleteByUsername(user.getUsername());

        return buildMessageResponse("auth.password.change.success", true);
    }

    @Override
    public MessageResponseDTO<ReferenceDataDto> getReferenceData(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("auth.user.notfound"));

        Long companyId = resolveCompanyId(null);
        String roleCode = user.getRole() != null ? user.getRole().getCode() : "UNKNOWN";
        String cacheKey = roleCode + ":" + user.getUsername() + ":" + companyId;
        var cached = referenceDataCache.get(cacheKey);
        if (cached.isPresent()) {
            return MessageResponseDTO.<ReferenceDataDto>builder()
                    .success(true)
                    .message(messageSource.getMessage(
                            "auth.reference.success",
                            null,
                            "Success",
                            LocaleContextHolder.getLocale()
                    ))
                    .data(cached.get())
                    .errors(null)
                    .errorCode(0)
                    .responseTime(LocalDateTime.now())
                    .build();
        }

        var accesses = roleModuleAccessRepository.findByRoleAndCompanyId(user.getRole(), companyId);
        var viewableIds = accesses.stream()
                .filter(RoleModuleAccess::getCanView)
                .map(a -> a.getModule().getId())
                .collect(java.util.stream.Collectors.toSet());

        var modules = moduleRepository.findAllActiveOrderBySortOrderAsc(ModuleStatus.ACTIVE);
        SubscriptionScope subscriptionScope = loadSubscriptionScope(companyId);

        var moduleDtos = modules.stream()
                .filter(module -> isModuleSubscribed(subscriptionScope, module.getCode()))
                .map(m -> ModulePermissionDto.builder()
                        .code(m.getCode())
                        .name(m.getName())
                        .canView(viewableIds.contains(m.getId()))
                        .build())
                .toList();

        ReferenceDataDto data = ReferenceDataDto.builder()
                .user(CurrentUserDto.builder()
                        .username(user.getUsername())
                        .displayName(buildDisplayName(user))
                        .email(user.getEmail())
                        .mobile(user.getMobile())
                        .roleCode(user.getRole() != null ? user.getRole().getCode() : null)
                        .build())
                .modules(moduleDtos)
                .build();

        referenceDataCache.put(cacheKey, data);

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

    @Override
    public MessageResponseDTO<Map<String, ModuleDashboardSectionDto>> getModuleDashboard(String username, String moduleCode) {
        if (!StringUtils.hasText(username) || !StringUtils.hasText(moduleCode)) {
            throw new BadRequestException("auth.dashboard.invalid");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("auth.user.notfound"));

        Module module = moduleRepository.findByCode(moduleCode)
                .orElseThrow(() -> new NotFoundException("module.notfound"));

        if (module.getStatus() != ModuleStatus.ACTIVE) {
            throw new NotFoundException("module.notfound");
        }

        Long companyId = resolveCompanyId(null);
        SubscriptionScope subscriptionScope = loadSubscriptionScope(companyId);
        boolean canView = roleModuleAccessRepository.findByRoleAndCompanyId(user.getRole(), companyId).stream()
                .anyMatch(access -> access.getModule().getId().equals(module.getId())
                        && Boolean.TRUE.equals(access.getCanView()));
        if (!isModuleSubscribed(subscriptionScope, module.getCode())) {
            canView = false;
        }

        Map<String, ModuleDashboardSectionDto> data = Map.of();
        if (canView) {
            List<Section> sections = sectionRepository.findAllByModuleAndActiveTrueOrderBySortOrderAsc(module);
            Map<String, ModuleDashboardSectionDto> sectionMap = new LinkedHashMap<>();
            for (Section section : sections) {
                List<Page> pages = pageRepository.findAllBySectionOrderBySortOrderAsc(section);
                List<ModuleDashboardPageDto> pageDtos = pages.stream()
                        .map(page -> ModuleDashboardPageDto.builder()
                                .code(page.getCode())
                                .url(page.getUrl())
                                .description(StringUtils.hasText(page.getDescription()) ? page.getDescription() : page.getName())
                                .status(Boolean.TRUE.equals(page.getActive()) ? "ACTIVE" : "DEACTIVE")
                                .build())
                        .toList();

                ModuleDashboardSectionDto sectionDto = ModuleDashboardSectionDto.builder()
                        .code(section.getCode())
                        .description(StringUtils.hasText(section.getDescription()) ? section.getDescription() : section.getName())
                        .pages(pageDtos)
                        .build();

                sectionMap.put(section.getCode(), sectionDto);
            }

            data = sectionMap;
        }

        String message = messageSource.getMessage(
                "auth.module.dashboard.success",
                null,
                "Success",
                LocaleContextHolder.getLocale()
        );

        return MessageResponseDTO.<Map<String, ModuleDashboardSectionDto>>builder()
                .success(true)
                .message(message)
                .data(data)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    public MessageResponseDTO<LoginData> switchCompany(String username, SwitchCompanyRequest request) {
        if (!StringUtils.hasText(username) || request == null
                || (request.getCompanyId() == null && !StringUtils.hasText(request.getCompanyCode()))) {
            throw new BadRequestException("auth.company.switch.invalid");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("auth.user.notfound"));

        Company company = resolveSwitchCompany(request);
        UserCompany mapping = userCompanyRepository.findByUserAndCompany_Id(user, company.getId())
                .orElseThrow(() -> new UnauthorizedException("auth.company.switch.invalid"));

        if (mapping.getStatus() != null
                && mapping.getStatus() != com.zynolo_nexus.auth_service.enums.UserCompanyStatus.ACTIVE) {
            throw new UnauthorizedException("auth.company.switch.invalid");
        }

        Long companyId = mapping.getCompany() != null ? mapping.getCompany().getId() : company.getId();
        String accessToken = jwtUtil.generateAccessToken(user.getUsername(), companyId);
        String refreshToken = jwtUtil.generateRefreshToken(user.getUsername(), companyId);

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

        ProfileDetails profileDetails = userMapper.toProfileDetails(user);
        var companyInfo = resolveLoginCompany(user);
        List<CompanySummaryDto> companies = companyInfo.companies().stream()
                .map(item -> CompanySummaryDto.builder()
                        .id(item.getId())
                        .code(item.getCode())
                        .description(item.getDescription())
                        .isDefault(item.getId() != null && item.getId().equals(companyId))
                        .build())
                .toList();

        LoginData loginData = LoginData.builder()
                .profileDetails(profileDetails)
                .tokenDetails(tokenDetails)
                .defaultCompanyId(companyId)
                .companies(companies)
                .build();

        return MessageResponseDTO.<LoginData>builder()
                .success(true)
                .message(messageSource.getMessage(
                        "auth.company.switch.success",
                        null,
                        "Success",
                        LocaleContextHolder.getLocale()
                ))
                .data(loginData)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    private Company resolveSwitchCompany(SwitchCompanyRequest request) {
        if (request == null) {
            throw new BadRequestException("auth.company.switch.invalid");
        }
        if (request.getCompanyId() != null) {
            return companyRepository.findById(request.getCompanyId())
                    .orElseThrow(() -> new BadRequestException("auth.company.switch.invalid"));
        }
        if (StringUtils.hasText(request.getCompanyCode())) {
            return companyRepository.findByCode(request.getCompanyCode().trim())
                    .orElseThrow(() -> new BadRequestException("auth.company.switch.invalid"));
        }
        throw new BadRequestException("auth.company.switch.invalid");
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

    private LoginCompanyInfo resolveLoginCompany(User user) {
        if (user == null) {
            return new LoginCompanyInfo(defaultCompanyId, List.of());
        }
        List<UserCompany> mappings = userCompanyRepository.findByUser(user);
        if (mappings == null || mappings.isEmpty()) {
            LoginCompanyInfo fallback = resolveCompanyFromUser(user);
            return fallback != null ? fallback : new LoginCompanyInfo(defaultCompanyId, List.of());
        }

        mappings = mappings.stream()
                .filter(m -> m.getStatus() == null
                        || m.getStatus() == com.zynolo_nexus.auth_service.enums.UserCompanyStatus.ACTIVE)
                .toList();
        if (mappings.isEmpty()) {
            return new LoginCompanyInfo(defaultCompanyId, List.of());
        }

        UserCompany defaultMapping = mappings.stream()
                .filter(m -> Boolean.TRUE.equals(m.getIsDefault()))
                .findFirst()
                .orElse(mappings.get(0));

        Long defaultId = defaultMapping.getCompany() != null
                ? defaultMapping.getCompany().getId()
                : defaultCompanyId;

        List<CompanySummaryDto> companies = mappings.stream()
                .filter(m -> m.getCompany() != null)
                .map(m -> CompanySummaryDto.builder()
                        .id(m.getCompany().getId())
                        .code(m.getCompany().getCode())
                        .description(m.getCompany().getDescription())
                        .isDefault(Boolean.TRUE.equals(m.getIsDefault()))
                        .build())
                .toList();

        return new LoginCompanyInfo(defaultId != null ? defaultId : defaultCompanyId, companies);
    }

    private LoginCompanyInfo resolveCompanyFromUser(User user) {
        if (user == null || !StringUtils.hasText(user.getCompany())) {
            return null;
        }
        Company company = companyRepository.findByCode(user.getCompany()).orElse(null);
        if (company == null) {
            return null;
        }
        CompanySummaryDto summary = CompanySummaryDto.builder()
                .id(company.getId())
                .code(company.getCode())
                .description(company.getDescription())
                .isDefault(true)
                .build();
        return new LoginCompanyInfo(company.getId(), List.of(summary));
    }

    private Long resolveCompanyId(Long fallback) {
        Long companyId = CompanyContext.getCompanyId();
        if (companyId != null) {
            return companyId;
        }
        if (fallback != null) {
            return fallback;
        }
        return defaultCompanyId;
    }

    private SubscriptionScope loadSubscriptionScope(Long companyId) {
        if (companyId == null) {
            return new SubscriptionScope(false, Set.of());
        }
        List<com.zynolo_nexus.auth_service.model.CompanyModuleSubscription> subscriptions =
                companyModuleSubscriptionRepository.findByCompany_Id(companyId);
        if (subscriptions == null || subscriptions.isEmpty()) {
            return new SubscriptionScope(false, Set.of());
        }
        Set<String> activeModuleCodes = subscriptions.stream()
                .filter(subscription -> subscription.getStatus() == CompanyModuleSubscriptionStatus.ACTIVE)
                .map(com.zynolo_nexus.auth_service.model.CompanyModuleSubscription::getModuleCode)
                .filter(StringUtils::hasText)
                .map(code -> code.trim().toLowerCase())
                .collect(java.util.stream.Collectors.toSet());
        return new SubscriptionScope(true, activeModuleCodes);
    }

    private boolean isModuleSubscribed(SubscriptionScope subscriptionScope, String moduleCode) {
        if (subscriptionScope == null || !subscriptionScope.enforced()) {
            return true;
        }
        if (!StringUtils.hasText(moduleCode)) {
            return false;
        }
        return subscriptionScope.activeModuleCodes().contains(moduleCode.trim().toLowerCase());
    }

    private record LoginCompanyInfo(Long defaultCompanyId, List<CompanySummaryDto> companies) {
    }

    private record SubscriptionScope(boolean enforced, Set<String> activeModuleCodes) {
    }

}
