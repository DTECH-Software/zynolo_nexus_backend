package com.zynolo_nexus.meeting_room_booking_service.controller;

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
import com.zynolo_nexus.meeting_room_booking_service.service.MeetingInvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/v1/meeting-room/invoices")
@RequiredArgsConstructor
public class MeetingInvoiceController {

    private final MeetingInvoiceService meetingInvoiceService;

    @PostMapping("/reference-data")
    public MessageResponseDTO<MeetingInvoiceReferenceDataDto> referenceData(
            @RequestBody(required = false) MeetingInvoiceReferenceDataRequest request) {
        return meetingInvoiceService.referenceData(request);
    }

    @PostMapping("/filter-list")
    public MessageResponseDTO<MeetingInvoiceFilterResultDto> filterList(@RequestBody MeetingInvoiceFilterRequest request) {
        return meetingInvoiceService.filterList(request);
    }

    @PostMapping("/details/reference-data")
    public MessageResponseDTO<MeetingInvoiceDetailReferenceDataDto> detailReferenceData(
            @RequestBody(required = false) MeetingInvoiceReferenceDataRequest request) {
        return meetingInvoiceService.detailReferenceData(request);
    }

    @PostMapping("/details/filter-list")
    public MessageResponseDTO<MeetingInvoiceFilterResultDto> readyFilterList(@RequestBody MeetingInvoiceFilterRequest request) {
        return meetingInvoiceService.readyFilterList(request);
    }

    @PostMapping("/preview")
    public MessageResponseDTO<MeetingInvoicePreviewDto> preview(@RequestBody MeetingInvoicePreviewRequest request) {
        return meetingInvoiceService.preview(request);
    }

    @PostMapping("/generate")
    public MessageResponseDTO<MeetingInvoiceDto> generate(@RequestBody MeetingInvoiceGenerateRequest request) {
        return meetingInvoiceService.generate(request);
    }

    @PostMapping("/view")
    public MessageResponseDTO<MeetingInvoiceDto> view(@RequestBody MeetingInvoiceViewRequest request) {
        return meetingInvoiceService.view(request);
    }

    @PostMapping("/download-pdf")
    public ResponseEntity<byte[]> downloadPdf(@RequestBody MeetingInvoiceDownloadRequest request) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"meeting_invoice_" + timestamp() + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(meetingInvoiceService.downloadPdf(request));
    }

    private String timestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }
}
