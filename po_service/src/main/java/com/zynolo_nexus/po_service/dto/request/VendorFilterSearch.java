package com.zynolo_nexus.po_service.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VendorFilterSearch {
    private String code;
    private String description;
    private String contactNo;
    private String email;
    private String status;
}
