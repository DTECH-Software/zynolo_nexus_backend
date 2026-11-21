package com.zynolo_nexus.fleet_service.repository;

import com.zynolo_nexus.fleet_service.enums.MasterStatus;
import com.zynolo_nexus.fleet_service.model.FleetDriver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface FleetDriverRepository extends JpaRepository<FleetDriver, Long>, JpaSpecificationExecutor<FleetDriver> {

    boolean existsByCompanyIdAndMobileNormalized(Long companyId, String mobileNormalized);

    boolean existsByCompanyIdAndMobileNormalizedAndIdNot(Long companyId, String mobileNormalized, Long id);

    Optional<FleetDriver> findByIdAndCompanyId(Long id, Long companyId);

    List<FleetDriver> findByCompanyIdAndStatusOrderByNameAsc(Long companyId, MasterStatus status);
}