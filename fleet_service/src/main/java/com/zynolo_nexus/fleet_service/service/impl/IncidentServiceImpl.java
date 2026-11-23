package com.zynolo_nexus.fleet_service.service.impl;

import com.zynolo_nexus.fleet_service.context.CompanyContext;
import com.zynolo_nexus.fleet_service.dto.request.IncidentCreateRequest;
import com.zynolo_nexus.fleet_service.dto.request.IncidentFilterRequest;
import com.zynolo_nexus.fleet_service.dto.request.IncidentFilterSearch;
import com.zynolo_nexus.fleet_service.dto.request.IncidentReferenceDataRequest;
import com.zynolo_nexus.fleet_service.dto.request.IncidentViewRequest;
import com.zynolo_nexus.fleet_service.dto.response.IncidentDto;
import com.zynolo_nexus.fleet_service.dto.response.IncidentFilterResultDto;
import com.zynolo_nexus.fleet_service.dto.response.IncidentListItemDto;
import com.zynolo_nexus.fleet_service.dto.response.IncidentPrivilegesDto;
import com.zynolo_nexus.fleet_service.dto.response.IncidentReferenceDataDto;
import com.zynolo_nexus.fleet_service.dto.response.ReferenceOptionDto;
import com.zynolo_nexus.fleet_service.enums.IncidentSeverity;
import com.zynolo_nexus.fleet_service.enums.IncidentStatus;
import com.zynolo_nexus.fleet_service.enums.IncidentType;
import com.zynolo_nexus.fleet_service.enums.TripStatus;
import com.zynolo_nexus.fleet_service.exception.BadRequestException;
import com.zynolo_nexus.fleet_service.exception.ResourceNotFoundException;
import com.zynolo_nexus.fleet_service.model.FleetDriver;
import com.zynolo_nexus.fleet_service.model.FleetTrip;
import com.zynolo_nexus.fleet_service.model.FleetTripIncident;
import com.zynolo_nexus.fleet_service.repository.FleetDriverRepository;
import com.zynolo_nexus.fleet_service.repository.FleetTripIncidentRepository;
import com.zynolo_nexus.fleet_service.repository.FleetTripRepository;
import com.zynolo_nexus.fleet_service.service.IncidentService;
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
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class IncidentServiceImpl implements IncidentService {

    private static final String PAGE_CODE = "FLDI";
    private static final Set<TripStatus> REPORTABLE = EnumSet.of(
            TripStatus.ASSIGNED,
            TripStatus.STARTED,
            TripStatus.COMPLETED
    );

    private final FleetTripIncidentRepository incidentRepository;
    private final FleetTripRepository fleetTripRepository;
    private final FleetDriverRepository fleetDriverRepository;
    private final PagePrivilegeResolver pagePrivilegeResolver;

    @Value("${app.default.company-id:1}")
    private Long defaultCompanyId;

    @Override
    @Transactional(readOnly = true)
    public IncidentReferenceDataDto getReferenceData(IncidentReferenceDataRequest request) {
        FleetDriver driver = requireLinkedDriver(request.getUsername());
        var privileges = pagePrivilegeResolver.resolve(
                request != null ? request.getUsername() : null,
                PAGE_CODE
        );
        return IncidentReferenceDataDto.builder()
                .driverId(driver.getId())
                .driverName(driver.getName())
                .defaultIncidentType(List.of(
                        option(IncidentType.BREAKDOWN.name(), "Breakdown"),
                        option(IncidentType.ACCIDENT.name(), "Accident"),
                        option(IncidentType.DELAY.name(), "Delay"),
                        option(IncidentType.PASSENGER.name(), "Passenger"),
                        option(IncidentType.OTHER.name(), "Other")
                ))
                .defaultSeverity(List.of(
                        option(IncidentSeverity.LOW.name(), "Low"),
                        option(IncidentSeverity.MEDIUM.name(), "Medium"),
                        option(IncidentSeverity.HIGH.name(), "High")
                ))
                .defaultStatus(List.of(
                        option(IncidentStatus.OPEN.name(), "Open"),
                        option(IncidentStatus.CLOSED.name(), "Closed")
                ))
                .privileges(IncidentPrivilegesDto.builder()
                        .add(privileges.isAdd())
                        .view(privileges.isView())
                        .search(privileges.isSearch())
                        .build())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public IncidentFilterResultDto filterList(IncidentFilterRequest request) {
        Long companyId = requireCompanyId();
        FleetDriver driver = requireLinkedDriver(request.getUsername());
        Pageable pageable = PageRequest.of(
                request.getPage(),
                request.getSize(),
                Sort.by(resolveDirection(request.getSortDirection()), resolveSortColumn(request.getSortColumn()))
        );

        Page<FleetTripIncident> page = incidentRepository.findAll(
                buildSpecification(companyId, driver.getId(), request.getSearch()),
                pageable
        );

        Map<Long, String> tripNumbers = loadTripNumbers(companyId, page.getContent());
        List<IncidentListItemDto> content = page.getContent().stream()
                .map(incident -> toListItemDto(incident, tripNumbers.get(incident.getTripId())))
                .toList();

        return IncidentFilterResultDto.builder()
                .content(content)
                .size(content.size())
                .totalRecords(page.getTotalElements())
                .build();
    }

    @Override
    @Transactional
    public IncidentDto create(IncidentCreateRequest request) {
        Long companyId = requireCompanyId();
        FleetDriver driver = requireLinkedDriver(request.getUsername());
        FleetTrip trip = getTripForCompany(request.getTripId());
        if (!Objects.equals(trip.getDriverId(), driver.getId())) {
            throw new BadRequestException("You can only report incidents on your own trips");
        }
        if (!REPORTABLE.contains(trip.getStatus())) {
            throw new BadRequestException("Incidents can only be reported on ASSIGNED, STARTED, or COMPLETED trips");
        }

        LocalDateTime now = LocalDateTime.now();
        FleetTripIncident incident = new FleetTripIncident();
        incident.setCompanyId(companyId);
        incident.setTripId(trip.getId());
        incident.setDriverId(driver.getId());
        incident.setIncidentType(parseType(request.getIncidentType()));
        incident.setSeverity(parseSeverity(request.getSeverity()));
        incident.setDescription(trimRequired(request.getDescription(), "description"));
        incident.setLocationNote(trimToNull(request.getLocationNote()));
        incident.setReportedAt(now);
        incident.setStatus(IncidentStatus.OPEN);
        applyAudit(incident, request.getUsername(), true);

        return toDto(incidentRepository.save(incident), trip.getTripNumber(), driver.getName());
    }

    @Override
    @Transactional(readOnly = true)
    public IncidentDto view(IncidentViewRequest request) {
        FleetDriver driver = requireLinkedDriver(request.getUsername());
        FleetTripIncident incident = getOwnIncident(request.getId(), driver.getId());
        String tripNumber = fleetTripRepository.findByIdAndCompanyId(incident.getTripId(), incident.getCompanyId())
                .map(FleetTrip::getTripNumber)
                .orElse(null);
        return toDto(incident, tripNumber, driver.getName());
    }

    private FleetTripIncident getOwnIncident(Long id, Long driverId) {
        FleetTripIncident incident = getIncidentForCompany(id);
        if (!Objects.equals(incident.getDriverId(), driverId)) {
            throw new BadRequestException("You can only view your own incidents");
        }
        return incident;
    }

    private FleetTripIncident getIncidentForCompany(Long id) {
        Long companyId = requireCompanyId();
        return incidentRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found with ID: " + id));
    }

    private FleetTrip getTripForCompany(Long id) {
        Long companyId = requireCompanyId();
        return fleetTripRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found with ID: " + id));
    }

    private FleetDriver requireLinkedDriver(String username) {
        Long companyId = requireCompanyId();
        String actor = requireUsername(username);
        return fleetDriverRepository.findByCompanyIdAndLinkedUsernameIgnoreCase(companyId, actor)
                .orElseThrow(() -> new BadRequestException(
                        "No driver profile linked to username: " + actor + ". Set linkedUsername on Driver Master."
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

    private String requireUsername(String username) {
        if (!hasText(username)) {
            throw new BadRequestException("username is required");
        }
        return username.trim();
    }

    private Specification<FleetTripIncident> buildSpecification(
            Long companyId,
            Long driverId,
            IncidentFilterSearch search
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("companyId"), companyId));
            predicates.add(criteriaBuilder.equal(root.get("driverId"), driverId));

            if (search != null) {
                if (search.getTripId() != null) {
                    predicates.add(criteriaBuilder.equal(root.get("tripId"), search.getTripId()));
                }
                if (hasText(search.getIncidentType())) {
                    predicates.add(criteriaBuilder.equal(root.get("incidentType"), parseType(search.getIncidentType())));
                }
                if (hasText(search.getSeverity())) {
                    predicates.add(criteriaBuilder.equal(root.get("severity"), parseSeverity(search.getSeverity())));
                }
                if (hasText(search.getStatus())) {
                    predicates.add(criteriaBuilder.equal(root.get("status"), parseStatus(search.getStatus())));
                }
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private Map<Long, String> loadTripNumbers(Long companyId, List<FleetTripIncident> incidents) {
        Map<Long, String> map = new HashMap<>();
        incidents.stream()
                .map(FleetTripIncident::getTripId)
                .filter(Objects::nonNull)
                .distinct()
                .forEach(id -> fleetTripRepository.findByIdAndCompanyId(id, companyId)
                        .ifPresent(t -> map.put(id, t.getTripNumber())));
        return map;
    }

    private IncidentDto toDto(FleetTripIncident incident, String tripNumber, String driverName) {
        return IncidentDto.builder()
                .id(incident.getId())
                .companyId(incident.getCompanyId())
                .tripId(incident.getTripId())
                .tripNumber(tripNumber)
                .driverId(incident.getDriverId())
                .driverName(driverName)
                .incidentType(incident.getIncidentType().name())
                .incidentTypeDescription(toTypeDescription(incident.getIncidentType()))
                .severity(incident.getSeverity().name())
                .severityDescription(toSeverityDescription(incident.getSeverity()))
                .description(incident.getDescription())
                .locationNote(incident.getLocationNote())
                .reportedAt(incident.getReportedAt())
                .status(incident.getStatus().name())
                .statusDescription(toStatusDescription(incident.getStatus()))
                .createdDate(incident.getCreatedDate())
                .lastModifiedDate(incident.getLastModifiedDate())
                .createdBy(incident.getCreatedBy())
                .lastModifiedBy(incident.getLastModifiedBy())
                .build();
    }

    private IncidentListItemDto toListItemDto(FleetTripIncident incident, String tripNumber) {
        return IncidentListItemDto.builder()
                .id(incident.getId())
                .tripId(incident.getTripId())
                .tripNumber(tripNumber)
                .incidentType(incident.getIncidentType().name())
                .incidentTypeDescription(toTypeDescription(incident.getIncidentType()))
                .severity(incident.getSeverity().name())
                .severityDescription(toSeverityDescription(incident.getSeverity()))
                .status(incident.getStatus().name())
                .statusDescription(toStatusDescription(incident.getStatus()))
                .reportedAt(incident.getReportedAt())
                .createdDate(incident.getCreatedDate())
                .createdBy(incident.getCreatedBy())
                .build();
    }

    private void applyAudit(FleetTripIncident incident, String username, boolean create) {
        LocalDateTime now = LocalDateTime.now();
        String actor = hasText(username) ? username.trim() : "system";
        if (create) {
            incident.setCreatedDate(now);
            incident.setCreatedBy(actor);
        }
        incident.setLastModifiedDate(now);
        incident.setLastModifiedBy(actor);
    }

    private IncidentType parseType(String value) {
        try {
            return IncidentType.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (Exception ex) {
            throw new BadRequestException("Invalid incidentType: " + value);
        }
    }

    private IncidentSeverity parseSeverity(String value) {
        try {
            return IncidentSeverity.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (Exception ex) {
            throw new BadRequestException("Invalid severity: " + value);
        }
    }

    private IncidentStatus parseStatus(String value) {
        try {
            return IncidentStatus.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (Exception ex) {
            throw new BadRequestException("Invalid status: " + value);
        }
    }

    private String resolveSortColumn(String sortColumn) {
        if (!hasText(sortColumn)) {
            return "reportedAt";
        }
        return switch (sortColumn) {
            case "reportedAt", "incidentType", "severity", "status", "tripId",
                 "createdDate", "lastModifiedDate", "createdBy" -> sortColumn;
            default -> "reportedAt";
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

    private String toTypeDescription(IncidentType type) {
        return switch (type) {
            case BREAKDOWN -> "Breakdown";
            case ACCIDENT -> "Accident";
            case DELAY -> "Delay";
            case PASSENGER -> "Passenger";
            case OTHER -> "Other";
        };
    }

    private String toSeverityDescription(IncidentSeverity severity) {
        return switch (severity) {
            case LOW -> "Low";
            case MEDIUM -> "Medium";
            case HIGH -> "High";
        };
    }

    private String toStatusDescription(IncidentStatus status) {
        return status == IncidentStatus.OPEN ? "Open" : "Closed";
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
}