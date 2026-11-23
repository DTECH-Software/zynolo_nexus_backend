package com.zynolo_nexus.setting_service.service.impl;

import com.zynolo_nexus.setting_service.client.AuthModuleClient;
import com.zynolo_nexus.setting_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.setting_service.dto.request.BankCreateRequest;
import com.zynolo_nexus.setting_service.dto.request.BankFilterRequest;
import com.zynolo_nexus.setting_service.dto.request.BankFilterSearch;
import com.zynolo_nexus.setting_service.dto.request.BankReferenceDataRequest;
import com.zynolo_nexus.setting_service.dto.request.BankStatusUpdateRequest;
import com.zynolo_nexus.setting_service.dto.request.BankUpdateRequest;
import com.zynolo_nexus.setting_service.dto.response.BankDto;
import com.zynolo_nexus.setting_service.dto.response.BankFilterResultDto;
import com.zynolo_nexus.setting_service.dto.response.BankListItemDto;
import com.zynolo_nexus.setting_service.dto.response.BankPrivilegesDto;
import com.zynolo_nexus.setting_service.dto.response.BankReferenceDataDto;
import com.zynolo_nexus.setting_service.dto.response.ReferenceStatusDto;
import com.zynolo_nexus.setting_service.enums.BankStatus;
import com.zynolo_nexus.setting_service.model.Bank;
import com.zynolo_nexus.setting_service.model.User;
import com.zynolo_nexus.setting_service.repository.BankRepository;
import com.zynolo_nexus.setting_service.repository.UserRepository;
import com.zynolo_nexus.setting_service.service.BankService;
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
public class BankServiceImpl implements BankService {

    private static final String PAGE_CODE = "BANM";

    private final BankRepository bankRepository;
    private final UserRepository userRepository;
    private final AuthModuleClient authModuleClient;

    @Override
    public MessageResponseDTO<BankDto> create(BankCreateRequest request) {
        if (request == null
                || !StringUtils.hasText(request.getCode())
                || !StringUtils.hasText(request.getName())) {
            return error("Invalid create request", 400);
        }

        String code = request.getCode().trim();
        if (bankRepository.existsByCodeIgnoreCase(code)) {
            return error("Bank code already exists", 400);
        }

        Bank bank = Bank.builder()
                .code(code)
                .name(request.getName().trim())
                .status(request.getStatus() != null ? request.getStatus() : BankStatus.ACTIVE)
                .build();

        bank = bankRepository.save(bank);
        return success("Bank created successfully", toDto(bank));
    }

    @Override
    public MessageResponseDTO<BankDto> update(BankUpdateRequest request) {
        if (request == null || request.getId() == null) {
            return error("Invalid update request", 400);
        }

        Bank bank = bankRepository.findById(request.getId()).orElse(null);
        if (bank == null) {
            return error("Bank not found", 404);
        }

        if (StringUtils.hasText(request.getCode())) {
            String code = request.getCode().trim();
            if (bankRepository.existsByCodeIgnoreCaseAndIdNot(code, bank.getId())) {
                return error("Bank code already exists", 400);
            }
            bank.setCode(code);
        }

        if (StringUtils.hasText(request.getName())) {
            bank.setName(request.getName().trim());
        }

        if (!StringUtils.hasText(bank.getCode()) || !StringUtils.hasText(bank.getName())) {
            return error("Invalid bank details", 400);
        }

        if (request.getStatus() != null) {
            bank.setStatus(request.getStatus());
        }

        bank = bankRepository.save(bank);
        return success("Bank updated successfully", toDto(bank));
    }

    @Override
    public MessageResponseDTO<BankDto> view(Long id) {
        if (id == null) {
            return error("Invalid view request", 400);
        }

        Bank bank = bankRepository.findById(id).orElse(null);
        if (bank == null) {
            return error("Bank not found", 404);
        }

        return success("Bank found with ID: " + id, toDto(bank));
    }

    @Override
    public MessageResponseDTO<BankDto> updateStatus(BankStatusUpdateRequest request) {
        if (request == null || request.getId() == null || request.getStatus() == null) {
            return error("Invalid status update request", 400);
        }

        Bank bank = bankRepository.findById(request.getId()).orElse(null);
        if (bank == null) {
            return error("Bank not found", 404);
        }

        bank.setStatus(request.getStatus());
        bank = bankRepository.save(bank);

        return success("Bank status updated successfully", toDto(bank));
    }

    @Override
    public MessageResponseDTO<BankFilterResultDto> filterList(BankFilterRequest request) {
        List<Bank> banks = bankRepository.findAll();

        BankFilterSearch search = request != null ? request.getSearch() : null;
        String code = normalize(search != null ? search.getCode() : null);
        String name = normalize(search != null ? search.getName() : null);
        String status = normalize(search != null ? search.getStatus() : null);

        List<BankListItemDto> filtered = banks.stream()
                .filter(bank -> matches(code, bank.getCode()))
                .filter(bank -> matches(name, bank.getName()))
                .filter(bank -> matchesStatus(status, bank.getStatus()))
                .map(this::toListItem)
                .toList();

        Comparator<BankListItemDto> comparator = resolveComparator(
                request != null ? request.getSortColumn() : null,
                request != null ? request.getSortDirection() : null
        );

        List<BankListItemDto> sorted = filtered.stream().sorted(comparator).toList();
        int size = request != null && request.getSize() != null && request.getSize() > 0 ? request.getSize() : 10;
        int page = request != null && request.getPage() != null && request.getPage() >= 0 ? request.getPage() : 0;
        int totalRecords = sorted.size();
        int totalPages = size == 0 ? 1 : (int) Math.ceil((double) totalRecords / size);
        int fromIndex = Math.min(page * size, totalRecords);
        int toIndex = Math.min(fromIndex + size, totalRecords);
        List<BankListItemDto> content = sorted.subList(fromIndex, toIndex);

        BankFilterResultDto result = BankFilterResultDto.builder()
                .content(content)
                .size(content.size())
                .totalRecords(totalRecords)
                .page(page)
                .totalPages(totalPages)
                .build();

        return MessageResponseDTO.<BankFilterResultDto>builder()
                .success(true)
                .message("Bank list filtered successfully")
                .data(result)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    public MessageResponseDTO<BankReferenceDataDto> referenceData(BankReferenceDataRequest request) {
        BankPrivilegesDto privileges = resolvePrivileges(request);
        BankReferenceDataDto data = BankReferenceDataDto.builder()
                .defaultStatus(List.of(
                        ReferenceStatusDto.builder().code("ACTIVE").description("Active").build(),
                        ReferenceStatusDto.builder().code("INACTIVE").description("Inactive").build()
                ))
                .privileges(privileges)
                .build();

        return MessageResponseDTO.<BankReferenceDataDto>builder()
                .success(true)
                .message("Reference data " + PAGE_CODE + " retrieved successfully")
                .data(data)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    private MessageResponseDTO<BankDto> success(String message, BankDto data) {
        return MessageResponseDTO.<BankDto>builder()
                .success(true)
                .message(message)
                .data(data)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    private MessageResponseDTO<BankDto> error(String message, int errorCode) {
        return MessageResponseDTO.<BankDto>builder()
                .success(false)
                .message(message)
                .data(null)
                .errors(null)
                .errorCode(errorCode)
                .responseTime(LocalDateTime.now())
                .build();
    }

    private BankDto toDto(Bank bank) {
        BankStatus current = bank.getStatus() != null ? bank.getStatus() : BankStatus.ACTIVE;
        return BankDto.builder()
                .id(bank.getId())
                .code(bank.getCode())
                .name(bank.getName())
                .status(current)
                .statusDescription(current == BankStatus.INACTIVE ? "Inactive" : "Active")
                .createdDate(bank.getCreatedDate())
                .lastModifiedDate(bank.getLastModifiedDate())
                .createdBy(bank.getCreatedBy())
                .lastModifiedBy(bank.getLastModifiedBy())
                .build();
    }

    private BankListItemDto toListItem(Bank bank) {
        BankStatus current = bank.getStatus() != null ? bank.getStatus() : BankStatus.ACTIVE;
        return BankListItemDto.builder()
                .id(bank.getId())
                .code(bank.getCode())
                .name(bank.getName())
                .status(current.name())
                .statusDescription(current == BankStatus.INACTIVE ? "Inactive" : "Active")
                .createdDate(bank.getCreatedDate())
                .lastModifiedDate(bank.getLastModifiedDate())
                .createdBy(bank.getCreatedBy())
                .lastModifiedBy(bank.getLastModifiedBy())
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

    private boolean matchesStatus(String status, BankStatus bankStatus) {
        if (!StringUtils.hasText(status)) {
            return true;
        }
        String value = status.trim().toLowerCase(Locale.ROOT);
        return bankStatus != null && bankStatus.name().toLowerCase(Locale.ROOT).equals(value);
    }

    private Comparator<BankListItemDto> resolveComparator(String sortColumn, String sortDirection) {
        String column = normalize(sortColumn);
        Comparator<BankListItemDto> comparator;
        if ("name".equals(column)) {
            comparator = Comparator.comparing(BankListItemDto::getName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
        } else if ("status".equals(column)) {
            comparator = Comparator.comparing(BankListItemDto::getStatus, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
        } else if ("createddate".equals(column)) {
            comparator = Comparator.comparing(BankListItemDto::getCreatedDate, Comparator.nullsLast(Comparator.naturalOrder()));
        } else if ("lastmodifieddate".equals(column)) {
            comparator = Comparator.comparing(BankListItemDto::getLastModifiedDate, Comparator.nullsLast(Comparator.naturalOrder()));
        } else {
            comparator = Comparator.comparing(BankListItemDto::getCode, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
        }

        String dir = normalize(sortDirection);
        return "desc".equals(dir) ? comparator.reversed() : comparator;
    }

    private BankPrivilegesDto resolvePrivileges(BankReferenceDataRequest request) {
        String username = request != null ? request.getUsername() : null;
        if (!StringUtils.hasText(username)) {
            username = getAuthenticatedUsername();
        }
        if (!StringUtils.hasText(username)) {
            return emptyPrivileges();
        }

        User user = userRepository.findByUsername(username).orElse(null);
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
                    .filter(page -> PAGE_CODE.equalsIgnoreCase(page.getPageCode()))
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

            return BankPrivilegesDto.builder()
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

    private BankPrivilegesDto emptyPrivileges() {
        return BankPrivilegesDto.builder()
                .add(false)
                .update(false)
                .view(false)
                .search(false)
                .delete(false)
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
