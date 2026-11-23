package com.zynolo_nexus.po_service.service;

import com.zynolo_nexus.po_service.dto.request.DashboardActionItemsRequest;
import com.zynolo_nexus.po_service.dto.request.DashboardReferenceDataRequest;
import com.zynolo_nexus.po_service.dto.request.DashboardStatusBreakdownRequest;
import com.zynolo_nexus.po_service.dto.request.DashboardSummaryRequest;
import com.zynolo_nexus.po_service.dto.request.DashboardTrendRequest;
import com.zynolo_nexus.po_service.dto.response.DashboardActionItemsDto;
import com.zynolo_nexus.po_service.dto.response.DashboardReferenceDataDto;
import com.zynolo_nexus.po_service.dto.response.DashboardStatusBreakdownDto;
import com.zynolo_nexus.po_service.dto.response.DashboardSummaryDto;
import com.zynolo_nexus.po_service.dto.response.DashboardTrendDto;

public interface PurchaseOrderDashboardService {

    DashboardReferenceDataDto getReferenceData(DashboardReferenceDataRequest request);

    DashboardSummaryDto getSummary(DashboardSummaryRequest request);

    DashboardStatusBreakdownDto getStatusBreakdown(DashboardStatusBreakdownRequest request);

    DashboardTrendDto getTrend(DashboardTrendRequest request);

    DashboardActionItemsDto getActionItems(DashboardActionItemsRequest request);
}
