package com.zynolo_nexus.po_service.service.impl;

import com.zynolo_nexus.po_service.dto.request.PoReferenceDataRequest;
import com.zynolo_nexus.po_service.dto.request.PoRequestCreateRequest;
import com.zynolo_nexus.po_service.dto.request.PoRequestFilterRequest;
import com.zynolo_nexus.po_service.dto.request.PoRequestFilterSearch;
import com.zynolo_nexus.po_service.dto.request.PoRequestItemRequest;
import com.zynolo_nexus.po_service.dto.request.PoRequestSubmitRequest;
import com.zynolo_nexus.po_service.dto.request.PoRequestUpdateRequest;
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
import com.zynolo_nexus.po_service.model.Company;
import com.zynolo_nexus.po_service.model.CostCenter;
import com.zynolo_nexus.po_service.model.Currency;
import com.zynolo_nexus.po_service.model.Department;
import com.zynolo_nexus.po_service.model.PoRequest;
import com.zynolo_nexus.po_service.model.PoRequestItem;
import com.zynolo_nexus.po_service.model.Vendor;
import com.zynolo_nexus.po_service.repository.CompanyRepository;
import com.zynolo_nexus.po_service.repository.CostCenterRepository;
import com.zynolo_nexus.po_service.repository.CurrencyRepository;
import com.zynolo_nexus.po_service.repository.DepartmentRepository;
import com.zynolo_nexus.po_service.repository.PoRequestRepository;
import com.zynolo_nexus.po_service.repository.VendorRepository;
import com.zynolo_nexus.po_service.service.PoRequestService;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class PoRequestServiceImpl implements PoRequestService {

    private static final String PAGE_CODE = "PORC";
    private static final DateTimeFormatter REQUEST_NO_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final Map<String, String> REQUEST_TYPES = new LinkedHashMap<>();

    static {
        REQUEST_TYPES.put("GOODS", "Goods");
        REQUEST_TYPES.put("SERVICES", "Services");
        REQUEST_TYPES.put("COMBINED", "Combined");
    }

    private final CompanyRepository companyRepository;
    private final CostCenterRepository costCenterRepository;
    private final CurrencyRepository currencyRepository;
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
                .departments(departmentRepository.findAllByStatusOrderByCodeAsc(MasterStatus.ACTIVE).stream()
                        .map(department -> option(department.getCode(), department.getDescription()))
                        .toList())
                .costCenters(costCenterRepository.findAllByStatusOrderByCodeAsc(MasterStatus.ACTIVE).stream()
                        .map(costCenter -> option(costCenter.getCode(), costCenter.getDescription()))
                        .toList())
                .vendors(vendorRepository.findAllByStatusOrderByCodeAsc("ACTIVE").stream()
                        .map(vendor -> option(vendor.getCode(), vendor.getDescription()))
                        .toList())
                .requestTypes(REQUEST_TYPES.entrySet().stream()
                        .map(entry -> option(entry.getKey(), entry.getValue()))
                        .toList())
                .currencies(currencyRepository.findAllByStatusOrderByCodeAsc(MasterStatus.ACTIVE).stream()
                        .map(currency -> option(currency.getCode(), currency.getDescription()))
                        .toList())
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
                        .build())
                .build();
    }

    @Override
    @Transactional
    public PoRequestDto create(PoRequestCreateRequest request) {
        PoRequest poRequest = new PoRequest();
        poRequest.setRequestNo(generateRequestNo());
        poRequest.setStatus(PoRequestStatus.DRAFT);
        populateRequest(poRequest, request.getCompanyCode(), request.getCompanyName(), request.getRequestType(),
                request.getDepartment(), request.getCostCenter(), request.getCurrencyCode(), request.getVendorCode(),
                request.getVendorName(), request.getRequiredDate(), request.getJustification(), request.getItems());
        applyAudit(poRequest, request.getUsername(), true);

        return toDto(poRequestRepository.save(poRequest));
    }

    @Override
    @Transactional(readOnly = true)
    public PoRequestDto view(PoRequestViewRequest request) {
        return toDto(getById(request.getId()));
    }

    @Override
    @Transactional
    public PoRequestDto update(PoRequestUpdateRequest request) {
        PoRequest poRequest = getById(request.getId());
        validateEditable(poRequest.getStatus());

        populateRequest(poRequest, request.getCompanyCode(), request.getCompanyName(), request.getRequestType(),
                request.getDepartment(), request.getCostCenter(), request.getCurrencyCode(), request.getVendorCode(),
                request.getVendorName(), request.getRequiredDate(), request.getJustification(), request.getItems());
        applyAudit(poRequest, request.getUsername(), false);

        return toDto(poRequestRepository.save(poRequest));
    }

    @Override
    @Transactional
    public PoRequestDto submit(PoRequestSubmitRequest request) {
        PoRequest poRequest = getById(request.getId());
        validateEditable(poRequest.getStatus());

        poRequest.setStatus(PoRequestStatus.SUBMITTED);
        poRequest.setSubmittedDate(LocalDateTime.now());
        applyAudit(poRequest, request.getUsername(), false);

        return toDto(poRequestRepository.save(poRequest));
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

    private void validateEditable(PoRequestStatus status) {
        if (!(status == PoRequestStatus.DRAFT || status == PoRequestStatus.REJECTED)) {
            throw new BadRequestException("Only DRAFT or REJECTED requests can be modified");
        }
    }

    private void populateRequest(PoRequest poRequest,
                                 String companyCode,
                                 String companyName,
                                 String requestType,
                                 String departmentValue,
                                 String costCenter,
                                 String currencyCode,
                                 String vendorCode,
                                 String vendorName,
                                 LocalDate requiredDate,
                                 String justification,
                                 List<PoRequestItemRequest> items) {
        Company company = companyRepository.findByCodeIgnoreCaseAndStatus(companyCode, MasterStatus.ACTIVE)
                .orElseThrow(() -> new BadRequestException("Active company not found for code: " + companyCode));
        Currency currency = currencyRepository.findByCodeIgnoreCaseAndStatus(currencyCode, MasterStatus.ACTIVE)
                .orElseThrow(() -> new BadRequestException("Active currency not found for code: " + currencyCode));
        Department resolvedDepartment = resolveDepartment(departmentValue);
        CostCenter resolvedCostCenter = resolveCostCenter(costCenter);
        Vendor resolvedVendor = resolveVendor(vendorCode);

        poRequest.setCompanyCode(company.getCode());
        poRequest.setCompanyName(company.getDescription());
        poRequest.setRequestType(parseRequestType(requestType));
        poRequest.setDepartment(resolvedDepartment != null ? resolvedDepartment.getDescription() : null);
        poRequest.setCostCenter(resolvedCostCenter != null ? resolvedCostCenter.getCode() : null);
        poRequest.setCurrencyCode(currency.getCode());
        poRequest.setVendorCode(resolvedVendor != null ? resolvedVendor.getCode() : null);
        poRequest.setVendorName(resolvedVendor != null ? resolvedVendor.getDescription() : null);
        poRequest.setRequiredDate(requiredDate);
        poRequest.setJustification(trim(justification));

        poRequest.clearItems();

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (PoRequestItemRequest itemRequest : items) {
            PoRequestItem item = new PoRequestItem();
            item.setItemCode(trim(itemRequest.getItemCode()));
            item.setItemDescription(trim(itemRequest.getItemDescription()));
            item.setUom(trim(itemRequest.getUom()));
            item.setQuantity(itemRequest.getQuantity());
            item.setUnitPrice(itemRequest.getUnitPrice());
            item.setLineAmount(itemRequest.getQuantity().multiply(itemRequest.getUnitPrice()));
            totalAmount = totalAmount.add(item.getLineAmount());
            poRequest.addItem(item);
        }

        poRequest.setTotalAmount(totalAmount);
    }

    private void applyAudit(PoRequest poRequest, String username, boolean create) {
        LocalDateTime now = LocalDateTime.now();
        if (create) {
            poRequest.setCreatedDate(now);
            poRequest.setCreatedBy(username);
        }
        poRequest.setLastModifiedDate(now);
        poRequest.setLastModifiedBy(username);
    }

    private Specification<PoRequest> buildSpecification(PoRequestFilterSearch search) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (hasText(search.getRequestNo())) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("requestNo")), like(search.getRequestNo())));
            }
            if (hasText(search.getCompanyCode())) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("companyCode")), like(search.getCompanyCode())));
            }
            if (hasText(search.getVendorCode())) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("vendorCode")), like(search.getVendorCode())));
            }
            if (hasText(search.getVendorName())) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("vendorName")), like(search.getVendorName())));
            }
            if (hasText(search.getRequestType())) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("requestType")), like(search.getRequestType())));
            }
            if (hasText(search.getRequestedBy())) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("createdBy")), like(search.getRequestedBy())));
            }
            if (hasText(search.getStatus())) {
                try {
                    predicates.add(criteriaBuilder.equal(root.get("status"), PoRequestStatus.valueOf(search.getStatus().trim().toUpperCase(Locale.ROOT))));
                } catch (IllegalArgumentException ex) {
                    throw new BadRequestException("Invalid status: " + search.getStatus());
                }
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private String generateRequestNo() {
        return "POR-" + LocalDateTime.now().format(REQUEST_NO_FORMAT) + "-"
                + ThreadLocalRandom.current().nextInt(1000, 9999);
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
        return PoRequestDto.builder()
                .id(poRequest.getId())
                .requestNo(poRequest.getRequestNo())
                .companyCode(poRequest.getCompanyCode())
                .companyName(poRequest.getCompanyName())
                .requestType(poRequest.getRequestType())
                .department(poRequest.getDepartment())
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
                .createdDate(poRequest.getCreatedDate())
                .lastModifiedDate(poRequest.getLastModifiedDate())
                .createdBy(poRequest.getCreatedBy())
                .lastModifiedBy(poRequest.getLastModifiedBy())
                .items(poRequest.getItems().stream().map(this::toItemDto).toList())
                .build();
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

    private ReferenceOptionDto option(String code, String description) {
        return ReferenceOptionDto.builder()
                .code(code)
                .description(description)
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

    private String parseRequestType(String requestType) {
        if (!hasText(requestType)) {
            throw new BadRequestException("requestType is required");
        }
        String normalized = requestType.trim().toUpperCase(Locale.ROOT);
        if (!REQUEST_TYPES.containsKey(normalized)) {
            throw new BadRequestException("Invalid request type: " + requestType);
        }
        return normalized;
    }

    private Department resolveDepartment(String departmentValue) {
        if (!hasText(departmentValue)) {
            return null;
        }

        return departmentRepository.findByCodeIgnoreCaseAndStatus(departmentValue, MasterStatus.ACTIVE)
                .or(() -> departmentRepository.findByDescriptionIgnoreCaseAndStatus(departmentValue, MasterStatus.ACTIVE))
                .orElseThrow(() -> new BadRequestException("Active department not found for value: " + departmentValue));
    }

    private CostCenter resolveCostCenter(String costCenterValue) {
        if (!hasText(costCenterValue)) {
            return null;
        }

        return costCenterRepository.findByCodeIgnoreCaseAndStatus(costCenterValue, MasterStatus.ACTIVE)
                .or(() -> costCenterRepository.findByDescriptionIgnoreCaseAndStatus(costCenterValue, MasterStatus.ACTIVE))
                .orElseThrow(() -> new BadRequestException("Active cost center not found for value: " + costCenterValue));
    }

    private Vendor resolveVendor(String vendorCode) {
        if (!hasText(vendorCode)) {
            return null;
        }
        return vendorRepository.findByCodeIgnoreCaseAndStatus(vendorCode, "ACTIVE")
                .orElseThrow(() -> new BadRequestException("Active vendor not found for code: " + vendorCode));
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String like(String value) {
        return "%" + value.trim().toLowerCase(Locale.ROOT) + "%";
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}
