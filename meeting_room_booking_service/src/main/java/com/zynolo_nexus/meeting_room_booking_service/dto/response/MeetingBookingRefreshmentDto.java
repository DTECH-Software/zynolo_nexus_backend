package com.zynolo_nexus.meeting_room_booking_service.dto.response;

import com.zynolo_nexus.meeting_room_booking_service.enums.RefreshmentCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingBookingRefreshmentDto {
    private Long id;
    private Long refreshmentId;
    private String refreshmentCode;
    private String itemName;
    private RefreshmentCategory category;
    private String categoryDescription;
    private Long vendorId;
    private String vendorCode;
    private String vendorName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalAmount;
}
