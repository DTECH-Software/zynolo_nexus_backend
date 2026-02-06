package com.zynolo_nexus.setting_service.controller;

import com.zynolo_nexus.setting_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.setting_service.dto.request.CompanyRequest;
import com.zynolo_nexus.setting_service.dto.response.CompanyDto;
import com.zynolo_nexus.setting_service.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/setting/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    @PostMapping
    public MessageResponseDTO<CompanyDto> create(@RequestBody CompanyRequest request) {
        return companyService.createCompany(request);
    }

    @PostMapping("/{code}/update")
    public MessageResponseDTO<CompanyDto> update(@PathVariable String code, @RequestBody CompanyRequest request) {
        return companyService.updateCompany(code, request);
    }

    @PostMapping("/{code}/get")
    public MessageResponseDTO<CompanyDto> get(@PathVariable String code) {
        return companyService.getCompany(code);
    }

    @PostMapping("/list")
    public MessageResponseDTO<List<CompanyDto>> list() {
        return companyService.getAllCompanies();
    }

    @PostMapping("/{code}/deactivate")
    public MessageResponseDTO<String> deactivate(@PathVariable String code) {
        return companyService.deactivateCompany(code);
    }
}
