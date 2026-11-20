package com.zynolo_nexus.fleet_service.repository;

import com.zynolo_nexus.fleet_service.model.FleetVehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface FleetVehicleRepository extends JpaRepository<FleetVehicle, Long>, JpaSpecificationExecutor<FleetVehicle> {

    boolean existsByCompanyIdAndRegistrationNormalized(Long companyId, String registrationNormalized);

    boolean existsByCompanyIdAndRegistrationNormalizedAndIdNot(Long companyId, String registrationNormalized, Long id);

    Optional<FleetVehicle> findByIdAndCompanyId(Long id, Long companyId);
}
