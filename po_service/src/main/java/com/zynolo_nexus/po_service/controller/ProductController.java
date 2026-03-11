package com.zynolo_nexus.po_service.controller;

import com.zynolo_nexus.po_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.po_service.dto.request.ProductCreateRequest;
import com.zynolo_nexus.po_service.dto.request.ProductFilterRequest;
import com.zynolo_nexus.po_service.dto.request.ProductReferenceDataRequest;
import com.zynolo_nexus.po_service.dto.request.ProductStatusRequest;
import com.zynolo_nexus.po_service.dto.request.ProductUpdateRequest;
import com.zynolo_nexus.po_service.dto.request.ProductViewRequest;
import com.zynolo_nexus.po_service.dto.response.ProductDto;
import com.zynolo_nexus.po_service.dto.response.ProductFilterResultDto;
import com.zynolo_nexus.po_service.dto.response.ProductReferenceDataDto;
import com.zynolo_nexus.po_service.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/po/products")
public class ProductController {

    private final ProductService productService;

    @PostMapping("/reference-data")
    public MessageResponseDTO<ProductReferenceDataDto> referenceData(@Valid @RequestBody ProductReferenceDataRequest request) {
        return success("Reference data PRDM retrieved successfully", productService.getReferenceData(request));
    }

    @PostMapping
    public MessageResponseDTO<ProductDto> create(@Valid @RequestBody ProductCreateRequest request) {
        return success("Product created successfully", productService.create(request));
    }

    @PostMapping("/view")
    public MessageResponseDTO<ProductDto> view(@Valid @RequestBody ProductViewRequest request) {
        return success("Product retrieved successfully", productService.view(request));
    }

    @PostMapping("/update")
    public MessageResponseDTO<ProductDto> update(@Valid @RequestBody ProductUpdateRequest request) {
        return success("Product updated successfully", productService.update(request));
    }

    @PostMapping("/status")
    public MessageResponseDTO<ProductDto> updateStatus(@Valid @RequestBody ProductStatusRequest request) {
        return success("Product status updated successfully", productService.updateStatus(request));
    }

    @PostMapping("/filter-list")
    public MessageResponseDTO<ProductFilterResultDto> filterList(@Valid @RequestBody ProductFilterRequest request) {
        return success("Products filtered successfully", productService.filterList(request));
    }

    private <T> MessageResponseDTO<T> success(String message, T data) {
        return MessageResponseDTO.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }
}
