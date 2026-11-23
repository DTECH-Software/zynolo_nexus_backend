package com.zynolo_nexus.meeting_room_booking_service.service;

import com.zynolo_nexus.meeting_room_booking_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.BookingReportExportRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.BookingReportFilterRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.BookingReportReferenceDataRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.BookingReportViewRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.BookingReportDetailDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.BookingReportExportDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.BookingReportFilterResultDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.BookingReportReferenceDataDto;

public interface BookingReportService {
    MessageResponseDTO<BookingReportReferenceDataDto> referenceData(BookingReportReferenceDataRequest request);
    MessageResponseDTO<BookingReportFilterResultDto> filterList(BookingReportFilterRequest request);
    MessageResponseDTO<BookingReportDetailDto> view(BookingReportViewRequest request);
    MessageResponseDTO<BookingReportExportDto> export(BookingReportExportRequest request);
    byte[] exportExcelFile(BookingReportExportRequest request);
    byte[] exportPdfFile(BookingReportExportRequest request);
    byte[] exportCsvFile(BookingReportExportRequest request);
}
