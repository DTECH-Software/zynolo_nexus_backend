package com.zynolo_nexus.fleet_service.service;

import com.zynolo_nexus.fleet_service.dto.request.TripDetailReferenceDataRequest;
import com.zynolo_nexus.fleet_service.dto.request.TripDetailViewRequest;
import com.zynolo_nexus.fleet_service.dto.response.TripDetailDto;
import com.zynolo_nexus.fleet_service.dto.response.TripDetailReferenceDataDto;

public interface TripDetailService {

    TripDetailReferenceDataDto getReferenceData(TripDetailReferenceDataRequest request);

    TripDetailDto view(TripDetailViewRequest request);
}
