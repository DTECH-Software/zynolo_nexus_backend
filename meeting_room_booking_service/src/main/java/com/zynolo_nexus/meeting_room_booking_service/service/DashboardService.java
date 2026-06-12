package com.zynolo_nexus.meeting_room_booking_service.service;

import com.zynolo_nexus.meeting_room_booking_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.DashboardOverviewRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.DashboardReferenceDataRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.DashboardOverviewDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.DashboardReferenceDataDto;

public interface DashboardService {
    MessageResponseDTO<DashboardReferenceDataDto> referenceData(DashboardReferenceDataRequest request);
    MessageResponseDTO<DashboardOverviewDto> overview(DashboardOverviewRequest request);
}
