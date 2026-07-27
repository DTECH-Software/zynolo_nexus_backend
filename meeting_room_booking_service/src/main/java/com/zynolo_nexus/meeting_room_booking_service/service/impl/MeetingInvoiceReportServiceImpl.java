package com.zynolo_nexus.meeting_room_booking_service.service.impl;

import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.zynolo_nexus.meeting_room_booking_service.context.CompanyContext;
import com.zynolo_nexus.meeting_room_booking_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingInvoiceReportExportRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingInvoiceReportFilterRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingInvoiceReportFilterSearch;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingInvoiceReportReferenceDataRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingInvoiceReportViewRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingInvoiceDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingInvoiceLineDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingInvoiceReportExportDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingInvoiceReportFilterResultDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingInvoiceReportListItemDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingInvoiceReportPrivilegesDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingInvoiceReportReferenceDataDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingInvoiceReportSummaryDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.ReferenceOptionDto;
import com.zynolo_nexus.meeting_room_booking_service.enums.MeetingBookingType;
import com.zynolo_nexus.meeting_room_booking_service.enums.MeetingInvoiceStatus;
import com.zynolo_nexus.meeting_room_booking_service.exception.BadRequestException;
import com.zynolo_nexus.meeting_room_booking_service.exception.ResourceNotFoundException;
import com.zynolo_nexus.meeting_room_booking_service.model.CompanyLookup;
import com.zynolo_nexus.meeting_room_booking_service.model.MeetingInvoice;
import com.zynolo_nexus.meeting_room_booking_service.model.MeetingInvoiceLine;
import com.zynolo_nexus.meeting_room_booking_service.repository.CompanyLookupRepository;
import com.zynolo_nexus.meeting_room_booking_service.repository.MeetingInvoiceRepository;
import com.zynolo_nexus.meeting_room_booking_service.service.MeetingInvoiceReportService;
import com.zynolo_nexus.meeting_room_booking_service.service.support.PagePrivilegeResolver;
import com.zynolo_nexus.meeting_room_booking_service.service.support.PageTaskPrivileges;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class MeetingInvoiceReportServiceImpl implements MeetingInvoiceReportService {

    private static final String PAGE_CODE = "MBM_RPRT_INVR";

    private final MeetingInvoiceRepository meetingInvoiceRepository;
    private final CompanyLookupRepository companyLookupRepository;
    private final PagePrivilegeResolver pagePrivilegeResolver;

    @Value("${app.default.company-id:1}")
    private Long defaultCompanyId;

    @Override
    @Transactional(readOnly = true)
    public MessageResponseDTO<MeetingInvoiceReportReferenceDataDto> referenceData(MeetingInvoiceReportReferenceDataRequest request) {
        Long companyId = resolveCompanyId();
        CompanyLookup company = resolveCompany(companyId);
        PageTaskPrivileges privileges = pagePrivilegeResolver.resolve(request != null ? request.getUsername() : null, PAGE_CODE);

        return MessageResponseDTO.<MeetingInvoiceReportReferenceDataDto>builder()
                .success(true)
                .message("Meeting invoice report reference data loaded successfully")
                .data(MeetingInvoiceReportReferenceDataDto.builder()
                        .companyId(companyId)
                        .companyCode(resolveCompanyCode(company))
                        .companyName(resolveCompanyName(company))
                        .meetingTypes(toOptions(MeetingBookingType.values()))
                        .statuses(toOptions(MeetingInvoiceStatus.values()))
                        .exportTypes(exportTypes())
                        .privileges(toPrivileges(privileges))
                        .build())
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public MessageResponseDTO<MeetingInvoiceReportFilterResultDto> filterList(MeetingInvoiceReportFilterRequest request) {
        int page = request != null && request.getPage() != null && request.getPage() >= 0 ? request.getPage() : 0;
        int size = request != null && request.getSize() != null && request.getSize() > 0 ? request.getSize() : 10;
        List<MeetingInvoice> invoices = filteredInvoices(request != null ? request.getSearch() : null);
        MeetingInvoiceReportSummaryDto summary = toSummary(invoices);

        List<MeetingInvoiceReportListItemDto> rows = invoices.stream()
                .sorted(resolveComparator(request))
                .map(this::toListItem)
                .toList();
        int from = Math.min(page * size, rows.size());
        int to = Math.min(from + size, rows.size());
        List<MeetingInvoiceReportListItemDto> content = rows.subList(from, to);
        int totalPages = rows.isEmpty() ? 0 : (int) Math.ceil((double) rows.size() / size);

        return MessageResponseDTO.<MeetingInvoiceReportFilterResultDto>builder()
                .success(true)
                .message("Meeting invoice report filtered successfully")
                .data(MeetingInvoiceReportFilterResultDto.builder()
                        .content(content)
                        .summary(summary)
                        .size(content.size())
                        .totalRecords(rows.size())
                        .page(page)
                        .totalPages(totalPages)
                        .build())
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public MessageResponseDTO<MeetingInvoiceDto> view(MeetingInvoiceReportViewRequest request) {
        MeetingInvoice invoice = findInvoice(request != null ? request.getId() : null,
                request != null ? request.getInvoiceId() : null,
                request != null ? request.getBookingId() : null,
                request != null ? request.getInvoiceNo() : null);
        return MessageResponseDTO.<MeetingInvoiceDto>builder()
                .success(true)
                .message("Meeting invoice report detail loaded successfully")
                .data(toDto(invoice))
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public MessageResponseDTO<MeetingInvoiceReportExportDto> export(MeetingInvoiceReportExportRequest request) {
        String exportType = StringUtils.hasText(request != null ? request.getExportType() : null)
                ? request.getExportType().trim().toUpperCase(Locale.ENGLISH)
                : "EXCEL";
        if (!List.of("EXCEL", "PDF", "CSV", "PRINT").contains(exportType)) {
            throw new BadRequestException("Invalid export type");
        }
        List<MeetingInvoice> invoices = filteredInvoices(request != null ? request.getSearch() : null);
        return MessageResponseDTO.<MeetingInvoiceReportExportDto>builder()
                .success(true)
                .message("Meeting invoice report export data loaded successfully")
                .data(MeetingInvoiceReportExportDto.builder()
                        .exportType(exportType)
                        .generatedDate(LocalDateTime.now())
                        .summary(toSummary(invoices))
                        .records(invoices.stream().map(this::toListItem).toList())
                        .build())
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportExcelFile(MeetingInvoiceReportExportRequest request) {
        List<MeetingInvoiceReportListItemDto> rows = filteredInvoices(request != null ? request.getSearch() : null)
                .stream().map(this::toListItem).toList();
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Invoice Report");
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            int rowIndex = 0;
            Row title = sheet.createRow(rowIndex++);
            title.createCell(0).setCellValue("Meeting Invoice Report");
            rowIndex++;
            Row header = sheet.createRow(rowIndex++);
            String[] headers = headers();
            for (int i = 0; i < headers.length; i++) {
                header.createCell(i).setCellValue(headers[i]);
                header.getCell(i).setCellStyle(headerStyle);
            }
            for (MeetingInvoiceReportListItemDto row : rows) {
                writeExcelRow(sheet.createRow(rowIndex++), row);
            }
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (Exception ex) {
            throw new BadRequestException("Unable to generate Excel report");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportPdfFile(MeetingInvoiceReportExportRequest request) {
        List<MeetingInvoiceReportListItemDto> rows = filteredInvoices(request != null ? request.getSearch() : null)
                .stream().map(this::toListItem).toList();
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, outputStream);
            document.open();
            document.add(new Paragraph("Meeting Invoice Report"));
            document.add(new Paragraph("Generated Date: " + LocalDateTime.now()));
            document.add(new Paragraph(" "));
            PdfPTable table = new PdfPTable(headers().length);
            table.setWidthPercentage(100);
            for (String header : headers()) {
                table.addCell(new PdfPCell(new Phrase(header)));
            }
            for (MeetingInvoiceReportListItemDto row : rows) {
                for (String value : values(row)) {
                    table.addCell(new Phrase(value));
                }
            }
            document.add(table);
            document.close();
            return outputStream.toByteArray();
        } catch (Exception ex) {
            throw new BadRequestException("Unable to generate PDF report");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportCsvFile(MeetingInvoiceReportExportRequest request) {
        List<MeetingInvoiceReportListItemDto> rows = filteredInvoices(request != null ? request.getSearch() : null)
                .stream().map(this::toListItem).toList();
        StringBuilder builder = new StringBuilder();
        builder.append("Meeting Invoice Report").append(System.lineSeparator());
        builder.append(String.join(",", headers())).append(System.lineSeparator());
        for (MeetingInvoiceReportListItemDto row : rows) {
            builder.append(String.join(",", List.of(values(row)).stream().map(this::csv).toList()))
                    .append(System.lineSeparator());
        }
        return builder.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    private List<MeetingInvoice> filteredInvoices(MeetingInvoiceReportFilterSearch search) {
        Long companyId = resolveCompanyId();
        return meetingInvoiceRepository.findAll().stream()
                .filter(invoice -> companyId.equals(invoice.getCompanyId()))
                .filter(invoice -> matches(invoice, search))
                .toList();
    }

    private boolean matches(MeetingInvoice invoice, MeetingInvoiceReportFilterSearch search) {
        if (search == null) {
            return true;
        }
        return between(invoice.getMeetingDate(), search.getDateFrom(), search.getDateTo())
                && contains(invoice.getInvoiceNo(), search.getInvoiceNo())
                && contains(invoice.getRequestNo(), search.getRequestNo())
                && contains(invoice.getMeetingName(), search.getMeetingName())
                && contains(invoice.getMeetingRoomName(), search.getMeetingRoomName())
                && contains(invoice.getMeetingType() != null ? invoice.getMeetingType().name() : null, search.getMeetingType())
                && contains(invoice.getCustomerCompanyName(), search.getCustomerCompanyName())
                && contains(invoice.getStatus() != null ? invoice.getStatus().name() : null, search.getStatus())
                && contains(invoice.getGeneratedBy(), search.getGeneratedBy());
    }

    private MeetingInvoice findInvoice(Long id, Long invoiceId, Long bookingId, String invoiceNo) {
        Long companyId = resolveCompanyId();
        Long resolvedId = id != null ? id : invoiceId;
        MeetingInvoice invoice;
        if (resolvedId != null) {
            invoice = meetingInvoiceRepository.findById(resolvedId)
                    .orElseThrow(() -> new ResourceNotFoundException("Meeting invoice not found"));
        } else if (bookingId != null) {
            invoice = meetingInvoiceRepository.findByCompanyIdAndBookingId(companyId, bookingId)
                    .orElseThrow(() -> new ResourceNotFoundException("Meeting invoice not found"));
        } else if (StringUtils.hasText(invoiceNo)) {
            invoice = meetingInvoiceRepository.findByCompanyIdAndInvoiceNoIgnoreCase(companyId, invoiceNo.trim())
                    .orElseThrow(() -> new ResourceNotFoundException("Meeting invoice not found"));
        } else {
            throw new BadRequestException("Invalid invoice report view request");
        }
        if (!companyId.equals(invoice.getCompanyId())) {
            throw new ResourceNotFoundException("Meeting invoice not found");
        }
        return invoice;
    }

    private MeetingInvoiceReportSummaryDto toSummary(List<MeetingInvoice> invoices) {
        BigDecimal total = invoices.stream()
                .filter(invoice -> invoice.getStatus() != MeetingInvoiceStatus.CANCELLED)
                .map(MeetingInvoice::getTotalAmount)
                .map(this::defaultZero)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return MeetingInvoiceReportSummaryDto.builder()
                .totalInvoices(invoices.size())
                .generatedInvoices((int) invoices.stream().filter(invoice -> invoice.getStatus() == MeetingInvoiceStatus.GENERATED).count())
                .cancelledInvoices((int) invoices.stream().filter(invoice -> invoice.getStatus() == MeetingInvoiceStatus.CANCELLED).count())
                .internalMeetingInvoices((int) invoices.stream().filter(invoice -> invoice.getMeetingType() == MeetingBookingType.INTERNAL_MEETING).count())
                .externalMeetingInvoices((int) invoices.stream().filter(invoice -> invoice.getMeetingType() == MeetingBookingType.EXTERNAL_MEETING).count())
                .totalAmount(total)
                .build();
    }

    private MeetingInvoiceReportListItemDto toListItem(MeetingInvoice invoice) {
        return MeetingInvoiceReportListItemDto.builder()
                .id(invoice.getId())
                .bookingId(invoice.getBookingId())
                .invoiceNo(invoice.getInvoiceNo())
                .requestNo(invoice.getRequestNo())
                .meetingName(invoice.getMeetingName())
                .meetingType(invoice.getMeetingType())
                .meetingTypeDescription(invoice.getMeetingType() != null ? toTitleCase(invoice.getMeetingType().name()) : null)
                .meetingRoomName(invoice.getMeetingRoomName())
                .meetingDate(invoice.getMeetingDate())
                .startTime(invoice.getStartTime())
                .endTime(invoice.getEndTime())
                .customerCompanyName(invoice.getCustomerCompanyName())
                .contactPerson(invoice.getContactPerson())
                .contactNumber(invoice.getContactNumber())
                .status(invoice.getStatus())
                .statusDescription(invoice.getStatus() != null ? toTitleCase(invoice.getStatus().name()) : null)
                .lineCount(invoice.getLines() != null ? invoice.getLines().size() : 0)
                .totalAmount(invoice.getTotalAmount())
                .generatedBy(invoice.getGeneratedBy())
                .generatedDate(invoice.getGeneratedDate())
                .lastModifiedDate(invoice.getLastModifiedDate())
                .build();
    }

    private MeetingInvoiceDto toDto(MeetingInvoice invoice) {
        return MeetingInvoiceDto.builder()
                .id(invoice.getId())
                .companyId(invoice.getCompanyId())
                .companyCode(invoice.getCompanyCode())
                .companyName(invoice.getCompanyName())
                .invoiceNo(invoice.getInvoiceNo())
                .bookingId(invoice.getBookingId())
                .requestNo(invoice.getRequestNo())
                .meetingName(invoice.getMeetingName())
                .meetingType(invoice.getMeetingType())
                .meetingTypeDescription(invoice.getMeetingType() != null ? toTitleCase(invoice.getMeetingType().name()) : null)
                .meetingRoomId(invoice.getMeetingRoomId())
                .meetingRoomName(invoice.getMeetingRoomName())
                .meetingDate(invoice.getMeetingDate())
                .startTime(invoice.getStartTime())
                .endTime(invoice.getEndTime())
                .customerId(invoice.getCustomerId())
                .customerCode(invoice.getCustomerCode())
                .customerCompanyName(invoice.getCustomerCompanyName())
                .contactPerson(invoice.getContactPerson())
                .contactNumber(invoice.getContactNumber())
                .status(invoice.getStatus())
                .statusDescription(invoice.getStatus() != null ? toTitleCase(invoice.getStatus().name()) : null)
                .subTotal(invoice.getSubTotal())
                .totalAmount(invoice.getTotalAmount())
                .lines(invoice.getLines().stream().map(this::toLineDto).toList())
                .generatedBy(invoice.getGeneratedBy())
                .generatedDate(invoice.getGeneratedDate())
                .cancelledBy(invoice.getCancelledBy())
                .cancelledDate(invoice.getCancelledDate())
                .cancellationReason(invoice.getCancellationReason())
                .createdDate(invoice.getCreatedDate())
                .lastModifiedDate(invoice.getLastModifiedDate())
                .createdBy(invoice.getCreatedBy())
                .lastModifiedBy(invoice.getLastModifiedBy())
                .build();
    }

    private MeetingInvoiceLineDto toLineDto(MeetingInvoiceLine line) {
        return MeetingInvoiceLineDto.builder()
                .id(line.getId())
                .lineKey(line.getLineKey())
                .lineType(line.getLineType())
                .lineTypeDescription(line.getLineType() != null ? toTitleCase(line.getLineType().name()) : null)
                .sourceId(line.getSourceId())
                .description(line.getDescription())
                .quantity(line.getQuantity())
                .unitPrice(line.getUnitPrice())
                .amount(line.getAmount())
                .selected(true)
                .build();
    }

    private Comparator<MeetingInvoice> resolveComparator(MeetingInvoiceReportFilterRequest request) {
        String column = request != null && StringUtils.hasText(request.getSortColumn()) ? request.getSortColumn().trim() : "generatedDate";
        boolean desc = request == null || !"ASC".equalsIgnoreCase(request.getSortDirection());
        Comparator<MeetingInvoice> comparator = switch (column) {
            case "invoiceNo" -> Comparator.comparing(MeetingInvoice::getInvoiceNo, Comparator.nullsLast(String::compareToIgnoreCase));
            case "requestNo" -> Comparator.comparing(MeetingInvoice::getRequestNo, Comparator.nullsLast(String::compareToIgnoreCase));
            case "meetingName" -> Comparator.comparing(MeetingInvoice::getMeetingName, Comparator.nullsLast(String::compareToIgnoreCase));
            case "meetingDate" -> Comparator.comparing(MeetingInvoice::getMeetingDate, Comparator.nullsLast(LocalDate::compareTo));
            case "customerCompanyName" -> Comparator.comparing(MeetingInvoice::getCustomerCompanyName, Comparator.nullsLast(String::compareToIgnoreCase));
            case "status" -> Comparator.comparing(invoice -> invoice.getStatus() != null ? invoice.getStatus().name() : null, Comparator.nullsLast(String::compareToIgnoreCase));
            case "totalAmount" -> Comparator.comparing(MeetingInvoice::getTotalAmount, Comparator.nullsLast(BigDecimal::compareTo));
            default -> Comparator.comparing(MeetingInvoice::getGeneratedDate, Comparator.nullsLast(LocalDateTime::compareTo));
        };
        return desc ? comparator.reversed() : comparator;
    }

    private void writeExcelRow(Row row, MeetingInvoiceReportListItemDto item) {
        String[] values = values(item);
        for (int i = 0; i < values.length; i++) {
            row.createCell(i).setCellValue(values[i]);
        }
    }

    private String[] headers() {
        return new String[]{"Invoice No", "Request No", "Meeting Name", "Room", "Meeting Date", "Type", "Customer/Company", "Status", "Line Count", "Total Amount", "Generated By", "Generated Date"};
    }

    private String[] values(MeetingInvoiceReportListItemDto item) {
        return new String[]{
                nullSafe(item.getInvoiceNo()),
                nullSafe(item.getRequestNo()),
                nullSafe(item.getMeetingName()),
                nullSafe(item.getMeetingRoomName()),
                nullSafe(item.getMeetingDate()),
                item.getMeetingType() != null ? toTitleCase(item.getMeetingType().name()) : "",
                nullSafe(item.getCustomerCompanyName()),
                item.getStatus() != null ? toTitleCase(item.getStatus().name()) : "",
                item.getLineCount() != null ? String.valueOf(item.getLineCount()) : "0",
                formatAmount(item.getTotalAmount()),
                nullSafe(item.getGeneratedBy()),
                nullSafe(item.getGeneratedDate())
        };
    }

    private MeetingInvoiceReportPrivilegesDto toPrivileges(PageTaskPrivileges privileges) {
        boolean export = privileges.isExport() || privileges.isDownload();
        return MeetingInvoiceReportPrivilegesDto.builder()
                .view(privileges.isView())
                .search(privileges.isSearch())
                .exportExcel(export)
                .exportPdf(export)
                .exportCsv(export)
                .print(privileges.isPrint() || export)
                .build();
    }

    private List<ReferenceOptionDto> exportTypes() {
        return List.of(
                ReferenceOptionDto.builder().code("EXCEL").description("Excel").build(),
                ReferenceOptionDto.builder().code("PDF").description("Pdf").build(),
                ReferenceOptionDto.builder().code("CSV").description("Csv").build(),
                ReferenceOptionDto.builder().code("PRINT").description("Print").build()
        );
    }

    private List<ReferenceOptionDto> toOptions(Enum<?>[] values) {
        return List.of(values).stream()
                .map(value -> ReferenceOptionDto.builder().code(value.name()).description(toTitleCase(value.name())).build())
                .toList();
    }

    private Long resolveCompanyId() {
        return CompanyContext.getCompanyId() != null ? CompanyContext.getCompanyId() : (defaultCompanyId != null ? defaultCompanyId : 1L);
    }

    private CompanyLookup resolveCompany(Long companyId) {
        return companyId != null ? companyLookupRepository.findById(companyId).orElse(null) : null;
    }

    private String resolveCompanyCode(CompanyLookup company) {
        if (StringUtils.hasText(CompanyContext.getCompanyCode())) {
            return CompanyContext.getCompanyCode().trim();
        }
        return company != null ? trimToNull(company.getCode()) : null;
    }

    private String resolveCompanyName(CompanyLookup company) {
        if (StringUtils.hasText(CompanyContext.getCompanyName())) {
            return CompanyContext.getCompanyName().trim();
        }
        return company != null ? trimToNull(company.getDescription()) : null;
    }

    private boolean contains(String source, String expected) {
        if (!StringUtils.hasText(expected)) {
            return true;
        }
        return source != null && source.toLowerCase(Locale.ENGLISH).contains(expected.trim().toLowerCase(Locale.ENGLISH));
    }

    private boolean between(LocalDate value, LocalDate from, LocalDate to) {
        if (value == null) {
            return from == null && to == null;
        }
        if (from != null && value.isBefore(from)) {
            return false;
        }
        return to == null || !value.isAfter(to);
    }

    private BigDecimal defaultZero(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private String formatAmount(BigDecimal amount) {
        return defaultZero(amount).setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private String csv(String value) {
        String safe = value != null ? value.replace("\"", "\"\"") : "";
        return "\"" + safe + "\"";
    }

    private String nullSafe(Object value) {
        return value != null ? String.valueOf(value) : "";
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String toTitleCase(String value) {
        String[] parts = value.toLowerCase(Locale.ENGLISH).split("_");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (part.isBlank()) {
                continue;
            }
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return builder.toString();
    }
}
