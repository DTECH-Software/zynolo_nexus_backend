package com.zynolo_nexus.po_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryDto {
    private long totalRequests;
    private long draftRequests;
    private long submittedRequests;
    private long approvedRequests;
    private long rejectedRequests;
    private long approvedRequestsPendingPoCreation;
    private long totalPurchaseOrders;
    private long draftPurchaseOrders;
    private long sentPurchaseOrders;
    private long vendorConfirmedPurchaseOrders;
    private long vendorRejectedPurchaseOrders;
    private long partiallyReceivedPurchaseOrders;
    private long receivedPurchaseOrders;
    private long partiallyInvoicedPurchaseOrders;
    private long invoicedPurchaseOrders;
    private long matchedPurchaseOrders;
    private long partiallyMatchedPurchaseOrders;
    private long mismatchedPurchaseOrders;
    private long partiallyPaidPurchaseOrders;
    private long paidPurchaseOrders;
    private BigDecimal totalRequestAmount;
    private BigDecimal totalPurchaseOrderAmount;
    private BigDecimal totalInvoicedAmount;
    private BigDecimal totalPaidAmount;
    private BigDecimal outstandingPaymentAmount;
}
