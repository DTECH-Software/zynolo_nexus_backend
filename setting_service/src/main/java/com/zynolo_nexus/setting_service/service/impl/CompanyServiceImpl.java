package com.zynolo_nexus.setting_service.service.impl;

import com.zynolo_nexus.setting_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.setting_service.dto.request.CompanyRequest;
import com.zynolo_nexus.setting_service.dto.response.CompanyDto;
import com.zynolo_nexus.setting_service.enums.CompanyStatus;
import com.zynolo_nexus.setting_service.exception.BadRequestException;
import com.zynolo_nexus.setting_service.exception.NotFoundException;
import com.zynolo_nexus.setting_service.model.Company;
import com.zynolo_nexus.setting_service.repository.CompanyRepository;
import com.zynolo_nexus.setting_service.service.CompanyService;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;
    private final MessageSource messageSource;

    public CompanyServiceImpl(CompanyRepository companyRepository, MessageSource messageSource) {
        this.companyRepository = companyRepository;
        this.messageSource = messageSource;
    }

    @Override
    public MessageResponseDTO<CompanyDto> createCompany(CompanyRequest request) {
        validate(request);
        if (companyRepository.existsByCode(request.getCode())) {
            throw new BadRequestException("company.code.exists");
        }
        Company company = Company.builder()
                .code(request.getCode())
                .description(request.getDescription())
                .status(resolveStatus(request.getStatus()))
                .build();
        company = companyRepository.save(company);
        return buildResponse(toDto(company), "company.create.success");
    }

    @Override
    public MessageResponseDTO<CompanyDto> updateCompany(String code, CompanyRequest request) {
        validate(request);
        Company company = companyRepository.findByCode(code)
                .orElseThrow(() -> new NotFoundException("company.notfound"));
        company.setDescription(request.getDescription());
        company.setStatus(resolveStatus(request.getStatus()));
        company = companyRepository.save(company);
        return buildResponse(toDto(company), "company.update.success");
    }

    @Override
    public MessageResponseDTO<CompanyDto> getCompany(String code) {
        Company company = companyRepository.findByCode(code)
                .orElseThrow(() -> new NotFoundException("company.notfound"));
        return buildResponse(toDto(company), "company.fetch.success");
    }

    @Override
    public MessageResponseDTO<List<CompanyDto>> getAllCompanies() {
        List<CompanyDto> companies = companyRepository.findAll().stream()
                .map(this::toDto)
                .toList();
        String message = messageSource.getMessage("company.fetch.success", null, LocaleContextHolder.getLocale());
        return MessageResponseDTO.<List<CompanyDto>>builder()
                .success(true)
                .message(message)
                .data(companies)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    public MessageResponseDTO<String> deactivateCompany(String code) {
        Company company = companyRepository.findByCode(code)
                .orElseThrow(() -> new NotFoundException("company.notfound"));
        company.setStatus(CompanyStatus.DEACTIVE);
        companyRepository.save(company);
        return buildMessageResponse("company.deactivate.success");
    }

    private void validate(CompanyRequest request) {
        if (request == null || !StringUtils.hasText(request.getCode()) || !StringUtils.hasText(request.getDescription())) {
            throw new BadRequestException("company.invalid");
        }
    }

    private CompanyStatus resolveStatus(CompanyStatus status) {
        return status != null ? status : CompanyStatus.ACTIVE;
    }

    private CompanyDto toDto(Company company) {
        return CompanyDto.builder()
                .id(company.getId())
                .code(company.getCode())
                .description(company.getDescription())
                .status(company.getStatus())
                .build();
    }

    private MessageResponseDTO<CompanyDto> buildResponse(CompanyDto dto, String messageKey) {
        String message = messageSource.getMessage(messageKey, null, LocaleContextHolder.getLocale());
        return MessageResponseDTO.<CompanyDto>builder()
                .success(true)
                .message(message)
                .data(dto)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    private MessageResponseDTO<String> buildMessageResponse(String messageKey) {
        String message = messageSource.getMessage(messageKey, null, LocaleContextHolder.getLocale());
        return MessageResponseDTO.<String>builder()
                .success(true)
                .message(message)
                .data(null)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }
}
