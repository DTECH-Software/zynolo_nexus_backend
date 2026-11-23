package com.zynolo_nexus.po_service.service;

import com.zynolo_nexus.po_service.dto.request.ThreeWayMatchExecuteRequest;
import com.zynolo_nexus.po_service.dto.request.ThreeWayMatchFilterRequest;
import com.zynolo_nexus.po_service.dto.request.ThreeWayMatchReferenceDataRequest;
import com.zynolo_nexus.po_service.dto.request.ThreeWayMatchViewRequest;
import com.zynolo_nexus.po_service.dto.response.ThreeWayMatchFilterResultDto;
import com.zynolo_nexus.po_service.dto.response.ThreeWayMatchReferenceDataDto;
import com.zynolo_nexus.po_service.dto.response.ThreeWayMatchViewDto;

public interface ThreeWayMatchService {

    ThreeWayMatchReferenceDataDto getReferenceData(ThreeWayMatchReferenceDataRequest request);

    ThreeWayMatchFilterResultDto filterList(ThreeWayMatchFilterRequest request);

    ThreeWayMatchViewDto view(ThreeWayMatchViewRequest request);

    ThreeWayMatchViewDto match(ThreeWayMatchExecuteRequest request);
}
