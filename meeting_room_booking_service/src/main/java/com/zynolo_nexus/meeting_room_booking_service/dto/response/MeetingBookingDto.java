package com.zynolo_nexus.meeting_room_booking_service.dto.response;

import com.zynolo_nexus.meeting_room_booking_service.enums.MeetingBookingStatus;
import com.zynolo_nexus.meeting_room_booking_service.enums.MeetingBookingType;
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
public class MeetingBookingDto {
    private Long id;
    private Long companyId;
    private String companyCode;
    private String companyName;
    private String requestNo;
    private String meetingName;
    private MeetingBookingType meetingType;
    private String meetingTypeDescription;
    private Long meetingRoomId;
    private String meetingRoomCode;
    private String meetingRoomName;
    private Integer roomCapacity;
    private LocalDate meetingDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer numberOfAttendees;
    private String purposeRemarks;
    private MeetingBookingStatus status;
    private String statusDescription;
    private BigDecimal durationHours;
    private BigDecimal estimatedRefreshmentCost;
    private BigDecimal estimatedBeverageCost;
    private BigDecimal estimatedSupportCost;
    private BigDecimal roomCharge;
    private BigDecimal totalEstimatedCost;
    private List<MeetingBookingRefreshmentDto> refreshments;
    private List<MeetingBookingBeverageDto> beverages;
    private List<MeetingBookingSupportServiceDto> supportServices;
    private List<MeetingBookingParticipantDto> participants;
    private String submittedBy;
    private LocalDateTime submittedDate;
    private String approvedBy;
    private LocalDateTime approvedDate;
    private String approvalRemark;
    private String rejectedBy;
    private LocalDateTime rejectedDate;
    private String rejectionRemark;
    private String cancellationReason;
    private String cancelledBy;
    private LocalDateTime cancelledDate;
    private String ongoingUpdate;
    private Integer ongoingAdditionalAttendees;
    private Integer ongoingUpdatedTotalAttendees;
    private String ongoingUpdatedBy;
    private LocalDateTime ongoingUpdatedDate;
    private String invoiceNo;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
    private String createdBy;
    private String lastModifiedBy;
}
