package com.zynolo_nexus.meeting_room_booking_service.dto.response;

import com.zynolo_nexus.meeting_room_booking_service.enums.RefreshmentCategory;
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
public class MeetingRefreshmentDto {
    private Long id;
    private Long companyId;
    private String companyCode;
    private String companyName;
    private String refreshmentCode;
    private RefreshmentCategory category;
    private String categoryDescription;
    private String itemName;
    private Long defaultVendorId;
    private String defaultVendorCode;
    private String defaultVendorName;
    private BigDecimal unitPrice;
    private String description;
    private Boolean active;
    private String activeStatusDescription;
    private Boolean selectable;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
    private String createdBy;
    private String lastModifiedBy;
}
