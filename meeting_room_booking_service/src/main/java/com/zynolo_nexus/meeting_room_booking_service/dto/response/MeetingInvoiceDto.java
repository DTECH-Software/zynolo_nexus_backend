package com.zynolo_nexus.meeting_room_booking_service.dto.response;

import com.zynolo_nexus.meeting_room_booking_service.enums.MeetingBookingType;
import com.zynolo_nexus.meeting_room_booking_service.enums.MeetingInvoiceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingInvoiceDto {
    private Long id;
    private Long companyId;
    private String companyCode;
    private String companyName;
    private String invoiceNo;
    private Long bookingId;
    private String requestNo;
    private String meetingName;
    private MeetingBookingType meetingType;
    private String meetingTypeDescription;
    private Long meetingRoomId;
    private String meetingRoomName;
    private LocalDate meetingDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Long customerId;
    private String customerCode;
    private String customerCompanyName;
    private String contactPerson;
    private String contactNumber;
    private MeetingInvoiceStatus status;
    private String statusDescription;
    private BigDecimal subTotal;
    private BigDecimal totalAmount;
    private List<MeetingInvoiceLineDto> lines;
    private String generatedBy;
    private LocalDateTime generatedDate;
    private String cancelledBy;
    private LocalDateTime cancelledDate;
    private String cancellationReason;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
    private String createdBy;
    private String lastModifiedBy;
}
