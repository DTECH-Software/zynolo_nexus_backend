package com.zynolo_nexus.cheque_service.service.impl;

import com.zynolo_nexus.cheque_service.client.AuthModuleClient;
import com.zynolo_nexus.cheque_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.cheque_service.dto.request.ChequeCustomerCreateRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeCustomerFilterRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeCustomerFilterSearch;
import com.zynolo_nexus.cheque_service.dto.request.ChequeCustomerReferenceDataRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeCustomerStatusUpdateRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeCustomerUpdateRequest;
import com.zynolo_nexus.cheque_service.dto.response.ChequeCustomerDto;
import com.zynolo_nexus.cheque_service.dto.response.ChequeCustomerFilterResultDto;
import com.zynolo_nexus.cheque_service.dto.response.ChequeCustomerListItemDto;
import com.zynolo_nexus.cheque_service.dto.response.ChequeCustomerPrivilegesDto;
import com.zynolo_nexus.cheque_service.dto.response.ChequeCustomerReferenceDataDto;
import com.zynolo_nexus.cheque_service.dto.response.ChequeReferenceStatusDto;
import com.zynolo_nexus.cheque_service.enums.ChequeCustomerStatus;
import com.zynolo_nexus.cheque_service.model.ChequeCustomer;
import com.zynolo_nexus.cheque_service.model.UserAccount;
import com.zynolo_nexus.cheque_service.repository.ChequeCustomerRepository;
import com.zynolo_nexus.cheque_service.repository.UserAccountRepository;
import com.zynolo_nexus.cheque_service.service.ChequeCustomerService;
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
public class ChequeCustomerServiceImpl implements ChequeCustomerService {

    private static final String PAGE_CODE = "CHCU";

    private final ChequeCustomerRepository chequeCustomerRepository;
    private final UserAccountRepository userAccountRepository;
    private final AuthModuleClient authModuleClient;

    @Override
    public MessageResponseDTO<ChequeCustomerDto> create(ChequeCustomerCreateRequest request) {
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
        if (chequeCustomerRepository.existsByCodeIgnoreCase(code)) {
            return error("Customer code already exists", 400);
        }

        String actor = normalizeUsername(request.getUsername());
        ChequeCustomer customer = ChequeCustomer.builder()
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
                .status(request.getStatus() != null ? request.getStatus() : ChequeCustomerStatus.ACTIVE)
                .createdBy(actor)
                .lastModifiedBy(actor)
                .build();

        customer = chequeCustomerRepository.save(customer);
        return success("Cheque customer created successfully", toDto(customer));
    }

    @Override
    public MessageResponseDTO<ChequeCustomerDto> update(ChequeCustomerUpdateRequest request) {
        if (request == null || request.getId() == null) {
            return error("Invalid update request", 400);
        }

        ChequeCustomer customer = chequeCustomerRepository.findById(request.getId()).orElse(null);
        if (customer == null) {
            return error("Cheque customer not found", 404);
        }

        if (StringUtils.hasText(request.getCode())) {
            String code = request.getCode().trim();
            if (chequeCustomerRepository.existsByCodeIgnoreCaseAndIdNot(code, customer.getId())) {
                return error("Customer code already exists", 400);
            }
            customer.setCode(code);
        }

        if (StringUtils.hasText(request.getDescription())) {
            customer.setDescription(request.getDescription().trim());
        }
        if (request.getStreet1() != null) {
            customer.setStreet1(trimToNull(request.getStreet1()));
        }
        if (request.getStreet2() != null) {
            customer.setStreet2(trimToNull(request.getStreet2()));
        }
        if (request.getCity() != null) {
            customer.setCity(trimToNull(request.getCity()));
        }
        if (request.getState() != null) {
            customer.setState(trimToNull(request.getState()));
        }
        if (request.getCountry() != null) {
            customer.setCountry(trimToNull(request.getCountry()));
        }
        if (request.getZipCode() != null) {
            customer.setZipCode(trimToNull(request.getZipCode()));
        }
        if (request.getContactNo() != null) {
            customer.setContactNo(trimToNull(request.getContactNo()));
        }
        if (request.getEmail() != null) {
            customer.setEmail(trimToNull(request.getEmail()));
        }
        if (request.getWebsite() != null) {
            customer.setWebsite(trimToNull(request.getWebsite()));
        }

        if (!StringUtils.hasText(customer.getStreet1())
                || !StringUtils.hasText(customer.getCity())
                || !StringUtils.hasText(customer.getState())
                || !StringUtils.hasText(customer.getCountry())
                || !StringUtils.hasText(customer.getZipCode())
                || !StringUtils.hasText(customer.getContactNo())
                || !StringUtils.hasText(customer.getEmail())) {
            return error("Missing mandatory customer details", 400);
        }

        if (request.getStatus() != null) {
            customer.setStatus(request.getStatus());
        }

        String actor = normalizeUsername(request.getUsername());
        if (StringUtils.hasText(actor)) {
            customer.setLastModifiedBy(actor);
            if (!StringUtils.hasText(customer.getCreatedBy())) {
                customer.setCreatedBy(actor);
            }
        }

        customer = chequeCustomerRepository.save(customer);
        return success("Cheque customer updated successfully", toDto(customer));
    }

    @Override
    public MessageResponseDTO<ChequeCustomerDto> view(Long id) {
        if (id == null) {
            return error("Invalid view request", 400);
        }

        ChequeCustomer customer = chequeCustomerRepository.findById(id).orElse(null);
        if (customer == null) {
            return error("Cheque customer not found", 404);
        }

        return success("Cheque customer found with ID: " + id, toDto(customer));
    }

    @Override
    public MessageResponseDTO<ChequeCustomerDto> updateStatus(ChequeCustomerStatusUpdateRequest request) {
        if (request == null || request.getId() == null || request.getStatus() == null) {
            return error("Invalid status update request", 400);
        }

        ChequeCustomer customer = chequeCustomerRepository.findById(request.getId()).orElse(null);
        if (customer == null) {
            return error("Cheque customer not found", 404);
        }

        customer.setStatus(request.getStatus());
        String actor = normalizeUsername(request.getUsername());
        if (StringUtils.hasText(actor)) {
            customer.setLastModifiedBy(actor);
            if (!StringUtils.hasText(customer.getCreatedBy())) {
                customer.setCreatedBy(actor);
            }
        }

        customer = chequeCustomerRepository.save(customer);
        return success("Cheque customer status updated successfully", toDto(customer));
    }

    @Override
    public MessageResponseDTO<ChequeCustomerFilterResultDto> filterList(ChequeCustomerFilterRequest request) {
        List<ChequeCustomer> customers = chequeCustomerRepository.findAll();

        ChequeCustomerFilterSearch search = request != null ? request.getSearch() : null;
        String code = normalize(search != null ? search.getCode() : null);
        String description = normalize(search != null ? search.getDescription() : null);
        String contactNo = normalize(search != null ? search.getContactNo() : null);
        String email = normalize(search != null ? search.getEmail() : null);
        String status = normalize(search != null ? search.getStatus() : null);

        List<ChequeCustomerListItemDto> filtered = customers.stream()
                .filter(customer -> matches(code, customer.getCode()))
                .filter(customer -> matches(description, customer.getDescription()))
                .filter(customer -> matches(contactNo, customer.getContactNo()))
                .filter(customer -> matches(email, customer.getEmail()))
                .filter(customer -> matchesStatus(status, customer.getStatus()))
                .map(this::toListItem)
                .toList();

        Comparator<ChequeCustomerListItemDto> comparator = resolveComparator(
                request != null ? request.getSortColumn() : null,
                request != null ? request.getSortDirection() : null
        );

        List<ChequeCustomerListItemDto> sorted = filtered.stream().sorted(comparator).toList();
        int requestedSize = request != null && request.getSize() != null && request.getSize() > 0 ? request.getSize() : 10;
        int page = request != null && request.getPage() != null && request.getPage() >= 0 ? request.getPage() : 0;

        int totalRecords = sorted.size();
        int fromIndex = Math.min(page * requestedSize, totalRecords);
        int toIndex = Math.min(fromIndex + requestedSize, totalRecords);
        List<ChequeCustomerListItemDto> content = sorted.subList(fromIndex, toIndex);
        int totalPages = requestedSize == 0 ? 1 : (int) Math.ceil((double) totalRecords / requestedSize);

        ChequeCustomerFilterResultDto result = ChequeCustomerFilterResultDto.builder()
                .content(content)
                .size(content.size())
                .totalRecords(totalRecords)
                .page(page)
                .totalPages(totalPages)
                .build();

        return MessageResponseDTO.<ChequeCustomerFilterResultDto>builder()
                .success(true)
                .message("Cheque customer list filtered successfully")
                .data(result)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    public MessageResponseDTO<ChequeCustomerReferenceDataDto> referenceData(ChequeCustomerReferenceDataRequest request) {
        String pageCode = StringUtils.hasText(request != null ? request.getPageCode() : null)
                ? request.getPageCode().trim().toUpperCase(Locale.ROOT)
                : PAGE_CODE;

        ChequeCustomerPrivilegesDto privileges = resolvePrivileges(request, pageCode);

        ChequeCustomerReferenceDataDto data = ChequeCustomerReferenceDataDto.builder()
                .defaultStatus(List.of(
                        ChequeReferenceStatusDto.builder().code("ACTIVE").description("Active").build(),
                        ChequeReferenceStatusDto.builder().code("INACTIVE").description("Inactive").build()
                ))
                .privileges(privileges)
                .build();

        return MessageResponseDTO.<ChequeCustomerReferenceDataDto>builder()
                .success(true)
                .message("Reference data " + pageCode + " retrieved successfully")
                .data(data)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    private ChequeCustomerPrivilegesDto resolvePrivileges(ChequeCustomerReferenceDataRequest request, String pageCode) {
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

            return ChequeCustomerPrivilegesDto.builder()
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

    private MessageResponseDTO<ChequeCustomerDto> success(String message, ChequeCustomerDto data) {
        return MessageResponseDTO.<ChequeCustomerDto>builder()
                .success(true)
                .message(message)
                .data(data)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    private MessageResponseDTO<ChequeCustomerDto> error(String message, int errorCode) {
        return MessageResponseDTO.<ChequeCustomerDto>builder()
                .success(false)
                .message(message)
                .data(null)
                .errors(null)
                .errorCode(errorCode)
                .responseTime(LocalDateTime.now())
                .build();
    }

    private ChequeCustomerDto toDto(ChequeCustomer customer) {
        ChequeCustomerStatus status = customer.getStatus() != null ? customer.getStatus() : ChequeCustomerStatus.ACTIVE;
        return ChequeCustomerDto.builder()
                .id(customer.getId())
                .code(customer.getCode())
                .description(customer.getDescription())
                .street1(customer.getStreet1())
                .street2(customer.getStreet2())
                .city(customer.getCity())
                .state(customer.getState())
                .country(customer.getCountry())
                .zipCode(customer.getZipCode())
                .contactNo(customer.getContactNo())
                .email(customer.getEmail())
                .website(customer.getWebsite())
                .status(status)
                .statusDescription(status == ChequeCustomerStatus.ACTIVE ? "Active" : "Inactive")
                .createdDate(customer.getCreatedDate())
                .lastModifiedDate(customer.getLastModifiedDate())
                .createdBy(customer.getCreatedBy())
                .lastModifiedBy(customer.getLastModifiedBy())
                .build();
    }

    private ChequeCustomerListItemDto toListItem(ChequeCustomer customer) {
        ChequeCustomerStatus status = customer.getStatus() != null ? customer.getStatus() : ChequeCustomerStatus.ACTIVE;
        return ChequeCustomerListItemDto.builder()
                .id(customer.getId())
                .code(customer.getCode())
                .description(customer.getDescription())
                .street1(customer.getStreet1())
                .street2(customer.getStreet2())
                .city(customer.getCity())
                .state(customer.getState())
                .country(customer.getCountry())
                .zipCode(customer.getZipCode())
                .contactNo(customer.getContactNo())
                .email(customer.getEmail())
                .website(customer.getWebsite())
                .status(status == ChequeCustomerStatus.ACTIVE ? "ACTIVE" : "INACTIVE")
                .statusDescription(status == ChequeCustomerStatus.ACTIVE ? "Active" : "Inactive")
                .createdDate(customer.getCreatedDate())
                .lastModifiedDate(customer.getLastModifiedDate())
                .createdBy(customer.getCreatedBy())
                .lastModifiedBy(customer.getLastModifiedBy())
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

    private ChequeCustomerPrivilegesDto emptyPrivileges() {
        return ChequeCustomerPrivilegesDto.builder()
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

    private boolean matchesStatus(String search, ChequeCustomerStatus status) {
        if (!StringUtils.hasText(search)) {
            return true;
        }
        String normalized = search.trim().toUpperCase(Locale.ROOT);
        if ("INACTIVE".equals(normalized)) {
            normalized = "DEACTIVE";
        }
        return status != null && status.name().equals(normalized);
    }

    private Comparator<ChequeCustomerListItemDto> resolveComparator(String sortColumn, String sortDirection) {
        String column = normalize(sortColumn);
        Comparator<ChequeCustomerListItemDto> comparator;
        if ("description".equals(column)) {
            comparator = Comparator.comparing(ChequeCustomerListItemDto::getDescription, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
        } else if ("status".equals(column)) {
            comparator = Comparator.comparing(ChequeCustomerListItemDto::getStatus, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
        } else if ("contactno".equals(column)) {
            comparator = Comparator.comparing(ChequeCustomerListItemDto::getContactNo, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
        } else if ("email".equals(column)) {
            comparator = Comparator.comparing(ChequeCustomerListItemDto::getEmail, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
        } else if ("createddate".equals(column)) {
            comparator = Comparator.comparing(ChequeCustomerListItemDto::getCreatedDate, Comparator.nullsLast(Comparator.naturalOrder()));
        } else if ("lastmodifieddate".equals(column)) {
            comparator = Comparator.comparing(ChequeCustomerListItemDto::getLastModifiedDate, Comparator.nullsLast(Comparator.naturalOrder()));
        } else {
            comparator = Comparator.comparing(ChequeCustomerListItemDto::getCode, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
        }

        String direction = normalize(sortDirection);
        return "desc".equals(direction) ? comparator.reversed() : comparator;
    }
}
