package com.zynolo_nexus.fleet_service.controller;

import com.zynolo_nexus.fleet_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.fleet_service.dto.request.VehicleCreateRequest;
import com.zynolo_nexus.fleet_service.dto.request.VehicleFilterRequest;
import com.zynolo_nexus.fleet_service.dto.request.VehicleReferenceDataRequest;
import com.zynolo_nexus.fleet_service.dto.request.VehicleStatusUpdateRequest;
import com.zynolo_nexus.fleet_service.dto.request.VehicleUpdateRequest;
import com.zynolo_nexus.fleet_service.dto.request.VehicleViewRequest;
import com.zynolo_nexus.fleet_service.dto.response.VehicleDto;
import com.zynolo_nexus.fleet_service.dto.response.VehicleFilterResultDto;
import com.zynolo_nexus.fleet_service.dto.response.VehicleReferenceDataDto;
import com.zynolo_nexus.fleet_service.service.VehicleService;
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
@RequestMapping("/api/v1/fleet/vehicles")
@Tag(name = "FLVM - Vehicle Master", description = "Page code FLVM (`FLEET_MASTER`)")
public class VehicleController {

    private final VehicleService vehicleService;
    private final ResponseMessageHelper messages;

    @PostMapping("/reference-data")
    public MessageResponseDTO<VehicleReferenceDataDto> referenceData(@Valid @RequestBody VehicleReferenceDataRequest request) {
        return success(messages.get("fleet.vehicle.reference.success"), vehicleService.getReferenceData(request));
    }

    @PostMapping("/filter-list")
    public MessageResponseDTO<VehicleFilterResultDto> filterList(@Valid @RequestBody VehicleFilterRequest request) {
        return success(messages.get("fleet.vehicle.filter.success"), vehicleService.filterList(request));
    }

    @PostMapping("/add")
    public MessageResponseDTO<VehicleDto> add(@Valid @RequestBody VehicleCreateRequest request) {
        return success(messages.get("fleet.vehicle.create.success"), vehicleService.create(request));
    }

    @PostMapping("/view")
    public MessageResponseDTO<VehicleDto> view(@Valid @RequestBody VehicleViewRequest request) {
        return success(messages.get("fleet.vehicle.view.success"), vehicleService.view(request));
    }

    @PostMapping("/update")
    public MessageResponseDTO<VehicleDto> update(@Valid @RequestBody VehicleUpdateRequest request) {
        return success(messages.get("fleet.vehicle.update.success"), vehicleService.update(request));
    }

    @PostMapping("/status-update")
    public MessageResponseDTO<VehicleDto> statusUpdate(@Valid @RequestBody VehicleStatusUpdateRequest request) {
        return success(messages.get("fleet.vehicle.status.success"), vehicleService.updateStatus(request));
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
