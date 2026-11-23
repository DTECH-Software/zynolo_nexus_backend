package com.zynolo_nexus.meeting_room_booking_service.dto.request;

import lombok.Data;

@Data
public class RoomUtilizationReportExportRequest {
    private String channel;
    private String ip;
    private String message;
    private String userAgent;
    private String username;
    private String exportType;
    private String sortColumn;
    private String sortDirection;
    private RoomUtilizationReportFilterSearch search;
}
