package com.zynolo_nexus.meeting_room_booking_service.dto.response;

import com.zynolo_nexus.meeting_room_booking_service.enums.RoomAvailabilityStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingRoomDto {
    private Long id;
    private Long companyId;
    private String companyCode;
    private String companyName;
    private String roomCode;
    private String roomName;
    private Integer capacity;
    private String location;
    private String floor;
    private BigDecimal perHourCharge;
    private RoomAvailabilityStatus availabilityStatus;
    private String availabilityStatusDescription;
    private String description;
    private Boolean active;
    private String activeStatusDescription;
    private Boolean bookable;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
    private String createdBy;
    private String lastModifiedBy;
}
