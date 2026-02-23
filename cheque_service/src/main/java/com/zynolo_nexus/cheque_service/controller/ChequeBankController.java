package com.zynolo_nexus.cheque_service.controller;

import com.zynolo_nexus.cheque_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.cheque_service.dto.request.ChequeBankCreateRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeBankFilterRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeBankReferenceDataRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeBankStatusUpdateRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeBankUpdateRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeBankViewRequest;
import com.zynolo_nexus.cheque_service.dto.response.ChequeBankDto;
import com.zynolo_nexus.cheque_service.dto.response.ChequeBankFilterResultDto;
import com.zynolo_nexus.cheque_service.dto.response.ChequeBankReferenceDataDto;
import com.zynolo_nexus.cheque_service.service.ChequeBankService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/cheque/banks")
@RequiredArgsConstructor
public class ChequeBankController {

    private final ChequeBankService chequeBankService;

    @PostMapping
    public MessageResponseDTO<ChequeBankDto> create(@RequestBody ChequeBankCreateRequest request) {
        return chequeBankService.create(request);
    }

    @PostMapping("/update")
    public MessageResponseDTO<ChequeBankDto> update(@RequestBody ChequeBankUpdateRequest request) {
        return chequeBankService.update(request);
    }

    @PostMapping("/view")
    public MessageResponseDTO<ChequeBankDto> view(@RequestBody ChequeBankViewRequest request) {
        Long id = request != null ? request.getId() : null;
        return chequeBankService.view(id);
    }

    @PostMapping("/status")
    public MessageResponseDTO<ChequeBankDto> status(@RequestBody ChequeBankStatusUpdateRequest request) {
        return chequeBankService.updateStatus(request);
    }

    @PostMapping("/filter-list")
    public MessageResponseDTO<ChequeBankFilterResultDto> filterList(@RequestBody ChequeBankFilterRequest request) {
        return chequeBankService.filterList(request);
    }

    @PostMapping("/reference-data")
    public MessageResponseDTO<ChequeBankReferenceDataDto> referenceData(
            @RequestBody(required = false) ChequeBankReferenceDataRequest request) {
        return chequeBankService.referenceData(request);
    }
}
