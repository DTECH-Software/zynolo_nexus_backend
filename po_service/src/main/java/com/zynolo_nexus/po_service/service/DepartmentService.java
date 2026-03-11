package com.zynolo_nexus.po_service.service;

import com.zynolo_nexus.po_service.dto.request.DepartmentCreateRequest;
import com.zynolo_nexus.po_service.dto.request.DepartmentFilterRequest;
import com.zynolo_nexus.po_service.dto.request.DepartmentReferenceDataRequest;
import com.zynolo_nexus.po_service.dto.request.DepartmentStatusRequest;
import com.zynolo_nexus.po_service.dto.request.DepartmentUpdateRequest;
import com.zynolo_nexus.po_service.dto.request.DepartmentViewRequest;
import com.zynolo_nexus.po_service.dto.response.DepartmentDto;
import com.zynolo_nexus.po_service.dto.response.DepartmentFilterResultDto;
import com.zynolo_nexus.po_service.dto.response.DepartmentReferenceDataDto;

public interface DepartmentService {

    DepartmentReferenceDataDto getReferenceData(DepartmentReferenceDataRequest request);

    DepartmentDto create(DepartmentCreateRequest request);

    DepartmentDto view(DepartmentViewRequest request);

    DepartmentDto update(DepartmentUpdateRequest request);

    DepartmentDto updateStatus(DepartmentStatusRequest request);

    DepartmentFilterResultDto filterList(DepartmentFilterRequest request);
}
