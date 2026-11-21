package com.zynolo_nexus.fleet_service.controller;

import com.zynolo_nexus.fleet_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.fleet_service.dto.request.TransportRequestCreateRequest;
import com.zynolo_nexus.fleet_service.dto.request.TransportRequestFilterRequest;
import com.zynolo_nexus.fleet_service.dto.request.TransportRequestReferenceDataRequest;
import com.zynolo_nexus.fleet_service.dto.request.TransportRequestSubmitRequest;
import com.zynolo_nexus.fleet_service.dto.request.TransportRequestUpdateRequest;
import com.zynolo_nexus.fleet_service.dto.request.TransportRequestViewRequest;
import com.zynolo_nexus.fleet_service.dto.response.TransportRequestDto;
import com.zynolo_nexus.fleet_service.dto.response.TransportRequestFilterResultDto;
import com.zynolo_nexus.fleet_service.dto.response.TransportRequestReferenceDataDto;
import com.zynolo_nexus.fleet_service.service.TransportRequestCreateService;
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
@RequestMapping("/api/v1/fleet/request-create")
@Tag(name = "FLNR - New Request", description = "Page code FLNR (`FLEET_REQUEST`)")
public class TransportRequestCreateController {

    private final TransportRequestCreateService transportRequestCreateService;
    private final ResponseMessageHelper messages;

    @PostMapping("/reference-data")
    public MessageResponseDTO<TransportRequestReferenceDataDto> referenceData(
            @Valid @RequestBody TransportRequestReferenceDataRequest request
    ) {
        return success(messages.get("fleet.request.create.reference.success"), transportRequestCreateService.getReferenceData(request));
    }

    @PostMapping("/filter-list")
    public MessageResponseDTO<TransportRequestFilterResultDto> filterList(
            @Valid @RequestBody TransportRequestFilterRequest request
    ) {
        return success(messages.get("fleet.request.create.filter.success"), transportRequestCreateService.filterList(request));
    }

    @PostMapping("/add")
    public MessageResponseDTO<TransportRequestDto> add(@Valid @RequestBody TransportRequestCreateRequest request) {
        return success(messages.get("fleet.request.create.add.success"), transportRequestCreateService.create(request));
    }

    @PostMapping("/view")
    public MessageResponseDTO<TransportRequestDto> view(@Valid @RequestBody TransportRequestViewRequest request) {
        return success(messages.get("fleet.request.create.view.success"), transportRequestCreateService.view(request));
    }

    @PostMapping("/update")
    public MessageResponseDTO<TransportRequestDto> update(@Valid @RequestBody TransportRequestUpdateRequest request) {
        return success(messages.get("fleet.request.create.update.success"), transportRequestCreateService.update(request));
    }

    @PostMapping("/submit")
    public MessageResponseDTO<TransportRequestDto> submit(@Valid @RequestBody TransportRequestSubmitRequest request) {
        return success(messages.get("fleet.request.create.submit.success"), transportRequestCreateService.submit(request));
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