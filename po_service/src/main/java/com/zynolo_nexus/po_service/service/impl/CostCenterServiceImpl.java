package com.zynolo_nexus.po_service.service.impl;

import com.zynolo_nexus.po_service.dto.request.CostCenterCreateRequest;
import com.zynolo_nexus.po_service.dto.request.CostCenterFilterRequest;
import com.zynolo_nexus.po_service.dto.request.CostCenterFilterSearch;
import com.zynolo_nexus.po_service.dto.request.CostCenterReferenceDataRequest;
import com.zynolo_nexus.po_service.dto.request.CostCenterStatusRequest;
import com.zynolo_nexus.po_service.dto.request.CostCenterUpdateRequest;
import com.zynolo_nexus.po_service.dto.request.CostCenterViewRequest;
import com.zynolo_nexus.po_service.dto.response.CostCenterDto;
import com.zynolo_nexus.po_service.dto.response.CostCenterFilterResultDto;
import com.zynolo_nexus.po_service.dto.response.CostCenterListItemDto;
import com.zynolo_nexus.po_service.dto.response.CostCenterPrivilegesDto;
import com.zynolo_nexus.po_service.dto.response.CostCenterReferenceDataDto;
import com.zynolo_nexus.po_service.dto.response.ReferenceOptionDto;
import com.zynolo_nexus.po_service.enums.MasterStatus;
import com.zynolo_nexus.po_service.exception.BadRequestException;
import com.zynolo_nexus.po_service.exception.ResourceNotFoundException;
import com.zynolo_nexus.po_service.model.CostCenter;
import com.zynolo_nexus.po_service.repository.CostCenterRepository;
import com.zynolo_nexus.po_service.service.CostCenterService;
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

@Service
@RequiredArgsConstructor
public class CostCenterServiceImpl implements CostCenterService {

    private static final String PAGE_CODE = "CCEM";

    private final CostCenterRepository costCenterRepository;
    private final PagePrivilegeResolver pagePrivilegeResolver;

    @Override
    public CostCenterReferenceDataDto getReferenceData(CostCenterReferenceDataRequest request) {
        var privileges = pagePrivilegeResolver.resolve(request != null ? request.getUsername() : null, PAGE_CODE);
        return CostCenterReferenceDataDto.builder()
                .defaultStatus(List.of(
                        option(MasterStatus.ACTIVE.name(), "Active"),
                        option(MasterStatus.INACTIVE.name(), "Inactive")
                ))
                .privileges(CostCenterPrivilegesDto.builder()
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
    public CostCenterDto create(CostCenterCreateRequest request) {
        String code = normalizeCode(request.getCode());
        if (costCenterRepository.existsByCodeIgnoreCase(code)) {
            throw new BadRequestException("Cost center code already exists: " + code);
        }

        CostCenter costCenter = new CostCenter();
        costCenter.setCode(code);
        costCenter.setDescription(trim(request.getDescription()));
        costCenter.setStatus(MasterStatus.ACTIVE);
        applyAudit(costCenter, request.getUsername(), true);

        return toDto(costCenterRepository.save(costCenter));
    }

    @Override
    @Transactional(readOnly = true)
    public CostCenterDto view(CostCenterViewRequest request) {
        return toDto(getById(request.getId()));
    }

    @Override
    @Transactional
    public CostCenterDto update(CostCenterUpdateRequest request) {
        CostCenter costCenter = getById(request.getId());
        String code = normalizeCode(request.getCode());
        boolean duplicateExists = costCenterRepository.existsByCodeIgnoreCase(code)
                && !costCenter.getCode().equalsIgnoreCase(code);
        if (duplicateExists) {
            throw new BadRequestException("Cost center code already exists: " + code);
        }

        costCenter.setCode(code);
        costCenter.setDescription(trim(request.getDescription()));
        if (hasText(request.getStatus())) {
            costCenter.setStatus(parseStatus(request.getStatus()));
        }
        applyAudit(costCenter, request.getUsername(), false);

        return toDto(costCenterRepository.save(costCenter));
    }

    @Override
    @Transactional
    public CostCenterDto updateStatus(CostCenterStatusRequest request) {
        CostCenter costCenter = getById(request.getId());
        costCenter.setStatus(parseStatus(request.getStatus()));
        applyAudit(costCenter, request.getUsername(), false);

        return toDto(costCenterRepository.save(costCenter));
    }

    @Override
    @Transactional(readOnly = true)
    public CostCenterFilterResultDto filterList(CostCenterFilterRequest request) {
        Pageable pageable = PageRequest.of(
                request.getPage(),
                request.getSize(),
                Sort.by(resolveDirection(request.getSortDirection()), resolveSortColumn(request.getSortColumn()))
        );

        Page<CostCenter> page = costCenterRepository.findAll(buildSpecification(request.getSearch()), pageable);
        List<CostCenterListItemDto> content = page.getContent().stream()
                .map(this::toListItemDto)
                .toList();

        return CostCenterFilterResultDto.builder()
                .content(content)
                .size(content.size())
                .totalRecords(page.getTotalElements())
                .build();
    }

    private CostCenter getById(Long id) {
        return costCenterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cost center not found with ID: " + id));
    }

    private Specification<CostCenter> buildSpecification(CostCenterFilterSearch search) {
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

    private CostCenterDto toDto(CostCenter costCenter) {
        return CostCenterDto.builder()
                .id(costCenter.getId())
                .code(costCenter.getCode())
                .description(costCenter.getDescription())
                .status(costCenter.getStatus().name())
                .statusDescription(toStatusDescription(costCenter.getStatus()))
                .createdDate(costCenter.getCreatedDate())
                .lastModifiedDate(costCenter.getLastModifiedDate())
                .createdBy(costCenter.getCreatedBy())
                .lastModifiedBy(costCenter.getLastModifiedBy())
                .build();
    }

    private CostCenterListItemDto toListItemDto(CostCenter costCenter) {
        return CostCenterListItemDto.builder()
                .id(costCenter.getId())
                .code(costCenter.getCode())
                .description(costCenter.getDescription())
                .status(costCenter.getStatus().name())
                .statusDescription(toStatusDescription(costCenter.getStatus()))
                .createdDate(costCenter.getCreatedDate())
                .lastModifiedDate(costCenter.getLastModifiedDate())
                .createdBy(costCenter.getCreatedBy())
                .lastModifiedBy(costCenter.getLastModifiedBy())
                .build();
    }

    private void applyAudit(CostCenter costCenter, String username, boolean create) {
        LocalDateTime now = LocalDateTime.now();
        if (create) {
            costCenter.setCreatedDate(now);
            costCenter.setCreatedBy(username);
        }
        costCenter.setLastModifiedDate(now);
        costCenter.setLastModifiedBy(username);
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
        return code.trim().toUpperCase(Locale.ROOT);
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private String like(String value) {
        return "%" + value.trim().toLowerCase(Locale.ROOT) + "%";
    }
}
