package com.zynolo_nexus.fleet_service.service;

import com.zynolo_nexus.fleet_service.dto.request.OutstandingFilterRequest;
import com.zynolo_nexus.fleet_service.dto.request.OutstandingReferenceDataRequest;
import com.zynolo_nexus.fleet_service.dto.request.OutstandingViewRequest;
import com.zynolo_nexus.fleet_service.dto.response.OutstandingDto;
import com.zynolo_nexus.fleet_service.dto.response.OutstandingFilterResultDto;
import com.zynolo_nexus.fleet_service.dto.response.OutstandingReferenceDataDto;

public interface OutstandingService {

    OutstandingReferenceDataDto getReferenceData(OutstandingReferenceDataRequest request);

    OutstandingFilterResultDto filterList(OutstandingFilterRequest request);

    OutstandingDto view(OutstandingViewRequest request);
}
