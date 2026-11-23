package com.zynolo_nexus.fleet_service.service.impl;

import com.zynolo_nexus.fleet_service.context.CompanyContext;
import com.zynolo_nexus.fleet_service.dto.request.ReceiptFilterRequest;
import com.zynolo_nexus.fleet_service.dto.request.ReceiptFilterSearch;
import com.zynolo_nexus.fleet_service.dto.request.ReceiptReferenceDataRequest;
import com.zynolo_nexus.fleet_service.dto.request.ReceiptViewRequest;
import com.zynolo_nexus.fleet_service.dto.response.ReceiptDto;
import com.zynolo_nexus.fleet_service.dto.response.ReceiptFilterResultDto;
import com.zynolo_nexus.fleet_service.dto.response.ReceiptListItemDto;
import com.zynolo_nexus.fleet_service.dto.response.ReceiptPrivilegesDto;
import com.zynolo_nexus.fleet_service.dto.response.ReceiptReferenceDataDto;
import com.zynolo_nexus.fleet_service.dto.response.ReferenceOptionDto;
import com.zynolo_nexus.fleet_service.enums.FarePaymentMethod;
import com.zynolo_nexus.fleet_service.enums.FareStatus;
import com.zynolo_nexus.fleet_service.exception.BadRequestException;
import com.zynolo_nexus.fleet_service.exception.ResourceNotFoundException;
import com.zynolo_nexus.fleet_service.model.FleetTransportRequest;
import com.zynolo_nexus.fleet_service.model.FleetTrip;
import com.zynolo_nexus.fleet_service.model.FleetTripFare;
import com.zynolo_nexus.fleet_service.repository.FleetTransportRequestRepository;
import com.zynolo_nexus.fleet_service.repository.FleetTripFareRepository;
import com.zynolo_nexus.fleet_service.repository.FleetTripRepository;
import com.zynolo_nexus.fleet_service.service.ReceiptService;
import com.zynolo_nexus.fleet_service.service.support.PagePrivilegeResolver;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ReceiptServiceImpl implements ReceiptService {

    private static final String PAGE_CODE = "FLRC";

    private final FleetTripFareRepository tripFareRepository;
    private final FleetTripRepository fleetTripRepository;
    private final FleetTransportRequestRepository transportRequestRepository;
    private final PagePrivilegeResolver pagePrivilegeResolver;

    @Value("${app.default.company-id:1}")
    private Long defaultCompanyId;

    @Override
    @Transactional(readOnly = true)
    public ReceiptReferenceDataDto getReferenceData(ReceiptReferenceDataRequest request) {
        requireUsername(request != null ? request.getUsername() : null);
        var privileges = pagePrivilegeResolver.resolve(
                request != null ? request.getUsername() : null,
                PAGE_CODE
        );
        return ReceiptReferenceDataDto.builder()
                .defaultPaymentMethod(List.of(
                        option(FarePaymentMethod.CASH.name(), "Cash"),
                        option(FarePaymentMethod.CARD.name(), "Card"),
                        option(FarePaymentMethod.COMPANY.name(), "Company"),
                        option(FarePaymentMethod.OTHER.name(), "Other")
                ))
                .defaultStatus(List.of(
                        option(FareStatus.RECORDED.name(), "Recorded"),
                        option(FareStatus.CANCELLED.name(), "Cancelled")
                ))
                .privileges(ReceiptPrivilegesDto.builder()
                        .view(privileges.isView())
                        .search(privileges.isSearch())
                        .build())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ReceiptFilterResultDto filterList(ReceiptFilterRequest request) {
        Long companyId = requireCompanyId();
        String actor = requireUsername(request.getUsername());
        Pageable pageable = PageRequest.of(
                request.getPage(),
                request.getSize(),
                Sort.by(resolveDirection(request.getSortDirection()), resolveSortColumn(request.getSortColumn()))
        );

        Page<FleetTripFare> page = tripFareRepository.findAll(
                buildSpecification(companyId, actor, request.getSearch()),
                pageable
        );

        Map<Long, FleetTrip> trips = loadTrips(companyId, page.getContent());
        Map<Long, FleetTransportRequest> requests = loadRequests(companyId, trips);

        List<ReceiptListItemDto> content = page.getContent().stream()
                .map(fare -> {
                    FleetTrip trip = trips.get(fare.getTripId());
                    FleetTransportRequest transportRequest = trip != null
                            ? requests.get(trip.getTransportRequestId())
                            : null;
                    return toListItemDto(fare, trip, transportRequest);
                })
                .toList();

        return ReceiptFilterResultDto.builder()
                .content(content)
                .size(content.size())
                .totalRecords(page.getTotalElements())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ReceiptDto view(ReceiptViewRequest request) {
        String actor = requireUsername(request.getUsername());
        FleetTripFare fare = getFareForCompany(request.getId());
        FleetTrip trip = getTripForCompany(fare.getTripId());
        FleetTransportRequest transportRequest = transportRequestRepository
                .findByIdAndCompanyId(trip.getTransportRequestId(), fare.getCompanyId())
                .orElse(null);

        if (Boolean.TRUE.equals(request.getOwnOnly())) {
            if (transportRequest == null
                    || !actor.equalsIgnoreCase(transportRequest.getRequesterUsername())) {
                throw new BadRequestException("You can only view your own receipts");
            }
        }

        return toDto(fare, trip, transportRequest);
    }

    private Specification<FleetTripFare> buildSpecification(
            Long companyId,
            String actor,
            ReceiptFilterSearch search
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("companyId"), companyId));

            // Default to RECORDED receipts when status not supplied
            if (search == null || !hasText(search.getStatus())) {
                predicates.add(criteriaBuilder.equal(root.get("status"), FareStatus.RECORDED));
            } else {
                predicates.add(criteriaBuilder.equal(root.get("status"), parseFareStatus(search.getStatus())));
            }

            if (search != null) {
                if (hasText(search.getReceiptNumber())) {
                    predicates.add(criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("fareNumber")),
                            like(search.getReceiptNumber())
                    ));
                }
                if (hasText(search.getPaymentMethod())) {
                    predicates.add(criteriaBuilder.equal(
                            root.get("paymentMethod"),
                            parsePaymentMethod(search.getPaymentMethod())
                    ));
                }
                if (hasText(search.getTripNumber())) {
                    predicates.add(root.get("tripId").in(tripIdsByTripNumber(query, criteriaBuilder, companyId, search.getTripNumber())));
                }

                boolean ownOnly = Boolean.TRUE.equals(search.getOwnOnly());
                if (ownOnly) {
                    predicates.add(root.get("tripId").in(
                            tripIdsByRequester(query, criteriaBuilder, companyId, actor)
                    ));
                } else if (hasText(search.getRequesterUsername())) {
                    predicates.add(root.get("tripId").in(
                            tripIdsByRequester(query, criteriaBuilder, companyId, search.getRequesterUsername().trim())
                    ));
                }
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private Subquery<Long> tripIdsByTripNumber(
            jakarta.persistence.criteria.CriteriaQuery<?> query,
            jakarta.persistence.criteria.CriteriaBuilder criteriaBuilder,
            Long companyId,
            String tripNumber
    ) {
        Subquery<Long> tripIds = query.subquery(Long.class);
        Root<FleetTrip> tripRoot = tripIds.from(FleetTrip.class);
        tripIds.select(tripRoot.get("id"));
        tripIds.where(
                criteriaBuilder.equal(tripRoot.get("companyId"), companyId),
                criteriaBuilder.like(
                        criteriaBuilder.lower(tripRoot.get("tripNumber")),
                        like(tripNumber)
                )
        );
        return tripIds;
    }

    private Subquery<Long> tripIdsByRequester(
            jakarta.persistence.criteria.CriteriaQuery<?> query,
            jakarta.persistence.criteria.CriteriaBuilder criteriaBuilder,
            Long companyId,
            String requesterUsername
    ) {
        Subquery<Long> requestIds = query.subquery(Long.class);
        Root<FleetTransportRequest> requestRoot = requestIds.from(FleetTransportRequest.class);
        requestIds.select(requestRoot.get("id"));
        requestIds.where(
                criteriaBuilder.equal(requestRoot.get("companyId"), companyId),
                criteriaBuilder.equal(
                        criteriaBuilder.lower(requestRoot.get("requesterUsername")),
                        requesterUsername.toLowerCase(Locale.ROOT)
                )
        );

        Subquery<Long> tripIds = query.subquery(Long.class);
        Root<FleetTrip> tripRoot = tripIds.from(FleetTrip.class);
        tripIds.select(tripRoot.get("id"));
        tripIds.where(
                criteriaBuilder.equal(tripRoot.get("companyId"), companyId),
                tripRoot.get("transportRequestId").in(requestIds)
        );
        return tripIds;
    }

    private FleetTripFare getFareForCompany(Long id) {
        Long companyId = requireCompanyId();
        return tripFareRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Receipt not found with ID: " + id));
    }

    private FleetTrip getTripForCompany(Long id) {
        Long companyId = requireCompanyId();
        return fleetTripRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found with ID: " + id));
    }

    private Map<Long, FleetTrip> loadTrips(Long companyId, List<FleetTripFare> fares) {
        Map<Long, FleetTrip> map = new HashMap<>();
        fares.stream()
                .map(FleetTripFare::getTripId)
                .filter(Objects::nonNull)
                .distinct()
                .forEach(id -> fleetTripRepository.findByIdAndCompanyId(id, companyId)
                        .ifPresent(t -> map.put(id, t)));
        return map;
    }

    private Map<Long, FleetTransportRequest> loadRequests(Long companyId, Map<Long, FleetTrip> trips) {
        Map<Long, FleetTransportRequest> map = new HashMap<>();
        trips.values().stream()
                .map(FleetTrip::getTransportRequestId)
                .filter(Objects::nonNull)
                .distinct()
                .forEach(id -> transportRequestRepository.findByIdAndCompanyId(id, companyId)
                        .ifPresent(r -> map.put(id, r)));
        return map;
    }

    private ReceiptDto toDto(FleetTripFare fare, FleetTrip trip, FleetTransportRequest request) {
        return ReceiptDto.builder()
                .id(fare.getId())
                .companyId(fare.getCompanyId())
                .receiptNumber(fare.getFareNumber())
                .tripId(fare.getTripId())
                .tripNumber(trip != null ? trip.getTripNumber() : null)
                .transportRequestId(trip != null ? trip.getTransportRequestId() : null)
                .requestNumber(request != null ? request.getRequestNumber() : null)
                .requesterUsername(request != null ? request.getRequesterUsername() : null)
                .pickupLocation(request != null ? request.getPickupLocation() : null)
                .dropLocation(request != null ? request.getDropLocation() : null)
                .passengerCount(request != null ? request.getPassengerCount() : null)
                .amount(fare.getAmount())
                .currency(fare.getCurrency())
                .paymentMethod(fare.getPaymentMethod().name())
                .paymentMethodDescription(toPaymentMethodDescription(fare.getPaymentMethod()))
                .notes(fare.getNotes())
                .recordedAt(fare.getRecordedAt())
                .recordedBy(fare.getRecordedBy())
                .status(fare.getStatus().name())
                .statusDescription(toStatusDescription(fare.getStatus()))
                .createdDate(fare.getCreatedDate())
                .lastModifiedDate(fare.getLastModifiedDate())
                .createdBy(fare.getCreatedBy())
                .lastModifiedBy(fare.getLastModifiedBy())
                .build();
    }

    private ReceiptListItemDto toListItemDto(
            FleetTripFare fare,
            FleetTrip trip,
            FleetTransportRequest request
    ) {
        return ReceiptListItemDto.builder()
                .id(fare.getId())
                .receiptNumber(fare.getFareNumber())
                .tripId(fare.getTripId())
                .tripNumber(trip != null ? trip.getTripNumber() : null)
                .requesterUsername(request != null ? request.getRequesterUsername() : null)
                .pickupLocation(request != null ? request.getPickupLocation() : null)
                .dropLocation(request != null ? request.getDropLocation() : null)
                .amount(fare.getAmount())
                .currency(fare.getCurrency())
                .paymentMethod(fare.getPaymentMethod().name())
                .paymentMethodDescription(toPaymentMethodDescription(fare.getPaymentMethod()))
                .recordedAt(fare.getRecordedAt())
                .status(fare.getStatus().name())
                .statusDescription(toStatusDescription(fare.getStatus()))
                .createdDate(fare.getCreatedDate())
                .lastModifiedDate(fare.getLastModifiedDate())
                .createdBy(fare.getCreatedBy())
                .lastModifiedBy(fare.getLastModifiedBy())
                .build();
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

    private FarePaymentMethod parsePaymentMethod(String value) {
        try {
            return FarePaymentMethod.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (Exception ex) {
            throw new BadRequestException("Invalid paymentMethod: " + value);
        }
    }

    private FareStatus parseFareStatus(String value) {
        try {
            return FareStatus.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (Exception ex) {
            throw new BadRequestException("Invalid status: " + value);
        }
    }

    private String resolveSortColumn(String sortColumn) {
        if (!hasText(sortColumn)) {
            return "recordedAt";
        }
        return switch (sortColumn) {
            case "fareNumber", "receiptNumber" -> "fareNumber";
            case "amount", "currency", "paymentMethod", "recordedAt", "status",
                 "createdDate", "lastModifiedDate", "createdBy" -> sortColumn;
            default -> "recordedAt";
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

    private String toPaymentMethodDescription(FarePaymentMethod method) {
        return switch (method) {
            case CASH -> "Cash";
            case CARD -> "Card";
            case COMPANY -> "Company";
            case OTHER -> "Other";
        };
    }

    private String toStatusDescription(FareStatus status) {
        return switch (status) {
            case RECORDED -> "Recorded";
            case CANCELLED -> "Cancelled";
        };
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String like(String value) {
        return "%" + value.trim().toLowerCase(Locale.ROOT) + "%";
    }
}
