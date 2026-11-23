package com.zynolo_nexus.po_service.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VendorProductFilterSearch {
    private String vendorCode;
    private String vendorDescription;
    private String productCode;
    private String productDescription;
    private String status;
}
