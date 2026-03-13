package com.zynolo_nexus.po_service.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClosureFilterSearch {
    private String poNo;
    private String requestNo;
    private String companyCode;
    private String vendorCode;
    private String vendorName;
    private String status;
    private String matchStatus;
    private String createdBy;
}
