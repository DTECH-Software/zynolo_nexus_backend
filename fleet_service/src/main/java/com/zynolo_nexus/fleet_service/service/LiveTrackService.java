package com.zynolo_nexus.fleet_service.service;

import com.zynolo_nexus.fleet_service.dto.request.LiveTrackFilterRequest;
import com.zynolo_nexus.fleet_service.dto.request.LiveTrackReferenceDataRequest;
import com.zynolo_nexus.fleet_service.dto.request.LiveTrackViewRequest;
import com.zynolo_nexus.fleet_service.dto.response.LiveTrackDto;
import com.zynolo_nexus.fleet_service.dto.response.LiveTrackFilterResultDto;
import com.zynolo_nexus.fleet_service.dto.response.LiveTrackReferenceDataDto;

public interface LiveTrackService {

    LiveTrackReferenceDataDto getReferenceData(LiveTrackReferenceDataRequest request);

    LiveTrackFilterResultDto filterList(LiveTrackFilterRequest request);

    LiveTrackDto view(LiveTrackViewRequest request);
}