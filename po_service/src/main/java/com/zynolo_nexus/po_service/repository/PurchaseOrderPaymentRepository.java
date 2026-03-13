package com.zynolo_nexus.po_service.repository;

import com.zynolo_nexus.po_service.model.PurchaseOrderPayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PurchaseOrderPaymentRepository extends JpaRepository<PurchaseOrderPayment, Long> {

    List<PurchaseOrderPayment> findAllByPurchaseOrder_IdOrderByPaymentDateDescIdDesc(Long purchaseOrderId);

    List<PurchaseOrderPayment> findAllByPurchaseOrder_IdInOrderByPurchaseOrder_IdAscPaymentDateDescIdDesc(Collection<Long> purchaseOrderIds);

    Optional<PurchaseOrderPayment> findTopByPurchaseOrder_IdOrderByPaymentDateDescIdDesc(Long purchaseOrderId);
}
