package com.zynolo_nexus.fleet_service.controller;

import com.zynolo_nexus.fleet_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.fleet_service.dto.request.DriverCreateRequest;
import com.zynolo_nexus.fleet_service.dto.request.DriverFilterRequest;
import com.zynolo_nexus.fleet_service.dto.request.DriverReferenceDataRequest;
import com.zynolo_nexus.fleet_service.dto.request.DriverStatusUpdateRequest;
import com.zynolo_nexus.fleet_service.dto.request.DriverUpdateRequest;
import com.zynolo_nexus.fleet_service.dto.request.DriverViewRequest;
import com.zynolo_nexus.fleet_service.dto.response.DriverDto;
import com.zynolo_nexus.fleet_service.dto.response.DriverFilterResultDto;
import com.zynolo_nexus.fleet_service.dto.response.DriverReferenceDataDto;
import com.zynolo_nexus.fleet_service.service.DriverService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/fleet/drivers")
public class DriverController {

    private final DriverService driverService;

    @PostMapping("/reference-data")
    public MessageResponseDTO<DriverReferenceDataDto> referenceData(@Valid @RequestBody DriverReferenceDataRequest request) {
        return success("Reference data FLDM retrieved successfully", driverService.getReferenceData(request));
    }

    @PostMapping("/filter-list")
    public MessageResponseDTO<DriverFilterResultDto> filterList(@Valid @RequestBody DriverFilterRequest request) {
        return success("Drivers filtered successfully", driverService.filterList(request));
    }

    @PostMapping("/add")
    public MessageResponseDTO<DriverDto> add(@Valid @RequestBody DriverCreateRequest request) {
        return success("Driver created successfully", driverService.create(request));
    }

    @PostMapping("/view")
    public MessageResponseDTO<DriverDto> view(@Valid @RequestBody DriverViewRequest request) {
        return success("Driver retrieved successfully", driverService.view(request));
    }

    @PostMapping("/update")
    public MessageResponseDTO<DriverDto> update(@Valid @RequestBody DriverUpdateRequest request) {
        return success("Driver updated successfully", driverService.update(request));
    }

    @PostMapping("/status-update")
    public MessageResponseDTO<DriverDto> statusUpdate(@Valid @RequestBody DriverStatusUpdateRequest request) {
        return success("Driver status updated successfully", driverService.updateStatus(request));
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