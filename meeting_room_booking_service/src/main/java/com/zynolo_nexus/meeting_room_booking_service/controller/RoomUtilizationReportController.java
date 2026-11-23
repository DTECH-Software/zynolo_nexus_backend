package com.zynolo_nexus.meeting_room_booking_service.controller;

import com.zynolo_nexus.meeting_room_booking_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.RoomUtilizationReportExportRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.RoomUtilizationReportFilterRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.RoomUtilizationReportReferenceDataRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.RoomUtilizationReportViewRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.RoomUtilizationReportDetailDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.RoomUtilizationReportExportDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.RoomUtilizationReportFilterResultDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.RoomUtilizationReportReferenceDataDto;
import com.zynolo_nexus.meeting_room_booking_service.service.RoomUtilizationReportService;
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
@RequestMapping("/api/v1/meeting-room/reports/room-utilization-report")
@RequiredArgsConstructor
public class RoomUtilizationReportController {

    private final RoomUtilizationReportService roomUtilizationReportService;

    @PostMapping("/reference-data")
    public MessageResponseDTO<RoomUtilizationReportReferenceDataDto> referenceData(
            @RequestBody(required = false) RoomUtilizationReportReferenceDataRequest request) {
        return roomUtilizationReportService.referenceData(request);
    }

    @PostMapping("/filter-list")
    public MessageResponseDTO<RoomUtilizationReportFilterResultDto> filterList(
            @RequestBody RoomUtilizationReportFilterRequest request) {
        return roomUtilizationReportService.filterList(request);
    }

    @PostMapping("/view")
    public MessageResponseDTO<RoomUtilizationReportDetailDto> view(@RequestBody RoomUtilizationReportViewRequest request) {
        return roomUtilizationReportService.view(request);
    }

    @PostMapping("/export")
    public MessageResponseDTO<RoomUtilizationReportExportDto> export(@RequestBody RoomUtilizationReportExportRequest request) {
        return roomUtilizationReportService.export(request);
    }

    @PostMapping("/export-excel")
    public ResponseEntity<byte[]> exportExcel(@RequestBody RoomUtilizationReportExportRequest request) {
        return fileResponse(
                roomUtilizationReportService.exportExcelFile(request),
                "room_utilization_report_" + timestamp() + ".xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        );
    }

    @PostMapping("/export-pdf")
    public ResponseEntity<byte[]> exportPdf(@RequestBody RoomUtilizationReportExportRequest request) {
        return fileResponse(
                roomUtilizationReportService.exportPdfFile(request),
                "room_utilization_report_" + timestamp() + ".pdf",
                MediaType.APPLICATION_PDF_VALUE
        );
    }

    @PostMapping("/export-csv")
    public ResponseEntity<byte[]> exportCsv(@RequestBody RoomUtilizationReportExportRequest request) {
        return fileResponse(
                roomUtilizationReportService.exportCsvFile(request),
                "room_utilization_report_" + timestamp() + ".csv",
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
