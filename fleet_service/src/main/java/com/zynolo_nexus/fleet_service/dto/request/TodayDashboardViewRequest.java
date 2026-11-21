package com.zynolo_nexus.fleet_service.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class TodayDashboardViewRequest extends AuditRequest {

    /** Defaults to today when null. */
    private LocalDate reportDate;
}
