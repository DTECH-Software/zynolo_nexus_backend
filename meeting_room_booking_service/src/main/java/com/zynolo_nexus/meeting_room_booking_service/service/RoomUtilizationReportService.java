package com.zynolo_nexus.meeting_room_booking_service.service;

import com.zynolo_nexus.meeting_room_booking_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.RoomUtilizationReportExportRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.RoomUtilizationReportFilterRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.RoomUtilizationReportReferenceDataRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.RoomUtilizationReportViewRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.RoomUtilizationReportDetailDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.RoomUtilizationReportExportDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.RoomUtilizationReportFilterResultDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.RoomUtilizationReportReferenceDataDto;

public interface RoomUtilizationReportService {
    MessageResponseDTO<RoomUtilizationReportReferenceDataDto> referenceData(RoomUtilizationReportReferenceDataRequest request);
    MessageResponseDTO<RoomUtilizationReportFilterResultDto> filterList(RoomUtilizationReportFilterRequest request);
    MessageResponseDTO<RoomUtilizationReportDetailDto> view(RoomUtilizationReportViewRequest request);
    MessageResponseDTO<RoomUtilizationReportExportDto> export(RoomUtilizationReportExportRequest request);
    byte[] exportExcelFile(RoomUtilizationReportExportRequest request);
    byte[] exportPdfFile(RoomUtilizationReportExportRequest request);
    byte[] exportCsvFile(RoomUtilizationReportExportRequest request);
}
