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
public class MeetingBookingBeverageDto {
    private Long id;
    private Long beverageId;
    private String beverageCode;
    private String beverageName;
    private Long vendorId;
    private String vendorCode;
    private String vendorName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalAmount;
}
