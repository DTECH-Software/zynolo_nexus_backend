package com.zynolo_nexus.po_service.controller;

import com.zynolo_nexus.po_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.po_service.dto.request.ThreeWayMatchExecuteRequest;
import com.zynolo_nexus.po_service.dto.request.ThreeWayMatchFilterRequest;
import com.zynolo_nexus.po_service.dto.request.ThreeWayMatchReferenceDataRequest;
import com.zynolo_nexus.po_service.dto.request.ThreeWayMatchViewRequest;
import com.zynolo_nexus.po_service.dto.response.ThreeWayMatchFilterResultDto;
import com.zynolo_nexus.po_service.dto.response.ThreeWayMatchReferenceDataDto;
import com.zynolo_nexus.po_service.dto.response.ThreeWayMatchViewDto;
import com.zynolo_nexus.po_service.service.ThreeWayMatchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/po/three-way-match")
public class ThreeWayMatchController {

    private final ThreeWayMatchService threeWayMatchService;

    @PostMapping("/reference-data")
    public MessageResponseDTO<ThreeWayMatchReferenceDataDto> referenceData(@Valid @RequestBody ThreeWayMatchReferenceDataRequest request) {
        return success("Reference data POTM retrieved successfully", threeWayMatchService.getReferenceData(request));
    }

    @PostMapping("/filter-list")
    public MessageResponseDTO<ThreeWayMatchFilterResultDto> filterList(@Valid @RequestBody ThreeWayMatchFilterRequest request) {
        return success("Three-way match records filtered successfully", threeWayMatchService.filterList(request));
    }

    @PostMapping("/view")
    public MessageResponseDTO<ThreeWayMatchViewDto> view(@Valid @RequestBody ThreeWayMatchViewRequest request) {
        return success("Three-way match details retrieved successfully", threeWayMatchService.view(request));
    }

    @PostMapping("/match")
    public MessageResponseDTO<ThreeWayMatchViewDto> match(@Valid @RequestBody ThreeWayMatchExecuteRequest request) {
        return success("Three-way match completed successfully", threeWayMatchService.match(request));
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
