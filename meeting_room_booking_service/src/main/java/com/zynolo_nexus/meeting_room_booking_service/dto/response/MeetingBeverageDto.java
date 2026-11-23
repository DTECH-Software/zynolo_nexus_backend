package com.zynolo_nexus.meeting_room_booking_service.dto.response;

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
public class MeetingBeverageDto {
    private Long id;
    private Long companyId;
    private String companyCode;
    private String companyName;
    private String beverageCode;
    private String beverageName;
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
