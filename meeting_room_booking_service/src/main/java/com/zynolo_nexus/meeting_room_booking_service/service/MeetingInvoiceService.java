package com.zynolo_nexus.meeting_room_booking_service.service;

import com.zynolo_nexus.meeting_room_booking_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingInvoiceDownloadRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingInvoiceFilterRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingInvoiceGenerateRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingInvoicePreviewRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingInvoiceReferenceDataRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingInvoiceViewRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingInvoiceDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingInvoiceDetailReferenceDataDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingInvoiceFilterResultDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingInvoicePreviewDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingInvoiceReferenceDataDto;

public interface MeetingInvoiceService {
    MessageResponseDTO<MeetingInvoiceReferenceDataDto> referenceData(MeetingInvoiceReferenceDataRequest request);
    MessageResponseDTO<MeetingInvoiceDetailReferenceDataDto> detailReferenceData(MeetingInvoiceReferenceDataRequest request);
    MessageResponseDTO<MeetingInvoiceFilterResultDto> filterList(MeetingInvoiceFilterRequest request);
    MessageResponseDTO<MeetingInvoiceFilterResultDto> readyFilterList(MeetingInvoiceFilterRequest request);
    MessageResponseDTO<MeetingInvoicePreviewDto> preview(MeetingInvoicePreviewRequest request);
    MessageResponseDTO<MeetingInvoiceDto> generate(MeetingInvoiceGenerateRequest request);
    MessageResponseDTO<MeetingInvoiceDto> view(MeetingInvoiceViewRequest request);
    byte[] downloadPdf(MeetingInvoiceDownloadRequest request);
}
