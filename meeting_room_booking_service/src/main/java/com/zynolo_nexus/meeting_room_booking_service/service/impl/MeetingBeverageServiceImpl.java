package com.zynolo_nexus.meeting_room_booking_service.service.impl;

import com.zynolo_nexus.meeting_room_booking_service.context.CompanyContext;
import com.zynolo_nexus.meeting_room_booking_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingBeverageActiveStatusRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingBeverageCreateRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingBeverageFilterRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingBeverageFilterSearch;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingBeverageReferenceDataRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingBeverageUpdateRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingBeverageDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingBeverageFilterResultDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingBeveragePrivilegesDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingBeverageReferenceDataDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingVendorDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.ReferenceOptionDto;
import com.zynolo_nexus.meeting_room_booking_service.exception.BadRequestException;
import com.zynolo_nexus.meeting_room_booking_service.exception.ResourceNotFoundException;
import com.zynolo_nexus.meeting_room_booking_service.model.CompanyLookup;
import com.zynolo_nexus.meeting_room_booking_service.model.MeetingBeverage;
import com.zynolo_nexus.meeting_room_booking_service.model.MeetingVendor;
import com.zynolo_nexus.meeting_room_booking_service.repository.CompanyLookupRepository;
import com.zynolo_nexus.meeting_room_booking_service.repository.MeetingBeverageRepository;
import com.zynolo_nexus.meeting_room_booking_service.repository.MeetingVendorRepository;
import com.zynolo_nexus.meeting_room_booking_service.service.MeetingBeverageService;
import com.zynolo_nexus.meeting_room_booking_service.service.support.PagePrivilegeResolver;
import lombok.RequiredArgsConstructor;
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
public class MeetingBeverageServiceImpl implements MeetingBeverageService {

    private static final String PAGE_CODE = "MBM_SYSC_BEVM";

    private final MeetingBeverageRepository meetingBeverageRepository;
    private final MeetingVendorRepository meetingVendorRepository;
    private final CompanyLookupRepository companyLookupRepository;
    private final PagePrivilegeResolver pagePrivilegeResolver;

    @Value("${app.default.company-id:1}")
    private Long defaultCompanyId;

    @Override
    @Transactional
    public MessageResponseDTO<MeetingBeverageDto> create(MeetingBeverageCreateRequest request) {
        if (request == null) {
            throw new BadRequestException("Invalid beverage request");
        }
        validateRequired(request.getBeverageCode(), request.getBeverageName(), request.getUnitPrice(), request.getActive());

        Long companyId = resolveCompanyId();
        String beverageCode = normalizeCode(request.getBeverageCode());
        String beverageName = request.getBeverageName().trim();

        if (meetingBeverageRepository.existsByCompanyIdAndBeverageCodeIgnoreCase(companyId, beverageCode)) {
            throw new BadRequestException("Beverage code already exists");
        }
        if (meetingBeverageRepository.existsByCompanyIdAndBeverageNameIgnoreCase(companyId, beverageName)) {
            throw new BadRequestException("Beverage name already exists");
        }

        MeetingVendor defaultVendor = resolveActiveVendor(request.getDefaultVendorId(), companyId);
        CompanyLookup company = resolveCompany(companyId);

        MeetingBeverage beverage = MeetingBeverage.builder()
                .companyId(companyId)
                .companyCode(resolveCompanyCode(company))
                .companyName(resolveCompanyName(company))
                .beverageCode(beverageCode)
                .beverageName(beverageName)
                .defaultVendorId(defaultVendor != null ? defaultVendor.getId() : null)
                .defaultVendorCode(defaultVendor != null ? defaultVendor.getVendorCode() : null)
                .defaultVendorName(defaultVendor != null ? defaultVendor.getVendorName() : null)
                .unitPrice(request.getUnitPrice())
                .description(trimToNull(request.getDescription()))
                .active(request.getActive())
                .createdBy(trimToNull(request.getUsername()))
                .lastModifiedBy(trimToNull(request.getUsername()))
                .build();

        return success("Beverage created successfully", toDto(meetingBeverageRepository.save(beverage)));
    }

    @Override
    @Transactional
    public MessageResponseDTO<MeetingBeverageDto> update(MeetingBeverageUpdateRequest request) {
        if (request == null || request.getId() == null) {
            throw new BadRequestException("Invalid beverage update request");
        }
        MeetingBeverage beverage = findBeverage(request.getId());
        Long companyId = beverage.getCompanyId();

        if (StringUtils.hasText(request.getBeverageCode())) {
            String beverageCode = normalizeCode(request.getBeverageCode());
            if (!beverageCode.equalsIgnoreCase(beverage.getBeverageCode())
                    && meetingBeverageRepository.existsByCompanyIdAndBeverageCodeIgnoreCaseAndIdNot(
                    companyId, beverageCode, beverage.getId())) {
                throw new BadRequestException("Beverage code already exists");
            }
            beverage.setBeverageCode(beverageCode);
        }
        if (StringUtils.hasText(request.getBeverageName())) {
            String beverageName = request.getBeverageName().trim();
            if (!beverageName.equalsIgnoreCase(beverage.getBeverageName())
                    && meetingBeverageRepository.existsByCompanyIdAndBeverageNameIgnoreCaseAndIdNot(
                    companyId, beverageName, beverage.getId())) {
                throw new BadRequestException("Beverage name already exists");
            }
            beverage.setBeverageName(beverageName);
        }
        if (request.getDefaultVendorId() != null) {
            MeetingVendor defaultVendor = resolveActiveVendor(request.getDefaultVendorId(), companyId);
            beverage.setDefaultVendorId(defaultVendor.getId());
            beverage.setDefaultVendorCode(defaultVendor.getVendorCode());
            beverage.setDefaultVendorName(defaultVendor.getVendorName());
        }
        if (request.getUnitPrice() != null) {
            validateUnitPrice(request.getUnitPrice());
            beverage.setUnitPrice(request.getUnitPrice());
        }
        if (request.getDescription() != null) {
            beverage.setDescription(trimToNull(request.getDescription()));
        }
        if (request.getActive() != null) {
            beverage.setActive(request.getActive());
        }
        if (StringUtils.hasText(request.getUsername())) {
            beverage.setLastModifiedBy(request.getUsername().trim());
        }

        return success("Beverage updated successfully", toDto(meetingBeverageRepository.save(beverage)));
    }

    @Override
    @Transactional(readOnly = true)
    public MessageResponseDTO<MeetingBeverageDto> view(Long id) {
        if (id == null) {
            throw new BadRequestException("Invalid beverage view request");
        }
        return success("Beverage retrieved successfully", toDto(findBeverage(id)));
    }

    @Override
    @Transactional
    public MessageResponseDTO<MeetingBeverageDto> updateActiveStatus(MeetingBeverageActiveStatusRequest request) {
        if (request == null || request.getId() == null || request.getActive() == null) {
            throw new BadRequestException("Invalid beverage active status request");
        }
        MeetingBeverage beverage = findBeverage(request.getId());
        beverage.setActive(request.getActive());
        if (StringUtils.hasText(request.getUsername())) {
            beverage.setLastModifiedBy(request.getUsername().trim());
        }
        String message = request.getActive() ? "Beverage activated successfully" : "Beverage deactivated successfully";
        return success(message, toDto(meetingBeverageRepository.save(beverage)));
    }

    @Override
    @Transactional(readOnly = true)
    public MessageResponseDTO<MeetingBeverageFilterResultDto> filterList(MeetingBeverageFilterRequest request) {
        int page = request != null && request.getPage() != null && request.getPage() >= 0 ? request.getPage() : 0;
        int size = request != null && request.getSize() != null && request.getSize() > 0 ? request.getSize() : 10;
        MeetingBeverageFilterSearch search = request != null ? request.getSearch() : null;
        Long companyId = resolveCompanyId();

        List<MeetingBeverageDto> filtered = meetingBeverageRepository.findAll().stream()
                .filter(beverage -> companyId.equals(beverage.getCompanyId()))
                .filter(beverage -> matches(beverage, search))
                .sorted(resolveComparator(request))
                .map(this::toDto)
                .toList();

        int from = Math.min(page * size, filtered.size());
        int to = Math.min(from + size, filtered.size());
        List<MeetingBeverageDto> content = filtered.subList(from, to);
        int totalPages = filtered.isEmpty() ? 0 : (int) Math.ceil((double) filtered.size() / size);

        return MessageResponseDTO.<MeetingBeverageFilterResultDto>builder()
                .success(true)
                .message("Beverages filtered successfully")
                .data(MeetingBeverageFilterResultDto.builder()
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
    public MessageResponseDTO<MeetingBeverageReferenceDataDto> referenceData(MeetingBeverageReferenceDataRequest request) {
        Long companyId = resolveCompanyId();
        CompanyLookup company = resolveCompany(companyId);
        var pagePrivileges = pagePrivilegeResolver.resolve(request != null ? request.getUsername() : null, PAGE_CODE);

        List<ReferenceOptionDto> commonBeverages = List.of(
                ReferenceOptionDto.builder().code("WATER_BOTTLE").description("Water Bottle").build(),
                ReferenceOptionDto.builder().code("TEA").description("Tea").build(),
                ReferenceOptionDto.builder().code("PLAIN_TEA").description("Plain Tea").build(),
                ReferenceOptionDto.builder().code("COFFEE").description("Coffee").build()
        );

        List<MeetingVendorDto> activeVendors = meetingVendorRepository.findAll().stream()
                .filter(vendor -> companyId.equals(vendor.getCompanyId()))
                .filter(vendor -> Boolean.TRUE.equals(vendor.getActive()))
                .sorted(Comparator.comparing(MeetingVendor::getVendorName, Comparator.nullsLast(String::compareToIgnoreCase)))
                .map(this::toVendorDto)
                .toList();

        return MessageResponseDTO.<MeetingBeverageReferenceDataDto>builder()
                .success(true)
                .message("Beverage reference data loaded successfully")
                .data(MeetingBeverageReferenceDataDto.builder()
                        .companyId(companyId)
                        .companyCode(resolveCompanyCode(company))
                        .companyName(resolveCompanyName(company))
                        .commonBeverages(commonBeverages)
                        .activeVendors(activeVendors)
                        .privileges(MeetingBeveragePrivilegesDto.builder()
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

    private MeetingBeverage findBeverage(Long id) {
        Long companyId = resolveCompanyId();
        MeetingBeverage beverage = meetingBeverageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Beverage not found"));
        if (!companyId.equals(beverage.getCompanyId())) {
            throw new ResourceNotFoundException("Beverage not found");
        }
        return beverage;
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

    private void validateRequired(String beverageCode, String beverageName, BigDecimal unitPrice, Boolean active) {
        if (!StringUtils.hasText(beverageCode) || !StringUtils.hasText(beverageName) || unitPrice == null || active == null) {
            throw new BadRequestException("Invalid beverage request");
        }
        validateUnitPrice(unitPrice);
    }

    private void validateUnitPrice(BigDecimal unitPrice) {
        if (unitPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Unit price must be 0 or greater");
        }
    }

    private boolean matches(MeetingBeverage beverage, MeetingBeverageFilterSearch search) {
        if (search == null) {
            return true;
        }
        return contains(beverage.getBeverageCode(), search.getBeverageCode())
                && contains(beverage.getBeverageName(), search.getBeverageName())
                && (search.getDefaultVendorId() == null || search.getDefaultVendorId().equals(beverage.getDefaultVendorId()))
                && contains(beverage.getDefaultVendorName(), search.getDefaultVendorName())
                && (search.getActive() == null || search.getActive().equals(beverage.getActive()));
    }

    private Comparator<MeetingBeverage> resolveComparator(MeetingBeverageFilterRequest request) {
        String column = request != null && StringUtils.hasText(request.getSortColumn())
                ? request.getSortColumn().trim()
                : "lastModifiedDate";
        boolean desc = request == null || !"ASC".equalsIgnoreCase(request.getSortDirection());
        Comparator<MeetingBeverage> comparator = switch (column) {
            case "beverageCode" -> Comparator.comparing(MeetingBeverage::getBeverageCode, Comparator.nullsLast(String::compareToIgnoreCase));
            case "beverageName" -> Comparator.comparing(MeetingBeverage::getBeverageName, Comparator.nullsLast(String::compareToIgnoreCase));
            case "defaultVendorName" -> Comparator.comparing(MeetingBeverage::getDefaultVendorName, Comparator.nullsLast(String::compareToIgnoreCase));
            case "unitPrice" -> Comparator.comparing(MeetingBeverage::getUnitPrice, Comparator.nullsLast(BigDecimal::compareTo));
            case "active" -> Comparator.comparing(MeetingBeverage::getActive, Comparator.nullsLast(Boolean::compareTo));
            default -> Comparator.comparing(MeetingBeverage::getLastModifiedDate, Comparator.nullsLast(LocalDateTime::compareTo));
        };
        return desc ? comparator.reversed() : comparator;
    }

    private MeetingBeverageDto toDto(MeetingBeverage beverage) {
        return MeetingBeverageDto.builder()
                .id(beverage.getId())
                .companyId(beverage.getCompanyId())
                .companyCode(beverage.getCompanyCode())
                .companyName(beverage.getCompanyName())
                .beverageCode(beverage.getBeverageCode())
                .beverageName(beverage.getBeverageName())
                .defaultVendorId(beverage.getDefaultVendorId())
                .defaultVendorCode(beverage.getDefaultVendorCode())
                .defaultVendorName(beverage.getDefaultVendorName())
                .unitPrice(beverage.getUnitPrice())
                .description(beverage.getDescription())
                .active(beverage.getActive())
                .activeStatusDescription(Boolean.TRUE.equals(beverage.getActive()) ? "Active" : "Inactive")
                .selectable(Boolean.TRUE.equals(beverage.getActive()))
                .createdDate(beverage.getCreatedDate())
                .lastModifiedDate(beverage.getLastModifiedDate())
                .createdBy(beverage.getCreatedBy())
                .lastModifiedBy(beverage.getLastModifiedBy())
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

    private MessageResponseDTO<MeetingBeverageDto> success(String message, MeetingBeverageDto data) {
        return MessageResponseDTO.<MeetingBeverageDto>builder()
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
