package com.zynolo_nexus.po_service.service.impl;

import com.zynolo_nexus.po_service.dto.request.ThreeWayMatchExecuteRequest;
import com.zynolo_nexus.po_service.dto.request.ThreeWayMatchFilterRequest;
import com.zynolo_nexus.po_service.dto.request.ThreeWayMatchFilterSearch;
import com.zynolo_nexus.po_service.dto.request.ThreeWayMatchReferenceDataRequest;
import com.zynolo_nexus.po_service.dto.request.ThreeWayMatchViewRequest;
import com.zynolo_nexus.po_service.dto.response.ReferenceOptionDto;
import com.zynolo_nexus.po_service.dto.response.ThreeWayMatchFilterResultDto;
import com.zynolo_nexus.po_service.dto.response.ThreeWayMatchLineDto;
import com.zynolo_nexus.po_service.dto.response.ThreeWayMatchListItemDto;
import com.zynolo_nexus.po_service.dto.response.ThreeWayMatchPrivilegesDto;
import com.zynolo_nexus.po_service.dto.response.ThreeWayMatchReferenceDataDto;
import com.zynolo_nexus.po_service.dto.response.ThreeWayMatchViewDto;
import com.zynolo_nexus.po_service.enums.MasterStatus;
import com.zynolo_nexus.po_service.enums.PurchaseOrderStatus;
import com.zynolo_nexus.po_service.exception.BadRequestException;
import com.zynolo_nexus.po_service.exception.ResourceNotFoundException;
import com.zynolo_nexus.po_service.model.GoodsReceipt;
import com.zynolo_nexus.po_service.model.GoodsReceiptItem;
import com.zynolo_nexus.po_service.model.InvoiceReceipt;
import com.zynolo_nexus.po_service.model.InvoiceReceiptItem;
import com.zynolo_nexus.po_service.model.PurchaseOrder;
import com.zynolo_nexus.po_service.model.PurchaseOrderItem;
import com.zynolo_nexus.po_service.repository.CompanyRepository;
import com.zynolo_nexus.po_service.repository.GoodsReceiptRepository;
import com.zynolo_nexus.po_service.repository.InvoiceReceiptRepository;
import com.zynolo_nexus.po_service.repository.PurchaseOrderRepository;
import com.zynolo_nexus.po_service.repository.VendorRepository;
import com.zynolo_nexus.po_service.service.ThreeWayMatchService;
import com.zynolo_nexus.po_service.service.support.PagePrivilegeResolver;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
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
public class ThreeWayMatchServiceImpl implements ThreeWayMatchService {

    private static final String PAGE_CODE = "POTM";
    private static final String PENDING_MATCH = "PENDING_MATCH";
    private static final String MATCHED = "MATCHED";
    private static final String PARTIALLY_MATCHED = "PARTIALLY_MATCHED";
    private static final String MISMATCHED = "MISMATCHED";

    private final CompanyRepository companyRepository;
    private final VendorRepository vendorRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final GoodsReceiptRepository goodsReceiptRepository;
    private final InvoiceReceiptRepository invoiceReceiptRepository;
    private final PagePrivilegeResolver pagePrivilegeResolver;

    @Override
    @Transactional(readOnly = true)
    public ThreeWayMatchReferenceDataDto getReferenceData(ThreeWayMatchReferenceDataRequest request) {
        var privileges = pagePrivilegeResolver.resolve(request != null ? request.getUsername() : null, PAGE_CODE);

        return ThreeWayMatchReferenceDataDto.builder()
                .companies(companyRepository.findAllByStatusOrderByCodeAsc(MasterStatus.ACTIVE).stream()
                        .map(company -> option(company.getCode(), company.getDescription()))
                        .toList())
                .vendors(vendorRepository.findAllByStatusOrderByCodeAsc("ACTIVE").stream()
                        .map(vendor -> option(vendor.getCode(), vendor.getDescription()))
                        .toList())
                .defaultStatus(List.of(
                        option(PurchaseOrderStatus.PARTIALLY_RECEIVED.name(), "Partially Received"),
                        option(PurchaseOrderStatus.RECEIVED.name(), "Received"),
                        option("PARTIALLY_INVOICED", "Partially Invoiced"),
                        option("INVOICED", "Invoiced")
                ))
                .matchStatuses(List.of(
                        option(PENDING_MATCH, "Pending Match"),
                        option(MATCHED, "Matched"),
                        option(PARTIALLY_MATCHED, "Partially Matched"),
                        option(MISMATCHED, "Mismatched")
                ))
                .privileges(ThreeWayMatchPrivilegesDto.builder()
                        .view(privileges.isView())
                        .search(privileges.isSearch())
                        .match(privileges.isMatch())
                        .build())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ThreeWayMatchFilterResultDto filterList(ThreeWayMatchFilterRequest request) {
        List<PurchaseOrder> purchaseOrders = purchaseOrderRepository.findAll(buildSpecification(request.getSearch()));
        Map<Long, List<GoodsReceipt>> goodsReceipts = loadGoodsReceipts(purchaseOrders);
        Map<Long, List<InvoiceReceipt>> invoiceReceipts = loadInvoiceReceipts(purchaseOrders);

        List<ThreeWayMatchListItemDto> filtered = purchaseOrders.stream()
                .filter(po -> !invoiceReceipts.getOrDefault(po.getId(), List.of()).isEmpty())
                .filter(po -> matchesStatus(po, goodsReceipts.getOrDefault(po.getId(), List.of()), invoiceReceipts.getOrDefault(po.getId(), List.of()), request.getSearch()))
                .filter(po -> matchesMatchStatus(po, goodsReceipts.getOrDefault(po.getId(), List.of()), invoiceReceipts.getOrDefault(po.getId(), List.of()), request.getSearch()))
                .filter(po -> matchesInvoiceNo(invoiceReceipts.getOrDefault(po.getId(), List.of()), request.getSearch()))
                .map(po -> toListItemDto(po, goodsReceipts.getOrDefault(po.getId(), List.of()), invoiceReceipts.getOrDefault(po.getId(), List.of())))
                .sorted(resolveComparator(request.getSortColumn(), request.getSortDirection()))
                .toList();

        int fromIndex = Math.min(request.getPage() * request.getSize(), filtered.size());
        int toIndex = Math.min(fromIndex + request.getSize(), filtered.size());
        List<ThreeWayMatchListItemDto> content = filtered.subList(fromIndex, toIndex);

        return ThreeWayMatchFilterResultDto.builder()
                .content(content)
                .size(content.size())
                .totalRecords(filtered.size())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ThreeWayMatchViewDto view(ThreeWayMatchViewRequest request) {
        PurchaseOrder purchaseOrder = getPurchaseOrder(request.getId());
        MatchContext context = buildContext(purchaseOrder);
        validateVisible(context);
        return toViewDto(purchaseOrder, context);
    }

    @Override
    @Transactional
    public ThreeWayMatchViewDto match(ThreeWayMatchExecuteRequest request) {
        PurchaseOrder purchaseOrder = getPurchaseOrder(request.getId());
        MatchContext context = buildContext(purchaseOrder);
        validateVisible(context);
        List<ThreeWayMatchLineDto> lines = computeLineMatches(purchaseOrder, context.goodsReceipts(), context.invoiceReceipts());

        purchaseOrder.setMatchStatus(effectiveMatchStatus(purchaseOrder, lines));
        purchaseOrder.setMatchedDate(LocalDateTime.now());
        purchaseOrder.setMatchedBy(request.getUsername());
        purchaseOrder.setMatchRemark(trim(request.getMatchRemark()));
        purchaseOrder.setLastModifiedDate(LocalDateTime.now());
        purchaseOrder.setLastModifiedBy(request.getUsername());
        purchaseOrderRepository.save(purchaseOrder);

        return toViewDto(purchaseOrder, context);
    }

    private Specification<PurchaseOrder> buildSpecification(ThreeWayMatchFilterSearch search) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(root.get("status").in(PurchaseOrderStatus.PARTIALLY_RECEIVED, PurchaseOrderStatus.RECEIVED));

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
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private MatchContext buildContext(PurchaseOrder purchaseOrder) {
        List<GoodsReceipt> goodsReceipts = goodsReceiptRepository.findAllByPurchaseOrder_IdOrderByReceiptDateDescIdDesc(purchaseOrder.getId());
        List<InvoiceReceipt> invoiceReceipts = invoiceReceiptRepository.findAllByPurchaseOrder_IdOrderByInvoiceDateDescIdDesc(purchaseOrder.getId());
        return new MatchContext(goodsReceipts, invoiceReceipts);
    }

    private void validateVisible(MatchContext context) {
        if (context.invoiceReceipts().isEmpty()) {
            throw new BadRequestException("Three-way match is not available until invoice receipts exist");
        }
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

    private boolean matchesStatus(PurchaseOrder purchaseOrder, List<GoodsReceipt> goodsReceipts, List<InvoiceReceipt> invoiceReceipts, ThreeWayMatchFilterSearch search) {
        if (search == null || !hasText(search.getStatus())) {
            return true;
        }
        return deriveLifecycleStatus(purchaseOrder, goodsReceipts, invoiceReceipts).equalsIgnoreCase(search.getStatus().trim());
    }

    private boolean matchesMatchStatus(PurchaseOrder purchaseOrder, List<GoodsReceipt> goodsReceipts, List<InvoiceReceipt> invoiceReceipts, ThreeWayMatchFilterSearch search) {
        if (search == null || !hasText(search.getMatchStatus())) {
            return true;
        }
        return effectiveMatchStatus(purchaseOrder, computeLineMatches(purchaseOrder, goodsReceipts, invoiceReceipts)).equalsIgnoreCase(search.getMatchStatus().trim());
    }

    private boolean matchesInvoiceNo(List<InvoiceReceipt> invoiceReceipts, ThreeWayMatchFilterSearch search) {
        if (search == null || !hasText(search.getInvoiceNo())) {
            return true;
        }
        String expected = search.getInvoiceNo().trim().toLowerCase(Locale.ROOT);
        return invoiceReceipts.stream()
                .map(InvoiceReceipt::getInvoiceNo)
                .filter(this::hasText)
                .map(value -> value.toLowerCase(Locale.ROOT))
                .anyMatch(value -> value.contains(expected));
    }

    private PurchaseOrder getPurchaseOrder(Long id) {
        return purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase order not found with ID: " + id));
    }

    private ThreeWayMatchListItemDto toListItemDto(PurchaseOrder purchaseOrder, List<GoodsReceipt> goodsReceipts, List<InvoiceReceipt> invoiceReceipts) {
        List<ThreeWayMatchLineDto> lines = computeLineMatches(purchaseOrder, goodsReceipts, invoiceReceipts);
        String matchStatus = effectiveMatchStatus(purchaseOrder, lines);
        InvoiceReceipt latestInvoice = invoiceReceipts.isEmpty() ? null : invoiceReceipts.get(0);

        return ThreeWayMatchListItemDto.builder()
                .id(purchaseOrder.getId())
                .poNo(purchaseOrder.getPoNo())
                .requestNo(purchaseOrder.getRequestNo())
                .companyCode(purchaseOrder.getCompanyCode())
                .companyName(purchaseOrder.getCompanyName())
                .vendorCode(purchaseOrder.getVendorCode())
                .vendorName(purchaseOrder.getVendorName())
                .status(deriveLifecycleStatus(purchaseOrder, goodsReceipts, invoiceReceipts))
                .statusDescription(toLifecycleStatusDescription(deriveLifecycleStatus(purchaseOrder, goodsReceipts, invoiceReceipts)))
                .matchStatus(matchStatus)
                .matchStatusDescription(toMatchStatusDescription(matchStatus))
                .totalAmount(purchaseOrder.getTotalAmount())
                .lastInvoiceNo(latestInvoice != null ? latestInvoice.getInvoiceNo() : null)
                .lastInvoiceDate(latestInvoice != null ? latestInvoice.getInvoiceDate() : null)
                .createdDate(purchaseOrder.getCreatedDate())
                .lastModifiedDate(purchaseOrder.getLastModifiedDate())
                .createdBy(purchaseOrder.getCreatedBy())
                .lastModifiedBy(purchaseOrder.getLastModifiedBy())
                .build();
    }

    private ThreeWayMatchViewDto toViewDto(PurchaseOrder purchaseOrder, MatchContext context) {
        List<ThreeWayMatchLineDto> lines = computeLineMatches(purchaseOrder, context.goodsReceipts(), context.invoiceReceipts());
        String matchStatus = effectiveMatchStatus(purchaseOrder, lines);
        InvoiceReceipt latestInvoice = context.invoiceReceipts().isEmpty() ? null : context.invoiceReceipts().get(0);

        return ThreeWayMatchViewDto.builder()
                .id(purchaseOrder.getId())
                .poNo(purchaseOrder.getPoNo())
                .requestNo(purchaseOrder.getRequestNo())
                .companyCode(purchaseOrder.getCompanyCode())
                .companyName(purchaseOrder.getCompanyName())
                .vendorCode(purchaseOrder.getVendorCode())
                .vendorName(purchaseOrder.getVendorName())
                .currencyCode(purchaseOrder.getCurrencyCode())
                .status(deriveLifecycleStatus(purchaseOrder, context.goodsReceipts(), context.invoiceReceipts()))
                .statusDescription(toLifecycleStatusDescription(deriveLifecycleStatus(purchaseOrder, context.goodsReceipts(), context.invoiceReceipts())))
                .matchStatus(matchStatus)
                .matchStatusDescription(toMatchStatusDescription(matchStatus))
                .totalAmount(purchaseOrder.getTotalAmount())
                .lastInvoiceNo(latestInvoice != null ? latestInvoice.getInvoiceNo() : null)
                .lastInvoiceDate(latestInvoice != null ? latestInvoice.getInvoiceDate() : null)
                .matchedDate(purchaseOrder.getMatchedDate())
                .matchedBy(purchaseOrder.getMatchedBy())
                .matchRemark(purchaseOrder.getMatchRemark())
                .createdDate(purchaseOrder.getCreatedDate())
                .lastModifiedDate(purchaseOrder.getLastModifiedDate())
                .createdBy(purchaseOrder.getCreatedBy())
                .lastModifiedBy(purchaseOrder.getLastModifiedBy())
                .items(lines)
                .build();
    }

    private List<ThreeWayMatchLineDto> computeLineMatches(PurchaseOrder purchaseOrder, List<GoodsReceipt> goodsReceipts, List<InvoiceReceipt> invoiceReceipts) {
        Map<Long, BigDecimal> receivedQuantities = new LinkedHashMap<>();
        goodsReceipts.forEach(receipt -> receipt.getItems().forEach(item ->
                receivedQuantities.merge(item.getPurchaseOrderItem().getId(), item.getReceivedQuantity(), BigDecimal::add)
        ));

        Map<Long, BigDecimal> invoicedQuantities = new LinkedHashMap<>();
        Map<Long, BigDecimal> invoicedAmounts = new LinkedHashMap<>();
        invoiceReceipts.forEach(receipt -> receipt.getItems().forEach(item -> {
            invoicedQuantities.merge(item.getPurchaseOrderItem().getId(), item.getInvoicedQuantity(), BigDecimal::add);
            invoicedAmounts.merge(item.getPurchaseOrderItem().getId(), item.getLineAmount(), BigDecimal::add);
        }));

        return purchaseOrder.getItems().stream()
                .map(item -> {
                    BigDecimal receivedQuantity = receivedQuantities.getOrDefault(item.getId(), BigDecimal.ZERO);
                    BigDecimal invoicedQuantity = invoicedQuantities.getOrDefault(item.getId(), BigDecimal.ZERO);
                    BigDecimal invoiceAmount = invoicedAmounts.getOrDefault(item.getId(), BigDecimal.ZERO);
                    BigDecimal invoiceUnitPrice = invoicedQuantity.compareTo(BigDecimal.ZERO) > 0
                            ? invoiceAmount.divide(invoicedQuantity, 2, RoundingMode.HALF_UP)
                            : BigDecimal.ZERO;
                    String lineStatus = resolveLineStatus(receivedQuantity, invoicedQuantity, item.getUnitPrice(), invoiceUnitPrice);

                    return ThreeWayMatchLineDto.builder()
                            .purchaseOrderItemId(item.getId())
                            .itemCode(item.getItemCode())
                            .itemDescription(item.getItemDescription())
                            .uom(item.getUom())
                            .orderedQuantity(item.getQuantity())
                            .approvedQuantity(effectiveApprovedQuantity(item))
                            .receivedQuantity(receivedQuantity)
                            .invoicedQuantity(invoicedQuantity)
                            .poUnitPrice(item.getUnitPrice())
                            .invoiceUnitPrice(invoiceUnitPrice)
                            .poLineAmount(item.getLineAmount())
                            .invoiceLineAmount(invoiceAmount)
                            .lineMatchStatus(lineStatus)
                            .lineMatchStatusDescription(toMatchStatusDescription(lineStatus))
                            .build();
                })
                .toList();
    }

    private String resolveLineStatus(BigDecimal receivedQuantity, BigDecimal invoicedQuantity, BigDecimal poUnitPrice, BigDecimal invoiceUnitPrice) {
        if (receivedQuantity.compareTo(BigDecimal.ZERO) == 0 || invoicedQuantity.compareTo(BigDecimal.ZERO) == 0) {
            return PENDING_MATCH;
        }
        boolean quantityMatched = receivedQuantity.compareTo(invoicedQuantity) == 0;
        boolean priceMatched = poUnitPrice.compareTo(invoiceUnitPrice) == 0;
        if (quantityMatched && priceMatched) {
            return MATCHED;
        }
        if (invoicedQuantity.compareTo(receivedQuantity) < 0 && priceMatched) {
            return PARTIALLY_MATCHED;
        }
        return MISMATCHED;
    }

    private String effectiveMatchStatus(PurchaseOrder purchaseOrder, List<ThreeWayMatchLineDto> lines) {
        if (hasText(purchaseOrder.getMatchStatus())) {
            return purchaseOrder.getMatchStatus();
        }
        if (lines.isEmpty()) {
            return PENDING_MATCH;
        }
        boolean anyMismatch = lines.stream().anyMatch(line -> MISMATCHED.equals(line.getLineMatchStatus()));
        if (anyMismatch) {
            return MISMATCHED;
        }
        boolean allMatched = lines.stream().allMatch(line -> MATCHED.equals(line.getLineMatchStatus()));
        if (allMatched) {
            return MATCHED;
        }
        boolean anyMatched = lines.stream().anyMatch(line -> MATCHED.equals(line.getLineMatchStatus()) || PARTIALLY_MATCHED.equals(line.getLineMatchStatus()));
        return anyMatched ? PARTIALLY_MATCHED : PENDING_MATCH;
    }

    private String deriveLifecycleStatus(PurchaseOrder purchaseOrder, List<GoodsReceipt> goodsReceipts, List<InvoiceReceipt> invoiceReceipts) {
        if (invoiceReceipts.isEmpty()) {
            return purchaseOrder.getStatus().name();
        }
        Map<Long, BigDecimal> receivedQuantities = new LinkedHashMap<>();
        goodsReceipts.forEach(receipt -> receipt.getItems().forEach(item ->
                receivedQuantities.merge(item.getPurchaseOrderItem().getId(), item.getReceivedQuantity(), BigDecimal::add)
        ));
        Map<Long, BigDecimal> invoicedQuantities = new LinkedHashMap<>();
        invoiceReceipts.forEach(receipt -> receipt.getItems().forEach(item ->
                invoicedQuantities.merge(item.getPurchaseOrderItem().getId(), item.getInvoicedQuantity(), BigDecimal::add)
        ));

        boolean fullyInvoiced = purchaseOrder.getStatus() == PurchaseOrderStatus.RECEIVED
                && purchaseOrder.getItems().stream()
                .allMatch(item -> invoicedQuantities.getOrDefault(item.getId(), BigDecimal.ZERO)
                        .compareTo(receivedQuantities.getOrDefault(item.getId(), BigDecimal.ZERO)) >= 0);

        return fullyInvoiced ? "INVOICED" : "PARTIALLY_INVOICED";
    }

    private Comparator<ThreeWayMatchListItemDto> resolveComparator(String sortColumn, String sortDirection) {
        Comparator<ThreeWayMatchListItemDto> comparator = switch (sortColumn) {
            case "poNo" -> Comparator.comparing(ThreeWayMatchListItemDto::getPoNo, Comparator.nullsLast(String::compareToIgnoreCase));
            case "requestNo" -> Comparator.comparing(ThreeWayMatchListItemDto::getRequestNo, Comparator.nullsLast(String::compareToIgnoreCase));
            case "companyCode" -> Comparator.comparing(ThreeWayMatchListItemDto::getCompanyCode, Comparator.nullsLast(String::compareToIgnoreCase));
            case "vendorCode" -> Comparator.comparing(ThreeWayMatchListItemDto::getVendorCode, Comparator.nullsLast(String::compareToIgnoreCase));
            case "vendorName" -> Comparator.comparing(ThreeWayMatchListItemDto::getVendorName, Comparator.nullsLast(String::compareToIgnoreCase));
            case "status" -> Comparator.comparing(ThreeWayMatchListItemDto::getStatus, Comparator.nullsLast(String::compareToIgnoreCase));
            case "matchStatus" -> Comparator.comparing(ThreeWayMatchListItemDto::getMatchStatus, Comparator.nullsLast(String::compareToIgnoreCase));
            case "totalAmount" -> Comparator.comparing(ThreeWayMatchListItemDto::getTotalAmount, Comparator.nullsLast(BigDecimal::compareTo));
            case "lastInvoiceDate" -> Comparator.comparing(ThreeWayMatchListItemDto::getLastInvoiceDate, Comparator.nullsLast(Comparator.naturalOrder()));
            case "createdDate" -> Comparator.comparing(ThreeWayMatchListItemDto::getCreatedDate, Comparator.nullsLast(Comparator.naturalOrder()));
            case "lastModifiedDate" -> Comparator.comparing(ThreeWayMatchListItemDto::getLastModifiedDate, Comparator.nullsLast(Comparator.naturalOrder()));
            default -> Comparator.comparing(ThreeWayMatchListItemDto::getLastModifiedDate, Comparator.nullsLast(Comparator.naturalOrder()));
        };
        return "ASC".equalsIgnoreCase(sortDirection) ? comparator : comparator.reversed();
    }

    private ReferenceOptionDto option(String code, String description) {
        return ReferenceOptionDto.builder().code(code).description(description).build();
    }

    private String toLifecycleStatusDescription(String status) {
        return switch (status) {
            case "PARTIALLY_RECEIVED" -> "Partially Received";
            case "RECEIVED" -> "Received";
            case "PARTIALLY_INVOICED" -> "Partially Invoiced";
            case "INVOICED" -> "Invoiced";
            default -> status;
        };
    }

    private String toMatchStatusDescription(String status) {
        return switch (status) {
            case PENDING_MATCH -> "Pending Match";
            case MATCHED -> "Matched";
            case PARTIALLY_MATCHED -> "Partially Matched";
            case MISMATCHED -> "Mismatched";
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

    private BigDecimal effectiveApprovedQuantity(PurchaseOrderItem item) {
        return item.getApprovedQuantity() != null ? item.getApprovedQuantity() : item.getQuantity();
    }

    private record MatchContext(List<GoodsReceipt> goodsReceipts, List<InvoiceReceipt> invoiceReceipts) {
    }
}
