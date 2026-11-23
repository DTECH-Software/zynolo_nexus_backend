package com.zynolo_nexus.meeting_room_booking_service.dto.response;

import com.zynolo_nexus.meeting_room_booking_service.enums.MeetingBookingType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingInvoiceListItemDto {
    private Long invoiceId;
    private Long bookingId;
    private String invoiceNo;
    private String requestNo;
    private String meetingName;
    private MeetingBookingType meetingType;
    private String meetingTypeDescription;
    private String meetingRoomName;
    private LocalDate meetingDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String customerCompanyName;
    private String contactPerson;
    private String contactNumber;
    private String status;
    private String statusDescription;
    private Boolean invoiceGenerated;
    private Integer lineCount;
    private BigDecimal totalAmount;
    private LocalDateTime generatedDate;
    private String generatedBy;
    private LocalDateTime lastModifiedDate;
}
