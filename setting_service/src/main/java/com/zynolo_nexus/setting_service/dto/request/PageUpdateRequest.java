package com.zynolo_nexus.setting_service.dto.request;

import com.zynolo_nexus.contracts.pages.PageStatus;
import lombok.Data;

@Data
public class PageUpdateRequest {

    private String username;
    private String code;
    private String sectionCode;
    private String name;
    private String description;
    private String url;
    private Integer sortOrder;
    private PageStatus status;
}
