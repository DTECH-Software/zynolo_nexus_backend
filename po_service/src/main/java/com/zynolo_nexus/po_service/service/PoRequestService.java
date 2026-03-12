package com.zynolo_nexus.po_service.service;

import com.zynolo_nexus.po_service.dto.request.PoReferenceDataRequest;
import com.zynolo_nexus.po_service.dto.request.PoRequestCreateRequest;
import com.zynolo_nexus.po_service.dto.request.PoRequestFilterRequest;
import com.zynolo_nexus.po_service.dto.request.PoRequestSubmitRequest;
import com.zynolo_nexus.po_service.dto.request.PoRequestUpdateRequest;
import com.zynolo_nexus.po_service.dto.request.PoRequestViewRequest;
import com.zynolo_nexus.po_service.dto.request.PoVendorProductsRequest;
import com.zynolo_nexus.po_service.dto.response.PoRequestDto;
import com.zynolo_nexus.po_service.dto.response.PoRequestFilterResultDto;
import com.zynolo_nexus.po_service.dto.response.PoRequestReferenceDataDto;
import com.zynolo_nexus.po_service.dto.response.PoVendorProductOptionDto;

import java.util.List;

public interface PoRequestService {

    PoRequestReferenceDataDto getReferenceData(PoReferenceDataRequest request);

    List<PoVendorProductOptionDto> getVendorProducts(PoVendorProductsRequest request);

    PoRequestDto create(PoRequestCreateRequest request);

    PoRequestDto view(PoRequestViewRequest request);

    PoRequestDto update(PoRequestUpdateRequest request);

    PoRequestDto submit(PoRequestSubmitRequest request);

    PoRequestFilterResultDto filterList(PoRequestFilterRequest request);
}
