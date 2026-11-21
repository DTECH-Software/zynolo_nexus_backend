package com.zynolo_nexus.fleet_service.service.impl;

import com.zynolo_nexus.fleet_service.context.CompanyContext;
import com.zynolo_nexus.fleet_service.dto.request.DriverCreateRequest;
import com.zynolo_nexus.fleet_service.dto.request.DriverFilterRequest;
import com.zynolo_nexus.fleet_service.dto.request.DriverFilterSearch;
import com.zynolo_nexus.fleet_service.dto.request.DriverReferenceDataRequest;
import com.zynolo_nexus.fleet_service.dto.request.DriverStatusUpdateRequest;
import com.zynolo_nexus.fleet_service.dto.request.DriverUpdateRequest;
import com.zynolo_nexus.fleet_service.dto.request.DriverViewRequest;
import com.zynolo_nexus.fleet_service.dto.response.DriverDto;
import com.zynolo_nexus.fleet_service.dto.response.DriverFilterResultDto;
import com.zynolo_nexus.fleet_service.dto.response.DriverListItemDto;
import com.zynolo_nexus.fleet_service.dto.response.DriverPrivilegesDto;
import com.zynolo_nexus.fleet_service.dto.response.DriverReferenceDataDto;
import com.zynolo_nexus.fleet_service.dto.response.DriverVehicleOptionDto;
import com.zynolo_nexus.fleet_service.dto.response.ReferenceOptionDto;
import com.zynolo_nexus.fleet_service.enums.DriverAvailability;
import com.zynolo_nexus.fleet_service.enums.MasterStatus;
import com.zynolo_nexus.fleet_service.exception.BadRequestException;
import com.zynolo_nexus.fleet_service.exception.ResourceNotFoundException;
import com.zynolo_nexus.fleet_service.model.FleetDriver;
import com.zynolo_nexus.fleet_service.model.FleetVehicle;
import com.zynolo_nexus.fleet_service.repository.FleetDriverRepository;
import com.zynolo_nexus.fleet_service.repository.FleetVehicleRepository;
import com.zynolo_nexus.fleet_service.service.DriverService;
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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DriverServiceImpl implements DriverService {

    private static final String PAGE_CODE = "FLDM";

    private final FleetDriverRepository fleetDriverRepository;
    private final FleetVehicleRepository fleetVehicleRepository;
    private final PagePrivilegeResolver pagePrivilegeResolver;

    @Value("${app.default.company-id:1}")
    private Long defaultCompanyId;

    @Override
    @Transactional(readOnly = true)
    public DriverReferenceDataDto getReferenceData(DriverReferenceDataRequest request) {
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

        return DriverReferenceDataDto.builder()
                .defaultStatus(List.of(
                        option(MasterStatus.ACTIVE.name(), "Active"),
                        option(MasterStatus.INACTIVE.name(), "Inactive")
                ))
                .defaultAvailability(List.of(
                        option(DriverAvailability.AVAILABLE.name(), "Available"),
                        option(DriverAvailability.UNAVAILABLE.name(), "Unavailable")
                ))
                .vehicles(vehicles)
                .privileges(DriverPrivilegesDto.builder()
                        .add(privileges.isAdd())
                        .update(privileges.isUpdate())
                        .view(privileges.isView())
                        .search(privileges.isSearch())
                        .delete(privileges.isDelete())
                        .build())
                .build();
    }

    @Override
    @Transactional
    public DriverDto create(DriverCreateRequest request) {
        Long companyId = requireCompanyId();
        String name = trimRequired(request.getName(), "name");
        String mobile = trimRequired(request.getMobile(), "mobile");
        String normalizedMobile = normalizeMobile(mobile);

        if (fleetDriverRepository.existsByCompanyIdAndMobileNormalized(companyId, normalizedMobile)) {
            throw new BadRequestException("Driver mobile already exists: " + mobile);
        }

        Long usualVehicleId = resolveUsualVehicleId(companyId, request.getUsualVehicleId());

        FleetDriver driver = new FleetDriver();
        driver.setCompanyId(companyId);
        driver.setName(name);
        driver.setMobile(mobile);
        driver.setMobileNormalized(normalizedMobile);
        driver.setLicenceNumber(trimToNull(request.getLicenceNumber()));
        driver.setLinkedUsername(trimToNull(request.getLinkedUsername()));
        driver.setUsualVehicleId(usualVehicleId);
        driver.setAvailability(parseAvailability(request.getAvailability(), DriverAvailability.AVAILABLE));
        driver.setNotes(trimToNull(request.getNotes()));
        driver.setStatus(MasterStatus.ACTIVE);
        applyAudit(driver, request.getUsername(), true);

        return toDto(fleetDriverRepository.save(driver));
    }

    @Override
    @Transactional(readOnly = true)
    public DriverDto view(DriverViewRequest request) {
        return toDto(getByIdForCompany(request.getId()));
    }

    @Override
    @Transactional
    public DriverDto update(DriverUpdateRequest request) {
        FleetDriver driver = getByIdForCompany(request.getId());
        Long companyId = driver.getCompanyId();

        String mobile = trimRequired(request.getMobile(), "mobile");
        String normalizedMobile = normalizeMobile(mobile);
        if (fleetDriverRepository.existsByCompanyIdAndMobileNormalizedAndIdNot(companyId, normalizedMobile, driver.getId())) {
            throw new BadRequestException("Driver mobile already exists: " + mobile);
        }

        driver.setName(trimRequired(request.getName(), "name"));
        driver.setMobile(mobile);
        driver.setMobileNormalized(normalizedMobile);
        driver.setLicenceNumber(trimToNull(request.getLicenceNumber()));
        driver.setLinkedUsername(trimToNull(request.getLinkedUsername()));
        driver.setUsualVehicleId(resolveUsualVehicleId(companyId, request.getUsualVehicleId()));
        if (hasText(request.getAvailability())) {
            driver.setAvailability(parseAvailability(request.getAvailability(), driver.getAvailability()));
        }
        driver.setNotes(trimToNull(request.getNotes()));
        if (hasText(request.getStatus())) {
            driver.setStatus(parseStatus(request.getStatus()));
        }
        applyAudit(driver, request.getUsername(), false);

        return toDto(fleetDriverRepository.save(driver));
    }

    @Override
    @Transactional
    public DriverDto updateStatus(DriverStatusUpdateRequest request) {
        FleetDriver driver = getByIdForCompany(request.getId());
        driver.setStatus(parseStatus(request.getStatus()));
        applyAudit(driver, request.getUsername(), false);
        return toDto(fleetDriverRepository.save(driver));
    }

    @Override
    @Transactional(readOnly = true)
    public DriverFilterResultDto filterList(DriverFilterRequest request) {
        Long companyId = requireCompanyId();
        Pageable pageable = PageRequest.of(
                request.getPage(),
                request.getSize(),
                Sort.by(resolveDirection(request.getSortDirection()), resolveSortColumn(request.getSortColumn()))
        );

        Page<FleetDriver> page = fleetDriverRepository.findAll(
                buildSpecification(companyId, request.getSearch()),
                pageable
        );

        Map<Long, String> vehicleRegistrations = loadVehicleRegistrations(companyId, page.getContent());

        List<DriverListItemDto> content = page.getContent().stream()
                .map(driver -> toListItemDto(driver, vehicleRegistrations))
                .toList();

        return DriverFilterResultDto.builder()
                .content(content)
                .size(content.size())
                .totalRecords(page.getTotalElements())
                .build();
    }

    private FleetDriver getByIdForCompany(Long id) {
        Long companyId = requireCompanyId();
        return fleetDriverRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found with ID: " + id));
    }

    private Long resolveUsualVehicleId(Long companyId, Long vehicleId) {
        if (vehicleId == null) {
            return null;
        }
        fleetVehicleRepository.findByIdAndCompanyId(vehicleId, companyId)
                .orElseThrow(() -> new BadRequestException("Usual vehicle not found with ID: " + vehicleId));
        return vehicleId;
    }

    private Map<Long, String> loadVehicleRegistrations(Long companyId, List<FleetDriver> drivers) {
        List<Long> ids = drivers.stream()
                .map(FleetDriver::getUsualVehicleId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());
        if (ids.isEmpty()) {
            return Map.of();
        }
        Map<Long, String> map = new HashMap<>();
        for (Long id : ids) {
            fleetVehicleRepository.findByIdAndCompanyId(id, companyId)
                    .ifPresent(v -> map.put(id, v.getRegistration()));
        }
        return map;
    }

    private String resolveVehicleRegistration(Long companyId, Long vehicleId) {
        if (vehicleId == null) {
            return null;
        }
        return fleetVehicleRepository.findByIdAndCompanyId(vehicleId, companyId)
                .map(FleetVehicle::getRegistration)
                .orElse(null);
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

    private Specification<FleetDriver> buildSpecification(Long companyId, DriverFilterSearch search) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("companyId"), companyId));

            if (search != null) {
                if (hasText(search.getName())) {
                    predicates.add(criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("name")),
                            like(search.getName())
                    ));
                }
                if (hasText(search.getMobile())) {
                    predicates.add(criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("mobile")),
                            like(search.getMobile())
                    ));
                }
                if (hasText(search.getAvailability())) {
                    predicates.add(criteriaBuilder.equal(
                            root.get("availability"),
                            parseAvailability(search.getAvailability(), null)
                    ));
                }
                if (hasText(search.getStatus())) {
                    predicates.add(criteriaBuilder.equal(root.get("status"), parseStatus(search.getStatus())));
                }
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private DriverDto toDto(FleetDriver driver) {
        Long companyId = driver.getCompanyId();
        return DriverDto.builder()
                .id(driver.getId())
                .companyId(companyId)
                .name(driver.getName())
                .mobile(driver.getMobile())
                .licenceNumber(driver.getLicenceNumber())
                .linkedUsername(driver.getLinkedUsername())
                .usualVehicleId(driver.getUsualVehicleId())
                .usualVehicleRegistration(resolveVehicleRegistration(companyId, driver.getUsualVehicleId()))
                .availability(driver.getAvailability().name())
                .availabilityDescription(toAvailabilityDescription(driver.getAvailability()))
                .notes(driver.getNotes())
                .status(driver.getStatus().name())
                .statusDescription(toStatusDescription(driver.getStatus()))
                .createdDate(driver.getCreatedDate())
                .lastModifiedDate(driver.getLastModifiedDate())
                .createdBy(driver.getCreatedBy())
                .lastModifiedBy(driver.getLastModifiedBy())
                .build();
    }

    private DriverListItemDto toListItemDto(FleetDriver driver, Map<Long, String> vehicleRegistrations) {
        String registration = driver.getUsualVehicleId() != null
                ? vehicleRegistrations.get(driver.getUsualVehicleId())
                : null;
        return DriverListItemDto.builder()
                .id(driver.getId())
                .name(driver.getName())
                .mobile(driver.getMobile())
                .availability(driver.getAvailability().name())
                .availabilityDescription(toAvailabilityDescription(driver.getAvailability()))
                .usualVehicleRegistration(registration)
                .status(driver.getStatus().name())
                .statusDescription(toStatusDescription(driver.getStatus()))
                .createdDate(driver.getCreatedDate())
                .lastModifiedDate(driver.getLastModifiedDate())
                .createdBy(driver.getCreatedBy())
                .lastModifiedBy(driver.getLastModifiedBy())
                .build();
    }

    private void applyAudit(FleetDriver driver, String username, boolean create) {
        LocalDateTime now = LocalDateTime.now();
        String actor = hasText(username) ? username.trim() : "system";
        if (create) {
            driver.setCreatedDate(now);
            driver.setCreatedBy(actor);
        }
        driver.setLastModifiedDate(now);
        driver.setLastModifiedBy(actor);
    }

    private MasterStatus parseStatus(String value) {
        try {
            return MasterStatus.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (Exception ex) {
            throw new BadRequestException("Invalid status: " + value);
        }
    }

    private DriverAvailability parseAvailability(String value, DriverAvailability fallback) {
        if (!hasText(value)) {
            if (fallback == null) {
                throw new BadRequestException("Invalid availability");
            }
            return fallback;
        }
        try {
            return DriverAvailability.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (Exception ex) {
            throw new BadRequestException("Invalid availability: " + value);
        }
    }

    private String resolveSortColumn(String sortColumn) {
        if (!hasText(sortColumn)) {
            return "lastModifiedDate";
        }
        return switch (sortColumn) {
            case "name", "mobile", "availability", "status",
                 "createdDate", "lastModifiedDate", "createdBy" -> sortColumn;
            default -> "lastModifiedDate";
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

    private String toStatusDescription(MasterStatus status) {
        return status == MasterStatus.ACTIVE ? "Active" : "Inactive";
    }

    private String toAvailabilityDescription(DriverAvailability availability) {
        return availability == DriverAvailability.AVAILABLE ? "Available" : "Unavailable";
    }

    private String normalizeMobile(String mobile) {
        return mobile.replaceAll("[\\s-]+", "");
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

    private String like(String value) {
        return "%" + value.trim().toLowerCase(Locale.ROOT) + "%";
    }
}