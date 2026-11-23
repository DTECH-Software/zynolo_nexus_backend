package com.zynolo_nexus.fleet_service.service;

import com.zynolo_nexus.fleet_service.dto.request.TodayDashboardReferenceDataRequest;
import com.zynolo_nexus.fleet_service.dto.request.TodayDashboardViewRequest;
import com.zynolo_nexus.fleet_service.dto.response.TodayDashboardDto;
import com.zynolo_nexus.fleet_service.dto.response.TodayDashboardReferenceDataDto;

public interface TodayDashboardService {

    TodayDashboardReferenceDataDto getReferenceData(TodayDashboardReferenceDataRequest request);

    TodayDashboardDto view(TodayDashboardViewRequest request);
}
