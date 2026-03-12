package com.zynolo_nexus.po_service.repository;

import com.zynolo_nexus.po_service.model.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long>, JpaSpecificationExecutor<PurchaseOrder> {

    boolean existsByRequest_Id(Long requestId);

    Optional<PurchaseOrder> findByRequest_Id(Long requestId);
}