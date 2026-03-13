package com.zynolo_nexus.po_service.service;

import com.zynolo_nexus.po_service.dto.request.TrackingExportRequest;
import com.zynolo_nexus.po_service.dto.request.TrackingFilterRequest;
import com.zynolo_nexus.po_service.dto.request.TrackingReferenceDataRequest;
import com.zynolo_nexus.po_service.dto.request.TrackingTimelineRequest;
import com.zynolo_nexus.po_service.dto.request.TrackingViewRequest;
import com.zynolo_nexus.po_service.dto.response.TrackingExportDto;
import com.zynolo_nexus.po_service.dto.response.TrackingFilterResultDto;
import com.zynolo_nexus.po_service.dto.response.TrackingReferenceDataDto;
import com.zynolo_nexus.po_service.dto.response.TrackingTimelineDto;
import com.zynolo_nexus.po_service.dto.response.TrackingViewDto;

public interface PurchaseOrderTrackingService {

    TrackingReferenceDataDto getReferenceData(TrackingReferenceDataRequest request);

    TrackingFilterResultDto filterList(TrackingFilterRequest request);

    TrackingViewDto view(TrackingViewRequest request);

    TrackingTimelineDto timeline(TrackingTimelineRequest request);

    TrackingExportDto export(TrackingExportRequest request);
}
