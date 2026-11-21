package com.zynolo_nexus.fleet_service.service.impl;

import com.zynolo_nexus.fleet_service.context.CompanyContext;
import com.zynolo_nexus.fleet_service.dto.request.RequestQueueReferenceDataRequest;
import com.zynolo_nexus.fleet_service.dto.request.RequestQueueUpdateRequest;
import com.zynolo_nexus.fleet_service.dto.request.TransportRequestFilterRequest;
import com.zynolo_nexus.fleet_service.dto.request.TransportRequestFilterSearch;
import com.zynolo_nexus.fleet_service.dto.request.TransportRequestViewRequest;
import com.zynolo_nexus.fleet_service.dto.response.ReferenceOptionDto;
import com.zynolo_nexus.fleet_service.dto.response.RequestQueuePrivilegesDto;
import com.zynolo_nexus.fleet_service.dto.response.RequestQueueReferenceDataDto;
import com.zynolo_nexus.fleet_service.dto.response.TransportRequestDto;
import com.zynolo_nexus.fleet_service.dto.response.TransportRequestFilterResultDto;
import com.zynolo_nexus.fleet_service.dto.response.TransportRequestListItemDto;
import com.zynolo_nexus.fleet_service.enums.TransportRequestStatus;
import com.zynolo_nexus.fleet_service.exception.BadRequestException;
import com.zynolo_nexus.fleet_service.exception.ResourceNotFoundException;
import com.zynolo_nexus.fleet_service.model.FleetTransportRequest;
import com.zynolo_nexus.fleet_service.repository.FleetTransportRequestRepository;
import com.zynolo_nexus.fleet_service.service.RequestQueueService;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class RequestQueueServiceImpl implements RequestQueueService {

    private static final String PAGE_CODE = "FLRQ";

    private final FleetTransportRequestRepository transportRequestRepository;
    private final PagePrivilegeResolver pagePrivilegeResolver;

    @Value("${app.default.company-id:1}")
    private Long defaultCompanyId;

    @Override
    @Transactional(readOnly = true)
    public RequestQueueReferenceDataDto getReferenceData(RequestQueueReferenceDataRequest request) {
        var privileges = pagePrivilegeResolver.resolve(
                request != null ? request.getUsername() : null,
                PAGE_CODE
        );
        return RequestQueueReferenceDataDto.builder()
                .defaultStatus(List.of(
                        option(TransportRequestStatus.DRAFT.name(), "Draft"),
                        option(TransportRequestStatus.SUBMITTED.name(), "Submitted"),
                        option(TransportRequestStatus.ASSIGNED.name(), "Assigned"),
                        option(TransportRequestStatus.CANCELLED.name(), "Cancelled")
                ))
                .privileges(RequestQueuePrivilegesDto.builder()
                        .view(privileges.isView())
                        .search(privileges.isSearch())
                        .update(privileges.isUpdate())
                        .build())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public TransportRequestFilterResultDto filterList(TransportRequestFilterRequest request) {
        Long companyId = requireCompanyId();
        Pageable pageable = PageRequest.of(
                request.getPage(),
                request.getSize(),
                Sort.by(resolveDirection(request.getSortDirection()), resolveSortColumn(request.getSortColumn()))
        );

        Page<FleetTransportRequest> page = transportRequestRepository.findAll(
                buildSpecification(companyId, request.getSearch()),
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

    @Override
    @Transactional(readOnly = true)
    public TransportRequestDto view(TransportRequestViewRequest request) {
        return toDto(getByIdForCompany(request.getId()));
    }

    @Override
    @Transactional
    public TransportRequestDto update(RequestQueueUpdateRequest request) {
        FleetTransportRequest entity = getByIdForCompany(request.getId());
        if (entity.getStatus() != TransportRequestStatus.SUBMITTED) {
            throw new BadRequestException("Dispatcher notes can only be updated for SUBMITTED requests");
        }
        entity.setDispatcherNotes(trimToNull(request.getDispatcherNotes()));
        applyAudit(entity, request.getUsername());
        return toDto(transportRequestRepository.save(entity));
    }

    private FleetTransportRequest getByIdForCompany(Long id) {
        Long companyId = requireCompanyId();
        return transportRequestRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Transport request not found with ID: " + id));
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

    private Specification<FleetTransportRequest> buildSpecification(
            Long companyId,
            TransportRequestFilterSearch search
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("companyId"), companyId));

            boolean statusProvided = search != null && hasText(search.getStatus());
            if (statusProvided) {
                predicates.add(criteriaBuilder.equal(root.get("status"), parseStatus(search.getStatus())));
            } else {
                // Queue default: open submitted work
                predicates.add(criteriaBuilder.equal(root.get("status"), TransportRequestStatus.SUBMITTED));
            }

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

    private void applyAudit(FleetTransportRequest entity, String username) {
        entity.setLastModifiedDate(LocalDateTime.now());
        entity.setLastModifiedBy(hasText(username) ? username.trim() : "system");
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
            return "submittedAt";
        }
        return switch (sortColumn) {
            case "requestNumber", "requesterUsername", "pickupLocation", "dropLocation", "requestedPickupAt",
                 "passengerCount", "status", "submittedAt", "createdDate", "lastModifiedDate", "createdBy" -> sortColumn;
            default -> "submittedAt";
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
            case ASSIGNED -> "Assigned";
            case CANCELLED -> "Cancelled";
        };
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