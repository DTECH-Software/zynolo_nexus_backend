package com.zynolo_nexus.po_service.service;

import com.zynolo_nexus.po_service.dto.request.ApprovedPoRequestFilterRequest;
import com.zynolo_nexus.po_service.dto.request.PurchaseOrderCreateRequest;
import com.zynolo_nexus.po_service.dto.request.PurchaseOrderFilterRequest;
import com.zynolo_nexus.po_service.dto.request.PurchaseOrderReferenceDataRequest;
import com.zynolo_nexus.po_service.dto.request.PurchaseOrderRequestViewRequest;
import com.zynolo_nexus.po_service.dto.request.PurchaseOrderSendRequest;
import com.zynolo_nexus.po_service.dto.request.PurchaseOrderUpdateRequest;
import com.zynolo_nexus.po_service.dto.request.PurchaseOrderViewRequest;
import com.zynolo_nexus.po_service.dto.response.PoRequestDto;
import com.zynolo_nexus.po_service.dto.response.PoRequestFilterResultDto;
import com.zynolo_nexus.po_service.dto.response.PurchaseOrderDto;
import com.zynolo_nexus.po_service.dto.response.PurchaseOrderFilterResultDto;
import com.zynolo_nexus.po_service.dto.response.PurchaseOrderReferenceDataDto;

public interface PurchaseOrderCreationService {

    PurchaseOrderReferenceDataDto getReferenceData(PurchaseOrderReferenceDataRequest request);

    PoRequestFilterResultDto approvedRequests(ApprovedPoRequestFilterRequest request);

    PoRequestDto requestView(PurchaseOrderRequestViewRequest request);

    PurchaseOrderDto create(PurchaseOrderCreateRequest request);

    PurchaseOrderDto view(PurchaseOrderViewRequest request);

    PurchaseOrderDto update(PurchaseOrderUpdateRequest request);

    PurchaseOrderDto sendToVendor(PurchaseOrderSendRequest request);

    PurchaseOrderFilterResultDto filterList(PurchaseOrderFilterRequest request);
}