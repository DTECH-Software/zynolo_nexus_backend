package com.zynolo_nexus.fleet_service.service;

import com.zynolo_nexus.fleet_service.dto.request.TripFareCreateRequest;
import com.zynolo_nexus.fleet_service.dto.request.TripFareFilterRequest;
import com.zynolo_nexus.fleet_service.dto.request.TripFareReferenceDataRequest;
import com.zynolo_nexus.fleet_service.dto.request.TripFareUpdateRequest;
import com.zynolo_nexus.fleet_service.dto.request.TripFareViewRequest;
import com.zynolo_nexus.fleet_service.dto.response.TripFareDto;
import com.zynolo_nexus.fleet_service.dto.response.TripFareFilterResultDto;
import com.zynolo_nexus.fleet_service.dto.response.TripFareReferenceDataDto;

public interface TripFareService {

    TripFareReferenceDataDto getReferenceData(TripFareReferenceDataRequest request);

    TripFareFilterResultDto filterList(TripFareFilterRequest request);

    TripFareDto create(TripFareCreateRequest request);

    TripFareDto view(TripFareViewRequest request);

    TripFareDto update(TripFareUpdateRequest request);
}
