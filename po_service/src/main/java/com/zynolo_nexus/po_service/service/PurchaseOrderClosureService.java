package com.zynolo_nexus.po_service.service;

import com.zynolo_nexus.po_service.dto.request.ClosureCloseRequest;
import com.zynolo_nexus.po_service.dto.request.ClosureFilterRequest;
import com.zynolo_nexus.po_service.dto.request.ClosureReferenceDataRequest;
import com.zynolo_nexus.po_service.dto.request.ClosureViewRequest;
import com.zynolo_nexus.po_service.dto.response.ClosureFilterResultDto;
import com.zynolo_nexus.po_service.dto.response.ClosureReferenceDataDto;
import com.zynolo_nexus.po_service.dto.response.ClosureViewDto;

public interface PurchaseOrderClosureService {

    ClosureReferenceDataDto getReferenceData(ClosureReferenceDataRequest request);

    ClosureFilterResultDto filterList(ClosureFilterRequest request);

    ClosureViewDto view(ClosureViewRequest request);

    ClosureViewDto close(ClosureCloseRequest request);
}
