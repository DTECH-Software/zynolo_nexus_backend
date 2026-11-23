package com.zynolo_nexus.po_service.repository;

import com.zynolo_nexus.po_service.enums.MasterStatus;
import com.zynolo_nexus.po_service.model.CostCenter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface CostCenterRepository extends JpaRepository<CostCenter, Long>, JpaSpecificationExecutor<CostCenter> {

    boolean existsByCodeIgnoreCase(String code);

    List<CostCenter> findAllByStatusOrderByCodeAsc(MasterStatus status);

    Optional<CostCenter> findByCodeIgnoreCaseAndStatus(String code, MasterStatus status);

    Optional<CostCenter> findByDescriptionIgnoreCaseAndStatus(String description, MasterStatus status);
}
