package com.zynolo_nexus.po_service.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CostCenterFilterSearch {
    private String code;
    private String description;
    private String status;
}
