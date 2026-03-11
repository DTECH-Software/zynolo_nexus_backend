package com.zynolo_nexus.po_service.service;

import com.zynolo_nexus.po_service.dto.request.ProductCreateRequest;
import com.zynolo_nexus.po_service.dto.request.ProductFilterRequest;
import com.zynolo_nexus.po_service.dto.request.ProductReferenceDataRequest;
import com.zynolo_nexus.po_service.dto.request.ProductStatusRequest;
import com.zynolo_nexus.po_service.dto.request.ProductUpdateRequest;
import com.zynolo_nexus.po_service.dto.request.ProductViewRequest;
import com.zynolo_nexus.po_service.dto.response.ProductDto;
import com.zynolo_nexus.po_service.dto.response.ProductFilterResultDto;
import com.zynolo_nexus.po_service.dto.response.ProductReferenceDataDto;

public interface ProductService {

    ProductReferenceDataDto getReferenceData(ProductReferenceDataRequest request);

    ProductDto create(ProductCreateRequest request);

    ProductDto view(ProductViewRequest request);

    ProductDto update(ProductUpdateRequest request);

    ProductDto updateStatus(ProductStatusRequest request);

    ProductFilterResultDto filterList(ProductFilterRequest request);
}
