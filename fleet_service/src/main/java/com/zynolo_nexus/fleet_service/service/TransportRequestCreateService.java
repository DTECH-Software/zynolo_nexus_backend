package com.zynolo_nexus.fleet_service.service;

import com.zynolo_nexus.fleet_service.dto.request.TransportRequestCreateRequest;
import com.zynolo_nexus.fleet_service.dto.request.TransportRequestFilterRequest;
import com.zynolo_nexus.fleet_service.dto.request.TransportRequestReferenceDataRequest;
import com.zynolo_nexus.fleet_service.dto.request.TransportRequestSubmitRequest;
import com.zynolo_nexus.fleet_service.dto.request.TransportRequestUpdateRequest;
import com.zynolo_nexus.fleet_service.dto.request.TransportRequestViewRequest;
import com.zynolo_nexus.fleet_service.dto.response.TransportRequestDto;
import com.zynolo_nexus.fleet_service.dto.response.TransportRequestFilterResultDto;
import com.zynolo_nexus.fleet_service.dto.response.TransportRequestReferenceDataDto;

public interface TransportRequestCreateService {

    TransportRequestReferenceDataDto getReferenceData(TransportRequestReferenceDataRequest request);

    TransportRequestDto create(TransportRequestCreateRequest request);

    TransportRequestDto view(TransportRequestViewRequest request);

    TransportRequestDto update(TransportRequestUpdateRequest request);

    TransportRequestDto submit(TransportRequestSubmitRequest request);

    TransportRequestFilterResultDto filterList(TransportRequestFilterRequest request);
}