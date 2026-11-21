package com.zynolo_nexus.fleet_service.service;

import com.zynolo_nexus.fleet_service.dto.request.TripStatusFilterRequest;
import com.zynolo_nexus.fleet_service.dto.request.TripStatusReferenceDataRequest;
import com.zynolo_nexus.fleet_service.dto.request.TripStatusUpdateRequest;
import com.zynolo_nexus.fleet_service.dto.request.TripStatusViewRequest;
import com.zynolo_nexus.fleet_service.dto.response.TripDto;
import com.zynolo_nexus.fleet_service.dto.response.TripFilterResultDto;
import com.zynolo_nexus.fleet_service.dto.response.TripStatusReferenceDataDto;

public interface TripStatusUpdateService {

    TripStatusReferenceDataDto getReferenceData(TripStatusReferenceDataRequest request);

    TripFilterResultDto filterList(TripStatusFilterRequest request);

    TripDto view(TripStatusViewRequest request);

    TripDto updateStatus(TripStatusUpdateRequest request);
}