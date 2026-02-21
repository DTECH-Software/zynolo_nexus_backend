package com.zynolo_nexus.cheque_service.service.impl;

import com.zynolo_nexus.cheque_service.client.AuthModuleClient;
import com.zynolo_nexus.cheque_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.cheque_service.dto.request.ChequeVoucherCreateRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeVoucherExportPdfRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeVoucherFilterRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeVoucherFilterSearch;
import com.zynolo_nexus.cheque_service.dto.request.ChequeVoucherInvoiceRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeVoucherReferenceDataRequest;
import com.zynolo_nexus.cheque_service.dto.request.ChequeVoucherUpdateRequest;
import com.zynolo_nexus.cheque_service.dto.response.ChequeReferenceCompanyDto;
import com.zynolo_nexus.cheque_service.dto.response.ChequeReferenceCustomerDto;
import com.zynolo_nexus.cheque_service.dto.response.ChequeReferenceStatusDto;
import com.zynolo_nexus.cheque_service.dto.response.ChequeVoucherDto;
import com.zynolo_nexus.cheque_service.dto.response.ChequeVoucherFilterResultDto;
import com.zynolo_nexus.cheque_service.dto.response.ChequeVoucherInvoiceDto;
import com.zynolo_nexus.cheque_service.dto.response.ChequeVoucherListItemDto;
import com.zynolo_nexus.cheque_service.dto.response.ChequeVoucherPdfDto;
import com.zynolo_nexus.cheque_service.dto.response.ChequeVoucherPrivilegesDto;
import com.zynolo_nexus.cheque_service.dto.response.ChequeVoucherReferenceDataDto;
import com.zynolo_nexus.cheque_service.enums.ChequeCompanyStatus;
import com.zynolo_nexus.cheque_service.enums.ChequeCustomerStatus;
import com.zynolo_nexus.cheque_service.enums.ChequeVoucherStatus;
import com.zynolo_nexus.cheque_service.model.ChequeCompany;
import com.zynolo_nexus.cheque_service.model.ChequeCustomer;
import com.zynolo_nexus.cheque_service.model.ChequeVoucher;
import com.zynolo_nexus.cheque_service.model.ChequeVoucherInvoice;
import com.zynolo_nexus.cheque_service.model.UserAccount;
import com.zynolo_nexus.cheque_service.repository.ChequeCompanyRepository;
import com.zynolo_nexus.cheque_service.repository.ChequeCustomerRepository;
import com.zynolo_nexus.cheque_service.repository.ChequeVoucherRepository;
import com.zynolo_nexus.cheque_service.repository.UserAccountRepository;
import com.zynolo_nexus.cheque_service.service.ChequeVoucherService;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChequeVoucherServiceImpl implements ChequeVoucherService {

    private static final String PAGE_CODE = "CHVM";

    private final ChequeVoucherRepository chequeVoucherRepository;
    private final ChequeCompanyRepository chequeCompanyRepository;
    private final ChequeCustomerRepository chequeCustomerRepository;
    private final UserAccountRepository userAccountRepository;
    private final AuthModuleClient authModuleClient;

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

        if (!isValidInvoiceLines(request.getInvoices())) {
            return error("Invalid invoice details", 400);
        }

        String actor = normalizeUsername(request.getUsername());
        String voucherNo = generateVoucherNo();
        BigDecimal totalAmount = calculateTotal(request.getInvoices());

        ChequeVoucher voucher = ChequeVoucher.builder()
                .voucherNo(voucherNo)
                .companyCode(company.getCode())
                .customerCode(customer.getCode())
                .chequeNo(request.getChequeNo().trim())
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
        return success("Voucher created successfully", toDto(voucher, company, customer));
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
        return success("Voucher found with ID: " + id, toDto(voucher, company, customer));
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

        voucher.setCompanyCode(company.getCode());
        voucher.setCustomerCode(customer.getCode());
        voucher.setChequeNo(request.getChequeNo().trim());
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
        return success("Voucher updated successfully", toDto(voucher, company, customer));
    }

    @Override
    @Transactional(readOnly = true)
    public MessageResponseDTO<ChequeVoucherFilterResultDto> filterList(ChequeVoucherFilterRequest request) {
        List<ChequeVoucher> vouchers = chequeVoucherRepository.findAll();

        ChequeVoucherFilterSearch search = request != null ? request.getSearch() : null;
        String voucherNo = normalize(search != null ? search.getVoucherNo() : null);
        String companyCode = normalize(search != null ? search.getCompanyCode() : null);
        String customerCode = normalize(search != null ? search.getCustomerCode() : null);
        String chequeNo = normalize(search != null ? search.getChequeNo() : null);
        String status = normalize(search != null ? search.getStatus() : null);

        Map<String, String> companyDescriptions = new HashMap<>();
        chequeCompanyRepository.findAll()
                .forEach(company -> companyDescriptions.put(company.getCode(), company.getDescription()));
        Map<String, String> customerDescriptions = new HashMap<>();
        chequeCustomerRepository.findAll()
                .forEach(customer -> customerDescriptions.put(customer.getCode(), customer.getDescription()));

        List<ChequeVoucherListItemDto> filtered = vouchers.stream()
                .filter(voucher -> matches(voucherNo, voucher.getVoucherNo()))
                .filter(voucher -> matches(companyCode, voucher.getCompanyCode()))
                .filter(voucher -> matches(customerCode, voucher.getCustomerCode()))
                .filter(voucher -> matches(chequeNo, voucher.getChequeNo()))
                .filter(voucher -> matchesStatus(status, voucher.getStatus()))
                .map(voucher -> toListItem(
                        voucher,
                        companyDescriptions.get(voucher.getCompanyCode()),
                        customerDescriptions.get(voucher.getCustomerCode())))
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
                .message("Voucher list filtered successfully")
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
                : PAGE_CODE;

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

        ChequeVoucherReferenceDataDto data = ChequeVoucherReferenceDataDto.builder()
                .defaultStatus(List.of(
                        ChequeReferenceStatusDto.builder().code("DRAFT").description("Draft").build(),
                        ChequeReferenceStatusDto.builder().code("PENDING_APPROVAL").description("Pending Approval").build(),
                        ChequeReferenceStatusDto.builder().code("APPROVED").description("Approved").build(),
                        ChequeReferenceStatusDto.builder().code("REJECTED").description("Rejected").build(),
                        ChequeReferenceStatusDto.builder().code("CHEQUE_CREATED").description("Cheque Created").build()
                ))
                .companies(companies)
                .customers(customers)
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
        if (request == null || request.getVoucherId() == null) {
            return pdfError("Invalid voucher export request", 400);
        }

        ChequeVoucher voucher = chequeVoucherRepository.findById(request.getVoucherId()).orElse(null);
        if (voucher == null) {
            return pdfError("Voucher not found", 404);
        }

        ChequeCompany company = chequeCompanyRepository.findByCodeIgnoreCase(voucher.getCompanyCode()).orElse(null);
        ChequeCustomer customer = chequeCustomerRepository.findByCodeIgnoreCase(voucher.getCustomerCode()).orElse(null);
        ChequeVoucherDto dto = toDto(voucher, company, customer);

        try (PDDocument document = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.setLeading(16f);
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
                contentStream.newLineAtOffset(50, 780);
                contentStream.showText("Cheque Voucher");
                contentStream.newLine();
                contentStream.newLine();
                contentStream.setFont(PDType1Font.HELVETICA, 11);

                writeLine(contentStream, "Voucher No: " + safe(dto.getVoucherNo()));
                writeLine(contentStream, "Company: " + safe(dto.getCompanyCode()) + " - " + safe(dto.getCompanyDescription()));
                writeLine(contentStream, "Customer: " + safe(dto.getCustomerCode()) + " - " + safe(dto.getCustomerDescription()));
                writeLine(contentStream, "Cheque No: " + safe(dto.getChequeNo()));
                writeLine(contentStream, "Description: " + safe(dto.getDescription()));
                writeLine(contentStream, "Status: " + safe(dto.getStatusDescription()));
                writeLine(contentStream, "Total Amount: " + (dto.getTotalAmount() != null ? dto.getTotalAmount().toPlainString() : "0.00"));
                contentStream.newLine();
                writeLine(contentStream, "Invoice Lines");
                contentStream.newLine();

                if (dto.getInvoices() != null) {
                    for (ChequeVoucherInvoiceDto line : dto.getInvoices()) {
                        String invoiceLine = String.format(
                                Locale.ROOT,
                                "%s | %s | %s | %s",
                                line.getInvoiceDate() != null ? line.getInvoiceDate().toString() : "",
                                safe(line.getInvoiceNo()),
                                safe(line.getDescription()),
                                line.getAmount() != null ? line.getAmount().toPlainString() : "0.00"
                        );
                        writeLine(contentStream, invoiceLine);
                    }
                }
                contentStream.endText();
            }

            document.save(out);
            String base64 = Base64.getEncoder().encodeToString(out.toByteArray());
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
        } catch (Exception ex) {
            return pdfError("Unable to export voucher PDF", 500);
        }
    }

    private void writeLine(PDPageContentStream contentStream, String text) throws Exception {
        contentStream.showText(text);
        contentStream.newLine();
    }

    private String safe(String value) {
        return value != null ? value : "";
    }

    private boolean isValidCreateRequest(ChequeVoucherCreateRequest request) {
        return request != null
                && StringUtils.hasText(request.getCompanyCode())
                && StringUtils.hasText(request.getCustomerCode())
                && StringUtils.hasText(request.getChequeNo())
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
        return MessageResponseDTO.<ChequeVoucherPdfDto>builder()
                .success(false)
                .message(message)
                .data(null)
                .errors(null)
                .errorCode(errorCode)
                .responseTime(LocalDateTime.now())
                .build();
    }

    private ChequeVoucherDto toDto(ChequeVoucher voucher, ChequeCompany company, ChequeCustomer customer) {
        ChequeVoucherStatus status = voucher.getStatus() != null ? voucher.getStatus() : ChequeVoucherStatus.DRAFT;
        return ChequeVoucherDto.builder()
                .id(voucher.getId())
                .voucherNo(voucher.getVoucherNo())
                .companyCode(voucher.getCompanyCode())
                .companyDescription(company != null ? company.getDescription() : null)
                .customerCode(voucher.getCustomerCode())
                .customerDescription(customer != null ? customer.getDescription() : null)
                .chequeNo(voucher.getChequeNo())
                .description(voucher.getDescription())
                .totalAmount(voucher.getTotalAmount())
                .status(status)
                .statusDescription(formatStatus(status))
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

    private ChequeVoucherListItemDto toListItem(ChequeVoucher voucher, String companyDescription, String customerDescription) {
        ChequeVoucherStatus status = voucher.getStatus() != null ? voucher.getStatus() : ChequeVoucherStatus.DRAFT;
        return ChequeVoucherListItemDto.builder()
                .id(voucher.getId())
                .voucherNo(voucher.getVoucherNo())
                .companyCode(voucher.getCompanyCode())
                .companyDescription(companyDescription)
                .customerCode(voucher.getCustomerCode())
                .customerDescription(customerDescription)
                .chequeNo(voucher.getChequeNo())
                .description(voucher.getDescription())
                .totalAmount(voucher.getTotalAmount())
                .status(status.name())
                .statusDescription(formatStatus(status))
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

    private Comparator<ChequeVoucherListItemDto> resolveComparator(String sortColumn, String sortDirection) {
        String column = normalize(sortColumn);
        Comparator<ChequeVoucherListItemDto> comparator;
        if ("companycode".equals(column)) {
            comparator = Comparator.comparing(ChequeVoucherListItemDto::getCompanyCode, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
        } else if ("customercode".equals(column)) {
            comparator = Comparator.comparing(ChequeVoucherListItemDto::getCustomerCode, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
        } else if ("chequeno".equals(column)) {
            comparator = Comparator.comparing(ChequeVoucherListItemDto::getChequeNo, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
        } else if ("status".equals(column)) {
            comparator = Comparator.comparing(ChequeVoucherListItemDto::getStatus, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
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
}
