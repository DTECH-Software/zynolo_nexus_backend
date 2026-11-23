package com.zynolo_nexus.auth_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModuleDashboardPageDto {

    private String code;
    private String url;
    private String description;
    private String status;
}
