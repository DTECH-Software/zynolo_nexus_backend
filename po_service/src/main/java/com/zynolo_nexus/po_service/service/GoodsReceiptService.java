package com.zynolo_nexus.po_service.service;

import com.zynolo_nexus.po_service.dto.request.GoodsReceiptFilterRequest;
import com.zynolo_nexus.po_service.dto.request.GoodsReceiptHistoryRequest;
import com.zynolo_nexus.po_service.dto.request.GoodsReceiptReceiveRequest;
import com.zynolo_nexus.po_service.dto.request.GoodsReceiptReferenceDataRequest;
import com.zynolo_nexus.po_service.dto.request.GoodsReceiptViewRequest;
import com.zynolo_nexus.po_service.dto.response.GoodsReceiptFilterResultDto;
import com.zynolo_nexus.po_service.dto.response.GoodsReceiptHistoryDto;
import com.zynolo_nexus.po_service.dto.response.GoodsReceiptReferenceDataDto;
import com.zynolo_nexus.po_service.dto.response.GoodsReceiptViewDto;

public interface GoodsReceiptService {

    GoodsReceiptReferenceDataDto getReferenceData(GoodsReceiptReferenceDataRequest request);

    GoodsReceiptFilterResultDto filterList(GoodsReceiptFilterRequest request);

    GoodsReceiptViewDto view(GoodsReceiptViewRequest request);

    GoodsReceiptViewDto receive(GoodsReceiptReceiveRequest request);

    GoodsReceiptHistoryDto history(GoodsReceiptHistoryRequest request);
}
