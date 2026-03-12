package com.zynolo_nexus.po_service.service;

import com.zynolo_nexus.po_service.dto.request.PurchaseOrderFilterRequest;
import com.zynolo_nexus.po_service.dto.request.PurchaseOrderReferenceDataRequest;
import com.zynolo_nexus.po_service.dto.request.PurchaseOrderSendRequest;
import com.zynolo_nexus.po_service.dto.request.PurchaseOrderUpdateRequest;
import com.zynolo_nexus.po_service.dto.request.PurchaseOrderViewRequest;
import com.zynolo_nexus.po_service.dto.response.PurchaseOrderDto;
import com.zynolo_nexus.po_service.dto.response.PurchaseOrderFilterResultDto;
import com.zynolo_nexus.po_service.dto.response.PurchaseOrderReferenceDataDto;

public interface PurchaseOrderManagementService {

    PurchaseOrderReferenceDataDto getReferenceData(PurchaseOrderReferenceDataRequest request);

    PurchaseOrderFilterResultDto filterList(PurchaseOrderFilterRequest request);

    PurchaseOrderDto view(PurchaseOrderViewRequest request);

    PurchaseOrderDto update(PurchaseOrderUpdateRequest request);

    PurchaseOrderDto sendToVendor(PurchaseOrderSendRequest request);
}
