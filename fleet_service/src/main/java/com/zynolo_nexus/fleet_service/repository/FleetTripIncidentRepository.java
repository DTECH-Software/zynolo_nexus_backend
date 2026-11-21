package com.zynolo_nexus.fleet_service.repository;

import com.zynolo_nexus.fleet_service.model.FleetTripIncident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface FleetTripIncidentRepository
        extends JpaRepository<FleetTripIncident, Long>, JpaSpecificationExecutor<FleetTripIncident> {

    Optional<FleetTripIncident> findByIdAndCompanyId(Long id, Long companyId);
}