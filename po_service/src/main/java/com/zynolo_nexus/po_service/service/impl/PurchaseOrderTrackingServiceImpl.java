package com.zynolo_nexus.po_service.service.impl;

import com.zynolo_nexus.po_service.dto.request.TrackingExportRequest;
import com.zynolo_nexus.po_service.dto.request.TrackingFilterRequest;
import com.zynolo_nexus.po_service.dto.request.TrackingFilterSearch;
import com.zynolo_nexus.po_service.dto.request.TrackingReferenceDataRequest;
import com.zynolo_nexus.po_service.dto.request.TrackingTimelineRequest;
import com.zynolo_nexus.po_service.dto.request.TrackingViewRequest;
import com.zynolo_nexus.po_service.dto.response.ReferenceOptionDto;
import com.zynolo_nexus.po_service.dto.response.TrackingExportDto;
import com.zynolo_nexus.po_service.dto.response.TrackingFilterResultDto;
import com.zynolo_nexus.po_service.dto.response.TrackingItemDto;
import com.zynolo_nexus.po_service.dto.response.TrackingListItemDto;
import com.zynolo_nexus.po_service.dto.response.TrackingPrivilegesDto;
import com.zynolo_nexus.po_service.dto.response.TrackingReferenceDataDto;
import com.zynolo_nexus.po_service.dto.response.TrackingTimelineDto;
import com.zynolo_nexus.po_service.dto.response.TrackingTimelineEntryDto;
import com.zynolo_nexus.po_service.dto.response.TrackingViewDto;
import com.zynolo_nexus.po_service.enums.MasterStatus;
import com.zynolo_nexus.po_service.enums.PoRequestStatus;
import com.zynolo_nexus.po_service.enums.PurchaseOrderStatus;
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
import com.zynolo_nexus.po_service.service.PurchaseOrderTrackingService;
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
public class PurchaseOrderTrackingServiceImpl implements PurchaseOrderTrackingService {

    private static final String PAGE_CODE = "POTR";
    private static final String PENDING_MATCH = "PENDING_MATCH";
    private static final String MATCHED = "MATCHED";
    private static final String PARTIALLY_MATCHED = "PARTIALLY_MATCHED";
    private static final String MISMATCHED = "MISMATCHED";
    private static final String PARTIALLY_INVOICED = "PARTIALLY_INVOICED";
    private static final String INVOICED = "INVOICED";
    private static final String NOT_READY = "NOT_READY";
    private static final String PENDING_PAYMENT = "PENDING_PAYMENT";
    private static final String PARTIALLY_PAID = "PARTIALLY_PAID";
    private static final String PAID = "PAID";

    private final CompanyRepository companyRepository;
    private final VendorRepository vendorRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final GoodsReceiptRepository goodsReceiptRepository;
    private final InvoiceReceiptRepository invoiceReceiptRepository;
    private final PurchaseOrderPaymentRepository purchaseOrderPaymentRepository;
    private final PagePrivilegeResolver pagePrivilegeResolver;

    @Override
    @Transactional(readOnly = true)
    public TrackingReferenceDataDto getReferenceData(TrackingReferenceDataRequest request) {
        var privileges = pagePrivilegeResolver.resolve(request != null ? request.getUsername() : null, PAGE_CODE);

        return TrackingReferenceDataDto.builder()
                .companies(companyRepository.findAllByStatusOrderByCodeAsc(MasterStatus.ACTIVE).stream()
                        .map(company -> option(company.getCode(), company.getDescription()))
                        .toList())
                .vendors(vendorRepository.findAllByStatusOrderByCodeAsc("ACTIVE").stream()
                        .map(vendor -> option(vendor.getCode(), vendor.getDescription()))
                        .toList())
                .poStatuses(List.of(
                        option(PurchaseOrderStatus.DRAFT.name(), "Draft"),
                        option(PurchaseOrderStatus.SENT.name(), "Sent"),
                        option(PurchaseOrderStatus.VENDOR_CONFIRMED.name(), "Vendor Confirmed"),
                        option(PurchaseOrderStatus.PARTIALLY_CONFIRMED.name(), "Partially Confirmed"),
                        option(PurchaseOrderStatus.PARTIALLY_APPROVED.name(), "Partially Approved"),
                        option(PurchaseOrderStatus.VENDOR_REJECTED.name(), "Vendor Rejected"),
                        option(PurchaseOrderStatus.PARTIALLY_RECEIVED.name(), "Partially Received"),
                        option(PurchaseOrderStatus.RECEIVED.name(), "Received")
                ))
                .matchStatuses(List.of(
                        option(PENDING_MATCH, "Pending Match"),
                        option(MATCHED, "Matched"),
                        option(PARTIALLY_MATCHED, "Partially Matched"),
                        option(MISMATCHED, "Mismatched")
                ))
                .paymentStatuses(List.of(
                        option(NOT_READY, "Not Ready"),
                        option(PENDING_PAYMENT, "Pending Payment"),
                        option(PARTIALLY_PAID, "Partially Paid"),
                        option(PAID, "Paid")
                ))
                .privileges(TrackingPrivilegesDto.builder()
                        .view(privileges.isView())
                        .search(privileges.isSearch())
                        .export(privileges.isExport())
                        .build())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public TrackingFilterResultDto filterList(TrackingFilterRequest request) {
        List<TrackingListItemDto> filtered = filterTrackingItems(request.getSearch(), request.getSortColumn(), request.getSortDirection());
        int fromIndex = Math.min(request.getPage() * request.getSize(), filtered.size());
        int toIndex = Math.min(fromIndex + request.getSize(), filtered.size());
        List<TrackingListItemDto> content = filtered.subList(fromIndex, toIndex);

        return TrackingFilterResultDto.builder()
                .content(content)
                .size(content.size())
                .totalRecords(filtered.size())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public TrackingViewDto view(TrackingViewRequest request) {
        PurchaseOrder purchaseOrder = getPurchaseOrder(request.getId());
        return toViewDto(purchaseOrder, loadContext(purchaseOrder));
    }

    @Override
    @Transactional(readOnly = true)
    public TrackingTimelineDto timeline(TrackingTimelineRequest request) {
        PurchaseOrder purchaseOrder = getPurchaseOrder(request.getId());
        TrackingContext context = loadContext(purchaseOrder);
        List<TrackingTimelineEntryDto> entries = buildTimelineEntries(purchaseOrder, context).stream()
                .sorted(Comparator.comparing(TrackingTimelineEntryDto::getEventDate, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();

        return TrackingTimelineDto.builder()
                .id(purchaseOrder.getId())
                .poNo(purchaseOrder.getPoNo())
                .requestNo(purchaseOrder.getRequestNo())
                .entries(entries)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public TrackingExportDto export(TrackingExportRequest request) {
        List<TrackingListItemDto> filtered = filterTrackingItems(request.getSearch(), request.getSortColumn(), request.getSortDirection());
        return TrackingExportDto.builder()
                .content(filtered)
                .totalRecords(filtered.size())
                .build();
    }

    private List<TrackingListItemDto> filterTrackingItems(TrackingFilterSearch search, String sortColumn, String sortDirection) {
        List<PurchaseOrder> purchaseOrders = purchaseOrderRepository.findAll(buildSpecification(search));
        Map<Long, List<InvoiceReceipt>> invoiceReceipts = loadInvoiceReceipts(purchaseOrders);
        Map<Long, List<PurchaseOrderPayment>> payments = loadPayments(purchaseOrders);

        return purchaseOrders.stream()
                .filter(po -> matchesDerivedStatuses(
                        po,
                        invoiceReceipts.getOrDefault(po.getId(), List.of()),
                        payments.getOrDefault(po.getId(), List.of()),
                        search))
                .map(po -> toListItemDto(
                        po,
                        invoiceReceipts.getOrDefault(po.getId(), List.of()),
                        payments.getOrDefault(po.getId(), List.of())))
                .sorted(resolveComparator(sortColumn, sortDirection))
                .toList();
    }

    private Specification<PurchaseOrder> buildSpecification(TrackingFilterSearch search) {
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
                if (hasText(search.getRequestType())) {
                    predicates.add(cb.like(cb.lower(root.get("requestType")), like(search.getRequestType())));
                }
                if (hasText(search.getCreatedBy())) {
                    predicates.add(cb.like(cb.lower(root.get("createdBy")), like(search.getCreatedBy())));
                }
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private TrackingContext loadContext(PurchaseOrder purchaseOrder) {
        return new TrackingContext(
                goodsReceiptRepository.findAllByPurchaseOrder_IdOrderByReceiptDateDescIdDesc(purchaseOrder.getId()),
                invoiceReceiptRepository.findAllByPurchaseOrder_IdOrderByInvoiceDateDescIdDesc(purchaseOrder.getId()),
                purchaseOrderPaymentRepository.findAllByPurchaseOrder_IdOrderByPaymentDateDescIdDesc(purchaseOrder.getId())
        );
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
                                           List<PurchaseOrderPayment> payments,
                                           TrackingFilterSearch search) {
        if (search == null) {
            return true;
        }
        if (hasText(search.getPoStatus()) && !purchaseOrder.getStatus().name().equalsIgnoreCase(search.getPoStatus().trim())) {
            return false;
        }
        String matchStatus = deriveMatchStatus(purchaseOrder, invoiceReceipts);
        if (hasText(search.getMatchStatus()) && !matchStatus.equalsIgnoreCase(search.getMatchStatus().trim())) {
            return false;
        }
        String paymentStatus = derivePaymentStatus(purchaseOrder, invoiceReceipts, payments);
        return !hasText(search.getPaymentStatus()) || paymentStatus.equalsIgnoreCase(search.getPaymentStatus().trim());
    }

    private TrackingListItemDto toListItemDto(PurchaseOrder purchaseOrder,
                                              List<InvoiceReceipt> invoiceReceipts,
                                              List<PurchaseOrderPayment> payments) {
        String matchStatus = deriveMatchStatus(purchaseOrder, invoiceReceipts);
        String paymentStatus = derivePaymentStatus(purchaseOrder, invoiceReceipts, payments);

        return TrackingListItemDto.builder()
                .id(purchaseOrder.getId())
                .poNo(purchaseOrder.getPoNo())
                .requestNo(purchaseOrder.getRequestNo())
                .companyCode(purchaseOrder.getCompanyCode())
                .companyName(purchaseOrder.getCompanyName())
                .vendorCode(purchaseOrder.getVendorCode())
                .vendorName(purchaseOrder.getVendorName())
                .requestType(purchaseOrder.getRequestType())
                .poStatus(purchaseOrder.getStatus().name())
                .poStatusDescription(toPurchaseOrderStatusDescription(purchaseOrder.getStatus()))
                .matchStatus(matchStatus)
                .matchStatusDescription(toMatchStatusDescription(matchStatus))
                .paymentStatus(paymentStatus)
                .paymentStatusDescription(toPaymentStatusDescription(paymentStatus))
                .totalAmount(purchaseOrder.getTotalAmount())
                .requiredDate(purchaseOrder.getRequiredDate())
                .createdDate(purchaseOrder.getCreatedDate())
                .lastModifiedDate(purchaseOrder.getLastModifiedDate())
                .createdBy(purchaseOrder.getCreatedBy())
                .lastModifiedBy(purchaseOrder.getLastModifiedBy())
                .build();
    }

    private TrackingViewDto toViewDto(PurchaseOrder purchaseOrder, TrackingContext context) {
        InvoiceReceipt latestInvoice = context.invoiceReceipts().isEmpty() ? null : context.invoiceReceipts().get(0);
        PurchaseOrderPayment latestPayment = context.payments().isEmpty() ? null : context.payments().get(0);
        BigDecimal totalInvoicedAmount = sum(context.invoiceReceipts(), InvoiceReceipt::getTotalAmount);
        BigDecimal totalPaidAmount = sum(context.payments(), PurchaseOrderPayment::getPaidAmount);
        BigDecimal balanceAmount = max(BigDecimal.ZERO, totalInvoicedAmount.subtract(totalPaidAmount));
        String matchStatus = deriveMatchStatus(purchaseOrder, context.invoiceReceipts());
        String paymentStatus = derivePaymentStatus(purchaseOrder, context.invoiceReceipts(), context.payments());
        Map<Long, BigDecimal> receivedQuantities = aggregateReceivedQuantities(context.goodsReceipts());
        Map<Long, BigDecimal> invoicedQuantities = aggregateInvoicedQuantities(context.invoiceReceipts());

        return TrackingViewDto.builder()
                .id(purchaseOrder.getId())
                .requestId(purchaseOrder.getRequest().getId())
                .poNo(purchaseOrder.getPoNo())
                .requestNo(purchaseOrder.getRequestNo())
                .companyCode(purchaseOrder.getCompanyCode())
                .companyName(purchaseOrder.getCompanyName())
                .requestType(purchaseOrder.getRequestType())
                .requestStatus(purchaseOrder.getRequest().getStatus().name())
                .requestStatusDescription(toRequestStatusDescription(purchaseOrder.getRequest().getStatus()))
                .department(purchaseOrder.getDepartment())
                .costCenter(purchaseOrder.getCostCenter())
                .currencyCode(purchaseOrder.getCurrencyCode())
                .vendorCode(purchaseOrder.getVendorCode())
                .vendorName(purchaseOrder.getVendorName())
                .requiredDate(purchaseOrder.getRequiredDate())
                .justification(purchaseOrder.getJustification())
                .poStatus(purchaseOrder.getStatus().name())
                .poStatusDescription(toPurchaseOrderStatusDescription(purchaseOrder.getStatus()))
                .matchStatus(matchStatus)
                .matchStatusDescription(toMatchStatusDescription(matchStatus))
                .paymentStatus(paymentStatus)
                .paymentStatusDescription(toPaymentStatusDescription(paymentStatus))
                .totalAmount(purchaseOrder.getTotalAmount())
                .totalInvoicedAmount(totalInvoicedAmount)
                .totalPaidAmount(totalPaidAmount)
                .balanceAmount(balanceAmount)
                .requestSubmittedDate(purchaseOrder.getRequest().getSubmittedDate())
                .requestReviewedDate(purchaseOrder.getRequest().getReviewedDate())
                .requestReviewedBy(purchaseOrder.getRequest().getReviewedBy())
                .requestReviewRemark(purchaseOrder.getRequest().getReviewRemark())
                .sentDate(purchaseOrder.getSentDate())
                .sentBy(purchaseOrder.getSentBy())
                .sendRemark(purchaseOrder.getSendRemark())
                .vendorConfirmationDate(purchaseOrder.getVendorConfirmationDate())
                .vendorConfirmationBy(purchaseOrder.getVendorConfirmationBy())
                .vendorReferenceNo(purchaseOrder.getVendorReferenceNo())
                .expectedDeliveryDate(purchaseOrder.getExpectedDeliveryDate())
                .vendorConfirmationRemark(purchaseOrder.getVendorConfirmationRemark())
                .matchedDate(purchaseOrder.getMatchedDate())
                .matchedBy(purchaseOrder.getMatchedBy())
                .matchRemark(purchaseOrder.getMatchRemark())
                .lastInvoiceDate(latestInvoice != null ? latestInvoice.getInvoiceDate() : null)
                .lastInvoiceNo(latestInvoice != null ? latestInvoice.getInvoiceNo() : null)
                .lastInvoiceRemark(latestInvoice != null ? latestInvoice.getInvoiceRemark() : null)
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
                        .map(item -> TrackingItemDto.builder()
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

    private List<TrackingTimelineEntryDto> buildTimelineEntries(PurchaseOrder purchaseOrder, TrackingContext context) {
        List<TrackingTimelineEntryDto> entries = new ArrayList<>();

        entries.add(timelineEntry("REQUEST_CREATED",
                purchaseOrder.getRequest().getStatus().name(),
                toRequestStatusDescription(purchaseOrder.getRequest().getStatus()),
                purchaseOrder.getRequest().getCreatedDate(),
                purchaseOrder.getRequest().getCreatedBy(),
                purchaseOrder.getRequestNo(),
                purchaseOrder.getJustification()));

        if (purchaseOrder.getRequest().getSubmittedDate() != null) {
            entries.add(timelineEntry("REQUEST_SUBMITTED",
                    PoRequestStatus.SUBMITTED.name(),
                    toRequestStatusDescription(PoRequestStatus.SUBMITTED),
                    purchaseOrder.getRequest().getSubmittedDate(),
                    purchaseOrder.getRequest().getLastModifiedBy(),
                    purchaseOrder.getRequestNo(),
                    null));
        }

        if (purchaseOrder.getRequest().getReviewedDate() != null) {
            entries.add(timelineEntry("REQUEST_REVIEWED",
                    purchaseOrder.getRequest().getStatus().name(),
                    toRequestStatusDescription(purchaseOrder.getRequest().getStatus()),
                    purchaseOrder.getRequest().getReviewedDate(),
                    purchaseOrder.getRequest().getReviewedBy(),
                    purchaseOrder.getRequestNo(),
                    purchaseOrder.getRequest().getReviewRemark()));
        }

        entries.add(timelineEntry("PO_CREATED",
                PurchaseOrderStatus.DRAFT.name(),
                toPurchaseOrderStatusDescription(PurchaseOrderStatus.DRAFT),
                purchaseOrder.getCreatedDate(),
                purchaseOrder.getCreatedBy(),
                purchaseOrder.getPoNo(),
                purchaseOrder.getJustification()));

        if (purchaseOrder.getSentDate() != null) {
            entries.add(timelineEntry("PO_SENT",
                    PurchaseOrderStatus.SENT.name(),
                    toPurchaseOrderStatusDescription(PurchaseOrderStatus.SENT),
                    purchaseOrder.getSentDate(),
                    purchaseOrder.getSentBy(),
                    purchaseOrder.getPoNo(),
                    purchaseOrder.getSendRemark()));
        }

        if (purchaseOrder.getVendorConfirmationDate() != null) {
            entries.add(timelineEntry("VENDOR_CONFIRMATION",
                    purchaseOrder.getStatus().name(),
                    toPurchaseOrderStatusDescription(purchaseOrder.getStatus()),
                    purchaseOrder.getVendorConfirmationDate(),
                    purchaseOrder.getVendorConfirmationBy(),
                    purchaseOrder.getVendorReferenceNo(),
                    purchaseOrder.getVendorConfirmationRemark()));
        }

        context.goodsReceipts().forEach(receipt -> entries.add(timelineEntry("GOODS_RECEIPT",
                purchaseOrder.getStatus().name(),
                toPurchaseOrderStatusDescription(purchaseOrder.getStatus()),
                receipt.getCreatedDate(),
                receipt.getReceivedBy(),
                receipt.getDeliveryNoteNo(),
                receipt.getReceiveRemark())));

        String invoiceStatus = deriveInvoiceStatus(purchaseOrder, context.invoiceReceipts());
        context.invoiceReceipts().forEach(receipt -> entries.add(timelineEntry("INVOICE_RECEIPT",
                invoiceStatus,
                toInvoiceStatusDescription(invoiceStatus),
                receipt.getCreatedDate(),
                receipt.getReceivedBy(),
                receipt.getInvoiceNo(),
                receipt.getInvoiceRemark())));

        if (purchaseOrder.getMatchedDate() != null) {
            String matchStatus = deriveMatchStatus(purchaseOrder, context.invoiceReceipts());
            entries.add(timelineEntry("THREE_WAY_MATCH",
                    matchStatus,
                    toMatchStatusDescription(matchStatus),
                    purchaseOrder.getMatchedDate(),
                    purchaseOrder.getMatchedBy(),
                    purchaseOrder.getPoNo(),
                    purchaseOrder.getMatchRemark()));
        }

        String paymentStatus = derivePaymentStatus(purchaseOrder, context.invoiceReceipts(), context.payments());
        context.payments().forEach(payment -> entries.add(timelineEntry("PAYMENT",
                paymentStatus,
                toPaymentStatusDescription(paymentStatus),
                payment.getCreatedDate(),
                payment.getPaidBy(),
                hasText(payment.getPaymentReferenceNo()) ? payment.getPaymentReferenceNo() : payment.getChequeNo(),
                payment.getPaymentRemark())));

        return entries;
    }

    private TrackingTimelineEntryDto timelineEntry(String stage,
                                                   String status,
                                                   String statusDescription,
                                                   LocalDateTime eventDate,
                                                   String performedBy,
                                                   String referenceNo,
                                                   String remark) {
        return TrackingTimelineEntryDto.builder()
                .stage(stage)
                .status(status)
                .statusDescription(statusDescription)
                .eventDate(eventDate)
                .performedBy(performedBy)
                .referenceNo(referenceNo)
                .remark(remark)
                .build();
    }

    private Comparator<TrackingListItemDto> resolveComparator(String sortColumn, String sortDirection) {
        Comparator<TrackingListItemDto> comparator = switch (sortColumn) {
            case "poNo" -> Comparator.comparing(TrackingListItemDto::getPoNo, Comparator.nullsLast(String::compareToIgnoreCase));
            case "requestNo" -> Comparator.comparing(TrackingListItemDto::getRequestNo, Comparator.nullsLast(String::compareToIgnoreCase));
            case "companyCode" -> Comparator.comparing(TrackingListItemDto::getCompanyCode, Comparator.nullsLast(String::compareToIgnoreCase));
            case "vendorCode" -> Comparator.comparing(TrackingListItemDto::getVendorCode, Comparator.nullsLast(String::compareToIgnoreCase));
            case "vendorName" -> Comparator.comparing(TrackingListItemDto::getVendorName, Comparator.nullsLast(String::compareToIgnoreCase));
            case "requestType" -> Comparator.comparing(TrackingListItemDto::getRequestType, Comparator.nullsLast(String::compareToIgnoreCase));
            case "poStatus" -> Comparator.comparing(TrackingListItemDto::getPoStatus, Comparator.nullsLast(String::compareToIgnoreCase));
            case "matchStatus" -> Comparator.comparing(TrackingListItemDto::getMatchStatus, Comparator.nullsLast(String::compareToIgnoreCase));
            case "paymentStatus" -> Comparator.comparing(TrackingListItemDto::getPaymentStatus, Comparator.nullsLast(String::compareToIgnoreCase));
            case "totalAmount" -> Comparator.comparing(TrackingListItemDto::getTotalAmount, Comparator.nullsLast(BigDecimal::compareTo));
            case "requiredDate" -> Comparator.comparing(TrackingListItemDto::getRequiredDate, Comparator.nullsLast(Comparator.naturalOrder()));
            case "createdDate" -> Comparator.comparing(TrackingListItemDto::getCreatedDate, Comparator.nullsLast(Comparator.naturalOrder()));
            case "lastModifiedDate" -> Comparator.comparing(TrackingListItemDto::getLastModifiedDate, Comparator.nullsLast(Comparator.naturalOrder()));
            case "createdBy" -> Comparator.comparing(TrackingListItemDto::getCreatedBy, Comparator.nullsLast(String::compareToIgnoreCase));
            default -> Comparator.comparing(TrackingListItemDto::getLastModifiedDate, Comparator.nullsLast(Comparator.naturalOrder()));
        };
        return "ASC".equalsIgnoreCase(sortDirection) ? comparator : comparator.reversed();
    }

    private PurchaseOrder getPurchaseOrder(Long id) {
        return purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase order not found with ID: " + id));
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
        String matchStatus = deriveMatchStatus(purchaseOrder, invoiceReceipts);
        if (invoiceReceipts.isEmpty() || !hasPayableMatch(matchStatus)) {
            return NOT_READY;
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

    private ReferenceOptionDto option(String code, String description) {
        return ReferenceOptionDto.builder().code(code).description(description).build();
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
        };
    }

    private String toRequestStatusDescription(PoRequestStatus status) {
        return switch (status) {
            case DRAFT -> "Draft";
            case SUBMITTED -> "Submitted";
            case APPROVED -> "Approved";
            case REJECTED -> "Rejected";
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

    private String toPaymentStatusDescription(String status) {
        return switch (trim(status)) {
            case NOT_READY -> "Not Ready";
            case PENDING_PAYMENT -> "Pending Payment";
            case PARTIALLY_PAID -> "Partially Paid";
            case PAID -> "Paid";
            default -> status;
        };
    }

    private String toInvoiceStatusDescription(String status) {
        return switch (trim(status)) {
            case PARTIALLY_INVOICED -> "Partially Invoiced";
            case INVOICED -> "Invoiced";
            default -> status;
        };
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

    private record TrackingContext(List<GoodsReceipt> goodsReceipts,
                                   List<InvoiceReceipt> invoiceReceipts,
                                   List<PurchaseOrderPayment> payments) {
    }
}
