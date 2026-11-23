package com.zynolo_nexus.meeting_room_booking_service.dto.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class MeetingInvoiceFilterSearch {
    private String invoiceNo;
    private String requestNo;
    private String meetingName;
    private String meetingType;
    private String meetingRoomName;
    private String customerCompanyName;
    private String status;
    private LocalDate dateFrom;
    private LocalDate dateTo;
}
