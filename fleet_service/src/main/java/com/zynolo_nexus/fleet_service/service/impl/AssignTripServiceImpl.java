package com.zynolo_nexus.fleet_service.service.impl;

import com.zynolo_nexus.fleet_service.context.CompanyContext;
import com.zynolo_nexus.fleet_service.dto.request.AssignTripCreateRequest;
import com.zynolo_nexus.fleet_service.dto.request.AssignTripReferenceDataRequest;
import com.zynolo_nexus.fleet_service.dto.request.AssignTripUpdateRequest;
import com.zynolo_nexus.fleet_service.dto.request.AssignTripViewRequest;
import com.zynolo_nexus.fleet_service.dto.request.TransportRequestFilterRequest;
import com.zynolo_nexus.fleet_service.dto.request.TransportRequestFilterSearch;
import com.zynolo_nexus.fleet_service.dto.request.TransportRequestViewRequest;
import com.zynolo_nexus.fleet_service.dto.response.AssignTripDriverOptionDto;
import com.zynolo_nexus.fleet_service.dto.response.AssignTripPrivilegesDto;
import com.zynolo_nexus.fleet_service.dto.response.AssignTripReferenceDataDto;
import com.zynolo_nexus.fleet_service.dto.response.DriverVehicleOptionDto;
import com.zynolo_nexus.fleet_service.dto.response.ReferenceOptionDto;
import com.zynolo_nexus.fleet_service.dto.response.TransportRequestDto;
import com.zynolo_nexus.fleet_service.dto.response.TransportRequestFilterResultDto;
import com.zynolo_nexus.fleet_service.dto.response.TransportRequestListItemDto;
import com.zynolo_nexus.fleet_service.dto.response.TripDto;
import com.zynolo_nexus.fleet_service.enums.MasterStatus;
import com.zynolo_nexus.fleet_service.enums.TransportRequestStatus;
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
import com.zynolo_nexus.fleet_service.service.AssignTripService;
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
public class AssignTripServiceImpl implements AssignTripService {

    private static final String PAGE_CODE = "FLAT";
    private static final DateTimeFormatter TRIP_DATE = DateTimeFormatter.BASIC_ISO_DATE;

    private final FleetTripRepository fleetTripRepository;
    private final FleetTransportRequestRepository transportRequestRepository;
    private final FleetVehicleRepository fleetVehicleRepository;
    private final FleetDriverRepository fleetDriverRepository;
    private final PagePrivilegeResolver pagePrivilegeResolver;

    @Value("${app.default.company-id:1}")
    private Long defaultCompanyId;

    @Override
    @Transactional(readOnly = true)
    public AssignTripReferenceDataDto getReferenceData(AssignTripReferenceDataRequest request) {
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

        return AssignTripReferenceDataDto.builder()
                .defaultRequestStatus(List.of(
                        option(TransportRequestStatus.SUBMITTED.name(), "Submitted")
                ))
                .defaultTripStatus(List.of(
                        option(TripStatus.ASSIGNED.name(), "Assigned"),
                        option(TripStatus.CANCELLED.name(), "Cancelled")
                ))
                .vehicles(vehicles)
                .drivers(drivers)
                .privileges(AssignTripPrivilegesDto.builder()
                        .add(privileges.isAdd())
                        .update(privileges.isUpdate())
                        .view(privileges.isView())
                        .search(privileges.isSearch())
                        .build())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public TransportRequestFilterResultDto filterOpenRequests(TransportRequestFilterRequest request) {
        Long companyId = requireCompanyId();
        Pageable pageable = PageRequest.of(
                request.getPage(),
                request.getSize(),
                Sort.by(resolveRequestDirection(request.getSortDirection()), resolveRequestSortColumn(request.getSortColumn()))
        );

        Page<FleetTransportRequest> page = transportRequestRepository.findAll(
                buildOpenRequestSpecification(companyId, request.getSearch()),
                pageable
        );

        List<TransportRequestListItemDto> content = page.getContent().stream()
                .map(this::toRequestListItemDto)
                .toList();

        return TransportRequestFilterResultDto.builder()
                .content(content)
                .size(content.size())
                .totalRecords(page.getTotalElements())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public TransportRequestDto viewRequest(TransportRequestViewRequest request) {
        FleetTransportRequest entity = getRequestForCompany(request.getId());
        if (entity.getStatus() != TransportRequestStatus.SUBMITTED
                && entity.getStatus() != TransportRequestStatus.ASSIGNED) {
            throw new BadRequestException("Only SUBMITTED or ASSIGNED requests can be viewed on Assign Trip");
        }
        return toRequestDto(entity);
    }

    @Override
    @Transactional
    public TripDto assign(AssignTripCreateRequest request) {
        Long companyId = requireCompanyId();
        String actor = requireUsername(request.getUsername());

        FleetTransportRequest transportRequest = getRequestForCompany(request.getTransportRequestId());
        if (transportRequest.getStatus() != TransportRequestStatus.SUBMITTED) {
            throw new BadRequestException("Only SUBMITTED requests can be assigned");
        }
        if (fleetTripRepository.existsByTransportRequestId(transportRequest.getId())) {
            throw new BadRequestException("Transport request already has an assigned trip");
        }

        FleetVehicle vehicle = requireActiveVehicle(companyId, request.getVehicleId());
        FleetDriver driver = requireActiveDriver(companyId, request.getDriverId());
        if (vehicle.getPassengerCapacity() != null
                && transportRequest.getPassengerCount() != null
                && vehicle.getPassengerCapacity() < transportRequest.getPassengerCount()) {
            throw new BadRequestException("Vehicle passenger capacity is less than requested passenger count");
        }

        LocalDateTime scheduledPickupAt = request.getScheduledPickupAt() != null
                ? request.getScheduledPickupAt()
                : transportRequest.getRequestedPickupAt();

        LocalDateTime now = LocalDateTime.now();
        FleetTrip trip = new FleetTrip();
        trip.setCompanyId(companyId);
        trip.setTripNumber(nextTripNumber(companyId));
        trip.setTransportRequestId(transportRequest.getId());
        trip.setVehicleId(vehicle.getId());
        trip.setDriverId(driver.getId());
        trip.setScheduledPickupAt(scheduledPickupAt);
        trip.setAssignedAt(now);
        trip.setAssignedBy(actor);
        trip.setNotes(trimToNull(request.getNotes()));
        trip.setStatus(TripStatus.ASSIGNED);
        applyAudit(trip, actor, true);

        transportRequest.setStatus(TransportRequestStatus.ASSIGNED);
        transportRequest.setLastModifiedDate(now);
        transportRequest.setLastModifiedBy(actor);
        transportRequestRepository.save(transportRequest);

        return toTripDto(fleetTripRepository.save(trip), transportRequest, vehicle, driver);
    }

    @Override
    @Transactional(readOnly = true)
    public TripDto view(AssignTripViewRequest request) {
        FleetTrip trip = getTripForCompany(request.getId());
        return toTripDto(trip);
    }

    @Override
    @Transactional
    public TripDto update(AssignTripUpdateRequest request) {
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
        applyAudit(trip, request.getUsername(), false);

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

    private String nextTripNumber(Long companyId) {
        String datePart = LocalDate.now().format(TRIP_DATE);
        for (int i = 0; i < 10; i++) {
            int suffix = ThreadLocalRandom.current().nextInt(1000, 10000);
            String candidate = "FLT-" + datePart + "-" + suffix;
            if (!fleetTripRepository.existsByCompanyIdAndTripNumber(companyId, candidate)) {
                return candidate;
            }
        }
        throw new BadRequestException("Unable to generate trip number, please retry");
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

    private Specification<FleetTransportRequest> buildOpenRequestSpecification(
            Long companyId,
            TransportRequestFilterSearch search
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("companyId"), companyId));
            predicates.add(criteriaBuilder.equal(root.get("status"), TransportRequestStatus.SUBMITTED));

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
                .statusDescription(trip.getStatus() == TripStatus.ASSIGNED ? "Assigned" : "Cancelled")
                .createdDate(trip.getCreatedDate())
                .lastModifiedDate(trip.getLastModifiedDate())
                .createdBy(trip.getCreatedBy())
                .lastModifiedBy(trip.getLastModifiedBy())
                .build();
    }

    private TransportRequestDto toRequestDto(FleetTransportRequest entity) {
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
                .statusDescription(toRequestStatusDescription(entity.getStatus()))
                .submittedAt(entity.getSubmittedAt())
                .createdDate(entity.getCreatedDate())
                .lastModifiedDate(entity.getLastModifiedDate())
                .createdBy(entity.getCreatedBy())
                .lastModifiedBy(entity.getLastModifiedBy())
                .build();
    }

    private TransportRequestListItemDto toRequestListItemDto(FleetTransportRequest entity) {
        return TransportRequestListItemDto.builder()
                .id(entity.getId())
                .requestNumber(entity.getRequestNumber())
                .requesterUsername(entity.getRequesterUsername())
                .pickupLocation(entity.getPickupLocation())
                .dropLocation(entity.getDropLocation())
                .requestedPickupAt(entity.getRequestedPickupAt())
                .passengerCount(entity.getPassengerCount())
                .status(entity.getStatus().name())
                .statusDescription(toRequestStatusDescription(entity.getStatus()))
                .createdDate(entity.getCreatedDate())
                .lastModifiedDate(entity.getLastModifiedDate())
                .createdBy(entity.getCreatedBy())
                .lastModifiedBy(entity.getLastModifiedBy())
                .build();
    }

    private void applyAudit(FleetTrip trip, String username, boolean create) {
        LocalDateTime now = LocalDateTime.now();
        String actor = hasText(username) ? username.trim() : "system";
        if (create) {
            trip.setCreatedDate(now);
            trip.setCreatedBy(actor);
        }
        trip.setLastModifiedDate(now);
        trip.setLastModifiedBy(actor);
    }

    private String toRequestStatusDescription(TransportRequestStatus status) {
        return switch (status) {
            case DRAFT -> "Draft";
            case SUBMITTED -> "Submitted";
            case ASSIGNED -> "Assigned";
            case CANCELLED -> "Cancelled";
        };
    }

    private String resolveRequestSortColumn(String sortColumn) {
        if (!hasText(sortColumn)) {
            return "submittedAt";
        }
        return switch (sortColumn) {
            case "requestNumber", "requesterUsername", "pickupLocation", "dropLocation", "requestedPickupAt",
                 "passengerCount", "status", "submittedAt", "createdDate", "lastModifiedDate", "createdBy" -> sortColumn;
            default -> "submittedAt";
        };
    }

    private Sort.Direction resolveRequestDirection(String direction) {
        return "DESC".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
    }

    private ReferenceOptionDto option(String code, String description) {
        return ReferenceOptionDto.builder()
                .code(code)
                .description(description)
                .build();
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