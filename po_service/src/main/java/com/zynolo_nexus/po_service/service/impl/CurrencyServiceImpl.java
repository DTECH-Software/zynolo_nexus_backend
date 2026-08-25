package com.zynolo_nexus.po_service.service.impl;

import com.zynolo_nexus.po_service.dto.request.CurrencyCreateRequest;
import com.zynolo_nexus.po_service.dto.request.CurrencyFilterRequest;
import com.zynolo_nexus.po_service.dto.request.CurrencyFilterSearch;
import com.zynolo_nexus.po_service.dto.request.CurrencyReferenceDataRequest;
import com.zynolo_nexus.po_service.dto.request.CurrencyStatusRequest;
import com.zynolo_nexus.po_service.dto.request.CurrencyUpdateRequest;
import com.zynolo_nexus.po_service.dto.request.CurrencyViewRequest;
import com.zynolo_nexus.po_service.dto.response.CurrencyDto;
import com.zynolo_nexus.po_service.dto.response.CurrencyFilterResultDto;
import com.zynolo_nexus.po_service.dto.response.CurrencyListItemDto;
import com.zynolo_nexus.po_service.dto.response.CurrencyPrivilegesDto;
import com.zynolo_nexus.po_service.dto.response.CurrencyReferenceDataDto;
import com.zynolo_nexus.po_service.dto.response.ReferenceOptionDto;
import com.zynolo_nexus.po_service.enums.MasterStatus;
import com.zynolo_nexus.po_service.exception.BadRequestException;
import com.zynolo_nexus.po_service.exception.ResourceNotFoundException;
import com.zynolo_nexus.po_service.model.Currency;
import com.zynolo_nexus.po_service.repository.CurrencyRepository;
import com.zynolo_nexus.po_service.service.CurrencyService;
import com.zynolo_nexus.po_service.service.support.PagePrivilegeResolver;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CurrencyServiceImpl implements CurrencyService {

    private static final String PAGE_CODE = "CURM";
    private static final Set<String> ISO_4217_CODES = java.util.Currency.getAvailableCurrencies().stream()
            .map(java.util.Currency::getCurrencyCode)
            .collect(Collectors.toUnmodifiableSet());

    private final CurrencyRepository currencyRepository;
    private final PagePrivilegeResolver pagePrivilegeResolver;

    @Override
    public CurrencyReferenceDataDto getReferenceData(CurrencyReferenceDataRequest request) {
        var privileges = pagePrivilegeResolver.resolve(request != null ? request.getUsername() : null, PAGE_CODE);
        return CurrencyReferenceDataDto.builder()
                .defaultStatus(List.of(
                        option(MasterStatus.ACTIVE.name(), "Active"),
                        option(MasterStatus.INACTIVE.name(), "Inactive")
                ))
                .privileges(CurrencyPrivilegesDto.builder()
                        .add(privileges.isAdd())
                        .update(privileges.isUpdate())
                        .view(privileges.isView())
                        .search(privileges.isSearch())
                        .delete(privileges.isDelete())
                        .build())
                .build();
    }

    @Override
    @Transactional
    public CurrencyDto create(CurrencyCreateRequest request) {
        String code = normalizeCode(request.getCode());
        if (currencyRepository.existsByCodeIgnoreCase(code)) {
            throw new BadRequestException("Currency code already exists: " + code);
        }

        Currency currency = new Currency();
        currency.setCode(code);
        currency.setDescription(trim(request.getDescription()));
        currency.setStatus(MasterStatus.ACTIVE);
        applyAudit(currency, request.getUsername(), true);

        return toDto(currencyRepository.save(currency));
    }

    @Override
    @Transactional(readOnly = true)
    public CurrencyDto view(CurrencyViewRequest request) {
        return toDto(getById(request.getId()));
    }

    @Override
    @Transactional
    public CurrencyDto update(CurrencyUpdateRequest request) {
        Currency currency = getById(request.getId());
        String code = normalizeCode(request.getCode());
        boolean duplicateExists = currencyRepository.existsByCodeIgnoreCase(code)
                && !currency.getCode().equalsIgnoreCase(code);
        if (duplicateExists) {
            throw new BadRequestException("Currency code already exists: " + code);
        }

        currency.setCode(code);
        currency.setDescription(trim(request.getDescription()));
        applyAudit(currency, request.getUsername(), false);

        return toDto(currencyRepository.save(currency));
    }

    @Override
    @Transactional
    public CurrencyDto updateStatus(CurrencyStatusRequest request) {
        Currency currency = getById(request.getId());
        currency.setStatus(parseStatus(request.getStatus()));
        applyAudit(currency, request.getUsername(), false);
        return toDto(currencyRepository.save(currency));
    }

    @Override
    @Transactional(readOnly = true)
    public CurrencyFilterResultDto filterList(CurrencyFilterRequest request) {
        Pageable pageable = PageRequest.of(
                request.getPage(),
                request.getSize(),
                Sort.by(resolveDirection(request.getSortDirection()), resolveSortColumn(request.getSortColumn()))
        );

        Page<Currency> page = currencyRepository.findAll(buildSpecification(request.getSearch()), pageable);
        List<CurrencyListItemDto> content = page.getContent().stream()
                .map(this::toListItemDto)
                .toList();

        return CurrencyFilterResultDto.builder()
                .content(content)
                .size(content.size())
                .totalRecords(page.getTotalElements())
                .build();
    }

    private Currency getById(Long id) {
        return currencyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Currency not found with ID: " + id));
    }

    private Specification<Currency> buildSpecification(CurrencyFilterSearch search) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (hasText(search.getCode())) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("code")), like(search.getCode())));
            }
            if (hasText(search.getDescription())) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), like(search.getDescription())));
            }
            if (hasText(search.getStatus())) {
                predicates.add(criteriaBuilder.equal(root.get("status"), parseStatus(search.getStatus())));
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private CurrencyDto toDto(Currency currency) {
        return CurrencyDto.builder()
                .id(currency.getId())
                .code(currency.getCode())
                .description(currency.getDescription())
                .status(currency.getStatus().name())
                .statusDescription(toStatusDescription(currency.getStatus()))
                .createdDate(currency.getCreatedDate())
                .lastModifiedDate(currency.getLastModifiedDate())
                .createdBy(currency.getCreatedBy())
                .lastModifiedBy(currency.getLastModifiedBy())
                .build();
    }

    private CurrencyListItemDto toListItemDto(Currency currency) {
        return CurrencyListItemDto.builder()
                .id(currency.getId())
                .code(currency.getCode())
                .description(currency.getDescription())
                .status(currency.getStatus().name())
                .statusDescription(toStatusDescription(currency.getStatus()))
                .createdDate(currency.getCreatedDate())
                .lastModifiedDate(currency.getLastModifiedDate())
                .createdBy(currency.getCreatedBy())
                .lastModifiedBy(currency.getLastModifiedBy())
                .build();
    }

    private void applyAudit(Currency currency, String username, boolean create) {
        LocalDateTime now = LocalDateTime.now();
        if (create) {
            currency.setCreatedDate(now);
            currency.setCreatedBy(username);
        }
        currency.setLastModifiedDate(now);
        currency.setLastModifiedBy(username);
    }

    private MasterStatus parseStatus(String value) {
        try {
            return MasterStatus.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (Exception ex) {
            throw new BadRequestException("Invalid status: " + value);
        }
    }

    private String resolveSortColumn(String sortColumn) {
        if (!hasText(sortColumn)) {
            return "lastModifiedDate";
        }
        return switch (sortColumn) {
            case "code", "description", "status", "createdDate", "lastModifiedDate", "createdBy" -> sortColumn;
            default -> "lastModifiedDate";
        };
    }

    private Sort.Direction resolveDirection(String direction) {
        return "ASC".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
    }

    private ReferenceOptionDto option(String code, String description) {
        return ReferenceOptionDto.builder()
                .code(code)
                .description(description)
                .build();
    }

    private String toStatusDescription(MasterStatus status) {
        return status == MasterStatus.ACTIVE ? "Active" : "Inactive";
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String normalizeCode(String code) {
        if (code == null || !code.matches("[A-Z]{3}")) {
            throw new BadRequestException("Currency code must contain exactly 3 uppercase letters");
        }
        if (!ISO_4217_CODES.contains(code)) {
            throw new BadRequestException("Invalid ISO 4217 currency code: " + code);
        }
        return code;
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private String like(String value) {
        return "%" + value.trim().toLowerCase(Locale.ROOT) + "%";
    }
}
