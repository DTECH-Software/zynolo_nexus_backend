package com.zynolo_nexus.fleet_service.service.impl;

import com.zynolo_nexus.fleet_service.context.CompanyContext;
import com.zynolo_nexus.fleet_service.dto.request.TransportRequestCreateRequest;
import com.zynolo_nexus.fleet_service.dto.request.TransportRequestFilterRequest;
import com.zynolo_nexus.fleet_service.dto.request.TransportRequestFilterSearch;
import com.zynolo_nexus.fleet_service.dto.request.TransportRequestReferenceDataRequest;
import com.zynolo_nexus.fleet_service.dto.request.TransportRequestSubmitRequest;
import com.zynolo_nexus.fleet_service.dto.request.TransportRequestUpdateRequest;
import com.zynolo_nexus.fleet_service.dto.request.TransportRequestViewRequest;
import com.zynolo_nexus.fleet_service.dto.response.ReferenceOptionDto;
import com.zynolo_nexus.fleet_service.dto.response.TransportRequestDto;
import com.zynolo_nexus.fleet_service.dto.response.TransportRequestFilterResultDto;
import com.zynolo_nexus.fleet_service.dto.response.TransportRequestListItemDto;
import com.zynolo_nexus.fleet_service.dto.response.TransportRequestPrivilegesDto;
import com.zynolo_nexus.fleet_service.dto.response.TransportRequestReferenceDataDto;
import com.zynolo_nexus.fleet_service.enums.TransportRequestStatus;
import com.zynolo_nexus.fleet_service.exception.BadRequestException;
import com.zynolo_nexus.fleet_service.exception.ResourceNotFoundException;
import com.zynolo_nexus.fleet_service.model.FleetTransportRequest;
import com.zynolo_nexus.fleet_service.repository.FleetTransportRequestRepository;
import com.zynolo_nexus.fleet_service.service.TransportRequestCreateService;
import com.zynolo_nexus.fleet_service.service.support.PagePrivilegeResolver;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class TransportRequestCreateServiceImpl implements TransportRequestCreateService {

    private static final String PAGE_CODE = "FLNR";
    private static final DateTimeFormatter REQUEST_DATE = DateTimeFormatter.BASIC_ISO_DATE;

    private final FleetTransportRequestRepository transportRequestRepository;
    private final PagePrivilegeResolver pagePrivilegeResolver;

    @Value("${app.default.company-id:1}")
    private Long defaultCompanyId;

    @Override
    @Transactional(readOnly = true)
    public TransportRequestReferenceDataDto getReferenceData(TransportRequestReferenceDataRequest request) {
        var privileges = pagePrivilegeResolver.resolve(
                request != null ? request.getUsername() : null,
                PAGE_CODE
        );
        return TransportRequestReferenceDataDto.builder()
                .defaultStatus(List.of(
                        option(TransportRequestStatus.DRAFT.name(), "Draft"),
                        option(TransportRequestStatus.SUBMITTED.name(), "Submitted"),
                        option(TransportRequestStatus.CANCELLED.name(), "Cancelled")
                ))
                .privileges(TransportRequestPrivilegesDto.builder()
                        .add(privileges.isAdd())
                        .update(privileges.isUpdate())
                        .view(privileges.isView())
                        .search(privileges.isSearch())
                        .submit(privileges.isSubmit())
                        .build())
                .build();
    }

    @Override
    @Transactional
    public TransportRequestDto create(TransportRequestCreateRequest request) {
        Long companyId = requireCompanyId();
        String actor = requireUsername(request.getUsername());

        FleetTransportRequest entity = new FleetTransportRequest();
        entity.setCompanyId(companyId);
        entity.setRequestNumber(nextRequestNumber(companyId));
        entity.setRequesterUsername(actor);
        applyMutableFields(
                entity,
                request.getPickupLocation(),
                request.getDropLocation(),
                request.getRequestedPickupAt(),
                request.getPassengerCount(),
                request.getContactMobile(),
                request.getPurpose(),
                request.getNotes()
        );
        entity.setStatus(TransportRequestStatus.DRAFT);
        entity.setSubmittedAt(null);
        applyAudit(entity, actor, true);

        return toDto(transportRequestRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public TransportRequestDto view(TransportRequestViewRequest request) {
        FleetTransportRequest entity = getOwnForCompany(request.getId(), request.getUsername());
        return toDto(entity);
    }

    @Override
    @Transactional
    public TransportRequestDto update(TransportRequestUpdateRequest request) {
        FleetTransportRequest entity = getOwnForCompany(request.getId(), request.getUsername());
        requireDraft(entity);
        applyMutableFields(
                entity,
                request.getPickupLocation(),
                request.getDropLocation(),
                request.getRequestedPickupAt(),
                request.getPassengerCount(),
                request.getContactMobile(),
                request.getPurpose(),
                request.getNotes()
        );
        applyAudit(entity, request.getUsername(), false);
        return toDto(transportRequestRepository.save(entity));
    }

    @Override
    @Transactional
    public TransportRequestDto submit(TransportRequestSubmitRequest request) {
        FleetTransportRequest entity = getOwnForCompany(request.getId(), request.getUsername());
        requireDraft(entity);
        entity.setStatus(TransportRequestStatus.SUBMITTED);
        entity.setSubmittedAt(LocalDateTime.now());
        applyAudit(entity, request.getUsername(), false);
        return toDto(transportRequestRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public TransportRequestFilterResultDto filterList(TransportRequestFilterRequest request) {
        Long companyId = requireCompanyId();
        String actor = requireUsername(request.getUsername());
        Pageable pageable = PageRequest.of(
                request.getPage(),
                request.getSize(),
                Sort.by(resolveDirection(request.getSortDirection()), resolveSortColumn(request.getSortColumn()))
        );

        Page<FleetTransportRequest> page = transportRequestRepository.findAll(
                buildSpecification(companyId, actor, request.getSearch()),
                pageable
        );

        List<TransportRequestListItemDto> content = page.getContent().stream()
                .map(this::toListItemDto)
                .toList();

        return TransportRequestFilterResultDto.builder()
                .content(content)
                .size(content.size())
                .totalRecords(page.getTotalElements())
                .build();
    }

    private FleetTransportRequest getOwnForCompany(Long id, String username) {
        Long companyId = requireCompanyId();
        String actor = requireUsername(username);
        FleetTransportRequest entity = transportRequestRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Transport request not found with ID: " + id));
        if (!actor.equalsIgnoreCase(entity.getRequesterUsername())) {
            throw new BadRequestException("You can only access your own transport requests on this page");
        }
        return entity;
    }

    private void requireDraft(FleetTransportRequest entity) {
        if (entity.getStatus() != TransportRequestStatus.DRAFT) {
            throw new BadRequestException("Only DRAFT requests can be updated or submitted");
        }
    }

    private void applyMutableFields(
            FleetTransportRequest entity,
            String pickupLocation,
            String dropLocation,
            LocalDateTime requestedPickupAt,
            Integer passengerCount,
            String contactMobile,
            String purpose,
            String notes
    ) {
        entity.setPickupLocation(trimRequired(pickupLocation, "pickupLocation"));
        entity.setDropLocation(trimRequired(dropLocation, "dropLocation"));
        if (requestedPickupAt == null) {
            throw new BadRequestException("requestedPickupAt is required");
        }
        entity.setRequestedPickupAt(requestedPickupAt);
        if (passengerCount == null || passengerCount < 1) {
            throw new BadRequestException("passengerCount must be a positive whole number");
        }
        entity.setPassengerCount(passengerCount);
        entity.setContactMobile(trimToNull(contactMobile));
        entity.setPurpose(trimToNull(purpose));
        entity.setNotes(trimToNull(notes));
    }

    private String nextRequestNumber(Long companyId) {
        String datePart = LocalDate.now().format(REQUEST_DATE);
        for (int i = 0; i < 10; i++) {
            int suffix = ThreadLocalRandom.current().nextInt(1000, 10000);
            String candidate = "FLR-" + datePart + "-" + suffix;
            if (!transportRequestRepository.existsByCompanyIdAndRequestNumber(companyId, candidate)) {
                return candidate;
            }
        }
        throw new BadRequestException("Unable to generate request number, please retry");
    }

    private Long requireCompanyId() {
        Long companyId = CompanyContext.getCompanyId();
        if (companyId == null) {
            companyId = defaultCompanyId;
        }
        if (companyId == null) {
            throw new BadRequestException("Company context is required");
        }
        return companyId;
    }

    private String requireUsername(String username) {
        if (!hasText(username)) {
            throw new BadRequestException("username is required");
        }
        return username.trim();
    }

    private Specification<FleetTransportRequest> buildSpecification(
            Long companyId,
            String requesterUsername,
            TransportRequestFilterSearch search
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("companyId"), companyId));
            predicates.add(criteriaBuilder.equal(
                    criteriaBuilder.lower(root.get("requesterUsername")),
                    requesterUsername.toLowerCase(Locale.ROOT)
            ));

            if (search != null) {
                if (hasText(search.getRequestNumber())) {
                    predicates.add(criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("requestNumber")),
                            like(search.getRequestNumber())
                    ));
                }
                if (hasText(search.getRequesterUsername())) {
                    predicates.add(criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("requesterUsername")),
                            like(search.getRequesterUsername())
                    ));
                }
                if (hasText(search.getPickupLocation())) {
                    predicates.add(criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("pickupLocation")),
                            like(search.getPickupLocation())
                    ));
                }
                if (hasText(search.getDropLocation())) {
                    predicates.add(criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("dropLocation")),
                            like(search.getDropLocation())
                    ));
                }
                if (hasText(search.getStatus())) {
                    predicates.add(criteriaBuilder.equal(root.get("status"), parseStatus(search.getStatus())));
                }
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private TransportRequestDto toDto(FleetTransportRequest entity) {
        return TransportRequestDto.builder()
                .id(entity.getId())
                .companyId(entity.getCompanyId())
                .requestNumber(entity.getRequestNumber())
                .requesterUsername(entity.getRequesterUsername())
                .pickupLocation(entity.getPickupLocation())
                .dropLocation(entity.getDropLocation())
                .requestedPickupAt(entity.getRequestedPickupAt())
                .passengerCount(entity.getPassengerCount())
                .contactMobile(entity.getContactMobile())
                .purpose(entity.getPurpose())
                .notes(entity.getNotes())
                .dispatcherNotes(entity.getDispatcherNotes())
                .status(entity.getStatus().name())
                .statusDescription(toStatusDescription(entity.getStatus()))
                .submittedAt(entity.getSubmittedAt())
                .createdDate(entity.getCreatedDate())
                .lastModifiedDate(entity.getLastModifiedDate())
                .createdBy(entity.getCreatedBy())
                .lastModifiedBy(entity.getLastModifiedBy())
                .build();
    }

    private TransportRequestListItemDto toListItemDto(FleetTransportRequest entity) {
        return TransportRequestListItemDto.builder()
                .id(entity.getId())
                .requestNumber(entity.getRequestNumber())
                .requesterUsername(entity.getRequesterUsername())
                .pickupLocation(entity.getPickupLocation())
                .dropLocation(entity.getDropLocation())
                .requestedPickupAt(entity.getRequestedPickupAt())
                .passengerCount(entity.getPassengerCount())
                .status(entity.getStatus().name())
                .statusDescription(toStatusDescription(entity.getStatus()))
                .createdDate(entity.getCreatedDate())
                .lastModifiedDate(entity.getLastModifiedDate())
                .createdBy(entity.getCreatedBy())
                .lastModifiedBy(entity.getLastModifiedBy())
                .build();
    }

    private void applyAudit(FleetTransportRequest entity, String username, boolean create) {
        LocalDateTime now = LocalDateTime.now();
        String actor = hasText(username) ? username.trim() : "system";
        if (create) {
            entity.setCreatedDate(now);
            entity.setCreatedBy(actor);
        }
        entity.setLastModifiedDate(now);
        entity.setLastModifiedBy(actor);
    }

    private TransportRequestStatus parseStatus(String value) {
        try {
            return TransportRequestStatus.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (Exception ex) {
            throw new BadRequestException("Invalid status: " + value);
        }
    }

    private String resolveSortColumn(String sortColumn) {
        if (!hasText(sortColumn)) {
            return "lastModifiedDate";
        }
        return switch (sortColumn) {
            case "requestNumber", "pickupLocation", "dropLocation", "requestedPickupAt",
                 "passengerCount", "status", "createdDate", "lastModifiedDate", "createdBy" -> sortColumn;
            default -> "lastModifiedDate";
        };
    }

    private Sort.Direction resolveDirection(String direction) {
        return "ASC".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
    }

    private ReferenceOptionDto option(String code, String description) {
        return ReferenceOptionDto.builder()
                .code(code)
                .description(description)
                .build();
    }

    private String toStatusDescription(TransportRequestStatus status) {
        return switch (status) {
            case DRAFT -> "Draft";
            case SUBMITTED -> "Submitted";
            case CANCELLED -> "Cancelled";
        };
    }

    private String trimRequired(String value, String field) {
        if (!hasText(value)) {
            throw new BadRequestException(field + " is required");
        }
        return value.trim();
    }

    private String trimToNull(String value) {
        if (!hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String like(String value) {
        return "%" + value.trim().toLowerCase(Locale.ROOT) + "%";
    }
}