package com.zynolo_nexus.fleet_service.service.impl;

import com.zynolo_nexus.fleet_service.context.CompanyContext;
import com.zynolo_nexus.fleet_service.dto.request.MyAssignmentFilterRequest;
import com.zynolo_nexus.fleet_service.dto.request.MyAssignmentFilterSearch;
import com.zynolo_nexus.fleet_service.dto.request.MyAssignmentReferenceDataRequest;
import com.zynolo_nexus.fleet_service.dto.request.MyAssignmentViewRequest;
import com.zynolo_nexus.fleet_service.dto.response.MyAssignmentPrivilegesDto;
import com.zynolo_nexus.fleet_service.dto.response.MyAssignmentReferenceDataDto;
import com.zynolo_nexus.fleet_service.dto.response.ReferenceOptionDto;
import com.zynolo_nexus.fleet_service.dto.response.TripDto;
import com.zynolo_nexus.fleet_service.dto.response.TripFilterResultDto;
import com.zynolo_nexus.fleet_service.dto.response.TripListItemDto;
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
import com.zynolo_nexus.fleet_service.service.MyAssignmentService;
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

@Service
@RequiredArgsConstructor
public class MyAssignmentServiceImpl implements MyAssignmentService {

    private static final String PAGE_CODE = "FLDA";

    private final FleetTripRepository fleetTripRepository;
    private final FleetTransportRequestRepository transportRequestRepository;
    private final FleetVehicleRepository fleetVehicleRepository;
    private final FleetDriverRepository fleetDriverRepository;
    private final PagePrivilegeResolver pagePrivilegeResolver;

    @Value("${app.default.company-id:1}")
    private Long defaultCompanyId;

    @Override
    @Transactional(readOnly = true)
    public MyAssignmentReferenceDataDto getReferenceData(MyAssignmentReferenceDataRequest request) {
        FleetDriver driver = requireLinkedDriver(request.getUsername());
        var privileges = pagePrivilegeResolver.resolve(
                request != null ? request.getUsername() : null,
                PAGE_CODE
        );
        return MyAssignmentReferenceDataDto.builder()
                .driverId(driver.getId())
                .driverName(driver.getName())
                .defaultTripStatus(List.of(
                        option(TripStatus.ASSIGNED.name(), "Assigned"),
                        option(TripStatus.STARTED.name(), "Started"),
                        option(TripStatus.COMPLETED.name(), "Completed"),
                        option(TripStatus.CANCELLED.name(), "Cancelled")
                ))
                .privileges(MyAssignmentPrivilegesDto.builder()
                        .view(privileges.isView())
                        .search(privileges.isSearch())
                        .build())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public TripFilterResultDto filterList(MyAssignmentFilterRequest request) {
        Long companyId = requireCompanyId();
        FleetDriver driver = requireLinkedDriver(request.getUsername());
        Pageable pageable = PageRequest.of(
                request.getPage(),
                request.getSize(),
                Sort.by(resolveDirection(request.getSortDirection()), resolveSortColumn(request.getSortColumn()))
        );

        Page<FleetTrip> page = fleetTripRepository.findAll(
                buildSpecification(companyId, driver.getId(), request.getSearch()),
                pageable
        );

        Map<Long, FleetTransportRequest> requests = loadRequests(companyId, page.getContent());
        Map<Long, FleetVehicle> vehicles = loadVehicles(companyId, page.getContent());

        List<TripListItemDto> content = page.getContent().stream()
                .map(trip -> toListItemDto(
                        trip,
                        requests.get(trip.getTransportRequestId()),
                        vehicles.get(trip.getVehicleId()),
                        driver
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
    public TripDto view(MyAssignmentViewRequest request) {
        FleetDriver driver = requireLinkedDriver(request.getUsername());
        FleetTrip trip = getTripForCompany(request.getId());
        if (!Objects.equals(trip.getDriverId(), driver.getId())) {
            throw new BadRequestException("You can only view your own assigned trips");
        }
        return toTripDto(trip, driver);
    }

    private FleetDriver requireLinkedDriver(String username) {
        Long companyId = requireCompanyId();
        String actor = requireUsername(username);
        return fleetDriverRepository.findByCompanyIdAndLinkedUsernameIgnoreCase(companyId, actor)
                .orElseThrow(() -> new BadRequestException(
                        "No driver profile linked to username: " + actor + ". Set linkedUsername on Driver Master."
                ));
    }

    private FleetTrip getTripForCompany(Long id) {
        Long companyId = requireCompanyId();
        return fleetTripRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found with ID: " + id));
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

    private Specification<FleetTrip> buildSpecification(
            Long companyId,
            Long driverId,
            MyAssignmentFilterSearch search
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("companyId"), companyId));
            predicates.add(criteriaBuilder.equal(root.get("driverId"), driverId));

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
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
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

    private Map<Long, FleetVehicle> loadVehicles(Long companyId, List<FleetTrip> trips) {
        Map<Long, FleetVehicle> map = new HashMap<>();
        trips.stream()
                .map(FleetTrip::getVehicleId)
                .filter(Objects::nonNull)
                .distinct()
                .forEach(id -> fleetVehicleRepository.findByIdAndCompanyId(id, companyId)
                        .ifPresent(v -> map.put(id, v)));
        return map;
    }

    private TripDto toTripDto(FleetTrip trip, FleetDriver driver) {
        FleetTransportRequest request = transportRequestRepository
                .findByIdAndCompanyId(trip.getTransportRequestId(), trip.getCompanyId())
                .orElse(null);
        FleetVehicle vehicle = fleetVehicleRepository
                .findByIdAndCompanyId(trip.getVehicleId(), trip.getCompanyId())
                .orElse(null);
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
                .startedAt(trip.getStartedAt())
                .completedAt(trip.getCompletedAt())
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
            case "tripNumber", "scheduledPickupAt", "status", "assignedAt",
                 "createdDate", "lastModifiedDate", "createdBy" -> sortColumn;
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

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String like(String value) {
        return "%" + value.trim().toLowerCase(Locale.ROOT) + "%";
    }
}