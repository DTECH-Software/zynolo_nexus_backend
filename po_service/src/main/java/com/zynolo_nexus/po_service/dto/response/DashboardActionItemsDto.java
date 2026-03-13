package com.zynolo_nexus.po_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardActionItemsDto {
    private long pendingApprovals;
    private long approvedRequestsPendingPoCreation;
    private long purchaseOrdersPendingDispatch;
    private long purchaseOrdersPendingVendorConfirmation;
    private long purchaseOrdersPendingGoodsReceipt;
    private long purchaseOrdersPendingInvoiceReceipt;
    private long purchaseOrdersPendingThreeWayMatch;
    private long purchaseOrdersPendingPayment;
    private long mismatchedPurchaseOrders;
    private long vendorRejectedPurchaseOrders;
}
