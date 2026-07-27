package com.zynolo_nexus.meeting_room_booking_service.service;

import com.zynolo_nexus.meeting_room_booking_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingInvoiceReportExportRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingInvoiceReportFilterRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingInvoiceReportReferenceDataRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingInvoiceReportViewRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingInvoiceDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingInvoiceReportExportDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingInvoiceReportFilterResultDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingInvoiceReportReferenceDataDto;

public interface MeetingInvoiceReportService {
    MessageResponseDTO<MeetingInvoiceReportReferenceDataDto> referenceData(MeetingInvoiceReportReferenceDataRequest request);
    MessageResponseDTO<MeetingInvoiceReportFilterResultDto> filterList(MeetingInvoiceReportFilterRequest request);
    MessageResponseDTO<MeetingInvoiceDto> view(MeetingInvoiceReportViewRequest request);
    MessageResponseDTO<MeetingInvoiceReportExportDto> export(MeetingInvoiceReportExportRequest request);
    byte[] exportExcelFile(MeetingInvoiceReportExportRequest request);
    byte[] exportPdfFile(MeetingInvoiceReportExportRequest request);
    byte[] exportCsvFile(MeetingInvoiceReportExportRequest request);
}
