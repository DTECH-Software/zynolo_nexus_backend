package com.zynolo_nexus.po_service.service.impl;

import com.zynolo_nexus.po_service.dto.request.DepartmentCreateRequest;
import com.zynolo_nexus.po_service.dto.request.DepartmentFilterRequest;
import com.zynolo_nexus.po_service.dto.request.DepartmentFilterSearch;
import com.zynolo_nexus.po_service.dto.request.DepartmentReferenceDataRequest;
import com.zynolo_nexus.po_service.dto.request.DepartmentStatusRequest;
import com.zynolo_nexus.po_service.dto.request.DepartmentUpdateRequest;
import com.zynolo_nexus.po_service.dto.request.DepartmentViewRequest;
import com.zynolo_nexus.po_service.dto.response.DepartmentDto;
import com.zynolo_nexus.po_service.dto.response.DepartmentFilterResultDto;
import com.zynolo_nexus.po_service.dto.response.DepartmentListItemDto;
import com.zynolo_nexus.po_service.dto.response.DepartmentPrivilegesDto;
import com.zynolo_nexus.po_service.dto.response.DepartmentReferenceDataDto;
import com.zynolo_nexus.po_service.dto.response.ReferenceOptionDto;
import com.zynolo_nexus.po_service.enums.MasterStatus;
import com.zynolo_nexus.po_service.exception.BadRequestException;
import com.zynolo_nexus.po_service.exception.ResourceNotFoundException;
import com.zynolo_nexus.po_service.model.Department;
import com.zynolo_nexus.po_service.repository.DepartmentRepository;
import com.zynolo_nexus.po_service.service.DepartmentService;
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
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;

    @Override
    public DepartmentReferenceDataDto getReferenceData(DepartmentReferenceDataRequest request) {
        return DepartmentReferenceDataDto.builder()
                .defaultStatus(List.of(
                        option(MasterStatus.ACTIVE.name(), "Active"),
                        option(MasterStatus.INACTIVE.name(), "Inactive")
                ))
                .privileges(DepartmentPrivilegesDto.builder()
                        .add(true)
                        .update(true)
                        .view(true)
                        .search(true)
                        .delete(false)
                        .build())
                .build();
    }

    @Override
    @Transactional
    public DepartmentDto create(DepartmentCreateRequest request) {
        String code = normalizeCode(request.getCode());
        if (departmentRepository.existsByCodeIgnoreCase(code)) {
            throw new BadRequestException("Department code already exists: " + code);
        }

        Department department = new Department();
        department.setCode(code);
        department.setDescription(trim(request.getDescription()));
        department.setStatus(MasterStatus.ACTIVE);
        applyAudit(department, request.getUsername(), true);

        return toDto(departmentRepository.save(department));
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentDto view(DepartmentViewRequest request) {
        return toDto(getById(request.getId()));
    }

    @Override
    @Transactional
    public DepartmentDto update(DepartmentUpdateRequest request) {
        Department department = getById(request.getId());
        String code = normalizeCode(request.getCode());
        boolean duplicateExists = departmentRepository.existsByCodeIgnoreCase(code)
                && !department.getCode().equalsIgnoreCase(code);
        if (duplicateExists) {
            throw new BadRequestException("Department code already exists: " + code);
        }

        department.setCode(code);
        department.setDescription(trim(request.getDescription()));
        applyAudit(department, request.getUsername(), false);

        return toDto(departmentRepository.save(department));
    }

    @Override
    @Transactional
    public DepartmentDto updateStatus(DepartmentStatusRequest request) {
        Department department = getById(request.getId());
        department.setStatus(parseStatus(request.getStatus()));
        applyAudit(department, request.getUsername(), false);

        return toDto(departmentRepository.save(department));
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentFilterResultDto filterList(DepartmentFilterRequest request) {
        Pageable pageable = PageRequest.of(
                request.getPage(),
                request.getSize(),
                Sort.by(resolveDirection(request.getSortDirection()), resolveSortColumn(request.getSortColumn()))
        );

        Page<Department> page = departmentRepository.findAll(buildSpecification(request.getSearch()), pageable);
        List<DepartmentListItemDto> content = page.getContent().stream()
                .map(this::toListItemDto)
                .toList();

        return DepartmentFilterResultDto.builder()
                .content(content)
                .size(content.size())
                .totalRecords(page.getTotalElements())
                .build();
    }

    private Department getById(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + id));
    }

    private Specification<Department> buildSpecification(DepartmentFilterSearch search) {
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

    private DepartmentDto toDto(Department department) {
        return DepartmentDto.builder()
                .id(department.getId())
                .code(department.getCode())
                .description(department.getDescription())
                .status(department.getStatus().name())
                .statusDescription(toStatusDescription(department.getStatus()))
                .createdDate(department.getCreatedDate())
                .lastModifiedDate(department.getLastModifiedDate())
                .createdBy(department.getCreatedBy())
                .lastModifiedBy(department.getLastModifiedBy())
                .build();
    }

    private DepartmentListItemDto toListItemDto(Department department) {
        return DepartmentListItemDto.builder()
                .id(department.getId())
                .code(department.getCode())
                .description(department.getDescription())
                .status(department.getStatus().name())
                .statusDescription(toStatusDescription(department.getStatus()))
                .createdDate(department.getCreatedDate())
                .lastModifiedDate(department.getLastModifiedDate())
                .createdBy(department.getCreatedBy())
                .lastModifiedBy(department.getLastModifiedBy())
                .build();
    }

    private void applyAudit(Department department, String username, boolean create) {
        LocalDateTime now = LocalDateTime.now();
        if (create) {
            department.setCreatedDate(now);
            department.setCreatedBy(username);
        }
        department.setLastModifiedDate(now);
        department.setLastModifiedBy(username);
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
