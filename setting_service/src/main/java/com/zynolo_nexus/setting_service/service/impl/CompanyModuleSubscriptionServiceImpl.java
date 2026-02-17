package com.zynolo_nexus.setting_service.service.impl;

import com.zynolo_nexus.contracts.modules.ModuleDto;
import com.zynolo_nexus.contracts.modules.ModuleStatus;
import com.zynolo_nexus.setting_service.client.AuthModuleClient;
import com.zynolo_nexus.setting_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.setting_service.dto.request.CompanyModuleBulkUpdateRequest;
import com.zynolo_nexus.setting_service.dto.request.CompanyModuleBulkViewRequest;
import com.zynolo_nexus.setting_service.dto.request.CompanyModuleCheckRequest;
import com.zynolo_nexus.setting_service.dto.request.CompanyModuleCreateRequest;
import com.zynolo_nexus.setting_service.dto.request.CompanyModuleFilterRequest;
import com.zynolo_nexus.setting_service.dto.request.CompanyModuleFilterSearch;
import com.zynolo_nexus.setting_service.dto.request.CompanyModuleReferenceDataRequest;
import com.zynolo_nexus.setting_service.dto.request.CompanyModuleStatusUpdateRequest;
import com.zynolo_nexus.setting_service.dto.request.CompanyModuleUpdateRequest;
import com.zynolo_nexus.setting_service.dto.response.CompanyModuleBulkItemDto;
import com.zynolo_nexus.setting_service.dto.response.CompanyModuleBulkViewDto;
import com.zynolo_nexus.setting_service.dto.response.CompanyModuleCheckDto;
import com.zynolo_nexus.setting_service.dto.response.CompanyModuleDto;
import com.zynolo_nexus.setting_service.dto.response.CompanyModuleFilterResultDto;
import com.zynolo_nexus.setting_service.dto.response.CompanyModuleListItemDto;
import com.zynolo_nexus.setting_service.dto.response.CompanyModulePrivilegesDto;
import com.zynolo_nexus.setting_service.dto.response.CompanyModuleReferenceDataDto;
import com.zynolo_nexus.setting_service.dto.response.CompanyModuleReferenceModuleDto;
import com.zynolo_nexus.setting_service.dto.response.ReferenceCompanyDto;
import com.zynolo_nexus.setting_service.dto.response.ReferenceStatusDto;
import com.zynolo_nexus.setting_service.enums.CompanyModuleSubscriptionStatus;
import com.zynolo_nexus.setting_service.enums.CompanyStatus;
import com.zynolo_nexus.setting_service.exception.BadRequestException;
import com.zynolo_nexus.setting_service.exception.NotFoundException;
import com.zynolo_nexus.setting_service.model.Company;
import com.zynolo_nexus.setting_service.model.CompanyModuleSubscription;
import com.zynolo_nexus.setting_service.model.User;
import com.zynolo_nexus.setting_service.repository.CompanyModuleSubscriptionRepository;
import com.zynolo_nexus.setting_service.repository.CompanyRepository;
import com.zynolo_nexus.setting_service.repository.UserRepository;
import com.zynolo_nexus.setting_service.service.CompanyModuleSubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompanyModuleSubscriptionServiceImpl implements CompanyModuleSubscriptionService {

    private static final String COMPANY_MODULE_PAGE_CODE = "COMS";

    private final CompanyModuleSubscriptionRepository companyModuleSubscriptionRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final AuthModuleClient authModuleClient;

    @Override
    @Transactional
    public MessageResponseDTO<CompanyModuleDto> create(CompanyModuleCreateRequest request) {
        if (request == null
                || !StringUtils.hasText(request.getCompanyCode())
                || !StringUtils.hasText(request.getModuleCode())) {
            throw new BadRequestException("company.module.invalid");
        }

        Company company = resolveActiveCompany(request.getCompanyCode());
        ModuleDto module = resolveModule(request.getModuleCode(), true);

        companyModuleSubscriptionRepository.findByCompanyAndModuleCodeIgnoreCase(company, module.getCode())
                .ifPresent(existing -> {
                    throw new BadRequestException("company.module.exists");
                });

        CompanyModuleSubscription entity = CompanyModuleSubscription.builder()
                .company(company)
                .moduleCode(module.getCode())
                .status(toStatus(request.getEnabled()))
                .build();

        entity = companyModuleSubscriptionRepository.save(entity);
        return MessageResponseDTO.<CompanyModuleDto>builder()
                .success(true)
                .message("Company module subscription created successfully")
                .data(toDto(entity, module))
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    public MessageResponseDTO<CompanyModuleDto> view(Long id) {
        if (id == null) {
            return MessageResponseDTO.<CompanyModuleDto>builder()
                    .success(false)
                    .message("Invalid view request")
                    .data(null)
                    .errors(null)
                    .errorCode(400)
                    .responseTime(LocalDateTime.now())
                    .build();
        }
        CompanyModuleSubscription entity = companyModuleSubscriptionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("company.module.notfound"));
        ModuleDto module = resolveModule(entity.getModuleCode(), false);
        return MessageResponseDTO.<CompanyModuleDto>builder()
                .success(true)
                .message("Company module subscription retrieved successfully")
                .data(toDto(entity, module))
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    @Transactional
    public MessageResponseDTO<CompanyModuleDto> update(CompanyModuleUpdateRequest request) {
        if (request == null || request.getId() == null) {
            return MessageResponseDTO.<CompanyModuleDto>builder()
                    .success(false)
                    .message("Invalid update request")
                    .data(null)
                    .errors(null)
                    .errorCode(400)
                    .responseTime(LocalDateTime.now())
                    .build();
        }

        CompanyModuleSubscription entity = companyModuleSubscriptionRepository.findById(request.getId())
                .orElseThrow(() -> new NotFoundException("company.module.notfound"));

        Company company = entity.getCompany();
        if (StringUtils.hasText(request.getCompanyCode())) {
            company = resolveActiveCompany(request.getCompanyCode());
            entity.setCompany(company);
        }

        ModuleDto module = resolveModule(entity.getModuleCode(), false);
        if (StringUtils.hasText(request.getModuleCode())) {
            module = resolveModule(request.getModuleCode(), true);
            entity.setModuleCode(module.getCode());
        }

        if (companyModuleSubscriptionRepository.existsByCompanyAndModuleCodeIgnoreCaseAndIdNot(
                company, entity.getModuleCode(), entity.getId())) {
            throw new BadRequestException("company.module.exists");
        }

        if (request.getEnabled() != null) {
            entity.setStatus(toStatus(request.getEnabled()));
        }

        entity = companyModuleSubscriptionRepository.save(entity);
        return MessageResponseDTO.<CompanyModuleDto>builder()
                .success(true)
                .message("Company module subscription updated successfully")
                .data(toDto(entity, module))
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    @Transactional
    public MessageResponseDTO<CompanyModuleDto> updateStatus(CompanyModuleStatusUpdateRequest request) {
        if (request == null || request.getId() == null) {
            return MessageResponseDTO.<CompanyModuleDto>builder()
                    .success(false)
                    .message("Invalid status request")
                    .data(null)
                    .errors(null)
                    .errorCode(400)
                    .responseTime(LocalDateTime.now())
                    .build();
        }

        CompanyModuleSubscription entity = companyModuleSubscriptionRepository.findById(request.getId())
                .orElseThrow(() -> new NotFoundException("company.module.notfound"));

        if (request.getEnabled() != null) {
            entity.setStatus(toStatus(request.getEnabled()));
        }

        entity = companyModuleSubscriptionRepository.save(entity);
        ModuleDto module = resolveModule(entity.getModuleCode(), false);
        return MessageResponseDTO.<CompanyModuleDto>builder()
                .success(true)
                .message("Company module subscription status updated successfully")
                .data(toDto(entity, module))
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    public MessageResponseDTO<CompanyModuleFilterResultDto> filterList(CompanyModuleFilterRequest request) {
        List<CompanyModuleSubscription> subscriptions = companyModuleSubscriptionRepository.findAll();
        Map<String, ModuleDto> modulesByCode = loadModuleMap(false);

        CompanyModuleFilterSearch search = request != null ? request.getSearch() : null;
        String companyCode = search != null ? normalize(search.getCompanyCode()) : null;
        String companyDescription = search != null ? normalize(search.getCompanyDescription()) : null;
        String moduleCode = search != null ? normalize(search.getModuleCode()) : null;
        String moduleDescription = search != null ? normalize(search.getModuleDescription()) : null;
        String status = search != null ? normalize(search.getStatus()) : null;

        List<CompanyModuleListItemDto> filtered = subscriptions.stream()
                .filter(item -> matches(companyCode, item.getCompany() != null ? item.getCompany().getCode() : null))
                .filter(item -> matches(companyDescription, item.getCompany() != null ? item.getCompany().getDescription() : null))
                .filter(item -> matches(moduleCode, item.getModuleCode()))
                .filter(item -> matches(moduleDescription, resolveModuleDescription(item.getModuleCode(), modulesByCode)))
                .filter(item -> matchesStatus(status, item.getStatus()))
                .map(item -> toListItem(item, modulesByCode.get(normalize(item.getModuleCode()))))
                .toList();

        Comparator<CompanyModuleListItemDto> comparator = resolveComparator(
                request != null ? request.getSortColumn() : null,
                request != null ? request.getSortDirection() : null
        );
        List<CompanyModuleListItemDto> sorted = filtered.stream().sorted(comparator).toList();

        int size = request != null && request.getSize() != null && request.getSize() > 0 ? request.getSize() : 10;
        int page = request != null && request.getPage() != null && request.getPage() >= 0 ? request.getPage() : 0;
        int totalElements = sorted.size();
        int totalPages = size == 0 ? 1 : (int) Math.ceil((double) totalElements / size);
        int fromIndex = Math.min(page * size, totalElements);
        int toIndex = Math.min(fromIndex + size, totalElements);
        List<CompanyModuleListItemDto> pageItems = sorted.subList(fromIndex, toIndex);

        CompanyModuleFilterResultDto result = CompanyModuleFilterResultDto.builder()
                .content(pageItems)
                .totalRecords(totalElements)
                .totalPages(totalPages)
                .page(page)
                .size(size)
                .build();

        return MessageResponseDTO.<CompanyModuleFilterResultDto>builder()
                .success(true)
                .message("Company module subscriptions filtered successfully")
                .data(result)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    public MessageResponseDTO<CompanyModuleReferenceDataDto> referenceData(CompanyModuleReferenceDataRequest request) {
        CompanyModulePrivilegesDto privileges = resolvePrivileges(request);

        List<ReferenceCompanyDto> companies = companyRepository.findAllByStatusOrderByCodeAsc(CompanyStatus.ACTIVE)
                .stream()
                .map(company -> ReferenceCompanyDto.builder()
                        .code(company.getCode())
                        .description(company.getDescription())
                        .build())
                .toList();

        List<CompanyModuleReferenceModuleDto> modules = authModuleClient.getAllModulesAll().stream()
                .filter(module -> module != null && (module.getStatus() == null || module.getStatus() == ModuleStatus.ACTIVE))
                .map(module -> CompanyModuleReferenceModuleDto.builder()
                        .id(module.getId())
                        .code(module.getCode())
                        .description(StringUtils.hasText(module.getDescription()) ? module.getDescription() : module.getName())
                        .build())
                .toList();

        CompanyModuleReferenceDataDto data = CompanyModuleReferenceDataDto.builder()
                .companies(companies)
                .modules(modules)
                .defaultStatus(List.of(
                        ReferenceStatusDto.builder().code("ACTIVE").description("Active").build(),
                        ReferenceStatusDto.builder().code("INACTIVE").description("Inactive").build()
                ))
                .privileges(privileges)
                .build();

        return MessageResponseDTO.<CompanyModuleReferenceDataDto>builder()
                .success(true)
                .message("Reference data COMS retrieved successfully")
                .data(data)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    public MessageResponseDTO<CompanyModuleCheckDto> check(CompanyModuleCheckRequest request) {
        if (request == null || !StringUtils.hasText(request.getModuleCode())) {
            throw new BadRequestException("company.module.invalid");
        }

        Company company = resolveCompanyForCheck(request.getCompanyId(), request.getCompanyCode());
        String moduleCode = request.getModuleCode().trim();
        boolean allowed = companyModuleSubscriptionRepository
                .findByCompanyAndModuleCodeIgnoreCase(company, moduleCode)
                .map(subscription -> subscription.getStatus() == CompanyModuleSubscriptionStatus.ACTIVE)
                .orElse(false);

        CompanyModuleCheckDto data = CompanyModuleCheckDto.builder()
                .companyId(company.getId())
                .companyCode(company.getCode())
                .moduleCode(moduleCode)
                .allowed(allowed)
                .build();

        return MessageResponseDTO.<CompanyModuleCheckDto>builder()
                .success(true)
                .message("Company module access checked successfully")
                .data(data)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    public MessageResponseDTO<CompanyModuleBulkViewDto> bulkView(CompanyModuleBulkViewRequest request) {
        if (request == null || !StringUtils.hasText(request.getCompanyCode())) {
            throw new BadRequestException("company.module.invalid");
        }

        Company company = resolveActiveCompany(request.getCompanyCode());
        CompanyModuleBulkViewDto data = buildBulkView(company);

        return MessageResponseDTO.<CompanyModuleBulkViewDto>builder()
                .success(true)
                .message("Company module matrix retrieved successfully")
                .data(data)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    @Transactional
    public MessageResponseDTO<CompanyModuleBulkViewDto> bulkUpdate(CompanyModuleBulkUpdateRequest request) {
        if (request == null
                || !StringUtils.hasText(request.getCompanyCode())
                || request.getModules() == null
                || request.getModules().isEmpty()) {
            throw new BadRequestException("company.module.invalid");
        }

        Company company = resolveActiveCompany(request.getCompanyCode());
        Map<String, ModuleDto> activeModules = loadModuleMap(true);
        Map<String, CompanyModuleSubscription> existing = companyModuleSubscriptionRepository.findByCompany_Id(company.getId())
                .stream()
                .filter(item -> StringUtils.hasText(item.getModuleCode()))
                .collect(java.util.stream.Collectors.toMap(
                        item -> item.getModuleCode().trim().toLowerCase(Locale.ROOT),
                        item -> item,
                        (left, right) -> left
                ));

        Set<String> seenModuleCodes = new HashSet<>();

        for (CompanyModuleBulkUpdateRequest.ModuleAccess item : request.getModules()) {
            if (item == null || !StringUtils.hasText(item.getModuleCode())) {
                throw new BadRequestException("company.module.invalid");
            }

            String codeKey = item.getModuleCode().trim().toLowerCase(Locale.ROOT);
            if (!seenModuleCodes.add(codeKey)) {
                throw new BadRequestException("company.module.invalid");
            }

            ModuleDto module = activeModules.get(codeKey);
            if (module == null) {
                throw new BadRequestException("module.notfound");
            }

            boolean allowed = Boolean.TRUE.equals(item.getAllowed());
            CompanyModuleSubscription entity = existing.get(codeKey);
            if (entity == null) {
                entity = CompanyModuleSubscription.builder()
                        .company(company)
                        .moduleCode(module.getCode())
                        .status(toStatus(allowed))
                        .build();
            } else {
                entity.setStatus(toStatus(allowed));
            }
            entity = companyModuleSubscriptionRepository.save(entity);
            existing.put(codeKey, entity);
        }

        CompanyModuleBulkViewDto data = buildBulkView(company);
        return MessageResponseDTO.<CompanyModuleBulkViewDto>builder()
                .success(true)
                .message("Company module subscriptions updated successfully")
                .data(data)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    private Company resolveActiveCompany(String companyCode) {
        Company company = companyRepository.findByCodeIgnoreCase(companyCode)
                .orElseThrow(() -> new BadRequestException("company.notfound"));
        if (company.getStatus() != null && company.getStatus() != CompanyStatus.ACTIVE) {
            throw new BadRequestException("company.notfound");
        }
        return company;
    }

    private Company resolveCompanyForCheck(Long companyId, String companyCode) {
        if (companyId != null) {
            return companyRepository.findById(companyId)
                    .orElseThrow(() -> new NotFoundException("company.notfound"));
        }
        if (!StringUtils.hasText(companyCode)) {
            throw new BadRequestException("company.module.invalid");
        }
        return companyRepository.findByCodeIgnoreCase(companyCode)
                .orElseThrow(() -> new NotFoundException("company.notfound"));
    }

    private CompanyModuleBulkViewDto buildBulkView(Company company) {
        Map<String, ModuleDto> activeModulesByCode = loadModuleMap(true);
        Map<String, CompanyModuleSubscription> subscriptionsByCode = companyModuleSubscriptionRepository
                .findByCompany_Id(company.getId())
                .stream()
                .filter(item -> StringUtils.hasText(item.getModuleCode()))
                .collect(java.util.stream.Collectors.toMap(
                        item -> item.getModuleCode().trim().toLowerCase(Locale.ROOT),
                        item -> item,
                        (left, right) -> left
                ));

        List<CompanyModuleBulkItemDto> modules = activeModulesByCode.values().stream()
                .sorted(Comparator
                        .comparing(ModuleDto::getSortOrder, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(ModuleDto::getCode, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .map(module -> {
                    CompanyModuleSubscription subscription =
                            subscriptionsByCode.get(module.getCode().trim().toLowerCase(Locale.ROOT));
                    boolean allowed = subscription != null && isEnabled(subscription.getStatus());
                    String status = allowed
                            ? CompanyModuleSubscriptionStatus.ACTIVE.name()
                            : CompanyModuleSubscriptionStatus.INACTIVE.name();

                    return CompanyModuleBulkItemDto.builder()
                            .subscriptionId(subscription != null ? subscription.getId() : null)
                            .moduleId(module.getId())
                            .moduleCode(module.getCode())
                            .moduleDescription(StringUtils.hasText(module.getDescription())
                                    ? module.getDescription()
                                    : module.getName())
                            .allowed(allowed)
                            .status(status)
                            .statusDescription(allowed ? "Active" : "Inactive")
                            .build();
                })
                .toList();

        return CompanyModuleBulkViewDto.builder()
                .companyId(company.getId())
                .companyCode(company.getCode())
                .companyDescription(company.getDescription())
                .modules(modules)
                .build();
    }

    private ModuleDto resolveModule(String moduleCode, boolean onlyActive) {
        if (!StringUtils.hasText(moduleCode)) {
            throw new BadRequestException("module.notfound");
        }
        return authModuleClient.getAllModulesAll().stream()
                .filter(module -> module != null && StringUtils.hasText(module.getCode()))
                .filter(module -> module.getCode().equalsIgnoreCase(moduleCode))
                .filter(module -> !onlyActive || module.getStatus() == null || module.getStatus() == ModuleStatus.ACTIVE)
                .findFirst()
                .orElseThrow(() -> new BadRequestException("module.notfound"));
    }

    private Map<String, ModuleDto> loadModuleMap(boolean onlyActive) {
        Map<String, ModuleDto> map = new HashMap<>();
        List<ModuleDto> modules = authModuleClient.getAllModulesAll();
        if (modules == null) {
            return map;
        }
        for (ModuleDto module : modules) {
            if (module == null || !StringUtils.hasText(module.getCode())) {
                continue;
            }
            if (onlyActive && module.getStatus() != null && module.getStatus() != ModuleStatus.ACTIVE) {
                continue;
            }
            map.put(module.getCode().trim().toLowerCase(Locale.ROOT), module);
        }
        return map;
    }

    private CompanyModuleSubscriptionStatus toStatus(Boolean enabled) {
        if (enabled == null || enabled) {
            return CompanyModuleSubscriptionStatus.ACTIVE;
        }
        return CompanyModuleSubscriptionStatus.INACTIVE;
    }

    private boolean isEnabled(CompanyModuleSubscriptionStatus status) {
        return status == null || status == CompanyModuleSubscriptionStatus.ACTIVE;
    }

    private String statusDescription(CompanyModuleSubscriptionStatus status) {
        return isEnabled(status) ? "Active" : "Inactive";
    }

    private CompanyModuleDto toDto(CompanyModuleSubscription entity, ModuleDto module) {
        Company company = entity.getCompany();
        return CompanyModuleDto.builder()
                .id(entity.getId())
                .companyId(company != null ? company.getId() : null)
                .companyCode(company != null ? company.getCode() : null)
                .companyDescription(company != null ? company.getDescription() : null)
                .moduleCode(entity.getModuleCode())
                .moduleDescription(module != null
                        ? (StringUtils.hasText(module.getDescription()) ? module.getDescription() : module.getName())
                        : null)
                .status(entity.getStatus() != null ? entity.getStatus().name() : CompanyModuleSubscriptionStatus.ACTIVE.name())
                .statusDescription(statusDescription(entity.getStatus()))
                .enabled(isEnabled(entity.getStatus()))
                .createdDate(entity.getCreatedDate())
                .lastModifiedDate(entity.getLastModifiedDate())
                .createdBy(entity.getCreatedBy())
                .lastModifiedBy(entity.getLastModifiedBy())
                .build();
    }

    private CompanyModuleListItemDto toListItem(CompanyModuleSubscription entity, ModuleDto module) {
        Company company = entity.getCompany();
        return CompanyModuleListItemDto.builder()
                .id(entity.getId())
                .companyCode(company != null ? company.getCode() : null)
                .companyDescription(company != null ? company.getDescription() : null)
                .moduleCode(entity.getModuleCode())
                .moduleDescription(module != null
                        ? (StringUtils.hasText(module.getDescription()) ? module.getDescription() : module.getName())
                        : null)
                .status(entity.getStatus() != null ? entity.getStatus().name() : CompanyModuleSubscriptionStatus.ACTIVE.name())
                .statusDescription(statusDescription(entity.getStatus()))
                .enabled(isEnabled(entity.getStatus()))
                .createdDate(entity.getCreatedDate())
                .lastModifiedDate(entity.getLastModifiedDate())
                .createdBy(entity.getCreatedBy())
                .lastModifiedBy(entity.getLastModifiedBy())
                .build();
    }

    private String resolveModuleDescription(String moduleCode, Map<String, ModuleDto> moduleMap) {
        if (!StringUtils.hasText(moduleCode) || moduleMap == null) {
            return null;
        }
        ModuleDto module = moduleMap.get(moduleCode.trim().toLowerCase(Locale.ROOT));
        if (module == null) {
            return null;
        }
        return StringUtils.hasText(module.getDescription()) ? module.getDescription() : module.getName();
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

    private boolean matchesStatus(String status, CompanyModuleSubscriptionStatus current) {
        if (!StringUtils.hasText(status)) {
            return true;
        }
        String value = current == CompanyModuleSubscriptionStatus.INACTIVE ? "inactive" : "active";
        return value.equals(status);
    }

    private Comparator<CompanyModuleListItemDto> resolveComparator(String sortColumn, String sortDirection) {
        String column = normalize(sortColumn);
        Comparator<CompanyModuleListItemDto> comparator;
        if ("companycode".equals(column)) {
            comparator = Comparator.comparing(CompanyModuleListItemDto::getCompanyCode, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
        } else if ("moduledescription".equals(column)) {
            comparator = Comparator.comparing(CompanyModuleListItemDto::getModuleDescription, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
        } else if ("modulecode".equals(column)) {
            comparator = Comparator.comparing(CompanyModuleListItemDto::getModuleCode, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
        } else if ("status".equals(column)) {
            comparator = Comparator.comparing(CompanyModuleListItemDto::getStatus, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
        } else if ("createddate".equals(column)) {
            comparator = Comparator.comparing(CompanyModuleListItemDto::getCreatedDate, Comparator.nullsLast(Comparator.naturalOrder()));
        } else if ("lastmodifieddate".equals(column)) {
            comparator = Comparator.comparing(CompanyModuleListItemDto::getLastModifiedDate, Comparator.nullsLast(Comparator.naturalOrder()));
        } else {
            comparator = Comparator.comparing(CompanyModuleListItemDto::getId, Comparator.nullsLast(Comparator.naturalOrder()));
        }

        if ("desc".equals(normalize(sortDirection))) {
            return comparator.reversed();
        }
        return comparator;
    }

    private CompanyModulePrivilegesDto resolvePrivileges(CompanyModuleReferenceDataRequest request) {
        String username = request != null ? request.getUsername() : null;
        if (!StringUtils.hasText(username)) {
            username = getAuthenticatedUsername();
        }

        if (!StringUtils.hasText(username)) {
            return CompanyModulePrivilegesDto.builder()
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
            return CompanyModulePrivilegesDto.builder()
                    .add(false)
                    .update(false)
                    .view(false)
                    .search(false)
                    .delete(false)
                    .build();
        }

        var access = authModuleClient.getRolePageTaskAccess(roleCode);
        if (access == null || access.getPages() == null) {
            return CompanyModulePrivilegesDto.builder()
                    .add(false)
                    .update(false)
                    .view(false)
                    .search(false)
                    .delete(false)
                    .build();
        }

        Map<String, Boolean> taskAccess = new HashMap<>();
        access.getPages().stream()
                .filter(page -> COMPANY_MODULE_PAGE_CODE.equalsIgnoreCase(page.getPageCode()))
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

        return CompanyModulePrivilegesDto.builder()
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
