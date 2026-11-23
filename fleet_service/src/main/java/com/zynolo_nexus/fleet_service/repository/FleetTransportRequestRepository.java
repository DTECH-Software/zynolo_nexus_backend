package com.zynolo_nexus.fleet_service.repository;

import com.zynolo_nexus.fleet_service.model.FleetTransportRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface FleetTransportRequestRepository
        extends JpaRepository<FleetTransportRequest, Long>, JpaSpecificationExecutor<FleetTransportRequest> {

    Optional<FleetTransportRequest> findByIdAndCompanyId(Long id, Long companyId);

    boolean existsByCompanyIdAndRequestNumber(Long companyId, String requestNumber);
}