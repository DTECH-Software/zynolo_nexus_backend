package com.zynolo_nexus.po_service.service.impl;

import com.zynolo_nexus.po_service.dto.request.VendorProductCreateRequest;
import com.zynolo_nexus.po_service.dto.request.VendorProductFilterRequest;
import com.zynolo_nexus.po_service.dto.request.VendorProductFilterSearch;
import com.zynolo_nexus.po_service.dto.request.VendorProductReferenceDataRequest;
import com.zynolo_nexus.po_service.dto.request.VendorProductStatusRequest;
import com.zynolo_nexus.po_service.dto.request.VendorProductUpdateRequest;
import com.zynolo_nexus.po_service.dto.request.VendorProductViewRequest;
import com.zynolo_nexus.po_service.dto.response.ReferenceOptionDto;
import com.zynolo_nexus.po_service.dto.response.VendorProductDto;
import com.zynolo_nexus.po_service.dto.response.VendorProductFilterResultDto;
import com.zynolo_nexus.po_service.dto.response.VendorProductListItemDto;
import com.zynolo_nexus.po_service.dto.response.VendorProductPrivilegesDto;
import com.zynolo_nexus.po_service.dto.response.VendorProductReferenceDataDto;
import com.zynolo_nexus.po_service.enums.MasterStatus;
import com.zynolo_nexus.po_service.exception.BadRequestException;
import com.zynolo_nexus.po_service.exception.ResourceNotFoundException;
import com.zynolo_nexus.po_service.model.Product;
import com.zynolo_nexus.po_service.model.Vendor;
import com.zynolo_nexus.po_service.model.VendorProductMapping;
import com.zynolo_nexus.po_service.repository.ProductRepository;
import com.zynolo_nexus.po_service.repository.VendorProductMappingRepository;
import com.zynolo_nexus.po_service.repository.VendorRepository;
import com.zynolo_nexus.po_service.service.VendorProductMappingService;
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
public class VendorProductMappingServiceImpl implements VendorProductMappingService {

    private static final String PAGE_CODE = "VPMP";
    private static final String ACTIVE_VENDOR_STATUS = "ACTIVE";

    private final VendorRepository vendorRepository;
    private final ProductRepository productRepository;
    private final VendorProductMappingRepository vendorProductMappingRepository;
    private final PagePrivilegeResolver pagePrivilegeResolver;

    @Override
    public VendorProductReferenceDataDto getReferenceData(VendorProductReferenceDataRequest request) {
        var privileges = pagePrivilegeResolver.resolve(request != null ? request.getUsername() : null, PAGE_CODE);
        return VendorProductReferenceDataDto.builder()
                .vendors(vendorRepository.findAllByStatusOrderByCodeAsc(ACTIVE_VENDOR_STATUS).stream()
                        .map(vendor -> option(vendor.getCode(), vendor.getDescription()))
                        .toList())
                .products(productRepository.findAllByStatusOrderByCodeAsc(MasterStatus.ACTIVE).stream()
                        .map(product -> option(product.getCode(), product.getDescription()))
                        .toList())
                .defaultStatus(List.of(
                        option(MasterStatus.ACTIVE.name(), "Active"),
                        option(MasterStatus.INACTIVE.name(), "Inactive")
                ))
                .privileges(VendorProductPrivilegesDto.builder()
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
    public VendorProductDto create(VendorProductCreateRequest request) {
        Vendor vendor = getActiveVendor(request.getVendorCode());
        Product product = getActiveProduct(request.getProductCode());
        if (vendorProductMappingRepository.findByVendorAndProduct(vendor, product).isPresent()) {
            throw new BadRequestException("Vendor product mapping already exists");
        }

        VendorProductMapping mapping = new VendorProductMapping();
        mapping.setVendor(vendor);
        mapping.setProduct(product);
        mapping.setVendorProductCode(trim(request.getVendorProductCode()));
        mapping.setLastPrice(request.getLastPrice());
        mapping.setLeadTimeDays(request.getLeadTimeDays());
        mapping.setStatus(MasterStatus.ACTIVE);
        applyAudit(mapping, request.getUsername(), true);

        return toDto(vendorProductMappingRepository.save(mapping));
    }

    @Override
    @Transactional(readOnly = true)
    public VendorProductDto view(VendorProductViewRequest request) {
        return toDto(getById(request.getId()));
    }

    @Override
    @Transactional
    public VendorProductDto update(VendorProductUpdateRequest request) {
        VendorProductMapping mapping = getById(request.getId());
        Vendor vendor = getActiveVendor(request.getVendorCode());
        Product product = getActiveProduct(request.getProductCode());

        vendorProductMappingRepository.findByVendorAndProduct(vendor, product)
                .filter(existing -> !existing.getId().equals(mapping.getId()))
                .ifPresent(existing -> {
                    throw new BadRequestException("Vendor product mapping already exists");
                });

        mapping.setVendor(vendor);
        mapping.setProduct(product);
        mapping.setVendorProductCode(trim(request.getVendorProductCode()));
        mapping.setLastPrice(request.getLastPrice());
        mapping.setLeadTimeDays(request.getLeadTimeDays());
        if (hasText(request.getStatus())) {
            mapping.setStatus(parseStatus(request.getStatus()));
        }
        applyAudit(mapping, request.getUsername(), false);

        return toDto(vendorProductMappingRepository.save(mapping));
    }

    @Override
    @Transactional
    public VendorProductDto updateStatus(VendorProductStatusRequest request) {
        VendorProductMapping mapping = getById(request.getId());
        mapping.setStatus(parseStatus(request.getStatus()));
        applyAudit(mapping, request.getUsername(), false);
        return toDto(vendorProductMappingRepository.save(mapping));
    }

    @Override
    @Transactional(readOnly = true)
    public VendorProductFilterResultDto filterList(VendorProductFilterRequest request) {
        Pageable pageable = PageRequest.of(
                request.getPage(),
                request.getSize(),
                Sort.by(resolveDirection(request.getSortDirection()), resolveSortColumn(request.getSortColumn()))
        );
        Page<VendorProductMapping> page = vendorProductMappingRepository.findAll(buildSpecification(request.getSearch()), pageable);
        List<VendorProductListItemDto> content = page.getContent().stream().map(this::toListItem).toList();
        return VendorProductFilterResultDto.builder()
                .content(content)
                .size(content.size())
                .totalRecords(page.getTotalElements())
                .build();
    }

    private VendorProductMapping getById(Long id) {
        return vendorProductMappingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor product mapping not found with ID: " + id));
    }

    private Vendor getActiveVendor(String vendorCode) {
        return vendorRepository.findByCodeIgnoreCaseAndStatus(vendorCode, ACTIVE_VENDOR_STATUS)
                .orElseThrow(() -> new BadRequestException("Active vendor not found for code: " + vendorCode));
    }

    private Product getActiveProduct(String productCode) {
        return productRepository.findByCodeIgnoreCaseAndStatus(productCode, MasterStatus.ACTIVE)
                .orElseThrow(() -> new BadRequestException("Active product not found for code: " + productCode));
    }

    private Specification<VendorProductMapping> buildSpecification(VendorProductFilterSearch search) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            var vendorJoin = root.join("vendor");
            var productJoin = root.join("product");

            if (hasText(search.getVendorCode())) {
                predicates.add(cb.like(cb.lower(vendorJoin.get("code")), like(search.getVendorCode())));
            }
            if (hasText(search.getVendorDescription())) {
                predicates.add(cb.like(cb.lower(vendorJoin.get("description")), like(search.getVendorDescription())));
            }
            if (hasText(search.getProductCode())) {
                predicates.add(cb.like(cb.lower(productJoin.get("code")), like(search.getProductCode())));
            }
            if (hasText(search.getProductDescription())) {
                predicates.add(cb.like(cb.lower(productJoin.get("description")), like(search.getProductDescription())));
            }
            if (hasText(search.getStatus())) {
                predicates.add(cb.equal(root.get("status"), parseStatus(search.getStatus())));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private VendorProductDto toDto(VendorProductMapping mapping) {
        return VendorProductDto.builder()
                .id(mapping.getId())
                .vendorCode(mapping.getVendor().getCode())
                .vendorDescription(mapping.getVendor().getDescription())
                .productCode(mapping.getProduct().getCode())
                .productDescription(mapping.getProduct().getDescription())
                .vendorProductCode(mapping.getVendorProductCode())
                .lastPrice(mapping.getLastPrice())
                .leadTimeDays(mapping.getLeadTimeDays())
                .status(mapping.getStatus().name())
                .statusDescription(toStatusDescription(mapping.getStatus()))
                .createdDate(mapping.getCreatedDate())
                .lastModifiedDate(mapping.getLastModifiedDate())
                .createdBy(mapping.getCreatedBy())
                .lastModifiedBy(mapping.getLastModifiedBy())
                .build();
    }

    private VendorProductListItemDto toListItem(VendorProductMapping mapping) {
        return VendorProductListItemDto.builder()
                .id(mapping.getId())
                .vendorCode(mapping.getVendor().getCode())
                .vendorDescription(mapping.getVendor().getDescription())
                .productCode(mapping.getProduct().getCode())
                .productDescription(mapping.getProduct().getDescription())
                .lastPrice(mapping.getLastPrice())
                .leadTimeDays(mapping.getLeadTimeDays())
                .status(mapping.getStatus().name())
                .statusDescription(toStatusDescription(mapping.getStatus()))
                .createdDate(mapping.getCreatedDate())
                .lastModifiedDate(mapping.getLastModifiedDate())
                .createdBy(mapping.getCreatedBy())
                .lastModifiedBy(mapping.getLastModifiedBy())
                .build();
    }

    private void applyAudit(VendorProductMapping mapping, String username, boolean create) {
        LocalDateTime now = LocalDateTime.now();
        if (create) {
            mapping.setCreatedDate(now);
            mapping.setCreatedBy(username);
        }
        mapping.setLastModifiedDate(now);
        mapping.setLastModifiedBy(username);
    }

    private MasterStatus parseStatus(String value) {
        try {
            return MasterStatus.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (Exception ex) {
            throw new BadRequestException("Invalid status: " + value);
        }
    }

    private String toStatusDescription(MasterStatus status) {
        return status == MasterStatus.ACTIVE ? "Active" : "Inactive";
    }

    private ReferenceOptionDto option(String code, String description) {
        return ReferenceOptionDto.builder().code(code).description(description).build();
    }

    private Sort.Direction resolveDirection(String direction) {
        return "ASC".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
    }

    private String resolveSortColumn(String sortColumn) {
        if (!hasText(sortColumn)) {
            return "lastModifiedDate";
        }
        return switch (sortColumn) {
            case "status", "createdDate", "lastModifiedDate", "createdBy", "lastPrice", "leadTimeDays" -> sortColumn;
            default -> "lastModifiedDate";
        };
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
