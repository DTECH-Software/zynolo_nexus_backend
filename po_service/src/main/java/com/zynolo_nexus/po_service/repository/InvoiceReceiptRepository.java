package com.zynolo_nexus.po_service.repository;

import com.zynolo_nexus.po_service.model.InvoiceReceipt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface InvoiceReceiptRepository extends JpaRepository<InvoiceReceipt, Long> {

    boolean existsByPurchaseOrder_IdAndInvoiceNoIgnoreCase(Long purchaseOrderId, String invoiceNo);

    List<InvoiceReceipt> findAllByPurchaseOrder_IdOrderByInvoiceDateDescIdDesc(Long purchaseOrderId);

    List<InvoiceReceipt> findAllByPurchaseOrder_IdInOrderByPurchaseOrder_IdAscInvoiceDateDescIdDesc(Collection<Long> purchaseOrderIds);

    Optional<InvoiceReceipt> findTopByPurchaseOrder_IdOrderByInvoiceDateDescIdDesc(Long purchaseOrderId);
}
