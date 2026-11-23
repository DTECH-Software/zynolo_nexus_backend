package com.zynolo_nexus.po_service.service.impl;

import com.zynolo_nexus.po_service.dto.request.DashboardActionItemsRequest;
import com.zynolo_nexus.po_service.dto.request.DashboardCriteriaRequest;
import com.zynolo_nexus.po_service.dto.request.DashboardReferenceDataRequest;
import com.zynolo_nexus.po_service.dto.request.DashboardStatusBreakdownRequest;
import com.zynolo_nexus.po_service.dto.request.DashboardSummaryRequest;
import com.zynolo_nexus.po_service.dto.request.DashboardTrendRequest;
import com.zynolo_nexus.po_service.dto.response.DashboardActionItemsDto;
import com.zynolo_nexus.po_service.dto.response.DashboardPrivilegesDto;
import com.zynolo_nexus.po_service.dto.response.DashboardReferenceDataDto;
import com.zynolo_nexus.po_service.dto.response.DashboardStatusBreakdownDto;
import com.zynolo_nexus.po_service.dto.response.DashboardStatusCountDto;
import com.zynolo_nexus.po_service.dto.response.DashboardSummaryDto;
import com.zynolo_nexus.po_service.dto.response.DashboardTrendDto;
import com.zynolo_nexus.po_service.dto.response.DashboardTrendPointDto;
import com.zynolo_nexus.po_service.dto.response.ReferenceOptionDto;
import com.zynolo_nexus.po_service.enums.MasterStatus;
import com.zynolo_nexus.po_service.enums.PoRequestStatus;
import com.zynolo_nexus.po_service.enums.PurchaseOrderStatus;
import com.zynolo_nexus.po_service.model.InvoiceReceipt;
import com.zynolo_nexus.po_service.model.PoRequest;
import com.zynolo_nexus.po_service.model.PurchaseOrder;
import com.zynolo_nexus.po_service.model.PurchaseOrderPayment;
import com.zynolo_nexus.po_service.repository.CompanyRepository;
import com.zynolo_nexus.po_service.repository.InvoiceReceiptRepository;
import com.zynolo_nexus.po_service.repository.PoRequestRepository;
import com.zynolo_nexus.po_service.repository.PurchaseOrderPaymentRepository;
import com.zynolo_nexus.po_service.repository.PurchaseOrderRepository;
import com.zynolo_nexus.po_service.repository.VendorRepository;
import com.zynolo_nexus.po_service.service.PurchaseOrderDashboardService;
import com.zynolo_nexus.po_service.service.support.PagePrivilegeResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PurchaseOrderDashboardServiceImpl implements PurchaseOrderDashboardService {

    private static final String PAGE_CODE = "PODB";
    private static final String PENDING_MATCH = "PENDING_MATCH";
    private static final String MATCHED = "MATCHED";
    private static final String PARTIALLY_MATCHED = "PARTIALLY_MATCHED";
    private static final String MISMATCHED = "MISMATCHED";
    private static final String PARTIALLY_INVOICED = "PARTIALLY_INVOICED";
    private static final String INVOICED = "INVOICED";
    private static final String PENDING_PAYMENT = "PENDING_PAYMENT";
    private static final String PARTIALLY_PAID = "PARTIALLY_PAID";
    private static final String PAID = "PAID";
    private static final DateTimeFormatter PERIOD_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    private final CompanyRepository companyRepository;
    private final VendorRepository vendorRepository;
    private final PoRequestRepository poRequestRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final InvoiceReceiptRepository invoiceReceiptRepository;
    private final PurchaseOrderPaymentRepository purchaseOrderPaymentRepository;
    private final PagePrivilegeResolver pagePrivilegeResolver;

    @Override
    @Transactional(readOnly = true)
    public DashboardReferenceDataDto getReferenceData(DashboardReferenceDataRequest request) {
        var privileges = pagePrivilegeResolver.resolve(request != null ? request.getUsername() : null, PAGE_CODE);

        return DashboardReferenceDataDto.builder()
                .companies(companyRepository.findAllByStatusOrderByCodeAsc(MasterStatus.ACTIVE).stream()
                        .map(company -> option(company.getCode(), company.getDescription()))
                        .toList())
                .vendors(vendorRepository.findAllByStatusOrderByCodeAsc("ACTIVE").stream()
                        .map(vendor -> option(vendor.getCode(), vendor.getDescription()))
                        .toList())
                .privileges(DashboardPrivilegesDto.builder()
                        .view(privileges.isView())
                        .search(privileges.isSearch())
                        .build())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardSummaryDto getSummary(DashboardSummaryRequest request) {
        DashboardData data = loadDashboardData(request);

        return DashboardSummaryDto.builder()
                .totalRequests(data.requests.size())
                .draftRequests(countRequests(data.requests, PoRequestStatus.DRAFT))
                .submittedRequests(countRequests(data.requests, PoRequestStatus.SUBMITTED))
                .approvedRequests(countRequests(data.requests, PoRequestStatus.APPROVED))
                .rejectedRequests(countRequests(data.requests, PoRequestStatus.REJECTED))
                .approvedRequestsPendingPoCreation(data.requests.stream()
                        .filter(poRequest -> poRequest.getStatus() == PoRequestStatus.APPROVED)
                        .filter(poRequest -> !data.purchaseOrderRequestIds.contains(poRequest.getId()))
                        .count())
                .totalPurchaseOrders(data.purchaseOrders.size())
                .draftPurchaseOrders(countPurchaseOrders(data.purchaseOrders, PurchaseOrderStatus.DRAFT))
                .sentPurchaseOrders(countPurchaseOrders(data.purchaseOrders, PurchaseOrderStatus.SENT))
                .vendorConfirmedPurchaseOrders(data.purchaseOrders.stream()
                        .filter(po -> po.getStatus() == PurchaseOrderStatus.VENDOR_CONFIRMED
                                || po.getStatus() == PurchaseOrderStatus.PARTIALLY_CONFIRMED
                                || po.getStatus() == PurchaseOrderStatus.PARTIALLY_APPROVED)
                        .count())
                .vendorRejectedPurchaseOrders(countPurchaseOrders(data.purchaseOrders, PurchaseOrderStatus.VENDOR_REJECTED))
                .partiallyReceivedPurchaseOrders(countPurchaseOrders(data.purchaseOrders, PurchaseOrderStatus.PARTIALLY_RECEIVED))
                .receivedPurchaseOrders(countPurchaseOrders(data.purchaseOrders, PurchaseOrderStatus.RECEIVED))
                .partiallyInvoicedPurchaseOrders(countInvoiceStatus(data.purchaseOrders, data.invoiceReceiptsByPoId, PARTIALLY_INVOICED))
                .invoicedPurchaseOrders(countInvoiceStatus(data.purchaseOrders, data.invoiceReceiptsByPoId, INVOICED))
                .matchedPurchaseOrders(countMatchStatus(data.purchaseOrders, data.invoiceReceiptsByPoId, MATCHED))
                .partiallyMatchedPurchaseOrders(countMatchStatus(data.purchaseOrders, data.invoiceReceiptsByPoId, PARTIALLY_MATCHED))
                .mismatchedPurchaseOrders(countMatchStatus(data.purchaseOrders, data.invoiceReceiptsByPoId, MISMATCHED))
                .partiallyPaidPurchaseOrders(countPaymentStatus(data.purchaseOrders, data.invoiceReceiptsByPoId, data.paymentsByPoId, PARTIALLY_PAID))
                .paidPurchaseOrders(countPaymentStatus(data.purchaseOrders, data.invoiceReceiptsByPoId, data.paymentsByPoId, PAID))
                .totalRequestAmount(sum(data.requests, PoRequest::getTotalAmount))
                .totalPurchaseOrderAmount(sum(data.purchaseOrders, PurchaseOrder::getTotalAmount))
                .totalInvoicedAmount(sumFlat(data.invoiceReceiptsByPoId.values(), InvoiceReceipt::getTotalAmount))
                .totalPaidAmount(sumFlat(data.paymentsByPoId.values(), PurchaseOrderPayment::getPaidAmount))
                .outstandingPaymentAmount(max(BigDecimal.ZERO,
                        sumFlat(data.invoiceReceiptsByPoId.values(), InvoiceReceipt::getTotalAmount)
                                .subtract(sumFlat(data.paymentsByPoId.values(), PurchaseOrderPayment::getPaidAmount))))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardStatusBreakdownDto getStatusBreakdown(DashboardStatusBreakdownRequest request) {
        DashboardData data = loadDashboardData(request);

        List<DashboardStatusCountDto> requestStatuses = List.of(
                statusCount(PoRequestStatus.DRAFT.name(), "Draft", countRequests(data.requests, PoRequestStatus.DRAFT)),
                statusCount(PoRequestStatus.SUBMITTED.name(), "Submitted", countRequests(data.requests, PoRequestStatus.SUBMITTED)),
                statusCount(PoRequestStatus.APPROVED.name(), "Approved", countRequests(data.requests, PoRequestStatus.APPROVED)),
                statusCount(PoRequestStatus.REJECTED.name(), "Rejected", countRequests(data.requests, PoRequestStatus.REJECTED))
        );

        List<DashboardStatusCountDto> purchaseOrderStatuses = List.of(
                statusCount(PurchaseOrderStatus.DRAFT.name(), "Draft", countPurchaseOrders(data.purchaseOrders, PurchaseOrderStatus.DRAFT)),
                statusCount(PurchaseOrderStatus.SENT.name(), "Sent", countPurchaseOrders(data.purchaseOrders, PurchaseOrderStatus.SENT)),
                statusCount(PurchaseOrderStatus.VENDOR_CONFIRMED.name(), "Vendor Confirmed", countPurchaseOrders(data.purchaseOrders, PurchaseOrderStatus.VENDOR_CONFIRMED)),
                statusCount(PurchaseOrderStatus.PARTIALLY_CONFIRMED.name(), "Partially Confirmed", countPurchaseOrders(data.purchaseOrders, PurchaseOrderStatus.PARTIALLY_CONFIRMED)),
                statusCount(PurchaseOrderStatus.PARTIALLY_APPROVED.name(), "Partially Approved", countPurchaseOrders(data.purchaseOrders, PurchaseOrderStatus.PARTIALLY_APPROVED)),
                statusCount(PurchaseOrderStatus.VENDOR_REJECTED.name(), "Vendor Rejected", countPurchaseOrders(data.purchaseOrders, PurchaseOrderStatus.VENDOR_REJECTED)),
                statusCount(PurchaseOrderStatus.PARTIALLY_RECEIVED.name(), "Partially Received", countPurchaseOrders(data.purchaseOrders, PurchaseOrderStatus.PARTIALLY_RECEIVED)),
                statusCount(PurchaseOrderStatus.RECEIVED.name(), "Received", countPurchaseOrders(data.purchaseOrders, PurchaseOrderStatus.RECEIVED)),
                statusCount(PurchaseOrderStatus.CLOSED.name(), "Closed", countPurchaseOrders(data.purchaseOrders, PurchaseOrderStatus.CLOSED))
        );

        List<DashboardStatusCountDto> matchStatuses = List.of(
                statusCount(PENDING_MATCH, "Pending Match", countMatchStatus(data.purchaseOrders, data.invoiceReceiptsByPoId, PENDING_MATCH)),
                statusCount(MATCHED, "Matched", countMatchStatus(data.purchaseOrders, data.invoiceReceiptsByPoId, MATCHED)),
                statusCount(PARTIALLY_MATCHED, "Partially Matched", countMatchStatus(data.purchaseOrders, data.invoiceReceiptsByPoId, PARTIALLY_MATCHED)),
                statusCount(MISMATCHED, "Mismatched", countMatchStatus(data.purchaseOrders, data.invoiceReceiptsByPoId, MISMATCHED))
        );

        List<DashboardStatusCountDto> paymentStatuses = List.of(
                statusCount(PENDING_PAYMENT, "Pending Payment", countPaymentStatus(data.purchaseOrders, data.invoiceReceiptsByPoId, data.paymentsByPoId, PENDING_PAYMENT)),
                statusCount(PARTIALLY_PAID, "Partially Paid", countPaymentStatus(data.purchaseOrders, data.invoiceReceiptsByPoId, data.paymentsByPoId, PARTIALLY_PAID)),
                statusCount(PAID, "Paid", countPaymentStatus(data.purchaseOrders, data.invoiceReceiptsByPoId, data.paymentsByPoId, PAID))
        );

        return DashboardStatusBreakdownDto.builder()
                .requestStatuses(requestStatuses)
                .purchaseOrderStatuses(purchaseOrderStatuses)
                .matchStatuses(matchStatuses)
                .paymentStatuses(paymentStatuses)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardTrendDto getTrend(DashboardTrendRequest request) {
        DashboardData data = loadDashboardData(request);
        List<YearMonth> periods = resolvePeriods(request);

        List<DashboardTrendPointDto> content = periods.stream()
                .map(period -> DashboardTrendPointDto.builder()
                        .period(period.format(PERIOD_FORMATTER))
                        .requestCount(countByMonth(data.requests, period, PoRequest::getCreatedDate))
                        .requestAmount(sumByMonth(data.requests, period, PoRequest::getCreatedDate, PoRequest::getTotalAmount))
                        .purchaseOrderCount(countByMonth(data.purchaseOrders, period, PurchaseOrder::getCreatedDate))
                        .purchaseOrderAmount(sumByMonth(data.purchaseOrders, period, PurchaseOrder::getCreatedDate, PurchaseOrder::getTotalAmount))
                        .invoicedAmount(sumByMonth(flatten(data.invoiceReceiptsByPoId.values()), period,
                                invoiceReceipt -> invoiceReceipt.getInvoiceDate() != null ? invoiceReceipt.getInvoiceDate().atStartOfDay() : null,
                                InvoiceReceipt::getTotalAmount))
                        .paidAmount(sumByMonth(flatten(data.paymentsByPoId.values()), period,
                                payment -> payment.getPaymentDate() != null ? payment.getPaymentDate().atStartOfDay() : null,
                                PurchaseOrderPayment::getPaidAmount))
                        .build())
                .toList();

        return DashboardTrendDto.builder()
                .content(content)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardActionItemsDto getActionItems(DashboardActionItemsRequest request) {
        DashboardData data = loadDashboardData(request);

        return DashboardActionItemsDto.builder()
                .pendingApprovals(countRequests(data.requests, PoRequestStatus.SUBMITTED))
                .approvedRequestsPendingPoCreation(data.requests.stream()
                        .filter(poRequest -> poRequest.getStatus() == PoRequestStatus.APPROVED)
                        .filter(poRequest -> !data.purchaseOrderRequestIds.contains(poRequest.getId()))
                        .count())
                .purchaseOrdersPendingDispatch(countPurchaseOrders(data.purchaseOrders, PurchaseOrderStatus.DRAFT))
                .purchaseOrdersPendingVendorConfirmation(countPurchaseOrders(data.purchaseOrders, PurchaseOrderStatus.SENT))
                .purchaseOrdersPendingGoodsReceipt(data.purchaseOrders.stream()
                        .filter(po -> po.getStatus() == PurchaseOrderStatus.VENDOR_CONFIRMED
                                || po.getStatus() == PurchaseOrderStatus.PARTIALLY_CONFIRMED
                                || po.getStatus() == PurchaseOrderStatus.PARTIALLY_APPROVED
                                || po.getStatus() == PurchaseOrderStatus.PARTIALLY_RECEIVED)
                        .count())
                .purchaseOrdersPendingInvoiceReceipt(data.purchaseOrders.stream()
                        .filter(po -> po.getStatus() == PurchaseOrderStatus.PARTIALLY_RECEIVED
                                || po.getStatus() == PurchaseOrderStatus.RECEIVED)
                        .filter(po -> !INVOICED.equals(deriveInvoiceStatus(po, data.invoiceReceiptsByPoId.getOrDefault(po.getId(), List.of()))))
                        .count())
                .purchaseOrdersPendingThreeWayMatch(data.purchaseOrders.stream()
                        .filter(po -> !data.invoiceReceiptsByPoId.getOrDefault(po.getId(), List.of()).isEmpty())
                        .filter(po -> PENDING_MATCH.equals(deriveMatchStatus(po, data.invoiceReceiptsByPoId.getOrDefault(po.getId(), List.of()))))
                        .count())
                .purchaseOrdersPendingPayment(data.purchaseOrders.stream()
                        .filter(po -> PENDING_PAYMENT.equals(derivePaymentStatus(po,
                                data.invoiceReceiptsByPoId.getOrDefault(po.getId(), List.of()),
                                data.paymentsByPoId.getOrDefault(po.getId(), List.of()))))
                        .count())
                .mismatchedPurchaseOrders(countMatchStatus(data.purchaseOrders, data.invoiceReceiptsByPoId, MISMATCHED))
                .vendorRejectedPurchaseOrders(countPurchaseOrders(data.purchaseOrders, PurchaseOrderStatus.VENDOR_REJECTED))
                .build();
    }

    private DashboardData loadDashboardData(DashboardCriteriaRequest request) {
        List<PoRequest> requests = poRequestRepository.findAll().stream()
                .filter(poRequest -> matchesRequest(poRequest, request))
                .toList();

        List<PurchaseOrder> purchaseOrders = purchaseOrderRepository.findAll().stream()
                .filter(purchaseOrder -> matchesPurchaseOrder(purchaseOrder, request))
                .toList();

        Set<Long> purchaseOrderIds = purchaseOrders.stream()
                .map(PurchaseOrder::getId)
                .collect(Collectors.toSet());

        Map<Long, List<InvoiceReceipt>> invoiceReceiptsByPoId = purchaseOrderIds.isEmpty()
                ? Map.of()
                : invoiceReceiptRepository.findAllByPurchaseOrder_IdInOrderByPurchaseOrder_IdAscInvoiceDateDescIdDesc(purchaseOrderIds).stream()
                .filter(invoiceReceipt -> matchesInvoiceReceipt(invoiceReceipt, request))
                .collect(Collectors.groupingBy(receipt -> receipt.getPurchaseOrder().getId(), LinkedHashMap::new, Collectors.toList()));

        Map<Long, List<PurchaseOrderPayment>> paymentsByPoId = purchaseOrderIds.isEmpty()
                ? Map.of()
                : purchaseOrderPaymentRepository.findAllByPurchaseOrder_IdInOrderByPurchaseOrder_IdAscPaymentDateDescIdDesc(purchaseOrderIds).stream()
                .filter(payment -> matchesPayment(payment, request))
                .collect(Collectors.groupingBy(payment -> payment.getPurchaseOrder().getId(), LinkedHashMap::new, Collectors.toList()));

        Set<Long> purchaseOrderRequestIds = purchaseOrders.stream()
                .map(purchaseOrder -> purchaseOrder.getRequest().getId())
                .collect(Collectors.toSet());

        return new DashboardData(requests, purchaseOrders, invoiceReceiptsByPoId, paymentsByPoId, purchaseOrderRequestIds);
    }

    private boolean matchesRequest(PoRequest poRequest, DashboardCriteriaRequest request) {
        if (poRequest == null) {
            return false;
        }
        if (request == null) {
            return true;
        }
        if (hasText(request.getCompanyCode()) && !equalsIgnoreCase(poRequest.getCompanyCode(), request.getCompanyCode())) {
            return false;
        }
        if (hasText(request.getVendorCode()) && !equalsIgnoreCase(poRequest.getVendorCode(), request.getVendorCode())) {
            return false;
        }
        return withinRange(poRequest.getCreatedDate(), request.getDateFrom(), request.getDateTo());
    }

    private boolean matchesPurchaseOrder(PurchaseOrder purchaseOrder, DashboardCriteriaRequest request) {
        if (purchaseOrder == null) {
            return false;
        }
        if (request == null) {
            return true;
        }
        if (hasText(request.getCompanyCode()) && !equalsIgnoreCase(purchaseOrder.getCompanyCode(), request.getCompanyCode())) {
            return false;
        }
        if (hasText(request.getVendorCode()) && !equalsIgnoreCase(purchaseOrder.getVendorCode(), request.getVendorCode())) {
            return false;
        }
        return withinRange(purchaseOrder.getCreatedDate(), request.getDateFrom(), request.getDateTo());
    }

    private boolean matchesInvoiceReceipt(InvoiceReceipt invoiceReceipt, DashboardCriteriaRequest request) {
        if (invoiceReceipt == null || request == null || (request.getDateFrom() == null && request.getDateTo() == null)) {
            return true;
        }
        if (invoiceReceipt.getInvoiceDate() == null) {
            return false;
        }
        if (request.getDateFrom() != null && invoiceReceipt.getInvoiceDate().isBefore(request.getDateFrom())) {
            return false;
        }
        return request.getDateTo() == null || !invoiceReceipt.getInvoiceDate().isAfter(request.getDateTo());
    }

    private boolean matchesPayment(PurchaseOrderPayment payment, DashboardCriteriaRequest request) {
        if (payment == null || request == null || (request.getDateFrom() == null && request.getDateTo() == null)) {
            return true;
        }
        if (payment.getPaymentDate() == null) {
            return false;
        }
        if (request.getDateFrom() != null && payment.getPaymentDate().isBefore(request.getDateFrom())) {
            return false;
        }
        return request.getDateTo() == null || !payment.getPaymentDate().isAfter(request.getDateTo());
    }

    private List<YearMonth> resolvePeriods(DashboardTrendRequest request) {
        if (request.getDateFrom() != null && request.getDateTo() != null) {
            YearMonth start = YearMonth.from(request.getDateFrom());
            YearMonth end = YearMonth.from(request.getDateTo());
            List<YearMonth> periods = new ArrayList<>();
            YearMonth current = start;
            while (!current.isAfter(end)) {
                periods.add(current);
                current = current.plusMonths(1);
            }
            return periods;
        }

        int months = request.getMonths();
        YearMonth end = YearMonth.now();
        YearMonth start = end.minusMonths(months - 1L);
        List<YearMonth> periods = new ArrayList<>();
        YearMonth current = start;
        while (!current.isAfter(end)) {
            periods.add(current);
            current = current.plusMonths(1);
        }
        return periods;
    }

    private long countRequests(List<PoRequest> requests, PoRequestStatus status) {
        return requests.stream().filter(poRequest -> poRequest.getStatus() == status).count();
    }

    private long countPurchaseOrders(List<PurchaseOrder> purchaseOrders, PurchaseOrderStatus status) {
        return purchaseOrders.stream().filter(purchaseOrder -> purchaseOrder.getStatus() == status).count();
    }

    private long countInvoiceStatus(List<PurchaseOrder> purchaseOrders,
                                    Map<Long, List<InvoiceReceipt>> invoiceReceiptsByPoId,
                                    String status) {
        return purchaseOrders.stream()
                .filter(purchaseOrder -> status.equals(deriveInvoiceStatus(purchaseOrder,
                        invoiceReceiptsByPoId.getOrDefault(purchaseOrder.getId(), List.of()))))
                .count();
    }

    private long countMatchStatus(List<PurchaseOrder> purchaseOrders,
                                  Map<Long, List<InvoiceReceipt>> invoiceReceiptsByPoId,
                                  String status) {
        return purchaseOrders.stream()
                .filter(purchaseOrder -> !invoiceReceiptsByPoId.getOrDefault(purchaseOrder.getId(), List.of()).isEmpty())
                .filter(purchaseOrder -> status.equals(deriveMatchStatus(purchaseOrder,
                        invoiceReceiptsByPoId.getOrDefault(purchaseOrder.getId(), List.of()))))
                .count();
    }

    private long countPaymentStatus(List<PurchaseOrder> purchaseOrders,
                                    Map<Long, List<InvoiceReceipt>> invoiceReceiptsByPoId,
                                    Map<Long, List<PurchaseOrderPayment>> paymentsByPoId,
                                    String status) {
        return purchaseOrders.stream()
                .filter(purchaseOrder -> status.equals(derivePaymentStatus(purchaseOrder,
                        invoiceReceiptsByPoId.getOrDefault(purchaseOrder.getId(), List.of()),
                        paymentsByPoId.getOrDefault(purchaseOrder.getId(), List.of()))))
                .count();
    }

    private String deriveMatchStatus(PurchaseOrder purchaseOrder, List<InvoiceReceipt> invoiceReceipts) {
        if (invoiceReceipts.isEmpty()) {
            return PENDING_MATCH;
        }
        if (hasText(purchaseOrder.getMatchStatus())) {
            return purchaseOrder.getMatchStatus();
        }
        return PENDING_MATCH;
    }

    private String deriveInvoiceStatus(PurchaseOrder purchaseOrder, List<InvoiceReceipt> invoiceReceipts) {
        if (invoiceReceipts.isEmpty()) {
            return purchaseOrder.getStatus() == PurchaseOrderStatus.RECEIVED ? PARTIALLY_INVOICED : purchaseOrder.getStatus().name();
        }
        BigDecimal totalInvoicedAmount = sum(invoiceReceipts, InvoiceReceipt::getTotalAmount);
        return totalInvoicedAmount.compareTo(nullSafe(purchaseOrder.getTotalAmount())) >= 0 ? INVOICED : PARTIALLY_INVOICED;
    }

    private String derivePaymentStatus(PurchaseOrder purchaseOrder,
                                       List<InvoiceReceipt> invoiceReceipts,
                                       List<PurchaseOrderPayment> payments) {
        if (invoiceReceipts.isEmpty() || !hasPayableMatch(deriveMatchStatus(purchaseOrder, invoiceReceipts))) {
            return null;
        }
        BigDecimal totalInvoicedAmount = sum(invoiceReceipts, InvoiceReceipt::getTotalAmount);
        BigDecimal totalPaidAmount = sum(payments, PurchaseOrderPayment::getPaidAmount);
        if (totalPaidAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return PENDING_PAYMENT;
        }
        return totalPaidAmount.compareTo(totalInvoicedAmount) >= 0 ? PAID : PARTIALLY_PAID;
    }

    private boolean hasPayableMatch(String matchStatus) {
        String normalized = trim(matchStatus);
        return MATCHED.equalsIgnoreCase(normalized) || PARTIALLY_MATCHED.equalsIgnoreCase(normalized);
    }

    private <T> BigDecimal sum(Collection<T> items, Function<T, BigDecimal> mapper) {
        if (items == null || items.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return items.stream()
                .map(mapper)
                .filter(value -> value != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private <T> BigDecimal sumFlat(Collection<List<T>> groups, Function<T, BigDecimal> mapper) {
        if (groups == null || groups.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return groups.stream()
                .flatMap(List::stream)
                .map(mapper)
                .filter(value -> value != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private <T> long countByMonth(Collection<T> items, YearMonth period, Function<T, LocalDateTime> dateExtractor) {
        return items.stream()
                .map(dateExtractor)
                .filter(date -> date != null && YearMonth.from(date).equals(period))
                .count();
    }

    private <T> BigDecimal sumByMonth(Collection<T> items,
                                      YearMonth period,
                                      Function<T, LocalDateTime> dateExtractor,
                                      Function<T, BigDecimal> amountExtractor) {
        return items.stream()
                .filter(item -> {
                    LocalDateTime date = dateExtractor.apply(item);
                    return date != null && YearMonth.from(date).equals(period);
                })
                .map(amountExtractor)
                .filter(value -> value != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private <T> List<T> flatten(Collection<List<T>> groups) {
        return groups.stream().flatMap(List::stream).toList();
    }

    private boolean withinRange(LocalDateTime value, LocalDate dateFrom, LocalDate dateTo) {
        if (value == null) {
            return false;
        }
        if (dateFrom != null && value.isBefore(dateFrom.atStartOfDay())) {
            return false;
        }
        return dateTo == null || !value.isAfter(dateTo.atTime(23, 59, 59));
    }

    private BigDecimal max(BigDecimal left, BigDecimal right) {
        return left.compareTo(right) >= 0 ? left : right;
    }

    private BigDecimal nullSafe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private DashboardStatusCountDto statusCount(String code, String description, long count) {
        return DashboardStatusCountDto.builder()
                .code(code)
                .description(description)
                .count(count)
                .build();
    }

    private ReferenceOptionDto option(String code, String description) {
        return ReferenceOptionDto.builder()
                .code(code)
                .description(description)
                .build();
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private boolean equalsIgnoreCase(String left, String right) {
        return trim(left) != null && trim(right) != null && trim(left).equalsIgnoreCase(trim(right));
    }

    private record DashboardData(List<PoRequest> requests,
                                 List<PurchaseOrder> purchaseOrders,
                                 Map<Long, List<InvoiceReceipt>> invoiceReceiptsByPoId,
                                 Map<Long, List<PurchaseOrderPayment>> paymentsByPoId,
                                 Set<Long> purchaseOrderRequestIds) {
    }
}
