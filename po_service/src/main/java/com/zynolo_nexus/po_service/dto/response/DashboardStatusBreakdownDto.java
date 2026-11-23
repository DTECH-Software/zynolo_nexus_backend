package com.zynolo_nexus.po_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatusBreakdownDto {
    private List<DashboardStatusCountDto> requestStatuses;
    private List<DashboardStatusCountDto> purchaseOrderStatuses;
    private List<DashboardStatusCountDto> matchStatuses;
    private List<DashboardStatusCountDto> paymentStatuses;
}
