package com.zynolo_nexus.po_service.controller;

import com.zynolo_nexus.po_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.po_service.dto.request.DepartmentCreateRequest;
import com.zynolo_nexus.po_service.dto.request.DepartmentFilterRequest;
import com.zynolo_nexus.po_service.dto.request.DepartmentReferenceDataRequest;
import com.zynolo_nexus.po_service.dto.request.DepartmentStatusRequest;
import com.zynolo_nexus.po_service.dto.request.DepartmentUpdateRequest;
import com.zynolo_nexus.po_service.dto.request.DepartmentViewRequest;
import com.zynolo_nexus.po_service.dto.response.DepartmentDto;
import com.zynolo_nexus.po_service.dto.response.DepartmentFilterResultDto;
import com.zynolo_nexus.po_service.dto.response.DepartmentReferenceDataDto;
import com.zynolo_nexus.po_service.service.DepartmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/po/departments")
public class DepartmentController {

    private final DepartmentService departmentService;

    @PostMapping("/reference-data")
    public MessageResponseDTO<DepartmentReferenceDataDto> referenceData(@Valid @RequestBody DepartmentReferenceDataRequest request) {
        return success("Reference data DEPM retrieved successfully", departmentService.getReferenceData(request));
    }

    @PostMapping
    public MessageResponseDTO<DepartmentDto> create(@Valid @RequestBody DepartmentCreateRequest request) {
        return success("Department created successfully", departmentService.create(request));
    }

    @PostMapping("/view")
    public MessageResponseDTO<DepartmentDto> view(@Valid @RequestBody DepartmentViewRequest request) {
        return success("Department retrieved successfully", departmentService.view(request));
    }

    @PostMapping("/update")
    public MessageResponseDTO<DepartmentDto> update(@Valid @RequestBody DepartmentUpdateRequest request) {
        return success("Department updated successfully", departmentService.update(request));
    }

    @PostMapping("/status")
    public MessageResponseDTO<DepartmentDto> updateStatus(@Valid @RequestBody DepartmentStatusRequest request) {
        return success("Department status updated successfully", departmentService.updateStatus(request));
    }

    @PostMapping("/filter-list")
    public MessageResponseDTO<DepartmentFilterResultDto> filterList(@Valid @RequestBody DepartmentFilterRequest request) {
        return success("Departments filtered successfully", departmentService.filterList(request));
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
