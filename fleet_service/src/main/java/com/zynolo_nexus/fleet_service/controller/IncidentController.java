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
@RequestMapping("/api/v1/fleet/incidents")
@Tag(name = "FLDI - Incidents", description = "Page code FLDI (`FLEET_DRIVER_OPS`)")
public class IncidentController {

    private final IncidentService incidentService;
    private final ResponseMessageHelper messages;

    @PostMapping("/reference-data")
    public MessageResponseDTO<IncidentReferenceDataDto> referenceData(
            @Valid @RequestBody IncidentReferenceDataRequest request
    ) {
        return success(messages.get("fleet.incidents.reference.success"), incidentService.getReferenceData(request));
    }

    @PostMapping("/filter-list")
    public MessageResponseDTO<IncidentFilterResultDto> filterList(
            @Valid @RequestBody IncidentFilterRequest request
    ) {
        return success(messages.get("fleet.incidents.filter.success"), incidentService.filterList(request));
    }

    @PostMapping("/add")
    public MessageResponseDTO<IncidentDto> add(@Valid @RequestBody IncidentCreateRequest request) {
        return success(messages.get("fleet.incidents.add.success"), incidentService.create(request));
    }

    @PostMapping("/view")
    public MessageResponseDTO<IncidentDto> view(@Valid @RequestBody IncidentViewRequest request) {
        return success(messages.get("fleet.incidents.view.success"), incidentService.view(request));
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