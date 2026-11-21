package com.zynolo_nexus.fleet_service.repository;

import com.zynolo_nexus.fleet_service.model.FleetTrip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface FleetTripRepository extends JpaRepository<FleetTrip, Long>, JpaSpecificationExecutor<FleetTrip> {

    Optional<FleetTrip> findByIdAndCompanyId(Long id, Long companyId);

    boolean existsByCompanyIdAndTripNumber(Long companyId, String tripNumber);

    boolean existsByTransportRequestId(Long transportRequestId);

    Optional<FleetTrip> findByTransportRequestIdAndCompanyId(Long transportRequestId, Long companyId);
}