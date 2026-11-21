package com.zynolo_nexus.fleet_service.service;

import com.zynolo_nexus.fleet_service.dto.request.IncidentCreateRequest;
import com.zynolo_nexus.fleet_service.dto.request.IncidentFilterRequest;
import com.zynolo_nexus.fleet_service.dto.request.IncidentReferenceDataRequest;
import com.zynolo_nexus.fleet_service.dto.request.IncidentViewRequest;
import com.zynolo_nexus.fleet_service.dto.response.IncidentDto;
import com.zynolo_nexus.fleet_service.dto.response.IncidentFilterResultDto;
import com.zynolo_nexus.fleet_service.dto.response.IncidentReferenceDataDto;

public interface IncidentService {

    IncidentReferenceDataDto getReferenceData(IncidentReferenceDataRequest request);

    IncidentFilterResultDto filterList(IncidentFilterRequest request);

    IncidentDto create(IncidentCreateRequest request);

    IncidentDto view(IncidentViewRequest request);
}