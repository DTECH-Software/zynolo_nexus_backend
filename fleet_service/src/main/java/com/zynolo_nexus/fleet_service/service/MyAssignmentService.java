package com.zynolo_nexus.fleet_service.service;

import com.zynolo_nexus.fleet_service.dto.request.MyAssignmentFilterRequest;
import com.zynolo_nexus.fleet_service.dto.request.MyAssignmentReferenceDataRequest;
import com.zynolo_nexus.fleet_service.dto.request.MyAssignmentViewRequest;
import com.zynolo_nexus.fleet_service.dto.response.MyAssignmentReferenceDataDto;
import com.zynolo_nexus.fleet_service.dto.response.TripDto;
import com.zynolo_nexus.fleet_service.dto.response.TripFilterResultDto;

public interface MyAssignmentService {

    MyAssignmentReferenceDataDto getReferenceData(MyAssignmentReferenceDataRequest request);

    TripFilterResultDto filterList(MyAssignmentFilterRequest request);

    TripDto view(MyAssignmentViewRequest request);
}