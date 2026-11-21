package com.zynolo_nexus.fleet_service.repository;

import com.zynolo_nexus.fleet_service.model.FleetTripFare;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface FleetTripFareRepository
        extends JpaRepository<FleetTripFare, Long>, JpaSpecificationExecutor<FleetTripFare> {

    Optional<FleetTripFare> findByIdAndCompanyId(Long id, Long companyId);

    boolean existsByTripId(Long tripId);

    Optional<FleetTripFare> findByTripIdAndCompanyId(Long tripId, Long companyId);

    boolean existsByCompanyIdAndFareNumber(Long companyId, String fareNumber);
}
