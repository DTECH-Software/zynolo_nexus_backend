package com.zynolo_nexus.po_service.service;

import com.zynolo_nexus.po_service.dto.request.VendorProductCreateRequest;
import com.zynolo_nexus.po_service.dto.request.VendorProductFilterRequest;
import com.zynolo_nexus.po_service.dto.request.VendorProductReferenceDataRequest;
import com.zynolo_nexus.po_service.dto.request.VendorProductStatusRequest;
import com.zynolo_nexus.po_service.dto.request.VendorProductUpdateRequest;
import com.zynolo_nexus.po_service.dto.request.VendorProductViewRequest;
import com.zynolo_nexus.po_service.dto.response.VendorProductDto;
import com.zynolo_nexus.po_service.dto.response.VendorProductFilterResultDto;
import com.zynolo_nexus.po_service.dto.response.VendorProductReferenceDataDto;

public interface VendorProductMappingService {

    VendorProductReferenceDataDto getReferenceData(VendorProductReferenceDataRequest request);

    VendorProductDto create(VendorProductCreateRequest request);

    VendorProductDto view(VendorProductViewRequest request);

    VendorProductDto update(VendorProductUpdateRequest request);

    VendorProductDto updateStatus(VendorProductStatusRequest request);

    VendorProductFilterResultDto filterList(VendorProductFilterRequest request);
}
