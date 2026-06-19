package com.zynolo_nexus.meeting_room_booking_service.service.impl;

import com.zynolo_nexus.meeting_room_booking_service.context.CompanyContext;
import com.zynolo_nexus.meeting_room_booking_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingSupportServiceActiveStatusRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingSupportServiceCreateRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingSupportServiceFilterRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingSupportServiceFilterSearch;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingSupportServiceReferenceDataRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingSupportServiceUpdateRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingSupportServiceDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingSupportServiceFilterResultDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingSupportServicePrivilegesDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingSupportServiceReferenceDataDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.ReferenceOptionDto;
import com.zynolo_nexus.meeting_room_booking_service.enums.SupportAssignedTeam;
import com.zynolo_nexus.meeting_room_booking_service.enums.SupportServiceCategory;
import com.zynolo_nexus.meeting_room_booking_service.exception.BadRequestException;
import com.zynolo_nexus.meeting_room_booking_service.exception.ResourceNotFoundException;
import com.zynolo_nexus.meeting_room_booking_service.model.CompanyLookup;
import com.zynolo_nexus.meeting_room_booking_service.model.MeetingSupportService;
import com.zynolo_nexus.meeting_room_booking_service.repository.CompanyLookupRepository;
import com.zynolo_nexus.meeting_room_booking_service.repository.MeetingSupportServiceRepository;
import com.zynolo_nexus.meeting_room_booking_service.service.MeetingSupportServiceMasterService;
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
public class MeetingSupportServiceMasterServiceImpl implements MeetingSupportServiceMasterService {

    private static final String PAGE_CODE = "MBM_SYSC_SESM";

    private final MeetingSupportServiceRepository meetingSupportServiceRepository;
    private final CompanyLookupRepository companyLookupRepository;
    private final PagePrivilegeResolver pagePrivilegeResolver;

    @Value("${app.default.company-id:1}")
    private Long defaultCompanyId;

    @Override
    @Transactional
    public MessageResponseDTO<MeetingSupportServiceDto> create(MeetingSupportServiceCreateRequest request) {
        if (request == null) {
            throw new BadRequestException("Invalid support service request");
        }
        validateRequired(request.getServiceCode(), request.getServiceName(), request.getServiceCategory(),
                request.getChargeable(), request.getDefaultCharge(), request.getActive());

        Long companyId = resolveCompanyId();
        String serviceCode = normalizeCode(request.getServiceCode());
        String serviceName = request.getServiceName().trim();

        if (meetingSupportServiceRepository.existsByCompanyIdAndServiceCodeIgnoreCase(companyId, serviceCode)) {
            throw new BadRequestException("Service code already exists");
        }
        if (meetingSupportServiceRepository.existsByCompanyIdAndServiceNameIgnoreCase(companyId, serviceName)) {
            throw new BadRequestException("Service name already exists");
        }

        CompanyLookup company = resolveCompany(companyId);
        MeetingSupportService service = MeetingSupportService.builder()
                .companyId(companyId)
                .companyCode(resolveCompanyCode(company))
                .companyName(resolveCompanyName(company))
                .serviceCode(serviceCode)
                .serviceName(serviceName)
                .serviceCategory(request.getServiceCategory())
                .assignedTeam(request.getAssignedTeam())
                .chargeable(request.getChargeable())
                .defaultCharge(request.getDefaultCharge())
                .description(trimToNull(request.getDescription()))
                .active(request.getActive())
                .createdBy(trimToNull(request.getUsername()))
                .lastModifiedBy(trimToNull(request.getUsername()))
                .build();

        return success("Support service created successfully", toDto(meetingSupportServiceRepository.save(service)));
    }

    @Override
    @Transactional
    public MessageResponseDTO<MeetingSupportServiceDto> update(MeetingSupportServiceUpdateRequest request) {
        if (request == null || request.getId() == null) {
            throw new BadRequestException("Invalid support service update request");
        }
        validateDefaultCharge(request.getDefaultCharge());

        MeetingSupportService service = findService(request.getId());
        Long companyId = service.getCompanyId();

        if (StringUtils.hasText(request.getServiceCode())) {
            String serviceCode = normalizeCode(request.getServiceCode());
            if (!serviceCode.equalsIgnoreCase(service.getServiceCode())
                    && meetingSupportServiceRepository.existsByCompanyIdAndServiceCodeIgnoreCaseAndIdNot(companyId, serviceCode, service.getId())) {
                throw new BadRequestException("Service code already exists");
            }
            service.setServiceCode(serviceCode);
        }
        if (StringUtils.hasText(request.getServiceName())) {
            String serviceName = request.getServiceName().trim();
            if (!serviceName.equalsIgnoreCase(service.getServiceName())
                    && meetingSupportServiceRepository.existsByCompanyIdAndServiceNameIgnoreCaseAndIdNot(companyId, serviceName, service.getId())) {
                throw new BadRequestException("Service name already exists");
            }
            service.setServiceName(serviceName);
        }
        if (request.getServiceCategory() != null) {
            service.setServiceCategory(request.getServiceCategory());
        }
        if (request.getAssignedTeam() != null) {
            service.setAssignedTeam(request.getAssignedTeam());
        }
        if (request.getChargeable() != null) {
            service.setChargeable(request.getChargeable());
        }
        if (request.getDefaultCharge() != null) {
            service.setDefaultCharge(request.getDefaultCharge());
        }
        if (request.getDescription() != null) {
            service.setDescription(trimToNull(request.getDescription()));
        }
        if (request.getActive() != null) {
            service.setActive(request.getActive());
        }
        if (StringUtils.hasText(request.getUsername())) {
            service.setLastModifiedBy(request.getUsername().trim());
        }

        return success("Support service updated successfully", toDto(meetingSupportServiceRepository.save(service)));
    }

    @Override
    @Transactional(readOnly = true)
    public MessageResponseDTO<MeetingSupportServiceDto> view(Long id) {
        if (id == null) {
            throw new BadRequestException("Invalid support service view request");
        }
        return success("Support service retrieved successfully", toDto(findService(id)));
    }

    @Override
    @Transactional
    public MessageResponseDTO<MeetingSupportServiceDto> updateActiveStatus(MeetingSupportServiceActiveStatusRequest request) {
        if (request == null || request.getId() == null || request.getActive() == null) {
            throw new BadRequestException("Invalid support service active status request");
        }
        MeetingSupportService service = findService(request.getId());
        service.setActive(request.getActive());
        if (StringUtils.hasText(request.getUsername())) {
            service.setLastModifiedBy(request.getUsername().trim());
        }
        String message = request.getActive() ? "Support service activated successfully" : "Support service deactivated successfully";
        return success(message, toDto(meetingSupportServiceRepository.save(service)));
    }

    @Override
    @Transactional(readOnly = true)
    public MessageResponseDTO<MeetingSupportServiceFilterResultDto> filterList(MeetingSupportServiceFilterRequest request) {
        int page = request != null && request.getPage() != null && request.getPage() >= 0 ? request.getPage() : 0;
        int size = request != null && request.getSize() != null && request.getSize() > 0 ? request.getSize() : 10;
        MeetingSupportServiceFilterSearch search = request != null ? request.getSearch() : null;
        Long companyId = resolveCompanyId();

        List<MeetingSupportServiceDto> filtered = meetingSupportServiceRepository.findAll().stream()
                .filter(service -> companyId.equals(service.getCompanyId()))
                .filter(service -> matches(service, search))
                .sorted(resolveComparator(request))
                .map(this::toDto)
                .toList();

        int from = Math.min(page * size, filtered.size());
        int to = Math.min(from + size, filtered.size());
        List<MeetingSupportServiceDto> content = filtered.subList(from, to);
        int totalPages = filtered.isEmpty() ? 0 : (int) Math.ceil((double) filtered.size() / size);

        return MessageResponseDTO.<MeetingSupportServiceFilterResultDto>builder()
                .success(true)
                .message("Support services filtered successfully")
                .data(MeetingSupportServiceFilterResultDto.builder()
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
    public MessageResponseDTO<MeetingSupportServiceReferenceDataDto> referenceData(MeetingSupportServiceReferenceDataRequest request) {
        Long companyId = resolveCompanyId();
        CompanyLookup company = resolveCompany(companyId);
        var pagePrivileges = pagePrivilegeResolver.resolve(request != null ? request.getUsername() : null, PAGE_CODE);

        return MessageResponseDTO.<MeetingSupportServiceReferenceDataDto>builder()
                .success(true)
                .message("Support service reference data loaded successfully")
                .data(MeetingSupportServiceReferenceDataDto.builder()
                        .companyId(companyId)
                        .companyCode(resolveCompanyCode(company))
                        .companyName(resolveCompanyName(company))
                        .serviceCategories(List.of(SupportServiceCategory.values()).stream()
                                .map(category -> ReferenceOptionDto.builder()
                                        .code(category.name())
                                        .description(toTitleCase(category.name()))
                                        .build())
                                .toList())
                        .assignedTeams(List.of(SupportAssignedTeam.values()).stream()
                                .map(team -> ReferenceOptionDto.builder()
                                        .code(team.name())
                                        .description(toTitleCase(team.name()))
                                        .build())
                                .toList())
                        .privileges(MeetingSupportServicePrivilegesDto.builder()
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

    private MeetingSupportService findService(Long id) {
        Long companyId = resolveCompanyId();
        MeetingSupportService service = meetingSupportServiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Support service not found"));
        if (!companyId.equals(service.getCompanyId())) {
            throw new ResourceNotFoundException("Support service not found");
        }
        return service;
    }

    private void validateRequired(String serviceCode,
                                  String serviceName,
                                  SupportServiceCategory category,
                                  Boolean chargeable,
                                  BigDecimal defaultCharge,
                                  Boolean active) {
        if (!StringUtils.hasText(serviceCode) || !StringUtils.hasText(serviceName)
                || category == null || chargeable == null || active == null) {
            throw new BadRequestException("Invalid support service request");
        }
        validateDefaultCharge(defaultCharge);
    }

    private void validateDefaultCharge(BigDecimal defaultCharge) {
        if (defaultCharge != null && defaultCharge.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Default charge cannot be negative");
        }
    }

    private boolean matches(MeetingSupportService service, MeetingSupportServiceFilterSearch search) {
        if (search == null) {
            return true;
        }
        return contains(service.getServiceCode(), search.getServiceCode())
                && contains(service.getServiceName(), search.getServiceName())
                && contains(service.getServiceCategory() != null ? service.getServiceCategory().name() : null, search.getServiceCategory())
                && contains(service.getAssignedTeam() != null ? service.getAssignedTeam().name() : null, search.getAssignedTeam())
                && (search.getChargeable() == null || search.getChargeable().equals(service.getChargeable()))
                && matchesActive(search.getActive(), search.getStatus(), service.getActive());
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
    private Comparator<MeetingSupportService> resolveComparator(MeetingSupportServiceFilterRequest request) {
        String column = request != null && StringUtils.hasText(request.getSortColumn())
                ? request.getSortColumn().trim()
                : "lastModifiedDate";
        boolean desc = request == null || !"ASC".equalsIgnoreCase(request.getSortDirection());
        Comparator<MeetingSupportService> comparator = switch (column) {
            case "serviceCode" -> Comparator.comparing(MeetingSupportService::getServiceCode, Comparator.nullsLast(String::compareToIgnoreCase));
            case "serviceName" -> Comparator.comparing(MeetingSupportService::getServiceName, Comparator.nullsLast(String::compareToIgnoreCase));
            case "serviceCategory" -> Comparator.comparing(service -> service.getServiceCategory() != null ? service.getServiceCategory().name() : null,
                    Comparator.nullsLast(String::compareToIgnoreCase));
            case "assignedTeam" -> Comparator.comparing(service -> service.getAssignedTeam() != null ? service.getAssignedTeam().name() : null,
                    Comparator.nullsLast(String::compareToIgnoreCase));
            case "chargeable" -> Comparator.comparing(MeetingSupportService::getChargeable, Comparator.nullsLast(Boolean::compareTo));
            case "defaultCharge" -> Comparator.comparing(MeetingSupportService::getDefaultCharge, Comparator.nullsLast(BigDecimal::compareTo));
            case "active" -> Comparator.comparing(MeetingSupportService::getActive, Comparator.nullsLast(Boolean::compareTo));
            default -> Comparator.comparing(MeetingSupportService::getLastModifiedDate, Comparator.nullsLast(LocalDateTime::compareTo));
        };
        return desc ? comparator.reversed() : comparator;
    }

    private MeetingSupportServiceDto toDto(MeetingSupportService service) {
        return MeetingSupportServiceDto.builder()
                .id(service.getId())
                .companyId(service.getCompanyId())
                .companyCode(service.getCompanyCode())
                .companyName(service.getCompanyName())
                .serviceCode(service.getServiceCode())
                .serviceName(service.getServiceName())
                .serviceCategory(service.getServiceCategory())
                .serviceCategoryDescription(service.getServiceCategory() != null ? toTitleCase(service.getServiceCategory().name()) : null)
                .assignedTeam(service.getAssignedTeam())
                .assignedTeamDescription(service.getAssignedTeam() != null ? toTitleCase(service.getAssignedTeam().name()) : null)
                .chargeable(service.getChargeable())
                .defaultCharge(service.getDefaultCharge())
                .description(service.getDescription())
                .active(service.getActive())
                .activeStatusDescription(Boolean.TRUE.equals(service.getActive()) ? "Active" : "Inactive")
                .selectable(Boolean.TRUE.equals(service.getActive()))
                .createdDate(service.getCreatedDate())
                .lastModifiedDate(service.getLastModifiedDate())
                .createdBy(service.getCreatedBy())
                .lastModifiedBy(service.getLastModifiedBy())
                .build();
    }

    private MessageResponseDTO<MeetingSupportServiceDto> success(String message, MeetingSupportServiceDto data) {
        return MessageResponseDTO.<MeetingSupportServiceDto>builder()
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

