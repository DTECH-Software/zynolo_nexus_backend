package com.zynolo_nexus.po_service.service.impl;

import com.zynolo_nexus.po_service.dto.request.PoReferenceDataRequest;
import com.zynolo_nexus.po_service.dto.request.PoApprovalVendorProductsRequest;
import com.zynolo_nexus.po_service.dto.request.PoRequestApproveRequest;
import com.zynolo_nexus.po_service.dto.request.PoRequestApprovalItemRequest;
import com.zynolo_nexus.po_service.dto.request.PoRequestFilterRequest;
import com.zynolo_nexus.po_service.dto.request.PoRequestFilterSearch;
import com.zynolo_nexus.po_service.dto.request.PoRequestRejectRequest;
import com.zynolo_nexus.po_service.dto.request.PoRequestViewRequest;
import com.zynolo_nexus.po_service.dto.response.PoRequestDto;
import com.zynolo_nexus.po_service.dto.response.PoApprovalVendorProductDto;
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
import com.zynolo_nexus.po_service.model.Product;
import com.zynolo_nexus.po_service.model.Vendor;
import com.zynolo_nexus.po_service.model.VendorProductMapping;
import com.zynolo_nexus.po_service.repository.CompanyRepository;
import com.zynolo_nexus.po_service.repository.DepartmentRepository;
import com.zynolo_nexus.po_service.repository.PoRequestRepository;
import com.zynolo_nexus.po_service.repository.ProductRepository;
import com.zynolo_nexus.po_service.repository.VendorRepository;
import com.zynolo_nexus.po_service.repository.VendorProductMappingRepository;
import com.zynolo_nexus.po_service.service.PoRequestApprovalService;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PoRequestApprovalServiceImpl implements PoRequestApprovalService {

    private static final String PAGE_CODE = "PORA";
    private static final Map<String, String> REQUEST_TYPES = new LinkedHashMap<>();

    static {
        REQUEST_TYPES.put("GOODS", "Goods");
        REQUEST_TYPES.put("SERVICES", "Services");
        REQUEST_TYPES.put("COMBINED", "Combined");
    }

    private final CompanyRepository companyRepository;
    private final DepartmentRepository departmentRepository;
    private final VendorRepository vendorRepository;
    private final ProductRepository productRepository;
    private final VendorProductMappingRepository vendorProductMappingRepository;
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
                .products(List.of())
                .defaultStatus(List.of(
                        option(PoRequestStatus.SUBMITTED.name(), "Submitted"),
                        option(PoRequestStatus.APPROVED.name(), "Approved"),
                        option(PoRequestStatus.REJECTED.name(), "Rejected")
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

    @Override
    @Transactional(readOnly = true)
    public PoRequestDto view(PoRequestViewRequest request) {
        return toDto(getById(request.getId()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PoApprovalVendorProductDto> getVendorProducts(PoApprovalVendorProductsRequest request) {
        PoRequest poRequest = getById(request.getId());
        validateSubmitted(poRequest);
        Vendor vendor = resolveVendor(request.getVendorCode());

        return poRequest.getItems().stream()
                .map(item -> toApprovalVendorProduct(item, vendor))
                .toList();
    }

    @Override
    @Transactional
    public PoRequestDto approve(PoRequestApproveRequest request) {
        PoRequest poRequest = getById(request.getId());
        validateSubmitted(poRequest);

        Vendor vendor = resolveVendor(request.getVendorCode());
        Map<Long, PoRequestApprovalItemRequest> approvalItems = indexApprovalItems(request);
        if (approvalItems.size() != poRequest.getItems().size()) {
            throw new BadRequestException("Approval prices are required for every requested item");
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (PoRequestItem item : poRequest.getItems()) {
            PoRequestApprovalItemRequest approvalItem = approvalItems.get(item.getId());
            if (approvalItem == null) {
                throw new BadRequestException("Approval price is missing for request item ID: " + item.getId());
            }

            Product product = resolveActiveProduct(item.getItemCode());
            vendorProductMappingRepository.findByVendorAndProductAndStatus(vendor, product, MasterStatus.ACTIVE)
                    .orElseThrow(() -> new BadRequestException(
                            "Selected vendor does not supply active product: " + item.getItemCode()));

            item.setUnitPrice(approvalItem.getUnitPrice());
            item.setLineAmount(item.getQuantity().multiply(approvalItem.getUnitPrice()));
            totalAmount = totalAmount.add(item.getLineAmount());
        }

        poRequest.setVendorCode(vendor.getCode());
        poRequest.setVendorName(vendor.getDescription());
        poRequest.setTotalAmount(totalAmount);
        poRequest.setStatus(PoRequestStatus.APPROVED);
        poRequest.setReviewedDate(LocalDateTime.now());
        poRequest.setReviewedBy(request.getUsername());
        poRequest.setReviewRemark(trim(request.getReviewRemark()));
        applyAudit(poRequest, request.getUsername());

        return toDto(poRequestRepository.save(poRequest));
    }

    @Override
    @Transactional
    public PoRequestDto reject(PoRequestRejectRequest request) {
        PoRequest poRequest = getById(request.getId());
        validateSubmitted(poRequest);

        poRequest.setStatus(PoRequestStatus.REJECTED);
        poRequest.setReviewedDate(LocalDateTime.now());
        poRequest.setReviewedBy(request.getUsername());
        poRequest.setReviewRemark(trim(request.getRejectionReason()));
        applyAudit(poRequest, request.getUsername());

        return toDto(poRequestRepository.save(poRequest));
    }

    private PoRequest getById(Long id) {
        return poRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PO request not found with ID: " + id));
    }

    private void validateSubmitted(PoRequest poRequest) {
        if (poRequest.getStatus() != PoRequestStatus.SUBMITTED) {
            throw new BadRequestException("Only SUBMITTED requests can be approved or rejected");
        }
    }

    private void applyAudit(PoRequest poRequest, String username) {
        poRequest.setLastModifiedDate(LocalDateTime.now());
        poRequest.setLastModifiedBy(username);
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
            } else {
                predicates.add(root.get("status").in(PoRequestStatus.SUBMITTED, PoRequestStatus.APPROVED, PoRequestStatus.REJECTED));
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
                    "createdDate", "lastModifiedDate", "requiredDate", "totalAmount", "createdBy", "reviewedDate", "reviewedBy" -> sortColumn;
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
                .department(department != null ? department.getCode() : null)
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
                .reviewRemark(poRequest.getStatus() == PoRequestStatus.APPROVED ? poRequest.getReviewRemark() : null)
                .rejectionReason(poRequest.getStatus() == PoRequestStatus.REJECTED ? poRequest.getReviewRemark() : null)
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
                .estimatedUnitPrice(effectiveEstimatedUnitPrice(item))
                .unitPrice(item.getUnitPrice())
                .lineAmount(item.getLineAmount())
                .build();
    }

    private PoApprovalVendorProductDto toApprovalVendorProduct(PoRequestItem item, Vendor vendor) {
        Optional<Product> product = productRepository.findByCodeIgnoreCaseAndStatus(item.getItemCode(), MasterStatus.ACTIVE);
        Optional<VendorProductMapping> mapping = product.flatMap(value ->
                vendorProductMappingRepository.findByVendorAndProductAndStatus(vendor, value, MasterStatus.ACTIVE));

        return PoApprovalVendorProductDto.builder()
                .requestItemId(item.getId())
                .itemCode(item.getItemCode())
                .itemDescription(item.getItemDescription())
                .uom(item.getUom())
                .quantity(item.getQuantity())
                .estimatedUnitPrice(effectiveEstimatedUnitPrice(item))
                .available(mapping.isPresent())
                .vendorProductCode(mapping.map(VendorProductMapping::getVendorProductCode).orElse(null))
                .defaultPrice(product.map(Product::getDefaultPrice).orElse(null))
                .lastPrice(mapping.map(VendorProductMapping::getLastPrice).orElse(null))
                .leadTimeDays(mapping.map(VendorProductMapping::getLeadTimeDays).orElse(null))
                .build();
    }

    private Map<Long, PoRequestApprovalItemRequest> indexApprovalItems(PoRequestApproveRequest request) {
        Map<Long, PoRequestApprovalItemRequest> indexed = new HashMap<>();
        for (PoRequestApprovalItemRequest item : request.getItems()) {
            if (indexed.put(item.getRequestItemId(), item) != null) {
                throw new BadRequestException("Duplicate requestItemId: " + item.getRequestItemId());
            }
        }
        return indexed;
    }

    private Vendor resolveVendor(String vendorCode) {
        return vendorRepository.findByCodeIgnoreCaseAndStatus(vendorCode, "ACTIVE")
                .orElseThrow(() -> new BadRequestException("Active vendor not found for code: " + vendorCode));
    }

    private Product resolveActiveProduct(String itemCode) {
        return productRepository.findByCodeIgnoreCaseAndStatus(itemCode, MasterStatus.ACTIVE)
                .orElseThrow(() -> new BadRequestException("Active product not found for code: " + itemCode));
    }

    private BigDecimal effectiveEstimatedUnitPrice(PoRequestItem item) {
        return item.getEstimatedUnitPrice() != null ? item.getEstimatedUnitPrice() : item.getUnitPrice();
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

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}
