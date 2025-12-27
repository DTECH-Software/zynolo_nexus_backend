package com.zynolo_nexus.auth_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModulePermissionDto {

    private String code;     // SETTINGS, PO, CHEQUE, TREASURY_LOAN, STATIONARY, VEHICLE, MEETING_ROOM
    private String name;     // "Settings", "PO Module", ...
    private boolean canView; // whether this user can see the module in UI
    private List<SectionPermissionDto> sections;
}
