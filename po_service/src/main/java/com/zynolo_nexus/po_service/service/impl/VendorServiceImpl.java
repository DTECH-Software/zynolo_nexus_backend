package com.zynolo_nexus.po_service.service.impl;

import com.zynolo_nexus.po_service.dto.request.VendorCreateRequest;
import com.zynolo_nexus.po_service.dto.request.VendorFilterRequest;
import com.zynolo_nexus.po_service.dto.request.VendorFilterSearch;
import com.zynolo_nexus.po_service.dto.request.VendorReferenceDataRequest;
import com.zynolo_nexus.po_service.dto.request.VendorStatusRequest;
import com.zynolo_nexus.po_service.dto.request.VendorUpdateRequest;
import com.zynolo_nexus.po_service.dto.request.VendorViewRequest;
import com.zynolo_nexus.po_service.dto.response.ReferenceOptionDto;
import com.zynolo_nexus.po_service.dto.response.VendorDto;
import com.zynolo_nexus.po_service.dto.response.VendorFilterResultDto;
import com.zynolo_nexus.po_service.dto.response.VendorListItemDto;
import com.zynolo_nexus.po_service.dto.response.VendorPrivilegesDto;
import com.zynolo_nexus.po_service.dto.response.VendorReferenceDataDto;
import com.zynolo_nexus.po_service.exception.BadRequestException;
import com.zynolo_nexus.po_service.exception.ResourceNotFoundException;
import com.zynolo_nexus.po_service.model.Vendor;
import com.zynolo_nexus.po_service.repository.VendorRepository;
import com.zynolo_nexus.po_service.service.VendorService;
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
public class VendorServiceImpl implements VendorService {

    private static final String PAGE_CODE = "POVM";
    private static final String ACTIVE_STATUS = "ACTIVE";
    private static final String DEACTIVE_STATUS = "DEACTIVE";

    private final VendorRepository vendorRepository;
    private final PagePrivilegeResolver pagePrivilegeResolver;

    @Override
    public VendorReferenceDataDto getReferenceData(VendorReferenceDataRequest request) {
        var privileges = pagePrivilegeResolver.resolve(request != null ? request.getUsername() : null, PAGE_CODE);
        return VendorReferenceDataDto.builder()
                .defaultStatus(List.of(
                        option("ACTIVE", "Active"),
                        option("INACTIVE", "Inactive")
                ))
                .privileges(VendorPrivilegesDto.builder()
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
    public VendorDto create(VendorCreateRequest request) {
        String code = normalizeCode(request.getCode());
        if (vendorRepository.existsByCodeIgnoreCase(code)) {
            throw new BadRequestException("Vendor code already exists: " + code);
        }

        Vendor vendor = new Vendor();
        vendor.setCode(code);
        applyFields(vendor, request.getDescription(), request.getStreet1(), request.getStreet2(), request.getCity(),
                request.getState(), request.getCountry(), request.getZipCode(), request.getContactNo(),
                request.getEmail(), request.getWebsite());
        vendor.setStatus(ACTIVE_STATUS);
        applyAudit(vendor, request.getUsername(), true);

        return toDto(vendorRepository.save(vendor));
    }

    @Override
    @Transactional(readOnly = true)
    public VendorDto view(VendorViewRequest request) {
        return toDto(getById(request.getId()));
    }

    @Override
    @Transactional
    public VendorDto update(VendorUpdateRequest request) {
        Vendor vendor = getById(request.getId());
        String code = normalizeCode(request.getCode());
        boolean duplicateExists = vendorRepository.existsByCodeIgnoreCase(code)
                && !vendor.getCode().equalsIgnoreCase(code);
        if (duplicateExists) {
            throw new BadRequestException("Vendor code already exists: " + code);
        }

        vendor.setCode(code);
        applyFields(vendor, request.getDescription(), request.getStreet1(), request.getStreet2(), request.getCity(),
                request.getState(), request.getCountry(), request.getZipCode(), request.getContactNo(),
                request.getEmail(), request.getWebsite());
        if (hasText(request.getStatus())) {
            vendor.setStatus(parseVendorStatus(request.getStatus()));
        }
        applyAudit(vendor, request.getUsername(), false);

        return toDto(vendorRepository.save(vendor));
    }

    @Override
    @Transactional
    public VendorDto updateStatus(VendorStatusRequest request) {
        Vendor vendor = getById(request.getId());
        vendor.setStatus(parseVendorStatus(request.getStatus()));
        applyAudit(vendor, request.getUsername(), false);
        return toDto(vendorRepository.save(vendor));
    }

    @Override
    @Transactional(readOnly = true)
    public VendorFilterResultDto filterList(VendorFilterRequest request) {
        Pageable pageable = PageRequest.of(
                request.getPage(),
                request.getSize(),
                Sort.by(resolveDirection(request.getSortDirection()), resolveSortColumn(request.getSortColumn()))
        );
        Page<Vendor> page = vendorRepository.findAll(buildSpecification(request.getSearch()), pageable);
        List<VendorListItemDto> content = page.getContent().stream().map(this::toListItemDto).toList();
        return VendorFilterResultDto.builder()
                .content(content)
                .size(content.size())
                .totalRecords(page.getTotalElements())
                .build();
    }

    private Vendor getById(Long id) {
        return vendorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found with ID: " + id));
    }

    private Specification<Vendor> buildSpecification(VendorFilterSearch search) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (hasText(search.getCode())) {
                predicates.add(cb.like(cb.lower(root.get("code")), like(search.getCode())));
            }
            if (hasText(search.getDescription())) {
                predicates.add(cb.like(cb.lower(root.get("description")), like(search.getDescription())));
            }
            if (hasText(search.getContactNo())) {
                predicates.add(cb.like(cb.lower(root.get("contactNo")), like(search.getContactNo())));
            }
            if (hasText(search.getEmail())) {
                predicates.add(cb.like(cb.lower(root.get("email")), like(search.getEmail())));
            }
            if (hasText(search.getStatus())) {
                predicates.add(cb.equal(root.get("status"), parseVendorStatus(search.getStatus())));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private VendorDto toDto(Vendor vendor) {
        return VendorDto.builder()
                .id(vendor.getId())
                .code(vendor.getCode())
                .description(vendor.getDescription())
                .street1(vendor.getStreet1())
                .street2(vendor.getStreet2())
                .city(vendor.getCity())
                .state(vendor.getState())
                .country(vendor.getCountry())
                .zipCode(vendor.getZipCode())
                .contactNo(vendor.getContactNo())
                .email(vendor.getEmail())
                .website(vendor.getWebsite())
                .status(toApiStatus(vendor.getStatus()))
                .statusDescription(toStatusDescription(vendor.getStatus()))
                .createdDate(vendor.getCreatedDate())
                .lastModifiedDate(vendor.getLastModifiedDate())
                .createdBy(vendor.getCreatedBy())
                .lastModifiedBy(vendor.getLastModifiedBy())
                .build();
    }

    private VendorListItemDto toListItemDto(Vendor vendor) {
        return VendorListItemDto.builder()
                .id(vendor.getId())
                .code(vendor.getCode())
                .description(vendor.getDescription())
                .contactNo(vendor.getContactNo())
                .email(vendor.getEmail())
                .status(toApiStatus(vendor.getStatus()))
                .statusDescription(toStatusDescription(vendor.getStatus()))
                .createdDate(vendor.getCreatedDate())
                .lastModifiedDate(vendor.getLastModifiedDate())
                .createdBy(vendor.getCreatedBy())
                .lastModifiedBy(vendor.getLastModifiedBy())
                .build();
    }

    private void applyFields(Vendor vendor, String description, String street1, String street2, String city,
                             String state, String country, String zipCode, String contactNo, String email, String website) {
        vendor.setDescription(trim(description));
        vendor.setStreet1(trim(street1));
        vendor.setStreet2(trim(street2));
        vendor.setCity(trim(city));
        vendor.setState(trim(state));
        vendor.setCountry(trim(country));
        vendor.setZipCode(trim(zipCode));
        vendor.setContactNo(trim(contactNo));
        vendor.setEmail(trim(email));
        vendor.setWebsite(trim(website));
    }

    private void applyAudit(Vendor vendor, String username, boolean create) {
        LocalDateTime now = LocalDateTime.now();
        if (create) {
            vendor.setCreatedDate(now);
            vendor.setCreatedBy(username);
        }
        vendor.setLastModifiedDate(now);
        vendor.setLastModifiedBy(username);
    }

    private String parseVendorStatus(String value) {
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "ACTIVE" -> ACTIVE_STATUS;
            case "INACTIVE", "DEACTIVE" -> DEACTIVE_STATUS;
            default -> throw new BadRequestException("Invalid status: " + value);
        };
    }

    private String toApiStatus(String value) {
        return DEACTIVE_STATUS.equalsIgnoreCase(value) ? "INACTIVE" : "ACTIVE";
    }

    private String toStatusDescription(String value) {
        return DEACTIVE_STATUS.equalsIgnoreCase(value) ? "Inactive" : "Active";
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
            case "code", "description", "contactNo", "email", "status", "createdDate", "lastModifiedDate", "createdBy" -> sortColumn;
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
