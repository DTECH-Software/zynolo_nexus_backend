package com.zynolo_nexus.po_service.service.impl;

import com.zynolo_nexus.po_service.dto.request.ClosureCloseRequest;
import com.zynolo_nexus.po_service.dto.request.ClosureFilterRequest;
import com.zynolo_nexus.po_service.dto.request.ClosureFilterSearch;
import com.zynolo_nexus.po_service.dto.request.ClosureReferenceDataRequest;
import com.zynolo_nexus.po_service.dto.request.ClosureViewRequest;
import com.zynolo_nexus.po_service.dto.response.ClosureFilterResultDto;
import com.zynolo_nexus.po_service.dto.response.ClosureItemDto;
import com.zynolo_nexus.po_service.dto.response.ClosureListItemDto;
import com.zynolo_nexus.po_service.dto.response.ClosurePrivilegesDto;
import com.zynolo_nexus.po_service.dto.response.ClosureReferenceDataDto;
import com.zynolo_nexus.po_service.dto.response.ClosureViewDto;
import com.zynolo_nexus.po_service.dto.response.ReferenceOptionDto;
import com.zynolo_nexus.po_service.enums.MasterStatus;
import com.zynolo_nexus.po_service.enums.PurchaseOrderStatus;
import com.zynolo_nexus.po_service.exception.BadRequestException;
import com.zynolo_nexus.po_service.exception.ResourceNotFoundException;
import com.zynolo_nexus.po_service.model.GoodsReceipt;
import com.zynolo_nexus.po_service.model.InvoiceReceipt;
import com.zynolo_nexus.po_service.model.PurchaseOrder;
import com.zynolo_nexus.po_service.model.PurchaseOrderItem;
import com.zynolo_nexus.po_service.model.PurchaseOrderPayment;
import com.zynolo_nexus.po_service.repository.CompanyRepository;
import com.zynolo_nexus.po_service.repository.GoodsReceiptRepository;
import com.zynolo_nexus.po_service.repository.InvoiceReceiptRepository;
import com.zynolo_nexus.po_service.repository.PurchaseOrderPaymentRepository;
import com.zynolo_nexus.po_service.repository.PurchaseOrderRepository;
import com.zynolo_nexus.po_service.repository.VendorRepository;
import com.zynolo_nexus.po_service.service.PurchaseOrderClosureService;
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
public class PurchaseOrderClosureServiceImpl implements PurchaseOrderClosureService {

    private static final String PAGE_CODE = "POCL";
    private static final String PENDING_MATCH = "PENDING_MATCH";
    private static final String MATCHED = "MATCHED";
    private static final String PARTIALLY_MATCHED = "PARTIALLY_MATCHED";
    private static final String MISMATCHED = "MISMATCHED";

    private final CompanyRepository companyRepository;
    private final VendorRepository vendorRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final GoodsReceiptRepository goodsReceiptRepository;
    private final InvoiceReceiptRepository invoiceReceiptRepository;
    private final PurchaseOrderPaymentRepository purchaseOrderPaymentRepository;
    private final PagePrivilegeResolver pagePrivilegeResolver;

    @Override
    @Transactional(readOnly = true)
    public ClosureReferenceDataDto getReferenceData(ClosureReferenceDataRequest request) {
        var privileges = pagePrivilegeResolver.resolve(request != null ? request.getUsername() : null, PAGE_CODE);

        return ClosureReferenceDataDto.builder()
                .companies(companyRepository.findAllByStatusOrderByCodeAsc(MasterStatus.ACTIVE).stream()
                        .map(company -> option(company.getCode(), company.getDescription()))
                        .toList())
                .vendors(vendorRepository.findAllByStatusOrderByCodeAsc("ACTIVE").stream()
                        .map(vendor -> option(vendor.getCode(), vendor.getDescription()))
                        .toList())
                .defaultStatus(List.of(
                        option(PurchaseOrderStatus.RECEIVED.name(), "Received"),
                        option(PurchaseOrderStatus.CLOSED.name(), "Closed")
                ))
                .privileges(ClosurePrivilegesDto.builder()
                        .view(privileges.isView())
                        .search(privileges.isSearch())
                        .close(privileges.isClose())
                        .build())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ClosureFilterResultDto filterList(ClosureFilterRequest request) {
        List<PurchaseOrder> purchaseOrders = purchaseOrderRepository.findAll(buildSpecification(request.getSearch()));
        Map<Long, List<GoodsReceipt>> goodsReceipts = loadGoodsReceipts(purchaseOrders);
        Map<Long, List<InvoiceReceipt>> invoiceReceipts = loadInvoiceReceipts(purchaseOrders);
        Map<Long, List<PurchaseOrderPayment>> payments = loadPayments(purchaseOrders);

        List<ClosureListItemDto> filtered = purchaseOrders.stream()
                .filter(po -> matchesDerivedStatuses(
                        po,
                        invoiceReceipts.getOrDefault(po.getId(), List.of()),
                        request.getSearch()))
                .map(po -> toListItemDto(
                        po,
                        goodsReceipts.getOrDefault(po.getId(), List.of()),
                        invoiceReceipts.getOrDefault(po.getId(), List.of()),
                        payments.getOrDefault(po.getId(), List.of())))
                .sorted(resolveComparator(request.getSortColumn(), request.getSortDirection()))
                .toList();

        int fromIndex = Math.min(request.getPage() * request.getSize(), filtered.size());
        int toIndex = Math.min(fromIndex + request.getSize(), filtered.size());
        List<ClosureListItemDto> content = filtered.subList(fromIndex, toIndex);

        return ClosureFilterResultDto.builder()
                .content(content)
                .size(content.size())
                .totalRecords(filtered.size())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ClosureViewDto view(ClosureViewRequest request) {
        PurchaseOrder purchaseOrder = getPurchaseOrder(request.getId());
        return toViewDto(purchaseOrder, loadContext(purchaseOrder));
    }

    @Override
    @Transactional
    public ClosureViewDto close(ClosureCloseRequest request) {
        PurchaseOrder purchaseOrder = getPurchaseOrder(request.getId());
        ClosureContext context = loadContext(purchaseOrder);
        String reason = closureReason(purchaseOrder, context);
        if (reason != null) {
            throw new BadRequestException(reason);
        }

        purchaseOrder.setStatus(PurchaseOrderStatus.CLOSED);
        purchaseOrder.setClosedDate(LocalDateTime.now());
        purchaseOrder.setClosedBy(request.getUsername());
        purchaseOrder.setCloseRemark(trim(request.getCloseRemark()));
        purchaseOrder.setLastModifiedDate(LocalDateTime.now());
        purchaseOrder.setLastModifiedBy(request.getUsername());
        purchaseOrderRepository.save(purchaseOrder);

        return toViewDto(purchaseOrder, context);
    }

    private Specification<PurchaseOrder> buildSpecification(ClosureFilterSearch search) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

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
                if (hasText(search.getCreatedBy())) {
                    predicates.add(cb.like(cb.lower(root.get("createdBy")), like(search.getCreatedBy())));
                }
                if (hasText(search.getStatus())) {
                    predicates.add(cb.equal(root.get("status"), parseStatus(search.getStatus())));
                } else {
                    predicates.add(root.get("status").in(PurchaseOrderStatus.RECEIVED, PurchaseOrderStatus.CLOSED));
                }
            } else {
                predicates.add(root.get("status").in(PurchaseOrderStatus.RECEIVED, PurchaseOrderStatus.CLOSED));
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private Map<Long, List<GoodsReceipt>> loadGoodsReceipts(List<PurchaseOrder> purchaseOrders) {
        if (purchaseOrders.isEmpty()) {
            return Map.of();
        }
        return goodsReceiptRepository.findAllByPurchaseOrder_IdInOrderByPurchaseOrder_IdAscReceiptDateDescIdDesc(
                        purchaseOrders.stream().map(PurchaseOrder::getId).toList())
                .stream()
                .collect(Collectors.groupingBy(receipt -> receipt.getPurchaseOrder().getId(), LinkedHashMap::new, Collectors.toList()));
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

    private boolean matchesDerivedStatuses(PurchaseOrder purchaseOrder,
                                           List<InvoiceReceipt> invoiceReceipts,
                                           ClosureFilterSearch search) {
        if (search == null) {
            return true;
        }
        if (hasText(search.getMatchStatus()) && !deriveMatchStatus(purchaseOrder, invoiceReceipts)
                .equalsIgnoreCase(search.getMatchStatus().trim())) {
            return false;
        }
        return true;
    }

    private PurchaseOrder getPurchaseOrder(Long id) {
        return purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase order not found with ID: " + id));
    }

    private ClosureContext loadContext(PurchaseOrder purchaseOrder) {
        return new ClosureContext(
                goodsReceiptRepository.findAllByPurchaseOrder_IdOrderByReceiptDateDescIdDesc(purchaseOrder.getId()),
                invoiceReceiptRepository.findAllByPurchaseOrder_IdOrderByInvoiceDateDescIdDesc(purchaseOrder.getId()),
                purchaseOrderPaymentRepository.findAllByPurchaseOrder_IdOrderByPaymentDateDescIdDesc(purchaseOrder.getId())
        );
    }

    private ClosureListItemDto toListItemDto(PurchaseOrder purchaseOrder,
                                             List<GoodsReceipt> goodsReceipts,
                                             List<InvoiceReceipt> invoiceReceipts,
                                             List<PurchaseOrderPayment> payments) {
        BigDecimal totalInvoicedAmount = sum(invoiceReceipts, InvoiceReceipt::getTotalAmount);
        BigDecimal totalPaidAmount = sum(payments, PurchaseOrderPayment::getPaidAmount);

        return ClosureListItemDto.builder()
                .id(purchaseOrder.getId())
                .poNo(purchaseOrder.getPoNo())
                .requestNo(purchaseOrder.getRequestNo())
                .companyCode(purchaseOrder.getCompanyCode())
                .companyName(purchaseOrder.getCompanyName())
                .vendorCode(purchaseOrder.getVendorCode())
                .vendorName(purchaseOrder.getVendorName())
                .status(purchaseOrder.getStatus().name())
                .statusDescription(toPurchaseOrderStatusDescription(purchaseOrder.getStatus()))
                .matchStatus(deriveMatchStatus(purchaseOrder, invoiceReceipts))
                .matchStatusDescription(toMatchStatusDescription(deriveMatchStatus(purchaseOrder, invoiceReceipts)))
                .totalAmount(purchaseOrder.getTotalAmount())
                .totalInvoicedAmount(totalInvoicedAmount)
                .totalPaidAmount(totalPaidAmount)
                .balanceAmount(max(BigDecimal.ZERO, totalInvoicedAmount.subtract(totalPaidAmount)))
                .closedDate(purchaseOrder.getClosedDate())
                .createdDate(purchaseOrder.getCreatedDate())
                .lastModifiedDate(purchaseOrder.getLastModifiedDate())
                .createdBy(purchaseOrder.getCreatedBy())
                .lastModifiedBy(purchaseOrder.getLastModifiedBy())
                .build();
    }

    private ClosureViewDto toViewDto(PurchaseOrder purchaseOrder, ClosureContext context) {
        Map<Long, BigDecimal> receivedQuantities = aggregateReceivedQuantities(context.goodsReceipts());
        Map<Long, BigDecimal> invoicedQuantities = aggregateInvoicedQuantities(context.invoiceReceipts());
        BigDecimal totalInvoicedAmount = sum(context.invoiceReceipts(), InvoiceReceipt::getTotalAmount);
        BigDecimal totalPaidAmount = sum(context.payments(), PurchaseOrderPayment::getPaidAmount);
        String reason = closureReason(purchaseOrder, context);

        return ClosureViewDto.builder()
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
                .status(purchaseOrder.getStatus().name())
                .statusDescription(toPurchaseOrderStatusDescription(purchaseOrder.getStatus()))
                .matchStatus(deriveMatchStatus(purchaseOrder, context.invoiceReceipts()))
                .matchStatusDescription(toMatchStatusDescription(deriveMatchStatus(purchaseOrder, context.invoiceReceipts())))
                .totalAmount(purchaseOrder.getTotalAmount())
                .totalInvoicedAmount(totalInvoicedAmount)
                .totalPaidAmount(totalPaidAmount)
                .balanceAmount(max(BigDecimal.ZERO, totalInvoicedAmount.subtract(totalPaidAmount)))
                .closable(reason == null)
                .closureReason(reason)
                .closedDate(purchaseOrder.getClosedDate())
                .closedBy(purchaseOrder.getClosedBy())
                .closeRemark(purchaseOrder.getCloseRemark())
                .createdDate(purchaseOrder.getCreatedDate())
                .lastModifiedDate(purchaseOrder.getLastModifiedDate())
                .createdBy(purchaseOrder.getCreatedBy())
                .lastModifiedBy(purchaseOrder.getLastModifiedBy())
                .items(purchaseOrder.getItems().stream()
                        .map(item -> ClosureItemDto.builder()
                                .purchaseOrderItemId(item.getId())
                                .itemCode(item.getItemCode())
                                .itemDescription(item.getItemDescription())
                                .uom(item.getUom())
                                .orderedQuantity(item.getQuantity())
                                .approvedQuantity(effectiveApprovedQuantity(item))
                                .receivedQuantity(receivedQuantities.getOrDefault(item.getId(), BigDecimal.ZERO))
                                .invoicedQuantity(invoicedQuantities.getOrDefault(item.getId(), BigDecimal.ZERO))
                                .unitPrice(item.getUnitPrice())
                                .lineAmount(item.getLineAmount())
                                .build())
                        .toList())
                .build();
    }

    private String closureReason(PurchaseOrder purchaseOrder, ClosureContext context) {
        if (purchaseOrder.getStatus() == PurchaseOrderStatus.CLOSED) {
            return "Purchase order is already closed";
        }
        if (purchaseOrder.getStatus() != PurchaseOrderStatus.RECEIVED) {
            return "Purchase order is not fully received";
        }
        if (context.invoiceReceipts().isEmpty()) {
            return "Purchase order has no invoice receipts";
        }
        if (!hasClosableMatch(deriveMatchStatus(purchaseOrder, context.invoiceReceipts()))) {
            return "Purchase order is not matched for closure";
        }
        if (!isFullyInvoicedAgainstReceived(purchaseOrder, context)) {
            return "Purchase order is not fully invoiced";
        }
        BigDecimal totalInvoicedAmount = sum(context.invoiceReceipts(), InvoiceReceipt::getTotalAmount);
        if (totalInvoicedAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return "Purchase order has no invoice amount";
        }
        BigDecimal totalPaidAmount = sum(context.payments(), PurchaseOrderPayment::getPaidAmount);
        if (totalPaidAmount.compareTo(totalInvoicedAmount) < 0) {
            return "Purchase order has outstanding payment balance";
        }
        return null;
    }

    private boolean isFullyInvoicedAgainstReceived(PurchaseOrder purchaseOrder, ClosureContext context) {
        Map<Long, BigDecimal> receivedQuantities = aggregateReceivedQuantities(context.goodsReceipts());
        Map<Long, BigDecimal> invoicedQuantities = aggregateInvoicedQuantities(context.invoiceReceipts());

        for (PurchaseOrderItem item : purchaseOrder.getItems()) {
            BigDecimal received = receivedQuantities.getOrDefault(item.getId(), BigDecimal.ZERO);
            BigDecimal invoiced = invoicedQuantities.getOrDefault(item.getId(), BigDecimal.ZERO);
            if (received.compareTo(invoiced) > 0) {
                return false;
            }
        }
        return true;
    }

    private Map<Long, BigDecimal> aggregateReceivedQuantities(List<GoodsReceipt> goodsReceipts) {
        Map<Long, BigDecimal> quantities = new LinkedHashMap<>();
        goodsReceipts.forEach(receipt -> receipt.getItems().forEach(item ->
                quantities.merge(item.getPurchaseOrderItem().getId(), nullSafe(item.getReceivedQuantity()), BigDecimal::add)));
        return quantities;
    }

    private Map<Long, BigDecimal> aggregateInvoicedQuantities(List<InvoiceReceipt> invoiceReceipts) {
        Map<Long, BigDecimal> quantities = new LinkedHashMap<>();
        invoiceReceipts.forEach(receipt -> receipt.getItems().forEach(item ->
                quantities.merge(item.getPurchaseOrderItem().getId(), nullSafe(item.getInvoicedQuantity()), BigDecimal::add)));
        return quantities;
    }

    private BigDecimal effectiveApprovedQuantity(PurchaseOrderItem item) {
        return item.getApprovedQuantity() != null ? item.getApprovedQuantity() : nullSafe(item.getQuantity());
    }

    private Comparator<ClosureListItemDto> resolveComparator(String sortColumn, String sortDirection) {
        Comparator<ClosureListItemDto> comparator = switch (sortColumn) {
            case "poNo" -> Comparator.comparing(ClosureListItemDto::getPoNo, Comparator.nullsLast(String::compareToIgnoreCase));
            case "requestNo" -> Comparator.comparing(ClosureListItemDto::getRequestNo, Comparator.nullsLast(String::compareToIgnoreCase));
            case "companyCode" -> Comparator.comparing(ClosureListItemDto::getCompanyCode, Comparator.nullsLast(String::compareToIgnoreCase));
            case "vendorCode" -> Comparator.comparing(ClosureListItemDto::getVendorCode, Comparator.nullsLast(String::compareToIgnoreCase));
            case "vendorName" -> Comparator.comparing(ClosureListItemDto::getVendorName, Comparator.nullsLast(String::compareToIgnoreCase));
            case "status" -> Comparator.comparing(ClosureListItemDto::getStatus, Comparator.nullsLast(String::compareToIgnoreCase));
            case "matchStatus" -> Comparator.comparing(ClosureListItemDto::getMatchStatus, Comparator.nullsLast(String::compareToIgnoreCase));
            case "totalAmount" -> Comparator.comparing(ClosureListItemDto::getTotalAmount, Comparator.nullsLast(BigDecimal::compareTo));
            case "totalInvoicedAmount" -> Comparator.comparing(ClosureListItemDto::getTotalInvoicedAmount, Comparator.nullsLast(BigDecimal::compareTo));
            case "totalPaidAmount" -> Comparator.comparing(ClosureListItemDto::getTotalPaidAmount, Comparator.nullsLast(BigDecimal::compareTo));
            case "closedDate" -> Comparator.comparing(ClosureListItemDto::getClosedDate, Comparator.nullsLast(Comparator.naturalOrder()));
            case "createdDate" -> Comparator.comparing(ClosureListItemDto::getCreatedDate, Comparator.nullsLast(Comparator.naturalOrder()));
            case "lastModifiedDate" -> Comparator.comparing(ClosureListItemDto::getLastModifiedDate, Comparator.nullsLast(Comparator.naturalOrder()));
            case "createdBy" -> Comparator.comparing(ClosureListItemDto::getCreatedBy, Comparator.nullsLast(String::compareToIgnoreCase));
            default -> Comparator.comparing(ClosureListItemDto::getLastModifiedDate, Comparator.nullsLast(Comparator.naturalOrder()));
        };
        return "ASC".equalsIgnoreCase(sortDirection) ? comparator : comparator.reversed();
    }

    private boolean hasClosableMatch(String matchStatus) {
        String normalized = trim(matchStatus);
        return MATCHED.equalsIgnoreCase(normalized) || PARTIALLY_MATCHED.equalsIgnoreCase(normalized);
    }

    private String deriveMatchStatus(PurchaseOrder purchaseOrder, List<InvoiceReceipt> invoiceReceipts) {
        if (invoiceReceipts.isEmpty()) {
            return PENDING_MATCH;
        }
        return hasText(purchaseOrder.getMatchStatus()) ? purchaseOrder.getMatchStatus() : PENDING_MATCH;
    }

    private PurchaseOrderStatus parseStatus(String status) {
        try {
            return PurchaseOrderStatus.valueOf(status.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Invalid purchase order status: " + status);
        }
    }

    private String toPurchaseOrderStatusDescription(PurchaseOrderStatus status) {
        return switch (status) {
            case DRAFT -> "Draft";
            case SENT -> "Sent";
            case VENDOR_CONFIRMED -> "Vendor Confirmed";
            case PARTIALLY_CONFIRMED -> "Partially Confirmed";
            case PARTIALLY_APPROVED -> "Partially Approved";
            case VENDOR_REJECTED -> "Vendor Rejected";
            case PARTIALLY_RECEIVED -> "Partially Received";
            case RECEIVED -> "Received";
            case CLOSED -> "Closed";
        };
    }

    private String toMatchStatusDescription(String status) {
        return switch (trim(status)) {
            case PENDING_MATCH -> "Pending Match";
            case MATCHED -> "Matched";
            case PARTIALLY_MATCHED -> "Partially Matched";
            case MISMATCHED -> "Mismatched";
            default -> status;
        };
    }

    private ReferenceOptionDto option(String code, String description) {
        return ReferenceOptionDto.builder().code(code).description(description).build();
    }

    private <T> BigDecimal sum(List<T> values, java.util.function.Function<T, BigDecimal> mapper) {
        if (values == null || values.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return values.stream()
                .map(mapper)
                .filter(value -> value != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal max(BigDecimal left, BigDecimal right) {
        return left.compareTo(right) >= 0 ? left : right;
    }

    private BigDecimal nullSafe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
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

    private record ClosureContext(List<GoodsReceipt> goodsReceipts,
                                  List<InvoiceReceipt> invoiceReceipts,
                                  List<PurchaseOrderPayment> payments) {
    }
}
