package com.zynolo_nexus.setting_service.service.impl;

import com.zynolo_nexus.setting_service.client.AuthModuleClient;
import com.zynolo_nexus.setting_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.setting_service.dto.request.UserCompanyCreateRequest;
import com.zynolo_nexus.setting_service.dto.request.UserCompanyFilterRequest;
import com.zynolo_nexus.setting_service.dto.request.UserCompanyFilterSearch;
import com.zynolo_nexus.setting_service.dto.request.UserCompanyReferenceDataRequest;
import com.zynolo_nexus.setting_service.dto.request.UserCompanyStatusUpdateRequest;
import com.zynolo_nexus.setting_service.dto.request.UserCompanyUpdateRequest;
import com.zynolo_nexus.setting_service.dto.response.ReferenceStatusDto;
import com.zynolo_nexus.setting_service.dto.response.UserCompanyDto;
import com.zynolo_nexus.setting_service.dto.response.UserCompanyFilterResultDto;
import com.zynolo_nexus.setting_service.dto.response.UserCompanyListItemDto;
import com.zynolo_nexus.setting_service.dto.response.UserCompanyPrivilegesDto;
import com.zynolo_nexus.setting_service.dto.response.UserCompanyReferenceCompanyDto;
import com.zynolo_nexus.setting_service.dto.response.UserCompanyReferenceDataDto;
import com.zynolo_nexus.setting_service.dto.response.UserCompanyReferenceRoleDto;
import com.zynolo_nexus.setting_service.dto.response.UserCompanyReferenceUserDto;
import com.zynolo_nexus.setting_service.enums.CompanyStatus;
import com.zynolo_nexus.setting_service.enums.RoleStatus;
import com.zynolo_nexus.setting_service.enums.UserCompanyStatus;
import com.zynolo_nexus.setting_service.enums.UserStatus;
import com.zynolo_nexus.setting_service.exception.BadRequestException;
import com.zynolo_nexus.setting_service.exception.NotFoundException;
import com.zynolo_nexus.setting_service.model.Company;
import com.zynolo_nexus.setting_service.model.Role;
import com.zynolo_nexus.setting_service.model.User;
import com.zynolo_nexus.setting_service.model.UserCompany;
import com.zynolo_nexus.setting_service.repository.CompanyRepository;
import com.zynolo_nexus.setting_service.repository.RoleRepository;
import com.zynolo_nexus.setting_service.repository.UserCompanyRepository;
import com.zynolo_nexus.setting_service.repository.UserRepository;
import com.zynolo_nexus.setting_service.service.UserCompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserCompanyServiceImpl implements UserCompanyService {

    private static final String USER_COMPANY_MANAGEMENT_CODE = "UCOM";

    private final UserCompanyRepository userCompanyRepository;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final RoleRepository roleRepository;
    private final AuthModuleClient authModuleClient;

    @Override
    @Transactional
    public MessageResponseDTO<UserCompanyDto> createUserCompany(UserCompanyCreateRequest request) {
        if (request == null
                || (request.getUserId() == null && !StringUtils.hasText(request.getUserName()))
                || (request.getCompanyId() == null && !StringUtils.hasText(request.getCompanyCode()))
                || (request.getRoleId() == null && !StringUtils.hasText(request.getRoleCode()))) {
            throw new BadRequestException("user.company.invalid");
        }

        User user = resolveUser(request.getUserId(), request.getUserName());
        Company company = resolveCompany(request.getCompanyId(), request.getCompanyCode());
        Role role = resolveRole(request.getRoleId(), request.getRoleCode());

        userCompanyRepository.findByUserAndCompany_Id(user, company.getId())
                .ifPresent(existing -> { throw new BadRequestException("user.company.exists"); });

        boolean isDefault = Boolean.TRUE.equals(request.getIsDefault());
        if (isDefault) {
            clearDefaultForUser(user);
        }

        UserCompany mapping = UserCompany.builder()
                .user(user)
                .company(company)
                .role(role)
                .status(request.getStatus() != null ? request.getStatus() : UserCompanyStatus.ACTIVE)
                .isDefault(isDefault)
                .build();

        mapping = userCompanyRepository.save(mapping);

        return MessageResponseDTO.<UserCompanyDto>builder()
                .success(true)
                .message("User company created successfully")
                .data(toDto(mapping))
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    @Transactional
    public MessageResponseDTO<UserCompanyDto> updateUserCompany(UserCompanyUpdateRequest request) {
        if (request == null || request.getId() == null) {
            return MessageResponseDTO.<UserCompanyDto>builder()
                    .success(false)
                    .message("Invalid update request")
                    .data(null)
                    .errors(null)
                    .errorCode(400)
                    .responseTime(LocalDateTime.now())
                    .build();
        }

        UserCompany mapping = userCompanyRepository.findById(request.getId())
                .orElseThrow(() -> new NotFoundException("user.company.notfound"));

        Long companyId = request.getCompanyId();
        if (companyId == null && StringUtils.hasText(request.getCompanyCode())) {
            companyId = resolveCompany(null, request.getCompanyCode()).getId();
        }
        if (companyId != null && !companyId.equals(mapping.getCompany().getId())) {
            Company company = companyRepository.findById(companyId)
                    .orElseThrow(() -> new NotFoundException("company.notfound"));
            userCompanyRepository.findByUserAndCompany_Id(mapping.getUser(), company.getId())
                    .ifPresent(existing -> { throw new BadRequestException("user.company.exists"); });
            mapping.setCompany(company);
        }

        Long roleId = request.getRoleId();
        if (roleId == null && StringUtils.hasText(request.getRoleCode())) {
            roleId = resolveRole(null, request.getRoleCode()).getId();
        }
        if (roleId != null) {
            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new NotFoundException("role.notfound"));
            mapping.setRole(role);
        }

        if (request.getStatus() != null) {
            mapping.setStatus(request.getStatus());
        }

        if (request.getIsDefault() != null) {
            boolean isDefault = Boolean.TRUE.equals(request.getIsDefault());
            if (isDefault) {
                clearDefaultForUser(mapping.getUser());
            }
            mapping.setIsDefault(isDefault);
        }

        mapping = userCompanyRepository.save(mapping);

        return MessageResponseDTO.<UserCompanyDto>builder()
                .success(true)
                .message("User company updated successfully")
                .data(toDto(mapping))
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    public MessageResponseDTO<UserCompanyDto> viewUserCompany(Long id) {
        if (id == null) {
            return MessageResponseDTO.<UserCompanyDto>builder()
                    .success(false)
                    .message("Invalid view request")
                    .data(null)
                    .errors(null)
                    .errorCode(400)
                    .responseTime(LocalDateTime.now())
                    .build();
        }

        UserCompany mapping = userCompanyRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("user.company.notfound"));

        return MessageResponseDTO.<UserCompanyDto>builder()
                .success(true)
                .message("User company found with ID: " + id)
                .data(toDto(mapping))
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    @Transactional
    public MessageResponseDTO<UserCompanyDto> updateStatus(UserCompanyStatusUpdateRequest request) {
        if (request == null || request.getId() == null) {
            return MessageResponseDTO.<UserCompanyDto>builder()
                    .success(false)
                    .message("Invalid status request")
                    .data(null)
                    .errors(null)
                    .errorCode(400)
                    .responseTime(LocalDateTime.now())
                    .build();
        }

        UserCompany mapping = userCompanyRepository.findById(request.getId())
                .orElseThrow(() -> new NotFoundException("user.company.notfound"));

        UserCompanyStatus status = request.getStatus() != null
                ? request.getStatus()
                : mapping.getStatus();
        mapping.setStatus(status != null ? status : UserCompanyStatus.ACTIVE);
        mapping = userCompanyRepository.save(mapping);

        return MessageResponseDTO.<UserCompanyDto>builder()
                .success(true)
                .message("User company status updated successfully")
                .data(toDto(mapping))
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    public MessageResponseDTO<UserCompanyFilterResultDto> filterList(UserCompanyFilterRequest request) {
        List<UserCompany> mappings = userCompanyRepository.findAll();

        UserCompanyFilterSearch search = request != null ? request.getSearch() : null;
        String username = search != null ? normalize(search.getUsername()) : null;
        String companyCode = search != null ? normalize(search.getCompanyCode()) : null;
        String roleCode = search != null ? normalize(search.getRoleCode()) : null;
        String status = search != null ? normalize(search.getStatus()) : null;

        List<UserCompanyListItemDto> filtered = mappings.stream()
                .filter(mapping -> matches(username, mapping.getUser() != null ? mapping.getUser().getUsername() : null))
                .filter(mapping -> matches(companyCode, mapping.getCompany() != null ? mapping.getCompany().getCode() : null))
                .filter(mapping -> matches(roleCode, mapping.getRole() != null ? mapping.getRole().getCode() : null))
                .filter(mapping -> matchesStatus(status, mapping.getStatus()))
                .map(this::toListItem)
                .toList();

        Comparator<UserCompanyListItemDto> comparator = resolveComparator(
                request != null ? request.getSortColumn() : null,
                request != null ? request.getSortDirection() : null
        );

        List<UserCompanyListItemDto> sorted = filtered.stream().sorted(comparator).toList();

        int size = request != null && request.getSize() != null && request.getSize() > 0 ? request.getSize() : 10;
        int page = request != null && request.getPage() != null && request.getPage() >= 0 ? request.getPage() : 0;
        int totalElements = sorted.size();
        int totalPages = size == 0 ? 1 : (int) Math.ceil((double) totalElements / size);
        int fromIndex = Math.min(page * size, totalElements);
        int toIndex = Math.min(fromIndex + size, totalElements);
        List<UserCompanyListItemDto> pageItems = sorted.subList(fromIndex, toIndex);

        UserCompanyFilterResultDto result = UserCompanyFilterResultDto.builder()
                .content(pageItems)
                .totalRecords(totalElements)
                .totalPages(totalPages)
                .page(page)
                .size(size)
                .build();

        return MessageResponseDTO.<UserCompanyFilterResultDto>builder()
                .success(true)
                .message("User companies filtered successfully")
                .data(result)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    public MessageResponseDTO<UserCompanyReferenceDataDto> getReferenceData(UserCompanyReferenceDataRequest request) {
        UserCompanyPrivilegesDto privileges = resolvePrivileges(request);

        List<UserCompanyReferenceCompanyDto> companies = companyRepository.findAllByStatusOrderByCodeAsc(CompanyStatus.ACTIVE)
                .stream()
                .map(company -> UserCompanyReferenceCompanyDto.builder()
                        .id(company.getId())
                        .code(company.getCode())
                        .description(company.getDescription())
                        .build())
                .toList();

        List<UserCompanyReferenceRoleDto> roles = roleRepository.findAll().stream()
                .filter(role -> role.getStatus() == null || role.getStatus() == RoleStatus.ACTIVE)
                .map(role -> UserCompanyReferenceRoleDto.builder()
                        .id(role.getId())
                        .code(role.getCode())
                        .description(role.getDescription())
                        .build())
                .toList();

        List<UserCompanyReferenceUserDto> users = userRepository.findAll().stream()
                .filter(user -> user.getStatus() == null || user.getStatus() == UserStatus.ACTIVE)
                .map(user -> UserCompanyReferenceUserDto.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .build())
                .toList();

        UserCompanyReferenceDataDto data = UserCompanyReferenceDataDto.builder()
                .companies(companies)
                .roles(roles)
                .users(users)
                .defaultStatus(List.of(
                        ReferenceStatusDto.builder().code("ACTIVE").description("Active").build(),
                        ReferenceStatusDto.builder().code("INACTIVE").description("Inactive").build()
                ))
                .privileges(privileges)
                .build();

        return MessageResponseDTO.<UserCompanyReferenceDataDto>builder()
                .success(true)
                .message("Reference data UCOM retrieved successfully")
                .data(data)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    private void clearDefaultForUser(User user) {
        if (user == null) {
            return;
        }
        userCompanyRepository.findByUser(user).stream()
                .filter(mapping -> Boolean.TRUE.equals(mapping.getIsDefault()))
                .forEach(mapping -> {
                    mapping.setIsDefault(false);
                    userCompanyRepository.save(mapping);
                });
    }

    private UserCompanyDto toDto(UserCompany mapping) {
        UserCompanyStatus status = mapping.getStatus() != null ? mapping.getStatus() : UserCompanyStatus.ACTIVE;
        return UserCompanyDto.builder()
                .id(mapping.getId())
                .userId(mapping.getUser() != null ? mapping.getUser().getId() : null)
                .username(mapping.getUser() != null ? mapping.getUser().getUsername() : null)
                .companyId(mapping.getCompany() != null ? mapping.getCompany().getId() : null)
                .companyCode(mapping.getCompany() != null ? mapping.getCompany().getCode() : null)
                .roleId(mapping.getRole() != null ? mapping.getRole().getId() : null)
                .roleCode(mapping.getRole() != null ? mapping.getRole().getCode() : null)
                .status(status.name())
                .statusDescription(status == UserCompanyStatus.INACTIVE ? "Inactive" : "Active")
                .isDefault(Boolean.TRUE.equals(mapping.getIsDefault()))
                .createdDate(mapping.getCreatedDate())
                .lastModifiedDate(mapping.getLastModifiedDate())
                .createdBy(mapping.getCreatedBy())
                .lastModifiedBy(mapping.getLastModifiedBy())
                .build();
    }

    private UserCompanyListItemDto toListItem(UserCompany mapping) {
        UserCompanyStatus status = mapping.getStatus() != null ? mapping.getStatus() : UserCompanyStatus.ACTIVE;
        return UserCompanyListItemDto.builder()
                .id(mapping.getId())
                .userId(mapping.getUser() != null ? mapping.getUser().getId() : null)
                .username(mapping.getUser() != null ? mapping.getUser().getUsername() : null)
                .companyId(mapping.getCompany() != null ? mapping.getCompany().getId() : null)
                .companyCode(mapping.getCompany() != null ? mapping.getCompany().getCode() : null)
                .roleId(mapping.getRole() != null ? mapping.getRole().getId() : null)
                .roleCode(mapping.getRole() != null ? mapping.getRole().getCode() : null)
                .status(status.name())
                .statusDescription(status == UserCompanyStatus.INACTIVE ? "Inactive" : "Active")
                .isDefault(Boolean.TRUE.equals(mapping.getIsDefault()))
                .createdDate(mapping.getCreatedDate())
                .lastModifiedDate(mapping.getLastModifiedDate())
                .createdBy(mapping.getCreatedBy())
                .lastModifiedBy(mapping.getLastModifiedBy())
                .build();
    }

    private String normalize(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private boolean matches(String searchValue, String actual) {
        if (!StringUtils.hasText(searchValue)) {
            return true;
        }
        return actual != null && actual.toLowerCase(Locale.ROOT).contains(searchValue);
    }

    private boolean matchesStatus(String status, UserCompanyStatus current) {
        if (!StringUtils.hasText(status)) {
            return true;
        }
        String value = current == UserCompanyStatus.INACTIVE ? "inactive" : "active";
        return value.equals(status);
    }

    private Comparator<UserCompanyListItemDto> resolveComparator(String sortColumn, String sortDirection) {
        String column = normalize(sortColumn);
        Comparator<UserCompanyListItemDto> comparator;
        if ("username".equals(column)) {
            comparator = Comparator.comparing(UserCompanyListItemDto::getUsername, String.CASE_INSENSITIVE_ORDER);
        } else if ("companycode".equals(column)) {
            comparator = Comparator.comparing(UserCompanyListItemDto::getCompanyCode, String.CASE_INSENSITIVE_ORDER);
        } else if ("rolecode".equals(column)) {
            comparator = Comparator.comparing(UserCompanyListItemDto::getRoleCode, String.CASE_INSENSITIVE_ORDER);
        } else if ("status".equals(column)) {
            comparator = Comparator.comparing(UserCompanyListItemDto::getStatus, String.CASE_INSENSITIVE_ORDER);
        } else if ("createddate".equals(column)) {
            comparator = Comparator.comparing(UserCompanyListItemDto::getCreatedDate, Comparator.nullsLast(Comparator.naturalOrder()));
        } else if ("lastmodifieddate".equals(column)) {
            comparator = Comparator.comparing(UserCompanyListItemDto::getLastModifiedDate, Comparator.nullsLast(Comparator.naturalOrder()));
        } else {
            comparator = Comparator.comparing(UserCompanyListItemDto::getId, Comparator.nullsLast(Comparator.naturalOrder()));
        }

        String dir = normalize(sortDirection);
        if ("desc".equals(dir)) {
            return comparator.reversed();
        }
        return comparator;
    }

    private UserCompanyPrivilegesDto resolvePrivileges(UserCompanyReferenceDataRequest request) {
        String username = request != null ? request.getUsername() : null;
        if (!StringUtils.hasText(username)) {
            username = getAuthenticatedUsername();
        }

        if (!StringUtils.hasText(username)) {
            return UserCompanyPrivilegesDto.builder()
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
            return UserCompanyPrivilegesDto.builder()
                    .add(false)
                    .update(false)
                    .view(false)
                    .search(false)
                    .delete(false)
                    .build();
        }

        var access = authModuleClient.getRolePageTaskAccess(roleCode);
        if (access == null || access.getPages() == null) {
            return UserCompanyPrivilegesDto.builder()
                    .add(false)
                    .update(false)
                    .view(false)
                    .search(false)
                    .delete(false)
                    .build();
        }

        Map<String, Boolean> taskAccess = new HashMap<>();
        access.getPages().stream()
                .filter(page -> USER_COMPANY_MANAGEMENT_CODE.equalsIgnoreCase(page.getPageCode()))
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

        return UserCompanyPrivilegesDto.builder()
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

    private User resolveUser(Long userId, String username) {
        if (userId != null) {
            return userRepository.findById(userId)
                    .orElseThrow(() -> new NotFoundException("user.notfound"));
        }
        if (StringUtils.hasText(username)) {
            return userRepository.findByUsername(username)
                    .orElseThrow(() -> new NotFoundException("user.notfound"));
        }
        throw new NotFoundException("user.notfound");
    }

    private Company resolveCompany(Long companyId, String companyCode) {
        if (companyId != null) {
            return companyRepository.findById(companyId)
                    .orElseThrow(() -> new NotFoundException("company.notfound"));
        }
        if (StringUtils.hasText(companyCode)) {
            return companyRepository.findByCode(companyCode)
                    .orElseThrow(() -> new NotFoundException("company.notfound"));
        }
        throw new NotFoundException("company.notfound");
    }

    private Role resolveRole(Long roleId, String roleCode) {
        if (roleId != null) {
            return roleRepository.findById(roleId)
                    .orElseThrow(() -> new NotFoundException("role.notfound"));
        }
        if (StringUtils.hasText(roleCode)) {
            return roleRepository.findByCodeIgnoreCase(roleCode)
                    .orElseThrow(() -> new NotFoundException("role.notfound"));
        }
        throw new NotFoundException("role.notfound");
    }
}
