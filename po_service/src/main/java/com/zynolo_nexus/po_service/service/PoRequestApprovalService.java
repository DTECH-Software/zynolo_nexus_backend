package com.zynolo_nexus.po_service.service;

import com.zynolo_nexus.po_service.dto.request.PoReferenceDataRequest;
import com.zynolo_nexus.po_service.dto.request.PoApprovalVendorProductsRequest;
import com.zynolo_nexus.po_service.dto.request.PoRequestApproveRequest;
import com.zynolo_nexus.po_service.dto.request.PoRequestFilterRequest;
import com.zynolo_nexus.po_service.dto.request.PoRequestRejectRequest;
import com.zynolo_nexus.po_service.dto.request.PoRequestViewRequest;
import com.zynolo_nexus.po_service.dto.response.PoRequestDto;
import com.zynolo_nexus.po_service.dto.response.PoRequestFilterResultDto;
import com.zynolo_nexus.po_service.dto.response.PoRequestReferenceDataDto;
import com.zynolo_nexus.po_service.dto.response.PoApprovalVendorProductDto;

import java.util.List;

public interface PoRequestApprovalService {

    PoRequestReferenceDataDto getReferenceData(PoReferenceDataRequest request);

    PoRequestFilterResultDto filterList(PoRequestFilterRequest request);

    PoRequestDto view(PoRequestViewRequest request);

    List<PoApprovalVendorProductDto> getVendorProducts(PoApprovalVendorProductsRequest request);

    PoRequestDto approve(PoRequestApproveRequest request);

    PoRequestDto reject(PoRequestRejectRequest request);
}
