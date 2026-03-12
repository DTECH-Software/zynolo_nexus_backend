package com.zynolo_nexus.po_service.repository;

import com.zynolo_nexus.po_service.model.GoodsReceipt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GoodsReceiptRepository extends JpaRepository<GoodsReceipt, Long> {

    List<GoodsReceipt> findAllByPurchaseOrder_IdOrderByReceiptDateDescIdDesc(Long purchaseOrderId);

    Optional<GoodsReceipt> findTopByPurchaseOrder_IdOrderByReceiptDateDescIdDesc(Long purchaseOrderId);
}
