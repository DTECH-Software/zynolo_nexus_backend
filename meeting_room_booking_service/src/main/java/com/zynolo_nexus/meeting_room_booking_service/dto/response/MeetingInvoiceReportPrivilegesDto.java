package com.zynolo_nexus.meeting_room_booking_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingInvoiceReportPrivilegesDto {
    private boolean view;
    private boolean search;
    private boolean exportExcel;
    private boolean exportPdf;
    private boolean exportCsv;
    private boolean print;
}
