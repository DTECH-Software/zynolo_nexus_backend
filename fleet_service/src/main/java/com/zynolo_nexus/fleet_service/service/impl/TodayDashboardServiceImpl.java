package com.zynolo_nexus.fleet_service.service.impl;

import com.zynolo_nexus.fleet_service.context.CompanyContext;
import com.zynolo_nexus.fleet_service.dto.request.TodayDashboardReferenceDataRequest;
import com.zynolo_nexus.fleet_service.dto.request.TodayDashboardViewRequest;
import com.zynolo_nexus.fleet_service.dto.response.TodayDashboardDto;
import com.zynolo_nexus.fleet_service.dto.response.TodayDashboardPrivilegesDto;
import com.zynolo_nexus.fleet_service.dto.response.TodayDashboardReferenceDataDto;
import com.zynolo_nexus.fleet_service.enums.FareStatus;
import com.zynolo_nexus.fleet_service.enums.TransportRequestStatus;
import com.zynolo_nexus.fleet_service.enums.TripStatus;
import com.zynolo_nexus.fleet_service.exception.BadRequestException;
import com.zynolo_nexus.fleet_service.model.FleetTransportRequest;
import com.zynolo_nexus.fleet_service.model.FleetTrip;
import com.zynolo_nexus.fleet_service.model.FleetTripFare;
import com.zynolo_nexus.fleet_service.repository.FleetTransportRequestRepository;
import com.zynolo_nexus.fleet_service.repository.FleetTripFareRepository;
import com.zynolo_nexus.fleet_service.repository.FleetTripRepository;
import com.zynolo_nexus.fleet_service.service.TodayDashboardService;
import com.zynolo_nexus.fleet_service.service.support.PagePrivilegeResolver;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TodayDashboardServiceImpl implements TodayDashboardService {

    private static final String PAGE_CODE = "FLDB";

    private final FleetTransportRequestRepository transportRequestRepository;
    private final FleetTripRepository fleetTripRepository;
    private final FleetTripFareRepository tripFareRepository;
    private final PagePrivilegeResolver pagePrivilegeResolver;

    @Value("${app.default.company-id:1}")
    private Long defaultCompanyId;

    @Override
    @Transactional(readOnly = true)
    public TodayDashboardReferenceDataDto getReferenceData(TodayDashboardReferenceDataRequest request) {
        requireUsername(request != null ? request.getUsername() : null);
        var privileges = pagePrivilegeResolver.resolve(
                request != null ? request.getUsername() : null,
                PAGE_CODE
        );
        return TodayDashboardReferenceDataDto.builder()
                .defaultReportDate(LocalDate.now())
                .privileges(TodayDashboardPrivilegesDto.builder()
                        .view(privileges.isView())
                        .build())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public TodayDashboardDto view(TodayDashboardViewRequest request) {
        requireUsername(request.getUsername());
        Long companyId = requireCompanyId();
        LocalDate reportDate = request.getReportDate() != null ? request.getReportDate() : LocalDate.now();
        LocalDateTime dayStart = reportDate.atStartOfDay();
        LocalDateTime dayEnd = reportDate.atTime(LocalTime.MAX);

        long requestsSubmitted = transportRequestRepository.count(
                requestSubmittedOnDay(companyId, dayStart, dayEnd)
        );
        long requestsPendingQueue = transportRequestRepository.count(
                requestByStatus(companyId, TransportRequestStatus.SUBMITTED)
        );

        long tripsAssigned = fleetTripRepository.count(
                tripOnDayByStatus(companyId, dayStart, dayEnd, TripStatus.ASSIGNED)
        );
        long tripsStarted = fleetTripRepository.count(
                tripOnDayByStatus(companyId, dayStart, dayEnd, TripStatus.STARTED)
        );
        long tripsCompleted = fleetTripRepository.count(
                tripOnDayByStatus(companyId, dayStart, dayEnd, TripStatus.COMPLETED)
        );
        long tripsCancelled = fleetTripRepository.count(
                tripOnDayByStatus(companyId, dayStart, dayEnd, TripStatus.CANCELLED)
        );
        long tripsLive = fleetTripRepository.count(
                tripByStatus(companyId, TripStatus.STARTED)
        );

        long faresRecorded = tripFareRepository.count(
                fareRecordedOnDay(companyId, dayStart, dayEnd, FareStatus.RECORDED)
        );
        BigDecimal faresAmountTotal = tripFareRepository.sumAmountByCompanyAndStatusAndRecordedAtBetween(
                companyId,
                FareStatus.RECORDED,
                dayStart,
                dayEnd
        );
        if (faresAmountTotal == null) {
            faresAmountTotal = BigDecimal.ZERO;
        }

        long completedWithoutFare = fleetTripRepository.count(
                completedOnDayWithoutFare(companyId, dayStart, dayEnd)
        );

        return TodayDashboardDto.builder()
                .reportDate(reportDate)
                .requestsSubmitted(requestsSubmitted)
                .requestsPendingQueue(requestsPendingQueue)
                .tripsAssigned(tripsAssigned)
                .tripsStarted(tripsStarted)
                .tripsCompleted(tripsCompleted)
                .tripsCancelled(tripsCancelled)
                .tripsTotal(tripsAssigned + tripsStarted + tripsCompleted + tripsCancelled)
                .tripsLive(tripsLive)
                .faresRecorded(faresRecorded)
                .faresAmountTotal(faresAmountTotal)
                .completedWithoutFare(completedWithoutFare)
                .build();
    }

    private Specification<FleetTransportRequest> requestSubmittedOnDay(
            Long companyId,
            LocalDateTime dayStart,
            LocalDateTime dayEnd
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("companyId"), companyId));
            predicates.add(cb.isNotNull(root.get("submittedAt")));
            predicates.add(cb.between(root.get("submittedAt"), dayStart, dayEnd));
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private Specification<FleetTransportRequest> requestByStatus(Long companyId, TransportRequestStatus status) {
        return (root, query, cb) -> cb.and(
                cb.equal(root.get("companyId"), companyId),
                cb.equal(root.get("status"), status)
        );
    }

    private Specification<FleetTrip> tripOnDayByStatus(
            Long companyId,
            LocalDateTime dayStart,
            LocalDateTime dayEnd,
            TripStatus status
    ) {
        return (root, query, cb) -> cb.and(
                cb.equal(root.get("companyId"), companyId),
                cb.equal(root.get("status"), status),
                cb.between(root.get("scheduledPickupAt"), dayStart, dayEnd)
        );
    }

    private Specification<FleetTrip> tripByStatus(Long companyId, TripStatus status) {
        return (root, query, cb) -> cb.and(
                cb.equal(root.get("companyId"), companyId),
                cb.equal(root.get("status"), status)
        );
    }

    private Specification<FleetTripFare> fareRecordedOnDay(
            Long companyId,
            LocalDateTime dayStart,
            LocalDateTime dayEnd,
            FareStatus status
    ) {
        return (root, query, cb) -> cb.and(
                cb.equal(root.get("companyId"), companyId),
                cb.equal(root.get("status"), status),
                cb.between(root.get("recordedAt"), dayStart, dayEnd)
        );
    }

    private Specification<FleetTrip> completedOnDayWithoutFare(
            Long companyId,
            LocalDateTime dayStart,
            LocalDateTime dayEnd
    ) {
        return (root, query, cb) -> {
            Subquery<Long> fareTripIds = query.subquery(Long.class);
            Root<FleetTripFare> fareRoot = fareTripIds.from(FleetTripFare.class);
            fareTripIds.select(fareRoot.get("tripId"));
            fareTripIds.where(cb.equal(fareRoot.get("companyId"), companyId));

            return cb.and(
                    cb.equal(root.get("companyId"), companyId),
                    cb.equal(root.get("status"), TripStatus.COMPLETED),
                    cb.between(root.get("scheduledPickupAt"), dayStart, dayEnd),
                    cb.not(root.get("id").in(fareTripIds))
            );
        };
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
        if (username == null || username.trim().isEmpty()) {
            throw new BadRequestException("username is required");
        }
        return username.trim();
    }
}
