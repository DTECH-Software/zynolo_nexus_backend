package com.zynolo_nexus.auth_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanySummaryDto {

    private Long id;
    private String code;
    private String description;
    private boolean isDefault;
}
