package com.zynolo_nexus.fleet_service.service;

import com.zynolo_nexus.fleet_service.dto.request.MyTripFilterRequest;
import com.zynolo_nexus.fleet_service.dto.request.MyTripReferenceDataRequest;
import com.zynolo_nexus.fleet_service.dto.request.MyTripViewRequest;
import com.zynolo_nexus.fleet_service.dto.response.MyTripReferenceDataDto;
import com.zynolo_nexus.fleet_service.dto.response.TripDto;
import com.zynolo_nexus.fleet_service.dto.response.TripFilterResultDto;

public interface MyTripService {

    MyTripReferenceDataDto getReferenceData(MyTripReferenceDataRequest request);

    TripFilterResultDto filterList(MyTripFilterRequest request);

    TripDto view(MyTripViewRequest request);
}
