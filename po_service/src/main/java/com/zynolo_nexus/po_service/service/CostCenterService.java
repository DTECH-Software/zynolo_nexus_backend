package com.zynolo_nexus.po_service.service;

import com.zynolo_nexus.po_service.dto.request.CostCenterCreateRequest;
import com.zynolo_nexus.po_service.dto.request.CostCenterFilterRequest;
import com.zynolo_nexus.po_service.dto.request.CostCenterReferenceDataRequest;
import com.zynolo_nexus.po_service.dto.request.CostCenterStatusRequest;
import com.zynolo_nexus.po_service.dto.request.CostCenterUpdateRequest;
import com.zynolo_nexus.po_service.dto.request.CostCenterViewRequest;
import com.zynolo_nexus.po_service.dto.response.CostCenterDto;
import com.zynolo_nexus.po_service.dto.response.CostCenterFilterResultDto;
import com.zynolo_nexus.po_service.dto.response.CostCenterReferenceDataDto;

public interface CostCenterService {

    CostCenterReferenceDataDto getReferenceData(CostCenterReferenceDataRequest request);

    CostCenterDto create(CostCenterCreateRequest request);

    CostCenterDto view(CostCenterViewRequest request);

    CostCenterDto update(CostCenterUpdateRequest request);

    CostCenterDto updateStatus(CostCenterStatusRequest request);

    CostCenterFilterResultDto filterList(CostCenterFilterRequest request);
}
