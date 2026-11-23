package com.zynolo_nexus.fleet_service.service;

import com.zynolo_nexus.fleet_service.dto.request.AssignTripCreateRequest;
import com.zynolo_nexus.fleet_service.dto.request.AssignTripReferenceDataRequest;
import com.zynolo_nexus.fleet_service.dto.request.AssignTripUpdateRequest;
import com.zynolo_nexus.fleet_service.dto.request.AssignTripViewRequest;
import com.zynolo_nexus.fleet_service.dto.request.TransportRequestFilterRequest;
import com.zynolo_nexus.fleet_service.dto.request.TransportRequestViewRequest;
import com.zynolo_nexus.fleet_service.dto.response.AssignTripReferenceDataDto;
import com.zynolo_nexus.fleet_service.dto.response.TransportRequestDto;
import com.zynolo_nexus.fleet_service.dto.response.TransportRequestFilterResultDto;
import com.zynolo_nexus.fleet_service.dto.response.TripDto;

public interface AssignTripService {

    AssignTripReferenceDataDto getReferenceData(AssignTripReferenceDataRequest request);

    TransportRequestFilterResultDto filterOpenRequests(TransportRequestFilterRequest request);

    TransportRequestDto viewRequest(TransportRequestViewRequest request);

    TripDto assign(AssignTripCreateRequest request);

    TripDto view(AssignTripViewRequest request);

    TripDto update(AssignTripUpdateRequest request);
}