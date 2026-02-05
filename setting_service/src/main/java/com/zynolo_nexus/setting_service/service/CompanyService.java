package com.zynolo_nexus.setting_service.service;

import com.zynolo_nexus.setting_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.setting_service.dto.request.CompanyRequest;
import com.zynolo_nexus.setting_service.dto.response.CompanyDto;

import java.util.List;

public interface CompanyService {

    MessageResponseDTO<CompanyDto> createCompany(CompanyRequest request);

    MessageResponseDTO<CompanyDto> updateCompany(String code, CompanyRequest request);

    MessageResponseDTO<CompanyDto> getCompany(String code);

    MessageResponseDTO<List<CompanyDto>> getAllCompanies();

    MessageResponseDTO<String> deactivateCompany(String code);
}
