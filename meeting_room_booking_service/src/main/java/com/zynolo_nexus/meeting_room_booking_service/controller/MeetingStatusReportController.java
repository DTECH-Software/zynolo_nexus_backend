package com.zynolo_nexus.meeting_room_booking_service.controller;

import com.zynolo_nexus.meeting_room_booking_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingStatusReportExportRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingStatusReportFilterRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingStatusReportReferenceDataRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingStatusReportViewRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingStatusReportDetailDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingStatusReportExportDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingStatusReportFilterResultDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingStatusReportReferenceDataDto;
import com.zynolo_nexus.meeting_room_booking_service.service.MeetingStatusReportService;
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
@RequestMapping("/api/v1/meeting-room/reports/meeting-status-report")
@RequiredArgsConstructor
public class MeetingStatusReportController {

    private final MeetingStatusReportService meetingStatusReportService;

    @PostMapping("/reference-data")
    public MessageResponseDTO<MeetingStatusReportReferenceDataDto> referenceData(
            @RequestBody(required = false) MeetingStatusReportReferenceDataRequest request) {
        return meetingStatusReportService.referenceData(request);
    }

    @PostMapping("/filter-list")
    public MessageResponseDTO<MeetingStatusReportFilterResultDto> filterList(
            @RequestBody MeetingStatusReportFilterRequest request) {
        return meetingStatusReportService.filterList(request);
    }

    @PostMapping("/view")
    public MessageResponseDTO<MeetingStatusReportDetailDto> view(@RequestBody MeetingStatusReportViewRequest request) {
        return meetingStatusReportService.view(request);
    }

    @PostMapping("/export")
    public MessageResponseDTO<MeetingStatusReportExportDto> export(@RequestBody MeetingStatusReportExportRequest request) {
        return meetingStatusReportService.export(request);
    }

    @PostMapping("/export-excel")
    public ResponseEntity<byte[]> exportExcel(@RequestBody MeetingStatusReportExportRequest request) {
        return fileResponse(meetingStatusReportService.exportExcelFile(request),
                "meeting_status_report_" + timestamp() + ".xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    }

    @PostMapping("/export-pdf")
    public ResponseEntity<byte[]> exportPdf(@RequestBody MeetingStatusReportExportRequest request) {
        return fileResponse(meetingStatusReportService.exportPdfFile(request),
                "meeting_status_report_" + timestamp() + ".pdf",
                MediaType.APPLICATION_PDF_VALUE);
    }

    @PostMapping("/export-csv")
    public ResponseEntity<byte[]> exportCsv(@RequestBody MeetingStatusReportExportRequest request) {
        return fileResponse(meetingStatusReportService.exportCsvFile(request),
                "meeting_status_report_" + timestamp() + ".csv",
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
