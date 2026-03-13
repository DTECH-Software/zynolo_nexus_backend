package com.zynolo_nexus.po_service.service.impl;

import com.zynolo_nexus.po_service.dto.request.PaymentFilterRequest;
import com.zynolo_nexus.po_service.dto.request.PaymentFilterSearch;
import com.zynolo_nexus.po_service.dto.request.PaymentHistoryRequest;
import com.zynolo_nexus.po_service.dto.request.PaymentPayRequest;
import com.zynolo_nexus.po_service.dto.request.PaymentReferenceDataRequest;
import com.zynolo_nexus.po_service.dto.request.PaymentViewRequest;
import com.zynolo_nexus.po_service.dto.response.PaymentFilterResultDto;
import com.zynolo_nexus.po_service.dto.response.PaymentHistoryDto;
import com.zynolo_nexus.po_service.dto.response.PaymentHistoryEntryDto;
import com.zynolo_nexus.po_service.dto.response.PaymentItemBalanceDto;
import com.zynolo_nexus.po_service.dto.response.PaymentListItemDto;
import com.zynolo_nexus.po_service.dto.response.PaymentPrivilegesDto;
import com.zynolo_nexus.po_service.dto.response.PaymentReferenceDataDto;
import com.zynolo_nexus.po_service.dto.response.PaymentViewDto;
import com.zynolo_nexus.po_service.dto.response.ReferenceOptionDto;
import com.zynolo_nexus.po_service.enums.MasterStatus;
import com.zynolo_nexus.po_service.exception.BadRequestException;
import com.zynolo_nexus.po_service.exception.ResourceNotFoundException;
import com.zynolo_nexus.po_service.model.InvoiceReceipt;
import com.zynolo_nexus.po_service.model.PurchaseOrder;
import com.zynolo_nexus.po_service.model.PurchaseOrderItem;
import com.zynolo_nexus.po_service.model.PurchaseOrderPayment;
import com.zynolo_nexus.po_service.repository.CompanyRepository;
import com.zynolo_nexus.po_service.repository.InvoiceReceiptRepository;
import com.zynolo_nexus.po_service.repository.PurchaseOrderPaymentRepository;
import com.zynolo_nexus.po_service.repository.PurchaseOrderRepository;
import com.zynolo_nexus.po_service.repository.VendorRepository;
import com.zynolo_nexus.po_service.service.PurchaseOrderPaymentService;
import com.zynolo_nexus.po_service.service.support.PagePrivilegeResolver;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PurchaseOrderPaymentServiceImpl implements PurchaseOrderPaymentService {

    private static final String PAGE_CODE = "POPY";
    private static final String STATUS_MATCHED = "MATCHED";
    private static final String STATUS_PARTIALLY_MATCHED = "PARTIALLY_MATCHED";
    private static final String STATUS_PARTIALLY_PAID = "PARTIALLY_PAID";
    private static final String STATUS_PAID = "PAID";
    private static final String PAYMENT_METHOD_CHEQUE = "CHEQUE";

    private final CompanyRepository companyRepository;
    private final VendorRepository vendorRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final InvoiceReceiptRepository invoiceReceiptRepository;
    private final PurchaseOrderPaymentRepository purchaseOrderPaymentRepository;
    private final PagePrivilegeResolver pagePrivilegeResolver;

    @Override
    @Transactional(readOnly = true)
    public PaymentReferenceDataDto getReferenceData(PaymentReferenceDataRequest request) {
        var privileges = pagePrivilegeResolver.resolve(request != null ? request.getUsername() : null, PAGE_CODE);

        return PaymentReferenceDataDto.builder()
                .companies(companyRepository.findAllByStatusOrderByCodeAsc(MasterStatus.ACTIVE).stream()
                        .map(company -> option(company.getCode(), company.getDescription()))
                        .toList())
                .vendors(vendorRepository.findAllByStatusOrderByCodeAsc("ACTIVE").stream()
                        .map(vendor -> option(vendor.getCode(), vendor.getDescription()))
                        .toList())
                .paymentMethods(List.of(option(PAYMENT_METHOD_CHEQUE, "Cheque")))
                .defaultStatus(List.of(
                        option(STATUS_MATCHED, "Matched"),
                        option(STATUS_PARTIALLY_MATCHED, "Partially Matched"),
                        option(STATUS_PARTIALLY_PAID, "Partially Paid"),
                        option(STATUS_PAID, "Paid")
                ))
                .privileges(PaymentPrivilegesDto.builder()
                        .view(privileges.isView())
                        .search(privileges.isSearch())
                        .pay(privileges.isPay())
                        .build())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentFilterResultDto filterList(PaymentFilterRequest request) {
        List<PurchaseOrder> purchaseOrders = purchaseOrderRepository.findAll(buildSpecification(request.getSearch()));
        Map<Long, List<InvoiceReceipt>> invoiceReceipts = loadInvoiceReceipts(purchaseOrders);
        Map<Long, List<PurchaseOrderPayment>> payments = loadPayments(purchaseOrders);

        List<PaymentListItemDto> filtered = purchaseOrders.stream()
                .filter(po -> isVisibleForPayment(po, invoiceReceipts.getOrDefault(po.getId(), List.of())))
                .filter(po -> matchesStatus(po, invoiceReceipts.getOrDefault(po.getId(), List.of()), payments.getOrDefault(po.getId(), List.of()), request.getSearch()))
                .filter(po -> matchesChequeNo(payments.getOrDefault(po.getId(), List.of()), request.getSearch()))
                .map(po -> toListItemDto(po, invoiceReceipts.getOrDefault(po.getId(), List.of()), payments.getOrDefault(po.getId(), List.of())))
                .sorted(resolveComparator(request.getSortColumn(), request.getSortDirection()))
                .toList();

        int fromIndex = Math.min(request.getPage() * request.getSize(), filtered.size());
        int toIndex = Math.min(fromIndex + request.getSize(), filtered.size());
        List<PaymentListItemDto> content = filtered.subList(fromIndex, toIndex);

        return PaymentFilterResultDto.builder()
                .content(content)
                .size(content.size())
                .totalRecords(filtered.size())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentViewDto view(PaymentViewRequest request) {
        PurchaseOrder purchaseOrder = getPurchaseOrder(request.getId());
        List<InvoiceReceipt> invoiceReceipts = invoiceReceiptRepository.findAllByPurchaseOrder_IdOrderByInvoiceDateDescIdDesc(purchaseOrder.getId());
        List<PurchaseOrderPayment> payments = purchaseOrderPaymentRepository.findAllByPurchaseOrder_IdOrderByPaymentDateDescIdDesc(purchaseOrder.getId());
        validateVisible(purchaseOrder, invoiceReceipts);
        return toViewDto(purchaseOrder, invoiceReceipts, payments);
    }

    @Override
    @Transactional
    public PaymentViewDto pay(PaymentPayRequest request) {
        PurchaseOrder purchaseOrder = getPurchaseOrder(request.getId());
        List<InvoiceReceipt> invoiceReceipts = invoiceReceiptRepository.findAllByPurchaseOrder_IdOrderByInvoiceDateDescIdDesc(purchaseOrder.getId());
        List<PurchaseOrderPayment> payments = purchaseOrderPaymentRepository.findAllByPurchaseOrder_IdOrderByPaymentDateDescIdDesc(purchaseOrder.getId());
        validatePayable(purchaseOrder, invoiceReceipts, payments);

        BigDecimal totalInvoicedAmount = totalInvoicedAmount(invoiceReceipts);
        BigDecimal totalPaidAmount = totalPaidAmount(payments);
        BigDecimal balanceAmount = totalInvoicedAmount.subtract(totalPaidAmount);
        if (request.getPaidAmount().compareTo(balanceAmount) > 0) {
            throw new BadRequestException("Paid amount exceeds payment balance");
        }

        PurchaseOrderPayment payment = new PurchaseOrderPayment();
        payment.setPurchaseOrder(purchaseOrder);
        payment.setPaymentReferenceNo(trim(request.getPaymentReferenceNo()));
        payment.setPaymentMethod(hasText(request.getPaymentMethod())
                ? request.getPaymentMethod().trim().toUpperCase(Locale.ROOT)
                : PAYMENT_METHOD_CHEQUE);
        payment.setChequeNo(trim(request.getChequeNo()));
        payment.setPaymentDate(request.getPaymentDate());
        payment.setPaidAmount(request.getPaidAmount());
        payment.setPaymentRemark(trim(request.getPaymentRemark()));
        payment.setPaidBy(request.getUsername());
        applyAudit(payment, request.getUsername());
        purchaseOrderPaymentRepository.save(payment);

        purchaseOrder.setLastModifiedDate(LocalDateTime.now());
        purchaseOrder.setLastModifiedBy(request.getUsername());
        purchaseOrderRepository.save(purchaseOrder);

        List<PurchaseOrderPayment> updatedPayments = purchaseOrderPaymentRepository.findAllByPurchaseOrder_IdOrderByPaymentDateDescIdDesc(purchaseOrder.getId());
        return toViewDto(purchaseOrder, invoiceReceipts, updatedPayments);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentHistoryDto history(PaymentHistoryRequest request) {
        PurchaseOrder purchaseOrder = getPurchaseOrder(request.getId());
        List<InvoiceReceipt> invoiceReceipts = invoiceReceiptRepository.findAllByPurchaseOrder_IdOrderByInvoiceDateDescIdDesc(purchaseOrder.getId());
        List<PurchaseOrderPayment> payments = purchaseOrderPaymentRepository.findAllByPurchaseOrder_IdOrderByPaymentDateDescIdDesc(purchaseOrder.getId());
        validateVisible(purchaseOrder, invoiceReceipts);
        return PaymentHistoryDto.builder()
                .id(purchaseOrder.getId())
                .poNo(purchaseOrder.getPoNo())
                .entries(payments.stream().map(this::toHistoryEntryDto).toList())
                .build();
    }

    private Specification<PurchaseOrder> buildSpecification(PaymentFilterSearch search) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(root.get("matchStatus").in(STATUS_MATCHED, STATUS_PARTIALLY_MATCHED));

            if (search != null) {
                if (hasText(search.getPoNo())) {
                    predicates.add(cb.like(cb.lower(root.get("poNo")), like(search.getPoNo())));
                }
                if (hasText(search.getRequestNo())) {
                    predicates.add(cb.like(cb.lower(root.get("requestNo")), like(search.getRequestNo())));
                }
                if (hasText(search.getCompanyCode())) {
                    predicates.add(cb.like(cb.lower(root.get("companyCode")), like(search.getCompanyCode())));
                }
                if (hasText(search.getVendorCode())) {
                    predicates.add(cb.like(cb.lower(root.get("vendorCode")), like(search.getVendorCode())));
                }
                if (hasText(search.getVendorName())) {
                    predicates.add(cb.like(cb.lower(root.get("vendorName")), like(search.getVendorName())));
                }
                if (hasText(search.getMatchedBy())) {
                    predicates.add(cb.like(cb.lower(root.get("matchedBy")), like(search.getMatchedBy())));
                }
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private Map<Long, List<InvoiceReceipt>> loadInvoiceReceipts(List<PurchaseOrder> purchaseOrders) {
        if (purchaseOrders.isEmpty()) {
            return Map.of();
        }
        return invoiceReceiptRepository.findAllByPurchaseOrder_IdInOrderByPurchaseOrder_IdAscInvoiceDateDescIdDesc(
                        purchaseOrders.stream().map(PurchaseOrder::getId).toList())
                .stream()
                .collect(Collectors.groupingBy(receipt -> receipt.getPurchaseOrder().getId(), LinkedHashMap::new, Collectors.toList()));
    }

    private Map<Long, List<PurchaseOrderPayment>> loadPayments(List<PurchaseOrder> purchaseOrders) {
        if (purchaseOrders.isEmpty()) {
            return Map.of();
        }
        return purchaseOrderPaymentRepository.findAllByPurchaseOrder_IdInOrderByPurchaseOrder_IdAscPaymentDateDescIdDesc(
                        purchaseOrders.stream().map(PurchaseOrder::getId).toList())
                .stream()
                .collect(Collectors.groupingBy(payment -> payment.getPurchaseOrder().getId(), LinkedHashMap::new, Collectors.toList()));
    }

    private boolean isVisibleForPayment(PurchaseOrder purchaseOrder, List<InvoiceReceipt> invoiceReceipts) {
        return hasPayableMatch(purchaseOrder) && !invoiceReceipts.isEmpty();
    }

    private boolean matchesStatus(PurchaseOrder purchaseOrder, List<InvoiceReceipt> invoiceReceipts, List<PurchaseOrderPayment> payments, PaymentFilterSearch search) {
        if (search == null || !hasText(search.getStatus())) {
            return true;
        }
        return deriveStatus(purchaseOrder, invoiceReceipts, payments).equalsIgnoreCase(search.getStatus().trim());
    }

    private boolean matchesChequeNo(List<PurchaseOrderPayment> payments, PaymentFilterSearch search) {
        if (search == null || !hasText(search.getChequeNo())) {
            return true;
        }
        String expected = search.getChequeNo().trim().toLowerCase(Locale.ROOT);
        return payments.stream()
                .map(PurchaseOrderPayment::getChequeNo)
                .filter(this::hasText)
                .map(value -> value.toLowerCase(Locale.ROOT))
                .anyMatch(value -> value.contains(expected));
    }

    private PurchaseOrder getPurchaseOrder(Long id) {
        return purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase order not found with ID: " + id));
    }

    private void validateVisible(PurchaseOrder purchaseOrder, List<InvoiceReceipt> invoiceReceipts) {
        if (!isVisibleForPayment(purchaseOrder, invoiceReceipts)) {
            throw new BadRequestException("Payment is not available for this purchase order");
        }
    }

    private void validatePayable(PurchaseOrder purchaseOrder, List<InvoiceReceipt> invoiceReceipts, List<PurchaseOrderPayment> payments) {
        validateVisible(purchaseOrder, invoiceReceipts);
        BigDecimal balanceAmount = totalInvoicedAmount(invoiceReceipts).subtract(totalPaidAmount(payments));
        if (balanceAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Payment is already completed for this purchase order");
        }
    }

    private PaymentListItemDto toListItemDto(PurchaseOrder purchaseOrder, List<InvoiceReceipt> invoiceReceipts, List<PurchaseOrderPayment> payments) {
        PurchaseOrderPayment latestPayment = payments.isEmpty() ? null : payments.get(0);
        BigDecimal totalInvoicedAmount = totalInvoicedAmount(invoiceReceipts);
        BigDecimal totalPaidAmount = totalPaidAmount(payments);
        BigDecimal balanceAmount = totalInvoicedAmount.subtract(totalPaidAmount);
        String derivedStatus = deriveStatus(purchaseOrder, invoiceReceipts, payments);

        return PaymentListItemDto.builder()
                .id(purchaseOrder.getId())
                .poNo(purchaseOrder.getPoNo())
                .requestNo(purchaseOrder.getRequestNo())
                .companyCode(purchaseOrder.getCompanyCode())
                .companyName(purchaseOrder.getCompanyName())
                .vendorCode(purchaseOrder.getVendorCode())
                .vendorName(purchaseOrder.getVendorName())
                .status(derivedStatus)
                .statusDescription(toStatusDescription(derivedStatus))
                .matchStatus(purchaseOrder.getMatchStatus())
                .matchStatusDescription(toStatusDescription(purchaseOrder.getMatchStatus()))
                .totalAmount(purchaseOrder.getTotalAmount())
                .totalInvoicedAmount(totalInvoicedAmount)
                .totalPaidAmount(totalPaidAmount)
                .balanceAmount(balanceAmount)
                .lastChequeNo(latestPayment != null ? latestPayment.getChequeNo() : null)
                .lastPaymentDate(latestPayment != null ? latestPayment.getPaymentDate() : null)
                .createdDate(purchaseOrder.getCreatedDate())
                .lastModifiedDate(purchaseOrder.getLastModifiedDate())
                .createdBy(purchaseOrder.getCreatedBy())
                .lastModifiedBy(purchaseOrder.getLastModifiedBy())
                .build();
    }

    private PaymentViewDto toViewDto(PurchaseOrder purchaseOrder, List<InvoiceReceipt> invoiceReceipts, List<PurchaseOrderPayment> payments) {
        PurchaseOrderPayment latestPayment = payments.isEmpty() ? null : payments.get(0);
        BigDecimal totalInvoicedAmount = totalInvoicedAmount(invoiceReceipts);
        BigDecimal totalPaidAmount = totalPaidAmount(payments);
        BigDecimal balanceAmount = totalInvoicedAmount.subtract(totalPaidAmount);
        Map<Long, BigDecimal> invoicedQuantities = calculateInvoicedQuantities(invoiceReceipts);
        String derivedStatus = deriveStatus(purchaseOrder, invoiceReceipts, payments);

        return PaymentViewDto.builder()
                .id(purchaseOrder.getId())
                .poNo(purchaseOrder.getPoNo())
                .requestNo(purchaseOrder.getRequestNo())
                .companyCode(purchaseOrder.getCompanyCode())
                .companyName(purchaseOrder.getCompanyName())
                .vendorCode(purchaseOrder.getVendorCode())
                .vendorName(purchaseOrder.getVendorName())
                .currencyCode(purchaseOrder.getCurrencyCode())
                .department(purchaseOrder.getDepartment())
                .costCenter(purchaseOrder.getCostCenter())
                .status(derivedStatus)
                .statusDescription(toStatusDescription(derivedStatus))
                .matchStatus(purchaseOrder.getMatchStatus())
                .matchStatusDescription(toStatusDescription(purchaseOrder.getMatchStatus()))
                .totalAmount(purchaseOrder.getTotalAmount())
                .totalInvoicedAmount(totalInvoicedAmount)
                .totalPaidAmount(totalPaidAmount)
                .balanceAmount(balanceAmount)
                .lastPaymentDate(latestPayment != null ? latestPayment.getPaymentDate() : null)
                .lastChequeNo(latestPayment != null ? latestPayment.getChequeNo() : null)
                .lastPaymentMethod(latestPayment != null ? latestPayment.getPaymentMethod() : null)
                .lastPaymentReferenceNo(latestPayment != null ? latestPayment.getPaymentReferenceNo() : null)
                .lastPaymentRemark(latestPayment != null ? latestPayment.getPaymentRemark() : null)
                .createdDate(purchaseOrder.getCreatedDate())
                .lastModifiedDate(purchaseOrder.getLastModifiedDate())
                .createdBy(purchaseOrder.getCreatedBy())
                .lastModifiedBy(purchaseOrder.getLastModifiedBy())
                .items(purchaseOrder.getItems().stream()
                        .map(item -> toItemBalanceDto(item, invoicedQuantities.getOrDefault(item.getId(), BigDecimal.ZERO)))
                        .toList())
                .build();
    }

    private PaymentItemBalanceDto toItemBalanceDto(PurchaseOrderItem item, BigDecimal invoicedQuantity) {
        return PaymentItemBalanceDto.builder()
                .purchaseOrderItemId(item.getId())
                .itemCode(item.getItemCode())
                .itemDescription(item.getItemDescription())
                .uom(item.getUom())
                .orderedQuantity(item.getQuantity())
                .approvedQuantity(item.getApprovedQuantity() != null ? item.getApprovedQuantity() : item.getQuantity())
                .receivedQuantity(invoicedQuantity)
                .invoicedQuantity(invoicedQuantity)
                .unitPrice(item.getUnitPrice())
                .lineAmount(item.getLineAmount())
                .build();
    }

    private PaymentHistoryEntryDto toHistoryEntryDto(PurchaseOrderPayment payment) {
        return PaymentHistoryEntryDto.builder()
                .id(payment.getId())
                .paymentReferenceNo(payment.getPaymentReferenceNo())
                .paymentMethod(payment.getPaymentMethod())
                .chequeNo(payment.getChequeNo())
                .paymentDate(payment.getPaymentDate())
                .paidAmount(payment.getPaidAmount())
                .paymentRemark(payment.getPaymentRemark())
                .paidBy(payment.getPaidBy())
                .createdDate(payment.getCreatedDate())
                .build();
    }

    private Map<Long, BigDecimal> calculateInvoicedQuantities(List<InvoiceReceipt> receipts) {
        Map<Long, BigDecimal> totals = new LinkedHashMap<>();
        receipts.forEach(receipt -> receipt.getItems().forEach(item ->
                totals.merge(item.getPurchaseOrderItem().getId(), item.getInvoicedQuantity(), BigDecimal::add)
        ));
        return totals;
    }

    private BigDecimal totalInvoicedAmount(List<InvoiceReceipt> receipts) {
        return receipts.stream()
                .map(InvoiceReceipt::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal totalPaidAmount(List<PurchaseOrderPayment> payments) {
        return payments.stream()
                .map(PurchaseOrderPayment::getPaidAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private boolean hasPayableMatch(PurchaseOrder purchaseOrder) {
        return STATUS_MATCHED.equalsIgnoreCase(trim(purchaseOrder.getMatchStatus()))
                || STATUS_PARTIALLY_MATCHED.equalsIgnoreCase(trim(purchaseOrder.getMatchStatus()));
    }

    private String deriveStatus(PurchaseOrder purchaseOrder, List<InvoiceReceipt> invoiceReceipts, List<PurchaseOrderPayment> payments) {
        BigDecimal totalPaidAmount = totalPaidAmount(payments);
        if (totalPaidAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return trim(purchaseOrder.getMatchStatus());
        }
        BigDecimal totalInvoicedAmount = totalInvoicedAmount(invoiceReceipts);
        return totalPaidAmount.compareTo(totalInvoicedAmount) >= 0 ? STATUS_PAID : STATUS_PARTIALLY_PAID;
    }

    private Comparator<PaymentListItemDto> resolveComparator(String sortColumn, String sortDirection) {
        Comparator<PaymentListItemDto> comparator = switch (sortColumn) {
            case "poNo" -> Comparator.comparing(PaymentListItemDto::getPoNo, Comparator.nullsLast(String::compareToIgnoreCase));
            case "requestNo" -> Comparator.comparing(PaymentListItemDto::getRequestNo, Comparator.nullsLast(String::compareToIgnoreCase));
            case "companyCode" -> Comparator.comparing(PaymentListItemDto::getCompanyCode, Comparator.nullsLast(String::compareToIgnoreCase));
            case "vendorCode" -> Comparator.comparing(PaymentListItemDto::getVendorCode, Comparator.nullsLast(String::compareToIgnoreCase));
            case "vendorName" -> Comparator.comparing(PaymentListItemDto::getVendorName, Comparator.nullsLast(String::compareToIgnoreCase));
            case "status" -> Comparator.comparing(PaymentListItemDto::getStatus, Comparator.nullsLast(String::compareToIgnoreCase));
            case "matchStatus" -> Comparator.comparing(PaymentListItemDto::getMatchStatus, Comparator.nullsLast(String::compareToIgnoreCase));
            case "totalAmount" -> Comparator.comparing(PaymentListItemDto::getTotalAmount, Comparator.nullsLast(BigDecimal::compareTo));
            case "totalInvoicedAmount" -> Comparator.comparing(PaymentListItemDto::getTotalInvoicedAmount, Comparator.nullsLast(BigDecimal::compareTo));
            case "totalPaidAmount" -> Comparator.comparing(PaymentListItemDto::getTotalPaidAmount, Comparator.nullsLast(BigDecimal::compareTo));
            case "balanceAmount" -> Comparator.comparing(PaymentListItemDto::getBalanceAmount, Comparator.nullsLast(BigDecimal::compareTo));
            case "lastPaymentDate" -> Comparator.comparing(PaymentListItemDto::getLastPaymentDate, Comparator.nullsLast(Comparator.naturalOrder()));
            case "createdDate" -> Comparator.comparing(PaymentListItemDto::getCreatedDate, Comparator.nullsLast(Comparator.naturalOrder()));
            case "lastModifiedDate" -> Comparator.comparing(PaymentListItemDto::getLastModifiedDate, Comparator.nullsLast(Comparator.naturalOrder()));
            default -> Comparator.comparing(PaymentListItemDto::getLastModifiedDate, Comparator.nullsLast(Comparator.naturalOrder()));
        };

        return "ASC".equalsIgnoreCase(sortDirection) ? comparator : comparator.reversed();
    }

    private ReferenceOptionDto option(String code, String description) {
        return ReferenceOptionDto.builder().code(code).description(description).build();
    }

    private String toStatusDescription(String status) {
        if (!hasText(status)) {
            return status;
        }
        return switch (status) {
            case STATUS_MATCHED -> "Matched";
            case STATUS_PARTIALLY_MATCHED -> "Partially Matched";
            case STATUS_PARTIALLY_PAID -> "Partially Paid";
            case STATUS_PAID -> "Paid";
            default -> status;
        };
    }

    private void applyAudit(PurchaseOrderPayment payment, String username) {
        LocalDateTime now = LocalDateTime.now();
        payment.setCreatedDate(now);
        payment.setCreatedBy(username);
        payment.setLastModifiedDate(now);
        payment.setLastModifiedBy(username);
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String like(String value) {
        return "%" + value.trim().toLowerCase(Locale.ROOT) + "%";
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}
