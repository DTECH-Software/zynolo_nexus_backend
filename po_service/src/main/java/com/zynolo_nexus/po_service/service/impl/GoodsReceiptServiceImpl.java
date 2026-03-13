package com.zynolo_nexus.po_service.service.impl;

import com.zynolo_nexus.po_service.dto.request.GoodsReceiptFilterRequest;
import com.zynolo_nexus.po_service.dto.request.GoodsReceiptFilterSearch;
import com.zynolo_nexus.po_service.dto.request.GoodsReceiptHistoryRequest;
import com.zynolo_nexus.po_service.dto.request.GoodsReceiptReceiveRequest;
import com.zynolo_nexus.po_service.dto.request.GoodsReceiptReferenceDataRequest;
import com.zynolo_nexus.po_service.dto.request.GoodsReceiptViewRequest;
import com.zynolo_nexus.po_service.dto.response.GoodsReceiptFilterResultDto;
import com.zynolo_nexus.po_service.dto.response.GoodsReceiptHistoryDto;
import com.zynolo_nexus.po_service.dto.response.GoodsReceiptHistoryEntryDto;
import com.zynolo_nexus.po_service.dto.response.GoodsReceiptHistoryLineDto;
import com.zynolo_nexus.po_service.dto.response.GoodsReceiptItemBalanceDto;
import com.zynolo_nexus.po_service.dto.response.GoodsReceiptListItemDto;
import com.zynolo_nexus.po_service.dto.response.GoodsReceiptPrivilegesDto;
import com.zynolo_nexus.po_service.dto.response.GoodsReceiptReferenceDataDto;
import com.zynolo_nexus.po_service.dto.response.GoodsReceiptViewDto;
import com.zynolo_nexus.po_service.dto.response.ReferenceOptionDto;
import com.zynolo_nexus.po_service.enums.MasterStatus;
import com.zynolo_nexus.po_service.enums.PurchaseOrderStatus;
import com.zynolo_nexus.po_service.exception.BadRequestException;
import com.zynolo_nexus.po_service.exception.ResourceNotFoundException;
import com.zynolo_nexus.po_service.model.GoodsReceipt;
import com.zynolo_nexus.po_service.model.GoodsReceiptItem;
import com.zynolo_nexus.po_service.model.PurchaseOrder;
import com.zynolo_nexus.po_service.model.PurchaseOrderItem;
import com.zynolo_nexus.po_service.repository.CompanyRepository;
import com.zynolo_nexus.po_service.repository.GoodsReceiptRepository;
import com.zynolo_nexus.po_service.repository.PurchaseOrderRepository;
import com.zynolo_nexus.po_service.repository.VendorRepository;
import com.zynolo_nexus.po_service.service.GoodsReceiptService;
import com.zynolo_nexus.po_service.service.support.PagePrivilegeResolver;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GoodsReceiptServiceImpl implements GoodsReceiptService {

    private static final String PAGE_CODE = "POGR";

    private final CompanyRepository companyRepository;
    private final VendorRepository vendorRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final GoodsReceiptRepository goodsReceiptRepository;
    private final PagePrivilegeResolver pagePrivilegeResolver;

    @Override
    @Transactional(readOnly = true)
    public GoodsReceiptReferenceDataDto getReferenceData(GoodsReceiptReferenceDataRequest request) {
        var privileges = pagePrivilegeResolver.resolve(request != null ? request.getUsername() : null, PAGE_CODE);

        return GoodsReceiptReferenceDataDto.builder()
                .companies(companyRepository.findAllByStatusOrderByCodeAsc(MasterStatus.ACTIVE).stream()
                        .map(company -> option(company.getCode(), company.getDescription()))
                        .toList())
                .vendors(vendorRepository.findAllByStatusOrderByCodeAsc("ACTIVE").stream()
                        .map(vendor -> option(vendor.getCode(), vendor.getDescription()))
                        .toList())
                .defaultStatus(List.of(
                        option(PurchaseOrderStatus.VENDOR_CONFIRMED.name(), "Vendor Confirmed"),
                        option(PurchaseOrderStatus.PARTIALLY_CONFIRMED.name(), "Partially Confirmed"),
                        option(PurchaseOrderStatus.PARTIALLY_APPROVED.name(), "Partially Approved"),
                        option(PurchaseOrderStatus.PARTIALLY_RECEIVED.name(), "Partially Received"),
                        option(PurchaseOrderStatus.RECEIVED.name(), "Received")
                ))
                .privileges(GoodsReceiptPrivilegesDto.builder()
                        .view(privileges.isView())
                        .search(privileges.isSearch())
                        .receive(privileges.isReceive())
                        .build())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public GoodsReceiptFilterResultDto filterList(GoodsReceiptFilterRequest request) {
        Pageable pageable = PageRequest.of(
                request.getPage(),
                request.getSize(),
                Sort.by(resolveDirection(request.getSortDirection()), resolveSortColumn(request.getSortColumn()))
        );

        Page<PurchaseOrder> page = purchaseOrderRepository.findAll(buildSpecification(request.getSearch()), pageable);
        List<GoodsReceiptListItemDto> content = page.getContent().stream()
                .map(this::toListItemDto)
                .toList();

        return GoodsReceiptFilterResultDto.builder()
                .content(content)
                .size(content.size())
                .totalRecords(page.getTotalElements())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public GoodsReceiptViewDto view(GoodsReceiptViewRequest request) {
        PurchaseOrder purchaseOrder = getPurchaseOrder(request.getId());
        validateVisible(purchaseOrder);
        return toViewDto(purchaseOrder);
    }

    @Override
    @Transactional
    public GoodsReceiptViewDto receive(GoodsReceiptReceiveRequest request) {
        PurchaseOrder purchaseOrder = getPurchaseOrder(request.getId());
        validateReceivable(purchaseOrder);

        Map<Long, BigDecimal> receivedQuantities = calculateReceivedQuantities(purchaseOrder);
        Map<String, PurchaseOrderItem> itemMap = toItemMap(purchaseOrder);

        GoodsReceipt goodsReceipt = new GoodsReceipt();
        goodsReceipt.setPurchaseOrder(purchaseOrder);
        goodsReceipt.setReceiptDate(request.getReceiptDate());
        goodsReceipt.setDeliveryNoteNo(trim(request.getDeliveryNoteNo()));
        goodsReceipt.setReceiveRemark(trim(request.getReceiveRemark()));
        goodsReceipt.setReceivedBy(request.getUsername());
        applyAudit(goodsReceipt, request.getUsername());

        Map<String, String> payloadItems = new LinkedHashMap<>();
        request.getItems().forEach(item -> {
            String normalized = normalize(item.getItemCode());
            if (payloadItems.putIfAbsent(normalized, item.getItemCode()) != null) {
                throw new BadRequestException("Duplicate item code found in receipt payload: " + item.getItemCode());
            }

            PurchaseOrderItem purchaseOrderItem = itemMap.get(normalized);
            if (purchaseOrderItem == null) {
                throw new BadRequestException("Purchase order item not found for code: " + item.getItemCode());
            }

            BigDecimal approvedQuantity = effectiveApprovedQuantity(purchaseOrderItem);
            BigDecimal alreadyReceived = receivedQuantities.getOrDefault(purchaseOrderItem.getId(), BigDecimal.ZERO);
            BigDecimal balance = approvedQuantity.subtract(alreadyReceived);
            if (item.getReceivedQuantity().compareTo(balance) > 0) {
                throw new BadRequestException("Received quantity exceeds balance for item code: " + item.getItemCode());
            }

            GoodsReceiptItem receiptItem = new GoodsReceiptItem();
            receiptItem.setPurchaseOrderItem(purchaseOrderItem);
            receiptItem.setItemCode(purchaseOrderItem.getItemCode());
            receiptItem.setItemDescription(purchaseOrderItem.getItemDescription());
            receiptItem.setUom(purchaseOrderItem.getUom());
            receiptItem.setOrderedQuantity(purchaseOrderItem.getQuantity());
            receiptItem.setApprovedQuantity(approvedQuantity);
            receiptItem.setReceivedQuantity(item.getReceivedQuantity());
            goodsReceipt.addItem(receiptItem);

            receivedQuantities.put(purchaseOrderItem.getId(), alreadyReceived.add(item.getReceivedQuantity()));
        });

        goodsReceiptRepository.save(goodsReceipt);

        purchaseOrder.setStatus(resolveReceiptStatus(purchaseOrder, receivedQuantities));
        purchaseOrder.setLastModifiedDate(LocalDateTime.now());
        purchaseOrder.setLastModifiedBy(request.getUsername());
        purchaseOrderRepository.save(purchaseOrder);

        return toViewDto(purchaseOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public GoodsReceiptHistoryDto history(GoodsReceiptHistoryRequest request) {
        PurchaseOrder purchaseOrder = getPurchaseOrder(request.getId());
        List<GoodsReceiptHistoryEntryDto> entries = goodsReceiptRepository.findAllByPurchaseOrder_IdOrderByReceiptDateDescIdDesc(purchaseOrder.getId()).stream()
                .map(this::toHistoryEntryDto)
                .toList();

        return GoodsReceiptHistoryDto.builder()
                .poId(purchaseOrder.getId())
                .poNo(purchaseOrder.getPoNo())
                .entries(entries)
                .build();
    }

    private Specification<PurchaseOrder> buildSpecification(GoodsReceiptFilterSearch search) {
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
                if (hasText(search.getStatus())) {
                    predicates.add(cb.equal(root.get("status"), parseStatus(search.getStatus())));
                } else {
                    predicates.add(root.get("status").in(
                            PurchaseOrderStatus.VENDOR_CONFIRMED,
                            PurchaseOrderStatus.PARTIALLY_CONFIRMED,
                            PurchaseOrderStatus.PARTIALLY_APPROVED,
                            PurchaseOrderStatus.PARTIALLY_RECEIVED,
                            PurchaseOrderStatus.RECEIVED
                    ));
                }
            } else {
                predicates.add(root.get("status").in(
                        PurchaseOrderStatus.VENDOR_CONFIRMED,
                        PurchaseOrderStatus.PARTIALLY_CONFIRMED,
                        PurchaseOrderStatus.PARTIALLY_APPROVED,
                        PurchaseOrderStatus.PARTIALLY_RECEIVED,
                        PurchaseOrderStatus.RECEIVED
                ));
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private PurchaseOrder getPurchaseOrder(Long id) {
        return purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase order not found with ID: " + id));
    }

    private void validateVisible(PurchaseOrder purchaseOrder) {
        if (!(purchaseOrder.getStatus() == PurchaseOrderStatus.VENDOR_CONFIRMED
                || purchaseOrder.getStatus() == PurchaseOrderStatus.PARTIALLY_CONFIRMED
                || purchaseOrder.getStatus() == PurchaseOrderStatus.PARTIALLY_APPROVED
                || purchaseOrder.getStatus() == PurchaseOrderStatus.PARTIALLY_RECEIVED
                || purchaseOrder.getStatus() == PurchaseOrderStatus.RECEIVED)) {
            throw new BadRequestException("Goods receipt is not available for purchase order status: " + purchaseOrder.getStatus().name());
        }
    }

    private void validateReceivable(PurchaseOrder purchaseOrder) {
        if (!(purchaseOrder.getStatus() == PurchaseOrderStatus.VENDOR_CONFIRMED
                || purchaseOrder.getStatus() == PurchaseOrderStatus.PARTIALLY_CONFIRMED
                || purchaseOrder.getStatus() == PurchaseOrderStatus.PARTIALLY_APPROVED
                || purchaseOrder.getStatus() == PurchaseOrderStatus.PARTIALLY_RECEIVED)) {
            throw new BadRequestException("Only confirmed purchase orders can record receipt");
        }
    }

    private Map<Long, BigDecimal> calculateReceivedQuantities(PurchaseOrder purchaseOrder) {
        Map<Long, BigDecimal> totals = new LinkedHashMap<>();
        goodsReceiptRepository.findAllByPurchaseOrder_IdOrderByReceiptDateDescIdDesc(purchaseOrder.getId())
                .forEach(receipt -> receipt.getItems().forEach(item ->
                        totals.merge(item.getPurchaseOrderItem().getId(), item.getReceivedQuantity(), BigDecimal::add)
                ));
        return totals;
    }

    private Map<String, PurchaseOrderItem> toItemMap(PurchaseOrder purchaseOrder) {
        return purchaseOrder.getItems().stream()
                .collect(Collectors.toMap(
                        item -> normalize(item.getItemCode()),
                        item -> item,
                        (first, second) -> {
                            throw new BadRequestException("Duplicate item code found in purchase order: " + first.getItemCode());
                        },
                        LinkedHashMap::new
                ));
    }

    private PurchaseOrderStatus resolveReceiptStatus(PurchaseOrder purchaseOrder, Map<Long, BigDecimal> receivedQuantities) {
        boolean allReceived = purchaseOrder.getItems().stream()
                .allMatch(item -> receivedQuantities.getOrDefault(item.getId(), BigDecimal.ZERO)
                        .compareTo(effectiveApprovedQuantity(item)) >= 0);
        return allReceived ? PurchaseOrderStatus.RECEIVED : PurchaseOrderStatus.PARTIALLY_RECEIVED;
    }

    private GoodsReceiptViewDto toViewDto(PurchaseOrder purchaseOrder) {
        Map<Long, BigDecimal> receivedQuantities = calculateReceivedQuantities(purchaseOrder);
        GoodsReceipt latestReceipt = goodsReceiptRepository.findTopByPurchaseOrder_IdOrderByReceiptDateDescIdDesc(purchaseOrder.getId())
                .orElse(null);

        return GoodsReceiptViewDto.builder()
                .poId(purchaseOrder.getId())
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
                .status(purchaseOrder.getStatus().name())
                .statusDescription(toStatusDescription(purchaseOrder.getStatus()))
                .totalAmount(purchaseOrder.getTotalAmount())
                .sentDate(purchaseOrder.getSentDate())
                .sentBy(purchaseOrder.getSentBy())
                .sendRemark(purchaseOrder.getSendRemark())
                .lastReceiptDate(latestReceipt != null ? latestReceipt.getReceiptDate() : null)
                .lastReceivedBy(latestReceipt != null ? latestReceipt.getReceivedBy() : null)
                .lastDeliveryNoteNo(latestReceipt != null ? latestReceipt.getDeliveryNoteNo() : null)
                .lastReceiveRemark(latestReceipt != null ? latestReceipt.getReceiveRemark() : null)
                .createdDate(purchaseOrder.getCreatedDate())
                .lastModifiedDate(purchaseOrder.getLastModifiedDate())
                .createdBy(purchaseOrder.getCreatedBy())
                .lastModifiedBy(purchaseOrder.getLastModifiedBy())
                .items(purchaseOrder.getItems().stream()
                        .map(item -> toItemBalanceDto(item, receivedQuantities.getOrDefault(item.getId(), BigDecimal.ZERO)))
                        .toList())
                .build();
    }

    private GoodsReceiptListItemDto toListItemDto(PurchaseOrder purchaseOrder) {
        GoodsReceipt latestReceipt = goodsReceiptRepository.findTopByPurchaseOrder_IdOrderByReceiptDateDescIdDesc(purchaseOrder.getId())
                .orElse(null);
        return GoodsReceiptListItemDto.builder()
                .poId(purchaseOrder.getId())
                .poNo(purchaseOrder.getPoNo())
                .requestNo(purchaseOrder.getRequestNo())
                .companyCode(purchaseOrder.getCompanyCode())
                .companyName(purchaseOrder.getCompanyName())
                .vendorCode(purchaseOrder.getVendorCode())
                .vendorName(purchaseOrder.getVendorName())
                .requestType(purchaseOrder.getRequestType())
                .status(purchaseOrder.getStatus().name())
                .statusDescription(toStatusDescription(purchaseOrder.getStatus()))
                .totalAmount(purchaseOrder.getTotalAmount())
                .requiredDate(purchaseOrder.getRequiredDate())
                .sentDate(purchaseOrder.getSentDate())
                .lastReceiptDate(latestReceipt != null ? latestReceipt.getReceiptDate() : null)
                .createdDate(purchaseOrder.getCreatedDate())
                .lastModifiedDate(purchaseOrder.getLastModifiedDate())
                .createdBy(purchaseOrder.getCreatedBy())
                .lastModifiedBy(purchaseOrder.getLastModifiedBy())
                .build();
    }

    private GoodsReceiptItemBalanceDto toItemBalanceDto(PurchaseOrderItem item, BigDecimal receivedQuantity) {
        return GoodsReceiptItemBalanceDto.builder()
                .purchaseOrderItemId(item.getId())
                .itemCode(item.getItemCode())
                .itemDescription(item.getItemDescription())
                .uom(item.getUom())
                .orderedQuantity(item.getQuantity())
                .approvedQuantity(effectiveApprovedQuantity(item))
                .receivedQuantity(receivedQuantity)
                .balanceQuantity(effectiveApprovedQuantity(item).subtract(receivedQuantity))
                .unitPrice(item.getUnitPrice())
                .lineAmount(item.getLineAmount())
                .build();
    }

    private GoodsReceiptHistoryEntryDto toHistoryEntryDto(GoodsReceipt receipt) {
        return GoodsReceiptHistoryEntryDto.builder()
                .id(receipt.getId())
                .receiptDate(receipt.getReceiptDate())
                .deliveryNoteNo(receipt.getDeliveryNoteNo())
                .receiveRemark(receipt.getReceiveRemark())
                .receivedBy(receipt.getReceivedBy())
                .createdDate(receipt.getCreatedDate())
                .items(receipt.getItems().stream()
                        .map(this::toHistoryLineDto)
                        .toList())
                .build();
    }

    private GoodsReceiptHistoryLineDto toHistoryLineDto(GoodsReceiptItem item) {
        return GoodsReceiptHistoryLineDto.builder()
                .purchaseOrderItemId(item.getPurchaseOrderItem().getId())
                .itemCode(item.getItemCode())
                .itemDescription(item.getItemDescription())
                .uom(item.getUom())
                .orderedQuantity(item.getOrderedQuantity())
                .approvedQuantity(item.getApprovedQuantity())
                .receivedQuantity(item.getReceivedQuantity())
                .build();
    }

    private ReferenceOptionDto option(String code, String description) {
        return ReferenceOptionDto.builder()
                .code(code)
                .description(description)
                .build();
    }

    private PurchaseOrderStatus parseStatus(String status) {
        try {
            return PurchaseOrderStatus.valueOf(status.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Invalid status: " + status);
        }
    }

    private String toStatusDescription(PurchaseOrderStatus status) {
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

    private Sort.Direction resolveDirection(String direction) {
        return "ASC".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
    }

    private String resolveSortColumn(String sortColumn) {
        if (!hasText(sortColumn)) {
            return "lastModifiedDate";
        }
        return switch (sortColumn) {
            case "poNo", "requestNo", "companyCode", "vendorCode", "requestType", "status",
                    "createdDate", "lastModifiedDate", "requiredDate", "totalAmount", "createdBy", "sentDate" -> sortColumn;
            default -> "lastModifiedDate";
        };
    }

    private void applyAudit(GoodsReceipt goodsReceipt, String username) {
        LocalDateTime now = LocalDateTime.now();
        goodsReceipt.setCreatedDate(now);
        goodsReceipt.setCreatedBy(username);
        goodsReceipt.setLastModifiedDate(now);
        goodsReceipt.setLastModifiedBy(username);
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
