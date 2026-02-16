package com.zynolo_nexus.setting_service.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.zynolo_nexus.setting_service.client.AuthModuleClient;
import com.zynolo_nexus.setting_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.setting_service.dto.request.CreateUserRequest;
import com.zynolo_nexus.setting_service.dto.request.UpdateUserRequest;
import com.zynolo_nexus.setting_service.dto.request.ProfileImageUpdateRequest;
import com.zynolo_nexus.setting_service.dto.request.UserFilterRequest;
import com.zynolo_nexus.setting_service.dto.request.UserFilterSearch;
import com.zynolo_nexus.setting_service.dto.request.UserReferenceDataRequest;
import com.zynolo_nexus.setting_service.dto.request.UserStatusUpdateRequest;
import com.zynolo_nexus.setting_service.dto.request.UserUpdateByIdRequest;
import com.zynolo_nexus.setting_service.dto.request.validator.CreateUserRequestValidator;
import com.zynolo_nexus.setting_service.dto.request.validator.UpdateUserRequestValidator;
import com.zynolo_nexus.setting_service.dto.response.ProfileDetails;
import com.zynolo_nexus.setting_service.dto.response.ReferenceCompanyDto;
import com.zynolo_nexus.setting_service.dto.response.ReferenceRoleDto;
import com.zynolo_nexus.setting_service.dto.response.ReferenceStatusDto;
import com.zynolo_nexus.setting_service.dto.response.UserFilterResultDto;
import com.zynolo_nexus.setting_service.dto.response.UserListItemDto;
import com.zynolo_nexus.setting_service.dto.response.UserPrivilegesDto;
import com.zynolo_nexus.setting_service.dto.response.UserReferenceDataDto;
import com.zynolo_nexus.setting_service.enums.CompanyStatus;
import com.zynolo_nexus.setting_service.enums.LoginStatus;
import com.zynolo_nexus.setting_service.enums.RoleStatus;
import com.zynolo_nexus.setting_service.enums.UserCompanyStatus;
import com.zynolo_nexus.setting_service.enums.UserStatus;
import com.zynolo_nexus.setting_service.exception.BadRequestException;
import com.zynolo_nexus.setting_service.exception.NotFoundException;
import com.zynolo_nexus.setting_service.mapper.entityToDto.UserEntityToDtoMapper;
import com.zynolo_nexus.setting_service.model.Company;
import com.zynolo_nexus.setting_service.model.Role;
import com.zynolo_nexus.setting_service.model.User;
import com.zynolo_nexus.setting_service.model.UserCompany;
import com.zynolo_nexus.setting_service.model.PasswordPolicy;
import com.zynolo_nexus.setting_service.model.UsernamePolicy;
import com.zynolo_nexus.setting_service.repository.CompanyRepository;
import com.zynolo_nexus.setting_service.repository.PasswordPolicyRepository;
import com.zynolo_nexus.setting_service.repository.RoleRepository;
import com.zynolo_nexus.setting_service.repository.UserCompanyRepository;
import com.zynolo_nexus.setting_service.repository.UserRepository;
import com.zynolo_nexus.setting_service.repository.UsernamePolicyRepository;
import com.zynolo_nexus.setting_service.service.UserService;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private static final int PASSWORD_EXPIRY_DAYS = 30;
    private static final String USER_MANAGEMENT_CODE = "USER";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CompanyRepository companyRepository;
    private final UserCompanyRepository userCompanyRepository;
    private final PasswordPolicyRepository passwordPolicyRepository;
    private final UsernamePolicyRepository usernamePolicyRepository;
    private final AuthModuleClient authModuleClient;
    private final PasswordEncoder passwordEncoder;
    private final MessageSource messageSource;
    private final UserEntityToDtoMapper userMapper;
    private final CreateUserRequestValidator createValidator;
    private final UpdateUserRequestValidator updateValidator;

    @Value("${password.policy.default.minUpperCase:1}")
    private int defaultPasswordMinUpperCase;
    @Value("${password.policy.default.minLowerCase:1}")
    private int defaultPasswordMinLowerCase;
    @Value("${password.policy.default.minNumbers:1}")
    private int defaultPasswordMinNumbers;
    @Value("${password.policy.default.minSpecialCharacters:1}")
    private int defaultPasswordMinSpecialCharacters;
    @Value("${password.policy.default.minLength:3}")
    private int defaultPasswordMinLength;
    @Value("${password.policy.default.maxLength:10}")
    private int defaultPasswordMaxLength;

    @Value("${username.policy.default.minUpperCase:1}")
    private int defaultUsernameMinUpperCase;
    @Value("${username.policy.default.minLowerCase:4}")
    private int defaultUsernameMinLowerCase;
    @Value("${username.policy.default.minNumbers:1}")
    private int defaultUsernameMinNumbers;
    @Value("${username.policy.default.minSpecialCharacters:1}")
    private int defaultUsernameMinSpecialCharacters;
    @Value("${username.policy.default.minLength:1}")
    private int defaultUsernameMinLength;
    @Value("${username.policy.default.maxLength:15}")
    private int defaultUsernameMaxLength;

    public UserServiceImpl(UserRepository userRepository,
                           RoleRepository roleRepository,
                           CompanyRepository companyRepository,
                           UserCompanyRepository userCompanyRepository,
                           PasswordPolicyRepository passwordPolicyRepository,
                           UsernamePolicyRepository usernamePolicyRepository,
                           AuthModuleClient authModuleClient,
                           PasswordEncoder passwordEncoder,
                           MessageSource messageSource,
                           UserEntityToDtoMapper userMapper,
                           CreateUserRequestValidator createValidator,
                           UpdateUserRequestValidator updateValidator) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.companyRepository = companyRepository;
        this.userCompanyRepository = userCompanyRepository;
        this.passwordPolicyRepository = passwordPolicyRepository;
        this.usernamePolicyRepository = usernamePolicyRepository;
        this.authModuleClient = authModuleClient;
        this.passwordEncoder = passwordEncoder;
        this.messageSource = messageSource;
        this.userMapper = userMapper;
        this.createValidator = createValidator;
        this.updateValidator = updateValidator;
    }

    @Override
    public MessageResponseDTO<ProfileDetails> createUser(CreateUserRequest request) {
        createValidator.validate(request);
        String targetUsername = request.getTargetUsername();
        validateUsernameAgainstPolicy(targetUsername);
        validatePasswordAgainstPolicy(request.getPassword());

        if (userRepository.existsByUsername(targetUsername)) {
            throw new BadRequestException("user.create.username.exists");
        }
        if (StringUtils.hasText(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("user.create.email.exists");
        }

        Role role = resolveRole(request.getRoleCode());
        Company company = resolveCompany(request.getCompany());

        User user = User.builder()
                .username(targetUsername)
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

        if (company != null) {
            UserCompany mapping = UserCompany.builder()
                    .user(user)
                    .company(company)
                    .role(role)
                    .status(UserCompanyStatus.ACTIVE)
                    .isDefault(true)
                    .build();
            userCompanyRepository.save(mapping);
        }

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

        if (StringUtils.hasText(request.getCompany()) || StringUtils.hasText(request.getRoleCode())) {
            Company company = resolveCompany(request.getCompany());
            UserCompany mapping = userCompanyRepository.findFirstByUserAndIsDefaultTrue(user).orElse(null);
            if (mapping == null && company != null) {
                mapping = UserCompany.builder()
                        .user(user)
                        .company(company)
                        .role(user.getRole())
                        .status(UserCompanyStatus.ACTIVE)
                        .isDefault(true)
                        .build();
            }
            if (mapping != null) {
                if (company != null) {
                    mapping.setCompany(company);
                }
                if (user.getRole() != null) {
                    mapping.setRole(user.getRole());
                }
                mapping.setStatus(UserCompanyStatus.ACTIVE);
                userCompanyRepository.save(mapping);
            }
        }

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

    @Override
    @Transactional(readOnly = true)
    public MessageResponseDTO<UserReferenceDataDto> getReferenceData() {
        return getReferenceData(null);
    }

    @Override
    @Transactional(readOnly = true)
    public MessageResponseDTO<UserReferenceDataDto> getReferenceData(UserReferenceDataRequest request) {
        var companies = companyRepository.findAllByStatusOrderByCodeAsc(CompanyStatus.ACTIVE).stream()
                .map(c -> ReferenceCompanyDto.builder()
                        .code(c.getCode())
                        .description(c.getDescription())
                        .build())
                .toList();

        var roles = roleRepository.findAll().stream()
                .filter(role -> role.getStatus() == null || role.getStatus() == RoleStatus.ACTIVE)
                .map(r -> ReferenceRoleDto.builder()
                        .code(r.getCode())
                        .description(r.getDescription())
                        .build())
                .toList();

        UserReferenceDataDto data = UserReferenceDataDto.builder()
                .companies(companies)
                .roles(roles)
                .userStatus(List.of(
                        ReferenceStatusDto.builder().code("ACTIVE").description("Active").build(),
                        ReferenceStatusDto.builder().code("INACTIVE").description("Inactive").build(),
                        ReferenceStatusDto.builder().code("LOCKED").description("Locked").build()
                ))
                .loginStatus(List.of(
                        ReferenceStatusDto.builder().code("ACTIVE").description("Active").build(),
                        ReferenceStatusDto.builder().code("DISABLED").description("Disabled").build(),
                        ReferenceStatusDto.builder().code("PASSWORD_EXPIRED").description("Password Expired").build()
                ))
                .privileges(resolvePrivileges(request))
                .build();

        String message = messageSource.getMessage("user.reference.success", null, LocaleContextHolder.getLocale());
        return MessageResponseDTO.<UserReferenceDataDto>builder()
                .success(true)
                .message(message)
                .data(data)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public MessageResponseDTO<UserFilterResultDto> filterList(UserFilterRequest request) {
        List<User> users = userRepository.findAll();

        UserFilterSearch search = request != null ? request.getSearch() : null;
        String username = search != null ? normalize(search.getUsername()) : null;
        String role = search != null ? normalize(search.getRole()) : null;
        String nic = search != null ? normalize(search.getNic()) : null;
        String email = search != null ? normalize(search.getEmail()) : null;
        String mobile = search != null ? normalize(search.getMobile()) : null;
        String firstName = search != null ? normalize(search.getFirstName()) : null;
        String lastName = search != null ? normalize(search.getLastName()) : null;
        String status = search != null ? normalize(search.getStatus()) : null;
        String loginStatus = search != null ? normalize(search.getLoginStatus()) : null;

        List<UserListItemDto> filtered = users.stream()
                .filter(user -> matches(username, user.getUsername()))
                .filter(user -> matches(role, user.getRole() != null ? user.getRole().getCode() : null))
                .filter(user -> matches(nic, user.getNic()))
                .filter(user -> matches(email, user.getEmail()))
                .filter(user -> matches(mobile, user.getMobile()))
                .filter(user -> matches(firstName, user.getFirstName()))
                .filter(user -> matches(lastName, user.getLastName()))
                .filter(user -> matchesStatus(status, user.getStatus()))
                .filter(user -> matchesLoginStatus(loginStatus, user.getLoginStatus()))
                .map(this::toListItem)
                .toList();

        java.util.Comparator<UserListItemDto> comparator = resolveComparator(
                request != null ? request.getSortColumn() : null,
                request != null ? request.getSortDirection() : null
        );

        List<UserListItemDto> sorted = filtered.stream().sorted(comparator).toList();

        int size = request != null && request.getSize() != null && request.getSize() > 0 ? request.getSize() : 10;
        int page = request != null && request.getPage() != null && request.getPage() >= 0 ? request.getPage() : 0;
        int totalElements = sorted.size();
        int totalPages = size == 0 ? 1 : (int) Math.ceil((double) totalElements / size);
        int fromIndex = Math.min(page * size, totalElements);
        int toIndex = Math.min(fromIndex + size, totalElements);
        List<UserListItemDto> pageItems = sorted.subList(fromIndex, toIndex);

        UserFilterResultDto result = UserFilterResultDto.builder()
                .content(pageItems)
                .totalRecords(totalElements)
                .totalPages(totalPages)
                .page(page)
                .size(size)
                .build();

        return MessageResponseDTO.<UserFilterResultDto>builder()
                .success(true)
                .message("Users filtered successfully")
                .data(result)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public MessageResponseDTO<ProfileDetails> getUser(Long id) {
        if (id == null) {
            return MessageResponseDTO.<ProfileDetails>builder()
                    .success(false)
                    .message("Invalid view request")
                    .data(null)
                    .errors(null)
                    .errorCode(400)
                    .responseTime(LocalDateTime.now())
                    .build();
        }
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("user.fetch.notfound"));
        return buildProfileResponse(userMapper.toProfileDetails(user), "user.fetch.success");
    }

    @Override
    public MessageResponseDTO<ProfileDetails> updateUser(UserUpdateByIdRequest request) {
        if (request == null || request.getId() == null) {
            return MessageResponseDTO.<ProfileDetails>builder()
                    .success(false)
                    .message("Invalid update request")
                    .data(null)
                    .errors(null)
                    .errorCode(400)
                    .responseTime(LocalDateTime.now())
                    .build();
        }

        User user = userRepository.findById(request.getId())
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

        if (StringUtils.hasText(request.getCompany()) || StringUtils.hasText(request.getRoleCode())) {
            Company company = resolveCompany(request.getCompany());
            UserCompany mapping = userCompanyRepository.findFirstByUserAndIsDefaultTrue(user).orElse(null);
            if (mapping == null && company != null) {
                mapping = UserCompany.builder()
                        .user(user)
                        .company(company)
                        .role(user.getRole())
                        .status(UserCompanyStatus.ACTIVE)
                        .isDefault(true)
                        .build();
            }
            if (mapping != null) {
                if (company != null) {
                    mapping.setCompany(company);
                }
                if (user.getRole() != null) {
                    mapping.setRole(user.getRole());
                }
                mapping.setStatus(UserCompanyStatus.ACTIVE);
                userCompanyRepository.save(mapping);
            }
        }

        return buildProfileResponse(userMapper.toProfileDetails(user), "user.update.success");
    }

    @Override
    public MessageResponseDTO<ProfileDetails> updateStatus(UserStatusUpdateRequest request) {
        if (request == null || request.getId() == null) {
            return MessageResponseDTO.<ProfileDetails>builder()
                    .success(false)
                    .message("Invalid status request")
                    .data(null)
                    .errors(null)
                    .errorCode(400)
                    .responseTime(LocalDateTime.now())
                    .build();
        }

        User user = userRepository.findById(request.getId())
                .orElseThrow(() -> new NotFoundException("user.update.notfound"));

        UserStatus status = request.getStatus() != null ? request.getStatus() : user.getStatus();
        user.setStatus(status != null ? status : UserStatus.ACTIVE);
        if (status == UserStatus.INACTIVE) {
            user.setLoginStatus(LoginStatus.DISABLED);
        } else if (status == UserStatus.ACTIVE && user.getLoginStatus() == LoginStatus.DISABLED) {
            user.setLoginStatus(LoginStatus.ACTIVE);
        }
        userRepository.save(user);

        return buildProfileResponse(userMapper.toProfileDetails(user), "user.update.success");
    }

    @Override
    public MessageResponseDTO<ProfileDetails> updateProfileImage(ProfileImageUpdateRequest request) {
        if (request == null
                || !StringUtils.hasText(request.getUsername())
                || !StringUtils.hasText(request.getFile())
                || !StringUtils.hasText(request.getFileName())
                || !StringUtils.hasText(request.getFileType())) {
            throw new BadRequestException("user.profile.image.invalid");
        }

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new NotFoundException("user.fetch.notfound"));

        user.setProfileImgDoc(request.getFile());
        user.setProfileImgFileName(request.getFileName());
        user.setProfileImgFileType(request.getFileType());
        user.setProfileImgType(request.getType());
        user.setLastModifiedDate(LocalDateTime.now());
        user = userRepository.save(user);

        return buildProfileResponse(userMapper.toProfileDetails(user), "user.profile.image.update.success");
    }

    private Role resolveRole(String roleCodeText) {
        if (!StringUtils.hasText(roleCodeText)) {
            throw new BadRequestException("user.create.role.notfound");
        }
        Role role = roleRepository.findByCodeIgnoreCase(roleCodeText)
                .orElseThrow(() -> new BadRequestException("user.create.role.notfound"));
        if (role.getStatus() != null && role.getStatus() != RoleStatus.ACTIVE) {
            throw new BadRequestException("user.create.role.notfound");
        }
        return role;
    }

    private void validateUsernameAgainstPolicy(String username) {
        if (!StringUtils.hasText(username)) {
            return;
        }

        UsernamePolicy policy = usernamePolicyRepository.findTopByOrderByIdAsc().orElse(null);
        int minUpperCase = valueOrDefault(policy != null ? policy.getMinUpperCase() : null, defaultUsernameMinUpperCase);
        int minLowerCase = valueOrDefault(policy != null ? policy.getMinLowerCase() : null, defaultUsernameMinLowerCase);
        int minNumbers = valueOrDefault(policy != null ? policy.getMinNumbers() : null, defaultUsernameMinNumbers);
        int minSpecialCharacters = valueOrDefault(policy != null ? policy.getMinSpecialCharacters() : null, defaultUsernameMinSpecialCharacters);
        int minLength = valueOrDefault(policy != null ? policy.getMinLength() : null, defaultUsernameMinLength);
        int maxLength = valueOrDefault(policy != null ? policy.getMaxLength() : null, defaultUsernameMaxLength);

        if (!matchesPolicy(username, minUpperCase, minLowerCase, minNumbers, minSpecialCharacters, minLength, maxLength)) {
            throw new BadRequestException("user.create.username.policy.invalid");
        }
    }

    private void validatePasswordAgainstPolicy(String password) {
        if (!StringUtils.hasText(password)) {
            return;
        }

        PasswordPolicy policy = passwordPolicyRepository.findTopByOrderByIdAsc().orElse(null);
        int minUpperCase = valueOrDefault(policy != null ? policy.getMinUpperCase() : null, defaultPasswordMinUpperCase);
        int minLowerCase = valueOrDefault(policy != null ? policy.getMinLowerCase() : null, defaultPasswordMinLowerCase);
        int minNumbers = valueOrDefault(policy != null ? policy.getMinNumbers() : null, defaultPasswordMinNumbers);
        int minSpecialCharacters = valueOrDefault(policy != null ? policy.getMinSpecialCharacters() : null, defaultPasswordMinSpecialCharacters);
        int minLength = valueOrDefault(policy != null ? policy.getMinLength() : null, defaultPasswordMinLength);
        int maxLength = valueOrDefault(policy != null ? policy.getMaxLength() : null, defaultPasswordMaxLength);

        if (!matchesPolicy(password, minUpperCase, minLowerCase, minNumbers, minSpecialCharacters, minLength, maxLength)) {
            throw new BadRequestException("user.create.password.policy.invalid");
        }
    }

    private int valueOrDefault(Integer value, int fallback) {
        return value != null ? Math.max(0, value) : Math.max(0, fallback);
    }

    private boolean matchesPolicy(String value,
                                  int minUpperCase,
                                  int minLowerCase,
                                  int minNumbers,
                                  int minSpecialCharacters,
                                  int minLength,
                                  int maxLength) {
        if (!StringUtils.hasText(value)) {
            return false;
        }

        if (value.length() < minLength) {
            return false;
        }
        if (maxLength > 0 && value.length() > maxLength) {
            return false;
        }

        int upperCount = 0;
        int lowerCount = 0;
        int numberCount = 0;
        int specialCount = 0;

        for (char ch : value.toCharArray()) {
            if (Character.isUpperCase(ch)) {
                upperCount++;
            } else if (Character.isLowerCase(ch)) {
                lowerCount++;
            } else if (Character.isDigit(ch)) {
                numberCount++;
            } else if (!Character.isWhitespace(ch)) {
                specialCount++;
            }
        }

        return upperCount >= minUpperCase
                && lowerCount >= minLowerCase
                && numberCount >= minNumbers
                && specialCount >= minSpecialCharacters;
    }

    private Company resolveCompany(String companyCode) {
        if (!StringUtils.hasText(companyCode)) {
            return null;
        }
        return companyRepository.findByCode(companyCode)
                .orElseThrow(() -> new BadRequestException("user.create.company.notfound"));
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

    private String normalize(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim().toLowerCase(LocaleContextHolder.getLocale());
    }

    private boolean matches(String searchValue, String actual) {
        if (!StringUtils.hasText(searchValue)) {
            return true;
        }
        return actual != null && actual.toLowerCase(LocaleContextHolder.getLocale()).contains(searchValue);
    }

    private boolean matchesStatus(String status, UserStatus current) {
        if (!StringUtils.hasText(status)) {
            return true;
        }
        String value = current != null ? current.name().toLowerCase(LocaleContextHolder.getLocale()) : "";
        return value.equals(status);
    }

    private boolean matchesLoginStatus(String status, LoginStatus current) {
        if (!StringUtils.hasText(status)) {
            return true;
        }
        String value = current != null ? current.name().toLowerCase(LocaleContextHolder.getLocale()) : "";
        return value.equals(status);
    }

    private java.util.Comparator<UserListItemDto> resolveComparator(String sortColumn, String sortDirection) {
        String column = normalize(sortColumn);
        java.util.Comparator<UserListItemDto> comparator;
        if ("email".equals(column)) {
            comparator = java.util.Comparator.comparing(UserListItemDto::getEmail,
                    java.util.Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
        } else if ("mobile".equals(column)) {
            comparator = java.util.Comparator.comparing(UserListItemDto::getMobile,
                    java.util.Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
        } else if ("firstname".equals(column)) {
            comparator = java.util.Comparator.comparing(UserListItemDto::getFirstName,
                    java.util.Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
        } else if ("lastname".equals(column)) {
            comparator = java.util.Comparator.comparing(UserListItemDto::getLastName,
                    java.util.Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
        } else if ("status".equals(column)) {
            comparator = java.util.Comparator.comparing(UserListItemDto::getStatus,
                    java.util.Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
        } else if ("loginstatus".equals(column)) {
            comparator = java.util.Comparator.comparing(UserListItemDto::getLoginStatus,
                    java.util.Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
        } else if ("createddate".equals(column)) {
            comparator = java.util.Comparator.comparing(UserListItemDto::getCreatedDate,
                    java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder()));
        } else if ("lastmodifieddate".equals(column)) {
            comparator = java.util.Comparator.comparing(UserListItemDto::getLastModifiedDate,
                    java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder()));
        } else {
            comparator = java.util.Comparator.comparing(UserListItemDto::getUsername,
                    java.util.Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
        }

        String dir = normalize(sortDirection);
        if ("desc".equals(dir)) {
            return comparator.reversed();
        }
        return comparator;
    }

    private UserListItemDto toListItem(User user) {
        String status = user.getStatus() != null ? user.getStatus().name() : null;
        String loginStatus = user.getLoginStatus() != null ? user.getLoginStatus().name() : null;

        return UserListItemDto.builder()
                .createdDate(user.getCreatedDate())
                .lastModifiedDate(user.getLastModifiedDate())
                .createdBy(user.getCreatedBy())
                .lastModifiedBy(user.getLastModifiedBy())
                .id(user.getId())
                .username(user.getUsername())
                .roleCode(user.getRole() != null ? user.getRole().getCode() : null)
                .roleDescription(user.getRole() != null ? user.getRole().getDescription() : null)
                .nic(user.getNic())
                .email(user.getEmail())
                .mobile(user.getMobile())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .status(status)
                .statusDescription(statusDescription(user.getStatus()))
                .loginStatus(loginStatus)
                .loginStatusDescription(loginStatusDescription(user.getLoginStatus()))
                .build();
    }

    private String statusDescription(UserStatus status) {
        if (status == null) {
            return null;
        }
        return switch (status) {
            case ACTIVE -> "Active";
            case INACTIVE -> "Inactive";
            case LOCKED -> "Locked";
        };
    }

    private String loginStatusDescription(LoginStatus status) {
        if (status == null) {
            return null;
        }
        return switch (status) {
            case ACTIVE -> "Active";
            case DISABLED -> "Disabled";
            case PASSWORD_EXPIRED -> "Password Expired";
        };
    }

    private UserPrivilegesDto resolvePrivileges(UserReferenceDataRequest request) {
        String username = request != null ? request.getUsername() : null;
        if (!StringUtils.hasText(username)) {
            username = getAuthenticatedUsername();
        }

        if (!StringUtils.hasText(username)) {
            return UserPrivilegesDto.builder()
                    .add(false)
                    .update(false)
                    .view(false)
                    .search(false)
                    .delete(false)
                    .build();
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("user.fetch.notfound"));

        String roleCode = user.getRole() != null && user.getRole().getCode() != null
                ? user.getRole().getCode()
                : null;

        if (!StringUtils.hasText(roleCode)) {
            return UserPrivilegesDto.builder()
                    .add(false)
                    .update(false)
                    .view(false)
                    .search(false)
                    .delete(false)
                    .build();
        }

        var access = authModuleClient.getRolePageTaskAccess(roleCode);
        if (access == null || access.getPages() == null) {
            return UserPrivilegesDto.builder()
                    .add(false)
                    .update(false)
                    .view(false)
                    .search(false)
                    .delete(false)
                    .build();
        }

        String pageCode = request != null && StringUtils.hasText(request.getPageCode())
                ? request.getPageCode()
                : USER_MANAGEMENT_CODE;

        java.util.Map<String, Boolean> taskAccess = new java.util.HashMap<>();
        access.getPages().stream()
                .filter(page -> pageCode.equalsIgnoreCase(page.getPageCode()))
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

        boolean add = hasTask(taskAccess, "ADD", "CREATE", "NEW");
        boolean update = hasTask(taskAccess, "UPDATE", "EDIT");
        boolean view = hasTask(taskAccess, "VIEW", "READ");
        boolean search = hasTask(taskAccess, "SEARCH", "FILTER", "LIST");
        boolean delete = hasTask(taskAccess, "DELETE", "REMOVE", "DEACTIVATE");

        return UserPrivilegesDto.builder()
                .add(add)
                .update(update)
                .view(view)
                .search(search)
                .delete(delete)
                .build();
    }

    private String normalizeTaskKey(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.replaceAll("[^A-Za-z0-9]", "").toUpperCase(LocaleContextHolder.getLocale());
    }

    private boolean hasTask(java.util.Map<String, Boolean> taskAccess, String... tokens) {
        if (taskAccess == null || taskAccess.isEmpty() || tokens == null) {
            return false;
        }
        for (var entry : taskAccess.entrySet()) {
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
        var authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
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
