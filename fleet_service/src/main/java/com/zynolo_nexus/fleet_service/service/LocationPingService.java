package com.zynolo_nexus.fleet_service.service;

import com.zynolo_nexus.fleet_service.dto.request.LocationPingCreateRequest;
import com.zynolo_nexus.fleet_service.dto.request.LocationPingReferenceDataRequest;
import com.zynolo_nexus.fleet_service.dto.response.LocationPingDto;
import com.zynolo_nexus.fleet_service.dto.response.LocationPingReferenceDataDto;

public interface LocationPingService {

    LocationPingReferenceDataDto getReferenceData(LocationPingReferenceDataRequest request);

    LocationPingDto ping(LocationPingCreateRequest request);
}