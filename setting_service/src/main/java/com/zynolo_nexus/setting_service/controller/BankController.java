package com.zynolo_nexus.setting_service.controller;

import com.zynolo_nexus.setting_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.setting_service.dto.request.BankCreateRequest;
import com.zynolo_nexus.setting_service.dto.request.BankFilterRequest;
import com.zynolo_nexus.setting_service.dto.request.BankReferenceDataRequest;
import com.zynolo_nexus.setting_service.dto.request.BankStatusUpdateRequest;
import com.zynolo_nexus.setting_service.dto.request.BankUpdateRequest;
import com.zynolo_nexus.setting_service.dto.request.BankViewRequest;
import com.zynolo_nexus.setting_service.dto.response.BankDto;
import com.zynolo_nexus.setting_service.dto.response.BankFilterResultDto;
import com.zynolo_nexus.setting_service.dto.response.BankReferenceDataDto;
import com.zynolo_nexus.setting_service.service.BankService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/setting/banks")
@RequiredArgsConstructor
public class BankController {

    private final BankService bankService;

    @PostMapping
    public MessageResponseDTO<BankDto> create(@RequestBody BankCreateRequest request) {
        return bankService.create(request);
    }

    @PostMapping("/update")
    public MessageResponseDTO<BankDto> update(@RequestBody BankUpdateRequest request) {
        return bankService.update(request);
    }

    @PostMapping("/view")
    public MessageResponseDTO<BankDto> view(@RequestBody BankViewRequest request) {
        Long id = request != null ? request.getId() : null;
        return bankService.view(id);
    }

    @PostMapping("/status")
    public MessageResponseDTO<BankDto> status(@RequestBody BankStatusUpdateRequest request) {
        return bankService.updateStatus(request);
    }

    @PostMapping("/filter-list")
    public MessageResponseDTO<BankFilterResultDto> filterList(@RequestBody BankFilterRequest request) {
        return bankService.filterList(request);
    }

    @PostMapping("/reference-data")
    public MessageResponseDTO<BankReferenceDataDto> referenceData(
            @RequestBody(required = false) BankReferenceDataRequest request) {
        return bankService.referenceData(request);
    }
}
