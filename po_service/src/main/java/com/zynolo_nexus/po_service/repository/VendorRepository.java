package com.zynolo_nexus.po_service.repository;

import com.zynolo_nexus.po_service.model.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface VendorRepository extends JpaRepository<Vendor, Long>, JpaSpecificationExecutor<Vendor> {

    boolean existsByCodeIgnoreCase(String code);

    List<Vendor> findAllByStatusOrderByCodeAsc(String status);

    Optional<Vendor> findByCodeIgnoreCaseAndStatus(String code, String status);
}
