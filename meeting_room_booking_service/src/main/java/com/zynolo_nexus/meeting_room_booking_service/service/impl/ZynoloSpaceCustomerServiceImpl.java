package com.zynolo_nexus.meeting_room_booking_service.service.impl;

import com.zynolo_nexus.meeting_room_booking_service.context.CompanyContext;
import com.zynolo_nexus.meeting_room_booking_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.ZynoloSpaceCustomerActiveStatusRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.ZynoloSpaceCustomerCreateRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.ZynoloSpaceCustomerFilterRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.ZynoloSpaceCustomerFilterSearch;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.ZynoloSpaceCustomerReferenceDataRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.ZynoloSpaceCustomerUpdateRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.ReferenceOptionDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.ZynoloSpaceCustomerDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.ZynoloSpaceCustomerFilterResultDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.ZynoloSpaceCustomerListItemDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.ZynoloSpaceCustomerPrivilegesDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.ZynoloSpaceCustomerReferenceDataDto;
import com.zynolo_nexus.meeting_room_booking_service.exception.BadRequestException;
import com.zynolo_nexus.meeting_room_booking_service.exception.ResourceNotFoundException;
import com.zynolo_nexus.meeting_room_booking_service.model.CompanyLookup;
import com.zynolo_nexus.meeting_room_booking_service.model.ZynoloSpaceCustomer;
import com.zynolo_nexus.meeting_room_booking_service.repository.CompanyLookupRepository;
import com.zynolo_nexus.meeting_room_booking_service.repository.ZynoloSpaceCustomerRepository;
import com.zynolo_nexus.meeting_room_booking_service.service.ZynoloSpaceCustomerService;
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
public class ZynoloSpaceCustomerServiceImpl implements ZynoloSpaceCustomerService {

    private static final String PAGE_CODE = "MBM_SYSC_ZSPC";
    private static final Pattern NUMERIC_PATTERN = Pattern.compile("^[0-9]+$");

    private final ZynoloSpaceCustomerRepository zynoloSpaceCustomerRepository;
    private final CompanyLookupRepository companyLookupRepository;
    private final PagePrivilegeResolver pagePrivilegeResolver;

    @Value("${app.default.company-id:1}")
    private Long defaultCompanyId;

    @Override
    @Transactional
    public MessageResponseDTO<ZynoloSpaceCustomerDto> create(ZynoloSpaceCustomerCreateRequest request) {
        if (request == null) {
            throw new BadRequestException("Invalid Zynolo Space customer request");
        }
        validateRequired(request.getCustomerCode(), request.getCustomerCompanyName(), request.getActive());
        validateContactNumber(request.getContactNumber());

        Long companyId = resolveCompanyId();
        String customerCode = normalizeCode(request.getCustomerCode());
        String customerCompanyName = request.getCustomerCompanyName().trim();

        if (zynoloSpaceCustomerRepository.existsByCompanyIdAndCustomerCodeIgnoreCase(companyId, customerCode)) {
            throw new BadRequestException("Customer code already exists");
        }
        if (zynoloSpaceCustomerRepository.existsByCompanyIdAndCustomerCompanyNameIgnoreCase(companyId, customerCompanyName)) {
            throw new BadRequestException("Customer company name already exists");
        }

        CompanyLookup company = resolveCompany(companyId);
        ZynoloSpaceCustomer customer = ZynoloSpaceCustomer.builder()
                .companyId(companyId)
                .companyCode(resolveCompanyCode(company))
                .companyName(resolveCompanyName(company))
                .customerCode(customerCode)
                .customerCompanyName(customerCompanyName)
                .contactPerson(trimToNull(request.getContactPerson()))
                .contactNumber(trimToNull(request.getContactNumber()))
                .emailAddress(trimToNull(request.getEmailAddress()))
                .address(trimToNull(request.getAddress()))
                .remarks(trimToNull(request.getRemarks()))
                .active(request.getActive())
                .createdBy(trimToNull(request.getUsername()))
                .lastModifiedBy(trimToNull(request.getUsername()))
                .build();

        return success("Zynolo Space customer created successfully", toDto(zynoloSpaceCustomerRepository.save(customer)));
    }

    @Override
    @Transactional
    public MessageResponseDTO<ZynoloSpaceCustomerDto> update(ZynoloSpaceCustomerUpdateRequest request) {
        if (request == null || request.getId() == null) {
            throw new BadRequestException("Invalid Zynolo Space customer update request");
        }
        validateContactNumber(request.getContactNumber());

        ZynoloSpaceCustomer customer = findCustomer(request.getId());
        Long companyId = customer.getCompanyId();

        if (StringUtils.hasText(request.getCustomerCode())) {
            String customerCode = normalizeCode(request.getCustomerCode());
            if (!customerCode.equalsIgnoreCase(customer.getCustomerCode())
                    && zynoloSpaceCustomerRepository.existsByCompanyIdAndCustomerCodeIgnoreCaseAndIdNot(companyId, customerCode, customer.getId())) {
                throw new BadRequestException("Customer code already exists");
            }
            customer.setCustomerCode(customerCode);
        }
        if (StringUtils.hasText(request.getCustomerCompanyName())) {
            String customerCompanyName = request.getCustomerCompanyName().trim();
            if (!customerCompanyName.equalsIgnoreCase(customer.getCustomerCompanyName())
                    && zynoloSpaceCustomerRepository.existsByCompanyIdAndCustomerCompanyNameIgnoreCaseAndIdNot(companyId, customerCompanyName, customer.getId())) {
                throw new BadRequestException("Customer company name already exists");
            }
            customer.setCustomerCompanyName(customerCompanyName);
        }
        if (request.getContactPerson() != null) {
            customer.setContactPerson(trimToNull(request.getContactPerson()));
        }
        if (request.getContactNumber() != null) {
            customer.setContactNumber(trimToNull(request.getContactNumber()));
        }
        if (request.getEmailAddress() != null) {
            customer.setEmailAddress(trimToNull(request.getEmailAddress()));
        }
        if (request.getAddress() != null) {
            customer.setAddress(trimToNull(request.getAddress()));
        }
        if (request.getRemarks() != null) {
            customer.setRemarks(trimToNull(request.getRemarks()));
        }
        if (request.getActive() != null) {
            customer.setActive(request.getActive());
        }
        if (StringUtils.hasText(request.getUsername())) {
            customer.setLastModifiedBy(request.getUsername().trim());
        }

        return success("Zynolo Space customer updated successfully", toDto(zynoloSpaceCustomerRepository.save(customer)));
    }

    @Override
    @Transactional(readOnly = true)
    public MessageResponseDTO<ZynoloSpaceCustomerDto> view(Long id) {
        if (id == null) {
            throw new BadRequestException("Invalid Zynolo Space customer view request");
        }
        return success("Zynolo Space customer retrieved successfully", toDto(findCustomer(id)));
    }

    @Override
    @Transactional
    public MessageResponseDTO<ZynoloSpaceCustomerDto> updateActiveStatus(ZynoloSpaceCustomerActiveStatusRequest request) {
        if (request == null || request.getId() == null || request.getActive() == null) {
            throw new BadRequestException("Invalid Zynolo Space customer active status request");
        }
        ZynoloSpaceCustomer customer = findCustomer(request.getId());
        customer.setActive(request.getActive());
        if (StringUtils.hasText(request.getUsername())) {
            customer.setLastModifiedBy(request.getUsername().trim());
        }
        String message = request.getActive() ? "Zynolo Space customer activated successfully" : "Zynolo Space customer deactivated successfully";
        return success(message, toDto(zynoloSpaceCustomerRepository.save(customer)));
    }

    @Override
    @Transactional(readOnly = true)
    public MessageResponseDTO<ZynoloSpaceCustomerFilterResultDto> filterList(ZynoloSpaceCustomerFilterRequest request) {
        int page = request != null && request.getPage() != null && request.getPage() >= 0 ? request.getPage() : 0;
        int size = request != null && request.getSize() != null && request.getSize() > 0 ? request.getSize() : 10;
        ZynoloSpaceCustomerFilterSearch search = request != null ? request.getSearch() : null;
        Long companyId = resolveCompanyId();

        List<ZynoloSpaceCustomerListItemDto> filtered = zynoloSpaceCustomerRepository.findAll().stream()
                .filter(customer -> companyId.equals(customer.getCompanyId()))
                .filter(customer -> matches(customer, search))
                .sorted(resolveComparator(request))
                .map(this::toListItem)
                .toList();

        int from = Math.min(page * size, filtered.size());
        int to = Math.min(from + size, filtered.size());
        List<ZynoloSpaceCustomerListItemDto> content = filtered.subList(from, to);
        int totalPages = filtered.isEmpty() ? 0 : (int) Math.ceil((double) filtered.size() / size);

        return MessageResponseDTO.<ZynoloSpaceCustomerFilterResultDto>builder()
                .success(true)
                .message("Zynolo Space customers filtered successfully")
                .data(ZynoloSpaceCustomerFilterResultDto.builder()
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
    public MessageResponseDTO<ZynoloSpaceCustomerReferenceDataDto> referenceData(ZynoloSpaceCustomerReferenceDataRequest request) {
        Long companyId = resolveCompanyId();
        CompanyLookup company = resolveCompany(companyId);
        var pagePrivileges = pagePrivilegeResolver.resolve(request != null ? request.getUsername() : null, PAGE_CODE);

        return MessageResponseDTO.<ZynoloSpaceCustomerReferenceDataDto>builder()
                .success(true)
                .message("Zynolo Space customer reference data loaded successfully")
                .data(ZynoloSpaceCustomerReferenceDataDto.builder()
                        .companyId(companyId)
                        .companyCode(resolveCompanyCode(company))
                        .companyName(resolveCompanyName(company))
                        .statuses(List.of(
                                ReferenceOptionDto.builder().code("ACTIVE").description("Active").build(),
                                ReferenceOptionDto.builder().code("INACTIVE").description("Inactive").build()
                        ))
                        .privileges(ZynoloSpaceCustomerPrivilegesDto.builder()
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

    private ZynoloSpaceCustomer findCustomer(Long id) {
        Long companyId = resolveCompanyId();
        ZynoloSpaceCustomer customer = zynoloSpaceCustomerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Zynolo Space customer not found"));
        if (!companyId.equals(customer.getCompanyId())) {
            throw new ResourceNotFoundException("Zynolo Space customer not found");
        }
        return customer;
    }

    private void validateRequired(String customerCode, String customerCompanyName, Boolean active) {
        if (!StringUtils.hasText(customerCode) || !StringUtils.hasText(customerCompanyName) || active == null) {
            throw new BadRequestException("Invalid Zynolo Space customer request");
        }
    }

    private void validateContactNumber(String contactNumber) {
        if (StringUtils.hasText(contactNumber) && !NUMERIC_PATTERN.matcher(contactNumber.trim()).matches()) {
            throw new BadRequestException("Contact number must be numeric");
        }
    }

    private boolean matches(ZynoloSpaceCustomer customer, ZynoloSpaceCustomerFilterSearch search) {
        if (search == null) {
            return true;
        }
        return contains(customer.getCustomerCode(), search.getCustomerCode())
                && contains(customer.getCustomerCompanyName(), search.getCustomerCompanyName())
                && contains(customer.getContactPerson(), search.getContactPerson())
                && contains(customer.getContactNumber(), search.getContactNumber())
                && contains(customer.getEmailAddress(), search.getEmailAddress())
                && matchesActive(search.getActive(), search.getStatus(), customer.getActive());
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

    private Comparator<ZynoloSpaceCustomer> resolveComparator(ZynoloSpaceCustomerFilterRequest request) {
        String column = request != null && StringUtils.hasText(request.getSortColumn())
                ? request.getSortColumn().trim()
                : "lastModifiedDate";
        boolean desc = request == null || !"ASC".equalsIgnoreCase(request.getSortDirection());
        Comparator<ZynoloSpaceCustomer> comparator = switch (column) {
            case "customerCode" -> Comparator.comparing(ZynoloSpaceCustomer::getCustomerCode, Comparator.nullsLast(String::compareToIgnoreCase));
            case "customerCompanyName" -> Comparator.comparing(ZynoloSpaceCustomer::getCustomerCompanyName, Comparator.nullsLast(String::compareToIgnoreCase));
            case "contactPerson" -> Comparator.comparing(ZynoloSpaceCustomer::getContactPerson, Comparator.nullsLast(String::compareToIgnoreCase));
            case "contactNumber" -> Comparator.comparing(ZynoloSpaceCustomer::getContactNumber, Comparator.nullsLast(String::compareToIgnoreCase));
            case "emailAddress" -> Comparator.comparing(ZynoloSpaceCustomer::getEmailAddress, Comparator.nullsLast(String::compareToIgnoreCase));
            case "active" -> Comparator.comparing(ZynoloSpaceCustomer::getActive, Comparator.nullsLast(Boolean::compareTo));
            default -> Comparator.comparing(ZynoloSpaceCustomer::getLastModifiedDate, Comparator.nullsLast(LocalDateTime::compareTo));
        };
        return desc ? comparator.reversed() : comparator;
    }

    private ZynoloSpaceCustomerDto toDto(ZynoloSpaceCustomer customer) {
        return ZynoloSpaceCustomerDto.builder()
                .id(customer.getId())
                .companyId(customer.getCompanyId())
                .companyCode(customer.getCompanyCode())
                .companyName(customer.getCompanyName())
                .customerCode(customer.getCustomerCode())
                .customerCompanyName(customer.getCustomerCompanyName())
                .contactPerson(customer.getContactPerson())
                .contactNumber(customer.getContactNumber())
                .emailAddress(customer.getEmailAddress())
                .address(customer.getAddress())
                .remarks(customer.getRemarks())
                .active(customer.getActive())
                .activeStatusDescription(Boolean.TRUE.equals(customer.getActive()) ? "Active" : "Inactive")
                .selectable(Boolean.TRUE.equals(customer.getActive()))
                .createdDate(customer.getCreatedDate())
                .lastModifiedDate(customer.getLastModifiedDate())
                .createdBy(customer.getCreatedBy())
                .lastModifiedBy(customer.getLastModifiedBy())
                .build();
    }

    private ZynoloSpaceCustomerListItemDto toListItem(ZynoloSpaceCustomer customer) {
        return ZynoloSpaceCustomerListItemDto.builder()
                .id(customer.getId())
                .customerCode(customer.getCustomerCode())
                .customerCompanyName(customer.getCustomerCompanyName())
                .contactPerson(customer.getContactPerson())
                .contactNumber(customer.getContactNumber())
                .emailAddress(customer.getEmailAddress())
                .active(customer.getActive())
                .activeStatusDescription(Boolean.TRUE.equals(customer.getActive()) ? "Active" : "Inactive")
                .selectable(Boolean.TRUE.equals(customer.getActive()))
                .createdDate(customer.getCreatedDate())
                .lastModifiedDate(customer.getLastModifiedDate())
                .createdBy(customer.getCreatedBy())
                .lastModifiedBy(customer.getLastModifiedBy())
                .build();
    }

    private MessageResponseDTO<ZynoloSpaceCustomerDto> success(String message, ZynoloSpaceCustomerDto data) {
        return MessageResponseDTO.<ZynoloSpaceCustomerDto>builder()
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
}
