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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
