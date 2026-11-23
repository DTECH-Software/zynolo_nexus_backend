package com.zynolo_nexus.fleet_service.service;

import com.zynolo_nexus.fleet_service.dto.request.TodayScheduleFilterRequest;
import com.zynolo_nexus.fleet_service.dto.request.TodayScheduleReferenceDataRequest;
import com.zynolo_nexus.fleet_service.dto.request.TodayScheduleUpdateRequest;
import com.zynolo_nexus.fleet_service.dto.request.TodayScheduleViewRequest;
import com.zynolo_nexus.fleet_service.dto.response.TodayScheduleReferenceDataDto;
import com.zynolo_nexus.fleet_service.dto.response.TripDto;
import com.zynolo_nexus.fleet_service.dto.response.TripFilterResultDto;

public interface TodayScheduleService {

    TodayScheduleReferenceDataDto getReferenceData(TodayScheduleReferenceDataRequest request);

    TripFilterResultDto filterList(TodayScheduleFilterRequest request);

    TripDto view(TodayScheduleViewRequest request);

    TripDto update(TodayScheduleUpdateRequest request);
}