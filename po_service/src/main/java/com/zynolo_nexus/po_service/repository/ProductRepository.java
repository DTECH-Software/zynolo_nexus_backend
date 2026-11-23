package com.zynolo_nexus.po_service.repository;

import com.zynolo_nexus.po_service.enums.MasterStatus;
import com.zynolo_nexus.po_service.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    boolean existsByCodeIgnoreCase(String code);

    List<Product> findAllByStatusOrderByCodeAsc(MasterStatus status);

    Optional<Product> findByCodeIgnoreCaseAndStatus(String code, MasterStatus status);
}
