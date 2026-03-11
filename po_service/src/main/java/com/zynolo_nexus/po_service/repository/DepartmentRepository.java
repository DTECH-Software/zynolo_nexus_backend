package com.zynolo_nexus.po_service.repository;

import com.zynolo_nexus.po_service.enums.MasterStatus;
import com.zynolo_nexus.po_service.model.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, Long>, JpaSpecificationExecutor<Department> {

    boolean existsByCodeIgnoreCase(String code);

    List<Department> findAllByStatusOrderByCodeAsc(MasterStatus status);

    Optional<Department> findByCodeIgnoreCaseAndStatus(String code, MasterStatus status);

    Optional<Department> findByDescriptionIgnoreCaseAndStatus(String description, MasterStatus status);
}
