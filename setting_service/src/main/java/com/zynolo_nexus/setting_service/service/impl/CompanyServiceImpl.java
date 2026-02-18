package com.zynolo_nexus.setting_service.service.impl;

import com.zynolo_nexus.setting_service.client.AuthModuleClient;
import com.zynolo_nexus.setting_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.setting_service.dto.request.CompanyCreateRequest;
import com.zynolo_nexus.setting_service.dto.request.CompanyFilterRequest;
import com.zynolo_nexus.setting_service.dto.request.CompanyFilterSearch;
import com.zynolo_nexus.setting_service.dto.request.CompanyReferenceDataRequest;
import com.zynolo_nexus.setting_service.dto.request.CompanyStatusUpdateRequest;
import com.zynolo_nexus.setting_service.dto.request.CompanyUpdateRequest;
import com.zynolo_nexus.setting_service.dto.response.CompanyDto;
import com.zynolo_nexus.setting_service.dto.response.CompanyFilterResultDto;
import com.zynolo_nexus.setting_service.dto.response.CompanyListItemDto;
import com.zynolo_nexus.setting_service.dto.response.CompanyPrivilegesDto;
import com.zynolo_nexus.setting_service.dto.response.CompanyReferenceDataDto;
import com.zynolo_nexus.setting_service.dto.response.ReferenceStatusDto;
import com.zynolo_nexus.setting_service.enums.CompanyStatus;
import com.zynolo_nexus.setting_service.exception.BadRequestException;
import com.zynolo_nexus.setting_service.exception.NotFoundException;
import com.zynolo_nexus.setting_service.model.Company;
import com.zynolo_nexus.setting_service.model.User;
import com.zynolo_nexus.setting_service.repository.CompanyRepository;
import com.zynolo_nexus.setting_service.repository.UserRepository;
import com.zynolo_nexus.setting_service.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CompanyServiceImpl implements CompanyService {

    private static final String COMPANY_MANAGEMENT_CODE = "COMM";

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final AuthModuleClient authModuleClient;

    @Override
    public MessageResponseDTO<CompanyDto> createCompany(CompanyCreateRequest request) {
        if (request == null
                || !StringUtils.hasText(request.getCode())
                || !StringUtils.hasText(request.getDescription())
                || !StringUtils.hasText(request.getStreet1())
                || !StringUtils.hasText(request.getCity())
                || !StringUtils.hasText(request.getState())
                || !StringUtils.hasText(request.getCountry())
                || !StringUtils.hasText(request.getZipCode())
                || !StringUtils.hasText(request.getPhoneNumber())) {
            throw new BadRequestException("company.invalid");
        }

        String code = normalizeCode(request.getCode());
        if (companyRepository.existsByCodeIgnoreCase(code)) {
            throw new BadRequestException("company.code.exists");
        }

        CompanyStatus status = request.getStatus() != null ? request.getStatus() : CompanyStatus.ACTIVE;

        Company company = Company.builder()
                .code(code)
                .description(request.getDescription().trim())
                .street1(request.getStreet1().trim())
                .street2(trimToNull(request.getStreet2()))
                .city(request.getCity().trim())
                .state(request.getState().trim())
                .country(request.getCountry().trim())
                .zipCode(request.getZipCode().trim())
                .phoneNumber(request.getPhoneNumber().trim())
                .mobileNumber(trimToNull(request.getMobileNumber()))
                .email(trimToNull(request.getEmail()))
                .website(trimToNull(request.getWebsite()))
                .taxId(trimToNull(request.getTaxId()))
                .status(status)
                .build();

        company = companyRepository.save(company);

        return MessageResponseDTO.<CompanyDto>builder()
                .success(true)
                .message("Company created successfully")
                .data(toDto(company))
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    public MessageResponseDTO<CompanyDto> updateCompany(CompanyUpdateRequest request) {
        if (request == null || request.getId() == null) {
            return MessageResponseDTO.<CompanyDto>builder()
                    .success(false)
                    .message("Invalid update request")
                    .data(null)
                    .errors(null)
                    .errorCode(400)
                    .responseTime(LocalDateTime.now())
                    .build();
        }

        Company company = companyRepository.findById(request.getId())
                .orElseThrow(() -> new NotFoundException("company.notfound"));

        if (StringUtils.hasText(request.getCode())) {
            String code = normalizeCode(request.getCode());
            if (!code.equalsIgnoreCase(company.getCode()) && companyRepository.existsByCodeIgnoreCase(code)) {
                throw new BadRequestException("company.code.exists");
            }
            company.setCode(code);
        }

        if (StringUtils.hasText(request.getDescription())) {
            company.setDescription(request.getDescription().trim());
        }

        if (request.getStreet1() != null) {
            company.setStreet1(trimToNull(request.getStreet1()));
        }
        if (request.getStreet2() != null) {
            company.setStreet2(trimToNull(request.getStreet2()));
        }
        if (request.getCity() != null) {
            company.setCity(trimToNull(request.getCity()));
        }
        if (request.getState() != null) {
            company.setState(trimToNull(request.getState()));
        }
        if (request.getCountry() != null) {
            company.setCountry(trimToNull(request.getCountry()));
        }
        if (request.getZipCode() != null) {
            company.setZipCode(trimToNull(request.getZipCode()));
        }
        if (request.getPhoneNumber() != null) {
            company.setPhoneNumber(trimToNull(request.getPhoneNumber()));
        }
        if (request.getMobileNumber() != null) {
            company.setMobileNumber(trimToNull(request.getMobileNumber()));
        }
        if (request.getEmail() != null) {
            company.setEmail(trimToNull(request.getEmail()));
        }
        if (request.getWebsite() != null) {
            company.setWebsite(trimToNull(request.getWebsite()));
        }
        if (request.getTaxId() != null) {
            company.setTaxId(trimToNull(request.getTaxId()));
        }

        if (!StringUtils.hasText(company.getStreet1())
                || !StringUtils.hasText(company.getCity())
                || !StringUtils.hasText(company.getState())
                || !StringUtils.hasText(company.getCountry())
                || !StringUtils.hasText(company.getZipCode())
                || !StringUtils.hasText(company.getPhoneNumber())) {
            throw new BadRequestException("company.invalid");
        }

        if (request.getStatus() != null) {
            company.setStatus(request.getStatus());
        }

        company = companyRepository.save(company);

        return MessageResponseDTO.<CompanyDto>builder()
                .success(true)
                .message("Company updated successfully")
                .data(toDto(company))
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    public MessageResponseDTO<CompanyDto> viewCompany(Long id) {
        if (id == null) {
            return MessageResponseDTO.<CompanyDto>builder()
                    .success(false)
                    .message("Invalid view request")
                    .data(null)
                    .errors(null)
                    .errorCode(400)
                    .responseTime(LocalDateTime.now())
                    .build();
        }

        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("company.notfound"));

        return MessageResponseDTO.<CompanyDto>builder()
                .success(true)
                .message("Company found with ID: " + id)
                .data(toDto(company))
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    public MessageResponseDTO<CompanyDto> updateStatus(CompanyStatusUpdateRequest request) {
        if (request == null || request.getId() == null) {
            return MessageResponseDTO.<CompanyDto>builder()
                    .success(false)
                    .message("Invalid status request")
                    .data(null)
                    .errors(null)
                    .errorCode(400)
                    .responseTime(LocalDateTime.now())
                    .build();
        }

        Company company = companyRepository.findById(request.getId())
                .orElseThrow(() -> new NotFoundException("company.notfound"));

        CompanyStatus status = request.getStatus() != null ? request.getStatus() : company.getStatus();
        company.setStatus(status != null ? status : CompanyStatus.ACTIVE);

        company = companyRepository.save(company);

        return MessageResponseDTO.<CompanyDto>builder()
                .success(true)
                .message("Company status updated successfully")
                .data(toDto(company))
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    public MessageResponseDTO<CompanyFilterResultDto> filterList(CompanyFilterRequest request) {
        List<Company> companies = companyRepository.findAll();

        CompanyFilterSearch search = request != null ? request.getSearch() : null;
        String code = search != null ? normalize(search.getCode()) : null;
        String description = search != null ? normalize(search.getDescription()) : null;
        String status = search != null ? normalize(search.getStatus()) : null;

        List<CompanyListItemDto> filtered = companies.stream()
                .filter(company -> matches(code, company.getCode()))
                .filter(company -> matches(description, company.getDescription()))
                .filter(company -> matchesStatus(status, company.getStatus()))
                .map(company -> {
                    CompanyStatus current = company.getStatus() != null ? company.getStatus() : CompanyStatus.ACTIVE;
                    return CompanyListItemDto.builder()
                            .id(company.getId())
                            .code(company.getCode())
                            .description(company.getDescription())
                            .street1(company.getStreet1())
                            .street2(company.getStreet2())
                            .city(company.getCity())
                            .state(company.getState())
                            .country(company.getCountry())
                            .zipCode(company.getZipCode())
                            .phoneNumber(company.getPhoneNumber())
                            .mobileNumber(company.getMobileNumber())
                            .email(company.getEmail())
                            .website(company.getWebsite())
                            .taxId(company.getTaxId())
                            .status(current.name())
                            .statusDescription(current == CompanyStatus.DEACTIVE ? "Inactive" : "Active")
                            .createdDate(company.getCreatedDate())
                            .lastModifiedDate(company.getLastModifiedDate())
                            .createdBy(company.getCreatedBy())
                            .lastModifiedBy(company.getLastModifiedBy())
                            .build();
                })
                .toList();

        Comparator<CompanyListItemDto> comparator = resolveComparator(
                request != null ? request.getSortColumn() : null,
                request != null ? request.getSortDirection() : null
        );

        List<CompanyListItemDto> sorted = filtered.stream().sorted(comparator).toList();

        int size = request != null && request.getSize() != null && request.getSize() > 0 ? request.getSize() : 10;
        int page = request != null && request.getPage() != null && request.getPage() >= 0 ? request.getPage() : 0;
        int totalElements = sorted.size();
        int totalPages = size == 0 ? 1 : (int) Math.ceil((double) totalElements / size);
        int fromIndex = Math.min(page * size, totalElements);
        int toIndex = Math.min(fromIndex + size, totalElements);
        List<CompanyListItemDto> pageItems = sorted.subList(fromIndex, toIndex);

        CompanyFilterResultDto result = CompanyFilterResultDto.builder()
                .content(pageItems)
                .totalRecords(totalElements)
                .totalPages(totalPages)
                .page(page)
                .size(size)
                .build();

        return MessageResponseDTO.<CompanyFilterResultDto>builder()
                .success(true)
                .message("Company list filtered successfully")
                .data(result)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    public MessageResponseDTO<CompanyReferenceDataDto> getReferenceData(CompanyReferenceDataRequest request) {
        CompanyPrivilegesDto privileges = resolvePrivileges(request);
        CompanyReferenceDataDto data = CompanyReferenceDataDto.builder()
                .defaultStatus(List.of(
                        ReferenceStatusDto.builder().code("ACTIVE").description("Active").build(),
                        ReferenceStatusDto.builder().code("DEACTIVE").description("Inactive").build()
                ))
                .privileges(privileges)
                .build();

        return MessageResponseDTO.<CompanyReferenceDataDto>builder()
                .success(true)
                .message("Reference data COMM retrieved successfully")
                .data(data)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    private CompanyDto toDto(Company company) {
        CompanyStatus current = company.getStatus() != null ? company.getStatus() : CompanyStatus.ACTIVE;
        return CompanyDto.builder()
                .id(company.getId())
                .code(company.getCode())
                .description(company.getDescription())
                .street1(company.getStreet1())
                .street2(company.getStreet2())
                .city(company.getCity())
                .state(company.getState())
                .country(company.getCountry())
                .zipCode(company.getZipCode())
                .phoneNumber(company.getPhoneNumber())
                .mobileNumber(company.getMobileNumber())
                .email(company.getEmail())
                .website(company.getWebsite())
                .taxId(company.getTaxId())
                .status(current)
                .statusDescription(current == CompanyStatus.DEACTIVE ? "Inactive" : "Active")
                .createdDate(company.getCreatedDate())
                .lastModifiedDate(company.getLastModifiedDate())
                .createdBy(company.getCreatedBy())
                .lastModifiedBy(company.getLastModifiedBy())
                .build();
    }

    private String normalizeCode(String value) {
        return value != null ? value.trim() : null;
    }

    private String normalize(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean matches(String searchValue, String actual) {
        if (!StringUtils.hasText(searchValue)) {
            return true;
        }
        return actual != null && actual.toLowerCase(Locale.ROOT).contains(searchValue);
    }

    private boolean matchesStatus(String status, CompanyStatus companyStatus) {
        if (!StringUtils.hasText(status)) {
            return true;
        }
        String value = status.trim().toLowerCase(Locale.ROOT);
        if ("deactive".equals(value)) {
            value = "inactive";
        }
        String current = companyStatus == CompanyStatus.DEACTIVE ? "inactive" : "active";
        return current.equals(value);
    }

    private Comparator<CompanyListItemDto> resolveComparator(String sortColumn, String sortDirection) {
        String column = normalize(sortColumn);
        Comparator<CompanyListItemDto> comparator;
        if ("description".equals(column)) {
            comparator = Comparator.comparing(CompanyListItemDto::getDescription, String.CASE_INSENSITIVE_ORDER);
        } else if ("status".equals(column)) {
            comparator = Comparator.comparing(CompanyListItemDto::getStatus, String.CASE_INSENSITIVE_ORDER);
        } else if ("createddate".equals(column)) {
            comparator = Comparator.comparing(CompanyListItemDto::getCreatedDate, Comparator.nullsLast(Comparator.naturalOrder()));
        } else if ("lastmodifieddate".equals(column)) {
            comparator = Comparator.comparing(CompanyListItemDto::getLastModifiedDate, Comparator.nullsLast(Comparator.naturalOrder()));
        } else {
            comparator = Comparator.comparing(CompanyListItemDto::getCode, String.CASE_INSENSITIVE_ORDER);
        }

        String dir = normalize(sortDirection);
        if ("desc".equals(dir)) {
            return comparator.reversed();
        }
        return comparator;
    }

    private CompanyPrivilegesDto resolvePrivileges(CompanyReferenceDataRequest request) {
        String username = request != null ? request.getUsername() : null;
        if (!StringUtils.hasText(username)) {
            username = getAuthenticatedUsername();
        }

        if (!StringUtils.hasText(username)) {
            return CompanyPrivilegesDto.builder()
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
            return CompanyPrivilegesDto.builder()
                    .add(false)
                    .update(false)
                    .view(false)
                    .search(false)
                    .delete(false)
                    .build();
        }

        var access = authModuleClient.getRolePageTaskAccess(roleCode);
        if (access == null || access.getPages() == null) {
            return CompanyPrivilegesDto.builder()
                    .add(false)
                    .update(false)
                    .view(false)
                    .search(false)
                    .delete(false)
                    .build();
        }

        Map<String, Boolean> taskAccess = new HashMap<>();
        access.getPages().stream()
                .filter(page -> COMPANY_MANAGEMENT_CODE.equalsIgnoreCase(page.getPageCode()))
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

        return CompanyPrivilegesDto.builder()
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
}
