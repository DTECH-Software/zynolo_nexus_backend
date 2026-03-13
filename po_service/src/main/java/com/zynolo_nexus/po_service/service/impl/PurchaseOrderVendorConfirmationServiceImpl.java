package com.zynolo_nexus.po_service.service.impl;

import com.zynolo_nexus.po_service.dto.request.PurchaseOrderFilterRequest;
import com.zynolo_nexus.po_service.dto.request.PurchaseOrderFilterSearch;
import com.zynolo_nexus.po_service.dto.request.PurchaseOrderReferenceDataRequest;
import com.zynolo_nexus.po_service.dto.request.PurchaseOrderVendorConfirmItemRequest;
import com.zynolo_nexus.po_service.dto.request.PurchaseOrderVendorConfirmRequest;
import com.zynolo_nexus.po_service.dto.request.PurchaseOrderViewRequest;
import com.zynolo_nexus.po_service.dto.response.PurchaseOrderDto;
import com.zynolo_nexus.po_service.dto.response.PurchaseOrderFilterResultDto;
import com.zynolo_nexus.po_service.dto.response.PurchaseOrderItemDto;
import com.zynolo_nexus.po_service.dto.response.PurchaseOrderListItemDto;
import com.zynolo_nexus.po_service.dto.response.PurchaseOrderPrivilegesDto;
import com.zynolo_nexus.po_service.dto.response.PurchaseOrderReferenceDataDto;
import com.zynolo_nexus.po_service.dto.response.ReferenceOptionDto;
import com.zynolo_nexus.po_service.enums.MasterStatus;
import com.zynolo_nexus.po_service.enums.PurchaseOrderStatus;
import com.zynolo_nexus.po_service.exception.BadRequestException;
import com.zynolo_nexus.po_service.exception.ResourceNotFoundException;
import com.zynolo_nexus.po_service.model.PurchaseOrder;
import com.zynolo_nexus.po_service.model.PurchaseOrderItem;
import com.zynolo_nexus.po_service.repository.CompanyRepository;
import com.zynolo_nexus.po_service.repository.CurrencyRepository;
import com.zynolo_nexus.po_service.repository.PurchaseOrderRepository;
import com.zynolo_nexus.po_service.repository.VendorRepository;
import com.zynolo_nexus.po_service.service.PurchaseOrderVendorConfirmationService;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PurchaseOrderVendorConfirmationServiceImpl implements PurchaseOrderVendorConfirmationService {

    private static final String PAGE_CODE = "POVC";
    private static final Map<String, String> REQUEST_TYPES = new LinkedHashMap<>();

    static {
        REQUEST_TYPES.put("GOODS", "Goods");
        REQUEST_TYPES.put("SERVICES", "Services");
        REQUEST_TYPES.put("COMBINED", "Combined");
    }

    private final CompanyRepository companyRepository;
    private final VendorRepository vendorRepository;
    private final CurrencyRepository currencyRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PagePrivilegeResolver pagePrivilegeResolver;

    @Override
    @Transactional(readOnly = true)
    public PurchaseOrderReferenceDataDto getReferenceData(PurchaseOrderReferenceDataRequest request) {
        var privileges = pagePrivilegeResolver.resolve(request != null ? request.getUsername() : null, PAGE_CODE);

        return PurchaseOrderReferenceDataDto.builder()
                .companies(companyRepository.findAllByStatusOrderByCodeAsc(MasterStatus.ACTIVE).stream()
                        .map(company -> option(company.getCode(), company.getDescription()))
                        .toList())
                .vendors(vendorRepository.findAllByStatusOrderByCodeAsc("ACTIVE").stream()
                        .map(vendor -> option(vendor.getCode(), vendor.getDescription()))
                        .toList())
                .requestTypes(REQUEST_TYPES.entrySet().stream()
                        .map(entry -> option(entry.getKey(), entry.getValue()))
                        .toList())
                .currencies(currencyRepository.findAllByStatusOrderByCodeAsc(MasterStatus.ACTIVE).stream()
                        .map(currency -> option(currency.getCode(), currency.getDescription()))
                        .toList())
                .defaultStatus(List.of(
                        option(PurchaseOrderStatus.SENT.name(), "Sent"),
                        option(PurchaseOrderStatus.VENDOR_CONFIRMED.name(), "Vendor Confirmed"),
                        option(PurchaseOrderStatus.PARTIALLY_CONFIRMED.name(), "Partially Confirmed"),
                        option(PurchaseOrderStatus.PARTIALLY_APPROVED.name(), "Partially Approved"),
                        option(PurchaseOrderStatus.VENDOR_REJECTED.name(), "Vendor Rejected")
                ))
                .privileges(PurchaseOrderPrivilegesDto.builder()
                        .add(false)
                        .update(false)
                        .view(privileges.isView())
                        .search(privileges.isSearch())
                        .send(false)
                        .confirm(privileges.isConfirm())
                        .build())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PurchaseOrderFilterResultDto filterList(PurchaseOrderFilterRequest request) {
        Pageable pageable = PageRequest.of(
                request.getPage(),
                request.getSize(),
                Sort.by(resolveDirection(request.getSortDirection()), resolveSortColumn(request.getSortColumn()))
        );

        Page<PurchaseOrder> page = purchaseOrderRepository.findAll(buildSpecification(request.getSearch()), pageable);
        List<PurchaseOrderListItemDto> content = page.getContent().stream()
                .map(this::toListItemDto)
                .toList();

        return PurchaseOrderFilterResultDto.builder()
                .content(content)
                .size(content.size())
                .totalRecords(page.getTotalElements())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PurchaseOrderDto view(PurchaseOrderViewRequest request) {
        return toDto(getPurchaseOrder(request.getId()));
    }

    @Override
    @Transactional
    public PurchaseOrderDto confirm(PurchaseOrderVendorConfirmRequest request) {
        PurchaseOrder purchaseOrder = getPurchaseOrder(request.getId());
        if (purchaseOrder.getStatus() != PurchaseOrderStatus.SENT) {
            throw new BadRequestException("Only SENT purchase orders can be vendor confirmed");
        }

        PurchaseOrderStatus confirmationStatus = parseConfirmationStatus(request.getStatus());
        applyApprovedQuantities(purchaseOrder, confirmationStatus, request.getItems());
        purchaseOrder.setStatus(confirmationStatus);
        purchaseOrder.setVendorConfirmationDate(LocalDateTime.now());
        purchaseOrder.setVendorConfirmationBy(request.getUsername());
        purchaseOrder.setVendorReferenceNo(trim(request.getVendorReferenceNo()));
        purchaseOrder.setExpectedDeliveryDate(request.getExpectedDeliveryDate());
        purchaseOrder.setVendorConfirmationRemark(trim(request.getVendorConfirmationRemark()));
        purchaseOrder.setLastModifiedDate(LocalDateTime.now());
        purchaseOrder.setLastModifiedBy(request.getUsername());

        return toDto(purchaseOrderRepository.save(purchaseOrder));
    }

    private void applyApprovedQuantities(PurchaseOrder purchaseOrder,
                                         PurchaseOrderStatus confirmationStatus,
                                         List<PurchaseOrderVendorConfirmItemRequest> items) {
        if (confirmationStatus == PurchaseOrderStatus.VENDOR_CONFIRMED) {
            purchaseOrder.getItems().forEach(item -> item.setApprovedQuantity(item.getQuantity()));
            return;
        }

        if (confirmationStatus == PurchaseOrderStatus.VENDOR_REJECTED) {
            purchaseOrder.getItems().forEach(item -> item.setApprovedQuantity(java.math.BigDecimal.ZERO));
            return;
        }

        if (items == null || items.isEmpty()) {
            throw new BadRequestException("items are required for partially approved/confirmed purchase orders");
        }

        Map<String, PurchaseOrderItem> purchaseOrderItems = new LinkedHashMap<>();
        purchaseOrder.getItems().forEach(item -> purchaseOrderItems.put(normalize(item.getItemCode()), item));

        Set<String> payloadCodes = new LinkedHashSet<>();
        boolean anyPartial = false;
        boolean anyPositive = false;

        for (PurchaseOrderVendorConfirmItemRequest itemRequest : items) {
            String normalized = normalize(itemRequest.getItemCode());
            if (!payloadCodes.add(normalized)) {
                throw new BadRequestException("Duplicate item code found in vendor confirmation payload: " + itemRequest.getItemCode());
            }

            PurchaseOrderItem purchaseOrderItem = purchaseOrderItems.get(normalized);
            if (purchaseOrderItem == null) {
                throw new BadRequestException("Purchase order item not found for code: " + itemRequest.getItemCode());
            }

            if (itemRequest.getApprovedQuantity().compareTo(purchaseOrderItem.getQuantity()) > 0) {
                throw new BadRequestException("Approved quantity exceeds ordered quantity for item code: " + itemRequest.getItemCode());
            }

            if (itemRequest.getApprovedQuantity().compareTo(java.math.BigDecimal.ZERO) < 0) {
                throw new BadRequestException("Approved quantity cannot be negative for item code: " + itemRequest.getItemCode());
            }

            if (itemRequest.getApprovedQuantity().compareTo(java.math.BigDecimal.ZERO) > 0) {
                anyPositive = true;
            }
            if (itemRequest.getApprovedQuantity().compareTo(purchaseOrderItem.getQuantity()) < 0) {
                anyPartial = true;
            }

            purchaseOrderItem.setApprovedQuantity(itemRequest.getApprovedQuantity());
        }

        if (!payloadCodes.equals(purchaseOrderItems.keySet())) {
            throw new BadRequestException("All purchase order item codes must be included for partial approval");
        }

        if (!anyPositive || !anyPartial) {
            throw new BadRequestException("Partial approval requires at least one approved quantity and at least one partially approved line");
        }
    }

    private Specification<PurchaseOrder> buildSpecification(PurchaseOrderFilterSearch search) {
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
                            PurchaseOrderStatus.SENT,
                            PurchaseOrderStatus.VENDOR_CONFIRMED,
                            PurchaseOrderStatus.PARTIALLY_CONFIRMED,
                            PurchaseOrderStatus.PARTIALLY_APPROVED,
                            PurchaseOrderStatus.VENDOR_REJECTED
                    ));
                }
            } else {
                predicates.add(root.get("status").in(
                        PurchaseOrderStatus.SENT,
                        PurchaseOrderStatus.VENDOR_CONFIRMED,
                        PurchaseOrderStatus.PARTIALLY_CONFIRMED,
                        PurchaseOrderStatus.PARTIALLY_APPROVED,
                        PurchaseOrderStatus.VENDOR_REJECTED
                ));
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private PurchaseOrder getPurchaseOrder(Long id) {
        return purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase order not found with ID: " + id));
    }

    private PurchaseOrderDto toDto(PurchaseOrder purchaseOrder) {
        return PurchaseOrderDto.builder()
                .id(purchaseOrder.getId())
                .requestId(purchaseOrder.getRequest().getId())
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
                .vendorConfirmationDate(purchaseOrder.getVendorConfirmationDate())
                .vendorConfirmationBy(purchaseOrder.getVendorConfirmationBy())
                .vendorReferenceNo(purchaseOrder.getVendorReferenceNo())
                .expectedDeliveryDate(purchaseOrder.getExpectedDeliveryDate())
                .vendorConfirmationRemark(purchaseOrder.getVendorConfirmationRemark())
                .createdDate(purchaseOrder.getCreatedDate())
                .lastModifiedDate(purchaseOrder.getLastModifiedDate())
                .createdBy(purchaseOrder.getCreatedBy())
                .lastModifiedBy(purchaseOrder.getLastModifiedBy())
                .items(purchaseOrder.getItems().stream().map(this::toItemDto).toList())
                .build();
    }

    private PurchaseOrderListItemDto toListItemDto(PurchaseOrder purchaseOrder) {
        return PurchaseOrderListItemDto.builder()
                .id(purchaseOrder.getId())
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
                .expectedDeliveryDate(purchaseOrder.getExpectedDeliveryDate())
                .sentDate(purchaseOrder.getSentDate())
                .createdDate(purchaseOrder.getCreatedDate())
                .lastModifiedDate(purchaseOrder.getLastModifiedDate())
                .createdBy(purchaseOrder.getCreatedBy())
                .lastModifiedBy(purchaseOrder.getLastModifiedBy())
                .build();
    }

    private PurchaseOrderItemDto toItemDto(PurchaseOrderItem item) {
        return PurchaseOrderItemDto.builder()
                .id(item.getId())
                .itemCode(item.getItemCode())
                .itemDescription(item.getItemDescription())
                .uom(item.getUom())
                .quantity(item.getQuantity())
                .approvedQuantity(effectiveApprovedQuantity(item))
                .unitPrice(item.getUnitPrice())
                .lineAmount(item.getLineAmount())
                .build();
    }

    private PurchaseOrderStatus parseStatus(String status) {
        try {
            return PurchaseOrderStatus.valueOf(status.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Invalid status: " + status);
        }
    }

    private PurchaseOrderStatus parseConfirmationStatus(String status) {
        PurchaseOrderStatus parsed = parseStatus(status);
        if (parsed != PurchaseOrderStatus.VENDOR_CONFIRMED
                && parsed != PurchaseOrderStatus.PARTIALLY_CONFIRMED
                && parsed != PurchaseOrderStatus.PARTIALLY_APPROVED
                && parsed != PurchaseOrderStatus.VENDOR_REJECTED) {
            throw new BadRequestException("Invalid vendor confirmation status: " + status);
        }
        return parsed;
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
            case CLOSED -> "Closed";
        };
    }

    private ReferenceOptionDto option(String code, String description) {
        return ReferenceOptionDto.builder()
                .code(code)
                .description(description)
                .build();
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
                    "createdDate", "lastModifiedDate", "requiredDate", "totalAmount", "createdBy", "sentDate", "expectedDeliveryDate" -> sortColumn;
            default -> "lastModifiedDate";
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

    private String normalize(String value) {
        return value == null ? null : value.trim().toUpperCase(Locale.ROOT);
    }

    private java.math.BigDecimal effectiveApprovedQuantity(PurchaseOrderItem item) {
        return item.getApprovedQuantity() != null ? item.getApprovedQuantity() : item.getQuantity();
    }
}
