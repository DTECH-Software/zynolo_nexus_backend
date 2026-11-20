package com.zynolo_nexus.fleet_service.service;

import com.zynolo_nexus.fleet_service.dto.request.VehicleCreateRequest;
import com.zynolo_nexus.fleet_service.dto.request.VehicleFilterRequest;
import com.zynolo_nexus.fleet_service.dto.request.VehicleReferenceDataRequest;
import com.zynolo_nexus.fleet_service.dto.request.VehicleStatusUpdateRequest;
import com.zynolo_nexus.fleet_service.dto.request.VehicleUpdateRequest;
import com.zynolo_nexus.fleet_service.dto.request.VehicleViewRequest;
import com.zynolo_nexus.fleet_service.dto.response.VehicleDto;
import com.zynolo_nexus.fleet_service.dto.response.VehicleFilterResultDto;
import com.zynolo_nexus.fleet_service.dto.response.VehicleReferenceDataDto;

public interface VehicleService {

    VehicleReferenceDataDto getReferenceData(VehicleReferenceDataRequest request);

    VehicleDto create(VehicleCreateRequest request);

    VehicleDto view(VehicleViewRequest request);

    VehicleDto update(VehicleUpdateRequest request);

    VehicleDto updateStatus(VehicleStatusUpdateRequest request);

    VehicleFilterResultDto filterList(VehicleFilterRequest request);
}
