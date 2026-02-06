package com.zynolo_nexus.auth_service.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModuleDashboardSectionDto {

    private String code;
    private String description;
    private String url;
    private List<ModuleDashboardPageDto> pages;
}
