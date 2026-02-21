package com.zynolo_nexus.cheque_service.service.impl;

import com.zynolo_nexus.cheque_service.client.AuthModuleClient;
import com.zynolo_nexus.cheque_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.cheque_service.dto.request.ChequeSupplierCreateRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeSupplierFilterRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeSupplierFilterSearch;
import com.zynolo_nexus.cheque_service.dto.request.ChequeSupplierReferenceDataRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeSupplierStatusUpdateRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeSupplierUpdateRequest;
import com.zynolo_nexus.cheque_service.dto.response.ChequeSupplierDto;
import com.zynolo_nexus.cheque_service.dto.response.ChequeSupplierFilterResultDto;
import com.zynolo_nexus.cheque_service.dto.response.ChequeSupplierListItemDto;
import com.zynolo_nexus.cheque_service.dto.response.ChequeSupplierPrivilegesDto;
import com.zynolo_nexus.cheque_service.dto.response.ChequeSupplierReferenceDataDto;
import com.zynolo_nexus.cheque_service.dto.response.ChequeReferenceStatusDto;
import com.zynolo_nexus.cheque_service.enums.ChequeSupplierStatus;
import com.zynolo_nexus.cheque_service.model.ChequeSupplier;
import com.zynolo_nexus.cheque_service.model.UserAccount;
import com.zynolo_nexus.cheque_service.repository.ChequeSupplierRepository;
import com.zynolo_nexus.cheque_service.repository.UserAccountRepository;
import com.zynolo_nexus.cheque_service.service.ChequeSupplierService;
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
public class ChequeSupplierServiceImpl implements ChequeSupplierService {

    private static final String PAGE_CODE = "CHSU";

    private final ChequeSupplierRepository chequeSupplierRepository;
    private final UserAccountRepository userAccountRepository;
    private final AuthModuleClient authModuleClient;

    @Override
    public MessageResponseDTO<ChequeSupplierDto> create(ChequeSupplierCreateRequest request) {
        if (request == null
                || !StringUtils.hasText(request.getCode())
                || !StringUtils.hasText(request.getDescription())
                || !StringUtils.hasText(request.getStreet1())
                || !StringUtils.hasText(request.getCity())
                || !StringUtils.hasText(request.getState())
                || !StringUtils.hasText(request.getCountry())
                || !StringUtils.hasText(request.getZipCode())
                || !StringUtils.hasText(request.getContactNo())
                || !StringUtils.hasText(request.getEmail())) {
            return error("Invalid create request", 400);
        }

        String code = request.getCode().trim();
        if (chequeSupplierRepository.existsByCodeIgnoreCase(code)) {
            return error("Supplier code already exists", 400);
        }

        String actor = normalizeUsername(request.getUsername());
        ChequeSupplier supplier = ChequeSupplier.builder()
                .code(code)
                .description(request.getDescription().trim())
                .street1(request.getStreet1().trim())
                .street2(trimToNull(request.getStreet2()))
                .city(request.getCity().trim())
                .state(request.getState().trim())
                .country(request.getCountry().trim())
                .zipCode(request.getZipCode().trim())
                .contactNo(request.getContactNo().trim())
                .email(request.getEmail().trim())
                .website(trimToNull(request.getWebsite()))
                .status(request.getStatus() != null ? request.getStatus() : ChequeSupplierStatus.ACTIVE)
                .createdBy(actor)
                .lastModifiedBy(actor)
                .build();

        supplier = chequeSupplierRepository.save(supplier);
        return success("Cheque supplier created successfully", toDto(supplier));
    }

    @Override
    public MessageResponseDTO<ChequeSupplierDto> update(ChequeSupplierUpdateRequest request) {
        if (request == null || request.getId() == null) {
            return error("Invalid update request", 400);
        }

        ChequeSupplier supplier = chequeSupplierRepository.findById(request.getId()).orElse(null);
        if (supplier == null) {
            return error("Cheque supplier not found", 404);
        }

        if (StringUtils.hasText(request.getCode())) {
            String code = request.getCode().trim();
            if (chequeSupplierRepository.existsByCodeIgnoreCaseAndIdNot(code, supplier.getId())) {
                return error("Supplier code already exists", 400);
            }
            supplier.setCode(code);
        }

        if (StringUtils.hasText(request.getDescription())) {
            supplier.setDescription(request.getDescription().trim());
        }
        if (request.getStreet1() != null) {
            supplier.setStreet1(trimToNull(request.getStreet1()));
        }
        if (request.getStreet2() != null) {
            supplier.setStreet2(trimToNull(request.getStreet2()));
        }
        if (request.getCity() != null) {
            supplier.setCity(trimToNull(request.getCity()));
        }
        if (request.getState() != null) {
            supplier.setState(trimToNull(request.getState()));
        }
        if (request.getCountry() != null) {
            supplier.setCountry(trimToNull(request.getCountry()));
        }
        if (request.getZipCode() != null) {
            supplier.setZipCode(trimToNull(request.getZipCode()));
        }
        if (request.getContactNo() != null) {
            supplier.setContactNo(trimToNull(request.getContactNo()));
        }
        if (request.getEmail() != null) {
            supplier.setEmail(trimToNull(request.getEmail()));
        }
        if (request.getWebsite() != null) {
            supplier.setWebsite(trimToNull(request.getWebsite()));
        }

        if (!StringUtils.hasText(supplier.getStreet1())
                || !StringUtils.hasText(supplier.getCity())
                || !StringUtils.hasText(supplier.getState())
                || !StringUtils.hasText(supplier.getCountry())
                || !StringUtils.hasText(supplier.getZipCode())
                || !StringUtils.hasText(supplier.getContactNo())
                || !StringUtils.hasText(supplier.getEmail())) {
            return error("Missing mandatory supplier details", 400);
        }

        if (request.getStatus() != null) {
            supplier.setStatus(request.getStatus());
        }

        String actor = normalizeUsername(request.getUsername());
        if (StringUtils.hasText(actor)) {
            supplier.setLastModifiedBy(actor);
            if (!StringUtils.hasText(supplier.getCreatedBy())) {
                supplier.setCreatedBy(actor);
            }
        }

        supplier = chequeSupplierRepository.save(supplier);
        return success("Cheque supplier updated successfully", toDto(supplier));
    }

    @Override
    public MessageResponseDTO<ChequeSupplierDto> view(Long id) {
        if (id == null) {
            return error("Invalid view request", 400);
        }

        ChequeSupplier supplier = chequeSupplierRepository.findById(id).orElse(null);
        if (supplier == null) {
            return error("Cheque supplier not found", 404);
        }

        return success("Cheque supplier found with ID: " + id, toDto(supplier));
    }

    @Override
    public MessageResponseDTO<ChequeSupplierDto> updateStatus(ChequeSupplierStatusUpdateRequest request) {
        if (request == null || request.getId() == null || request.getStatus() == null) {
            return error("Invalid status update request", 400);
        }

        ChequeSupplier supplier = chequeSupplierRepository.findById(request.getId()).orElse(null);
        if (supplier == null) {
            return error("Cheque supplier not found", 404);
        }

        supplier.setStatus(request.getStatus());
        String actor = normalizeUsername(request.getUsername());
        if (StringUtils.hasText(actor)) {
            supplier.setLastModifiedBy(actor);
            if (!StringUtils.hasText(supplier.getCreatedBy())) {
                supplier.setCreatedBy(actor);
            }
        }

        supplier = chequeSupplierRepository.save(supplier);
        return success("Cheque supplier status updated successfully", toDto(supplier));
    }

    @Override
    public MessageResponseDTO<ChequeSupplierFilterResultDto> filterList(ChequeSupplierFilterRequest request) {
        List<ChequeSupplier> suppliers = chequeSupplierRepository.findAll();

        ChequeSupplierFilterSearch search = request != null ? request.getSearch() : null;
        String code = normalize(search != null ? search.getCode() : null);
        String description = normalize(search != null ? search.getDescription() : null);
        String contactNo = normalize(search != null ? search.getContactNo() : null);
        String email = normalize(search != null ? search.getEmail() : null);
        String status = normalize(search != null ? search.getStatus() : null);

        List<ChequeSupplierListItemDto> filtered = suppliers.stream()
                .filter(supplier -> matches(code, supplier.getCode()))
                .filter(supplier -> matches(description, supplier.getDescription()))
                .filter(supplier -> matches(contactNo, supplier.getContactNo()))
                .filter(supplier -> matches(email, supplier.getEmail()))
                .filter(supplier -> matchesStatus(status, supplier.getStatus()))
                .map(this::toListItem)
                .toList();

        Comparator<ChequeSupplierListItemDto> comparator = resolveComparator(
                request != null ? request.getSortColumn() : null,
                request != null ? request.getSortDirection() : null
        );

        List<ChequeSupplierListItemDto> sorted = filtered.stream().sorted(comparator).toList();
        int requestedSize = request != null && request.getSize() != null && request.getSize() > 0 ? request.getSize() : 10;
        int page = request != null && request.getPage() != null && request.getPage() >= 0 ? request.getPage() : 0;

        int totalRecords = sorted.size();
        int fromIndex = Math.min(page * requestedSize, totalRecords);
        int toIndex = Math.min(fromIndex + requestedSize, totalRecords);
        List<ChequeSupplierListItemDto> content = sorted.subList(fromIndex, toIndex);
        int totalPages = requestedSize == 0 ? 1 : (int) Math.ceil((double) totalRecords / requestedSize);

        ChequeSupplierFilterResultDto result = ChequeSupplierFilterResultDto.builder()
                .content(content)
                .size(content.size())
                .totalRecords(totalRecords)
                .page(page)
                .totalPages(totalPages)
                .build();

        return MessageResponseDTO.<ChequeSupplierFilterResultDto>builder()
                .success(true)
                .message("Cheque supplier list filtered successfully")
                .data(result)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    public MessageResponseDTO<ChequeSupplierReferenceDataDto> referenceData(ChequeSupplierReferenceDataRequest request) {
        String pageCode = StringUtils.hasText(request != null ? request.getPageCode() : null)
                ? request.getPageCode().trim().toUpperCase(Locale.ROOT)
                : PAGE_CODE;

        ChequeSupplierPrivilegesDto privileges = resolvePrivileges(request, pageCode);

        ChequeSupplierReferenceDataDto data = ChequeSupplierReferenceDataDto.builder()
                .defaultStatus(List.of(
                        ChequeReferenceStatusDto.builder().code("ACTIVE").description("Active").build(),
                        ChequeReferenceStatusDto.builder().code("INACTIVE").description("Inactive").build()
                ))
                .privileges(privileges)
                .build();

        return MessageResponseDTO.<ChequeSupplierReferenceDataDto>builder()
                .success(true)
                .message("Reference data " + pageCode + " retrieved successfully")
                .data(data)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    private ChequeSupplierPrivilegesDto resolvePrivileges(ChequeSupplierReferenceDataRequest request, String pageCode) {
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

            return ChequeSupplierPrivilegesDto.builder()
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

    private MessageResponseDTO<ChequeSupplierDto> success(String message, ChequeSupplierDto data) {
        return MessageResponseDTO.<ChequeSupplierDto>builder()
                .success(true)
                .message(message)
                .data(data)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    private MessageResponseDTO<ChequeSupplierDto> error(String message, int errorCode) {
        return MessageResponseDTO.<ChequeSupplierDto>builder()
                .success(false)
                .message(message)
                .data(null)
                .errors(null)
                .errorCode(errorCode)
                .responseTime(LocalDateTime.now())
                .build();
    }

    private ChequeSupplierDto toDto(ChequeSupplier supplier) {
        ChequeSupplierStatus status = supplier.getStatus() != null ? supplier.getStatus() : ChequeSupplierStatus.ACTIVE;
        return ChequeSupplierDto.builder()
                .id(supplier.getId())
                .code(supplier.getCode())
                .description(supplier.getDescription())
                .street1(supplier.getStreet1())
                .street2(supplier.getStreet2())
                .city(supplier.getCity())
                .state(supplier.getState())
                .country(supplier.getCountry())
                .zipCode(supplier.getZipCode())
                .contactNo(supplier.getContactNo())
                .email(supplier.getEmail())
                .website(supplier.getWebsite())
                .status(status)
                .statusDescription(status == ChequeSupplierStatus.ACTIVE ? "Active" : "Inactive")
                .createdDate(supplier.getCreatedDate())
                .lastModifiedDate(supplier.getLastModifiedDate())
                .createdBy(supplier.getCreatedBy())
                .lastModifiedBy(supplier.getLastModifiedBy())
                .build();
    }

    private ChequeSupplierListItemDto toListItem(ChequeSupplier supplier) {
        ChequeSupplierStatus status = supplier.getStatus() != null ? supplier.getStatus() : ChequeSupplierStatus.ACTIVE;
        return ChequeSupplierListItemDto.builder()
                .id(supplier.getId())
                .code(supplier.getCode())
                .description(supplier.getDescription())
                .street1(supplier.getStreet1())
                .street2(supplier.getStreet2())
                .city(supplier.getCity())
                .state(supplier.getState())
                .country(supplier.getCountry())
                .zipCode(supplier.getZipCode())
                .contactNo(supplier.getContactNo())
                .email(supplier.getEmail())
                .website(supplier.getWebsite())
                .status(status == ChequeSupplierStatus.ACTIVE ? "ACTIVE" : "INACTIVE")
                .statusDescription(status == ChequeSupplierStatus.ACTIVE ? "Active" : "Inactive")
                .createdDate(supplier.getCreatedDate())
                .lastModifiedDate(supplier.getLastModifiedDate())
                .createdBy(supplier.getCreatedBy())
                .lastModifiedBy(supplier.getLastModifiedBy())
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

    private ChequeSupplierPrivilegesDto emptyPrivileges() {
        return ChequeSupplierPrivilegesDto.builder()
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

    private boolean matchesStatus(String search, ChequeSupplierStatus status) {
        if (!StringUtils.hasText(search)) {
            return true;
        }
        String normalized = search.trim().toUpperCase(Locale.ROOT);
        if ("INACTIVE".equals(normalized)) {
            normalized = "DEACTIVE";
        }
        return status != null && status.name().equals(normalized);
    }

    private Comparator<ChequeSupplierListItemDto> resolveComparator(String sortColumn, String sortDirection) {
        String column = normalize(sortColumn);
        Comparator<ChequeSupplierListItemDto> comparator;
        if ("description".equals(column)) {
            comparator = Comparator.comparing(ChequeSupplierListItemDto::getDescription, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
        } else if ("status".equals(column)) {
            comparator = Comparator.comparing(ChequeSupplierListItemDto::getStatus, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
        } else if ("contactno".equals(column)) {
            comparator = Comparator.comparing(ChequeSupplierListItemDto::getContactNo, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
        } else if ("email".equals(column)) {
            comparator = Comparator.comparing(ChequeSupplierListItemDto::getEmail, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
        } else if ("createddate".equals(column)) {
            comparator = Comparator.comparing(ChequeSupplierListItemDto::getCreatedDate, Comparator.nullsLast(Comparator.naturalOrder()));
        } else if ("lastmodifieddate".equals(column)) {
            comparator = Comparator.comparing(ChequeSupplierListItemDto::getLastModifiedDate, Comparator.nullsLast(Comparator.naturalOrder()));
        } else {
            comparator = Comparator.comparing(ChequeSupplierListItemDto::getCode, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
        }

        String direction = normalize(sortDirection);
        return "desc".equals(direction) ? comparator.reversed() : comparator;
    }
}
