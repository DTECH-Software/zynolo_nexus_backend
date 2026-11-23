package com.zynolo_nexus.auth_service.service.impl;

import com.zynolo_nexus.auth_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.auth_service.dto.response.LoginData;
import com.zynolo_nexus.auth_service.dto.response.ProfileDetails;
import com.zynolo_nexus.auth_service.dto.response.TokenDetails;
import com.zynolo_nexus.auth_service.dto.response.CompanySummaryDto;
import com.zynolo_nexus.auth_service.enums.LoginStatus;
import com.zynolo_nexus.auth_service.enums.UserStatus;
import com.zynolo_nexus.auth_service.exception.BadRequestException;
import com.zynolo_nexus.auth_service.exception.ConflictException;
import com.zynolo_nexus.auth_service.exception.NotFoundException;
import com.zynolo_nexus.auth_service.exception.UnauthorizedException;
import com.zynolo_nexus.auth_service.mapper.entityToDto.UserEntityToDtoMapper;
import com.zynolo_nexus.auth_service.model.RefreshToken;
import com.zynolo_nexus.auth_service.model.Role;
import com.zynolo_nexus.auth_service.model.User;
import com.zynolo_nexus.auth_service.model.UserIdentity;
import com.zynolo_nexus.auth_service.model.UserCompany;
import com.zynolo_nexus.auth_service.repository.RefreshTokenRepository;
import com.zynolo_nexus.auth_service.repository.RoleRepository;
import com.zynolo_nexus.auth_service.repository.UserIdentityRepository;
import com.zynolo_nexus.auth_service.repository.UserRepository;
import com.zynolo_nexus.auth_service.repository.UserCompanyRepository;
import com.zynolo_nexus.auth_service.service.GoogleSsoService;
import com.zynolo_nexus.auth_service.util.JwtUtil;
import com.zynolo_nexus.auth_service.context.CompanyContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class GoogleSsoServiceImpl implements GoogleSsoService {

    private static final int PASSWORD_EXPIRY_DAYS = 30;
    private static final String PROVIDER_GOOGLE = "google";

    private final JwtDecoder googleJwtDecoder;
    private final UserRepository userRepository;
    private final UserIdentityRepository userIdentityRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserCompanyRepository userCompanyRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final UserEntityToDtoMapper userMapper;
    private final MessageSource messageSource;

    @Value("${oauth.google.client-id}")
    private String googleClientId;

    @Value("${app.default.company-id:1}")
    private Long defaultCompanyId;

    @Override
    public MessageResponseDTO<LoginData> loginWithGoogle(String idToken) {
        if (!StringUtils.hasText(idToken)) {
            throw new BadRequestException("auth.sso.google.invalid");
        }

        Jwt jwt = decodeToken(idToken);
        validateAudience(jwt);
        String email = extractVerifiedEmail(jwt);
        String subject = jwt.getSubject();
        if (!StringUtils.hasText(subject)) {
            throw new BadRequestException("auth.sso.google.invalid");
        }

        User user = resolveUser(subject, email, jwt);
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new UnauthorizedException("auth.login.failed");
        }

        LocalDate today = LocalDate.now();
        user.setLastLoggedDate(today);
        user.setPasswordExpiredDate(today.plusDays(PASSWORD_EXPIRY_DAYS));
        user.setExpectingFirstTimeLogging(false);
        userRepository.save(user);

        ProfileDetails profileDetails = userMapper.toProfileDetails(user);

        Long companyId = resolveCompanyId();
        String sessionId = UUID.randomUUID().toString();
        String accessToken = jwtUtil.generateAccessToken(user.getUsername(), companyId, sessionId);
        String refreshToken = jwtUtil.generateRefreshToken(user.getUsername(), companyId, sessionId);

        refreshTokenRepository.deleteByUsername(user.getUsername());
        RefreshToken refreshTokenEntity = new RefreshToken();
        refreshTokenEntity.setUsername(user.getUsername());
        refreshTokenEntity.setSessionId(sessionId);
        refreshTokenEntity.setToken(refreshToken);
        refreshTokenEntity.setExpiresAt(
                LocalDateTime.now().plusSeconds(jwtUtil.getRefreshTokenValidityMs() / 1000));
        refreshTokenRepository.save(refreshTokenEntity);

        TokenDetails tokenDetails = TokenDetails.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();

        var companyInfo = resolveLoginCompany(user);
        LoginData loginData = LoginData.builder()
                .profileDetails(profileDetails)
                .tokenDetails(tokenDetails)
                .defaultCompanyId(companyInfo.defaultCompanyId())
                .companies(companyInfo.companies())
                .build();

        String message = messageSource.getMessage(
                "auth.sso.google.success",
                null,
                "Success",
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

    private Jwt decodeToken(String idToken) {
        try {
            return googleJwtDecoder.decode(idToken);
        } catch (JwtException ex) {
            throw new UnauthorizedException("auth.sso.google.invalid");
        }
    }

    private void validateAudience(Jwt jwt) {
        if (!StringUtils.hasText(googleClientId)) {
            throw new IllegalStateException("Google client id is not configured");
        }
        Object aud = jwt.getClaims().get("aud");
        boolean match = false;
        if (aud instanceof String audString) {
            match = googleClientId.equals(audString);
        } else if (aud instanceof Collection<?> audList) {
            match = audList.stream().anyMatch(item -> googleClientId.equals(String.valueOf(item)));
        }
        if (!match) {
            throw new UnauthorizedException("auth.sso.google.invalid");
        }
    }

    private String extractVerifiedEmail(Jwt jwt) {
        Object verifiedClaim = jwt.getClaims().get("email_verified");
        boolean verified = Boolean.TRUE.equals(verifiedClaim)
                || "true".equalsIgnoreCase(String.valueOf(verifiedClaim));
        if (!verified) {
            throw new BadRequestException("auth.sso.email.not.verified");
        }

        String email = jwt.getClaimAsString("email");
        if (!StringUtils.hasText(email)) {
            throw new BadRequestException("auth.sso.email.missing");
        }
        return email;
    }

    private User resolveUser(String subject, String email, Jwt jwt) {
        return userIdentityRepository.findByProviderAndProviderSubject(PROVIDER_GOOGLE, subject)
                .map(UserIdentity::getUser)
                .orElseGet(() -> createNewUser(subject, email, jwt));
    }

    private User createNewUser(String subject, String email, Jwt jwt) {
        userRepository.findByEmailIgnoreCase(email)
                .ifPresent(existing -> {
                    throw new ConflictException("auth.sso.link.required");
                });

        Role role = roleRepository.findByCodeIgnoreCase("USER")
                .orElseThrow(() -> new NotFoundException("user.create.role.notfound"));

        String username = generateUsername(email);
        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .email(email)
                .firstName(resolveFirstName(jwt))
                .lastName(resolveLastName(jwt))
                .status(UserStatus.ACTIVE)
                .loginStatus(LoginStatus.ACTIVE)
                .passwordExpiredDate(LocalDate.now().plusDays(PASSWORD_EXPIRY_DAYS))
                .expectingFirstTimeLogging(false)
                .reset(false)
                .role(role)
                .build();

        User savedUser = userRepository.save(user);
        UserIdentity identity = UserIdentity.builder()
                .provider(PROVIDER_GOOGLE)
                .providerSubject(subject)
                .user(savedUser)
                .build();
        userIdentityRepository.save(identity);
        return savedUser;
    }

    private String generateUsername(String email) {
        String localPart = email.split("@", 2)[0];
        String base = localPart.replaceAll("[^a-zA-Z0-9._-]", "");
        if (!StringUtils.hasText(base)) {
            base = "user";
        }
        String candidate = base.toLowerCase(Locale.ROOT);
        if (!userRepository.existsByUsername(candidate)) {
            return candidate;
        }
        int suffix = 1;
        while (userRepository.existsByUsername(candidate + suffix)) {
            suffix++;
        }
        return candidate + suffix;
    }

    private String resolveFirstName(Jwt jwt) {
        String given = jwt.getClaimAsString("given_name");
        if (StringUtils.hasText(given)) {
            return given;
        }
        String name = jwt.getClaimAsString("name");
        if (StringUtils.hasText(name)) {
            String[] parts = name.trim().split("\\s+", 2);
            return parts[0];
        }
        return null;
    }

    private String resolveLastName(Jwt jwt) {
        String family = jwt.getClaimAsString("family_name");
        if (StringUtils.hasText(family)) {
            return family;
        }
        String name = jwt.getClaimAsString("name");
        if (StringUtils.hasText(name)) {
            String[] parts = name.trim().split("\\s+", 2);
            return parts.length > 1 ? parts[1] : null;
        }
        return null;
    }

    private Long resolveCompanyId() {
        Long companyId = CompanyContext.getCompanyId();
        return companyId != null ? companyId : defaultCompanyId;
    }

    private LoginCompanyInfo resolveLoginCompany(User user) {
        if (user == null) {
            return new LoginCompanyInfo(defaultCompanyId, List.of());
        }
        List<UserCompany> mappings = userCompanyRepository.findByUser(user);
        if (mappings == null || mappings.isEmpty()) {
            return new LoginCompanyInfo(defaultCompanyId, List.of());
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

    private record LoginCompanyInfo(Long defaultCompanyId, List<CompanySummaryDto> companies) {
    }
}
