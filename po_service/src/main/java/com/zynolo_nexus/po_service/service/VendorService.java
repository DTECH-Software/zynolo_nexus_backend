package com.zynolo_nexus.po_service.service;

import com.zynolo_nexus.po_service.dto.request.VendorCreateRequest;
import com.zynolo_nexus.po_service.dto.request.VendorFilterRequest;
import com.zynolo_nexus.po_service.dto.request.VendorReferenceDataRequest;
import com.zynolo_nexus.po_service.dto.request.VendorStatusRequest;
import com.zynolo_nexus.po_service.dto.request.VendorUpdateRequest;
import com.zynolo_nexus.po_service.dto.request.VendorViewRequest;
import com.zynolo_nexus.po_service.dto.response.VendorDto;
import com.zynolo_nexus.po_service.dto.response.VendorFilterResultDto;
import com.zynolo_nexus.po_service.dto.response.VendorReferenceDataDto;

public interface VendorService {

    VendorReferenceDataDto getReferenceData(VendorReferenceDataRequest request);

    VendorDto create(VendorCreateRequest request);

    VendorDto view(VendorViewRequest request);

    VendorDto update(VendorUpdateRequest request);

    VendorDto updateStatus(VendorStatusRequest request);

    VendorFilterResultDto filterList(VendorFilterRequest request);
}
