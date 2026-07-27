package com.zynolo_nexus.meeting_room_booking_service.dto.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class MeetingInvoiceReportFilterSearch {
    private LocalDate dateFrom;
    private LocalDate dateTo;
    private String invoiceNo;
    private String requestNo;
    private String meetingName;
    private String meetingRoomName;
    private String meetingType;
    private String customerCompanyName;
    private String status;
    private String generatedBy;
}
