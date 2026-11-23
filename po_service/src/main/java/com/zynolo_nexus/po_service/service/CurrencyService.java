package com.zynolo_nexus.po_service.service;

import com.zynolo_nexus.po_service.dto.request.CurrencyCreateRequest;
import com.zynolo_nexus.po_service.dto.request.CurrencyFilterRequest;
import com.zynolo_nexus.po_service.dto.request.CurrencyReferenceDataRequest;
import com.zynolo_nexus.po_service.dto.request.CurrencyStatusRequest;
import com.zynolo_nexus.po_service.dto.request.CurrencyUpdateRequest;
import com.zynolo_nexus.po_service.dto.request.CurrencyViewRequest;
import com.zynolo_nexus.po_service.dto.response.CurrencyDto;
import com.zynolo_nexus.po_service.dto.response.CurrencyFilterResultDto;
import com.zynolo_nexus.po_service.dto.response.CurrencyReferenceDataDto;

public interface CurrencyService {

    CurrencyReferenceDataDto getReferenceData(CurrencyReferenceDataRequest request);

    CurrencyDto create(CurrencyCreateRequest request);

    CurrencyDto view(CurrencyViewRequest request);

    CurrencyDto update(CurrencyUpdateRequest request);

    CurrencyDto updateStatus(CurrencyStatusRequest request);

    CurrencyFilterResultDto filterList(CurrencyFilterRequest request);
}
