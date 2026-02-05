package com.zynolo_nexus.setting_service.dto.response;

import com.zynolo_nexus.setting_service.enums.CompanyStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyDto {

    private Long id;
    private String code;
    private String description;
    private CompanyStatus status;
}
