package com.zynolo_nexus.po_service.service;

import com.zynolo_nexus.po_service.dto.request.PaymentFilterRequest;
import com.zynolo_nexus.po_service.dto.request.PaymentHistoryRequest;
import com.zynolo_nexus.po_service.dto.request.PaymentPayRequest;
import com.zynolo_nexus.po_service.dto.request.PaymentReferenceDataRequest;
import com.zynolo_nexus.po_service.dto.request.PaymentViewRequest;
import com.zynolo_nexus.po_service.dto.response.PaymentFilterResultDto;
import com.zynolo_nexus.po_service.dto.response.PaymentHistoryDto;
import com.zynolo_nexus.po_service.dto.response.PaymentReferenceDataDto;
import com.zynolo_nexus.po_service.dto.response.PaymentViewDto;

public interface PurchaseOrderPaymentService {

    PaymentReferenceDataDto getReferenceData(PaymentReferenceDataRequest request);

    PaymentFilterResultDto filterList(PaymentFilterRequest request);

    PaymentViewDto view(PaymentViewRequest request);

    PaymentViewDto pay(PaymentPayRequest request);

    PaymentHistoryDto history(PaymentHistoryRequest request);
}
