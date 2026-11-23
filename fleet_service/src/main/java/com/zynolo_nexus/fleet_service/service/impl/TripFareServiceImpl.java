package com.zynolo_nexus.fleet_service.service.impl;

import com.zynolo_nexus.fleet_service.context.CompanyContext;
import com.zynolo_nexus.fleet_service.dto.request.TripFareCreateRequest;
import com.zynolo_nexus.fleet_service.dto.request.TripFareFilterRequest;
import com.zynolo_nexus.fleet_service.dto.request.TripFareFilterSearch;
import com.zynolo_nexus.fleet_service.dto.request.TripFareReferenceDataRequest;
import com.zynolo_nexus.fleet_service.dto.request.TripFareUpdateRequest;
import com.zynolo_nexus.fleet_service.dto.request.TripFareViewRequest;
import com.zynolo_nexus.fleet_service.dto.response.ReferenceOptionDto;
import com.zynolo_nexus.fleet_service.dto.response.TripFareDto;
import com.zynolo_nexus.fleet_service.dto.response.TripFareFilterResultDto;
import com.zynolo_nexus.fleet_service.dto.response.TripFareListItemDto;
import com.zynolo_nexus.fleet_service.dto.response.TripFarePrivilegesDto;
import com.zynolo_nexus.fleet_service.dto.response.TripFareReferenceDataDto;
import com.zynolo_nexus.fleet_service.enums.FarePaymentMethod;
import com.zynolo_nexus.fleet_service.enums.FareStatus;
import com.zynolo_nexus.fleet_service.enums.TripStatus;
import com.zynolo_nexus.fleet_service.exception.BadRequestException;
import com.zynolo_nexus.fleet_service.exception.ResourceNotFoundException;
import com.zynolo_nexus.fleet_service.model.FleetTransportRequest;
import com.zynolo_nexus.fleet_service.model.FleetTrip;
import com.zynolo_nexus.fleet_service.model.FleetTripFare;
import com.zynolo_nexus.fleet_service.repository.FleetTransportRequestRepository;
import com.zynolo_nexus.fleet_service.repository.FleetTripFareRepository;
import com.zynolo_nexus.fleet_service.repository.FleetTripRepository;
import com.zynolo_nexus.fleet_service.service.TripFareService;
import com.zynolo_nexus.fleet_service.service.support.PagePrivilegeResolver;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class TripFareServiceImpl implements TripFareService {

    private static final String PAGE_CODE = "FLTF";
    private static final String DEFAULT_CURRENCY = "LKR";
    private static final DateTimeFormatter FARE_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final FleetTripFareRepository tripFareRepository;
    private final FleetTripRepository fleetTripRepository;
    private final FleetTransportRequestRepository transportRequestRepository;
    private final PagePrivilegeResolver pagePrivilegeResolver;

    @Value("${app.default.company-id:1}")
    private Long defaultCompanyId;

    @Override
    @Transactional(readOnly = true)
    public TripFareReferenceDataDto getReferenceData(TripFareReferenceDataRequest request) {
        requireUsername(request != null ? request.getUsername() : null);
        var privileges = pagePrivilegeResolver.resolve(
                request != null ? request.getUsername() : null,
                PAGE_CODE
        );
        return TripFareReferenceDataDto.builder()
                .defaultPaymentMethod(List.of(
                        option(FarePaymentMethod.CASH.name(), "Cash"),
                        option(FarePaymentMethod.CARD.name(), "Card"),
                        option(FarePaymentMethod.COMPANY.name(), "Company"),
                        option(FarePaymentMethod.OTHER.name(), "Other")
                ))
                .defaultCurrency(List.of(
                        option("LKR", "Sri Lankan Rupee"),
                        option("USD", "US Dollar")
                ))
                .defaultStatus(List.of(
                        option(FareStatus.RECORDED.name(), "Recorded"),
                        option(FareStatus.CANCELLED.name(), "Cancelled")
                ))
                .defaultCurrencyCode(DEFAULT_CURRENCY)
                .privileges(TripFarePrivilegesDto.builder()
                        .add(privileges.isAdd())
                        .view(privileges.isView())
                        .search(privileges.isSearch())
                        .update(privileges.isUpdate())
                        .build())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public TripFareFilterResultDto filterList(TripFareFilterRequest request) {
        Long companyId = requireCompanyId();
        requireUsername(request.getUsername());
        Pageable pageable = PageRequest.of(
                request.getPage(),
                request.getSize(),
                Sort.by(resolveDirection(request.getSortDirection()), resolveSortColumn(request.getSortColumn()))
        );

        Page<FleetTripFare> page = tripFareRepository.findAll(
                buildSpecification(companyId, request.getSearch()),
                pageable
        );

        Map<Long, FleetTrip> trips = loadTrips(companyId, page.getContent());
        Map<Long, FleetTransportRequest> requests = loadRequests(companyId, trips);

        List<TripFareListItemDto> content = page.getContent().stream()
                .map(fare -> {
                    FleetTrip trip = trips.get(fare.getTripId());
                    FleetTransportRequest transportRequest = trip != null
                            ? requests.get(trip.getTransportRequestId())
                            : null;
                    return toListItemDto(fare, trip, transportRequest);
                })
                .toList();

        return TripFareFilterResultDto.builder()
                .content(content)
                .size(content.size())
                .totalRecords(page.getTotalElements())
                .build();
    }

    @Override
    @Transactional
    public TripFareDto create(TripFareCreateRequest request) {
        Long companyId = requireCompanyId();
        String actor = requireUsername(request.getUsername());
        FleetTrip trip = getTripForCompany(request.getTripId());
        if (trip.getStatus() != TripStatus.COMPLETED) {
            throw new BadRequestException("Fare can only be recorded on COMPLETED trips");
        }
        if (tripFareRepository.existsByTripId(trip.getId())) {
            throw new BadRequestException("A fare is already recorded for this trip");
        }

        LocalDateTime now = LocalDateTime.now();
        FleetTripFare fare = new FleetTripFare();
        fare.setCompanyId(companyId);
        fare.setFareNumber(generateFareNumber(companyId));
        fare.setTripId(trip.getId());
        fare.setAmount(normalizeAmount(request.getAmount()));
        fare.setCurrency(resolveCurrency(request.getCurrency()));
        fare.setPaymentMethod(parsePaymentMethod(request.getPaymentMethod()));
        fare.setNotes(trimToNull(request.getNotes()));
        fare.setRecordedAt(now);
        fare.setRecordedBy(actor);
        fare.setStatus(FareStatus.RECORDED);
        applyAudit(fare, actor, true);

        return toDto(tripFareRepository.save(fare), trip);
    }

    @Override
    @Transactional(readOnly = true)
    public TripFareDto view(TripFareViewRequest request) {
        requireUsername(request.getUsername());
        FleetTripFare fare = getFareForCompany(request.getId());
        FleetTrip trip = getTripForCompany(fare.getTripId());
        return toDto(fare, trip);
    }

    @Override
    @Transactional
    public TripFareDto update(TripFareUpdateRequest request) {
        String actor = requireUsername(request.getUsername());
        FleetTripFare fare = getFareForCompany(request.getId());
        if (fare.getStatus() != FareStatus.RECORDED) {
            throw new BadRequestException("Only RECORDED fares can be updated");
        }

        fare.setAmount(normalizeAmount(request.getAmount()));
        fare.setCurrency(resolveCurrency(request.getCurrency()));
        fare.setPaymentMethod(parsePaymentMethod(request.getPaymentMethod()));
        fare.setNotes(trimToNull(request.getNotes()));

        if (hasText(request.getStatus())) {
            FareStatus next = parseFareStatus(request.getStatus());
            if (next != FareStatus.RECORDED && next != FareStatus.CANCELLED) {
                throw new BadRequestException("Invalid status transition");
            }
            fare.setStatus(next);
        }

        applyAudit(fare, actor, false);
        FleetTrip trip = getTripForCompany(fare.getTripId());
        return toDto(tripFareRepository.save(fare), trip);
    }

    private Specification<FleetTripFare> buildSpecification(Long companyId, TripFareFilterSearch search) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("companyId"), companyId));

            if (search != null) {
                if (hasText(search.getFareNumber())) {
                    predicates.add(criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("fareNumber")),
                            like(search.getFareNumber())
                    ));
                }
                if (hasText(search.getPaymentMethod())) {
                    predicates.add(criteriaBuilder.equal(
                            root.get("paymentMethod"),
                            parsePaymentMethod(search.getPaymentMethod())
                    ));
                }
                if (hasText(search.getStatus())) {
                    predicates.add(criteriaBuilder.equal(root.get("status"), parseFareStatus(search.getStatus())));
                }
                if (hasText(search.getTripNumber())) {
                    Subquery<Long> tripIds = query.subquery(Long.class);
                    Root<FleetTrip> tripRoot = tripIds.from(FleetTrip.class);
                    tripIds.select(tripRoot.get("id"));
                    tripIds.where(
                            criteriaBuilder.equal(tripRoot.get("companyId"), companyId),
                            criteriaBuilder.like(
                                    criteriaBuilder.lower(tripRoot.get("tripNumber")),
                                    like(search.getTripNumber())
                            )
                    );
                    predicates.add(root.get("tripId").in(tripIds));
                }
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private FleetTripFare getFareForCompany(Long id) {
        Long companyId = requireCompanyId();
        return tripFareRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip fare not found with ID: " + id));
    }

    private FleetTrip getTripForCompany(Long id) {
        Long companyId = requireCompanyId();
        return fleetTripRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found with ID: " + id));
    }

    private Map<Long, FleetTrip> loadTrips(Long companyId, List<FleetTripFare> fares) {
        Map<Long, FleetTrip> map = new HashMap<>();
        fares.stream()
                .map(FleetTripFare::getTripId)
                .filter(Objects::nonNull)
                .distinct()
                .forEach(id -> fleetTripRepository.findByIdAndCompanyId(id, companyId)
                        .ifPresent(t -> map.put(id, t)));
        return map;
    }

    private Map<Long, FleetTransportRequest> loadRequests(Long companyId, Map<Long, FleetTrip> trips) {
        Map<Long, FleetTransportRequest> map = new HashMap<>();
        trips.values().stream()
                .map(FleetTrip::getTransportRequestId)
                .filter(Objects::nonNull)
                .distinct()
                .forEach(id -> transportRequestRepository.findByIdAndCompanyId(id, companyId)
                        .ifPresent(r -> map.put(id, r)));
        return map;
    }

    private TripFareDto toDto(FleetTripFare fare, FleetTrip trip) {
        FleetTransportRequest request = trip != null
                ? transportRequestRepository.findByIdAndCompanyId(trip.getTransportRequestId(), fare.getCompanyId())
                .orElse(null)
                : null;
        return TripFareDto.builder()
                .id(fare.getId())
                .companyId(fare.getCompanyId())
                .fareNumber(fare.getFareNumber())
                .tripId(fare.getTripId())
                .tripNumber(trip != null ? trip.getTripNumber() : null)
                .transportRequestId(trip != null ? trip.getTransportRequestId() : null)
                .requestNumber(request != null ? request.getRequestNumber() : null)
                .requesterUsername(request != null ? request.getRequesterUsername() : null)
                .pickupLocation(request != null ? request.getPickupLocation() : null)
                .dropLocation(request != null ? request.getDropLocation() : null)
                .amount(fare.getAmount())
                .currency(fare.getCurrency())
                .paymentMethod(fare.getPaymentMethod().name())
                .paymentMethodDescription(toPaymentMethodDescription(fare.getPaymentMethod()))
                .notes(fare.getNotes())
                .recordedAt(fare.getRecordedAt())
                .recordedBy(fare.getRecordedBy())
                .status(fare.getStatus().name())
                .statusDescription(toStatusDescription(fare.getStatus()))
                .createdDate(fare.getCreatedDate())
                .lastModifiedDate(fare.getLastModifiedDate())
                .createdBy(fare.getCreatedBy())
                .lastModifiedBy(fare.getLastModifiedBy())
                .build();
    }

    private TripFareListItemDto toListItemDto(
            FleetTripFare fare,
            FleetTrip trip,
            FleetTransportRequest request
    ) {
        return TripFareListItemDto.builder()
                .id(fare.getId())
                .fareNumber(fare.getFareNumber())
                .tripId(fare.getTripId())
                .tripNumber(trip != null ? trip.getTripNumber() : null)
                .requesterUsername(request != null ? request.getRequesterUsername() : null)
                .amount(fare.getAmount())
                .currency(fare.getCurrency())
                .paymentMethod(fare.getPaymentMethod().name())
                .paymentMethodDescription(toPaymentMethodDescription(fare.getPaymentMethod()))
                .recordedAt(fare.getRecordedAt())
                .status(fare.getStatus().name())
                .statusDescription(toStatusDescription(fare.getStatus()))
                .createdDate(fare.getCreatedDate())
                .lastModifiedDate(fare.getLastModifiedDate())
                .createdBy(fare.getCreatedBy())
                .lastModifiedBy(fare.getLastModifiedBy())
                .build();
    }

    private String generateFareNumber(Long companyId) {
        String datePart = LocalDate.now().format(FARE_DATE);
        for (int i = 0; i < 20; i++) {
            int suffix = ThreadLocalRandom.current().nextInt(1000, 10000);
            String candidate = "FTF-" + datePart + "-" + suffix;
            if (!tripFareRepository.existsByCompanyIdAndFareNumber(companyId, candidate)) {
                return candidate;
            }
        }
        throw new BadRequestException("Unable to generate fare number, please retry");
    }

    private BigDecimal normalizeAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("amount must be greater than zero");
        }
        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    private String resolveCurrency(String currency) {
        if (!hasText(currency)) {
            return DEFAULT_CURRENCY;
        }
        String normalized = currency.trim().toUpperCase(Locale.ROOT);
        if (!"LKR".equals(normalized) && !"USD".equals(normalized)) {
            throw new BadRequestException("Unsupported currency: " + currency + ". Use LKR or USD");
        }
        return normalized;
    }

    private FarePaymentMethod parsePaymentMethod(String value) {
        try {
            return FarePaymentMethod.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (Exception ex) {
            throw new BadRequestException("Invalid paymentMethod: " + value);
        }
    }

    private FareStatus parseFareStatus(String value) {
        try {
            return FareStatus.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (Exception ex) {
            throw new BadRequestException("Invalid status: " + value);
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

    private void applyAudit(FleetTripFare fare, String username, boolean create) {
        LocalDateTime now = LocalDateTime.now();
        String actor = hasText(username) ? username.trim() : "system";
        if (create) {
            fare.setCreatedDate(now);
            fare.setCreatedBy(actor);
        }
        fare.setLastModifiedDate(now);
        fare.setLastModifiedBy(actor);
    }

    private String resolveSortColumn(String sortColumn) {
        if (!hasText(sortColumn)) {
            return "recordedAt";
        }
        return switch (sortColumn) {
            case "fareNumber", "amount", "currency", "paymentMethod", "recordedAt", "status",
                 "createdDate", "lastModifiedDate", "createdBy" -> sortColumn;
            default -> "recordedAt";
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

    private String toPaymentMethodDescription(FarePaymentMethod method) {
        return switch (method) {
            case CASH -> "Cash";
            case CARD -> "Card";
            case COMPANY -> "Company";
            case OTHER -> "Other";
        };
    }

    private String toStatusDescription(FareStatus status) {
        return switch (status) {
            case RECORDED -> "Recorded";
            case CANCELLED -> "Cancelled";
        };
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String trimToNull(String value) {
        if (!hasText(value)) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.length() > 500 ? trimmed.substring(0, 500) : trimmed;
    }

    private String like(String value) {
        return "%" + value.trim().toLowerCase(Locale.ROOT) + "%";
    }
}
