package com.zynolo_nexus.setting_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageListItemDto {

    private String code;
    private String description;
    private String status;
}
