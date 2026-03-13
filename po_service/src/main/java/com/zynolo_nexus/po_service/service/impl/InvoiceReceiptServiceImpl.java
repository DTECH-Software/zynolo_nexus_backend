package com.zynolo_nexus.po_service.service.impl;

import com.zynolo_nexus.po_service.dto.request.InvoiceReceiptFilterRequest;
import com.zynolo_nexus.po_service.dto.request.InvoiceReceiptFilterSearch;
import com.zynolo_nexus.po_service.dto.request.InvoiceReceiptHistoryRequest;
import com.zynolo_nexus.po_service.dto.request.InvoiceReceiptReceiveRequest;
import com.zynolo_nexus.po_service.dto.request.InvoiceReceiptReferenceDataRequest;
import com.zynolo_nexus.po_service.dto.request.InvoiceReceiptViewRequest;
import com.zynolo_nexus.po_service.dto.response.InvoiceReceiptFilterResultDto;
import com.zynolo_nexus.po_service.dto.response.InvoiceReceiptHistoryDto;
import com.zynolo_nexus.po_service.dto.response.InvoiceReceiptHistoryEntryDto;
import com.zynolo_nexus.po_service.dto.response.InvoiceReceiptHistoryLineDto;
import com.zynolo_nexus.po_service.dto.response.InvoiceReceiptItemBalanceDto;
import com.zynolo_nexus.po_service.dto.response.InvoiceReceiptListItemDto;
import com.zynolo_nexus.po_service.dto.response.InvoiceReceiptPrivilegesDto;
import com.zynolo_nexus.po_service.dto.response.InvoiceReceiptReferenceDataDto;
import com.zynolo_nexus.po_service.dto.response.InvoiceReceiptViewDto;
import com.zynolo_nexus.po_service.dto.response.ReferenceOptionDto;
import com.zynolo_nexus.po_service.enums.MasterStatus;
import com.zynolo_nexus.po_service.enums.PurchaseOrderStatus;
import com.zynolo_nexus.po_service.exception.BadRequestException;
import com.zynolo_nexus.po_service.exception.ResourceNotFoundException;
import com.zynolo_nexus.po_service.model.InvoiceReceipt;
import com.zynolo_nexus.po_service.model.InvoiceReceiptItem;
import com.zynolo_nexus.po_service.model.PurchaseOrder;
import com.zynolo_nexus.po_service.model.PurchaseOrderItem;
import com.zynolo_nexus.po_service.repository.CompanyRepository;
import com.zynolo_nexus.po_service.repository.CurrencyRepository;
import com.zynolo_nexus.po_service.repository.GoodsReceiptRepository;
import com.zynolo_nexus.po_service.repository.InvoiceReceiptRepository;
import com.zynolo_nexus.po_service.repository.PurchaseOrderRepository;
import com.zynolo_nexus.po_service.repository.VendorRepository;
import com.zynolo_nexus.po_service.service.InvoiceReceiptService;
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
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InvoiceReceiptServiceImpl implements InvoiceReceiptService {

    private static final String PAGE_CODE = "POIR";
    private static final String STATUS_PARTIALLY_INVOICED = "PARTIALLY_INVOICED";
    private static final String STATUS_INVOICED = "INVOICED";

    private final CompanyRepository companyRepository;
    private final VendorRepository vendorRepository;
    private final CurrencyRepository currencyRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final GoodsReceiptRepository goodsReceiptRepository;
    private final InvoiceReceiptRepository invoiceReceiptRepository;
    private final PagePrivilegeResolver pagePrivilegeResolver;

    @Override
    @Transactional(readOnly = true)
    public InvoiceReceiptReferenceDataDto getReferenceData(InvoiceReceiptReferenceDataRequest request) {
        var privileges = pagePrivilegeResolver.resolve(request != null ? request.getUsername() : null, PAGE_CODE);

        return InvoiceReceiptReferenceDataDto.builder()
                .companies(companyRepository.findAllByStatusOrderByCodeAsc(MasterStatus.ACTIVE).stream()
                        .map(company -> option(company.getCode(), company.getDescription()))
                        .toList())
                .vendors(vendorRepository.findAllByStatusOrderByCodeAsc("ACTIVE").stream()
                        .map(vendor -> option(vendor.getCode(), vendor.getDescription()))
                        .toList())
                .currencies(currencyRepository.findAllByStatusOrderByCodeAsc(MasterStatus.ACTIVE).stream()
                        .map(currency -> option(currency.getCode(), currency.getDescription()))
                        .toList())
                .defaultStatus(List.of(
                        option(PurchaseOrderStatus.PARTIALLY_RECEIVED.name(), "Partially Received"),
                        option(PurchaseOrderStatus.RECEIVED.name(), "Received"),
                        option(STATUS_PARTIALLY_INVOICED, "Partially Invoiced"),
                        option(STATUS_INVOICED, "Invoiced")
                ))
                .privileges(InvoiceReceiptPrivilegesDto.builder()
                        .view(privileges.isView())
                        .search(privileges.isSearch())
                        .receive(privileges.isReceive())
                        .build())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceReceiptFilterResultDto filterList(InvoiceReceiptFilterRequest request) {
        List<PurchaseOrder> purchaseOrders = purchaseOrderRepository.findAll(buildSpecification(request.getSearch()));
        Map<Long, List<InvoiceReceipt>> receiptsByPo = loadReceiptsByPo(purchaseOrders);

        List<InvoiceReceiptListItemDto> filtered = purchaseOrders.stream()
                .filter(purchaseOrder -> matchesStatus(purchaseOrder, receiptsByPo.getOrDefault(purchaseOrder.getId(), List.of()), request.getSearch()))
                .filter(purchaseOrder -> matchesInvoiceNo(receiptsByPo.getOrDefault(purchaseOrder.getId(), List.of()), request.getSearch()))
                .map(purchaseOrder -> toListItemDto(purchaseOrder, receiptsByPo.getOrDefault(purchaseOrder.getId(), List.of())))
                .sorted(resolveComparator(request.getSortColumn(), request.getSortDirection()))
                .toList();

        int fromIndex = Math.min(request.getPage() * request.getSize(), filtered.size());
        int toIndex = Math.min(fromIndex + request.getSize(), filtered.size());
        List<InvoiceReceiptListItemDto> content = filtered.subList(fromIndex, toIndex);

        return InvoiceReceiptFilterResultDto.builder()
                .content(content)
                .size(content.size())
                .totalRecords(filtered.size())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceReceiptViewDto view(InvoiceReceiptViewRequest request) {
        PurchaseOrder purchaseOrder = getPurchaseOrder(request.getId());
        validateVisible(purchaseOrder);
        return toViewDto(purchaseOrder);
    }

    @Override
    @Transactional
    public InvoiceReceiptViewDto receive(InvoiceReceiptReceiveRequest request) {
        PurchaseOrder purchaseOrder = getPurchaseOrder(request.getId());
        validateReceivable(purchaseOrder);

        String invoiceNo = trim(request.getInvoiceNo());
        if (invoiceReceiptRepository.existsByPurchaseOrder_IdAndInvoiceNoIgnoreCase(purchaseOrder.getId(), invoiceNo)) {
            throw new BadRequestException("Invoice already exists for purchase order: " + invoiceNo);
        }

        Map<Long, BigDecimal> receivedQuantities = calculateReceivedQuantities(purchaseOrder);
        Map<Long, BigDecimal> alreadyInvoiced = calculateInvoicedQuantities(
                invoiceReceiptRepository.findAllByPurchaseOrder_IdOrderByInvoiceDateDescIdDesc(purchaseOrder.getId())
        );
        Map<String, PurchaseOrderItem> itemMap = toItemMap(purchaseOrder);

        InvoiceReceipt invoiceReceipt = new InvoiceReceipt();
        invoiceReceipt.setPurchaseOrder(purchaseOrder);
        invoiceReceipt.setInvoiceNo(invoiceNo);
        invoiceReceipt.setInvoiceDate(request.getInvoiceDate());
        invoiceReceipt.setInvoiceRemark(trim(request.getInvoiceRemark()));
        invoiceReceipt.setReceivedBy(request.getUsername());
        applyAudit(invoiceReceipt, request.getUsername());

        BigDecimal totalAmount = BigDecimal.ZERO;
        Map<String, String> payloadItems = new LinkedHashMap<>();

        for (var itemRequest : request.getItems()) {
            String normalized = normalize(itemRequest.getItemCode());
            if (payloadItems.putIfAbsent(normalized, itemRequest.getItemCode()) != null) {
                throw new BadRequestException("Duplicate item code found in invoice payload: " + itemRequest.getItemCode());
            }

            PurchaseOrderItem purchaseOrderItem = itemMap.get(normalized);
            if (purchaseOrderItem == null) {
                throw new BadRequestException("Purchase order item not found for code: " + itemRequest.getItemCode());
            }

            BigDecimal receivedQuantity = receivedQuantities.getOrDefault(purchaseOrderItem.getId(), BigDecimal.ZERO);
            if (receivedQuantity.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BadRequestException("No received quantity available for item code: " + itemRequest.getItemCode());
            }

            BigDecimal balance = receivedQuantity.subtract(alreadyInvoiced.getOrDefault(purchaseOrderItem.getId(), BigDecimal.ZERO));
            if (itemRequest.getInvoicedQuantity().compareTo(balance) > 0) {
                throw new BadRequestException("Invoiced quantity exceeds balance for item code: " + itemRequest.getItemCode());
            }

            InvoiceReceiptItem invoiceReceiptItem = new InvoiceReceiptItem();
            invoiceReceiptItem.setPurchaseOrderItem(purchaseOrderItem);
            invoiceReceiptItem.setItemCode(purchaseOrderItem.getItemCode());
            invoiceReceiptItem.setItemDescription(purchaseOrderItem.getItemDescription());
            invoiceReceiptItem.setUom(purchaseOrderItem.getUom());
            invoiceReceiptItem.setOrderedQuantity(purchaseOrderItem.getQuantity());
            invoiceReceiptItem.setApprovedQuantity(effectiveApprovedQuantity(purchaseOrderItem));
            invoiceReceiptItem.setReceivedQuantity(receivedQuantity);
            invoiceReceiptItem.setInvoicedQuantity(itemRequest.getInvoicedQuantity());
            invoiceReceiptItem.setUnitPrice(itemRequest.getUnitPrice());
            invoiceReceiptItem.setLineAmount(itemRequest.getInvoicedQuantity().multiply(itemRequest.getUnitPrice()));
            invoiceReceipt.addItem(invoiceReceiptItem);

            totalAmount = totalAmount.add(invoiceReceiptItem.getLineAmount());
            alreadyInvoiced.merge(purchaseOrderItem.getId(), itemRequest.getInvoicedQuantity(), BigDecimal::add);
        }

        invoiceReceipt.setTotalAmount(totalAmount);
        invoiceReceiptRepository.save(invoiceReceipt);

        purchaseOrder.setLastModifiedDate(LocalDateTime.now());
        purchaseOrder.setLastModifiedBy(request.getUsername());
        purchaseOrderRepository.save(purchaseOrder);

        return toViewDto(purchaseOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceReceiptHistoryDto history(InvoiceReceiptHistoryRequest request) {
        PurchaseOrder purchaseOrder = getPurchaseOrder(request.getId());
        validateVisible(purchaseOrder);
        return toHistoryDto(
                purchaseOrder,
                invoiceReceiptRepository.findAllByPurchaseOrder_IdOrderByInvoiceDateDescIdDesc(purchaseOrder.getId())
        );
    }

    private Specification<PurchaseOrder> buildSpecification(InvoiceReceiptFilterSearch search) {
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
                if (hasText(search.getRequestType())) {
                    predicates.add(cb.like(cb.lower(root.get("requestType")), like(search.getRequestType())));
                }
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private Map<Long, List<InvoiceReceipt>> loadReceiptsByPo(List<PurchaseOrder> purchaseOrders) {
        if (purchaseOrders.isEmpty()) {
            return Map.of();
        }
        return invoiceReceiptRepository.findAllByPurchaseOrder_IdInOrderByPurchaseOrder_IdAscInvoiceDateDescIdDesc(
                        purchaseOrders.stream().map(PurchaseOrder::getId).toList())
                .stream()
                .collect(Collectors.groupingBy(receipt -> receipt.getPurchaseOrder().getId(), LinkedHashMap::new, Collectors.toList()));
    }

    private boolean matchesStatus(PurchaseOrder purchaseOrder, List<InvoiceReceipt> receipts, InvoiceReceiptFilterSearch search) {
        if (search == null || !hasText(search.getStatus())) {
            return true;
        }
        return deriveStatus(purchaseOrder, receipts).equalsIgnoreCase(search.getStatus().trim());
    }

    private boolean matchesInvoiceNo(List<InvoiceReceipt> receipts, InvoiceReceiptFilterSearch search) {
        if (search == null || !hasText(search.getInvoiceNo())) {
            return true;
        }
        String expected = search.getInvoiceNo().trim().toLowerCase(Locale.ROOT);
        return receipts.stream()
                .map(InvoiceReceipt::getInvoiceNo)
                .filter(this::hasText)
                .map(value -> value.toLowerCase(Locale.ROOT))
                .anyMatch(value -> value.contains(expected));
    }

    private PurchaseOrder getPurchaseOrder(Long id) {
        return purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase order not found with ID: " + id));
    }

    private void validateVisible(PurchaseOrder purchaseOrder) {
        if (!(purchaseOrder.getStatus() == PurchaseOrderStatus.PARTIALLY_RECEIVED
                || purchaseOrder.getStatus() == PurchaseOrderStatus.RECEIVED)) {
            throw new BadRequestException("Invoice receipt is not available for purchase order status: " + purchaseOrder.getStatus().name());
        }
    }

    private void validateReceivable(PurchaseOrder purchaseOrder) {
        validateVisible(purchaseOrder);
    }

    private Map<Long, BigDecimal> calculateReceivedQuantities(PurchaseOrder purchaseOrder) {
        Map<Long, BigDecimal> totals = new LinkedHashMap<>();
        goodsReceiptRepository.findAllByPurchaseOrder_IdOrderByReceiptDateDescIdDesc(purchaseOrder.getId())
                .forEach(receipt -> receipt.getItems().forEach(item ->
                        totals.merge(item.getPurchaseOrderItem().getId(), item.getReceivedQuantity(), BigDecimal::add)
                ));
        return totals;
    }

    private Map<Long, BigDecimal> calculateInvoicedQuantities(List<InvoiceReceipt> receipts) {
        Map<Long, BigDecimal> totals = new LinkedHashMap<>();
        receipts.forEach(receipt -> receipt.getItems().forEach(item ->
                totals.merge(item.getPurchaseOrderItem().getId(), item.getInvoicedQuantity(), BigDecimal::add)
        ));
        return totals;
    }

    private Map<String, PurchaseOrderItem> toItemMap(PurchaseOrder purchaseOrder) {
        return purchaseOrder.getItems().stream()
                .collect(Collectors.toMap(
                        item -> normalize(item.getItemCode()),
                        Function.identity(),
                        (first, second) -> {
                            throw new BadRequestException("Duplicate item code found in purchase order: " + first.getItemCode());
                        },
                        LinkedHashMap::new
                ));
    }

    private InvoiceReceiptViewDto toViewDto(PurchaseOrder purchaseOrder) {
        List<InvoiceReceipt> receipts = invoiceReceiptRepository.findAllByPurchaseOrder_IdOrderByInvoiceDateDescIdDesc(purchaseOrder.getId());
        Map<Long, BigDecimal> receivedQuantities = calculateReceivedQuantities(purchaseOrder);
        Map<Long, BigDecimal> invoicedQuantities = calculateInvoicedQuantities(receipts);
        InvoiceReceipt latestReceipt = receipts.isEmpty() ? null : receipts.get(0);
        String derivedStatus = deriveStatus(purchaseOrder, receipts);

        return InvoiceReceiptViewDto.builder()
                .id(purchaseOrder.getId())
                .poNo(purchaseOrder.getPoNo())
                .requestNo(purchaseOrder.getRequestNo())
                .companyCode(purchaseOrder.getCompanyCode())
                .companyName(purchaseOrder.getCompanyName())
                .requestType(purchaseOrder.getRequestType())
                .department(purchaseOrder.getDepartment())
                .costCenter(purchaseOrder.getCostCenter())
                .currencyCode(purchaseOrder.getCurrencyCode())
                .vendorCode(purchaseOrder.getVendorCode())
                .vendorName(purchaseOrder.getVendorName())
                .requiredDate(purchaseOrder.getRequiredDate())
                .justification(purchaseOrder.getJustification())
                .status(derivedStatus)
                .statusDescription(toStatusDescription(derivedStatus))
                .totalAmount(purchaseOrder.getTotalAmount())
                .lastInvoiceDate(latestReceipt != null ? latestReceipt.getInvoiceDate() : null)
                .lastInvoiceNo(latestReceipt != null ? latestReceipt.getInvoiceNo() : null)
                .lastInvoiceRemark(latestReceipt != null ? latestReceipt.getInvoiceRemark() : null)
                .createdDate(purchaseOrder.getCreatedDate())
                .lastModifiedDate(purchaseOrder.getLastModifiedDate())
                .createdBy(purchaseOrder.getCreatedBy())
                .lastModifiedBy(purchaseOrder.getLastModifiedBy())
                .items(purchaseOrder.getItems().stream()
                        .map(item -> toItemBalanceDto(
                                item,
                                receivedQuantities.getOrDefault(item.getId(), BigDecimal.ZERO),
                                invoicedQuantities.getOrDefault(item.getId(), BigDecimal.ZERO)))
                        .toList())
                .build();
    }

    private InvoiceReceiptHistoryDto toHistoryDto(PurchaseOrder purchaseOrder, List<InvoiceReceipt> receipts) {
        return InvoiceReceiptHistoryDto.builder()
                .id(purchaseOrder.getId())
                .poNo(purchaseOrder.getPoNo())
                .entries(receipts.stream().map(this::toHistoryEntryDto).toList())
                .build();
    }

    private InvoiceReceiptListItemDto toListItemDto(PurchaseOrder purchaseOrder, List<InvoiceReceipt> receipts) {
        InvoiceReceipt latestReceipt = receipts.isEmpty() ? null : receipts.get(0);
        String derivedStatus = deriveStatus(purchaseOrder, receipts);

        return InvoiceReceiptListItemDto.builder()
                .id(purchaseOrder.getId())
                .poNo(purchaseOrder.getPoNo())
                .requestNo(purchaseOrder.getRequestNo())
                .companyCode(purchaseOrder.getCompanyCode())
                .companyName(purchaseOrder.getCompanyName())
                .vendorCode(purchaseOrder.getVendorCode())
                .vendorName(purchaseOrder.getVendorName())
                .requestType(purchaseOrder.getRequestType())
                .status(derivedStatus)
                .statusDescription(toStatusDescription(derivedStatus))
                .totalAmount(purchaseOrder.getTotalAmount())
                .lastInvoiceDate(latestReceipt != null ? latestReceipt.getInvoiceDate() : null)
                .lastInvoiceNo(latestReceipt != null ? latestReceipt.getInvoiceNo() : null)
                .createdDate(purchaseOrder.getCreatedDate())
                .lastModifiedDate(purchaseOrder.getLastModifiedDate())
                .createdBy(purchaseOrder.getCreatedBy())
                .lastModifiedBy(purchaseOrder.getLastModifiedBy())
                .build();
    }

    private InvoiceReceiptItemBalanceDto toItemBalanceDto(PurchaseOrderItem item, BigDecimal receivedQuantity, BigDecimal invoicedQuantity) {
        return InvoiceReceiptItemBalanceDto.builder()
                .purchaseOrderItemId(item.getId())
                .itemCode(item.getItemCode())
                .itemDescription(item.getItemDescription())
                .uom(item.getUom())
                .orderedQuantity(item.getQuantity())
                .approvedQuantity(effectiveApprovedQuantity(item))
                .receivedQuantity(receivedQuantity)
                .invoicedQuantity(invoicedQuantity)
                .balanceQuantity(receivedQuantity.subtract(invoicedQuantity))
                .unitPrice(item.getUnitPrice())
                .lineAmount(item.getLineAmount())
                .build();
    }

    private InvoiceReceiptHistoryEntryDto toHistoryEntryDto(InvoiceReceipt receipt) {
        return InvoiceReceiptHistoryEntryDto.builder()
                .id(receipt.getId())
                .invoiceNo(receipt.getInvoiceNo())
                .invoiceDate(receipt.getInvoiceDate())
                .invoiceRemark(receipt.getInvoiceRemark())
                .receivedBy(receipt.getReceivedBy())
                .createdDate(receipt.getCreatedDate())
                .totalAmount(receipt.getTotalAmount())
                .items(receipt.getItems().stream().map(this::toHistoryLineDto).toList())
                .build();
    }

    private InvoiceReceiptHistoryLineDto toHistoryLineDto(InvoiceReceiptItem item) {
        return InvoiceReceiptHistoryLineDto.builder()
                .purchaseOrderItemId(item.getPurchaseOrderItem().getId())
                .itemCode(item.getItemCode())
                .itemDescription(item.getItemDescription())
                .uom(item.getUom())
                .invoicedQuantity(item.getInvoicedQuantity())
                .unitPrice(item.getUnitPrice())
                .lineAmount(item.getLineAmount())
                .build();
    }

    private String deriveStatus(PurchaseOrder purchaseOrder, List<InvoiceReceipt> receipts) {
        if (receipts.isEmpty()) {
            return purchaseOrder.getStatus().name();
        }

        Map<Long, BigDecimal> receivedQuantities = calculateReceivedQuantities(purchaseOrder);
        Map<Long, BigDecimal> invoicedQuantities = calculateInvoicedQuantities(receipts);

        boolean anyInvoiced = invoicedQuantities.values().stream().anyMatch(quantity -> quantity.compareTo(BigDecimal.ZERO) > 0);
        if (!anyInvoiced) {
            return purchaseOrder.getStatus().name();
        }

        boolean fullyInvoiced = purchaseOrder.getStatus() == PurchaseOrderStatus.RECEIVED
                && purchaseOrder.getItems().stream()
                .allMatch(item -> invoicedQuantities.getOrDefault(item.getId(), BigDecimal.ZERO)
                        .compareTo(receivedQuantities.getOrDefault(item.getId(), BigDecimal.ZERO)) >= 0);

        return fullyInvoiced ? STATUS_INVOICED : STATUS_PARTIALLY_INVOICED;
    }

    private Comparator<InvoiceReceiptListItemDto> resolveComparator(String sortColumn, String sortDirection) {
        Comparator<InvoiceReceiptListItemDto> comparator = switch (sortColumn) {
            case "poNo" -> Comparator.comparing(InvoiceReceiptListItemDto::getPoNo, Comparator.nullsLast(String::compareToIgnoreCase));
            case "requestNo" -> Comparator.comparing(InvoiceReceiptListItemDto::getRequestNo, Comparator.nullsLast(String::compareToIgnoreCase));
            case "companyCode" -> Comparator.comparing(InvoiceReceiptListItemDto::getCompanyCode, Comparator.nullsLast(String::compareToIgnoreCase));
            case "vendorCode" -> Comparator.comparing(InvoiceReceiptListItemDto::getVendorCode, Comparator.nullsLast(String::compareToIgnoreCase));
            case "vendorName" -> Comparator.comparing(InvoiceReceiptListItemDto::getVendorName, Comparator.nullsLast(String::compareToIgnoreCase));
            case "requestType" -> Comparator.comparing(InvoiceReceiptListItemDto::getRequestType, Comparator.nullsLast(String::compareToIgnoreCase));
            case "status" -> Comparator.comparing(InvoiceReceiptListItemDto::getStatus, Comparator.nullsLast(String::compareToIgnoreCase));
            case "totalAmount" -> Comparator.comparing(InvoiceReceiptListItemDto::getTotalAmount, Comparator.nullsLast(BigDecimal::compareTo));
            case "lastInvoiceDate" -> Comparator.comparing(InvoiceReceiptListItemDto::getLastInvoiceDate, Comparator.nullsLast(Comparator.naturalOrder()));
            case "createdDate" -> Comparator.comparing(InvoiceReceiptListItemDto::getCreatedDate, Comparator.nullsLast(Comparator.naturalOrder()));
            case "lastModifiedDate" -> Comparator.comparing(InvoiceReceiptListItemDto::getLastModifiedDate, Comparator.nullsLast(Comparator.naturalOrder()));
            default -> Comparator.comparing(InvoiceReceiptListItemDto::getLastModifiedDate, Comparator.nullsLast(Comparator.naturalOrder()));
        };

        return "ASC".equalsIgnoreCase(sortDirection) ? comparator : comparator.reversed();
    }

    private ReferenceOptionDto option(String code, String description) {
        return ReferenceOptionDto.builder()
                .code(code)
                .description(description)
                .build();
    }

    private String toStatusDescription(String status) {
        return switch (status) {
            case "PARTIALLY_RECEIVED" -> "Partially Received";
            case "RECEIVED" -> "Received";
            case STATUS_PARTIALLY_INVOICED -> "Partially Invoiced";
            case STATUS_INVOICED -> "Invoiced";
            default -> status;
        };
    }

    private void applyAudit(InvoiceReceipt invoiceReceipt, String username) {
        LocalDateTime now = LocalDateTime.now();
        invoiceReceipt.setCreatedDate(now);
        invoiceReceipt.setCreatedBy(username);
        invoiceReceipt.setLastModifiedDate(now);
        invoiceReceipt.setLastModifiedBy(username);
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

    private String normalize(String value) {
        return value == null ? null : value.trim().toUpperCase(Locale.ROOT);
    }

    private BigDecimal effectiveApprovedQuantity(PurchaseOrderItem item) {
        return item.getApprovedQuantity() != null ? item.getApprovedQuantity() : item.getQuantity();
    }
}
