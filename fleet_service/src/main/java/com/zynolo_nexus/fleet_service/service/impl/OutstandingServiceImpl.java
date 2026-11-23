package com.zynolo_nexus.fleet_service.service.impl;

import com.zynolo_nexus.fleet_service.context.CompanyContext;
import com.zynolo_nexus.fleet_service.dto.request.OutstandingFilterRequest;
import com.zynolo_nexus.fleet_service.dto.request.OutstandingFilterSearch;
import com.zynolo_nexus.fleet_service.dto.request.OutstandingReferenceDataRequest;
import com.zynolo_nexus.fleet_service.dto.request.OutstandingViewRequest;
import com.zynolo_nexus.fleet_service.dto.response.OutstandingDto;
import com.zynolo_nexus.fleet_service.dto.response.OutstandingFilterResultDto;
import com.zynolo_nexus.fleet_service.dto.response.OutstandingListItemDto;
import com.zynolo_nexus.fleet_service.dto.response.OutstandingPrivilegesDto;
import com.zynolo_nexus.fleet_service.dto.response.OutstandingReferenceDataDto;
import com.zynolo_nexus.fleet_service.dto.response.ReferenceOptionDto;
import com.zynolo_nexus.fleet_service.enums.OutstandingItemType;
import com.zynolo_nexus.fleet_service.enums.TransportRequestStatus;
import com.zynolo_nexus.fleet_service.enums.TripStatus;
import com.zynolo_nexus.fleet_service.exception.BadRequestException;
import com.zynolo_nexus.fleet_service.exception.ResourceNotFoundException;
import com.zynolo_nexus.fleet_service.model.FleetDriver;
import com.zynolo_nexus.fleet_service.model.FleetTransportRequest;
import com.zynolo_nexus.fleet_service.model.FleetTrip;
import com.zynolo_nexus.fleet_service.model.FleetTripFare;
import com.zynolo_nexus.fleet_service.model.FleetVehicle;
import com.zynolo_nexus.fleet_service.repository.FleetDriverRepository;
import com.zynolo_nexus.fleet_service.repository.FleetTransportRequestRepository;
import com.zynolo_nexus.fleet_service.repository.FleetTripFareRepository;
import com.zynolo_nexus.fleet_service.repository.FleetTripRepository;
import com.zynolo_nexus.fleet_service.repository.FleetVehicleRepository;
import com.zynolo_nexus.fleet_service.service.OutstandingService;
import com.zynolo_nexus.fleet_service.service.support.PagePrivilegeResolver;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class OutstandingServiceImpl implements OutstandingService {

    private static final String PAGE_CODE = "FLOU";
    private static final int MAX_FETCH_PER_TYPE = 200;

    private final FleetTransportRequestRepository transportRequestRepository;
    private final FleetTripRepository fleetTripRepository;
    private final FleetTripFareRepository tripFareRepository;
    private final FleetVehicleRepository fleetVehicleRepository;
    private final FleetDriverRepository fleetDriverRepository;
    private final PagePrivilegeResolver pagePrivilegeResolver;

    @Value("${app.default.company-id:1}")
    private Long defaultCompanyId;

    @Override
    @Transactional(readOnly = true)
    public OutstandingReferenceDataDto getReferenceData(OutstandingReferenceDataRequest request) {
        requireUsername(request != null ? request.getUsername() : null);
        var privileges = pagePrivilegeResolver.resolve(
                request != null ? request.getUsername() : null,
                PAGE_CODE
        );
        return OutstandingReferenceDataDto.builder()
                .defaultItemType(List.of(
                        option(OutstandingItemType.REQUEST_SUBMITTED.name(), "Submitted requests"),
                        option(OutstandingItemType.TRIP_ASSIGNED.name(), "Assigned trips (not started)"),
                        option(OutstandingItemType.COMPLETED_WITHOUT_FARE.name(), "Completed without fare")
                ))
                .privileges(OutstandingPrivilegesDto.builder()
                        .view(privileges.isView())
                        .search(privileges.isSearch())
                        .build())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public OutstandingFilterResultDto filterList(OutstandingFilterRequest request) {
        Long companyId = requireCompanyId();
        requireUsername(request.getUsername());
        OutstandingFilterSearch search = request.getSearch();
        Set<OutstandingItemType> types = resolveTypes(search != null ? search.getItemType() : null);
        boolean ascending = "ASC".equalsIgnoreCase(request.getSortDirection());

        List<OutstandingListItemDto> all = new ArrayList<>();
        if (types.contains(OutstandingItemType.REQUEST_SUBMITTED)) {
            all.addAll(loadSubmittedRequests(companyId, search));
        }
        if (types.contains(OutstandingItemType.TRIP_ASSIGNED)) {
            all.addAll(loadAssignedTrips(companyId, search));
        }
        if (types.contains(OutstandingItemType.COMPLETED_WITHOUT_FARE)) {
            all.addAll(loadCompletedWithoutFare(companyId, search));
        }

        Comparator<OutstandingListItemDto> comparator = Comparator.comparing(
                OutstandingListItemDto::getRelevantAt,
                Comparator.nullsLast(Comparator.naturalOrder())
        );
        if (!ascending) {
            comparator = comparator.reversed();
        }
        all.sort(comparator);

        int page = Math.max(request.getPage(), 0);
        int size = Math.max(request.getSize(), 1);
        int from = Math.min(page * size, all.size());
        int to = Math.min(from + size, all.size());
        List<OutstandingListItemDto> content = all.subList(from, to);

        return OutstandingFilterResultDto.builder()
                .content(content)
                .size(content.size())
                .totalRecords(all.size())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public OutstandingDto view(OutstandingViewRequest request) {
        requireUsername(request.getUsername());
        OutstandingItemType type = parseItemType(request.getItemType());
        return switch (type) {
            case REQUEST_SUBMITTED -> viewSubmittedRequest(request.getId());
            case TRIP_ASSIGNED -> viewAssignedTrip(request.getId());
            case COMPLETED_WITHOUT_FARE -> viewCompletedWithoutFare(request.getId());
        };
    }

    private List<OutstandingListItemDto> loadSubmittedRequests(Long companyId, OutstandingFilterSearch search) {
        List<FleetTransportRequest> rows = transportRequestRepository.findAll(
                submittedSpec(companyId, search),
                Sort.by(Sort.Direction.DESC, "submittedAt")
        );
        if (rows.size() > MAX_FETCH_PER_TYPE) {
            rows = rows.subList(0, MAX_FETCH_PER_TYPE);
        }
        return rows.stream()
                .map(r -> OutstandingListItemDto.builder()
                        .itemType(OutstandingItemType.REQUEST_SUBMITTED.name())
                        .itemTypeDescription(toTypeDescription(OutstandingItemType.REQUEST_SUBMITTED))
                        .id(r.getId())
                        .referenceNumber(r.getRequestNumber())
                        .relatedNumber(null)
                        .requesterUsername(r.getRequesterUsername())
                        .pickupLocation(r.getPickupLocation())
                        .dropLocation(r.getDropLocation())
                        .relevantAt(r.getSubmittedAt() != null ? r.getSubmittedAt() : r.getCreatedDate())
                        .status(r.getStatus().name())
                        .statusDescription(toRequestStatusDescription(r.getStatus()))
                        .build())
                .toList();
    }

    private List<OutstandingListItemDto> loadAssignedTrips(Long companyId, OutstandingFilterSearch search) {
        List<FleetTrip> trips = fleetTripRepository.findAll(
                tripAssignedSpec(companyId, search),
                Sort.by(Sort.Direction.DESC, "scheduledPickupAt")
        );
        if (trips.size() > MAX_FETCH_PER_TYPE) {
            trips = trips.subList(0, MAX_FETCH_PER_TYPE);
        }
        Map<Long, FleetTransportRequest> requests = loadRequests(companyId, trips);
        return trips.stream()
                .map(trip -> {
                    FleetTransportRequest req = requests.get(trip.getTransportRequestId());
                    return OutstandingListItemDto.builder()
                            .itemType(OutstandingItemType.TRIP_ASSIGNED.name())
                            .itemTypeDescription(toTypeDescription(OutstandingItemType.TRIP_ASSIGNED))
                            .id(trip.getId())
                            .referenceNumber(trip.getTripNumber())
                            .relatedNumber(req != null ? req.getRequestNumber() : null)
                            .requesterUsername(req != null ? req.getRequesterUsername() : null)
                            .pickupLocation(req != null ? req.getPickupLocation() : null)
                            .dropLocation(req != null ? req.getDropLocation() : null)
                            .relevantAt(trip.getScheduledPickupAt())
                            .status(trip.getStatus().name())
                            .statusDescription(toTripStatusDescription(trip.getStatus()))
                            .build();
                })
                .toList();
    }

    private List<OutstandingListItemDto> loadCompletedWithoutFare(Long companyId, OutstandingFilterSearch search) {
        List<FleetTrip> trips = fleetTripRepository.findAll(
                completedWithoutFareSpec(companyId, search),
                Sort.by(Sort.Direction.DESC, "completedAt")
        );
        if (trips.size() > MAX_FETCH_PER_TYPE) {
            trips = trips.subList(0, MAX_FETCH_PER_TYPE);
        }
        Map<Long, FleetTransportRequest> requests = loadRequests(companyId, trips);
        return trips.stream()
                .map(trip -> {
                    FleetTransportRequest req = requests.get(trip.getTransportRequestId());
                    return OutstandingListItemDto.builder()
                            .itemType(OutstandingItemType.COMPLETED_WITHOUT_FARE.name())
                            .itemTypeDescription(toTypeDescription(OutstandingItemType.COMPLETED_WITHOUT_FARE))
                            .id(trip.getId())
                            .referenceNumber(trip.getTripNumber())
                            .relatedNumber(req != null ? req.getRequestNumber() : null)
                            .requesterUsername(req != null ? req.getRequesterUsername() : null)
                            .pickupLocation(req != null ? req.getPickupLocation() : null)
                            .dropLocation(req != null ? req.getDropLocation() : null)
                            .relevantAt(trip.getCompletedAt() != null ? trip.getCompletedAt() : trip.getScheduledPickupAt())
                            .status(trip.getStatus().name())
                            .statusDescription(toTripStatusDescription(trip.getStatus()))
                            .build();
                })
                .toList();
    }

    private OutstandingDto viewSubmittedRequest(Long id) {
        FleetTransportRequest request = transportRequestRepository.findByIdAndCompanyId(id, requireCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Transport request not found with ID: " + id));
        if (request.getStatus() != TransportRequestStatus.SUBMITTED) {
            throw new BadRequestException("Request is not in SUBMITTED outstanding queue");
        }
        return OutstandingDto.builder()
                .itemType(OutstandingItemType.REQUEST_SUBMITTED.name())
                .itemTypeDescription(toTypeDescription(OutstandingItemType.REQUEST_SUBMITTED))
                .id(request.getId())
                .referenceNumber(request.getRequestNumber())
                .relatedNumber(null)
                .transportRequestId(request.getId())
                .tripId(null)
                .requesterUsername(request.getRequesterUsername())
                .pickupLocation(request.getPickupLocation())
                .dropLocation(request.getDropLocation())
                .passengerCount(request.getPassengerCount())
                .relevantAt(request.getSubmittedAt() != null ? request.getSubmittedAt() : request.getCreatedDate())
                .submittedAt(request.getSubmittedAt())
                .status(request.getStatus().name())
                .statusDescription(toRequestStatusDescription(request.getStatus()))
                .notes(request.getDispatcherNotes() != null ? request.getDispatcherNotes() : request.getNotes())
                .build();
    }

    private OutstandingDto viewAssignedTrip(Long id) {
        FleetTrip trip = getTrip(id);
        if (trip.getStatus() != TripStatus.ASSIGNED) {
            throw new BadRequestException("Trip is not ASSIGNED outstanding");
        }
        return toTripOutstandingDto(OutstandingItemType.TRIP_ASSIGNED, trip);
    }

    private OutstandingDto viewCompletedWithoutFare(Long id) {
        FleetTrip trip = getTrip(id);
        if (trip.getStatus() != TripStatus.COMPLETED) {
            throw new BadRequestException("Trip is not COMPLETED");
        }
        if (tripFareRepository.existsByTripId(trip.getId())) {
            throw new BadRequestException("Trip already has a fare recorded");
        }
        return toTripOutstandingDto(OutstandingItemType.COMPLETED_WITHOUT_FARE, trip);
    }

    private OutstandingDto toTripOutstandingDto(OutstandingItemType type, FleetTrip trip) {
        FleetTransportRequest request = transportRequestRepository
                .findByIdAndCompanyId(trip.getTransportRequestId(), trip.getCompanyId())
                .orElse(null);
        FleetVehicle vehicle = fleetVehicleRepository
                .findByIdAndCompanyId(trip.getVehicleId(), trip.getCompanyId())
                .orElse(null);
        FleetDriver driver = fleetDriverRepository
                .findByIdAndCompanyId(trip.getDriverId(), trip.getCompanyId())
                .orElse(null);
        LocalDateTime relevantAt = type == OutstandingItemType.COMPLETED_WITHOUT_FARE
                ? (trip.getCompletedAt() != null ? trip.getCompletedAt() : trip.getScheduledPickupAt())
                : trip.getScheduledPickupAt();
        return OutstandingDto.builder()
                .itemType(type.name())
                .itemTypeDescription(toTypeDescription(type))
                .id(trip.getId())
                .referenceNumber(trip.getTripNumber())
                .relatedNumber(request != null ? request.getRequestNumber() : null)
                .transportRequestId(trip.getTransportRequestId())
                .tripId(trip.getId())
                .requesterUsername(request != null ? request.getRequesterUsername() : null)
                .pickupLocation(request != null ? request.getPickupLocation() : null)
                .dropLocation(request != null ? request.getDropLocation() : null)
                .passengerCount(request != null ? request.getPassengerCount() : null)
                .vehicleId(trip.getVehicleId())
                .vehicleRegistration(vehicle != null ? vehicle.getRegistration() : null)
                .driverId(trip.getDriverId())
                .driverName(driver != null ? driver.getName() : null)
                .relevantAt(relevantAt)
                .scheduledPickupAt(trip.getScheduledPickupAt())
                .completedAt(trip.getCompletedAt())
                .status(trip.getStatus().name())
                .statusDescription(toTripStatusDescription(trip.getStatus()))
                .notes(trip.getNotes())
                .build();
    }

    private Specification<FleetTransportRequest> submittedSpec(Long companyId, OutstandingFilterSearch search) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("companyId"), companyId));
            predicates.add(cb.equal(root.get("status"), TransportRequestStatus.SUBMITTED));
            if (search != null && hasText(search.getReferenceNumber())) {
                predicates.add(cb.like(
                        cb.lower(root.get("requestNumber")),
                        like(search.getReferenceNumber())
                ));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private Specification<FleetTrip> tripAssignedSpec(Long companyId, OutstandingFilterSearch search) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("companyId"), companyId));
            predicates.add(cb.equal(root.get("status"), TripStatus.ASSIGNED));
            applyScheduleDate(predicates, root, cb, search);
            applyTripReferenceNumber(predicates, root, query, cb, companyId, search);
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private Specification<FleetTrip> completedWithoutFareSpec(Long companyId, OutstandingFilterSearch search) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("companyId"), companyId));
            predicates.add(cb.equal(root.get("status"), TripStatus.COMPLETED));

            Subquery<Long> fareTripIds = query.subquery(Long.class);
            Root<FleetTripFare> fareRoot = fareTripIds.from(FleetTripFare.class);
            fareTripIds.select(fareRoot.get("tripId"));
            fareTripIds.where(cb.equal(fareRoot.get("companyId"), companyId));
            predicates.add(cb.not(root.get("id").in(fareTripIds)));

            applyScheduleDate(predicates, root, cb, search);
            applyTripReferenceNumber(predicates, root, query, cb, companyId, search);
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private void applyScheduleDate(
            List<Predicate> predicates,
            Root<FleetTrip> root,
            jakarta.persistence.criteria.CriteriaBuilder cb,
            OutstandingFilterSearch search
    ) {
        if (search == null || search.getScheduleDate() == null) {
            return;
        }
        LocalDate scheduleDate = search.getScheduleDate();
        predicates.add(cb.between(
                root.get("scheduledPickupAt"),
                scheduleDate.atStartOfDay(),
                scheduleDate.atTime(LocalTime.MAX)
        ));
    }

    private void applyTripReferenceNumber(
            List<Predicate> predicates,
            Root<FleetTrip> root,
            jakarta.persistence.criteria.CriteriaQuery<?> query,
            jakarta.persistence.criteria.CriteriaBuilder cb,
            Long companyId,
            OutstandingFilterSearch search
    ) {
        if (search == null || !hasText(search.getReferenceNumber())) {
            return;
        }
        String pattern = like(search.getReferenceNumber());
        Subquery<Long> matchingRequestIds = query.subquery(Long.class);
        Root<FleetTransportRequest> reqRoot = matchingRequestIds.from(FleetTransportRequest.class);
        matchingRequestIds.select(reqRoot.get("id"));
        matchingRequestIds.where(
                cb.equal(reqRoot.get("companyId"), companyId),
                cb.like(cb.lower(reqRoot.get("requestNumber")), pattern)
        );
        predicates.add(cb.or(
                cb.like(cb.lower(root.get("tripNumber")), pattern),
                root.get("transportRequestId").in(matchingRequestIds)
        ));
    }

    private FleetTrip getTrip(Long id) {
        return fleetTripRepository.findByIdAndCompanyId(id, requireCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found with ID: " + id));
    }

    private Map<Long, FleetTransportRequest> loadRequests(Long companyId, List<FleetTrip> trips) {
        Map<Long, FleetTransportRequest> map = new HashMap<>();
        trips.stream()
                .map(FleetTrip::getTransportRequestId)
                .filter(Objects::nonNull)
                .distinct()
                .forEach(id -> transportRequestRepository.findByIdAndCompanyId(id, companyId)
                        .ifPresent(r -> map.put(id, r)));
        return map;
    }

    private Set<OutstandingItemType> resolveTypes(String itemType) {
        if (!hasText(itemType)) {
            return EnumSet.allOf(OutstandingItemType.class);
        }
        return EnumSet.of(parseItemType(itemType));
    }

    private OutstandingItemType parseItemType(String value) {
        try {
            return OutstandingItemType.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (Exception ex) {
            throw new BadRequestException("Invalid itemType: " + value);
        }
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

    private ReferenceOptionDto option(String code, String description) {
        return ReferenceOptionDto.builder().code(code).description(description).build();
    }

    private String toTypeDescription(OutstandingItemType type) {
        return switch (type) {
            case REQUEST_SUBMITTED -> "Submitted requests";
            case TRIP_ASSIGNED -> "Assigned trips (not started)";
            case COMPLETED_WITHOUT_FARE -> "Completed without fare";
        };
    }

    private String toRequestStatusDescription(TransportRequestStatus status) {
        return switch (status) {
            case DRAFT -> "Draft";
            case SUBMITTED -> "Submitted";
            case ASSIGNED -> "Assigned";
            case CANCELLED -> "Cancelled";
        };
    }

    private String toTripStatusDescription(TripStatus status) {
        return switch (status) {
            case ASSIGNED -> "Assigned";
            case STARTED -> "Started";
            case COMPLETED -> "Completed";
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
