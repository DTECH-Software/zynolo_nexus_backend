package com.zynolo_nexus.fleet_service.repository;

import com.zynolo_nexus.fleet_service.model.FleetTripLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface FleetTripLocationRepository
        extends JpaRepository<FleetTripLocation, Long>, JpaSpecificationExecutor<FleetTripLocation> {

    List<FleetTripLocation> findTop100ByCompanyIdAndTripIdOrderByRecordedAtDesc(Long companyId, Long tripId);
}