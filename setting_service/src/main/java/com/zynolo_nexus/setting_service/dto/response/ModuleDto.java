package com.zynolo_nexus.setting_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModuleDto {

    private Long id;
    private String code;        // SETTINGS, PO, CHEQUE, TREASURY_LOAN, STATIONARY, VEHICLE, MEETING_ROOM
    private String name;        // "Settings", "PO Module", etc.
    private String description;
    private boolean active;
    private Integer sortOrder;
}
