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
import com.zynolo_nexus.fleet_service.service.support.ResponseMessageHelper;
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
@RequestMapping("/api/v1/fleet/drivers")
@Tag(name = "FLDM - Driver Master", description = "Page code FLDM (`FLEET_MASTER`)")
public class DriverController {

    private final DriverService driverService;
    private final ResponseMessageHelper messages;

    @PostMapping("/reference-data")
    public MessageResponseDTO<DriverReferenceDataDto> referenceData(@Valid @RequestBody DriverReferenceDataRequest request) {
        return success(messages.get("fleet.driver.reference.success"), driverService.getReferenceData(request));
    }

    @PostMapping("/filter-list")
    public MessageResponseDTO<DriverFilterResultDto> filterList(@Valid @RequestBody DriverFilterRequest request) {
        return success(messages.get("fleet.driver.filter.success"), driverService.filterList(request));
    }

    @PostMapping("/add")
    public MessageResponseDTO<DriverDto> add(@Valid @RequestBody DriverCreateRequest request) {
        return success(messages.get("fleet.driver.create.success"), driverService.create(request));
    }

    @PostMapping("/view")
    public MessageResponseDTO<DriverDto> view(@Valid @RequestBody DriverViewRequest request) {
        return success(messages.get("fleet.driver.view.success"), driverService.view(request));
    }

    @PostMapping("/update")
    public MessageResponseDTO<DriverDto> update(@Valid @RequestBody DriverUpdateRequest request) {
        return success(messages.get("fleet.driver.update.success"), driverService.update(request));
    }

    @PostMapping("/status-update")
    public MessageResponseDTO<DriverDto> statusUpdate(@Valid @RequestBody DriverStatusUpdateRequest request) {
        return success(messages.get("fleet.driver.status.success"), driverService.updateStatus(request));
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