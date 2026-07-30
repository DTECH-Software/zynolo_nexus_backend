package com.zynolo_nexus.meeting_room_booking_service.service.impl;

import com.zynolo_nexus.meeting_room_booking_service.context.CompanyContext;
import com.zynolo_nexus.meeting_room_booking_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingRefreshmentActiveStatusRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingRefreshmentCreateRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingRefreshmentFilterRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingRefreshmentFilterSearch;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingRefreshmentReferenceDataRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingRefreshmentUpdateRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingRefreshmentDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingRefreshmentFilterResultDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingRefreshmentPrivilegesDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingRefreshmentReferenceDataDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingVendorDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.ReferenceOptionDto;
import com.zynolo_nexus.meeting_room_booking_service.enums.RefreshmentCategory;
import com.zynolo_nexus.meeting_room_booking_service.exception.BadRequestException;
import com.zynolo_nexus.meeting_room_booking_service.exception.ResourceNotFoundException;
import com.zynolo_nexus.meeting_room_booking_service.model.CompanyLookup;
import com.zynolo_nexus.meeting_room_booking_service.model.MeetingRefreshment;
import com.zynolo_nexus.meeting_room_booking_service.model.MeetingVendor;
import com.zynolo_nexus.meeting_room_booking_service.repository.CompanyLookupRepository;
import com.zynolo_nexus.meeting_room_booking_service.repository.MeetingRefreshmentRepository;
import com.zynolo_nexus.meeting_room_booking_service.repository.MeetingVendorRepository;
import com.zynolo_nexus.meeting_room_booking_service.service.MeetingRefreshmentService;
import com.zynolo_nexus.meeting_room_booking_service.service.support.PagePrivilegeResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class MeetingRefreshmentServiceImpl implements MeetingRefreshmentService {

    private static final String PAGE_CODE = "MBM_SYSC_REFM";

    private final MeetingRefreshmentRepository meetingRefreshmentRepository;
    private final MeetingVendorRepository meetingVendorRepository;
    private final CompanyLookupRepository companyLookupRepository;
    private final PagePrivilegeResolver pagePrivilegeResolver;

    @Value("${app.default.company-id:1}")
    private Long defaultCompanyId;

    @Override
    @Transactional
    public MessageResponseDTO<MeetingRefreshmentDto> create(MeetingRefreshmentCreateRequest request) {
        if (request == null) {
            throw new BadRequestException("Invalid refreshment request");
        }
        validateRequired(request.getRefreshmentCode(), request.getCategory(), request.getItemName(), request.getUnitPrice(), request.getActive());

        Long companyId = resolveCompanyId();
        String refreshmentCode = normalizeCode(request.getRefreshmentCode());
        if (meetingRefreshmentRepository.existsByCompanyIdAndRefreshmentCodeIgnoreCase(companyId, refreshmentCode)) {
            throw new BadRequestException("Refreshment code already exists");
        }

        MeetingVendor defaultVendor = resolveActiveVendor(request.getDefaultVendorId(), companyId);
        CompanyLookup company = resolveCompany(companyId);

        MeetingRefreshment refreshment = MeetingRefreshment.builder()
                .companyId(companyId)
                .companyCode(resolveCompanyCode(company))
                .companyName(resolveCompanyName(company))
                .refreshmentCode(refreshmentCode)
                .category(request.getCategory())
                .itemName(request.getItemName().trim())
                .defaultVendorId(defaultVendor != null ? defaultVendor.getId() : null)
                .defaultVendorCode(defaultVendor != null ? defaultVendor.getVendorCode() : null)
                .defaultVendorName(defaultVendor != null ? defaultVendor.getVendorName() : null)
                .unitPrice(request.getUnitPrice())
                .description(trimToNull(request.getDescription()))
                .active(request.getActive())
                .createdBy(trimToNull(request.getUsername()))
                .lastModifiedBy(trimToNull(request.getUsername()))
                .build();

        try {
            return success("Refreshment created successfully", toDto(meetingRefreshmentRepository.saveAndFlush(refreshment)));
        } catch (DataIntegrityViolationException ex) {
            throw duplicateRefreshmentException(ex);
        }
    }

    @Override
    @Transactional
    public MessageResponseDTO<MeetingRefreshmentDto> update(MeetingRefreshmentUpdateRequest request) {
        if (request == null || request.getId() == null) {
            throw new BadRequestException("Invalid refreshment update request");
        }
        MeetingRefreshment refreshment = findRefreshment(request.getId());
        Long companyId = refreshment.getCompanyId();

        if (StringUtils.hasText(request.getRefreshmentCode())) {
            String refreshmentCode = normalizeCode(request.getRefreshmentCode());
            if (!refreshmentCode.equalsIgnoreCase(refreshment.getRefreshmentCode())
                    && meetingRefreshmentRepository.existsByCompanyIdAndRefreshmentCodeIgnoreCaseAndIdNot(
                    companyId, refreshmentCode, refreshment.getId())) {
                throw new BadRequestException("Refreshment code already exists");
            }
            refreshment.setRefreshmentCode(refreshmentCode);
        }
        if (request.getCategory() != null) {
            refreshment.setCategory(request.getCategory());
        }
        if (StringUtils.hasText(request.getItemName())) {
            refreshment.setItemName(request.getItemName().trim());
        }
        if (request.getDefaultVendorId() != null) {
            MeetingVendor defaultVendor = resolveActiveVendor(request.getDefaultVendorId(), companyId);
            refreshment.setDefaultVendorId(defaultVendor.getId());
            refreshment.setDefaultVendorCode(defaultVendor.getVendorCode());
            refreshment.setDefaultVendorName(defaultVendor.getVendorName());
        }
        if (request.getUnitPrice() != null) {
            validateUnitPrice(request.getUnitPrice());
            refreshment.setUnitPrice(request.getUnitPrice());
        }
        if (request.getDescription() != null) {
            refreshment.setDescription(trimToNull(request.getDescription()));
        }
        if (request.getActive() != null) {
            refreshment.setActive(request.getActive());
        }
        if (StringUtils.hasText(request.getUsername())) {
            refreshment.setLastModifiedBy(request.getUsername().trim());
        }

        try {
            return success("Refreshment updated successfully", toDto(meetingRefreshmentRepository.saveAndFlush(refreshment)));
        } catch (DataIntegrityViolationException ex) {
            throw duplicateRefreshmentException(ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public MessageResponseDTO<MeetingRefreshmentDto> view(Long id) {
        if (id == null) {
            throw new BadRequestException("Invalid refreshment view request");
        }
        return success("Refreshment retrieved successfully", toDto(findRefreshment(id)));
    }

    @Override
    @Transactional
    public MessageResponseDTO<MeetingRefreshmentDto> updateActiveStatus(MeetingRefreshmentActiveStatusRequest request) {
        if (request == null || request.getId() == null || request.getActive() == null) {
            throw new BadRequestException("Invalid refreshment active status request");
        }
        MeetingRefreshment refreshment = findRefreshment(request.getId());
        refreshment.setActive(request.getActive());
        if (StringUtils.hasText(request.getUsername())) {
            refreshment.setLastModifiedBy(request.getUsername().trim());
        }
        String message = request.getActive() ? "Refreshment activated successfully" : "Refreshment deactivated successfully";
        return success(message, toDto(meetingRefreshmentRepository.save(refreshment)));
    }

    @Override
    @Transactional(readOnly = true)
    public MessageResponseDTO<MeetingRefreshmentFilterResultDto> filterList(MeetingRefreshmentFilterRequest request) {
        int page = request != null && request.getPage() != null && request.getPage() >= 0 ? request.getPage() : 0;
        int size = request != null && request.getSize() != null && request.getSize() > 0 ? request.getSize() : 10;
        MeetingRefreshmentFilterSearch search = request != null ? request.getSearch() : null;
        Long companyId = resolveCompanyId();

        List<MeetingRefreshmentDto> filtered = meetingRefreshmentRepository.findAll().stream()
                .filter(refreshment -> companyId.equals(refreshment.getCompanyId()))
                .filter(refreshment -> matches(refreshment, search))
                .sorted(resolveComparator(request))
                .map(this::toDto)
                .toList();

        int from = Math.min(page * size, filtered.size());
        int to = Math.min(from + size, filtered.size());
        List<MeetingRefreshmentDto> content = filtered.subList(from, to);
        int totalPages = filtered.isEmpty() ? 0 : (int) Math.ceil((double) filtered.size() / size);

        return MessageResponseDTO.<MeetingRefreshmentFilterResultDto>builder()
                .success(true)
                .message("Refreshments filtered successfully")
                .data(MeetingRefreshmentFilterResultDto.builder()
                        .content(content)
                        .size(content.size())
                        .totalRecords(filtered.size())
                        .page(page)
                        .totalPages(totalPages)
                        .build())
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public MessageResponseDTO<MeetingRefreshmentReferenceDataDto> referenceData(MeetingRefreshmentReferenceDataRequest request) {
        Long companyId = resolveCompanyId();
        CompanyLookup company = resolveCompany(companyId);
        var pagePrivileges = pagePrivilegeResolver.resolve(request != null ? request.getUsername() : null, PAGE_CODE);

        List<ReferenceOptionDto> categories = List.of(RefreshmentCategory.values()).stream()
                .map(category -> ReferenceOptionDto.builder()
                        .code(category.name())
                        .description(toTitleCase(category.name()))
                        .build())
                .toList();

        List<MeetingVendorDto> activeVendors = meetingVendorRepository.findAll().stream()
                .filter(vendor -> companyId.equals(vendor.getCompanyId()))
                .filter(vendor -> Boolean.TRUE.equals(vendor.getActive()))
                .sorted(Comparator.comparing(MeetingVendor::getVendorName, Comparator.nullsLast(String::compareToIgnoreCase)))
                .map(this::toVendorDto)
                .toList();

        return MessageResponseDTO.<MeetingRefreshmentReferenceDataDto>builder()
                .success(true)
                .message("Refreshment reference data loaded successfully")
                .data(MeetingRefreshmentReferenceDataDto.builder()
                        .companyId(companyId)
                        .companyCode(resolveCompanyCode(company))
                        .companyName(resolveCompanyName(company))
                        .categories(categories)
                        .statuses(activeStatusOptions())
                        .activeVendors(activeVendors)
                        .privileges(MeetingRefreshmentPrivilegesDto.builder()
                                .add(pagePrivileges.isAdd())
                                .update(pagePrivileges.isUpdate())
                                .view(pagePrivileges.isView())
                                .search(pagePrivileges.isSearch())
                                .activate(pagePrivileges.isActivate())
                                .deactivate(pagePrivileges.isDeactivate())
                                .build())
                        .build())
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    private MeetingRefreshment findRefreshment(Long id) {
        Long companyId = resolveCompanyId();
        MeetingRefreshment refreshment = meetingRefreshmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Refreshment not found"));
        if (!companyId.equals(refreshment.getCompanyId())) {
            throw new ResourceNotFoundException("Refreshment not found");
        }
        return refreshment;
    }

    private MeetingVendor resolveActiveVendor(Long vendorId, Long companyId) {
        if (vendorId == null) {
            return null;
        }
        MeetingVendor vendor = meetingVendorRepository.findById(vendorId)
                .orElseThrow(() -> new BadRequestException("Default vendor not found"));
        if (!companyId.equals(vendor.getCompanyId())) {
            throw new BadRequestException("Default vendor not found");
        }
        if (!Boolean.TRUE.equals(vendor.getActive())) {
            throw new BadRequestException("Default vendor must be active");
        }
        return vendor;
    }

    private void validateRequired(String refreshmentCode,
                                  RefreshmentCategory category,
                                  String itemName,
                                  BigDecimal unitPrice,
                                  Boolean active) {
        if (!StringUtils.hasText(refreshmentCode) || category == null || !StringUtils.hasText(itemName)
                || unitPrice == null || active == null) {
            throw new BadRequestException("Invalid refreshment request");
        }
        validateUnitPrice(unitPrice);
    }

    private void validateUnitPrice(BigDecimal unitPrice) {
        if (unitPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Unit price must be 0 or greater");
        }
    }

    private BadRequestException duplicateRefreshmentException(DataIntegrityViolationException ex) {
        String message = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage();
        String normalized = message != null ? message.toLowerCase(Locale.ENGLISH) : "";
        if (normalized.contains("idx_meeting_refreshments_company_code") || normalized.contains("refreshment_code")) {
            return new BadRequestException("Refreshment code already exists");
        }
        return new BadRequestException("Refreshment code already exists");
    }

    private boolean matches(MeetingRefreshment refreshment, MeetingRefreshmentFilterSearch search) {
        if (search == null) {
            return true;
        }
        return contains(refreshment.getRefreshmentCode(), search.getRefreshmentCode())
                && contains(refreshment.getCategory() != null ? refreshment.getCategory().name() : null, search.getCategory())
                && contains(refreshment.getItemName(), search.getItemName())
                && (search.getDefaultVendorId() == null || search.getDefaultVendorId().equals(refreshment.getDefaultVendorId()))
                && contains(refreshment.getDefaultVendorName(), search.getDefaultVendorName())
                && matchesActive(search.getActive(), search.getStatus(), refreshment.getActive());
    }


    private boolean matchesActive(Boolean activeFilter, String statusFilter, Boolean actualActive) {
        if (StringUtils.hasText(statusFilter)) {
            String normalized = statusFilter.trim().toUpperCase(Locale.ENGLISH);
            if ("ACTIVE".equals(normalized)) {
                return Boolean.TRUE.equals(actualActive);
            }
            if ("INACTIVE".equals(normalized) || "DEACTIVE".equals(normalized) || "DEACTIVATED".equals(normalized)) {
                return !Boolean.TRUE.equals(actualActive);
            }
        }
        if (Boolean.FALSE.equals(activeFilter)) {
            return !Boolean.TRUE.equals(actualActive);
        }
        return true;
    }
    private Comparator<MeetingRefreshment> resolveComparator(MeetingRefreshmentFilterRequest request) {
        String column = request != null && StringUtils.hasText(request.getSortColumn())
                ? request.getSortColumn().trim()
                : "lastModifiedDate";
        boolean desc = request == null || !"ASC".equalsIgnoreCase(request.getSortDirection());
        Comparator<MeetingRefreshment> comparator = switch (column) {
            case "refreshmentCode" -> Comparator.comparing(MeetingRefreshment::getRefreshmentCode, Comparator.nullsLast(String::compareToIgnoreCase));
            case "category" -> Comparator.comparing(refreshment -> refreshment.getCategory() != null ? refreshment.getCategory().name() : null,
                    Comparator.nullsLast(String::compareToIgnoreCase));
            case "itemName" -> Comparator.comparing(MeetingRefreshment::getItemName, Comparator.nullsLast(String::compareToIgnoreCase));
            case "defaultVendorName" -> Comparator.comparing(MeetingRefreshment::getDefaultVendorName, Comparator.nullsLast(String::compareToIgnoreCase));
            case "unitPrice" -> Comparator.comparing(MeetingRefreshment::getUnitPrice, Comparator.nullsLast(BigDecimal::compareTo));
            case "active" -> Comparator.comparing(MeetingRefreshment::getActive, Comparator.nullsLast(Boolean::compareTo));
            default -> Comparator.comparing(MeetingRefreshment::getLastModifiedDate, Comparator.nullsLast(LocalDateTime::compareTo));
        };
        return desc ? comparator.reversed() : comparator;
    }

    private MeetingRefreshmentDto toDto(MeetingRefreshment refreshment) {
        return MeetingRefreshmentDto.builder()
                .id(refreshment.getId())
                .companyId(refreshment.getCompanyId())
                .companyCode(refreshment.getCompanyCode())
                .companyName(refreshment.getCompanyName())
                .refreshmentCode(refreshment.getRefreshmentCode())
                .category(refreshment.getCategory())
                .categoryDescription(refreshment.getCategory() != null ? toTitleCase(refreshment.getCategory().name()) : null)
                .itemName(refreshment.getItemName())
                .defaultVendorId(refreshment.getDefaultVendorId())
                .defaultVendorCode(refreshment.getDefaultVendorCode())
                .defaultVendorName(refreshment.getDefaultVendorName())
                .unitPrice(refreshment.getUnitPrice())
                .description(refreshment.getDescription())
                .active(refreshment.getActive())
                .activeStatusDescription(Boolean.TRUE.equals(refreshment.getActive()) ? "Active" : "Inactive")
                .selectable(Boolean.TRUE.equals(refreshment.getActive()))
                .createdDate(refreshment.getCreatedDate())
                .lastModifiedDate(refreshment.getLastModifiedDate())
                .createdBy(refreshment.getCreatedBy())
                .lastModifiedBy(refreshment.getLastModifiedBy())
                .build();
    }

    private MeetingVendorDto toVendorDto(MeetingVendor vendor) {
        return MeetingVendorDto.builder()
                .id(vendor.getId())
                .companyId(vendor.getCompanyId())
                .companyCode(vendor.getCompanyCode())
                .companyName(vendor.getCompanyName())
                .vendorCode(vendor.getVendorCode())
                .vendorName(vendor.getVendorName())
                .vendorType(vendor.getVendorType())
                .vendorTypeDescription(vendor.getVendorType() != null ? toTitleCase(vendor.getVendorType().name()) : null)
                .contactPerson(vendor.getContactPerson())
                .contactNumber(vendor.getContactNumber())
                .emailAddress(vendor.getEmailAddress())
                .active(vendor.getActive())
                .activeStatusDescription(Boolean.TRUE.equals(vendor.getActive()) ? "Active" : "Inactive")
                .selectable(Boolean.TRUE.equals(vendor.getActive()))
                .build();
    }

    private MessageResponseDTO<MeetingRefreshmentDto> success(String message, MeetingRefreshmentDto data) {
        return MessageResponseDTO.<MeetingRefreshmentDto>builder()
                .success(true)
                .message(message)
                .data(data)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    private String normalizeCode(String value) {
        return value.trim().toUpperCase(Locale.ENGLISH);
    }

    private Long resolveCompanyId() {
        if (CompanyContext.getCompanyId() != null) {
            return CompanyContext.getCompanyId();
        }
        return defaultCompanyId != null ? defaultCompanyId : 1L;
    }

    private CompanyLookup resolveCompany(Long companyId) {
        if (companyId == null) {
            return null;
        }
        return companyLookupRepository.findById(companyId).orElse(null);
    }

    private String resolveCompanyCode(CompanyLookup company) {
        if (StringUtils.hasText(CompanyContext.getCompanyCode())) {
            return CompanyContext.getCompanyCode().trim();
        }
        return company != null ? trimToNull(company.getCode()) : null;
    }

    private String resolveCompanyName(CompanyLookup company) {
        if (StringUtils.hasText(CompanyContext.getCompanyName())) {
            return CompanyContext.getCompanyName().trim();
        }
        return company != null ? trimToNull(company.getDescription()) : null;
    }

    private boolean contains(String source, String expected) {
        if (!StringUtils.hasText(expected)) {
            return true;
        }
        return source != null && source.toLowerCase(Locale.ENGLISH).contains(expected.trim().toLowerCase(Locale.ENGLISH));
    }

    private List<ReferenceOptionDto> activeStatusOptions() {
        return List.of(
                ReferenceOptionDto.builder().code("ACTIVE").description("Active").build(),
                ReferenceOptionDto.builder().code("INACTIVE").description("Inactive").build()
        );
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String toTitleCase(String value) {
        String[] parts = value.toLowerCase(Locale.ENGLISH).split("_");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (part.isBlank()) {
                continue;
            }
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return builder.toString();
    }
}

