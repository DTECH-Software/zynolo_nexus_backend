package com.zynolo_nexus.setting_service.dto.request;

import com.zynolo_nexus.contracts.pages.SectionStatus;
import lombok.Data;

@Data
public class SectionUpdateRequest {

    private String code;
    private String moduleCode;
    private String name;
    private String description;
    private String url;
    private Integer sortOrder;
    private SectionStatus status;
}
