package com.zynolo_nexus.po_service.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GoodsReceiptFilterSearch {
    private String poNo;
    private String requestNo;
    private String companyCode;
    private String vendorCode;
    private String vendorName;
    private String requestType;
    private String status;
    private String createdBy;
}
