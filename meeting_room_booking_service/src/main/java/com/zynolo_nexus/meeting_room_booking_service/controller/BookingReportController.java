package com.zynolo_nexus.meeting_room_booking_service.controller;

import com.zynolo_nexus.meeting_room_booking_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.BookingReportExportRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.BookingReportFilterRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.BookingReportReferenceDataRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.BookingReportViewRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.BookingReportDetailDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.BookingReportExportDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.BookingReportFilterResultDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.BookingReportReferenceDataDto;
import com.zynolo_nexus.meeting_room_booking_service.service.BookingReportService;
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
@RequestMapping("/api/v1/meeting-room/reports/booking-report")
@RequiredArgsConstructor
public class BookingReportController {

    private final BookingReportService bookingReportService;

    @PostMapping("/reference-data")
    public MessageResponseDTO<BookingReportReferenceDataDto> referenceData(@RequestBody(required = false) BookingReportReferenceDataRequest request) {
        return bookingReportService.referenceData(request);
    }

    @PostMapping("/filter-list")
    public MessageResponseDTO<BookingReportFilterResultDto> filterList(@RequestBody BookingReportFilterRequest request) {
        return bookingReportService.filterList(request);
    }

    @PostMapping("/view")
    public MessageResponseDTO<BookingReportDetailDto> view(@RequestBody BookingReportViewRequest request) {
        return bookingReportService.view(request);
    }

    @PostMapping("/export")
    public MessageResponseDTO<BookingReportExportDto> export(@RequestBody BookingReportExportRequest request) {
        return bookingReportService.export(request);
    }

    @PostMapping("/export-excel")
    public ResponseEntity<byte[]> exportExcel(@RequestBody BookingReportExportRequest request) {
        return fileResponse(
                bookingReportService.exportExcelFile(request),
                "meeting_booking_report_" + timestamp() + ".xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        );
    }

    @PostMapping("/export-pdf")
    public ResponseEntity<byte[]> exportPdf(@RequestBody BookingReportExportRequest request) {
        return fileResponse(
                bookingReportService.exportPdfFile(request),
                "meeting_booking_report_" + timestamp() + ".pdf",
                MediaType.APPLICATION_PDF_VALUE
        );
    }

    @PostMapping("/export-csv")
    public ResponseEntity<byte[]> exportCsv(@RequestBody BookingReportExportRequest request) {
        return fileResponse(
                bookingReportService.exportCsvFile(request),
                "meeting_booking_report_" + timestamp() + ".csv",
                "text/csv"
        );
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
