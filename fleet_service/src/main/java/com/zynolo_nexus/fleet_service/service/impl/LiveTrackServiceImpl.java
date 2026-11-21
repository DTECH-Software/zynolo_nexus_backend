package com.zynolo_nexus.fleet_service.service.impl;

import com.zynolo_nexus.fleet_service.context.CompanyContext;
import com.zynolo_nexus.fleet_service.dto.request.LiveTrackFilterRequest;
import com.zynolo_nexus.fleet_service.dto.request.LiveTrackFilterSearch;
import com.zynolo_nexus.fleet_service.dto.request.LiveTrackReferenceDataRequest;
import com.zynolo_nexus.fleet_service.dto.request.LiveTrackViewRequest;
import com.zynolo_nexus.fleet_service.dto.response.LiveTrackDto;
import com.zynolo_nexus.fleet_service.dto.response.LiveTrackFilterResultDto;
import com.zynolo_nexus.fleet_service.dto.response.LiveTrackListItemDto;
import com.zynolo_nexus.fleet_service.dto.response.LiveTrackPrivilegesDto;
import com.zynolo_nexus.fleet_service.dto.response.LiveTrackReferenceDataDto;
import com.zynolo_nexus.fleet_service.dto.response.LocationPointDto;
import com.zynolo_nexus.fleet_service.dto.response.ReferenceOptionDto;
import com.zynolo_nexus.fleet_service.enums.TripStatus;
import com.zynolo_nexus.fleet_service.exception.BadRequestException;
import com.zynolo_nexus.fleet_service.exception.ResourceNotFoundException;
import com.zynolo_nexus.fleet_service.model.FleetDriver;
import com.zynolo_nexus.fleet_service.model.FleetTransportRequest;
import com.zynolo_nexus.fleet_service.model.FleetTrip;
import com.zynolo_nexus.fleet_service.model.FleetTripLocation;
import com.zynolo_nexus.fleet_service.model.FleetVehicle;
import com.zynolo_nexus.fleet_service.repository.FleetDriverRepository;
import com.zynolo_nexus.fleet_service.repository.FleetTransportRequestRepository;
import com.zynolo_nexus.fleet_service.repository.FleetTripLocationRepository;
import com.zynolo_nexus.fleet_service.repository.FleetTripRepository;
import com.zynolo_nexus.fleet_service.repository.FleetVehicleRepository;
import com.zynolo_nexus.fleet_service.service.LiveTrackService;
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
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class LiveTrackServiceImpl implements LiveTrackService {

    private static final String PAGE_CODE = "FLLT";
    private static final int DEFAULT_TRAIL_LIMIT = 50;
    private static final int MAX_TRAIL_LIMIT = 100;
    private static final int SIGNAL_LOST_AFTER_SECONDS = 30;
    private static final int RECOMMENDED_POLL_SECONDS = 5;
    private static final Set<TripStatus> TRACKABLE = EnumSet.of(TripStatus.STARTED, TripStatus.ASSIGNED);

    private final FleetTripRepository fleetTripRepository;
    private final FleetTripLocationRepository tripLocationRepository;
    private final FleetTransportRequestRepository transportRequestRepository;
    private final FleetDriverRepository fleetDriverRepository;
    private final FleetVehicleRepository fleetVehicleRepository;
    private final PagePrivilegeResolver pagePrivilegeResolver;

    @Value("${app.default.company-id:1}")
    private Long defaultCompanyId;

    @Override
    @Transactional(readOnly = true)
    public LiveTrackReferenceDataDto getReferenceData(LiveTrackReferenceDataRequest request) {
        var privileges = pagePrivilegeResolver.resolve(
                request != null ? request.getUsername() : null,
                PAGE_CODE
        );
        return LiveTrackReferenceDataDto.builder()
                .defaultTripStatus(List.of(
                        option(TripStatus.ASSIGNED.name(), "Assigned"),
                        option(TripStatus.STARTED.name(), "Started")
                ))
                .recommendedPollSeconds(RECOMMENDED_POLL_SECONDS)
                .websocketEndpoint("/api/v1/fleet/ws")
                .websocketTopicPattern("/topic/trips/{tripId}/location")
                .privileges(LiveTrackPrivilegesDto.builder()
                        .view(privileges.isView())
                        .search(privileges.isSearch())
                        .build())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public LiveTrackFilterResultDto filterList(LiveTrackFilterRequest request) {
        Long companyId = requireCompanyId();
        Pageable pageable = PageRequest.of(
                request.getPage(),
                request.getSize(),
                Sort.by(resolveDirection(request.getSortDirection()), resolveSortColumn(request.getSortColumn()))
        );

        Page<FleetTrip> page = fleetTripRepository.findAll(
                buildSpecification(companyId, request.getSearch()),
                pageable
        );

        Map<Long, FleetTransportRequest> requests = loadRequests(companyId, page.getContent());
        Map<Long, FleetDriver> drivers = loadDrivers(companyId, page.getContent());
        Map<Long, FleetVehicle> vehicles = loadVehicles(companyId, page.getContent());
        LocalDateTime now = LocalDateTime.now();

        List<LiveTrackListItemDto> content = page.getContent().stream()
                .map(trip -> toListItemDto(
                        trip,
                        requests.get(trip.getTransportRequestId()),
                        drivers.get(trip.getDriverId()),
                        vehicles.get(trip.getVehicleId()),
                        now
                ))
                .toList();

        return LiveTrackFilterResultDto.builder()
                .content(content)
                .size(content.size())
                .totalRecords(page.getTotalElements())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public LiveTrackDto view(LiveTrackViewRequest request) {
        Long companyId = requireCompanyId();
        FleetTrip trip = fleetTripRepository.findByIdAndCompanyId(request.getTripId(), companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found with ID: " + request.getTripId()));

        FleetTransportRequest transportRequest = transportRequestRepository
                .findByIdAndCompanyId(trip.getTransportRequestId(), companyId)
                .orElse(null);
        FleetDriver driver = fleetDriverRepository.findByIdAndCompanyId(trip.getDriverId(), companyId).orElse(null);
        FleetVehicle vehicle = fleetVehicleRepository.findByIdAndCompanyId(trip.getVehicleId(), companyId).orElse(null);

        int trailLimit = resolveTrailLimit(request.getTrailLimit());
        List<FleetTripLocation> locations = tripLocationRepository
                .findTop100ByCompanyIdAndTripIdOrderByRecordedAtDesc(companyId, trip.getId());
        if (locations.size() > trailLimit) {
            locations = locations.subList(0, trailLimit);
        }
        List<LocationPointDto> trail = new ArrayList<>(locations.stream().map(this::toPoint).toList());
        Collections.reverse(trail);

        LocationPointDto last = trip.getLastLatitude() != null && trip.getLastLongitude() != null
                ? LocationPointDto.builder()
                .latitude(trip.getLastLatitude())
                .longitude(trip.getLastLongitude())
                .accuracyMeters(trip.getLastAccuracyMeters())
                .speedKmh(trip.getLastSpeedKmh())
                .headingDegrees(trip.getLastHeadingDegrees())
                .recordedAt(trip.getLastLocationAt())
                .build()
                : null;

        return LiveTrackDto.builder()
                .tripId(trip.getId())
                .tripNumber(trip.getTripNumber())
                .status(trip.getStatus().name())
                .statusDescription(toStatusDescription(trip.getStatus()))
                .transportRequestId(trip.getTransportRequestId())
                .requestNumber(transportRequest != null ? transportRequest.getRequestNumber() : null)
                .requesterUsername(transportRequest != null ? transportRequest.getRequesterUsername() : null)
                .pickupLocation(transportRequest != null ? transportRequest.getPickupLocation() : null)
                .dropLocation(transportRequest != null ? transportRequest.getDropLocation() : null)
                .driverId(trip.getDriverId())
                .driverName(driver != null ? driver.getName() : null)
                .vehicleId(trip.getVehicleId())
                .vehicleRegistration(vehicle != null ? vehicle.getRegistration() : null)
                .scheduledPickupAt(trip.getScheduledPickupAt())
                .startedAt(trip.getStartedAt())
                .lastLocation(last)
                .signalLost(isSignalLost(trip.getLastLocationAt(), LocalDateTime.now()))
                .signalLostAfterSeconds(SIGNAL_LOST_AFTER_SECONDS)
                .trail(trail)
                .build();
    }

    private Specification<FleetTrip> buildSpecification(Long companyId, LiveTrackFilterSearch search) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("companyId"), companyId));

            if (search != null && hasText(search.getStatus())) {
                predicates.add(criteriaBuilder.equal(root.get("status"), parseTripStatus(search.getStatus())));
            } else {
                predicates.add(root.get("status").in(TRACKABLE));
            }

            if (search != null) {
                if (hasText(search.getTripNumber())) {
                    predicates.add(criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("tripNumber")),
                            like(search.getTripNumber())
                    ));
                }
                if (search.getDriverId() != null) {
                    predicates.add(criteriaBuilder.equal(root.get("driverId"), search.getDriverId()));
                }
                if (search.getVehicleId() != null) {
                    predicates.add(criteriaBuilder.equal(root.get("vehicleId"), search.getVehicleId()));
                }
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private LiveTrackListItemDto toListItemDto(
            FleetTrip trip,
            FleetTransportRequest request,
            FleetDriver driver,
            FleetVehicle vehicle,
            LocalDateTime now
    ) {
        return LiveTrackListItemDto.builder()
                .tripId(trip.getId())
                .tripNumber(trip.getTripNumber())
                .status(trip.getStatus().name())
                .statusDescription(toStatusDescription(trip.getStatus()))
                .driverId(trip.getDriverId())
                .driverName(driver != null ? driver.getName() : null)
                .vehicleId(trip.getVehicleId())
                .vehicleRegistration(vehicle != null ? vehicle.getRegistration() : null)
                .pickupLocation(request != null ? request.getPickupLocation() : null)
                .dropLocation(request != null ? request.getDropLocation() : null)
                .scheduledPickupAt(trip.getScheduledPickupAt())
                .lastLatitude(trip.getLastLatitude())
                .lastLongitude(trip.getLastLongitude())
                .lastLocationAt(trip.getLastLocationAt())
                .signalLost(isSignalLost(trip.getLastLocationAt(), now))
                .build();
    }

    private LocationPointDto toPoint(FleetTripLocation location) {
        return LocationPointDto.builder()
                .latitude(location.getLatitude())
                .longitude(location.getLongitude())
                .accuracyMeters(location.getAccuracyMeters())
                .speedKmh(location.getSpeedKmh())
                .headingDegrees(location.getHeadingDegrees())
                .recordedAt(location.getRecordedAt())
                .build();
    }

    private boolean isSignalLost(LocalDateTime lastLocationAt, LocalDateTime now) {
        if (lastLocationAt == null) {
            return true;
        }
        return lastLocationAt.isBefore(now.minusSeconds(SIGNAL_LOST_AFTER_SECONDS));
    }

    private int resolveTrailLimit(Integer trailLimit) {
        if (trailLimit == null || trailLimit < 1) {
            return DEFAULT_TRAIL_LIMIT;
        }
        return Math.min(trailLimit, MAX_TRAIL_LIMIT);
    }

    private Map<Long, FleetTransportRequest> loadRequests(Long companyId, List<FleetTrip> trips) {
        Map<Long, FleetTransportRequest> map = new HashMap<>();
        trips.stream().map(FleetTrip::getTransportRequestId).filter(Objects::nonNull).distinct()
                .forEach(id -> transportRequestRepository.findByIdAndCompanyId(id, companyId)
                        .ifPresent(r -> map.put(id, r)));
        return map;
    }

    private Map<Long, FleetDriver> loadDrivers(Long companyId, List<FleetTrip> trips) {
        Map<Long, FleetDriver> map = new HashMap<>();
        trips.stream().map(FleetTrip::getDriverId).filter(Objects::nonNull).distinct()
                .forEach(id -> fleetDriverRepository.findByIdAndCompanyId(id, companyId)
                        .ifPresent(d -> map.put(id, d)));
        return map;
    }

    private Map<Long, FleetVehicle> loadVehicles(Long companyId, List<FleetTrip> trips) {
        Map<Long, FleetVehicle> map = new HashMap<>();
        trips.stream().map(FleetTrip::getVehicleId).filter(Objects::nonNull).distinct()
                .forEach(id -> fleetVehicleRepository.findByIdAndCompanyId(id, companyId)
                        .ifPresent(v -> map.put(id, v)));
        return map;
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

    private TripStatus parseTripStatus(String value) {
        try {
            return TripStatus.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (Exception ex) {
            throw new BadRequestException("Invalid status: " + value);
        }
    }

    private String resolveSortColumn(String sortColumn) {
        if (!hasText(sortColumn)) {
            return "lastLocationAt";
        }
        return switch (sortColumn) {
            case "tripNumber", "status", "scheduledPickupAt", "lastLocationAt",
                 "startedAt", "assignedAt", "createdDate", "lastModifiedDate" -> sortColumn;
            default -> "lastLocationAt";
        };
    }

    private Sort.Direction resolveDirection(String direction) {
        return "ASC".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
    }

    private ReferenceOptionDto option(String code, String description) {
        return ReferenceOptionDto.builder().code(code).description(description).build();
    }

    private String toStatusDescription(TripStatus status) {
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