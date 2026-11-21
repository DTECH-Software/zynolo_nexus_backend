package com.zynolo_nexus.fleet_service.controller;

import com.zynolo_nexus.fleet_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.fleet_service.dto.request.IncidentCreateRequest;
import com.zynolo_nexus.fleet_service.dto.request.IncidentFilterRequest;
import com.zynolo_nexus.fleet_service.dto.request.IncidentReferenceDataRequest;
import com.zynolo_nexus.fleet_service.dto.request.IncidentViewRequest;
import com.zynolo_nexus.fleet_service.dto.response.IncidentDto;
import com.zynolo_nexus.fleet_service.dto.response.IncidentFilterResultDto;
import com.zynolo_nexus.fleet_service.dto.response.IncidentReferenceDataDto;
import com.zynolo_nexus.fleet_service.service.IncidentService;
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
@RequestMapping("/api/v1/fleet/incidents")
@Tag(name = "FLDI - Incidents", description = "Page code FLDI (`FLEET_DRIVER_OPS`)")
public class IncidentController {

    private final IncidentService incidentService;

    @PostMapping("/reference-data")
    public MessageResponseDTO<IncidentReferenceDataDto> referenceData(
            @Valid @RequestBody IncidentReferenceDataRequest request
    ) {
        return success("Reference data FLDI retrieved successfully", incidentService.getReferenceData(request));
    }

    @PostMapping("/filter-list")
    public MessageResponseDTO<IncidentFilterResultDto> filterList(
            @Valid @RequestBody IncidentFilterRequest request
    ) {
        return success("Incidents filtered successfully", incidentService.filterList(request));
    }

    @PostMapping("/add")
    public MessageResponseDTO<IncidentDto> add(@Valid @RequestBody IncidentCreateRequest request) {
        return success("Incident reported successfully", incidentService.create(request));
    }

    @PostMapping("/view")
    public MessageResponseDTO<IncidentDto> view(@Valid @RequestBody IncidentViewRequest request) {
        return success("Incident retrieved successfully", incidentService.view(request));
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