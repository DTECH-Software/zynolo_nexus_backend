package com.zynolo_nexus.po_service.service;

import com.zynolo_nexus.po_service.dto.request.InvoiceReceiptFilterRequest;
import com.zynolo_nexus.po_service.dto.request.InvoiceReceiptHistoryRequest;
import com.zynolo_nexus.po_service.dto.request.InvoiceReceiptReceiveRequest;
import com.zynolo_nexus.po_service.dto.request.InvoiceReceiptReferenceDataRequest;
import com.zynolo_nexus.po_service.dto.request.InvoiceReceiptViewRequest;
import com.zynolo_nexus.po_service.dto.response.InvoiceReceiptFilterResultDto;
import com.zynolo_nexus.po_service.dto.response.InvoiceReceiptHistoryDto;
import com.zynolo_nexus.po_service.dto.response.InvoiceReceiptReferenceDataDto;
import com.zynolo_nexus.po_service.dto.response.InvoiceReceiptViewDto;

public interface InvoiceReceiptService {

    InvoiceReceiptReferenceDataDto getReferenceData(InvoiceReceiptReferenceDataRequest request);

    InvoiceReceiptFilterResultDto filterList(InvoiceReceiptFilterRequest request);

    InvoiceReceiptViewDto view(InvoiceReceiptViewRequest request);

    InvoiceReceiptViewDto receive(InvoiceReceiptReceiveRequest request);

    InvoiceReceiptHistoryDto history(InvoiceReceiptHistoryRequest request);
}
