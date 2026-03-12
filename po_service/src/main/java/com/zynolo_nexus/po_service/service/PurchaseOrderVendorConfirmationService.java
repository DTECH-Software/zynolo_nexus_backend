package com.zynolo_nexus.po_service.service;

import com.zynolo_nexus.po_service.dto.request.PurchaseOrderFilterRequest;
import com.zynolo_nexus.po_service.dto.request.PurchaseOrderReferenceDataRequest;
import com.zynolo_nexus.po_service.dto.request.PurchaseOrderVendorConfirmRequest;
import com.zynolo_nexus.po_service.dto.request.PurchaseOrderViewRequest;
import com.zynolo_nexus.po_service.dto.response.PurchaseOrderDto;
import com.zynolo_nexus.po_service.dto.response.PurchaseOrderFilterResultDto;
import com.zynolo_nexus.po_service.dto.response.PurchaseOrderReferenceDataDto;

public interface PurchaseOrderVendorConfirmationService {

    PurchaseOrderReferenceDataDto getReferenceData(PurchaseOrderReferenceDataRequest request);

    PurchaseOrderFilterResultDto filterList(PurchaseOrderFilterRequest request);

    PurchaseOrderDto view(PurchaseOrderViewRequest request);

    PurchaseOrderDto confirm(PurchaseOrderVendorConfirmRequest request);
}
