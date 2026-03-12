package com.zynolo_nexus.po_service.service.impl;

import com.zynolo_nexus.po_service.dto.request.PurchaseOrderFilterRequest;
import com.zynolo_nexus.po_service.dto.request.PurchaseOrderReferenceDataRequest;
import com.zynolo_nexus.po_service.dto.request.PurchaseOrderSendRequest;
import com.zynolo_nexus.po_service.dto.request.PurchaseOrderUpdateRequest;
import com.zynolo_nexus.po_service.dto.request.PurchaseOrderViewRequest;
import com.zynolo_nexus.po_service.dto.response.PurchaseOrderDto;
import com.zynolo_nexus.po_service.dto.response.PurchaseOrderFilterResultDto;
import com.zynolo_nexus.po_service.dto.response.PurchaseOrderPrivilegesDto;
import com.zynolo_nexus.po_service.dto.response.PurchaseOrderReferenceDataDto;
import com.zynolo_nexus.po_service.dto.response.ReferenceOptionDto;
import com.zynolo_nexus.po_service.enums.MasterStatus;
import com.zynolo_nexus.po_service.enums.PurchaseOrderStatus;
import com.zynolo_nexus.po_service.repository.CompanyRepository;
import com.zynolo_nexus.po_service.repository.CurrencyRepository;
import com.zynolo_nexus.po_service.repository.VendorRepository;
import com.zynolo_nexus.po_service.service.PurchaseOrderCreationService;
import com.zynolo_nexus.po_service.service.PurchaseOrderManagementService;
import com.zynolo_nexus.po_service.service.support.PagePrivilegeResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PurchaseOrderManagementServiceImpl implements PurchaseOrderManagementService {

    private static final String PAGE_CODE = "POMG";
    private static final Map<String, String> REQUEST_TYPES = new LinkedHashMap<>();

    static {
        REQUEST_TYPES.put("GOODS", "Goods");
        REQUEST_TYPES.put("SERVICES", "Services");
        REQUEST_TYPES.put("COMBINED", "Combined");
    }

    private final CompanyRepository companyRepository;
    private final VendorRepository vendorRepository;
    private final CurrencyRepository currencyRepository;
    private final PagePrivilegeResolver pagePrivilegeResolver;
    private final PurchaseOrderCreationService purchaseOrderCreationService;

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
                        option(PurchaseOrderStatus.SENT.name(), "Sent"),
                        option(PurchaseOrderStatus.VENDOR_CONFIRMED.name(), "Vendor Confirmed"),
                        option(PurchaseOrderStatus.PARTIALLY_CONFIRMED.name(), "Partially Confirmed"),
                        option(PurchaseOrderStatus.PARTIALLY_APPROVED.name(), "Partially Approved"),
                        option(PurchaseOrderStatus.VENDOR_REJECTED.name(), "Vendor Rejected"),
                        option(PurchaseOrderStatus.PARTIALLY_RECEIVED.name(), "Partially Received"),
                        option(PurchaseOrderStatus.RECEIVED.name(), "Received")
                ))
                .privileges(PurchaseOrderPrivilegesDto.builder()
                        .add(false)
                        .update(privileges.isUpdate())
                        .view(privileges.isView())
                        .search(privileges.isSearch())
                        .send(privileges.isSend())
                        .confirm(privileges.isConfirm())
                        .build())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PurchaseOrderFilterResultDto filterList(PurchaseOrderFilterRequest request) {
        return purchaseOrderCreationService.filterList(request);
    }

    @Override
    @Transactional(readOnly = true)
    public PurchaseOrderDto view(PurchaseOrderViewRequest request) {
        return purchaseOrderCreationService.view(request);
    }

    @Override
    @Transactional
    public PurchaseOrderDto update(PurchaseOrderUpdateRequest request) {
        return purchaseOrderCreationService.update(request);
    }

    @Override
    @Transactional
    public PurchaseOrderDto sendToVendor(PurchaseOrderSendRequest request) {
        return purchaseOrderCreationService.sendToVendor(request);
    }

    private ReferenceOptionDto option(String code, String description) {
        return ReferenceOptionDto.builder()
                .code(code)
                .description(description)
                .build();
    }
}
