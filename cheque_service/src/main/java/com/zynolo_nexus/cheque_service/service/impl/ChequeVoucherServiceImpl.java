package com.zynolo_nexus.cheque_service.service.impl;

import com.zynolo_nexus.cheque_service.client.AuthModuleClient;
import com.zynolo_nexus.cheque_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.cheque_service.dto.request.ChequePrintRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeReprintApproveRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeReprintCreateRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeReprintFilterRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeReprintFilterSearch;
import com.zynolo_nexus.cheque_service.dto.request.ChequeReprintReferenceDataRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeReprintRejectRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeVoucherApproveRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeVoucherCreateRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeVoucherExportPdfRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeVoucherFilterRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeVoucherFilterSearch;
import com.zynolo_nexus.cheque_service.dto.request.ChequeVoucherInvoiceRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeVoucherRejectRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeVoucherReferenceDataRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeVoucherSubmitForApprovalRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeVoucherUpdateRequest;
import com.zynolo_nexus.cheque_service.dto.response.ChequeReferenceBankDto;
import com.zynolo_nexus.cheque_service.dto.response.ChequeReferenceCompanyDto;
import com.zynolo_nexus.cheque_service.dto.response.ChequeReferenceCustomerDto;
import com.zynolo_nexus.cheque_service.dto.response.ChequeReferenceStatusDto;
import com.zynolo_nexus.cheque_service.dto.response.ChequeReprintFilterResultDto;
import com.zynolo_nexus.cheque_service.dto.response.ChequeReprintReferenceDataDto;
import com.zynolo_nexus.cheque_service.dto.response.ChequeReprintRequestDto;
import com.zynolo_nexus.cheque_service.dto.response.ChequeVoucherDto;
import com.zynolo_nexus.cheque_service.dto.response.ChequeVoucherFilterResultDto;
import com.zynolo_nexus.cheque_service.dto.response.ChequeVoucherInvoiceDto;
import com.zynolo_nexus.cheque_service.dto.response.ChequeVoucherListItemDto;
import com.zynolo_nexus.cheque_service.dto.response.ChequeVoucherPdfDto;
import com.zynolo_nexus.cheque_service.dto.response.ChequeVoucherPrivilegesDto;
import com.zynolo_nexus.cheque_service.dto.response.ChequeVoucherReferenceDataDto;
import com.zynolo_nexus.cheque_service.enums.ChequeBankStatus;
import com.zynolo_nexus.cheque_service.enums.ChequePrintStatus;
import com.zynolo_nexus.cheque_service.enums.ChequeReprintStatus;
import com.zynolo_nexus.cheque_service.enums.ChequeCompanyStatus;
import com.zynolo_nexus.cheque_service.enums.ChequeCustomerStatus;
import com.zynolo_nexus.cheque_service.enums.ChequeType;
import com.zynolo_nexus.cheque_service.enums.ChequeVoucherStatus;
import com.zynolo_nexus.cheque_service.model.ChequeBank;
import com.zynolo_nexus.cheque_service.model.ChequeCompany;
import com.zynolo_nexus.cheque_service.model.ChequeCustomer;
import com.zynolo_nexus.cheque_service.model.ChequeReprintRequest;
import com.zynolo_nexus.cheque_service.model.ChequeVoucher;
import com.zynolo_nexus.cheque_service.model.ChequeVoucherInvoice;
import com.zynolo_nexus.cheque_service.model.UserAccount;
import com.zynolo_nexus.cheque_service.repository.ChequeBankRepository;
import com.zynolo_nexus.cheque_service.repository.ChequeCompanyRepository;
import com.zynolo_nexus.cheque_service.repository.ChequeCustomerRepository;
import com.zynolo_nexus.cheque_service.repository.ChequeReprintRequestRepository;
import com.zynolo_nexus.cheque_service.repository.ChequeVoucherRepository;
import com.zynolo_nexus.cheque_service.repository.UserAccountRepository;
import com.zynolo_nexus.cheque_service.service.ChequeVoucherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChequeVoucherServiceImpl implements ChequeVoucherService {

    private static final String CHVM_PAGE_CODE = "CHVM";
    private static final String CHCP_PAGE_CODE = "CHCP";
    private static final String CHAP_PAGE_CODE = "CHAP";
    private static final String VOUCHER_REPORT_PATH = "/reports/cheque-voucher.jrxml";
    private static final String CHEQUE_PRINT_REPORT_PATH = "/reports/cheque-print-lk.jrxml";

    private final ChequeVoucherRepository chequeVoucherRepository;
    private final ChequeBankRepository chequeBankRepository;
    private final ChequeCompanyRepository chequeCompanyRepository;
    private final ChequeCustomerRepository chequeCustomerRepository;
    private final ChequeReprintRequestRepository chequeReprintRequestRepository;
    private final UserAccountRepository userAccountRepository;
    private final AuthModuleClient authModuleClient;

    private volatile JasperReport voucherReport;
    private volatile JasperReport chequePrintReport;

    @Override
    @Transactional
    public MessageResponseDTO<ChequeVoucherDto> create(ChequeVoucherCreateRequest request) {
        if (!isValidCreateRequest(request)) {
            return error("Invalid voucher create request", 400);
        }

        ChequeCompany company = resolveActiveCompany(request.getCompanyCode());
        if (company == null) {
            return error("Company not found or inactive", 400);
        }

        ChequeCustomer customer = resolveActiveCustomer(request.getCustomerCode());
        if (customer == null) {
            return error("Customer not found or inactive", 400);
        }

        ChequeBank bank = resolveActiveBank(request.getBankCode());
        if (bank == null) {
            return error("Bank not found or inactive", 400);
        }

        if (!isValidInvoiceLines(request.getInvoices())) {
            return error("Invalid invoice details", 400);
        }

        ChequeType chequeType = resolveChequeType(request.getChequeType());
        if (chequeType == null) {
            return error("Invalid cheque type. Allowed values: NORMAL, DATED", 400);
        }

        LocalDate chequeDate = resolveChequeDate(chequeType, request.getChequeDate());
        if (chequeDate == null) {
            return error("Cheque date is required for DATED cheque type", 400);
        }

        String actor = normalizeUsername(request.getUsername());
        String voucherNo = generateVoucherNo();
        BigDecimal totalAmount = calculateTotal(request.getInvoices());

        ChequeVoucher voucher = ChequeVoucher.builder()
                .voucherNo(voucherNo)
                .companyCode(company.getCode())
                .customerCode(customer.getCode())
                .chequeNo(request.getChequeNo().trim())
                .bankCode(bank.getCode())
                .chequeBankName(bank.getName())
                .chequeType(chequeType)
                .chequeDate(chequeDate)
                .description(trimToNull(request.getDescription()))
                .totalAmount(totalAmount)
                .status(ChequeVoucherStatus.DRAFT)
                .createdBy(actor)
                .lastModifiedBy(actor)
                .build();

        List<ChequeVoucherInvoice> invoices = buildInvoices(voucher, request.getInvoices());
        voucher.getInvoices().clear();
        voucher.getInvoices().addAll(invoices);

        voucher = chequeVoucherRepository.save(voucher);
        return success("Voucher created successfully", toDto(voucher, company, customer, bank));
    }

    @Override
    @Transactional(readOnly = true)
    public MessageResponseDTO<ChequeVoucherDto> view(Long id) {
        if (id == null) {
            return error("Invalid voucher view request", 400);
        }

        ChequeVoucher voucher = chequeVoucherRepository.findById(id).orElse(null);
        if (voucher == null) {
            return error("Voucher not found", 404);
        }

        ChequeCompany company = chequeCompanyRepository.findByCodeIgnoreCase(voucher.getCompanyCode()).orElse(null);
        ChequeCustomer customer = chequeCustomerRepository.findByCodeIgnoreCase(voucher.getCustomerCode()).orElse(null);
        ChequeBank bank = resolveBank(voucher.getBankCode());
        return success("Voucher found with ID: " + id, toDto(voucher, company, customer, bank));
    }

    @Override
    @Transactional
    public MessageResponseDTO<ChequeVoucherDto> update(ChequeVoucherUpdateRequest request) {
        if (request == null || request.getId() == null) {
            return error("Invalid voucher update request", 400);
        }

        ChequeVoucher voucher = chequeVoucherRepository.findById(request.getId()).orElse(null);
        if (voucher == null) {
            return error("Voucher not found", 404);
        }

        if (voucher.getStatus() != ChequeVoucherStatus.DRAFT && voucher.getStatus() != ChequeVoucherStatus.REJECTED) {
            return error("Only DRAFT or REJECTED vouchers can be updated", 400);
        }

        if (!StringUtils.hasText(request.getCompanyCode())
                || !StringUtils.hasText(request.getCustomerCode())
                || !StringUtils.hasText(request.getChequeNo())
                || !StringUtils.hasText(request.getBankCode())
                || !StringUtils.hasText(request.getChequeType())
                || !isValidInvoiceLines(request.getInvoices())) {
            return error("Invalid voucher update request", 400);
        }

        ChequeCompany company = resolveActiveCompany(request.getCompanyCode());
        if (company == null) {
            return error("Company not found or inactive", 400);
        }

        ChequeCustomer customer = resolveActiveCustomer(request.getCustomerCode());
        if (customer == null) {
            return error("Customer not found or inactive", 400);
        }

        ChequeBank bank = resolveActiveBank(request.getBankCode());
        if (bank == null) {
            return error("Bank not found or inactive", 400);
        }

        ChequeType chequeType = resolveChequeType(request.getChequeType());
        if (chequeType == null) {
            return error("Invalid cheque type. Allowed values: NORMAL, DATED", 400);
        }

        LocalDate chequeDate = resolveChequeDate(chequeType, request.getChequeDate());
        if (chequeDate == null) {
            return error("Cheque date is required for DATED cheque type", 400);
        }

        voucher.setCompanyCode(company.getCode());
        voucher.setCustomerCode(customer.getCode());
        voucher.setChequeNo(request.getChequeNo().trim());
        voucher.setBankCode(bank.getCode());
        voucher.setChequeBankName(bank.getName());
        voucher.setChequeType(chequeType);
        voucher.setChequeDate(chequeDate);
        voucher.setDescription(trimToNull(request.getDescription()));
        voucher.setTotalAmount(calculateTotal(request.getInvoices()));

        String actor = normalizeUsername(request.getUsername());
        if (StringUtils.hasText(actor)) {
            voucher.setLastModifiedBy(actor);
            if (!StringUtils.hasText(voucher.getCreatedBy())) {
                voucher.setCreatedBy(actor);
            }
        }

        voucher.getInvoices().clear();
        voucher.getInvoices().addAll(buildInvoices(voucher, request.getInvoices()));

        voucher = chequeVoucherRepository.save(voucher);
        return success("Voucher updated successfully", toDto(voucher, company, customer, bank));
    }

    @Override
    @Transactional(readOnly = true)
    public MessageResponseDTO<ChequeVoucherFilterResultDto> filterList(ChequeVoucherFilterRequest request) {
        return filterListByStatuses(
                request,
                EnumSet.of(ChequeVoucherStatus.DRAFT, ChequeVoucherStatus.REJECTED),
                "Voucher list filtered successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public MessageResponseDTO<ChequeVoucherFilterResultDto> approvalFilterList(ChequeVoucherFilterRequest request) {
        return filterListByStatuses(
                request,
                EnumSet.of(ChequeVoucherStatus.PENDING_APPROVAL, ChequeVoucherStatus.APPROVED),
                "Voucher approval list filtered successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public MessageResponseDTO<ChequeVoucherFilterResultDto> chequeFilterList(ChequeVoucherFilterRequest request) {
        return filterListByStatuses(
                request,
                EnumSet.of(ChequeVoucherStatus.APPROVED, ChequeVoucherStatus.CHEQUE_CREATED),
                "Cheque list filtered successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public MessageResponseDTO<ChequeVoucherDto> chequeView(Long id) {
        if (id == null) {
            return error("Invalid cheque view request", 400);
        }

        ChequeVoucher voucher = chequeVoucherRepository.findById(id).orElse(null);
        if (voucher == null) {
            return error("Cheque record not found", 404);
        }
        if (voucher.getStatus() != ChequeVoucherStatus.APPROVED
                && voucher.getStatus() != ChequeVoucherStatus.CHEQUE_CREATED) {
            return error("Cheque can be viewed only for APPROVED or CHEQUE_CREATED vouchers", 400);
        }

        ChequeCompany company = chequeCompanyRepository.findByCodeIgnoreCase(voucher.getCompanyCode()).orElse(null);
        ChequeCustomer customer = chequeCustomerRepository.findByCodeIgnoreCase(voucher.getCustomerCode()).orElse(null);
        ChequeBank bank = resolveBank(voucher.getBankCode());
        return success("Cheque details retrieved successfully", toDto(voucher, company, customer, bank));
    }

    @Override
    @Transactional
    public MessageResponseDTO<ChequeVoucherDto> submitForApproval(ChequeVoucherSubmitForApprovalRequest request) {
        if (request == null || request.getId() == null) {
            return error("Invalid submit for approval request", 400);
        }

        ChequeVoucher voucher = chequeVoucherRepository.findById(request.getId()).orElse(null);
        if (voucher == null) {
            return error("Voucher not found", 404);
        }
        if (voucher.getStatus() != ChequeVoucherStatus.DRAFT && voucher.getStatus() != ChequeVoucherStatus.REJECTED) {
            return error("Only DRAFT or REJECTED vouchers can be submitted for approval", 400);
        }

        String actor = normalizeUsername(request.getUsername());
        voucher.setStatus(ChequeVoucherStatus.PENDING_APPROVAL);
        voucher.setSubmittedBy(actor);
        voucher.setSubmittedDate(LocalDateTime.now());
        voucher.setRejectionReason(null);
        voucher.setRejectedBy(null);
        voucher.setRejectedDate(null);
        voucher.setApprovalRemark(null);
        voucher.setApprovedBy(null);
        voucher.setApprovedDate(null);
        if (StringUtils.hasText(actor)) {
            voucher.setLastModifiedBy(actor);
            if (!StringUtils.hasText(voucher.getCreatedBy())) {
                voucher.setCreatedBy(actor);
            }
        }

        voucher = chequeVoucherRepository.save(voucher);
        ChequeCompany company = chequeCompanyRepository.findByCodeIgnoreCase(voucher.getCompanyCode()).orElse(null);
        ChequeCustomer customer = chequeCustomerRepository.findByCodeIgnoreCase(voucher.getCustomerCode()).orElse(null);
        ChequeBank bank = resolveBank(voucher.getBankCode());
        return success("Voucher submitted for approval successfully", toDto(voucher, company, customer, bank));
    }

    @Override
    @Transactional
    public MessageResponseDTO<ChequeVoucherDto> approve(ChequeVoucherApproveRequest request) {
        if (request == null || request.getId() == null) {
            return error("Invalid approve request", 400);
        }

        ChequeVoucher voucher = chequeVoucherRepository.findById(request.getId()).orElse(null);
        if (voucher == null) {
            return error("Voucher not found", 404);
        }
        if (voucher.getStatus() != ChequeVoucherStatus.PENDING_APPROVAL) {
            return error("Only PENDING_APPROVAL vouchers can be approved", 400);
        }

        String actor = normalizeUsername(request.getUsername());
        voucher.setStatus(ChequeVoucherStatus.APPROVED);
        voucher.setApprovedBy(actor);
        voucher.setApprovedDate(LocalDateTime.now());
        voucher.setApprovalRemark(trimToNull(request.getRemark()));
        voucher.setRejectedBy(null);
        voucher.setRejectedDate(null);
        voucher.setRejectionReason(null);
        if (StringUtils.hasText(actor)) {
            voucher.setLastModifiedBy(actor);
        }

        voucher = chequeVoucherRepository.save(voucher);
        ChequeCompany company = chequeCompanyRepository.findByCodeIgnoreCase(voucher.getCompanyCode()).orElse(null);
        ChequeCustomer customer = chequeCustomerRepository.findByCodeIgnoreCase(voucher.getCustomerCode()).orElse(null);
        ChequeBank bank = resolveBank(voucher.getBankCode());
        return success("Voucher approved successfully", toDto(voucher, company, customer, bank));
    }

    @Override
    @Transactional
    public MessageResponseDTO<ChequeVoucherDto> reject(ChequeVoucherRejectRequest request) {
        if (request == null || request.getId() == null) {
            return error("Invalid reject request", 400);
        }
        if (!StringUtils.hasText(request.getRejectionReason())) {
            return error("Rejection reason is required", 400);
        }

        ChequeVoucher voucher = chequeVoucherRepository.findById(request.getId()).orElse(null);
        if (voucher == null) {
            return error("Voucher not found", 404);
        }
        if (voucher.getStatus() != ChequeVoucherStatus.PENDING_APPROVAL) {
            return error("Only PENDING_APPROVAL vouchers can be rejected", 400);
        }

        String actor = normalizeUsername(request.getUsername());
        voucher.setStatus(ChequeVoucherStatus.REJECTED);
        voucher.setRejectedBy(actor);
        voucher.setRejectedDate(LocalDateTime.now());
        voucher.setRejectionReason(request.getRejectionReason().trim());
        voucher.setApprovedBy(null);
        voucher.setApprovedDate(null);
        voucher.setApprovalRemark(null);
        if (StringUtils.hasText(actor)) {
            voucher.setLastModifiedBy(actor);
        }

        voucher = chequeVoucherRepository.save(voucher);
        ChequeCompany company = chequeCompanyRepository.findByCodeIgnoreCase(voucher.getCompanyCode()).orElse(null);
        ChequeCustomer customer = chequeCustomerRepository.findByCodeIgnoreCase(voucher.getCustomerCode()).orElse(null);
        ChequeBank bank = resolveBank(voucher.getBankCode());
        return success("Voucher rejected successfully", toDto(voucher, company, customer, bank));
    }

    private MessageResponseDTO<ChequeVoucherFilterResultDto> filterListByStatuses(
            ChequeVoucherFilterRequest request,
            Set<ChequeVoucherStatus> statuses,
            String successMessage) {
        List<ChequeVoucher> vouchers = chequeVoucherRepository.findAll();

        ChequeVoucherFilterSearch search = request != null ? request.getSearch() : null;
        String voucherNo = normalize(search != null ? search.getVoucherNo() : null);
        String companyCode = normalize(search != null ? search.getCompanyCode() : null);
        String customerCode = normalize(search != null ? search.getCustomerCode() : null);
        String chequeNo = normalize(search != null ? search.getChequeNo() : null);
        String bankCode = normalize(search != null ? search.getBankCode() : null);
        String chequeType = normalize(search != null ? search.getChequeType() : null);
        String status = normalize(search != null ? search.getStatus() : null);
        String printStatus = normalize(search != null ? search.getPrintStatus() : null);

        Map<String, String> companyDescriptions = new HashMap<>();
        chequeCompanyRepository.findAll()
                .forEach(company -> companyDescriptions.put(company.getCode(), company.getDescription()));
        Map<String, String> customerDescriptions = new HashMap<>();
        chequeCustomerRepository.findAll()
                .forEach(customer -> customerDescriptions.put(customer.getCode(), customer.getDescription()));
        Map<String, String> bankNames = new HashMap<>();
        chequeBankRepository.findAll()
                .forEach(bank -> bankNames.put(bank.getCode(), bank.getName()));

        List<ChequeVoucherListItemDto> filtered = vouchers.stream()
                .filter(voucher -> matches(voucherNo, voucher.getVoucherNo()))
                .filter(voucher -> matches(companyCode, voucher.getCompanyCode()))
                .filter(voucher -> matches(customerCode, voucher.getCustomerCode()))
                .filter(voucher -> matches(chequeNo, voucher.getChequeNo()))
                .filter(voucher -> matches(bankCode, voucher.getBankCode()))
                .filter(voucher -> matchesChequeType(chequeType, voucher.getChequeType()))
                .filter(voucher -> statuses == null || statuses.isEmpty() || statuses.contains(voucher.getStatus()))
                .filter(voucher -> matchesStatus(status, voucher.getStatus()))
                .filter(voucher -> matchesPrintStatus(printStatus, voucher.getPrintStatus()))
                .map(voucher -> toListItem(
                        voucher,
                        companyDescriptions.get(voucher.getCompanyCode()),
                        customerDescriptions.get(voucher.getCustomerCode()),
                        bankNames.get(voucher.getBankCode())))
                .toList();

        Comparator<ChequeVoucherListItemDto> comparator = resolveComparator(
                request != null ? request.getSortColumn() : null,
                request != null ? request.getSortDirection() : null
        );

        List<ChequeVoucherListItemDto> sorted = filtered.stream().sorted(comparator).toList();
        int requestedSize = request != null && request.getSize() != null && request.getSize() > 0 ? request.getSize() : 10;
        int page = request != null && request.getPage() != null && request.getPage() >= 0 ? request.getPage() : 0;

        int totalRecords = sorted.size();
        int fromIndex = Math.min(page * requestedSize, totalRecords);
        int toIndex = Math.min(fromIndex + requestedSize, totalRecords);
        List<ChequeVoucherListItemDto> content = sorted.subList(fromIndex, toIndex);
        int totalPages = requestedSize == 0 ? 1 : (int) Math.ceil((double) totalRecords / requestedSize);

        ChequeVoucherFilterResultDto result = ChequeVoucherFilterResultDto.builder()
                .content(content)
                .size(content.size())
                .totalRecords(totalRecords)
                .page(page)
                .totalPages(totalPages)
                .build();

        return MessageResponseDTO.<ChequeVoucherFilterResultDto>builder()
                .success(true)
                .message(successMessage)
                .data(result)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public MessageResponseDTO<ChequeVoucherReferenceDataDto> referenceData(ChequeVoucherReferenceDataRequest request) {
        String pageCode = StringUtils.hasText(request != null ? request.getPageCode() : null)
                ? request.getPageCode().trim().toUpperCase(Locale.ROOT)
                : CHVM_PAGE_CODE;

        ChequeVoucherPrivilegesDto privileges = resolvePrivileges(request, pageCode);

        List<ChequeReferenceCompanyDto> companies = chequeCompanyRepository.findAllByStatusOrderByCodeAsc(ChequeCompanyStatus.ACTIVE)
                .stream()
                .map(company -> ChequeReferenceCompanyDto.builder()
                        .code(company.getCode())
                        .description(company.getDescription())
                        .build())
                .toList();

        List<ChequeReferenceCustomerDto> customers = chequeCustomerRepository.findAllByStatusOrderByCodeAsc(ChequeCustomerStatus.ACTIVE)
                .stream()
                .map(customer -> ChequeReferenceCustomerDto.builder()
                        .code(customer.getCode())
                        .description(customer.getDescription())
                        .build())
                .toList();

        List<ChequeReferenceBankDto> banks = chequeBankRepository.findAllByStatusOrderByCodeAsc(ChequeBankStatus.ACTIVE)
                .stream()
                .map(bank -> ChequeReferenceBankDto.builder()
                        .code(bank.getCode())
                        .name(bank.getName())
                        .build())
                .toList();

        List<ChequeReferenceStatusDto> defaultStatuses = CHCP_PAGE_CODE.equalsIgnoreCase(pageCode)
                ? List.of(
                ChequeReferenceStatusDto.builder().code("APPROVED").description("Approved").build(),
                ChequeReferenceStatusDto.builder().code("CHEQUE_CREATED").description("Cheque Created").build()
        )
                : List.of(
                ChequeReferenceStatusDto.builder().code("DRAFT").description("Draft").build(),
                ChequeReferenceStatusDto.builder().code("PENDING_APPROVAL").description("Pending Approval").build(),
                ChequeReferenceStatusDto.builder().code("APPROVED").description("Approved").build(),
                ChequeReferenceStatusDto.builder().code("REJECTED").description("Rejected").build(),
                ChequeReferenceStatusDto.builder().code("CHEQUE_CREATED").description("Cheque Created").build()
        );

        ChequeVoucherReferenceDataDto data = ChequeVoucherReferenceDataDto.builder()
                .defaultStatus(defaultStatuses)
                .printStatuses(List.of(
                        ChequeReferenceStatusDto.builder().code("NOT_PRINTED").description("Not Printed").build(),
                        ChequeReferenceStatusDto.builder().code("PRINTED").description("Printed").build()
                ))
                .chequeTypes(List.of(
                        ChequeReferenceStatusDto.builder().code("NORMAL").description("Normal cheque").build(),
                        ChequeReferenceStatusDto.builder().code("DATED").description("Dated cheque").build()
                ))
                .companies(companies)
                .customers(customers)
                .banks(banks)
                .privileges(privileges)
                .build();

        return MessageResponseDTO.<ChequeVoucherReferenceDataDto>builder()
                .success(true)
                .message("Reference data " + pageCode + " retrieved successfully")
                .data(data)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public MessageResponseDTO<ChequeVoucherPdfDto> exportPdf(ChequeVoucherExportPdfRequest request) {
        Long voucherId = resolveVoucherId(request);
        if (voucherId == null) {
            return pdfError("Invalid voucher export request", 400);
        }

        ChequeVoucher voucher = chequeVoucherRepository.findById(voucherId).orElse(null);
        if (voucher == null) {
            return pdfError("Voucher not found", 404);
        }

        ChequeCompany company = chequeCompanyRepository.findByCodeIgnoreCase(voucher.getCompanyCode()).orElse(null);
        ChequeCustomer customer = chequeCustomerRepository.findByCodeIgnoreCase(voucher.getCustomerCode()).orElse(null);
        ChequeBank bank = resolveBank(voucher.getBankCode());
        ChequeVoucherDto dto = toDto(voucher, company, customer, bank);

        try {
            JasperReport report = getOrLoadVoucherReport();
            Map<String, Object> params = buildVoucherReportParams(dto, company, customer);
            JRMapCollectionDataSource dataSource = new JRMapCollectionDataSource(buildVoucherRows(dto));

            JasperPrint jasperPrint = JasperFillManager.fillReport(report, params, dataSource);
            byte[] pdfBytes = JasperExportManager.exportReportToPdf(jasperPrint);
            String base64 = Base64.getEncoder().encodeToString(pdfBytes);
            String fileName = (dto.getVoucherNo() != null ? dto.getVoucherNo() : "voucher") + ".pdf";

            return MessageResponseDTO.<ChequeVoucherPdfDto>builder()
                    .success(true)
                    .message("Voucher PDF exported successfully")
                    .data(ChequeVoucherPdfDto.builder()
                            .fileName(fileName)
                            .fileType("application/pdf")
                            .doc(base64)
                            .build())
                    .errors(null)
                    .errorCode(0)
                    .responseTime(LocalDateTime.now())
                    .build();
        } catch (Throwable ex) {
            log.error("Unable to export voucher PDF for voucherId={}", voucherId, ex);
            return pdfError("Unable to export voucher PDF", 500, rootCauseMessage(ex));
        }
    }

    @Override
    @Transactional
    public MessageResponseDTO<ChequeVoucherPdfDto> printCheque(ChequePrintRequest request) {
        Long voucherId = resolveVoucherId(request);
        if (voucherId == null) {
            return pdfError("Invalid cheque print request", 400);
        }

        ChequeVoucher voucher = chequeVoucherRepository.findById(voucherId).orElse(null);
        if (voucher == null) {
            return pdfError("Voucher not found", 404);
        }
        if (voucher.getStatus() != ChequeVoucherStatus.APPROVED
                && voucher.getStatus() != ChequeVoucherStatus.CHEQUE_CREATED) {
            return pdfError("Cheque can be printed only for APPROVED or CHEQUE_CREATED vouchers", 400);
        }

        int existingPrintCount = voucher.getPrintCount() != null ? voucher.getPrintCount() : 0;
        ChequeReprintRequest approvedReprint = null;
        if (existingPrintCount > 0) {
            approvedReprint = chequeReprintRequestRepository
                    .findFirstByVoucherIdAndStatusAndUsedForPrintFalseOrderByApprovedDateDesc(
                            voucher.getId(), ChequeReprintStatus.REPRINT_APPROVED)
                    .orElse(null);
            if (approvedReprint == null) {
                return pdfError("Reprint approval is required before printing again", 400);
            }
        }

        MessageResponseDTO<ChequeVoucherPdfDto> exported = exportChequePdfForVoucher(voucher);
        if (exported == null || !exported.isSuccess() || exported.getData() == null) {
            return exported != null ? exported : pdfError("Unable to print cheque", 500);
        }

        String actor = normalizeUsername(request.getUsername());
        LocalDateTime now = LocalDateTime.now();
        voucher.setPrintCount(existingPrintCount + 1);
        voucher.setPrintStatus(ChequePrintStatus.PRINTED);
        voucher.setLastPrintedBy(actor);
        voucher.setLastPrintedDate(now);
        if (voucher.getStatus() == ChequeVoucherStatus.APPROVED) {
            voucher.setStatus(ChequeVoucherStatus.CHEQUE_CREATED);
        }
        if (StringUtils.hasText(actor)) {
            voucher.setLastModifiedBy(actor);
            if (!StringUtils.hasText(voucher.getCreatedBy())) {
                voucher.setCreatedBy(actor);
            }
        }
        chequeVoucherRepository.save(voucher);

        if (approvedReprint != null) {
            approvedReprint.setUsedForPrint(true);
            approvedReprint.setUsedDate(now);
            if (StringUtils.hasText(actor)) {
                approvedReprint.setLastModifiedBy(actor);
                if (!StringUtils.hasText(approvedReprint.getCreatedBy())) {
                    approvedReprint.setCreatedBy(actor);
                }
            }
            chequeReprintRequestRepository.save(approvedReprint);
        }

        return MessageResponseDTO.<ChequeVoucherPdfDto>builder()
                .success(true)
                .message(existingPrintCount == 0 ? "Cheque printed successfully" : "Cheque reprinted successfully")
                .data(exported.getData())
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    @Transactional
    public MessageResponseDTO<ChequeReprintRequestDto> requestReprint(ChequeReprintCreateRequest request) {
        if (request == null || request.getVoucherId() == null || !StringUtils.hasText(request.getReprintReason())) {
            return reprintError("Invalid reprint request", 400);
        }

        ChequeVoucher voucher = chequeVoucherRepository.findById(request.getVoucherId()).orElse(null);
        if (voucher == null) {
            return reprintError("Voucher not found", 404);
        }
        if (voucher.getStatus() != ChequeVoucherStatus.CHEQUE_CREATED) {
            return reprintError("Reprint can be requested only after first cheque print", 400);
        }
        int printCount = voucher.getPrintCount() != null ? voucher.getPrintCount() : 0;
        if (printCount <= 0) {
            return reprintError("Reprint can be requested only after first cheque print", 400);
        }
        if (chequeReprintRequestRepository.existsByVoucherIdAndStatus(voucher.getId(), ChequeReprintStatus.REPRINT_PENDING)) {
            return reprintError("A pending reprint request already exists for this voucher", 400);
        }

        String actor = normalizeUsername(request.getUsername());
        ChequeReprintRequest reprintRequest = ChequeReprintRequest.builder()
                .voucherId(voucher.getId())
                .reprintReason(request.getReprintReason().trim())
                .status(ChequeReprintStatus.REPRINT_PENDING)
                .requestedBy(actor)
                .requestedDate(LocalDateTime.now())
                .createdBy(actor)
                .lastModifiedBy(actor)
                .build();

        reprintRequest = chequeReprintRequestRepository.save(reprintRequest);
        return reprintSuccess("Reprint request submitted successfully", toReprintDto(reprintRequest, voucher));
    }

    @Override
    @Transactional(readOnly = true)
    public MessageResponseDTO<ChequeReprintReferenceDataDto> reprintReferenceData(ChequeReprintReferenceDataRequest request) {
        String pageCode = StringUtils.hasText(request != null ? request.getPageCode() : null)
                ? request.getPageCode().trim().toUpperCase(Locale.ROOT)
                : CHAP_PAGE_CODE;

        ChequeVoucherReferenceDataRequest permissionRequest = new ChequeVoucherReferenceDataRequest();
        permissionRequest.setUsername(request != null ? request.getUsername() : null);
        permissionRequest.setPageCode(pageCode);
        ChequeVoucherPrivilegesDto privileges = resolvePrivileges(permissionRequest, pageCode);

        ChequeReprintReferenceDataDto data = ChequeReprintReferenceDataDto.builder()
                .defaultStatus(List.of(
                        ChequeReferenceStatusDto.builder().code("REPRINT_PENDING").description("Reprint Pending").build(),
                        ChequeReferenceStatusDto.builder().code("REPRINT_APPROVED").description("Reprint Approved").build(),
                        ChequeReferenceStatusDto.builder().code("REPRINT_REJECTED").description("Reprint Rejected").build()
                ))
                .privileges(privileges)
                .build();

        return MessageResponseDTO.<ChequeReprintReferenceDataDto>builder()
                .success(true)
                .message("Reference data " + pageCode + " retrieved successfully")
                .data(data)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public MessageResponseDTO<ChequeReprintFilterResultDto> reprintFilterList(ChequeReprintFilterRequest request) {
        List<ChequeReprintRequest> requests = chequeReprintRequestRepository.findAll();
        Map<Long, ChequeVoucher> voucherMap = loadVoucherMap(requests.stream().map(ChequeReprintRequest::getVoucherId).toList());

        ChequeReprintFilterSearch search = request != null ? request.getSearch() : null;
        String voucherNo = normalize(search != null ? search.getVoucherNo() : null);
        String companyCode = normalize(search != null ? search.getCompanyCode() : null);
        String customerCode = normalize(search != null ? search.getCustomerCode() : null);
        String bankCode = normalize(search != null ? search.getBankCode() : null);
        String status = normalize(search != null ? search.getStatus() : null);

        List<ChequeReprintRequestDto> filtered = requests.stream()
                .filter(item -> {
                    ChequeVoucher voucher = voucherMap.get(item.getVoucherId());
                    if (voucher == null) {
                        return false;
                    }
                    return matches(voucherNo, voucher.getVoucherNo())
                            && matches(companyCode, voucher.getCompanyCode())
                            && matches(customerCode, voucher.getCustomerCode())
                            && matches(bankCode, voucher.getBankCode())
                            && matchesReprintStatus(status, item.getStatus());
                })
                .map(item -> toReprintDto(item, voucherMap.get(item.getVoucherId())))
                .toList();

        Comparator<ChequeReprintRequestDto> comparator = resolveReprintComparator(
                request != null ? request.getSortColumn() : null,
                request != null ? request.getSortDirection() : null
        );

        List<ChequeReprintRequestDto> sorted = filtered.stream().sorted(comparator).toList();
        int requestedSize = request != null && request.getSize() != null && request.getSize() > 0 ? request.getSize() : 10;
        int page = request != null && request.getPage() != null && request.getPage() >= 0 ? request.getPage() : 0;

        int totalRecords = sorted.size();
        int fromIndex = Math.min(page * requestedSize, totalRecords);
        int toIndex = Math.min(fromIndex + requestedSize, totalRecords);
        List<ChequeReprintRequestDto> content = sorted.subList(fromIndex, toIndex);
        int totalPages = requestedSize == 0 ? 1 : (int) Math.ceil((double) totalRecords / requestedSize);

        ChequeReprintFilterResultDto result = ChequeReprintFilterResultDto.builder()
                .content(content)
                .size(content.size())
                .totalRecords(totalRecords)
                .page(page)
                .totalPages(totalPages)
                .build();

        return MessageResponseDTO.<ChequeReprintFilterResultDto>builder()
                .success(true)
                .message("Cheque reprint requests filtered successfully")
                .data(result)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public MessageResponseDTO<ChequeReprintRequestDto> reprintView(Long id) {
        if (id == null) {
            return reprintError("Invalid reprint view request", 400);
        }

        ChequeReprintRequest reprintRequest = chequeReprintRequestRepository.findById(id).orElse(null);
        if (reprintRequest == null) {
            return reprintError("Reprint request not found", 404);
        }
        ChequeVoucher voucher = chequeVoucherRepository.findById(reprintRequest.getVoucherId()).orElse(null);
        if (voucher == null) {
            return reprintError("Voucher not found for this reprint request", 404);
        }
        return reprintSuccess("Reprint request retrieved successfully", toReprintDto(reprintRequest, voucher));
    }

    @Override
    @Transactional
    public MessageResponseDTO<ChequeReprintRequestDto> approveReprint(ChequeReprintApproveRequest request) {
        if (request == null || request.getId() == null) {
            return reprintError("Invalid reprint approve request", 400);
        }

        ChequeReprintRequest reprintRequest = chequeReprintRequestRepository.findById(request.getId()).orElse(null);
        if (reprintRequest == null) {
            return reprintError("Reprint request not found", 404);
        }
        if (reprintRequest.getStatus() != ChequeReprintStatus.REPRINT_PENDING) {
            return reprintError("Only REPRINT_PENDING requests can be approved", 400);
        }

        String actor = normalizeUsername(request.getUsername());
        reprintRequest.setStatus(ChequeReprintStatus.REPRINT_APPROVED);
        reprintRequest.setApprovedBy(actor);
        reprintRequest.setApprovedDate(LocalDateTime.now());
        reprintRequest.setApprovalRemark(trimToNull(request.getApprovalRemark()));
        reprintRequest.setRejectedBy(null);
        reprintRequest.setRejectedDate(null);
        reprintRequest.setRejectionReason(null);
        if (StringUtils.hasText(actor)) {
            reprintRequest.setLastModifiedBy(actor);
            if (!StringUtils.hasText(reprintRequest.getCreatedBy())) {
                reprintRequest.setCreatedBy(actor);
            }
        }

        reprintRequest = chequeReprintRequestRepository.save(reprintRequest);
        ChequeVoucher voucher = chequeVoucherRepository.findById(reprintRequest.getVoucherId()).orElse(null);
        if (voucher == null) {
            return reprintError("Voucher not found for this reprint request", 404);
        }
        return reprintSuccess("Reprint request approved successfully", toReprintDto(reprintRequest, voucher));
    }

    @Override
    @Transactional
    public MessageResponseDTO<ChequeReprintRequestDto> rejectReprint(ChequeReprintRejectRequest request) {
        if (request == null || request.getId() == null || !StringUtils.hasText(request.getRejectionReason())) {
            return reprintError("Invalid reprint reject request", 400);
        }

        ChequeReprintRequest reprintRequest = chequeReprintRequestRepository.findById(request.getId()).orElse(null);
        if (reprintRequest == null) {
            return reprintError("Reprint request not found", 404);
        }
        if (reprintRequest.getStatus() != ChequeReprintStatus.REPRINT_PENDING) {
            return reprintError("Only REPRINT_PENDING requests can be rejected", 400);
        }

        String actor = normalizeUsername(request.getUsername());
        reprintRequest.setStatus(ChequeReprintStatus.REPRINT_REJECTED);
        reprintRequest.setRejectedBy(actor);
        reprintRequest.setRejectedDate(LocalDateTime.now());
        reprintRequest.setRejectionReason(request.getRejectionReason().trim());
        reprintRequest.setApprovedBy(null);
        reprintRequest.setApprovedDate(null);
        reprintRequest.setApprovalRemark(null);
        if (StringUtils.hasText(actor)) {
            reprintRequest.setLastModifiedBy(actor);
            if (!StringUtils.hasText(reprintRequest.getCreatedBy())) {
                reprintRequest.setCreatedBy(actor);
            }
        }

        reprintRequest = chequeReprintRequestRepository.save(reprintRequest);
        ChequeVoucher voucher = chequeVoucherRepository.findById(reprintRequest.getVoucherId()).orElse(null);
        if (voucher == null) {
            return reprintError("Voucher not found for this reprint request", 404);
        }
        return reprintSuccess("Reprint request rejected successfully", toReprintDto(reprintRequest, voucher));
    }

    private Long resolveVoucherId(ChequeVoucherExportPdfRequest request) {
        if (request == null) {
            return null;
        }
        if (request.getVoucherId() != null) {
            return request.getVoucherId();
        }
        return request.getId();
    }

    private Long resolveVoucherId(ChequePrintRequest request) {
        if (request == null) {
            return null;
        }
        if (request.getVoucherId() != null) {
            return request.getVoucherId();
        }
        return request.getId();
    }

    private MessageResponseDTO<ChequeVoucherPdfDto> exportPdfForVoucher(ChequeVoucher voucher) {
        if (voucher == null) {
            return pdfError("Voucher not found", 404);
        }

        ChequeCompany company = chequeCompanyRepository.findByCodeIgnoreCase(voucher.getCompanyCode()).orElse(null);
        ChequeCustomer customer = chequeCustomerRepository.findByCodeIgnoreCase(voucher.getCustomerCode()).orElse(null);
        ChequeBank bank = resolveBank(voucher.getBankCode());
        ChequeVoucherDto dto = toDto(voucher, company, customer, bank);

        try {
            JasperReport report = getOrLoadVoucherReport();
            Map<String, Object> params = buildVoucherReportParams(dto, company, customer);
            JRMapCollectionDataSource dataSource = new JRMapCollectionDataSource(buildVoucherRows(dto));

            JasperPrint jasperPrint = JasperFillManager.fillReport(report, params, dataSource);
            byte[] pdfBytes = JasperExportManager.exportReportToPdf(jasperPrint);
            String base64 = Base64.getEncoder().encodeToString(pdfBytes);
            String fileName = (dto.getVoucherNo() != null ? dto.getVoucherNo() : "voucher") + ".pdf";

            return MessageResponseDTO.<ChequeVoucherPdfDto>builder()
                    .success(true)
                    .message("Voucher PDF exported successfully")
                    .data(ChequeVoucherPdfDto.builder()
                            .fileName(fileName)
                            .fileType("application/pdf")
                            .doc(base64)
                            .build())
                    .errors(null)
                    .errorCode(0)
                    .responseTime(LocalDateTime.now())
                    .build();
        } catch (Throwable ex) {
            log.error("Unable to export voucher PDF for voucherId={}", voucher.getId(), ex);
            return pdfError("Unable to export voucher PDF", 500, rootCauseMessage(ex));
        }
    }

    private MessageResponseDTO<ChequeVoucherPdfDto> exportChequePdfForVoucher(ChequeVoucher voucher) {
        if (voucher == null) {
            return pdfError("Voucher not found", 404);
        }

        ChequeCustomer customer = chequeCustomerRepository.findByCodeIgnoreCase(voucher.getCustomerCode()).orElse(null);
        String customerName = customer != null ? customer.getDescription() : "";
        LocalDate chequeDate = voucher.getChequeDate() != null ? voucher.getChequeDate() : LocalDate.now();
        BigDecimal amount = voucher.getTotalAmount() != null ? voucher.getTotalAmount() : BigDecimal.ZERO;

        try {
            JasperReport report = getOrLoadChequePrintReport();
            Map<String, Object> params = buildChequePrintReportParams(chequeDate, customerName, amount);
            JRMapCollectionDataSource dataSource = new JRMapCollectionDataSource(List.of(Map.of("row", 1)));

            JasperPrint jasperPrint = JasperFillManager.fillReport(report, params, dataSource);
            byte[] pdfBytes = JasperExportManager.exportReportToPdf(jasperPrint);
            String base64 = Base64.getEncoder().encodeToString(pdfBytes);
            String fileName = "Cheque_" + voucher.getId() + ".pdf";

            return MessageResponseDTO.<ChequeVoucherPdfDto>builder()
                    .success(true)
                    .message("Cheque PDF generated successfully")
                    .data(ChequeVoucherPdfDto.builder()
                            .fileName(fileName)
                            .fileType("application/pdf")
                            .doc(base64)
                            .build())
                    .errors(null)
                    .errorCode(0)
                    .responseTime(LocalDateTime.now())
                    .build();
        } catch (Throwable ex) {
            log.error("Unable to generate cheque PDF for voucherId={}", voucher.getId(), ex);
            return pdfError("Unable to generate cheque PDF", 500, rootCauseMessage(ex));
        }
    }

    private JasperReport getOrLoadChequePrintReport() throws JRException {
        if (chequePrintReport != null) {
            return chequePrintReport;
        }
        synchronized (this) {
            if (chequePrintReport == null) {
                try (InputStream inputStream = ChequeVoucherServiceImpl.class.getResourceAsStream(CHEQUE_PRINT_REPORT_PATH)) {
                    if (inputStream == null) {
                        throw new JRException("Cheque print template not found: " + CHEQUE_PRINT_REPORT_PATH);
                    }
                    chequePrintReport = JasperCompileManager.compileReport(inputStream);
                } catch (Exception ex) {
                    if (ex instanceof JRException jrException) {
                        throw jrException;
                    }
                    throw new JRException("Unable to load cheque print template", ex);
                }
            }
            return chequePrintReport;
        }
    }

    private Map<String, Object> buildChequePrintReportParams(LocalDate chequeDate, String customerName, BigDecimal amount) {
        Map<String, Object> params = new HashMap<>();
        params.put("dateDay", String.format("%02d", chequeDate.getDayOfMonth()));
        params.put("dateMonth", String.format("%02d", chequeDate.getMonthValue()));
        params.put("dateYear", String.valueOf(chequeDate.getYear()));
        params.put("customerName", wrapWithStars(safe(customerName)));
        params.put("amountNumber", wrapWithStars(formatCurrency(amount)));
        String amountWords = wrapWithStars(amountToWords(amount));
        List<String> amountLines = splitTextIntoLines(amountWords, 40, 3);
        params.put("amountWords", amountWords);
        params.put("amountWordsLine1", amountLines.get(0));
        params.put("amountWordsLine2", amountLines.get(1));
        params.put("amountWordsLine3", amountLines.get(2));
        return params;
    }

    private String formatCurrency(BigDecimal amount) {
        BigDecimal value = amount != null ? amount.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(Locale.ENGLISH);
        symbols.setDecimalSeparator('.');
        symbols.setGroupingSeparator(',');
        DecimalFormat format = new DecimalFormat("#,##0.00", symbols);
        return format.format(value);
    }

    private String wrapWithStars(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        return "**" + value.trim() + "**";
    }

    private List<String> splitTextIntoLines(String text, int maxCharsPerLine, int maxLines) {
        if (maxLines <= 0) {
            return List.of();
        }
        List<String> lines = new ArrayList<>();
        if (!StringUtils.hasText(text)) {
            for (int i = 0; i < maxLines; i++) {
                lines.add("");
            }
            return lines;
        }

        String[] words = text.trim().replaceAll("\\s+", " ").split(" ");
        StringBuilder current = new StringBuilder();

        for (String word : words) {
            if (lines.size() == maxLines - 1) {
                if (current.length() > 0) {
                    current.append(' ');
                }
                current.append(word);
                continue;
            }

            if (current.isEmpty()) {
                current.append(word);
                continue;
            }

            if (current.length() + 1 + word.length() <= maxCharsPerLine) {
                current.append(' ').append(word);
            } else {
                lines.add(current.toString());
                current = new StringBuilder(word);
            }
        }

        if (!current.isEmpty()) {
            lines.add(current.toString());
        }

        while (lines.size() < maxLines) {
            lines.add("");
        }
        if (lines.size() > maxLines) {
            return lines.subList(0, maxLines);
        }
        return lines;
    }

    private MessageResponseDTO<ChequeReprintRequestDto> reprintSuccess(String message, ChequeReprintRequestDto data) {
        return MessageResponseDTO.<ChequeReprintRequestDto>builder()
                .success(true)
                .message(message)
                .data(data)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    private MessageResponseDTO<ChequeReprintRequestDto> reprintError(String message, int errorCode) {
        return MessageResponseDTO.<ChequeReprintRequestDto>builder()
                .success(false)
                .message(message)
                .data(null)
                .errors(null)
                .errorCode(errorCode)
                .responseTime(LocalDateTime.now())
                .build();
    }

    private Map<Long, ChequeVoucher> loadVoucherMap(List<Long> voucherIds) {
        if (voucherIds == null || voucherIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, ChequeVoucher> map = new HashMap<>();
        chequeVoucherRepository.findAllById(voucherIds).forEach(voucher -> map.put(voucher.getId(), voucher));
        return map;
    }

    private boolean matchesReprintStatus(String search, ChequeReprintStatus status) {
        if (!StringUtils.hasText(search)) {
            return true;
        }
        String normalized = search.trim().toUpperCase(Locale.ROOT);
        return status != null && status.name().equals(normalized);
    }

    private Comparator<ChequeReprintRequestDto> resolveReprintComparator(String sortColumn, String sortDirection) {
        String column = normalize(sortColumn);
        Comparator<ChequeReprintRequestDto> comparator;
        if ("status".equals(column)) {
            comparator = Comparator.comparing(ChequeReprintRequestDto::getStatus, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
        } else if ("voucherno".equals(column)) {
            comparator = Comparator.comparing(ChequeReprintRequestDto::getVoucherNo, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
        } else if ("requestedby".equals(column)) {
            comparator = Comparator.comparing(ChequeReprintRequestDto::getRequestedBy, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
        } else if ("approveddate".equals(column)) {
            comparator = Comparator.comparing(ChequeReprintRequestDto::getApprovedDate, Comparator.nullsLast(Comparator.naturalOrder()));
        } else if ("rejecteddate".equals(column)) {
            comparator = Comparator.comparing(ChequeReprintRequestDto::getRejectedDate, Comparator.nullsLast(Comparator.naturalOrder()));
        } else {
            comparator = Comparator.comparing(ChequeReprintRequestDto::getRequestedDate, Comparator.nullsLast(Comparator.naturalOrder()));
        }
        String direction = normalize(sortDirection);
        return "asc".equals(direction) ? comparator : comparator.reversed();
    }

    private ChequeReprintRequestDto toReprintDto(ChequeReprintRequest request, ChequeVoucher voucher) {
        if (request == null || voucher == null) {
            return null;
        }
        ChequeCompany company = chequeCompanyRepository.findByCodeIgnoreCase(voucher.getCompanyCode()).orElse(null);
        ChequeCustomer customer = chequeCustomerRepository.findByCodeIgnoreCase(voucher.getCustomerCode()).orElse(null);
        ChequeBank bank = resolveBank(voucher.getBankCode());

        ChequeVoucherStatus voucherStatus = voucher.getStatus() != null ? voucher.getStatus() : ChequeVoucherStatus.DRAFT;
        ChequeReprintStatus status = request.getStatus() != null ? request.getStatus() : ChequeReprintStatus.REPRINT_PENDING;

        return ChequeReprintRequestDto.builder()
                .id(request.getId())
                .voucherId(voucher.getId())
                .voucherNo(voucher.getVoucherNo())
                .companyCode(voucher.getCompanyCode())
                .companyDescription(company != null ? company.getDescription() : null)
                .customerCode(voucher.getCustomerCode())
                .customerDescription(customer != null ? customer.getDescription() : null)
                .bankCode(voucher.getBankCode())
                .bankName(bank != null ? bank.getName() : voucher.getChequeBankName())
                .chequeNo(voucher.getChequeNo())
                .chequeDate(voucher.getChequeDate())
                .totalAmount(voucher.getTotalAmount())
                .voucherStatus(voucherStatus.name())
                .voucherStatusDescription(formatStatus(voucherStatus))
                .reprintReason(request.getReprintReason())
                .status(status.name())
                .statusDescription(formatReprintStatus(status))
                .requestedBy(request.getRequestedBy())
                .requestedDate(request.getRequestedDate())
                .approvedBy(request.getApprovedBy())
                .approvedDate(request.getApprovedDate())
                .rejectedBy(request.getRejectedBy())
                .rejectedDate(request.getRejectedDate())
                .approvalRemark(request.getApprovalRemark())
                .rejectionReason(request.getRejectionReason())
                .usedForPrint(Boolean.TRUE.equals(request.getUsedForPrint()))
                .usedDate(request.getUsedDate())
                .createdDate(request.getCreatedDate())
                .lastModifiedDate(request.getLastModifiedDate())
                .createdBy(request.getCreatedBy())
                .lastModifiedBy(request.getLastModifiedBy())
                .build();
    }

    private String safe(String value) {
        return value != null ? value : "";
    }

    private JasperReport getOrLoadVoucherReport() throws JRException {
        if (voucherReport != null) {
            return voucherReport;
        }
        synchronized (this) {
            if (voucherReport == null) {
                try (InputStream inputStream = ChequeVoucherServiceImpl.class.getResourceAsStream(VOUCHER_REPORT_PATH)) {
                    if (inputStream == null) {
                        throw new JRException("Voucher report template not found: " + VOUCHER_REPORT_PATH);
                    }
                    voucherReport = JasperCompileManager.compileReport(inputStream);
                } catch (Exception ex) {
                    if (ex instanceof JRException jrException) {
                        throw jrException;
                    }
                    throw new JRException("Unable to load voucher report template", ex);
                }
            }
            return voucherReport;
        }
    }

    private Map<String, Object> buildVoucherReportParams(
            ChequeVoucherDto dto,
            ChequeCompany company,
            ChequeCustomer customer) {
        Map<String, Object> params = new HashMap<>();
        params.put("voucherNo", safe(dto.getVoucherNo()));
        params.put("voucherDate", dto.getCreatedDate() != null
                ? dto.getCreatedDate().toLocalDate().toString()
                : LocalDate.now().toString());
        params.put("companyName", company != null ? safe(company.getDescription()) : safe(dto.getCompanyDescription()));
        params.put("companyAddress", buildCompanyAddress(company));
        params.put("companyPhone", company != null ? safe(company.getPhoneNumber()) : "");
        params.put("payer", customer != null ? "M/s " + safe(customer.getDescription()) : safe(dto.getCustomerDescription()));
        params.put("chequeNo", safe(dto.getChequeNo()));
        params.put("chequeBankName", safe(StringUtils.hasText(dto.getBankName()) ? dto.getBankName() : dto.getBankCode()));
        params.put("chequeType", safe(dto.getChequeType()));
        params.put("chequeDate", dto.getChequeDate() != null ? dto.getChequeDate().toString() : "");
        params.put("voucherDescription", safe(dto.getDescription()));
        params.put("status", safe(dto.getStatusDescription()));
        params.put("totalAmount", dto.getTotalAmount() != null ? dto.getTotalAmount().toPlainString() : "0.00");
        params.put("amountInWords", amountToWords(dto.getTotalAmount()));
        params.put("printedAt", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        return params;
    }

    private String buildCompanyAddress(ChequeCompany company) {
        if (company == null) {
            return "";
        }
        List<String> parts = new ArrayList<>();
        if (StringUtils.hasText(company.getStreet1())) {
            parts.add(company.getStreet1().trim());
        }
        if (StringUtils.hasText(company.getStreet2())) {
            parts.add(company.getStreet2().trim());
        }
        if (StringUtils.hasText(company.getCity())) {
            parts.add(company.getCity().trim());
        }
        if (StringUtils.hasText(company.getState())) {
            parts.add(company.getState().trim());
        }
        if (StringUtils.hasText(company.getCountry())) {
            parts.add(company.getCountry().trim());
        }
        if (StringUtils.hasText(company.getZipCode())) {
            parts.add(company.getZipCode().trim());
        }
        return String.join(", ", parts);
    }

    private String amountToWords(BigDecimal amount) {
        if (amount == null) {
            return "ZERO ONLY";
        }
        long value = amount.setScale(0, RoundingMode.HALF_UP).longValue();
        if (value == 0) {
            return "ZERO ONLY";
        }
        return numberToWords(value).trim().toUpperCase(Locale.ROOT) + " ONLY";
    }

    private String numberToWords(long number) {
        if (number == 0) {
            return "zero";
        }

        String[] tensNames = {
                "", " ten", " twenty", " thirty", " forty", " fifty",
                " sixty", " seventy", " eighty", " ninety"
        };
        String[] numNames = {
                "", " one", " two", " three", " four", " five", " six", " seven",
                " eight", " nine", " ten", " eleven", " twelve", " thirteen", " fourteen",
                " fifteen", " sixteen", " seventeen", " eighteen", " nineteen"
        };

        StringBuilder words = new StringBuilder();
        long[] divisors = {1_000_000_000L, 1_000_000L, 1_000L, 1L};
        String[] labels = {" billion", " million", " thousand", ""};

        for (int i = 0; i < divisors.length; i++) {
            long divisor = divisors[i];
            int chunk = (int) (number / divisor);
            if (chunk > 0) {
                words.append(threeDigitToWords(chunk, numNames, tensNames)).append(labels[i]);
                number %= divisor;
            }
        }
        return words.toString().replaceAll("\\s+", " ").trim();
    }

    private String threeDigitToWords(int number, String[] numNames, String[] tensNames) {
        String current;
        if (number % 100 < 20) {
            current = numNames[number % 100];
            number /= 100;
        } else {
            current = numNames[number % 10];
            number /= 10;
            current = tensNames[number % 10] + current;
            number /= 10;
        }
        if (number == 0) {
            return current;
        }
        return numNames[number] + " hundred" + current;
    }

    private List<Map<String, ?>> buildVoucherRows(ChequeVoucherDto dto) {
        if (dto.getInvoices() == null || dto.getInvoices().isEmpty()) {
            return List.of();
        }
        List<Map<String, ?>> rows = new ArrayList<>();
        for (ChequeVoucherInvoiceDto line : dto.getInvoices()) {
            Map<String, Object> row = new HashMap<>();
            row.put("invoiceDate", line.getInvoiceDate() != null ? line.getInvoiceDate().toString() : "");
            row.put("invoiceNo", safe(line.getInvoiceNo()));
            row.put("invoiceDescription", safe(line.getDescription()));
            row.put("amount", line.getAmount() != null ? line.getAmount() : BigDecimal.ZERO);
            rows.add(row);
        }
        return rows;
    }

    private boolean isValidCreateRequest(ChequeVoucherCreateRequest request) {
        return request != null
                && StringUtils.hasText(request.getCompanyCode())
                && StringUtils.hasText(request.getCustomerCode())
                && StringUtils.hasText(request.getChequeNo())
                && StringUtils.hasText(request.getBankCode())
                && StringUtils.hasText(request.getChequeType())
                && request.getInvoices() != null
                && !request.getInvoices().isEmpty();
    }

    private boolean isValidInvoiceLines(List<ChequeVoucherInvoiceRequest> invoices) {
        if (invoices == null || invoices.isEmpty()) {
            return false;
        }
        for (ChequeVoucherInvoiceRequest invoice : invoices) {
            if (invoice == null
                    || invoice.getInvoiceDate() == null
                    || !StringUtils.hasText(invoice.getInvoiceNo())
                    || invoice.getAmount() == null
                    || invoice.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                return false;
            }
        }
        return true;
    }

    private ChequeType resolveChequeType(String chequeType) {
        if (!StringUtils.hasText(chequeType)) {
            return null;
        }
        try {
            return ChequeType.valueOf(chequeType.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private LocalDate resolveChequeDate(ChequeType chequeType, LocalDate requestedChequeDate) {
        if (chequeType == null) {
            return null;
        }
        if (chequeType == ChequeType.DATED) {
            return requestedChequeDate;
        }
        return LocalDate.now();
    }

    private ChequeCompany resolveActiveCompany(String companyCode) {
        if (!StringUtils.hasText(companyCode)) {
            return null;
        }
        ChequeCompany company = chequeCompanyRepository.findByCodeIgnoreCase(companyCode.trim()).orElse(null);
        if (company == null || company.getStatus() != ChequeCompanyStatus.ACTIVE) {
            return null;
        }
        return company;
    }

    private ChequeCustomer resolveActiveCustomer(String customerCode) {
        if (!StringUtils.hasText(customerCode)) {
            return null;
        }
        ChequeCustomer customer = chequeCustomerRepository.findByCodeIgnoreCase(customerCode.trim()).orElse(null);
        if (customer == null || customer.getStatus() != ChequeCustomerStatus.ACTIVE) {
            return null;
        }
        return customer;
    }

    private ChequeBank resolveActiveBank(String bankCode) {
        if (!StringUtils.hasText(bankCode)) {
            return null;
        }
        ChequeBank bank = chequeBankRepository.findByCodeIgnoreCase(bankCode.trim()).orElse(null);
        if (bank == null || bank.getStatus() != ChequeBankStatus.ACTIVE) {
            return null;
        }
        return bank;
    }

    private ChequeBank resolveBank(String bankCode) {
        if (!StringUtils.hasText(bankCode)) {
            return null;
        }
        return chequeBankRepository.findByCodeIgnoreCase(bankCode.trim()).orElse(null);
    }

    private List<ChequeVoucherInvoice> buildInvoices(ChequeVoucher voucher, List<ChequeVoucherInvoiceRequest> requests) {
        List<ChequeVoucherInvoice> invoices = new ArrayList<>();
        int lineNo = 1;
        for (ChequeVoucherInvoiceRequest line : requests) {
            invoices.add(ChequeVoucherInvoice.builder()
                    .voucher(voucher)
                    .lineNo(lineNo++)
                    .invoiceDate(line.getInvoiceDate())
                    .invoiceNo(line.getInvoiceNo().trim())
                    .description(trimToNull(line.getDescription()))
                    .amount(line.getAmount().setScale(2, RoundingMode.HALF_UP))
                    .build());
        }
        return invoices;
    }

    private BigDecimal calculateTotal(List<ChequeVoucherInvoiceRequest> invoices) {
        return invoices.stream()
                .map(ChequeVoucherInvoiceRequest::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private String generateVoucherNo() {
        LocalDate today = LocalDate.now();
        LocalDateTime from = LocalDateTime.of(today, LocalTime.MIN);
        LocalDateTime to = LocalDateTime.of(today, LocalTime.MAX);
        long sequence = chequeVoucherRepository.countByCreatedDateBetween(from, to) + 1;
        String datePart = today.format(DateTimeFormatter.BASIC_ISO_DATE);

        String voucherNo;
        do {
            voucherNo = "VCH" + datePart + String.format(Locale.ROOT, "%04d", sequence++);
        } while (chequeVoucherRepository.existsByVoucherNo(voucherNo));
        return voucherNo;
    }

    private ChequeVoucherPrivilegesDto resolvePrivileges(ChequeVoucherReferenceDataRequest request, String pageCode) {
        String username = request != null ? request.getUsername() : null;
        if (!StringUtils.hasText(username)) {
            return emptyPrivileges();
        }

        UserAccount user = userAccountRepository.findByUsername(username).orElse(null);
        if (user == null || user.getRole() == null || !StringUtils.hasText(user.getRole().getCode())) {
            return emptyPrivileges();
        }

        try {
            var access = authModuleClient.getRolePageTaskAccess(user.getRole().getCode());
            if (access == null || access.getPages() == null) {
                return emptyPrivileges();
            }

            Map<String, Boolean> taskAccess = new HashMap<>();
            access.getPages().stream()
                    .filter(page -> pageCode.equalsIgnoreCase(page.getPageCode()))
                    .findFirst()
                    .ifPresent(page -> {
                        if (page.getTasks() != null) {
                            page.getTasks().forEach(task -> {
                                String codeKey = normalizeTaskKey(task.getTaskCode());
                                if (StringUtils.hasText(codeKey)) {
                                    taskAccess.put(codeKey, task.isCanAccess());
                                }
                                String nameKey = normalizeTaskKey(task.getTaskName());
                                if (StringUtils.hasText(nameKey)) {
                                    taskAccess.putIfAbsent(nameKey, task.isCanAccess());
                                }
                            });
                        }
                    });

            return ChequeVoucherPrivilegesDto.builder()
                    .add(hasTask(taskAccess, "ADD", "CREATE", "NEW"))
                    .update(hasTask(taskAccess, "UPDATE", "EDIT"))
                    .view(hasTask(taskAccess, "VIEW", "READ"))
                    .search(hasTask(taskAccess, "SEARCH", "FILTER", "LIST"))
                    .delete(hasTask(taskAccess, "DELETE", "REMOVE", "DEACTIVATE"))
                    .export(hasTask(taskAccess, "EXPORT", "DOWNLOAD", "PDF"))
                    .approve(hasTask(taskAccess, "APPROVE", "AUTHORIZE", "AUTH"))
                    .print(hasTask(taskAccess, "PRINT"))
                    .requestReprint(hasTask(taskAccess, "REPRINT", "REQUESTREPRINT"))
                    .build();
        } catch (Exception ex) {
            return emptyPrivileges();
        }
    }

    private MessageResponseDTO<ChequeVoucherDto> success(String message, ChequeVoucherDto data) {
        return MessageResponseDTO.<ChequeVoucherDto>builder()
                .success(true)
                .message(message)
                .data(data)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    private MessageResponseDTO<ChequeVoucherDto> error(String message, int errorCode) {
        return MessageResponseDTO.<ChequeVoucherDto>builder()
                .success(false)
                .message(message)
                .data(null)
                .errors(null)
                .errorCode(errorCode)
                .responseTime(LocalDateTime.now())
                .build();
    }

    private MessageResponseDTO<ChequeVoucherPdfDto> pdfError(String message, int errorCode) {
        return pdfError(message, errorCode, null);
    }

    private MessageResponseDTO<ChequeVoucherPdfDto> pdfError(String message, int errorCode, String errors) {
        return MessageResponseDTO.<ChequeVoucherPdfDto>builder()
                .success(false)
                .message(message)
                .data(null)
                .errors(errors)
                .errorCode(errorCode)
                .responseTime(LocalDateTime.now())
                .build();
    }

    private String rootCauseMessage(Throwable throwable) {
        if (throwable == null) {
            return null;
        }
        Throwable root = throwable;
        while (root.getCause() != null && root.getCause() != root) {
            root = root.getCause();
        }
        String rootMessage = root.getMessage();
        if (!StringUtils.hasText(rootMessage)) {
            rootMessage = throwable.getMessage();
        }
        return StringUtils.hasText(rootMessage) ? rootMessage : root.getClass().getSimpleName();
    }

    private ChequeVoucherDto toDto(
            ChequeVoucher voucher,
            ChequeCompany company,
            ChequeCustomer customer,
            ChequeBank bank) {
        ChequeVoucherStatus status = voucher.getStatus() != null ? voucher.getStatus() : ChequeVoucherStatus.DRAFT;
        return ChequeVoucherDto.builder()
                .id(voucher.getId())
                .voucherNo(voucher.getVoucherNo())
                .companyCode(voucher.getCompanyCode())
                .companyDescription(company != null ? company.getDescription() : null)
                .customerCode(voucher.getCustomerCode())
                .customerDescription(customer != null ? customer.getDescription() : null)
                .chequeNo(voucher.getChequeNo())
                .bankCode(voucher.getBankCode())
                .bankName(bank != null ? bank.getName() : voucher.getChequeBankName())
                .chequeType(voucher.getChequeType() != null ? voucher.getChequeType().name() : null)
                .chequeDate(voucher.getChequeDate())
                .description(voucher.getDescription())
                .totalAmount(voucher.getTotalAmount())
                .amountInWords(amountToWords(voucher.getTotalAmount()))
                .status(status)
                .statusDescription(formatStatus(status))
                .submittedBy(voucher.getSubmittedBy())
                .submittedDate(voucher.getSubmittedDate())
                .approvedBy(voucher.getApprovedBy())
                .approvedDate(voucher.getApprovedDate())
                .rejectedBy(voucher.getRejectedBy())
                .rejectedDate(voucher.getRejectedDate())
                .rejectionReason(voucher.getRejectionReason())
                .approvalRemark(voucher.getApprovalRemark())
                .printStatus(resolvePrintStatus(voucher).name())
                .printStatusDescription(formatPrintStatus(resolvePrintStatus(voucher)))
                .printCount(voucher.getPrintCount() != null ? voucher.getPrintCount() : 0)
                .lastPrintedBy(voucher.getLastPrintedBy())
                .lastPrintedDate(voucher.getLastPrintedDate())
                .invoices(voucher.getInvoices() != null ? voucher.getInvoices().stream().map(this::toInvoiceDto).toList() : List.of())
                .createdDate(voucher.getCreatedDate())
                .lastModifiedDate(voucher.getLastModifiedDate())
                .createdBy(voucher.getCreatedBy())
                .lastModifiedBy(voucher.getLastModifiedBy())
                .build();
    }

    private ChequeVoucherInvoiceDto toInvoiceDto(ChequeVoucherInvoice invoice) {
        return ChequeVoucherInvoiceDto.builder()
                .lineNo(invoice.getLineNo())
                .invoiceDate(invoice.getInvoiceDate())
                .invoiceNo(invoice.getInvoiceNo())
                .description(invoice.getDescription())
                .amount(invoice.getAmount())
                .build();
    }

    private ChequeVoucherListItemDto toListItem(
            ChequeVoucher voucher,
            String companyDescription,
            String customerDescription,
            String bankName) {
        ChequeVoucherStatus status = voucher.getStatus() != null ? voucher.getStatus() : ChequeVoucherStatus.DRAFT;
        ChequePrintStatus printStatus = resolvePrintStatus(voucher);
        return ChequeVoucherListItemDto.builder()
                .id(voucher.getId())
                .voucherNo(voucher.getVoucherNo())
                .companyCode(voucher.getCompanyCode())
                .companyDescription(companyDescription)
                .customerCode(voucher.getCustomerCode())
                .customerDescription(customerDescription)
                .chequeNo(voucher.getChequeNo())
                .bankCode(voucher.getBankCode())
                .bankName(StringUtils.hasText(bankName) ? bankName : voucher.getChequeBankName())
                .chequeType(voucher.getChequeType() != null ? voucher.getChequeType().name() : null)
                .chequeDate(voucher.getChequeDate())
                .description(voucher.getDescription())
                .totalAmount(voucher.getTotalAmount())
                .status(status.name())
                .statusDescription(formatStatus(status))
                .printStatus(printStatus.name())
                .printStatusDescription(formatPrintStatus(printStatus))
                .printCount(voucher.getPrintCount() != null ? voucher.getPrintCount() : 0)
                .lastPrintedBy(voucher.getLastPrintedBy())
                .lastPrintedDate(voucher.getLastPrintedDate())
                .submittedDate(voucher.getSubmittedDate())
                .approvedDate(voucher.getApprovedDate())
                .rejectedDate(voucher.getRejectedDate())
                .rejectionReason(voucher.getRejectionReason())
                .createdDate(voucher.getCreatedDate())
                .lastModifiedDate(voucher.getLastModifiedDate())
                .createdBy(voucher.getCreatedBy())
                .lastModifiedBy(voucher.getLastModifiedBy())
                .build();
    }

    private String formatStatus(ChequeVoucherStatus status) {
        return switch (status) {
            case DRAFT -> "Draft";
            case PENDING_APPROVAL -> "Pending Approval";
            case APPROVED -> "Approved";
            case REJECTED -> "Rejected";
            case CHEQUE_CREATED -> "Cheque Created";
        };
    }

    private ChequePrintStatus resolvePrintStatus(ChequeVoucher voucher) {
        if (voucher != null && voucher.getPrintStatus() != null) {
            return voucher.getPrintStatus();
        }
        int count = voucher != null && voucher.getPrintCount() != null ? voucher.getPrintCount() : 0;
        return count > 0 ? ChequePrintStatus.PRINTED : ChequePrintStatus.NOT_PRINTED;
    }

    private String formatPrintStatus(ChequePrintStatus status) {
        return status == ChequePrintStatus.PRINTED ? "Printed" : "Not Printed";
    }

    private String normalize(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeUsername(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String normalizeTaskKey(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.replaceAll("[^A-Za-z0-9]", "").toUpperCase(Locale.ROOT);
    }

    private boolean hasTask(Map<String, Boolean> taskAccess, String... tokens) {
        if (taskAccess == null || taskAccess.isEmpty() || tokens == null) {
            return false;
        }
        for (Map.Entry<String, Boolean> entry : taskAccess.entrySet()) {
            if (!Boolean.TRUE.equals(entry.getValue())) {
                continue;
            }
            String key = entry.getKey();
            if (!StringUtils.hasText(key)) {
                continue;
            }
            for (String token : tokens) {
                if (StringUtils.hasText(token) && key.contains(token)) {
                    return true;
                }
            }
        }
        return false;
    }

    private ChequeVoucherPrivilegesDto emptyPrivileges() {
        return ChequeVoucherPrivilegesDto.builder()
                .add(false)
                .update(false)
                .view(false)
                .search(false)
                .delete(false)
                .export(false)
                .approve(false)
                .print(false)
                .requestReprint(false)
                .build();
    }

    private boolean matches(String search, String actual) {
        if (!StringUtils.hasText(search)) {
            return true;
        }
        return actual != null && actual.toLowerCase(Locale.ROOT).contains(search);
    }

    private boolean matchesStatus(String search, ChequeVoucherStatus status) {
        if (!StringUtils.hasText(search)) {
            return true;
        }
        String normalized = search.trim().toUpperCase(Locale.ROOT);
        return status != null && status.name().equals(normalized);
    }

    private boolean matchesPrintStatus(String search, ChequePrintStatus status) {
        if (!StringUtils.hasText(search)) {
            return true;
        }
        String normalized = search.trim().toUpperCase(Locale.ROOT);
        ChequePrintStatus current = status != null ? status : ChequePrintStatus.NOT_PRINTED;
        return current.name().equals(normalized);
    }

    private boolean matchesChequeType(String search, ChequeType chequeType) {
        if (!StringUtils.hasText(search)) {
            return true;
        }
        String normalized = search.trim().toUpperCase(Locale.ROOT);
        return chequeType != null && chequeType.name().contains(normalized);
    }

    private Comparator<ChequeVoucherListItemDto> resolveComparator(String sortColumn, String sortDirection) {
        String column = normalize(sortColumn);
        Comparator<ChequeVoucherListItemDto> comparator;
        if ("companycode".equals(column)) {
            comparator = Comparator.comparing(ChequeVoucherListItemDto::getCompanyCode, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
        } else if ("customercode".equals(column)) {
            comparator = Comparator.comparing(ChequeVoucherListItemDto::getCustomerCode, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
        } else if ("chequeno".equals(column)) {
            comparator = Comparator.comparing(ChequeVoucherListItemDto::getChequeNo, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
        } else if ("bankcode".equals(column)) {
            comparator = Comparator.comparing(ChequeVoucherListItemDto::getBankCode, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
        } else if ("bankname".equals(column) || "chequebankname".equals(column)) {
            comparator = Comparator.comparing(ChequeVoucherListItemDto::getBankName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
        } else if ("chequetype".equals(column)) {
            comparator = Comparator.comparing(ChequeVoucherListItemDto::getChequeType, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
        } else if ("chequedate".equals(column)) {
            comparator = Comparator.comparing(ChequeVoucherListItemDto::getChequeDate, Comparator.nullsLast(Comparator.naturalOrder()));
        } else if ("status".equals(column)) {
            comparator = Comparator.comparing(ChequeVoucherListItemDto::getStatus, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
        } else if ("printstatus".equals(column)) {
            comparator = Comparator.comparing(ChequeVoucherListItemDto::getPrintStatus, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
        } else if ("printcount".equals(column)) {
            comparator = Comparator.comparing(ChequeVoucherListItemDto::getPrintCount, Comparator.nullsLast(Comparator.naturalOrder()));
        } else if ("totalamount".equals(column)) {
            comparator = Comparator.comparing(ChequeVoucherListItemDto::getTotalAmount, Comparator.nullsLast(Comparator.naturalOrder()));
        } else if ("createddate".equals(column)) {
            comparator = Comparator.comparing(ChequeVoucherListItemDto::getCreatedDate, Comparator.nullsLast(Comparator.naturalOrder()));
        } else if ("lastmodifieddate".equals(column)) {
            comparator = Comparator.comparing(ChequeVoucherListItemDto::getLastModifiedDate, Comparator.nullsLast(Comparator.naturalOrder()));
        } else {
            comparator = Comparator.comparing(ChequeVoucherListItemDto::getVoucherNo, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
        }

        String direction = normalize(sortDirection);
        return "desc".equals(direction) ? comparator.reversed() : comparator;
    }

    private String formatReprintStatus(ChequeReprintStatus status) {
        return switch (status) {
            case REPRINT_PENDING -> "Reprint Pending";
            case REPRINT_APPROVED -> "Reprint Approved";
            case REPRINT_REJECTED -> "Reprint Rejected";
        };
    }

}
