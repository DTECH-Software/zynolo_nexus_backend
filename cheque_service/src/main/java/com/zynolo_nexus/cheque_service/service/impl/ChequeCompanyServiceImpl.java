package com.zynolo_nexus.cheque_service.service.impl;

import com.zynolo_nexus.cheque_service.client.AuthModuleClient;
import com.zynolo_nexus.cheque_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.cheque_service.dto.common.LogoDocumentDto;
import com.zynolo_nexus.cheque_service.dto.request.ChequeCompanyCreateRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeCompanyFilterRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeCompanyFilterSearch;
import com.zynolo_nexus.cheque_service.dto.request.ChequeCompanyReferenceDataRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeCompanyStatusUpdateRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeCompanyUpdateRequest;
import com.zynolo_nexus.cheque_service.dto.response.ChequeCompanyDto;
import com.zynolo_nexus.cheque_service.dto.response.ChequeCompanyFilterResultDto;
import com.zynolo_nexus.cheque_service.dto.response.ChequeCompanyListItemDto;
import com.zynolo_nexus.cheque_service.dto.response.ChequeCompanyPrivilegesDto;
import com.zynolo_nexus.cheque_service.dto.response.ChequeCompanyReferenceDataDto;
import com.zynolo_nexus.cheque_service.dto.response.ChequeReferenceStatusDto;
import com.zynolo_nexus.cheque_service.enums.ChequeCompanyStatus;
import com.zynolo_nexus.cheque_service.model.ChequeCompany;
import com.zynolo_nexus.cheque_service.model.UserAccount;
import com.zynolo_nexus.cheque_service.repository.ChequeCompanyRepository;
import com.zynolo_nexus.cheque_service.repository.UserAccountRepository;
import com.zynolo_nexus.cheque_service.service.ChequeCompanyService;
import lombok.RequiredArgsConstructor;
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
public class ChequeCompanyServiceImpl implements ChequeCompanyService {

    private static final String PAGE_CODE = "CHCM";

    private final ChequeCompanyRepository chequeCompanyRepository;
    private final UserAccountRepository userAccountRepository;
    private final AuthModuleClient authModuleClient;

    @Override
    public MessageResponseDTO<ChequeCompanyDto> create(ChequeCompanyCreateRequest request) {
        if (request == null
                || !StringUtils.hasText(request.getCode())
                || !StringUtils.hasText(request.getDescription())
                || !StringUtils.hasText(request.getStreet1())
                || !StringUtils.hasText(request.getCity())
                || !StringUtils.hasText(request.getState())
                || !StringUtils.hasText(request.getCountry())
                || !StringUtils.hasText(request.getZipCode())
                || !StringUtils.hasText(request.getPhoneNumber())) {
            return error("Invalid create request", 400);
        }

        String code = request.getCode().trim();
        if (chequeCompanyRepository.existsByCodeIgnoreCase(code)) {
            return error("Company code already exists", 400);
        }

        String actor = normalizeUsername(request.getUsername());
        ChequeCompany company = ChequeCompany.builder()
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
                .logoType(trimToNull(request.getLogo() != null ? request.getLogo().getType() : null))
                .logoFileName(trimToNull(request.getLogo() != null ? request.getLogo().getFileName() : null))
                .logoFileType(trimToNull(request.getLogo() != null ? request.getLogo().getFileType() : null))
                .logoDoc(trimToNull(request.getLogo() != null ? request.getLogo().getDoc() : null))
                .status(request.getStatus() != null ? request.getStatus() : ChequeCompanyStatus.ACTIVE)
                .createdBy(actor)
                .lastModifiedBy(actor)
                .build();

        company = chequeCompanyRepository.save(company);
        return success("Cheque company created successfully", toDto(company));
    }

    @Override
    public MessageResponseDTO<ChequeCompanyDto> update(ChequeCompanyUpdateRequest request) {
        if (request == null || request.getId() == null) {
            return error("Invalid update request", 400);
        }

        ChequeCompany company = chequeCompanyRepository.findById(request.getId()).orElse(null);
        if (company == null) {
            return error("Cheque company not found", 404);
        }

        if (StringUtils.hasText(request.getCode())) {
            String code = request.getCode().trim();
            if (chequeCompanyRepository.existsByCodeIgnoreCaseAndIdNot(code, company.getId())) {
                return error("Company code already exists", 400);
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
        if (request.getLogo() != null) {
            applyLogo(company, request.getLogo());
        }

        if (!StringUtils.hasText(company.getStreet1())
                || !StringUtils.hasText(company.getCity())
                || !StringUtils.hasText(company.getState())
                || !StringUtils.hasText(company.getCountry())
                || !StringUtils.hasText(company.getZipCode())
                || !StringUtils.hasText(company.getPhoneNumber())) {
            return error("Missing mandatory company details", 400);
        }

        if (request.getStatus() != null) {
            company.setStatus(request.getStatus());
        }

        String actor = normalizeUsername(request.getUsername());
        if (StringUtils.hasText(actor)) {
            company.setLastModifiedBy(actor);
            if (!StringUtils.hasText(company.getCreatedBy())) {
                company.setCreatedBy(actor);
            }
        }

        company = chequeCompanyRepository.save(company);
        return success("Cheque company updated successfully", toDto(company));
    }

    @Override
    public MessageResponseDTO<ChequeCompanyDto> view(Long id) {
        if (id == null) {
            return error("Invalid view request", 400);
        }

        ChequeCompany company = chequeCompanyRepository.findById(id).orElse(null);
        if (company == null) {
            return error("Cheque company not found", 404);
        }

        return success("Cheque company found with ID: " + id, toDto(company));
    }

    @Override
    public MessageResponseDTO<ChequeCompanyDto> updateStatus(ChequeCompanyStatusUpdateRequest request) {
        if (request == null || request.getId() == null || request.getStatus() == null) {
            return error("Invalid status update request", 400);
        }

        ChequeCompany company = chequeCompanyRepository.findById(request.getId()).orElse(null);
        if (company == null) {
            return error("Cheque company not found", 404);
        }

        company.setStatus(request.getStatus());
        String actor = normalizeUsername(request.getUsername());
        if (StringUtils.hasText(actor)) {
            company.setLastModifiedBy(actor);
            if (!StringUtils.hasText(company.getCreatedBy())) {
                company.setCreatedBy(actor);
            }
        }

        company = chequeCompanyRepository.save(company);
        return success("Cheque company status updated successfully", toDto(company));
    }

    @Override
    public MessageResponseDTO<ChequeCompanyFilterResultDto> filterList(ChequeCompanyFilterRequest request) {
        List<ChequeCompany> companies = chequeCompanyRepository.findAll();

        ChequeCompanyFilterSearch search = request != null ? request.getSearch() : null;
        String code = normalize(search != null ? search.getCode() : null);
        String description = normalize(search != null ? search.getDescription() : null);
        String status = normalize(search != null ? search.getStatus() : null);

        List<ChequeCompanyListItemDto> filtered = companies.stream()
                .filter(company -> matches(code, company.getCode()))
                .filter(company -> matches(description, company.getDescription()))
                .filter(company -> matchesStatus(status, company.getStatus()))
                .map(this::toListItem)
                .toList();

        Comparator<ChequeCompanyListItemDto> comparator = resolveComparator(
                request != null ? request.getSortColumn() : null,
                request != null ? request.getSortDirection() : null
        );

        List<ChequeCompanyListItemDto> sorted = filtered.stream().sorted(comparator).toList();
        int requestedSize = request != null && request.getSize() != null && request.getSize() > 0 ? request.getSize() : 10;
        int page = request != null && request.getPage() != null && request.getPage() >= 0 ? request.getPage() : 0;

        int totalRecords = sorted.size();
        int fromIndex = Math.min(page * requestedSize, totalRecords);
        int toIndex = Math.min(fromIndex + requestedSize, totalRecords);
        List<ChequeCompanyListItemDto> content = sorted.subList(fromIndex, toIndex);
        int totalPages = requestedSize == 0 ? 1 : (int) Math.ceil((double) totalRecords / requestedSize);

        ChequeCompanyFilterResultDto result = ChequeCompanyFilterResultDto.builder()
                .content(content)
                .size(content.size())
                .totalRecords(totalRecords)
                .page(page)
                .totalPages(totalPages)
                .build();

        return MessageResponseDTO.<ChequeCompanyFilterResultDto>builder()
                .success(true)
                .message("Cheque company list filtered successfully")
                .data(result)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    public MessageResponseDTO<ChequeCompanyReferenceDataDto> referenceData(ChequeCompanyReferenceDataRequest request) {
        String pageCode = StringUtils.hasText(request != null ? request.getPageCode() : null)
                ? request.getPageCode().trim().toUpperCase(Locale.ROOT)
                : PAGE_CODE;

        ChequeCompanyPrivilegesDto privileges = resolvePrivileges(request, pageCode);

        ChequeCompanyReferenceDataDto data = ChequeCompanyReferenceDataDto.builder()
                .defaultStatus(List.of(
                        ChequeReferenceStatusDto.builder().code("ACTIVE").description("Active").build(),
                        ChequeReferenceStatusDto.builder().code("INACTIVE").description("Inactive").build()
                ))
                .privileges(privileges)
                .build();

        return MessageResponseDTO.<ChequeCompanyReferenceDataDto>builder()
                .success(true)
                .message("Reference data " + pageCode + " retrieved successfully")
                .data(data)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    private ChequeCompanyPrivilegesDto resolvePrivileges(ChequeCompanyReferenceDataRequest request, String pageCode) {
        String username = request != null ? request.getUsername() : null;
        if (!StringUtils.hasText(username)) {
            return emptyPrivileges();
        }

        UserAccount user = userAccountRepository.findByUsername(username).orElse(null);
        if (user == null || user.getRole() == null || !StringUtils.hasText(user.getRole().getCode())) {
            return emptyPrivileges();
        }

        try {
            var access = authModuleClient.getRolePageTaskAccess(user.getRole().getCode());
            if (access == null || access.getPages() == null) {
                return emptyPrivileges();
            }

            Map<String, Boolean> taskAccess = new HashMap<>();
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

            return ChequeCompanyPrivilegesDto.builder()
                    .add(hasTask(taskAccess, "ADD", "CREATE", "NEW"))
                    .update(hasTask(taskAccess, "UPDATE", "EDIT"))
                    .view(hasTask(taskAccess, "VIEW", "READ"))
                    .search(hasTask(taskAccess, "SEARCH", "FILTER", "LIST"))
                    .delete(hasTask(taskAccess, "DELETE", "REMOVE", "DEACTIVATE"))
                    .build();
        } catch (Exception ex) {
            return emptyPrivileges();
        }
    }

    private MessageResponseDTO<ChequeCompanyDto> success(String message, ChequeCompanyDto data) {
        return MessageResponseDTO.<ChequeCompanyDto>builder()
                .success(true)
                .message(message)
                .data(data)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    private MessageResponseDTO<ChequeCompanyDto> error(String message, int errorCode) {
        return MessageResponseDTO.<ChequeCompanyDto>builder()
                .success(false)
                .message(message)
                .data(null)
                .errors(null)
                .errorCode(errorCode)
                .responseTime(LocalDateTime.now())
                .build();
    }

    private ChequeCompanyDto toDto(ChequeCompany company) {
        ChequeCompanyStatus status = company.getStatus() != null ? company.getStatus() : ChequeCompanyStatus.ACTIVE;
        return ChequeCompanyDto.builder()
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
                .logo(toLogoDto(company))
                .status(status)
                .statusDescription(status == ChequeCompanyStatus.ACTIVE ? "Active" : "Inactive")
                .createdDate(company.getCreatedDate())
                .lastModifiedDate(company.getLastModifiedDate())
                .createdBy(company.getCreatedBy())
                .lastModifiedBy(company.getLastModifiedBy())
                .build();
    }

    private ChequeCompanyListItemDto toListItem(ChequeCompany company) {
        ChequeCompanyStatus status = company.getStatus() != null ? company.getStatus() : ChequeCompanyStatus.ACTIVE;
        return ChequeCompanyListItemDto.builder()
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
                .logo(toLogoDto(company))
                .status(status.name())
                .statusDescription(status == ChequeCompanyStatus.ACTIVE ? "Active" : "Inactive")
                .createdDate(company.getCreatedDate())
                .lastModifiedDate(company.getLastModifiedDate())
                .createdBy(company.getCreatedBy())
                .lastModifiedBy(company.getLastModifiedBy())
                .build();
    }

    private void applyLogo(ChequeCompany company, LogoDocumentDto logo) {
        company.setLogoType(trimToNull(logo.getType()));
        company.setLogoFileName(trimToNull(logo.getFileName()));
        company.setLogoFileType(trimToNull(logo.getFileType()));
        company.setLogoDoc(trimToNull(logo.getDoc()));
    }

    private LogoDocumentDto toLogoDto(ChequeCompany company) {
        if (!StringUtils.hasText(company.getLogoType())
                && !StringUtils.hasText(company.getLogoFileName())
                && !StringUtils.hasText(company.getLogoFileType())
                && !StringUtils.hasText(company.getLogoDoc())) {
            return null;
        }
        return LogoDocumentDto.builder()
                .type(company.getLogoType())
                .fileName(company.getLogoFileName())
                .fileType(company.getLogoFileType())
                .doc(company.getLogoDoc())
                .build();
    }

    private String normalize(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeUsername(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
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

    private ChequeCompanyPrivilegesDto emptyPrivileges() {
        return ChequeCompanyPrivilegesDto.builder()
                .add(false)
                .update(false)
                .view(false)
                .search(false)
                .delete(false)
                .build();
    }

    private boolean matches(String search, String actual) {
        if (!StringUtils.hasText(search)) {
            return true;
        }
        return actual != null && actual.toLowerCase(Locale.ROOT).contains(search);
    }

    private boolean matchesStatus(String search, ChequeCompanyStatus status) {
        if (!StringUtils.hasText(search)) {
            return true;
        }
        String normalized = search.trim().toUpperCase(Locale.ROOT);
        if ("INACTIVE".equals(normalized)) {
            normalized = "DEACTIVE";
        }
        return status != null && status.name().equals(normalized);
    }

    private Comparator<ChequeCompanyListItemDto> resolveComparator(String sortColumn, String sortDirection) {
        String column = normalize(sortColumn);
        Comparator<ChequeCompanyListItemDto> comparator;
        if ("description".equals(column)) {
            comparator = Comparator.comparing(ChequeCompanyListItemDto::getDescription, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
        } else if ("status".equals(column)) {
            comparator = Comparator.comparing(ChequeCompanyListItemDto::getStatus, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
        } else if ("createddate".equals(column)) {
            comparator = Comparator.comparing(ChequeCompanyListItemDto::getCreatedDate, Comparator.nullsLast(Comparator.naturalOrder()));
        } else if ("lastmodifieddate".equals(column)) {
            comparator = Comparator.comparing(ChequeCompanyListItemDto::getLastModifiedDate, Comparator.nullsLast(Comparator.naturalOrder()));
        } else {
            comparator = Comparator.comparing(ChequeCompanyListItemDto::getCode, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
        }

        String direction = normalize(sortDirection);
        return "desc".equals(direction) ? comparator.reversed() : comparator;
    }
}
