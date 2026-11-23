package com.zynolo_nexus.po_service.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class DashboardCriteriaRequest extends AuditRequest {
    private String companyCode;
    private String vendorCode;
    private LocalDate dateFrom;
    private LocalDate dateTo;
}
