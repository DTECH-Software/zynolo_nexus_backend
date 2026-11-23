package com.zynolo_nexus.fleet_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TodayDashboardReferenceDataDto {
    private LocalDate defaultReportDate;
    private TodayDashboardPrivilegesDto privileges;
}
