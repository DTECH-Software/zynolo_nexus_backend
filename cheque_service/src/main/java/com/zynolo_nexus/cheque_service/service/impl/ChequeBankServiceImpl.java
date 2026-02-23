package com.zynolo_nexus.cheque_service.service.impl;

import com.zynolo_nexus.cheque_service.client.AuthModuleClient;
import com.zynolo_nexus.cheque_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.cheque_service.dto.request.ChequeBankCreateRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeBankFilterRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeBankFilterSearch;
import com.zynolo_nexus.cheque_service.dto.request.ChequeBankReferenceDataRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeBankStatusUpdateRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeBankUpdateRequest;
import com.zynolo_nexus.cheque_service.dto.response.ChequeBankDto;
import com.zynolo_nexus.cheque_service.dto.response.ChequeBankFilterResultDto;
import com.zynolo_nexus.cheque_service.dto.response.ChequeBankListItemDto;
import com.zynolo_nexus.cheque_service.dto.response.ChequeBankPrivilegesDto;
import com.zynolo_nexus.cheque_service.dto.response.ChequeBankReferenceDataDto;
import com.zynolo_nexus.cheque_service.dto.response.ChequeReferenceStatusDto;
import com.zynolo_nexus.cheque_service.enums.ChequeBankStatus;
import com.zynolo_nexus.cheque_service.model.ChequeBank;
import com.zynolo_nexus.cheque_service.model.UserAccount;
import com.zynolo_nexus.cheque_service.repository.ChequeBankRepository;
import com.zynolo_nexus.cheque_service.repository.UserAccountRepository;
import com.zynolo_nexus.cheque_service.service.ChequeBankService;
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
public class ChequeBankServiceImpl implements ChequeBankService {

    private static final String PAGE_CODE = "CHBM";

    private final ChequeBankRepository chequeBankRepository;
    private final UserAccountRepository userAccountRepository;
    private final AuthModuleClient authModuleClient;

    @Override
    public MessageResponseDTO<ChequeBankDto> create(ChequeBankCreateRequest request) {
        if (request == null
                || !StringUtils.hasText(request.getCode())
                || !StringUtils.hasText(request.getName())) {
            return error("Invalid create request", 400);
        }

        String code = request.getCode().trim();
        if (chequeBankRepository.existsByCodeIgnoreCase(code)) {
            return error("Bank code already exists", 400);
        }

        String actor = normalizeUsername(request.getUsername());
        ChequeBank bank = ChequeBank.builder()
                .code(code)
                .name(request.getName().trim())
                .status(request.getStatus() != null ? request.getStatus() : ChequeBankStatus.ACTIVE)
                .createdBy(actor)
                .lastModifiedBy(actor)
                .build();

        bank = chequeBankRepository.save(bank);
        return success("Cheque bank created successfully", toDto(bank));
    }

    @Override
    public MessageResponseDTO<ChequeBankDto> update(ChequeBankUpdateRequest request) {
        if (request == null || request.getId() == null) {
            return error("Invalid update request", 400);
        }

        ChequeBank bank = chequeBankRepository.findById(request.getId()).orElse(null);
        if (bank == null) {
            return error("Cheque bank not found", 404);
        }

        if (StringUtils.hasText(request.getCode())) {
            String code = request.getCode().trim();
            if (chequeBankRepository.existsByCodeIgnoreCaseAndIdNot(code, bank.getId())) {
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

        String actor = normalizeUsername(request.getUsername());
        if (StringUtils.hasText(actor)) {
            bank.setLastModifiedBy(actor);
            if (!StringUtils.hasText(bank.getCreatedBy())) {
                bank.setCreatedBy(actor);
            }
        }

        bank = chequeBankRepository.save(bank);
        return success("Cheque bank updated successfully", toDto(bank));
    }

    @Override
    public MessageResponseDTO<ChequeBankDto> view(Long id) {
        if (id == null) {
            return error("Invalid view request", 400);
        }

        ChequeBank bank = chequeBankRepository.findById(id).orElse(null);
        if (bank == null) {
            return error("Cheque bank not found", 404);
        }

        return success("Cheque bank found with ID: " + id, toDto(bank));
    }

    @Override
    public MessageResponseDTO<ChequeBankDto> updateStatus(ChequeBankStatusUpdateRequest request) {
        if (request == null || request.getId() == null || request.getStatus() == null) {
            return error("Invalid status update request", 400);
        }

        ChequeBank bank = chequeBankRepository.findById(request.getId()).orElse(null);
        if (bank == null) {
            return error("Cheque bank not found", 404);
        }

        bank.setStatus(request.getStatus());
        String actor = normalizeUsername(request.getUsername());
        if (StringUtils.hasText(actor)) {
            bank.setLastModifiedBy(actor);
            if (!StringUtils.hasText(bank.getCreatedBy())) {
                bank.setCreatedBy(actor);
            }
        }

        bank = chequeBankRepository.save(bank);
        return success("Cheque bank status updated successfully", toDto(bank));
    }

    @Override
    public MessageResponseDTO<ChequeBankFilterResultDto> filterList(ChequeBankFilterRequest request) {
        List<ChequeBank> banks = chequeBankRepository.findAll();

        ChequeBankFilterSearch search = request != null ? request.getSearch() : null;
        String code = normalize(search != null ? search.getCode() : null);
        String name = normalize(search != null ? search.getName() : null);
        String status = normalize(search != null ? search.getStatus() : null);

        List<ChequeBankListItemDto> filtered = banks.stream()
                .filter(bank -> matches(code, bank.getCode()))
                .filter(bank -> matches(name, bank.getName()))
                .filter(bank -> matchesStatus(status, bank.getStatus()))
                .map(this::toListItem)
                .toList();

        Comparator<ChequeBankListItemDto> comparator = resolveComparator(
                request != null ? request.getSortColumn() : null,
                request != null ? request.getSortDirection() : null
        );

        List<ChequeBankListItemDto> sorted = filtered.stream().sorted(comparator).toList();
        int requestedSize = request != null && request.getSize() != null && request.getSize() > 0 ? request.getSize() : 10;
        int page = request != null && request.getPage() != null && request.getPage() >= 0 ? request.getPage() : 0;

        int totalRecords = sorted.size();
        int fromIndex = Math.min(page * requestedSize, totalRecords);
        int toIndex = Math.min(fromIndex + requestedSize, totalRecords);
        List<ChequeBankListItemDto> content = sorted.subList(fromIndex, toIndex);
        int totalPages = requestedSize == 0 ? 1 : (int) Math.ceil((double) totalRecords / requestedSize);

        ChequeBankFilterResultDto result = ChequeBankFilterResultDto.builder()
                .content(content)
                .size(content.size())
                .totalRecords(totalRecords)
                .page(page)
                .totalPages(totalPages)
                .build();

        return MessageResponseDTO.<ChequeBankFilterResultDto>builder()
                .success(true)
                .message("Cheque bank list filtered successfully")
                .data(result)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    public MessageResponseDTO<ChequeBankReferenceDataDto> referenceData(ChequeBankReferenceDataRequest request) {
        String pageCode = StringUtils.hasText(request != null ? request.getPageCode() : null)
                ? request.getPageCode().trim().toUpperCase(Locale.ROOT)
                : PAGE_CODE;

        ChequeBankPrivilegesDto privileges = resolvePrivileges(request, pageCode);

        ChequeBankReferenceDataDto data = ChequeBankReferenceDataDto.builder()
                .defaultStatus(List.of(
                        ChequeReferenceStatusDto.builder().code("ACTIVE").description("Active").build(),
                        ChequeReferenceStatusDto.builder().code("INACTIVE").description("Inactive").build()
                ))
                .privileges(privileges)
                .build();

        return MessageResponseDTO.<ChequeBankReferenceDataDto>builder()
                .success(true)
                .message("Reference data " + pageCode + " retrieved successfully")
                .data(data)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    private ChequeBankPrivilegesDto resolvePrivileges(ChequeBankReferenceDataRequest request, String pageCode) {
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

            return ChequeBankPrivilegesDto.builder()
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

    private MessageResponseDTO<ChequeBankDto> success(String message, ChequeBankDto data) {
        return MessageResponseDTO.<ChequeBankDto>builder()
                .success(true)
                .message(message)
                .data(data)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    private MessageResponseDTO<ChequeBankDto> error(String message, int errorCode) {
        return MessageResponseDTO.<ChequeBankDto>builder()
                .success(false)
                .message(message)
                .data(null)
                .errors(null)
                .errorCode(errorCode)
                .responseTime(LocalDateTime.now())
                .build();
    }

    private ChequeBankDto toDto(ChequeBank bank) {
        ChequeBankStatus status = bank.getStatus() != null ? bank.getStatus() : ChequeBankStatus.ACTIVE;
        return ChequeBankDto.builder()
                .id(bank.getId())
                .code(bank.getCode())
                .name(bank.getName())
                .status(status)
                .statusDescription(status == ChequeBankStatus.ACTIVE ? "Active" : "Inactive")
                .createdDate(bank.getCreatedDate())
                .lastModifiedDate(bank.getLastModifiedDate())
                .createdBy(bank.getCreatedBy())
                .lastModifiedBy(bank.getLastModifiedBy())
                .build();
    }

    private ChequeBankListItemDto toListItem(ChequeBank bank) {
        ChequeBankStatus status = bank.getStatus() != null ? bank.getStatus() : ChequeBankStatus.ACTIVE;
        return ChequeBankListItemDto.builder()
                .id(bank.getId())
                .code(bank.getCode())
                .name(bank.getName())
                .status(status.name())
                .statusDescription(status == ChequeBankStatus.ACTIVE ? "Active" : "Inactive")
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

    private String normalizeUsername(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
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

    private ChequeBankPrivilegesDto emptyPrivileges() {
        return ChequeBankPrivilegesDto.builder()
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

    private boolean matchesStatus(String search, ChequeBankStatus status) {
        if (!StringUtils.hasText(search)) {
            return true;
        }
        String normalized = search.trim().toUpperCase(Locale.ROOT);
        if ("DEACTIVE".equals(normalized)) {
            normalized = "INACTIVE";
        }
        return status != null && status.name().equals(normalized);
    }

    private Comparator<ChequeBankListItemDto> resolveComparator(String sortColumn, String sortDirection) {
        String column = normalize(sortColumn);
        Comparator<ChequeBankListItemDto> comparator;
        if ("name".equals(column)) {
            comparator = Comparator.comparing(ChequeBankListItemDto::getName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
        } else if ("status".equals(column)) {
            comparator = Comparator.comparing(ChequeBankListItemDto::getStatus, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
        } else if ("createddate".equals(column)) {
            comparator = Comparator.comparing(ChequeBankListItemDto::getCreatedDate, Comparator.nullsLast(Comparator.naturalOrder()));
        } else if ("lastmodifieddate".equals(column)) {
            comparator = Comparator.comparing(ChequeBankListItemDto::getLastModifiedDate, Comparator.nullsLast(Comparator.naturalOrder()));
        } else {
            comparator = Comparator.comparing(ChequeBankListItemDto::getCode, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
        }

        String direction = normalize(sortDirection);
        return "desc".equals(direction) ? comparator.reversed() : comparator;
    }
}
