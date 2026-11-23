package com.zynolo_nexus.meeting_room_booking_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingInvoiceReportSummaryDto {
    private Integer totalInvoices;
    private Integer generatedInvoices;
    private Integer cancelledInvoices;
    private Integer internalMeetingInvoices;
    private Integer externalMeetingInvoices;
    private BigDecimal totalAmount;
}
