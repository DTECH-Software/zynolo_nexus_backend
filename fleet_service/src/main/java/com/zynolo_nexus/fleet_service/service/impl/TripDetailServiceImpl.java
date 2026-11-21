package com.zynolo_nexus.fleet_service.service.impl;

import com.zynolo_nexus.fleet_service.context.CompanyContext;
import com.zynolo_nexus.fleet_service.dto.request.TripDetailReferenceDataRequest;
import com.zynolo_nexus.fleet_service.dto.request.TripDetailViewRequest;
import com.zynolo_nexus.fleet_service.dto.response.LocationPointDto;
import com.zynolo_nexus.fleet_service.dto.response.ReferenceOptionDto;
import com.zynolo_nexus.fleet_service.dto.response.TripDetailDto;
import com.zynolo_nexus.fleet_service.dto.response.TripDetailPrivilegesDto;
import com.zynolo_nexus.fleet_service.dto.response.TripDetailReferenceDataDto;
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
import com.zynolo_nexus.fleet_service.service.TripDetailService;
import com.zynolo_nexus.fleet_service.service.support.PagePrivilegeResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TripDetailServiceImpl implements TripDetailService {

    private static final String PAGE_CODE = "FLTD";
    private static final int DEFAULT_TRAIL_LIMIT = 50;
    private static final int MAX_TRAIL_LIMIT = 100;
    private static final int SIGNAL_LOST_AFTER_SECONDS = 30;
    private static final int RECOMMENDED_POLL_SECONDS = 5;

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
    public TripDetailReferenceDataDto getReferenceData(TripDetailReferenceDataRequest request) {
        requireUsername(request != null ? request.getUsername() : null);
        var privileges = pagePrivilegeResolver.resolve(
                request != null ? request.getUsername() : null,
                PAGE_CODE
        );
        return TripDetailReferenceDataDto.builder()
                .defaultTripStatus(List.of(
                        option(TripStatus.ASSIGNED.name(), "Assigned"),
                        option(TripStatus.STARTED.name(), "Started"),
                        option(TripStatus.COMPLETED.name(), "Completed"),
                        option(TripStatus.CANCELLED.name(), "Cancelled")
                ))
                .recommendedPollSeconds(RECOMMENDED_POLL_SECONDS)
                .signalLostAfterSeconds(SIGNAL_LOST_AFTER_SECONDS)
                .websocketEndpoint("/api/v1/fleet/ws")
                .websocketTopicPattern("/topic/trips/{tripId}/location")
                .privileges(TripDetailPrivilegesDto.builder()
                        .view(privileges.isView())
                        .build())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public TripDetailDto view(TripDetailViewRequest request) {
        String actor = requireUsername(request.getUsername());
        Long companyId = requireCompanyId();
        FleetTrip trip = fleetTripRepository.findByIdAndCompanyId(request.getId(), companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found with ID: " + request.getId()));

        FleetTransportRequest transportRequest = transportRequestRepository
                .findByIdAndCompanyId(trip.getTransportRequestId(), companyId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Transport request not found for trip ID: " + trip.getId()
                ));
        if (!actor.equalsIgnoreCase(transportRequest.getRequesterUsername())) {
            throw new BadRequestException("You can only view your own trips");
        }

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

        LocalDateTime now = LocalDateTime.now();
        return TripDetailDto.builder()
                .id(trip.getId())
                .companyId(trip.getCompanyId())
                .tripNumber(trip.getTripNumber())
                .transportRequestId(trip.getTransportRequestId())
                .requestNumber(transportRequest.getRequestNumber())
                .requesterUsername(transportRequest.getRequesterUsername())
                .pickupLocation(transportRequest.getPickupLocation())
                .dropLocation(transportRequest.getDropLocation())
                .passengerCount(transportRequest.getPassengerCount())
                .contactMobile(transportRequest.getContactMobile())
                .purpose(transportRequest.getPurpose())
                .vehicleId(trip.getVehicleId())
                .vehicleRegistration(vehicle != null ? vehicle.getRegistration() : null)
                .driverId(trip.getDriverId())
                .driverName(driver != null ? driver.getName() : null)
                .driverMobile(driver != null ? driver.getMobile() : null)
                .scheduledPickupAt(trip.getScheduledPickupAt())
                .assignedAt(trip.getAssignedAt())
                .assignedBy(trip.getAssignedBy())
                .startedAt(trip.getStartedAt())
                .completedAt(trip.getCompletedAt())
                .notes(trip.getNotes())
                .status(trip.getStatus().name())
                .statusDescription(toStatusDescription(trip.getStatus()))
                .lastLocation(last)
                .signalLost(isSignalLost(trip.getLastLocationAt(), now))
                .signalLostAfterSeconds(SIGNAL_LOST_AFTER_SECONDS)
                .trail(trail)
                .websocketTopic("/topic/trips/" + trip.getId() + "/location")
                .createdDate(trip.getCreatedDate())
                .lastModifiedDate(trip.getLastModifiedDate())
                .createdBy(trip.getCreatedBy())
                .lastModifiedBy(trip.getLastModifiedBy())
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
        return ReferenceOptionDto.builder()
                .code(code)
                .description(description)
                .build();
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
}
