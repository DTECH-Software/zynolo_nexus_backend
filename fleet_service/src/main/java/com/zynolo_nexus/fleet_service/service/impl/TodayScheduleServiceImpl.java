package com.zynolo_nexus.fleet_service.service.impl;

import com.zynolo_nexus.fleet_service.context.CompanyContext;
import com.zynolo_nexus.fleet_service.dto.request.TodayScheduleFilterRequest;
import com.zynolo_nexus.fleet_service.dto.request.TodayScheduleFilterSearch;
import com.zynolo_nexus.fleet_service.dto.request.TodayScheduleReferenceDataRequest;
import com.zynolo_nexus.fleet_service.dto.request.TodayScheduleUpdateRequest;
import com.zynolo_nexus.fleet_service.dto.request.TodayScheduleViewRequest;
import com.zynolo_nexus.fleet_service.dto.response.AssignTripDriverOptionDto;
import com.zynolo_nexus.fleet_service.dto.response.DriverVehicleOptionDto;
import com.zynolo_nexus.fleet_service.dto.response.ReferenceOptionDto;
import com.zynolo_nexus.fleet_service.dto.response.TodaySchedulePrivilegesDto;
import com.zynolo_nexus.fleet_service.dto.response.TodayScheduleReferenceDataDto;
import com.zynolo_nexus.fleet_service.dto.response.TripDto;
import com.zynolo_nexus.fleet_service.dto.response.TripFilterResultDto;
import com.zynolo_nexus.fleet_service.dto.response.TripListItemDto;
import com.zynolo_nexus.fleet_service.enums.MasterStatus;
import com.zynolo_nexus.fleet_service.enums.TripStatus;
import com.zynolo_nexus.fleet_service.exception.BadRequestException;
import com.zynolo_nexus.fleet_service.exception.ResourceNotFoundException;
import com.zynolo_nexus.fleet_service.model.FleetDriver;
import com.zynolo_nexus.fleet_service.model.FleetTransportRequest;
import com.zynolo_nexus.fleet_service.model.FleetTrip;
import com.zynolo_nexus.fleet_service.model.FleetVehicle;
import com.zynolo_nexus.fleet_service.repository.FleetDriverRepository;
import com.zynolo_nexus.fleet_service.repository.FleetTransportRequestRepository;
import com.zynolo_nexus.fleet_service.repository.FleetTripRepository;
import com.zynolo_nexus.fleet_service.repository.FleetVehicleRepository;
import com.zynolo_nexus.fleet_service.service.TodayScheduleService;
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
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TodayScheduleServiceImpl implements TodayScheduleService {

    private static final String PAGE_CODE = "FLTS";

    private final FleetTripRepository fleetTripRepository;
    private final FleetTransportRequestRepository transportRequestRepository;
    private final FleetVehicleRepository fleetVehicleRepository;
    private final FleetDriverRepository fleetDriverRepository;
    private final PagePrivilegeResolver pagePrivilegeResolver;

    @Value("${app.default.company-id:1}")
    private Long defaultCompanyId;

    @Override
    @Transactional(readOnly = true)
    public TodayScheduleReferenceDataDto getReferenceData(TodayScheduleReferenceDataRequest request) {
        Long companyId = requireCompanyId();
        var privileges = pagePrivilegeResolver.resolve(
                request != null ? request.getUsername() : null,
                PAGE_CODE
        );

        List<DriverVehicleOptionDto> vehicles = fleetVehicleRepository
                .findByCompanyIdAndStatusOrderByRegistrationAsc(companyId, MasterStatus.ACTIVE)
                .stream()
                .map(v -> DriverVehicleOptionDto.builder()
                        .id(v.getId())
                        .registration(v.getRegistration())
                        .vehicleType(v.getVehicleType())
                        .build())
                .toList();

        List<AssignTripDriverOptionDto> drivers = fleetDriverRepository
                .findByCompanyIdAndStatusOrderByNameAsc(companyId, MasterStatus.ACTIVE)
                .stream()
                .map(d -> AssignTripDriverOptionDto.builder()
                        .id(d.getId())
                        .name(d.getName())
                        .mobile(d.getMobile())
                        .availability(d.getAvailability().name())
                        .usualVehicleId(d.getUsualVehicleId())
                        .build())
                .toList();

        return TodayScheduleReferenceDataDto.builder()
                .defaultTripStatus(List.of(
                        option(TripStatus.ASSIGNED.name(), "Assigned"),
                        option(TripStatus.STARTED.name(), "Started"),
                        option(TripStatus.COMPLETED.name(), "Completed"),
                        option(TripStatus.CANCELLED.name(), "Cancelled")
                ))
                .vehicles(vehicles)
                .drivers(drivers)
                .privileges(TodaySchedulePrivilegesDto.builder()
                        .view(privileges.isView())
                        .search(privileges.isSearch())
                        .update(privileges.isUpdate())
                        .build())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public TripFilterResultDto filterList(TodayScheduleFilterRequest request) {
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
        Map<Long, FleetVehicle> vehicles = loadVehicles(companyId, page.getContent());
        Map<Long, FleetDriver> drivers = loadDrivers(companyId, page.getContent());

        List<TripListItemDto> content = page.getContent().stream()
                .map(trip -> toListItemDto(
                        trip,
                        requests.get(trip.getTransportRequestId()),
                        vehicles.get(trip.getVehicleId()),
                        drivers.get(trip.getDriverId())
                ))
                .toList();

        return TripFilterResultDto.builder()
                .content(content)
                .size(content.size())
                .totalRecords(page.getTotalElements())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public TripDto view(TodayScheduleViewRequest request) {
        return toTripDto(getTripForCompany(request.getId()));
    }

    @Override
    @Transactional
    public TripDto update(TodayScheduleUpdateRequest request) {
        Long companyId = requireCompanyId();
        FleetTrip trip = getTripForCompany(request.getId());
        if (trip.getStatus() != TripStatus.ASSIGNED) {
            throw new BadRequestException("Only ASSIGNED trips can be updated");
        }

        FleetTransportRequest transportRequest = getRequestForCompany(trip.getTransportRequestId());
        FleetVehicle vehicle = requireActiveVehicle(companyId, request.getVehicleId());
        FleetDriver driver = requireActiveDriver(companyId, request.getDriverId());
        if (vehicle.getPassengerCapacity() != null
                && transportRequest.getPassengerCount() != null
                && vehicle.getPassengerCapacity() < transportRequest.getPassengerCount()) {
            throw new BadRequestException("Vehicle passenger capacity is less than requested passenger count");
        }

        trip.setVehicleId(vehicle.getId());
        trip.setDriverId(driver.getId());
        if (request.getScheduledPickupAt() != null) {
            trip.setScheduledPickupAt(request.getScheduledPickupAt());
        }
        trip.setNotes(trimToNull(request.getNotes()));
        applyAudit(trip, request.getUsername());

        return toTripDto(fleetTripRepository.save(trip), transportRequest, vehicle, driver);
    }

    private FleetTrip getTripForCompany(Long id) {
        Long companyId = requireCompanyId();
        return fleetTripRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found with ID: " + id));
    }

    private FleetTransportRequest getRequestForCompany(Long id) {
        Long companyId = requireCompanyId();
        return transportRequestRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Transport request not found with ID: " + id));
    }

    private FleetVehicle requireActiveVehicle(Long companyId, Long vehicleId) {
        FleetVehicle vehicle = fleetVehicleRepository.findByIdAndCompanyId(vehicleId, companyId)
                .orElseThrow(() -> new BadRequestException("Vehicle not found with ID: " + vehicleId));
        if (vehicle.getStatus() != MasterStatus.ACTIVE) {
            throw new BadRequestException("Vehicle must be ACTIVE");
        }
        return vehicle;
    }

    private FleetDriver requireActiveDriver(Long companyId, Long driverId) {
        FleetDriver driver = fleetDriverRepository.findByIdAndCompanyId(driverId, companyId)
                .orElseThrow(() -> new BadRequestException("Driver not found with ID: " + driverId));
        if (driver.getStatus() != MasterStatus.ACTIVE) {
            throw new BadRequestException("Driver must be ACTIVE");
        }
        return driver;
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

    private Specification<FleetTrip> buildSpecification(Long companyId, TodayScheduleFilterSearch search) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("companyId"), companyId));

            LocalDate scheduleDate = (search != null && search.getScheduleDate() != null)
                    ? search.getScheduleDate()
                    : LocalDate.now();
            LocalDateTime dayStart = scheduleDate.atStartOfDay();
            LocalDateTime dayEnd = scheduleDate.atTime(LocalTime.MAX);
            predicates.add(criteriaBuilder.between(root.get("scheduledPickupAt"), dayStart, dayEnd));

            if (search != null) {
                if (hasText(search.getTripNumber())) {
                    predicates.add(criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("tripNumber")),
                            like(search.getTripNumber())
                    ));
                }
                if (hasText(search.getStatus())) {
                    predicates.add(criteriaBuilder.equal(root.get("status"), parseTripStatus(search.getStatus())));
                }
                if (search.getVehicleId() != null) {
                    predicates.add(criteriaBuilder.equal(root.get("vehicleId"), search.getVehicleId()));
                }
                if (search.getDriverId() != null) {
                    predicates.add(criteriaBuilder.equal(root.get("driverId"), search.getDriverId()));
                }
                if (hasText(search.getRequesterUsername())) {
                    List<Long> requestIds = transportRequestRepository.findAll((reqRoot, reqQuery, cb) ->
                            cb.and(
                                    cb.equal(reqRoot.get("companyId"), companyId),
                                    cb.like(
                                            cb.lower(reqRoot.get("requesterUsername")),
                                            like(search.getRequesterUsername())
                                    )
                            )
                    ).stream().map(FleetTransportRequest::getId).toList();
                    if (requestIds.isEmpty()) {
                        predicates.add(criteriaBuilder.disjunction());
                    } else {
                        predicates.add(root.get("transportRequestId").in(requestIds));
                    }
                }
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private Map<Long, FleetTransportRequest> loadRequests(Long companyId, List<FleetTrip> trips) {
        List<Long> ids = trips.stream()
                .map(FleetTrip::getTransportRequestId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (ids.isEmpty()) {
            return Map.of();
        }
        Map<Long, FleetTransportRequest> map = new HashMap<>();
        for (Long id : ids) {
            transportRequestRepository.findByIdAndCompanyId(id, companyId)
                    .ifPresent(r -> map.put(id, r));
        }
        return map;
    }

    private Map<Long, FleetVehicle> loadVehicles(Long companyId, List<FleetTrip> trips) {
        List<Long> ids = trips.stream()
                .map(FleetTrip::getVehicleId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (ids.isEmpty()) {
            return Map.of();
        }
        Map<Long, FleetVehicle> map = new HashMap<>();
        for (Long id : ids) {
            fleetVehicleRepository.findByIdAndCompanyId(id, companyId)
                    .ifPresent(v -> map.put(id, v));
        }
        return map;
    }

    private Map<Long, FleetDriver> loadDrivers(Long companyId, List<FleetTrip> trips) {
        List<Long> ids = trips.stream()
                .map(FleetTrip::getDriverId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (ids.isEmpty()) {
            return Map.of();
        }
        Map<Long, FleetDriver> map = new HashMap<>();
        for (Long id : ids) {
            fleetDriverRepository.findByIdAndCompanyId(id, companyId)
                    .ifPresent(d -> map.put(id, d));
        }
        return map;
    }

    private TripDto toTripDto(FleetTrip trip) {
        FleetTransportRequest request = getRequestForCompany(trip.getTransportRequestId());
        FleetVehicle vehicle = fleetVehicleRepository.findByIdAndCompanyId(trip.getVehicleId(), trip.getCompanyId())
                .orElse(null);
        FleetDriver driver = fleetDriverRepository.findByIdAndCompanyId(trip.getDriverId(), trip.getCompanyId())
                .orElse(null);
        return toTripDto(trip, request, vehicle, driver);
    }

    private TripDto toTripDto(
            FleetTrip trip,
            FleetTransportRequest request,
            FleetVehicle vehicle,
            FleetDriver driver
    ) {
        return TripDto.builder()
                .id(trip.getId())
                .companyId(trip.getCompanyId())
                .tripNumber(trip.getTripNumber())
                .transportRequestId(trip.getTransportRequestId())
                .requestNumber(request != null ? request.getRequestNumber() : null)
                .requesterUsername(request != null ? request.getRequesterUsername() : null)
                .pickupLocation(request != null ? request.getPickupLocation() : null)
                .dropLocation(request != null ? request.getDropLocation() : null)
                .passengerCount(request != null ? request.getPassengerCount() : null)
                .vehicleId(trip.getVehicleId())
                .vehicleRegistration(vehicle != null ? vehicle.getRegistration() : null)
                .driverId(trip.getDriverId())
                .driverName(driver != null ? driver.getName() : null)
                .driverMobile(driver != null ? driver.getMobile() : null)
                .scheduledPickupAt(trip.getScheduledPickupAt())
                .assignedAt(trip.getAssignedAt())
                .assignedBy(trip.getAssignedBy())
                .notes(trip.getNotes())
                .status(trip.getStatus().name())
                .statusDescription(toStatusDescription(trip.getStatus()))
                .createdDate(trip.getCreatedDate())
                .lastModifiedDate(trip.getLastModifiedDate())
                .createdBy(trip.getCreatedBy())
                .lastModifiedBy(trip.getLastModifiedBy())
                .build();
    }

    private TripListItemDto toListItemDto(
            FleetTrip trip,
            FleetTransportRequest request,
            FleetVehicle vehicle,
            FleetDriver driver
    ) {
        return TripListItemDto.builder()
                .id(trip.getId())
                .tripNumber(trip.getTripNumber())
                .requestNumber(request != null ? request.getRequestNumber() : null)
                .requesterUsername(request != null ? request.getRequesterUsername() : null)
                .pickupLocation(request != null ? request.getPickupLocation() : null)
                .dropLocation(request != null ? request.getDropLocation() : null)
                .passengerCount(request != null ? request.getPassengerCount() : null)
                .vehicleId(trip.getVehicleId())
                .vehicleRegistration(vehicle != null ? vehicle.getRegistration() : null)
                .driverId(trip.getDriverId())
                .driverName(driver != null ? driver.getName() : null)
                .scheduledPickupAt(trip.getScheduledPickupAt())
                .status(trip.getStatus().name())
                .statusDescription(toStatusDescription(trip.getStatus()))
                .createdDate(trip.getCreatedDate())
                .lastModifiedDate(trip.getLastModifiedDate())
                .createdBy(trip.getCreatedBy())
                .lastModifiedBy(trip.getLastModifiedBy())
                .build();
    }

    private void applyAudit(FleetTrip trip, String username) {
        trip.setLastModifiedDate(LocalDateTime.now());
        trip.setLastModifiedBy(hasText(username) ? username.trim() : "system");
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
            return "scheduledPickupAt";
        }
        return switch (sortColumn) {
            case "tripNumber", "scheduledPickupAt", "status", "vehicleId", "driverId",
                 "assignedAt", "createdDate", "lastModifiedDate", "createdBy" -> sortColumn;
            default -> "scheduledPickupAt";
        };
    }

    private Sort.Direction resolveDirection(String direction) {
        return "DESC".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
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