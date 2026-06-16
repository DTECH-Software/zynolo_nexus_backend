package com.zynolo_nexus.meeting_room_booking_service.service;

import com.zynolo_nexus.meeting_room_booking_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingStatusReportExportRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingStatusReportFilterRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingStatusReportReferenceDataRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingStatusReportViewRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingStatusReportDetailDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingStatusReportExportDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingStatusReportFilterResultDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingStatusReportReferenceDataDto;

public interface MeetingStatusReportService {
    MessageResponseDTO<MeetingStatusReportReferenceDataDto> referenceData(MeetingStatusReportReferenceDataRequest request);
    MessageResponseDTO<MeetingStatusReportFilterResultDto> filterList(MeetingStatusReportFilterRequest request);
    MessageResponseDTO<MeetingStatusReportDetailDto> view(MeetingStatusReportViewRequest request);
    MessageResponseDTO<MeetingStatusReportExportDto> export(MeetingStatusReportExportRequest request);
    byte[] exportExcelFile(MeetingStatusReportExportRequest request);
    byte[] exportPdfFile(MeetingStatusReportExportRequest request);
    byte[] exportCsvFile(MeetingStatusReportExportRequest request);
}
