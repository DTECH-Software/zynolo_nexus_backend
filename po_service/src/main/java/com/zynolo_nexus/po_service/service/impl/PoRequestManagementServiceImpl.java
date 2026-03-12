package com.zynolo_nexus.po_service.service.impl;

import com.zynolo_nexus.po_service.dto.request.PoReferenceDataRequest;
import com.zynolo_nexus.po_service.dto.request.PoRequestFilterRequest;
import com.zynolo_nexus.po_service.dto.request.PoRequestFilterSearch;
import com.zynolo_nexus.po_service.dto.request.PoRequestViewRequest;
import com.zynolo_nexus.po_service.dto.response.PoRequestDto;
import com.zynolo_nexus.po_service.dto.response.PoRequestFilterResultDto;
import com.zynolo_nexus.po_service.dto.response.PoRequestItemDto;
import com.zynolo_nexus.po_service.dto.response.PoRequestListItemDto;
import com.zynolo_nexus.po_service.dto.response.PoRequestPrivilegesDto;
import com.zynolo_nexus.po_service.dto.response.PoRequestReferenceDataDto;
import com.zynolo_nexus.po_service.dto.response.ReferenceOptionDto;
import com.zynolo_nexus.po_service.enums.MasterStatus;
import com.zynolo_nexus.po_service.enums.PoRequestStatus;
import com.zynolo_nexus.po_service.exception.BadRequestException;
import com.zynolo_nexus.po_service.exception.ResourceNotFoundException;
import com.zynolo_nexus.po_service.model.Department;
import com.zynolo_nexus.po_service.model.PoRequest;
import com.zynolo_nexus.po_service.model.PoRequestItem;
import com.zynolo_nexus.po_service.repository.CompanyRepository;
import com.zynolo_nexus.po_service.repository.DepartmentRepository;
import com.zynolo_nexus.po_service.repository.PoRequestRepository;
import com.zynolo_nexus.po_service.repository.VendorRepository;
import com.zynolo_nexus.po_service.service.PoRequestManagementService;
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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PoRequestManagementServiceImpl implements PoRequestManagementService {

    private static final String PAGE_CODE = "PORM";
    private static final Map<String, String> REQUEST_TYPES = new LinkedHashMap<>();

    static {
        REQUEST_TYPES.put("GOODS", "Goods");
        REQUEST_TYPES.put("SERVICES", "Services");
        REQUEST_TYPES.put("COMBINED", "Combined");
    }

    private final CompanyRepository companyRepository;
    private final DepartmentRepository departmentRepository;
    private final VendorRepository vendorRepository;
    private final PoRequestRepository poRequestRepository;
    private final PagePrivilegeResolver pagePrivilegeResolver;

    @Override
    public PoRequestReferenceDataDto getReferenceData(PoReferenceDataRequest request) {
        var privileges = pagePrivilegeResolver.resolve(request != null ? request.getUsername() : null, PAGE_CODE);
        return PoRequestReferenceDataDto.builder()
                .companies(companyRepository.findAllByStatusOrderByCodeAsc(MasterStatus.ACTIVE).stream()
                        .map(company -> option(company.getCode(), company.getDescription()))
                        .toList())
                .departments(List.of())
                .costCenters(List.of())
                .vendors(vendorRepository.findAllByStatusOrderByCodeAsc("ACTIVE").stream()
                        .map(vendor -> option(vendor.getCode(), vendor.getDescription()))
                        .toList())
                .requestTypes(REQUEST_TYPES.entrySet().stream()
                        .map(entry -> option(entry.getKey(), entry.getValue()))
                        .toList())
                .currencies(List.of())
                .defaultStatus(List.of(
                        option(PoRequestStatus.DRAFT.name(), "Draft"),
                        option(PoRequestStatus.SUBMITTED.name(), "Submitted"),
                        option(PoRequestStatus.REJECTED.name(), "Rejected"),
                        option(PoRequestStatus.APPROVED.name(), "Approved")
                ))
                .privileges(PoRequestPrivilegesDto.builder()
                        .add(privileges.isAdd())
                        .update(privileges.isUpdate())
                        .view(privileges.isView())
                        .search(privileges.isSearch())
                        .submit(privileges.isSubmit())
                        .delete(privileges.isDelete())
                        .approve(privileges.isApprove())
                        .reject(privileges.isReject())
                        .build())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PoRequestDto view(PoRequestViewRequest request) {
        return toDto(getById(request.getId()));
    }

    @Override
    @Transactional(readOnly = true)
    public PoRequestFilterResultDto filterList(PoRequestFilterRequest request) {
        Pageable pageable = PageRequest.of(
                request.getPage(),
                request.getSize(),
                Sort.by(resolveDirection(request.getSortDirection()), resolveSortColumn(request.getSortColumn()))
        );

        Page<PoRequest> page = poRequestRepository.findAll(buildSpecification(request.getSearch()), pageable);
        List<PoRequestListItemDto> content = page.getContent().stream()
                .map(this::toListItemDto)
                .toList();

        return PoRequestFilterResultDto.builder()
                .content(content)
                .size(content.size())
                .totalRecords(page.getTotalElements())
                .build();
    }

    private PoRequest getById(Long id) {
        return poRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PO request not found with ID: " + id));
    }

    private Specification<PoRequest> buildSpecification(PoRequestFilterSearch search) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (hasText(search.getRequestNo())) {
                predicates.add(cb.like(cb.lower(root.get("requestNo")), like(search.getRequestNo())));
            }
            if (hasText(search.getCompanyCode())) {
                predicates.add(cb.like(cb.lower(root.get("companyCode")), like(search.getCompanyCode())));
            }
            if (hasText(search.getVendorCode())) {
                predicates.add(cb.like(cb.lower(root.get("vendorCode")), like(search.getVendorCode())));
            }
            if (hasText(search.getVendorName())) {
                predicates.add(cb.like(cb.lower(root.get("vendorName")), like(search.getVendorName())));
            }
            if (hasText(search.getRequestType())) {
                predicates.add(cb.like(cb.lower(root.get("requestType")), like(search.getRequestType())));
            }
            if (hasText(search.getRequestedBy())) {
                predicates.add(cb.like(cb.lower(root.get("createdBy")), like(search.getRequestedBy())));
            }
            if (hasText(search.getStatus())) {
                try {
                    predicates.add(cb.equal(root.get("status"), PoRequestStatus.valueOf(search.getStatus().trim().toUpperCase(Locale.ROOT))));
                } catch (IllegalArgumentException ex) {
                    throw new BadRequestException("Invalid status: " + search.getStatus());
                }
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private Sort.Direction resolveDirection(String direction) {
        return "ASC".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
    }

    private String resolveSortColumn(String sortColumn) {
        if (!hasText(sortColumn)) {
            return "lastModifiedDate";
        }
        return switch (sortColumn) {
            case "requestNo", "companyCode", "vendorCode", "requestType", "status",
                 "createdDate", "lastModifiedDate", "requiredDate", "totalAmount", "createdBy" -> sortColumn;
            default -> "lastModifiedDate";
        };
    }

    private PoRequestDto toDto(PoRequest poRequest) {
        Department department = resolveDepartmentForResponse(poRequest.getDepartment());
        return PoRequestDto.builder()
                .id(poRequest.getId())
                .requestNo(poRequest.getRequestNo())
                .companyCode(poRequest.getCompanyCode())
                .companyName(poRequest.getCompanyName())
                .requestType(poRequest.getRequestType())
                .departmentCode(department != null ? department.getCode() : null)
                .departmentDescription(poRequest.getDepartment())
                .costCenter(poRequest.getCostCenter())
                .currencyCode(poRequest.getCurrencyCode())
                .vendorCode(poRequest.getVendorCode())
                .vendorName(poRequest.getVendorName())
                .requiredDate(poRequest.getRequiredDate())
                .justification(poRequest.getJustification())
                .status(poRequest.getStatus().name())
                .statusDescription(toStatusDescription(poRequest.getStatus()))
                .totalAmount(poRequest.getTotalAmount())
                .submittedDate(poRequest.getSubmittedDate())
                .reviewedDate(poRequest.getReviewedDate())
                .reviewedBy(poRequest.getReviewedBy())
                .reviewRemark(poRequest.getReviewRemark())
                .createdDate(poRequest.getCreatedDate())
                .lastModifiedDate(poRequest.getLastModifiedDate())
                .createdBy(poRequest.getCreatedBy())
                .lastModifiedBy(poRequest.getLastModifiedBy())
                .items(poRequest.getItems().stream().map(this::toItemDto).toList())
                .build();
    }

    private Department resolveDepartmentForResponse(String departmentDescription) {
        if (!hasText(departmentDescription)) {
            return null;
        }
        return departmentRepository.findByDescriptionIgnoreCase(departmentDescription.trim()).orElse(null);
    }

    private PoRequestListItemDto toListItemDto(PoRequest poRequest) {
        return PoRequestListItemDto.builder()
                .id(poRequest.getId())
                .requestNo(poRequest.getRequestNo())
                .companyCode(poRequest.getCompanyCode())
                .companyName(poRequest.getCompanyName())
                .vendorCode(poRequest.getVendorCode())
                .vendorName(poRequest.getVendorName())
                .requestType(poRequest.getRequestType())
                .status(poRequest.getStatus().name())
                .statusDescription(toStatusDescription(poRequest.getStatus()))
                .totalAmount(poRequest.getTotalAmount())
                .requiredDate(poRequest.getRequiredDate())
                .createdDate(poRequest.getCreatedDate())
                .lastModifiedDate(poRequest.getLastModifiedDate())
                .createdBy(poRequest.getCreatedBy())
                .lastModifiedBy(poRequest.getLastModifiedBy())
                .build();
    }

    private PoRequestItemDto toItemDto(PoRequestItem item) {
        return PoRequestItemDto.builder()
                .id(item.getId())
                .itemCode(item.getItemCode())
                .itemDescription(item.getItemDescription())
                .uom(item.getUom())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .lineAmount(item.getLineAmount())
                .build();
    }

    private String toStatusDescription(PoRequestStatus status) {
        return switch (status) {
            case DRAFT -> "Draft";
            case SUBMITTED -> "Submitted";
            case APPROVED -> "Approved";
            case REJECTED -> "Rejected";
        };
    }

    private ReferenceOptionDto option(String code, String description) {
        return ReferenceOptionDto.builder()
                .code(code)
                .description(description)
                .build();
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String like(String value) {
        return "%" + value.trim().toLowerCase(Locale.ROOT) + "%";
    }
}
