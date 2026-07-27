package com.zynolo_nexus.meeting_room_booking_service.dto.request;

import lombok.Data;

@Data
public class MeetingInvoiceReportViewRequest {
    private String channel;
    private String ip;
    private String message;
    private String userAgent;
    private String username;
    private Long id;
    private Long invoiceId;
    private Long bookingId;
    private String invoiceNo;
}
