package com.zynolo_nexus.po_service.service;

import com.zynolo_nexus.po_service.dto.request.PoReferenceDataRequest;
import com.zynolo_nexus.po_service.dto.request.PoRequestFilterRequest;
import com.zynolo_nexus.po_service.dto.request.PoRequestViewRequest;
import com.zynolo_nexus.po_service.dto.response.PoRequestDto;
import com.zynolo_nexus.po_service.dto.response.PoRequestFilterResultDto;
import com.zynolo_nexus.po_service.dto.response.PoRequestReferenceDataDto;

public interface PoRequestManagementService {

    PoRequestReferenceDataDto getReferenceData(PoReferenceDataRequest request);

    PoRequestDto view(PoRequestViewRequest request);

    PoRequestFilterResultDto filterList(PoRequestFilterRequest request);
}
