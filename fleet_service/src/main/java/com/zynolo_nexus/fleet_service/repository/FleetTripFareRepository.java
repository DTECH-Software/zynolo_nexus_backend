package com.zynolo_nexus.fleet_service.repository;

import com.zynolo_nexus.fleet_service.enums.FareStatus;
import com.zynolo_nexus.fleet_service.model.FleetTripFare;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

public interface FleetTripFareRepository
        extends JpaRepository<FleetTripFare, Long>, JpaSpecificationExecutor<FleetTripFare> {

    Optional<FleetTripFare> findByIdAndCompanyId(Long id, Long companyId);

    boolean existsByTripId(Long tripId);

    Optional<FleetTripFare> findByTripIdAndCompanyId(Long tripId, Long companyId);

    boolean existsByCompanyIdAndFareNumber(Long companyId, String fareNumber);

    @Query("""
            select coalesce(sum(f.amount), 0)
            from FleetTripFare f
            where f.companyId = :companyId
              and f.status = :status
              and f.recordedAt >= :from
              and f.recordedAt <= :to
            """)
    BigDecimal sumAmountByCompanyAndStatusAndRecordedAtBetween(
            @Param("companyId") Long companyId,
            @Param("status") FareStatus status,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );
}

