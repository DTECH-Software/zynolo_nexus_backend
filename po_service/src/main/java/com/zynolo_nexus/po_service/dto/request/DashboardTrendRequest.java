package com.zynolo_nexus.po_service.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DashboardTrendRequest extends DashboardCriteriaRequest {

    @Min(value = 1, message = "months must be greater than zero")
    @Max(value = 24, message = "months must be 24 or less")
    private int months = 6;
}
