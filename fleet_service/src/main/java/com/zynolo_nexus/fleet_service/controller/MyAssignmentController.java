package com.zynolo_nexus.fleet_service.controller;

import com.zynolo_nexus.fleet_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.fleet_service.dto.request.MyAssignmentFilterRequest;
import com.zynolo_nexus.fleet_service.dto.request.MyAssignmentReferenceDataRequest;
import com.zynolo_nexus.fleet_service.dto.request.MyAssignmentViewRequest;
import com.zynolo_nexus.fleet_service.dto.response.MyAssignmentReferenceDataDto;
import com.zynolo_nexus.fleet_service.dto.response.TripDto;
import com.zynolo_nexus.fleet_service.dto.response.TripFilterResultDto;
import com.zynolo_nexus.fleet_service.service.MyAssignmentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/fleet/my-assignments")
@Tag(name = "FLDA - My Assignments", description = "Page code FLDA (`FLEET_DRIVER_OPS`)")
public class MyAssignmentController {

    private final MyAssignmentService myAssignmentService;

    @PostMapping("/reference-data")
    public MessageResponseDTO<MyAssignmentReferenceDataDto> referenceData(
            @Valid @RequestBody MyAssignmentReferenceDataRequest request
    ) {
        return success("Reference data FLDA retrieved successfully", myAssignmentService.getReferenceData(request));
    }

    @PostMapping("/filter-list")
    public MessageResponseDTO<TripFilterResultDto> filterList(
            @Valid @RequestBody MyAssignmentFilterRequest request
    ) {
        return success("My assignments filtered successfully", myAssignmentService.filterList(request));
    }

    @PostMapping("/view")
    public MessageResponseDTO<TripDto> view(@Valid @RequestBody MyAssignmentViewRequest request) {
        return success("Assignment retrieved successfully", myAssignmentService.view(request));
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