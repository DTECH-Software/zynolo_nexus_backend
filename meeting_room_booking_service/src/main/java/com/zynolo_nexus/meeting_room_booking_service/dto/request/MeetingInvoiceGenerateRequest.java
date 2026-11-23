package com.zynolo_nexus.meeting_room_booking_service.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class MeetingInvoiceGenerateRequest {
    private String channel;
    private String ip;
    private String message;
    private String userAgent;
    private String username;
    private Long bookingId;
    private List<String> selectedLineKeys;
}
