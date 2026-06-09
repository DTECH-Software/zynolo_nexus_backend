package com.zynolo_nexus.meeting_room_booking_service.service.impl;

import com.zynolo_nexus.meeting_room_booking_service.context.CompanyContext;
import com.zynolo_nexus.meeting_room_booking_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingVendorActiveStatusRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingVendorCreateRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingVendorFilterRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingVendorFilterSearch;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingVendorReferenceDataRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingVendorUpdateRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingVendorDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingVendorFilterResultDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingVendorPrivilegesDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingVendorReferenceDataDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.ReferenceOptionDto;
import com.zynolo_nexus.meeting_room_booking_service.enums.MeetingVendorType;
import com.zynolo_nexus.meeting_room_booking_service.exception.BadRequestException;
import com.zynolo_nexus.meeting_room_booking_service.exception.ResourceNotFoundException;
import com.zynolo_nexus.meeting_room_booking_service.model.CompanyLookup;
import com.zynolo_nexus.meeting_room_booking_service.model.MeetingVendor;
import com.zynolo_nexus.meeting_room_booking_service.repository.CompanyLookupRepository;
import com.zynolo_nexus.meeting_room_booking_service.repository.MeetingVendorRepository;
import com.zynolo_nexus.meeting_room_booking_service.service.MeetingVendorService;
import com.zynolo_nexus.meeting_room_booking_service.service.support.PagePrivilegeResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class MeetingVendorServiceImpl implements MeetingVendorService {

    private static final String PAGE_CODE = "MBM_SYSC_VENM";
    private static final Pattern NUMERIC_PATTERN = Pattern.compile("^[0-9]+$");

    private final MeetingVendorRepository meetingVendorRepository;
    private final CompanyLookupRepository companyLookupRepository;
    private final PagePrivilegeResolver pagePrivilegeResolver;

    @Value("${app.default.company-id:1}")
    private Long defaultCompanyId;

    @Override
    @Transactional
    public MessageResponseDTO<MeetingVendorDto> create(MeetingVendorCreateRequest request) {
        if (request == null) {
            throw new BadRequestException("Invalid vendor request");
        }
        validateRequired(request.getVendorCode(), request.getVendorName(), request.getVendorType(), request.getActive());
        validateContactNumber(request.getContactNumber());

        Long companyId = resolveCompanyId();
        String vendorCode = normalizeCode(request.getVendorCode());
        String vendorName = request.getVendorName().trim();

        if (meetingVendorRepository.existsByCompanyIdAndVendorCodeIgnoreCase(companyId, vendorCode)) {
            throw new BadRequestException("Vendor code already exists");
        }
        if (meetingVendorRepository.existsByCompanyIdAndVendorNameIgnoreCase(companyId, vendorName)) {
            throw new BadRequestException("Vendor name already exists");
        }

        CompanyLookup company = resolveCompany(companyId);
        MeetingVendor vendor = MeetingVendor.builder()
                .companyId(companyId)
                .companyCode(resolveCompanyCode(company))
                .companyName(resolveCompanyName(company))
                .vendorCode(vendorCode)
                .vendorName(vendorName)
                .vendorType(request.getVendorType())
                .contactPerson(trimToNull(request.getContactPerson()))
                .contactNumber(trimToNull(request.getContactNumber()))
                .emailAddress(trimToNull(request.getEmailAddress()))
                .address(trimToNull(request.getAddress()))
                .remarks(trimToNull(request.getRemarks()))
                .active(request.getActive())
                .createdBy(trimToNull(request.getUsername()))
                .lastModifiedBy(trimToNull(request.getUsername()))
                .build();

        return success("Vendor created successfully", toDto(meetingVendorRepository.save(vendor)));
    }

    @Override
    @Transactional
    public MessageResponseDTO<MeetingVendorDto> update(MeetingVendorUpdateRequest request) {
        if (request == null || request.getId() == null) {
            throw new BadRequestException("Invalid vendor update request");
        }

        validateContactNumber(request.getContactNumber());

        MeetingVendor vendor = findVendor(request.getId());
        Long companyId = vendor.getCompanyId();

        if (StringUtils.hasText(request.getVendorCode())) {
            String vendorCode = normalizeCode(request.getVendorCode());
            if (!vendorCode.equalsIgnoreCase(vendor.getVendorCode())
                    && meetingVendorRepository.existsByCompanyIdAndVendorCodeIgnoreCaseAndIdNot(companyId, vendorCode, vendor.getId())) {
                throw new BadRequestException("Vendor code already exists");
            }
            vendor.setVendorCode(vendorCode);
        }
        if (StringUtils.hasText(request.getVendorName())) {
            String vendorName = request.getVendorName().trim();
            if (!vendorName.equalsIgnoreCase(vendor.getVendorName())
                    && meetingVendorRepository.existsByCompanyIdAndVendorNameIgnoreCaseAndIdNot(companyId, vendorName, vendor.getId())) {
                throw new BadRequestException("Vendor name already exists");
            }
            vendor.setVendorName(vendorName);
        }
        if (request.getVendorType() != null) {
            vendor.setVendorType(request.getVendorType());
        }
        if (request.getContactPerson() != null) {
            vendor.setContactPerson(trimToNull(request.getContactPerson()));
        }
        if (request.getContactNumber() != null) {
            vendor.setContactNumber(trimToNull(request.getContactNumber()));
        }
        if (request.getEmailAddress() != null) {
            vendor.setEmailAddress(trimToNull(request.getEmailAddress()));
        }
        if (request.getAddress() != null) {
            vendor.setAddress(trimToNull(request.getAddress()));
        }
        if (request.getRemarks() != null) {
            vendor.setRemarks(trimToNull(request.getRemarks()));
        }
        if (request.getActive() != null) {
            vendor.setActive(request.getActive());
        }
        if (StringUtils.hasText(request.getUsername())) {
            vendor.setLastModifiedBy(request.getUsername().trim());
        }

        return success("Vendor updated successfully", toDto(meetingVendorRepository.save(vendor)));
    }

    @Override
    @Transactional(readOnly = true)
    public MessageResponseDTO<MeetingVendorDto> view(Long id) {
        if (id == null) {
            throw new BadRequestException("Invalid vendor view request");
        }
        return success("Vendor retrieved successfully", toDto(findVendor(id)));
    }

    @Override
    @Transactional
    public MessageResponseDTO<MeetingVendorDto> updateActiveStatus(MeetingVendorActiveStatusRequest request) {
        if (request == null || request.getId() == null || request.getActive() == null) {
            throw new BadRequestException("Invalid vendor active status request");
        }
        MeetingVendor vendor = findVendor(request.getId());
        vendor.setActive(request.getActive());
        if (StringUtils.hasText(request.getUsername())) {
            vendor.setLastModifiedBy(request.getUsername().trim());
        }
        String message = request.getActive() ? "Vendor activated successfully" : "Vendor deactivated successfully";
        return success(message, toDto(meetingVendorRepository.save(vendor)));
    }

    @Override
    @Transactional(readOnly = true)
    public MessageResponseDTO<MeetingVendorFilterResultDto> filterList(MeetingVendorFilterRequest request) {
        int page = request != null && request.getPage() != null && request.getPage() >= 0 ? request.getPage() : 0;
        int size = request != null && request.getSize() != null && request.getSize() > 0 ? request.getSize() : 10;
        MeetingVendorFilterSearch search = request != null ? request.getSearch() : null;
        Long companyId = resolveCompanyId();

        List<MeetingVendorDto> filtered = meetingVendorRepository.findAll().stream()
                .filter(vendor -> companyId.equals(vendor.getCompanyId()))
                .filter(vendor -> matches(vendor, search))
                .sorted(resolveComparator(request))
                .map(this::toDto)
                .toList();

        int from = Math.min(page * size, filtered.size());
        int to = Math.min(from + size, filtered.size());
        List<MeetingVendorDto> content = filtered.subList(from, to);
        int totalPages = filtered.isEmpty() ? 0 : (int) Math.ceil((double) filtered.size() / size);

        return MessageResponseDTO.<MeetingVendorFilterResultDto>builder()
                .success(true)
                .message("Vendors filtered successfully")
                .data(MeetingVendorFilterResultDto.builder()
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
    public MessageResponseDTO<MeetingVendorReferenceDataDto> referenceData(MeetingVendorReferenceDataRequest request) {
        List<ReferenceOptionDto> vendorTypes = List.of(MeetingVendorType.values()).stream()
                .map(type -> ReferenceOptionDto.builder()
                        .code(type.name())
                        .description(toTitleCase(type.name()))
                        .build())
                .toList();

        Long companyId = resolveCompanyId();
        CompanyLookup company = resolveCompany(companyId);
        var pagePrivileges = pagePrivilegeResolver.resolve(request != null ? request.getUsername() : null, PAGE_CODE);

        return MessageResponseDTO.<MeetingVendorReferenceDataDto>builder()
                .success(true)
                .message("Vendor reference data loaded successfully")
                .data(MeetingVendorReferenceDataDto.builder()
                        .companyId(companyId)
                        .companyCode(resolveCompanyCode(company))
                        .companyName(resolveCompanyName(company))
                        .vendorTypes(vendorTypes)
                        .privileges(MeetingVendorPrivilegesDto.builder()
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

    private MeetingVendor findVendor(Long id) {
        Long companyId = resolveCompanyId();
        MeetingVendor vendor = meetingVendorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));
        if (!companyId.equals(vendor.getCompanyId())) {
            throw new ResourceNotFoundException("Vendor not found");
        }
        return vendor;
    }

    private void validateRequired(String vendorCode, String vendorName, MeetingVendorType vendorType, Boolean active) {
        if (!StringUtils.hasText(vendorCode) || !StringUtils.hasText(vendorName) || vendorType == null || active == null) {
            throw new BadRequestException("Invalid vendor request");
        }
    }

    private void validateContactNumber(String contactNumber) {
        if (StringUtils.hasText(contactNumber) && !NUMERIC_PATTERN.matcher(contactNumber.trim()).matches()) {
            throw new BadRequestException("Contact number must be numeric");
        }
    }

    private boolean matches(MeetingVendor vendor, MeetingVendorFilterSearch search) {
        if (search == null) {
            return true;
        }
        return contains(vendor.getVendorCode(), search.getVendorCode())
                && contains(vendor.getVendorName(), search.getVendorName())
                && contains(vendor.getVendorType() != null ? vendor.getVendorType().name() : null, search.getVendorType())
                && contains(vendor.getContactPerson(), search.getContactPerson())
                && contains(vendor.getContactNumber(), search.getContactNumber())
                && (search.getActive() == null || search.getActive().equals(vendor.getActive()));
    }

    private Comparator<MeetingVendor> resolveComparator(MeetingVendorFilterRequest request) {
        String column = request != null && StringUtils.hasText(request.getSortColumn())
                ? request.getSortColumn().trim()
                : "lastModifiedDate";
        boolean desc = request == null || !"ASC".equalsIgnoreCase(request.getSortDirection());
        Comparator<MeetingVendor> comparator = switch (column) {
            case "vendorCode" -> Comparator.comparing(MeetingVendor::getVendorCode, Comparator.nullsLast(String::compareToIgnoreCase));
            case "vendorName" -> Comparator.comparing(MeetingVendor::getVendorName, Comparator.nullsLast(String::compareToIgnoreCase));
            case "vendorType" -> Comparator.comparing(vendor -> vendor.getVendorType() != null ? vendor.getVendorType().name() : null,
                    Comparator.nullsLast(String::compareToIgnoreCase));
            case "contactPerson" -> Comparator.comparing(MeetingVendor::getContactPerson, Comparator.nullsLast(String::compareToIgnoreCase));
            case "contactNumber" -> Comparator.comparing(MeetingVendor::getContactNumber, Comparator.nullsLast(String::compareToIgnoreCase));
            case "active" -> Comparator.comparing(MeetingVendor::getActive, Comparator.nullsLast(Boolean::compareTo));
            default -> Comparator.comparing(MeetingVendor::getLastModifiedDate, Comparator.nullsLast(LocalDateTime::compareTo));
        };
        return desc ? comparator.reversed() : comparator;
    }

    private MeetingVendorDto toDto(MeetingVendor vendor) {
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
                .address(vendor.getAddress())
                .remarks(vendor.getRemarks())
                .active(vendor.getActive())
                .activeStatusDescription(Boolean.TRUE.equals(vendor.getActive()) ? "Active" : "Inactive")
                .selectable(Boolean.TRUE.equals(vendor.getActive()))
                .createdDate(vendor.getCreatedDate())
                .lastModifiedDate(vendor.getLastModifiedDate())
                .createdBy(vendor.getCreatedBy())
                .lastModifiedBy(vendor.getLastModifiedBy())
                .build();
    }

    private MessageResponseDTO<MeetingVendorDto> success(String message, MeetingVendorDto data) {
        return MessageResponseDTO.<MeetingVendorDto>builder()
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
