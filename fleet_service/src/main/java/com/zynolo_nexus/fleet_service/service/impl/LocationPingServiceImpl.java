package com.zynolo_nexus.fleet_service.service.impl;

import com.zynolo_nexus.fleet_service.context.CompanyContext;
import com.zynolo_nexus.fleet_service.dto.request.LocationPingCreateRequest;
import com.zynolo_nexus.fleet_service.dto.request.LocationPingReferenceDataRequest;
import com.zynolo_nexus.fleet_service.dto.response.LocationPingDto;
import com.zynolo_nexus.fleet_service.dto.response.LocationPingPrivilegesDto;
import com.zynolo_nexus.fleet_service.dto.response.LocationPingReferenceDataDto;
import com.zynolo_nexus.fleet_service.dto.ws.TripLocationWsEvent;
import com.zynolo_nexus.fleet_service.enums.TripStatus;
import com.zynolo_nexus.fleet_service.exception.BadRequestException;
import com.zynolo_nexus.fleet_service.exception.ResourceNotFoundException;
import com.zynolo_nexus.fleet_service.model.FleetDriver;
import com.zynolo_nexus.fleet_service.model.FleetTrip;
import com.zynolo_nexus.fleet_service.model.FleetTripLocation;
import com.zynolo_nexus.fleet_service.repository.FleetDriverRepository;
import com.zynolo_nexus.fleet_service.repository.FleetTripLocationRepository;
import com.zynolo_nexus.fleet_service.repository.FleetTripRepository;
import com.zynolo_nexus.fleet_service.service.LocationPingService;
import com.zynolo_nexus.fleet_service.service.support.PagePrivilegeResolver;
import com.zynolo_nexus.fleet_service.service.support.TripLocationRealtimePublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class LocationPingServiceImpl implements LocationPingService {

    private static final String PAGE_CODE = "FLLP";
    private static final int RECOMMENDED_PING_SECONDS = 5;

    private final FleetTripRepository fleetTripRepository;
    private final FleetTripLocationRepository tripLocationRepository;
    private final FleetDriverRepository fleetDriverRepository;
    private final PagePrivilegeResolver pagePrivilegeResolver;
    private final TripLocationRealtimePublisher tripLocationRealtimePublisher;

    @Value("${app.default.company-id:1}")
    private Long defaultCompanyId;

    @Override
    @Transactional(readOnly = true)
    public LocationPingReferenceDataDto getReferenceData(LocationPingReferenceDataRequest request) {
        FleetDriver driver = requireLinkedDriver(request.getUsername());
        var privileges = pagePrivilegeResolver.resolve(
                request != null ? request.getUsername() : null,
                PAGE_CODE
        );
        return LocationPingReferenceDataDto.builder()
                .driverId(driver.getId())
                .driverName(driver.getName())
                .recommendedPingSeconds(RECOMMENDED_PING_SECONDS)
                .privileges(LocationPingPrivilegesDto.builder()
                        .add(privileges.isAdd())
                        .view(privileges.isView())
                        .build())
                .build();
    }

    @Override
    @Transactional
    public LocationPingDto ping(LocationPingCreateRequest request) {
        Long companyId = requireCompanyId();
        FleetDriver driver = requireLinkedDriver(request.getUsername());
        FleetTrip trip = fleetTripRepository.findByIdAndCompanyId(request.getTripId(), companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found with ID: " + request.getTripId()));

        if (!Objects.equals(trip.getDriverId(), driver.getId())) {
            throw new BadRequestException("You can only ping location for your own trips");
        }
        if (trip.getStatus() != TripStatus.STARTED) {
            throw new BadRequestException("Location ping is allowed only when trip status is STARTED");
        }

        validateCoordinates(request.getLatitude(), request.getLongitude());

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime recordedAt = request.getRecordedAt() != null ? request.getRecordedAt() : now;
        if (trip.getLastLocationAt() != null && recordedAt.isBefore(trip.getLastLocationAt())) {
            // Ignore stale/out-of-order points for last-location, still store history
        } else {
            trip.setLastLatitude(request.getLatitude());
            trip.setLastLongitude(request.getLongitude());
            trip.setLastAccuracyMeters(request.getAccuracyMeters());
            trip.setLastSpeedKmh(request.getSpeedKmh());
            trip.setLastHeadingDegrees(request.getHeadingDegrees());
            trip.setLastLocationAt(recordedAt);
            trip.setLastModifiedDate(now);
            trip.setLastModifiedBy(driver.getLinkedUsername() != null ? driver.getLinkedUsername() : request.getUsername());
            fleetTripRepository.save(trip);
        }

        FleetTripLocation location = new FleetTripLocation();
        location.setCompanyId(companyId);
        location.setTripId(trip.getId());
        location.setDriverId(driver.getId());
        location.setLatitude(request.getLatitude());
        location.setLongitude(request.getLongitude());
        location.setAccuracyMeters(request.getAccuracyMeters());
        location.setSpeedKmh(request.getSpeedKmh());
        location.setHeadingDegrees(request.getHeadingDegrees());
        location.setRecordedAt(recordedAt);
        location.setReceivedAt(now);
        location.setCreatedBy(hasText(request.getUsername()) ? request.getUsername().trim() : "system");

        FleetTripLocation saved = tripLocationRepository.save(location);

        tripLocationRealtimePublisher.publish(TripLocationWsEvent.builder()
                .tripId(trip.getId())
                .tripNumber(trip.getTripNumber())
                .companyId(companyId)
                .latitude(saved.getLatitude())
                .longitude(saved.getLongitude())
                .accuracyMeters(saved.getAccuracyMeters())
                .speedKmh(saved.getSpeedKmh())
                .headingDegrees(saved.getHeadingDegrees())
                .recordedAt(saved.getRecordedAt())
                .receivedAt(saved.getReceivedAt())
                .eventType("LOCATION_PING")
                .build());

        return LocationPingDto.builder()
                .id(saved.getId())
                .tripId(trip.getId())
                .tripNumber(trip.getTripNumber())
                .latitude(saved.getLatitude())
                .longitude(saved.getLongitude())
                .accuracyMeters(saved.getAccuracyMeters())
                .speedKmh(saved.getSpeedKmh())
                .headingDegrees(saved.getHeadingDegrees())
                .recordedAt(saved.getRecordedAt())
                .receivedAt(saved.getReceivedAt())
                .build();
    }

    private void validateCoordinates(Double latitude, Double longitude) {
        if (latitude == null || longitude == null) {
            throw new BadRequestException("latitude and longitude are required");
        }
        if (latitude < -90 || latitude > 90) {
            throw new BadRequestException("latitude must be between -90 and 90");
        }
        if (longitude < -180 || longitude > 180) {
            throw new BadRequestException("longitude must be between -180 and 180");
        }
    }

    private FleetDriver requireLinkedDriver(String username) {
        Long companyId = requireCompanyId();
        if (!hasText(username)) {
            throw new BadRequestException("username is required");
        }
        return fleetDriverRepository.findByCompanyIdAndLinkedUsernameIgnoreCase(companyId, username.trim())
                .orElseThrow(() -> new BadRequestException(
                        "No driver profile linked to username: " + username.trim()
                                + ". Set linkedUsername on Driver Master."
                ));
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

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}