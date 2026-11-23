package com.zynolo_nexus.fleet_service.service;

import com.zynolo_nexus.fleet_service.dto.request.DriverCreateRequest;
import com.zynolo_nexus.fleet_service.dto.request.DriverFilterRequest;
import com.zynolo_nexus.fleet_service.dto.request.DriverReferenceDataRequest;
import com.zynolo_nexus.fleet_service.dto.request.DriverStatusUpdateRequest;
import com.zynolo_nexus.fleet_service.dto.request.DriverUpdateRequest;
import com.zynolo_nexus.fleet_service.dto.request.DriverViewRequest;
import com.zynolo_nexus.fleet_service.dto.response.DriverDto;
import com.zynolo_nexus.fleet_service.dto.response.DriverFilterResultDto;
import com.zynolo_nexus.fleet_service.dto.response.DriverReferenceDataDto;

public interface DriverService {

    DriverReferenceDataDto getReferenceData(DriverReferenceDataRequest request);

    DriverDto create(DriverCreateRequest request);

    DriverDto view(DriverViewRequest request);

    DriverDto update(DriverUpdateRequest request);

    DriverDto updateStatus(DriverStatusUpdateRequest request);

    DriverFilterResultDto filterList(DriverFilterRequest request);
}