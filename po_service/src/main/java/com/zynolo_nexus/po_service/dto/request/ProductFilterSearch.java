package com.zynolo_nexus.po_service.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductFilterSearch {
    private String code;
    private String description;
    private String uom;
    private String status;
}
