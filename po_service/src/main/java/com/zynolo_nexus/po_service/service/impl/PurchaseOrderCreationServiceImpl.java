package com.zynolo_nexus.po_service.service.impl;

import com.zynolo_nexus.po_service.dto.request.ApprovedPoRequestFilterRequest;
import com.zynolo_nexus.po_service.dto.request.PoRequestFilterSearch;
import com.zynolo_nexus.po_service.dto.request.PurchaseOrderCreateRequest;
import com.zynolo_nexus.po_service.dto.request.PurchaseOrderFilterRequest;
import com.zynolo_nexus.po_service.dto.request.PurchaseOrderFilterSearch;
import com.zynolo_nexus.po_service.dto.request.PurchaseOrderReferenceDataRequest;
import com.zynolo_nexus.po_service.dto.request.PurchaseOrderRequestViewRequest;
import com.zynolo_nexus.po_service.dto.request.PurchaseOrderSendRequest;
import com.zynolo_nexus.po_service.dto.request.PurchaseOrderUpdateRequest;
import com.zynolo_nexus.po_service.dto.request.PurchaseOrderViewRequest;
import com.zynolo_nexus.po_service.dto.response.PoRequestDto;
import com.zynolo_nexus.po_service.dto.response.PoRequestFilterResultDto;
import com.zynolo_nexus.po_service.dto.response.PoRequestItemDto;
import com.zynolo_nexus.po_service.dto.response.PoRequestListItemDto;
import com.zynolo_nexus.po_service.dto.response.PurchaseOrderDto;
import com.zynolo_nexus.po_service.dto.response.PurchaseOrderFilterResultDto;
import com.zynolo_nexus.po_service.dto.response.PurchaseOrderItemDto;
import com.zynolo_nexus.po_service.dto.response.PurchaseOrderListItemDto;
import com.zynolo_nexus.po_service.dto.response.PurchaseOrderPrivilegesDto;
import com.zynolo_nexus.po_service.dto.response.PurchaseOrderReferenceDataDto;
import com.zynolo_nexus.po_service.dto.response.ReferenceOptionDto;
import com.zynolo_nexus.po_service.enums.MasterStatus;
import com.zynolo_nexus.po_service.enums.PoRequestStatus;
import com.zynolo_nexus.po_service.enums.PurchaseOrderStatus;
import com.zynolo_nexus.po_service.exception.BadRequestException;
import com.zynolo_nexus.po_service.exception.ResourceNotFoundException;
import com.zynolo_nexus.po_service.model.PoRequest;
import com.zynolo_nexus.po_service.model.PoRequestItem;
import com.zynolo_nexus.po_service.model.PurchaseOrder;
import com.zynolo_nexus.po_service.model.PurchaseOrderItem;
import com.zynolo_nexus.po_service.repository.CompanyRepository;
import com.zynolo_nexus.po_service.repository.CurrencyRepository;
import com.zynolo_nexus.po_service.repository.PoRequestRepository;
import com.zynolo_nexus.po_service.repository.PurchaseOrderRepository;
import com.zynolo_nexus.po_service.repository.VendorRepository;
import com.zynolo_nexus.po_service.service.PurchaseOrderCreationService;
import com.zynolo_nexus.po_service.service.support.PagePrivilegeResolver;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Subquery;
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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PurchaseOrderCreationServiceImpl implements PurchaseOrderCreationService {

    private static final String PAGE_CODE = "POCR";
    private static final DateTimeFormatter PO_NO_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final Map<String, String> REQUEST_TYPES = new LinkedHashMap<>();

    static {
        REQUEST_TYPES.put("GOODS", "Goods");
        REQUEST_TYPES.put("SERVICES", "Services");
        REQUEST_TYPES.put("COMBINED", "Combined");
    }

    private final CompanyRepository companyRepository;
    private final CurrencyRepository currencyRepository;
    private final VendorRepository vendorRepository;
    private final PoRequestRepository poRequestRepository;
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
                        option(PurchaseOrderStatus.DRAFT.name(), "Draft"),
                        option(PurchaseOrderStatus.SENT.name(), "Sent")
                ))
                .privileges(PurchaseOrderPrivilegesDto.builder()
                        .add(privileges.isAdd())
                        .update(privileges.isUpdate())
                        .view(privileges.isView())
                        .search(privileges.isSearch())
                        .send(privileges.isSend())
                        .build())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PoRequestFilterResultDto approvedRequests(ApprovedPoRequestFilterRequest request) {
        Pageable pageable = PageRequest.of(
                request.getPage(),
                request.getSize(),
                Sort.by(resolveDirection(request.getSortDirection()), resolveApprovedRequestSortColumn(request.getSortColumn()))
        );

        Page<PoRequest> page = poRequestRepository.findAll(buildApprovedRequestSpecification(request.getSearch()), pageable);
        List<PoRequestListItemDto> content = page.getContent().stream()
                .map(this::toPoRequestListItemDto)
                .toList();

        return PoRequestFilterResultDto.builder()
                .content(content)
                .size(content.size())
                .totalRecords(page.getTotalElements())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PoRequestDto requestView(PurchaseOrderRequestViewRequest request) {
        PoRequest poRequest = getPoRequest(request.getRequestId());
        validateApproved(poRequest, "Only APPROVED requests can be used for PO creation");
        return toPoRequestDto(poRequest);
    }

    @Override
    @Transactional
    public PurchaseOrderDto create(PurchaseOrderCreateRequest request) {
        PoRequest poRequest = getPoRequest(request.getRequestId());
        validateApproved(poRequest, "Only APPROVED requests can be converted to a purchase order");

        if (purchaseOrderRepository.existsByRequest_Id(poRequest.getId())) {
            throw new BadRequestException("Purchase order already exists for request ID: " + poRequest.getId());
        }

        PurchaseOrder purchaseOrder = new PurchaseOrder();
        purchaseOrder.setPoNo(generatePoNo());
        purchaseOrder.setRequest(poRequest);
        copyFromRequest(poRequest, purchaseOrder);
        purchaseOrder.setStatus(PurchaseOrderStatus.DRAFT);
        purchaseOrder.setSentDate(null);
        purchaseOrder.setSentBy(null);
        purchaseOrder.setSendRemark(null);
        copyItemsFromRequest(poRequest, purchaseOrder);
        applyAudit(purchaseOrder, request.getUsername(), true);

        return toPurchaseOrderDto(purchaseOrderRepository.save(purchaseOrder));
    }

    @Override
    @Transactional(readOnly = true)
    public PurchaseOrderDto view(PurchaseOrderViewRequest request) {
        return toPurchaseOrderDto(getPurchaseOrder(request.getId()));
    }

    @Override
    @Transactional
    public PurchaseOrderDto update(PurchaseOrderUpdateRequest request) {
        PurchaseOrder purchaseOrder = getPurchaseOrder(request.getId());
        validateDraft(purchaseOrder, "Only DRAFT purchase orders can be updated");

        purchaseOrder.setRequiredDate(request.getRequiredDate());
        purchaseOrder.setJustification(trim(request.getJustification()));
        replaceItems(purchaseOrder, request);
        purchaseOrder.setTotalAmount(calculateTotal(purchaseOrder.getItems()));
        applyAudit(purchaseOrder, request.getUsername(), false);

        return toPurchaseOrderDto(purchaseOrderRepository.save(purchaseOrder));
    }

    @Override
    @Transactional
    public PurchaseOrderDto sendToVendor(PurchaseOrderSendRequest request) {
        PurchaseOrder purchaseOrder = getPurchaseOrder(request.getId());
        validateDraft(purchaseOrder, "Only DRAFT purchase orders can be sent to vendor");

        purchaseOrder.setStatus(PurchaseOrderStatus.SENT);
        purchaseOrder.setSentDate(LocalDateTime.now());
        purchaseOrder.setSentBy(request.getUsername());
        purchaseOrder.setSendRemark(trim(request.getSendRemark()));
        applyAudit(purchaseOrder, request.getUsername(), false);

        return toPurchaseOrderDto(purchaseOrderRepository.save(purchaseOrder));
    }

    @Override
    @Transactional(readOnly = true)
    public PurchaseOrderFilterResultDto filterList(PurchaseOrderFilterRequest request) {
        Pageable pageable = PageRequest.of(
                request.getPage(),
                request.getSize(),
                Sort.by(resolveDirection(request.getSortDirection()), resolvePurchaseOrderSortColumn(request.getSortColumn()))
        );

        Page<PurchaseOrder> page = purchaseOrderRepository.findAll(buildPurchaseOrderSpecification(request.getSearch()), pageable);
        List<PurchaseOrderListItemDto> content = page.getContent().stream()
                .map(this::toPurchaseOrderListItemDto)
                .toList();

        return PurchaseOrderFilterResultDto.builder()
                .content(content)
                .size(content.size())
                .totalRecords(page.getTotalElements())
                .build();
    }

    private Specification<PoRequest> buildApprovedRequestSpecification(PoRequestFilterSearch search) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("status"), PoRequestStatus.APPROVED));

            Subquery<Long> subquery = query.subquery(Long.class);
            var poRoot = subquery.from(PurchaseOrder.class);
            subquery.select(poRoot.get("request").get("id"));
            subquery.where(cb.equal(poRoot.get("request").get("id"), root.get("id")));
            predicates.add(cb.not(cb.exists(subquery)));

            if (search != null) {
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
                if (hasText(search.getRequestedBy())) {
                    predicates.add(cb.like(cb.lower(root.get("createdBy")), like(search.getRequestedBy())));
                }
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private Specification<PurchaseOrder> buildPurchaseOrderSpecification(PurchaseOrderFilterSearch search) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (search != null) {
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
                if (hasText(search.getPoNo())) {
                    predicates.add(cb.like(cb.lower(root.get("poNo")), like(search.getPoNo())));
                }
                if (hasText(search.getStatus())) {
                    try {
                        predicates.add(cb.equal(root.get("status"), PurchaseOrderStatus.valueOf(search.getStatus().trim().toUpperCase(Locale.ROOT))));
                    } catch (IllegalArgumentException ex) {
                        throw new BadRequestException("Invalid status: " + search.getStatus());
                    }
                }
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private void copyFromRequest(PoRequest poRequest, PurchaseOrder purchaseOrder) {
        purchaseOrder.setRequestNo(poRequest.getRequestNo());
        purchaseOrder.setCompanyCode(poRequest.getCompanyCode());
        purchaseOrder.setCompanyName(poRequest.getCompanyName());
        purchaseOrder.setRequestType(poRequest.getRequestType());
        purchaseOrder.setDepartment(poRequest.getDepartment());
        purchaseOrder.setCostCenter(poRequest.getCostCenter());
        purchaseOrder.setCurrencyCode(poRequest.getCurrencyCode());
        purchaseOrder.setVendorCode(poRequest.getVendorCode());
        purchaseOrder.setVendorName(poRequest.getVendorName());
        purchaseOrder.setRequiredDate(poRequest.getRequiredDate());
        purchaseOrder.setJustification(poRequest.getJustification());
        purchaseOrder.setTotalAmount(poRequest.getTotalAmount());
    }

    private void copyItemsFromRequest(PoRequest poRequest, PurchaseOrder purchaseOrder) {
        purchaseOrder.clearItems();
        for (PoRequestItem requestItem : poRequest.getItems()) {
            PurchaseOrderItem orderItem = new PurchaseOrderItem();
            orderItem.setItemCode(requestItem.getItemCode());
            orderItem.setItemDescription(requestItem.getItemDescription());
            orderItem.setUom(requestItem.getUom());
            orderItem.setQuantity(requestItem.getQuantity());
            orderItem.setUnitPrice(requestItem.getUnitPrice());
            orderItem.setLineAmount(requestItem.getLineAmount());
            purchaseOrder.addItem(orderItem);
        }
    }

    private void replaceItems(PurchaseOrder purchaseOrder, PurchaseOrderUpdateRequest request) {
        Map<String, PurchaseOrderItem> existingItems = purchaseOrder.getItems().stream()
                .collect(Collectors.toMap(
                        item -> normalize(item.getItemCode()),
                        Function.identity(),
                        (first, second) -> {
                            throw new BadRequestException("Duplicate item code found in purchase order: " + first.getItemCode());
                        },
                        LinkedHashMap::new
                ));

        Map<String, String> requestItems = request.getItems().stream()
                .collect(Collectors.toMap(
                        item -> normalize(item.getItemCode()),
                        item -> item.getItemCode(),
                        (first, second) -> {
                            throw new BadRequestException("Duplicate item code found in update payload: " + first);
                        },
                        LinkedHashMap::new
                ));

        if (existingItems.size() != request.getItems().size() || !existingItems.keySet().equals(requestItems.keySet())) {
            throw new BadRequestException("Updated items must match the existing purchase order item codes");
        }

        purchaseOrder.clearItems();
        request.getItems().forEach(itemRequest -> {
            PurchaseOrderItem existingItem = existingItems.get(normalize(itemRequest.getItemCode()));
            PurchaseOrderItem updatedItem = new PurchaseOrderItem();
            updatedItem.setItemCode(existingItem.getItemCode());
            updatedItem.setItemDescription(existingItem.getItemDescription());
            updatedItem.setUom(existingItem.getUom());
            updatedItem.setQuantity(itemRequest.getQuantity());
            updatedItem.setUnitPrice(itemRequest.getUnitPrice());
            updatedItem.setLineAmount(itemRequest.getQuantity().multiply(itemRequest.getUnitPrice()));
            purchaseOrder.addItem(updatedItem);
        });
    }

    private BigDecimal calculateTotal(List<PurchaseOrderItem> items) {
        return items.stream()
                .map(PurchaseOrderItem::getLineAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private PurchaseOrder getPurchaseOrder(Long id) {
        return purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase order not found with ID: " + id));
    }

    private PoRequest getPoRequest(Long id) {
        return poRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PO request not found with ID: " + id));
    }

    private void validateApproved(PoRequest poRequest, String message) {
        if (poRequest.getStatus() != PoRequestStatus.APPROVED) {
            throw new BadRequestException(message);
        }
    }

    private void validateDraft(PurchaseOrder purchaseOrder, String message) {
        if (purchaseOrder.getStatus() != PurchaseOrderStatus.DRAFT) {
            throw new BadRequestException(message);
        }
    }

    private void applyAudit(PurchaseOrder purchaseOrder, String username, boolean create) {
        LocalDateTime now = LocalDateTime.now();
        if (create) {
            purchaseOrder.setCreatedDate(now);
            purchaseOrder.setCreatedBy(username);
        }
        purchaseOrder.setLastModifiedDate(now);
        purchaseOrder.setLastModifiedBy(username);
    }

    private String generatePoNo() {
        return "PO-" + LocalDateTime.now().format(PO_NO_FORMAT) + "-"
                + ThreadLocalRandom.current().nextInt(1000, 9999);
    }

    private Sort.Direction resolveDirection(String direction) {
        return "ASC".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
    }

    private String resolveApprovedRequestSortColumn(String sortColumn) {
        if (!hasText(sortColumn)) {
            return "lastModifiedDate";
        }
        return switch (sortColumn) {
            case "requestNo", "companyCode", "vendorCode", "requestType", "status",
                    "createdDate", "lastModifiedDate", "requiredDate", "totalAmount", "createdBy", "reviewedDate", "reviewedBy" -> sortColumn;
            default -> "lastModifiedDate";
        };
    }

    private String resolvePurchaseOrderSortColumn(String sortColumn) {
        if (!hasText(sortColumn)) {
            return "lastModifiedDate";
        }
        return switch (sortColumn) {
            case "poNo", "requestNo", "companyCode", "vendorCode", "requestType", "status",
                    "createdDate", "lastModifiedDate", "requiredDate", "totalAmount", "createdBy", "sentDate" -> sortColumn;
            default -> "lastModifiedDate";
        };
    }

    private PurchaseOrderDto toPurchaseOrderDto(PurchaseOrder purchaseOrder) {
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
                .statusDescription(toPurchaseOrderStatusDescription(purchaseOrder.getStatus()))
                .totalAmount(purchaseOrder.getTotalAmount())
                .sentDate(purchaseOrder.getSentDate())
                .sentBy(purchaseOrder.getSentBy())
                .sendRemark(purchaseOrder.getSendRemark())
                .createdDate(purchaseOrder.getCreatedDate())
                .lastModifiedDate(purchaseOrder.getLastModifiedDate())
                .createdBy(purchaseOrder.getCreatedBy())
                .lastModifiedBy(purchaseOrder.getLastModifiedBy())
                .items(purchaseOrder.getItems().stream().map(this::toPurchaseOrderItemDto).toList())
                .build();
    }

    private PurchaseOrderListItemDto toPurchaseOrderListItemDto(PurchaseOrder purchaseOrder) {
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
                .statusDescription(toPurchaseOrderStatusDescription(purchaseOrder.getStatus()))
                .totalAmount(purchaseOrder.getTotalAmount())
                .requiredDate(purchaseOrder.getRequiredDate())
                .sentDate(purchaseOrder.getSentDate())
                .createdDate(purchaseOrder.getCreatedDate())
                .lastModifiedDate(purchaseOrder.getLastModifiedDate())
                .createdBy(purchaseOrder.getCreatedBy())
                .lastModifiedBy(purchaseOrder.getLastModifiedBy())
                .build();
    }

    private PurchaseOrderItemDto toPurchaseOrderItemDto(PurchaseOrderItem item) {
        return PurchaseOrderItemDto.builder()
                .id(item.getId())
                .itemCode(item.getItemCode())
                .itemDescription(item.getItemDescription())
                .uom(item.getUom())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .lineAmount(item.getLineAmount())
                .build();
    }

    private PoRequestDto toPoRequestDto(PoRequest poRequest) {
        return PoRequestDto.builder()
                .id(poRequest.getId())
                .requestNo(poRequest.getRequestNo())
                .companyCode(poRequest.getCompanyCode())
                .companyName(poRequest.getCompanyName())
                .requestType(poRequest.getRequestType())
                .department(poRequest.getDepartment())
                .costCenter(poRequest.getCostCenter())
                .currencyCode(poRequest.getCurrencyCode())
                .vendorCode(poRequest.getVendorCode())
                .vendorName(poRequest.getVendorName())
                .requiredDate(poRequest.getRequiredDate())
                .justification(poRequest.getJustification())
                .status(poRequest.getStatus().name())
                .statusDescription(toPoRequestStatusDescription(poRequest.getStatus()))
                .totalAmount(poRequest.getTotalAmount())
                .submittedDate(poRequest.getSubmittedDate())
                .reviewedDate(poRequest.getReviewedDate())
                .reviewedBy(poRequest.getReviewedBy())
                .reviewRemark(poRequest.getReviewRemark())
                .createdDate(poRequest.getCreatedDate())
                .lastModifiedDate(poRequest.getLastModifiedDate())
                .createdBy(poRequest.getCreatedBy())
                .lastModifiedBy(poRequest.getLastModifiedBy())
                .items(poRequest.getItems().stream().map(this::toPoRequestItemDto).toList())
                .build();
    }

    private PoRequestListItemDto toPoRequestListItemDto(PoRequest poRequest) {
        return PoRequestListItemDto.builder()
                .id(poRequest.getId())
                .requestNo(poRequest.getRequestNo())
                .companyCode(poRequest.getCompanyCode())
                .companyName(poRequest.getCompanyName())
                .vendorCode(poRequest.getVendorCode())
                .vendorName(poRequest.getVendorName())
                .requestType(poRequest.getRequestType())
                .status(poRequest.getStatus().name())
                .statusDescription(toPoRequestStatusDescription(poRequest.getStatus()))
                .totalAmount(poRequest.getTotalAmount())
                .requiredDate(poRequest.getRequiredDate())
                .createdDate(poRequest.getCreatedDate())
                .lastModifiedDate(poRequest.getLastModifiedDate())
                .createdBy(poRequest.getCreatedBy())
                .lastModifiedBy(poRequest.getLastModifiedBy())
                .build();
    }

    private PoRequestItemDto toPoRequestItemDto(PoRequestItem item) {
        return PoRequestItemDto.builder()
                .id(item.getId())
                .itemCode(item.getItemCode())
                .itemDescription(item.getItemDescription())
                .uom(item.getUom())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .lineAmount(item.getLineAmount())
                .build();
    }

    private String toPoRequestStatusDescription(PoRequestStatus status) {
        return switch (status) {
            case DRAFT -> "Draft";
            case SUBMITTED -> "Submitted";
            case APPROVED -> "Approved";
            case REJECTED -> "Rejected";
        };
    }

    private String toPurchaseOrderStatusDescription(PurchaseOrderStatus status) {
        return switch (status) {
            case DRAFT -> "Draft";
            case SENT -> "Sent";
        };
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

    private String like(String value) {
        return "%" + value.trim().toLowerCase(Locale.ROOT) + "%";
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private String normalize(String value) {
        return value == null ? null : value.trim().toUpperCase(Locale.ROOT);
    }
}
