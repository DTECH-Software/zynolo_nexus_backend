package com.zynolo_nexus.fleet_service.service;

import com.zynolo_nexus.fleet_service.dto.request.ReceiptFilterRequest;
import com.zynolo_nexus.fleet_service.dto.request.ReceiptReferenceDataRequest;
import com.zynolo_nexus.fleet_service.dto.request.ReceiptViewRequest;
import com.zynolo_nexus.fleet_service.dto.response.ReceiptDto;
import com.zynolo_nexus.fleet_service.dto.response.ReceiptFilterResultDto;
import com.zynolo_nexus.fleet_service.dto.response.ReceiptReferenceDataDto;

public interface ReceiptService {

    ReceiptReferenceDataDto getReferenceData(ReceiptReferenceDataRequest request);

    ReceiptFilterResultDto filterList(ReceiptFilterRequest request);

    ReceiptDto view(ReceiptViewRequest request);
}
