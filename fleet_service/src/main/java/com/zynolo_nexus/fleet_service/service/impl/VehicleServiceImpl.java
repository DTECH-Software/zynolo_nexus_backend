package com.zynolo_nexus.fleet_service.service.impl;

import com.zynolo_nexus.fleet_service.context.CompanyContext;
import com.zynolo_nexus.fleet_service.dto.request.VehicleCreateRequest;
import com.zynolo_nexus.fleet_service.dto.request.VehicleFilterRequest;
import com.zynolo_nexus.fleet_service.dto.request.VehicleFilterSearch;
import com.zynolo_nexus.fleet_service.dto.request.VehicleReferenceDataRequest;
import com.zynolo_nexus.fleet_service.dto.request.VehicleStatusUpdateRequest;
import com.zynolo_nexus.fleet_service.dto.request.VehicleUpdateRequest;
import com.zynolo_nexus.fleet_service.dto.request.VehicleViewRequest;
import com.zynolo_nexus.fleet_service.dto.response.ReferenceOptionDto;
import com.zynolo_nexus.fleet_service.dto.response.VehicleDto;
import com.zynolo_nexus.fleet_service.dto.response.VehicleFilterResultDto;
import com.zynolo_nexus.fleet_service.dto.response.VehicleListItemDto;
import com.zynolo_nexus.fleet_service.dto.response.VehiclePrivilegesDto;
import com.zynolo_nexus.fleet_service.dto.response.VehicleReferenceDataDto;
import com.zynolo_nexus.fleet_service.enums.MasterStatus;
import com.zynolo_nexus.fleet_service.exception.BadRequestException;
import com.zynolo_nexus.fleet_service.exception.ResourceNotFoundException;
import com.zynolo_nexus.fleet_service.model.FleetVehicle;
import com.zynolo_nexus.fleet_service.repository.FleetVehicleRepository;
import com.zynolo_nexus.fleet_service.service.VehicleService;
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
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class VehicleServiceImpl implements VehicleService {

    private static final String PAGE_CODE = "FLVM";

    private final FleetVehicleRepository fleetVehicleRepository;
    private final PagePrivilegeResolver pagePrivilegeResolver;

    @Value("${app.default.company-id:1}")
    private Long defaultCompanyId;

    @Override
    public VehicleReferenceDataDto getReferenceData(VehicleReferenceDataRequest request) {
        var privileges = pagePrivilegeResolver.resolve(
                request != null ? request.getRoleCode() : null,
                PAGE_CODE
        );
        return VehicleReferenceDataDto.builder()
                .defaultStatus(List.of(
                        option(MasterStatus.ACTIVE.name(), "Active"),
                        option(MasterStatus.INACTIVE.name(), "Inactive")
                ))
                .vehicleTypes(List.of(
                        option("VAN", "Van"),
                        option("CAR", "Car"),
                        option("BUS", "Bus"),
                        option("SUV", "SUV"),
                        option("OTHER", "Other")
                ))
                .privileges(VehiclePrivilegesDto.builder()
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
    public VehicleDto create(VehicleCreateRequest request) {
        Long companyId = requireCompanyId();
        String registration = trimRequired(request.getRegistration(), "registration");
        String normalized = normalizeRegistration(registration);

        if (fleetVehicleRepository.existsByCompanyIdAndRegistrationNormalized(companyId, normalized)) {
            throw new BadRequestException("Vehicle registration already exists: " + registration);
        }

        Integer capacity = request.getPassengerCapacity();
        if (capacity == null || capacity < 1) {
            throw new BadRequestException("passengerCapacity must be a positive whole number");
        }

        FleetVehicle vehicle = new FleetVehicle();
        vehicle.setCompanyId(companyId);
        vehicle.setRegistration(registration);
        vehicle.setRegistrationNormalized(normalized);
        vehicle.setVehicleType(trimRequired(request.getVehicleType(), "vehicleType"));
        vehicle.setPassengerCapacity(capacity);
        vehicle.setNotes(trimToNull(request.getNotes()));
        vehicle.setStatus(MasterStatus.ACTIVE);
        applyAudit(vehicle, request.getUsername(), true);

        return toDto(fleetVehicleRepository.save(vehicle));
    }

    @Override
    @Transactional(readOnly = true)
    public VehicleDto view(VehicleViewRequest request) {
        return toDto(getByIdForCompany(request.getId()));
    }

    @Override
    @Transactional
    public VehicleDto update(VehicleUpdateRequest request) {
        FleetVehicle vehicle = getByIdForCompany(request.getId());
        Long companyId = vehicle.getCompanyId();

        String registration = trimRequired(request.getRegistration(), "registration");
        String normalized = normalizeRegistration(registration);
        if (fleetVehicleRepository.existsByCompanyIdAndRegistrationNormalizedAndIdNot(companyId, normalized, vehicle.getId())) {
            throw new BadRequestException("Vehicle registration already exists: " + registration);
        }

        Integer capacity = request.getPassengerCapacity();
        if (capacity == null || capacity < 1) {
            throw new BadRequestException("passengerCapacity must be a positive whole number");
        }

        vehicle.setRegistration(registration);
        vehicle.setRegistrationNormalized(normalized);
        vehicle.setVehicleType(trimRequired(request.getVehicleType(), "vehicleType"));
        vehicle.setPassengerCapacity(capacity);
        vehicle.setNotes(trimToNull(request.getNotes()));
        if (hasText(request.getStatus())) {
            vehicle.setStatus(parseStatus(request.getStatus()));
        }
        applyAudit(vehicle, request.getUsername(), false);

        return toDto(fleetVehicleRepository.save(vehicle));
    }

    @Override
    @Transactional
    public VehicleDto updateStatus(VehicleStatusUpdateRequest request) {
        FleetVehicle vehicle = getByIdForCompany(request.getId());
        vehicle.setStatus(parseStatus(request.getStatus()));
        applyAudit(vehicle, request.getUsername(), false);
        return toDto(fleetVehicleRepository.save(vehicle));
    }

    @Override
    @Transactional(readOnly = true)
    public VehicleFilterResultDto filterList(VehicleFilterRequest request) {
        Long companyId = requireCompanyId();
        Pageable pageable = PageRequest.of(
                request.getPage(),
                request.getSize(),
                Sort.by(resolveDirection(request.getSortDirection()), resolveSortColumn(request.getSortColumn()))
        );

        Page<FleetVehicle> page = fleetVehicleRepository.findAll(
                buildSpecification(companyId, request.getSearch()),
                pageable
        );

        List<VehicleListItemDto> content = page.getContent().stream()
                .map(this::toListItemDto)
                .toList();

        return VehicleFilterResultDto.builder()
                .content(content)
                .size(content.size())
                .totalRecords(page.getTotalElements())
                .build();
    }

    private FleetVehicle getByIdForCompany(Long id) {
        Long companyId = requireCompanyId();
        return fleetVehicleRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with ID: " + id));
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

    private Specification<FleetVehicle> buildSpecification(Long companyId, VehicleFilterSearch search) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("companyId"), companyId));

            if (search != null) {
                if (hasText(search.getRegistration())) {
                    predicates.add(criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("registration")),
                            like(search.getRegistration())
                    ));
                }
                if (hasText(search.getVehicleType())) {
                    predicates.add(criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("vehicleType")),
                            like(search.getVehicleType())
                    ));
                }
                if (hasText(search.getStatus())) {
                    predicates.add(criteriaBuilder.equal(root.get("status"), parseStatus(search.getStatus())));
                }
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private VehicleDto toDto(FleetVehicle vehicle) {
        return VehicleDto.builder()
                .id(vehicle.getId())
                .companyId(vehicle.getCompanyId())
                .registration(vehicle.getRegistration())
                .vehicleType(vehicle.getVehicleType())
                .passengerCapacity(vehicle.getPassengerCapacity())
                .notes(vehicle.getNotes())
                .status(vehicle.getStatus().name())
                .statusDescription(toStatusDescription(vehicle.getStatus()))
                .createdDate(vehicle.getCreatedDate())
                .lastModifiedDate(vehicle.getLastModifiedDate())
                .createdBy(vehicle.getCreatedBy())
                .lastModifiedBy(vehicle.getLastModifiedBy())
                .build();
    }

    private VehicleListItemDto toListItemDto(FleetVehicle vehicle) {
        return VehicleListItemDto.builder()
                .id(vehicle.getId())
                .registration(vehicle.getRegistration())
                .vehicleType(vehicle.getVehicleType())
                .passengerCapacity(vehicle.getPassengerCapacity())
                .status(vehicle.getStatus().name())
                .statusDescription(toStatusDescription(vehicle.getStatus()))
                .createdDate(vehicle.getCreatedDate())
                .lastModifiedDate(vehicle.getLastModifiedDate())
                .createdBy(vehicle.getCreatedBy())
                .lastModifiedBy(vehicle.getLastModifiedBy())
                .build();
    }

    private void applyAudit(FleetVehicle vehicle, String username, boolean create) {
        LocalDateTime now = LocalDateTime.now();
        String actor = hasText(username) ? username.trim() : "system";
        if (create) {
            vehicle.setCreatedDate(now);
            vehicle.setCreatedBy(actor);
        }
        vehicle.setLastModifiedDate(now);
        vehicle.setLastModifiedBy(actor);
    }

    private MasterStatus parseStatus(String value) {
        try {
            return MasterStatus.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (Exception ex) {
            throw new BadRequestException("Invalid status: " + value);
        }
    }

    private String resolveSortColumn(String sortColumn) {
        if (!hasText(sortColumn)) {
            return "lastModifiedDate";
        }
        return switch (sortColumn) {
            case "registration", "vehicleType", "passengerCapacity", "status",
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

    private String normalizeRegistration(String registration) {
        return registration.replaceAll("[\\s-]+", "").toUpperCase(Locale.ROOT);
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
