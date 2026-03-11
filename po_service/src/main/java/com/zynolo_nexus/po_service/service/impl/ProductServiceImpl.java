package com.zynolo_nexus.po_service.service.impl;

import com.zynolo_nexus.po_service.dto.request.ProductCreateRequest;
import com.zynolo_nexus.po_service.dto.request.ProductFilterRequest;
import com.zynolo_nexus.po_service.dto.request.ProductFilterSearch;
import com.zynolo_nexus.po_service.dto.request.ProductReferenceDataRequest;
import com.zynolo_nexus.po_service.dto.request.ProductStatusRequest;
import com.zynolo_nexus.po_service.dto.request.ProductUpdateRequest;
import com.zynolo_nexus.po_service.dto.request.ProductViewRequest;
import com.zynolo_nexus.po_service.dto.response.ProductDto;
import com.zynolo_nexus.po_service.dto.response.ProductFilterResultDto;
import com.zynolo_nexus.po_service.dto.response.ProductListItemDto;
import com.zynolo_nexus.po_service.dto.response.ProductPrivilegesDto;
import com.zynolo_nexus.po_service.dto.response.ProductReferenceDataDto;
import com.zynolo_nexus.po_service.dto.response.ReferenceOptionDto;
import com.zynolo_nexus.po_service.enums.MasterStatus;
import com.zynolo_nexus.po_service.exception.BadRequestException;
import com.zynolo_nexus.po_service.exception.ResourceNotFoundException;
import com.zynolo_nexus.po_service.model.Product;
import com.zynolo_nexus.po_service.repository.ProductRepository;
import com.zynolo_nexus.po_service.service.ProductService;
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
public class ProductServiceImpl implements ProductService {

    private static final String PAGE_CODE = "PRDM";

    private final ProductRepository productRepository;
    private final PagePrivilegeResolver pagePrivilegeResolver;

    @Override
    public ProductReferenceDataDto getReferenceData(ProductReferenceDataRequest request) {
        var privileges = pagePrivilegeResolver.resolve(request != null ? request.getUsername() : null, PAGE_CODE);
        return ProductReferenceDataDto.builder()
                .defaultStatus(List.of(
                        option(MasterStatus.ACTIVE.name(), "Active"),
                        option(MasterStatus.INACTIVE.name(), "Inactive")
                ))
                .privileges(ProductPrivilegesDto.builder()
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
    public ProductDto create(ProductCreateRequest request) {
        String code = normalizeCode(request.getCode());
        if (productRepository.existsByCodeIgnoreCase(code)) {
            throw new BadRequestException("Product code already exists: " + code);
        }

        Product product = new Product();
        product.setCode(code);
        product.setDescription(trim(request.getDescription()));
        product.setUom(trim(request.getUom()));
        product.setDefaultPrice(request.getDefaultPrice());
        product.setStatus(MasterStatus.ACTIVE);
        applyAudit(product, request.getUsername(), true);

        return toDto(productRepository.save(product));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDto view(ProductViewRequest request) {
        return toDto(getById(request.getId()));
    }

    @Override
    @Transactional
    public ProductDto update(ProductUpdateRequest request) {
        Product product = getById(request.getId());
        String code = normalizeCode(request.getCode());
        boolean duplicateExists = productRepository.existsByCodeIgnoreCase(code)
                && !product.getCode().equalsIgnoreCase(code);
        if (duplicateExists) {
            throw new BadRequestException("Product code already exists: " + code);
        }

        product.setCode(code);
        product.setDescription(trim(request.getDescription()));
        product.setUom(trim(request.getUom()));
        product.setDefaultPrice(request.getDefaultPrice());
        if (hasText(request.getStatus())) {
            product.setStatus(parseStatus(request.getStatus()));
        }
        applyAudit(product, request.getUsername(), false);

        return toDto(productRepository.save(product));
    }

    @Override
    @Transactional
    public ProductDto updateStatus(ProductStatusRequest request) {
        Product product = getById(request.getId());
        product.setStatus(parseStatus(request.getStatus()));
        applyAudit(product, request.getUsername(), false);
        return toDto(productRepository.save(product));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductFilterResultDto filterList(ProductFilterRequest request) {
        Pageable pageable = PageRequest.of(
                request.getPage(),
                request.getSize(),
                Sort.by(resolveDirection(request.getSortDirection()), resolveSortColumn(request.getSortColumn()))
        );
        Page<Product> page = productRepository.findAll(buildSpecification(request.getSearch()), pageable);
        List<ProductListItemDto> content = page.getContent().stream().map(this::toListItem).toList();
        return ProductFilterResultDto.builder()
                .content(content)
                .size(content.size())
                .totalRecords(page.getTotalElements())
                .build();
    }

    private Product getById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));
    }

    private Specification<Product> buildSpecification(ProductFilterSearch search) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (hasText(search.getCode())) {
                predicates.add(cb.like(cb.lower(root.get("code")), like(search.getCode())));
            }
            if (hasText(search.getDescription())) {
                predicates.add(cb.like(cb.lower(root.get("description")), like(search.getDescription())));
            }
            if (hasText(search.getUom())) {
                predicates.add(cb.like(cb.lower(root.get("uom")), like(search.getUom())));
            }
            if (hasText(search.getStatus())) {
                predicates.add(cb.equal(root.get("status"), parseStatus(search.getStatus())));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private ProductDto toDto(Product product) {
        return ProductDto.builder()
                .id(product.getId())
                .code(product.getCode())
                .description(product.getDescription())
                .uom(product.getUom())
                .defaultPrice(product.getDefaultPrice())
                .status(product.getStatus().name())
                .statusDescription(toStatusDescription(product.getStatus()))
                .createdDate(product.getCreatedDate())
                .lastModifiedDate(product.getLastModifiedDate())
                .createdBy(product.getCreatedBy())
                .lastModifiedBy(product.getLastModifiedBy())
                .build();
    }

    private ProductListItemDto toListItem(Product product) {
        return ProductListItemDto.builder()
                .id(product.getId())
                .code(product.getCode())
                .description(product.getDescription())
                .uom(product.getUom())
                .defaultPrice(product.getDefaultPrice())
                .status(product.getStatus().name())
                .statusDescription(toStatusDescription(product.getStatus()))
                .createdDate(product.getCreatedDate())
                .lastModifiedDate(product.getLastModifiedDate())
                .createdBy(product.getCreatedBy())
                .lastModifiedBy(product.getLastModifiedBy())
                .build();
    }

    private void applyAudit(Product product, String username, boolean create) {
        LocalDateTime now = LocalDateTime.now();
        if (create) {
            product.setCreatedDate(now);
            product.setCreatedBy(username);
        }
        product.setLastModifiedDate(now);
        product.setLastModifiedBy(username);
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

    private String normalizeCode(String value) {
        return value.trim().toUpperCase(Locale.ROOT);
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
            case "code", "description", "uom", "status", "createdDate", "lastModifiedDate", "createdBy", "defaultPrice" -> sortColumn;
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
