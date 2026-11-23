package com.zynolo_nexus.po_service.repository;

import com.zynolo_nexus.po_service.model.GoodsReceipt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface GoodsReceiptRepository extends JpaRepository<GoodsReceipt, Long> {

    List<GoodsReceipt> findAllByPurchaseOrder_IdOrderByReceiptDateDescIdDesc(Long purchaseOrderId);

    List<GoodsReceipt> findAllByPurchaseOrder_IdInOrderByPurchaseOrder_IdAscReceiptDateDescIdDesc(Collection<Long> purchaseOrderIds);

    Optional<GoodsReceipt> findTopByPurchaseOrder_IdOrderByReceiptDateDescIdDesc(Long purchaseOrderId);
}
