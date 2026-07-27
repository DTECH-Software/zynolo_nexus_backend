package com.zynolo_nexus.meeting_room_booking_service.controller;

import com.zynolo_nexus.meeting_room_booking_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingInvoiceReportExportRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingInvoiceReportFilterRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingInvoiceReportReferenceDataRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingInvoiceReportViewRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingInvoiceDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingInvoiceReportExportDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingInvoiceReportFilterResultDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingInvoiceReportReferenceDataDto;
import com.zynolo_nexus.meeting_room_booking_service.service.MeetingInvoiceReportService;
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
@RequestMapping("/api/v1/meeting-room/reports/invoice-report")
@RequiredArgsConstructor
public class MeetingInvoiceReportController {

    private final MeetingInvoiceReportService meetingInvoiceReportService;

    @PostMapping("/reference-data")
    public MessageResponseDTO<MeetingInvoiceReportReferenceDataDto> referenceData(
            @RequestBody(required = false) MeetingInvoiceReportReferenceDataRequest request) {
        return meetingInvoiceReportService.referenceData(request);
    }

    @PostMapping("/filter-list")
    public MessageResponseDTO<MeetingInvoiceReportFilterResultDto> filterList(@RequestBody MeetingInvoiceReportFilterRequest request) {
        return meetingInvoiceReportService.filterList(request);
    }

    @PostMapping("/view")
    public MessageResponseDTO<MeetingInvoiceDto> view(@RequestBody MeetingInvoiceReportViewRequest request) {
        return meetingInvoiceReportService.view(request);
    }

    @PostMapping("/export")
    public MessageResponseDTO<MeetingInvoiceReportExportDto> export(@RequestBody MeetingInvoiceReportExportRequest request) {
        return meetingInvoiceReportService.export(request);
    }

    @PostMapping("/export-excel")
    public ResponseEntity<byte[]> exportExcel(@RequestBody MeetingInvoiceReportExportRequest request) {
        return fileResponse(meetingInvoiceReportService.exportExcelFile(request),
                "meeting_invoice_report_" + timestamp() + ".xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    }

    @PostMapping("/export-pdf")
    public ResponseEntity<byte[]> exportPdf(@RequestBody MeetingInvoiceReportExportRequest request) {
        return fileResponse(meetingInvoiceReportService.exportPdfFile(request),
                "meeting_invoice_report_" + timestamp() + ".pdf",
                MediaType.APPLICATION_PDF_VALUE);
    }

    @PostMapping("/export-csv")
    public ResponseEntity<byte[]> exportCsv(@RequestBody MeetingInvoiceReportExportRequest request) {
        return fileResponse(meetingInvoiceReportService.exportCsvFile(request),
                "meeting_invoice_report_" + timestamp() + ".csv",
                "text/csv");
    }

    private ResponseEntity<byte[]> fileResponse(byte[] data, String filename, String contentType) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .body(data);
    }

    private String timestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }
}
