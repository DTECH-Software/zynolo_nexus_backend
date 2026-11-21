package com.zynolo_nexus.fleet_service.service;

import com.zynolo_nexus.fleet_service.dto.request.RequestQueueReferenceDataRequest;
import com.zynolo_nexus.fleet_service.dto.request.RequestQueueUpdateRequest;
import com.zynolo_nexus.fleet_service.dto.request.TransportRequestFilterRequest;
import com.zynolo_nexus.fleet_service.dto.request.TransportRequestViewRequest;
import com.zynolo_nexus.fleet_service.dto.response.RequestQueueReferenceDataDto;
import com.zynolo_nexus.fleet_service.dto.response.TransportRequestDto;
import com.zynolo_nexus.fleet_service.dto.response.TransportRequestFilterResultDto;

public interface RequestQueueService {

    RequestQueueReferenceDataDto getReferenceData(RequestQueueReferenceDataRequest request);

    TransportRequestFilterResultDto filterList(TransportRequestFilterRequest request);

    TransportRequestDto view(TransportRequestViewRequest request);

    TransportRequestDto update(RequestQueueUpdateRequest request);
}