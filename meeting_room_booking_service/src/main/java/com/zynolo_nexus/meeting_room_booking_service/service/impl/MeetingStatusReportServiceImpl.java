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
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingStatusReportExportRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingStatusReportFilterRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingStatusReportFilterSearch;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingStatusReportReferenceDataRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingStatusReportViewRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingBookingBeverageDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingBookingRefreshmentDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingBookingSupportServiceDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingRoomDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingStatusApprovalAnalysisDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingStatusApproverPerformanceDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingStatusCancellationAnalysisDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingStatusHistoryDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingStatusMonthlyTrendDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingStatusReportDetailDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingStatusReportExportDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingStatusReportFilterResultDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingStatusReportListItemDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingStatusReportPrivilegesDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingStatusReportReferenceDataDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingStatusReportSummaryDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingStatusSummaryItemDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.ReferenceOptionDto;
import com.zynolo_nexus.meeting_room_booking_service.enums.MeetingBookingStatus;
import com.zynolo_nexus.meeting_room_booking_service.enums.MeetingBookingType;
import com.zynolo_nexus.meeting_room_booking_service.enums.RoomAvailabilityStatus;
import com.zynolo_nexus.meeting_room_booking_service.exception.BadRequestException;
import com.zynolo_nexus.meeting_room_booking_service.exception.ResourceNotFoundException;
import com.zynolo_nexus.meeting_room_booking_service.model.CompanyLookup;
import com.zynolo_nexus.meeting_room_booking_service.model.MeetingBooking;
import com.zynolo_nexus.meeting_room_booking_service.model.MeetingBookingBeverage;
import com.zynolo_nexus.meeting_room_booking_service.model.MeetingBookingRefreshment;
import com.zynolo_nexus.meeting_room_booking_service.model.MeetingBookingSupportService;
import com.zynolo_nexus.meeting_room_booking_service.model.MeetingRoom;
import com.zynolo_nexus.meeting_room_booking_service.model.UserLookup;
import com.zynolo_nexus.meeting_room_booking_service.repository.CompanyLookupRepository;
import com.zynolo_nexus.meeting_room_booking_service.repository.MeetingBookingRepository;
import com.zynolo_nexus.meeting_room_booking_service.repository.MeetingRoomRepository;
import com.zynolo_nexus.meeting_room_booking_service.repository.UserLookupRepository;
import com.zynolo_nexus.meeting_room_booking_service.service.MeetingStatusReportService;
import com.zynolo_nexus.meeting_room_booking_service.service.support.PagePrivilegeResolver;
import com.zynolo_nexus.meeting_room_booking_service.service.support.PageTaskPrivileges;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
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
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MeetingStatusReportServiceImpl implements MeetingStatusReportService {

    private static final String PAGE_CODE = "MBM_RPRT_MESR";

    private final MeetingBookingRepository meetingBookingRepository;
    private final MeetingRoomRepository meetingRoomRepository;
    private final CompanyLookupRepository companyLookupRepository;
    private final UserLookupRepository userLookupRepository;
    private final PagePrivilegeResolver pagePrivilegeResolver;

    @Value("${app.default.company-id:1}")
    private Long defaultCompanyId;

    @Override
    @Transactional(readOnly = true)
    public MessageResponseDTO<MeetingStatusReportReferenceDataDto> referenceData(MeetingStatusReportReferenceDataRequest request) {
        Long companyId = resolveCompanyId();
        CompanyLookup company = resolveCompany(companyId);
        PageTaskPrivileges privileges = pagePrivilegeResolver.resolve(request != null ? request.getUsername() : null, PAGE_CODE);

        return MessageResponseDTO.<MeetingStatusReportReferenceDataDto>builder()
                .success(true)
                .message("Meeting status report reference data loaded successfully")
                .data(MeetingStatusReportReferenceDataDto.builder()
                        .companyId(companyId)
                        .companyCode(resolveCompanyCode(company))
                        .companyName(resolveCompanyName(company))
                        .statuses(toOptions(MeetingBookingStatus.values()))
                        .meetingTypes(toOptions(MeetingBookingType.values()))
                        .exportTypes(exportTypes())
                        .meetingRooms(companyRooms(companyId).stream().map(this::toRoomDto).toList())
                        .privileges(toPrivileges(privileges))
                        .build())
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public MessageResponseDTO<MeetingStatusReportFilterResultDto> filterList(MeetingStatusReportFilterRequest request) {
        String username = requireUsername(request != null ? request.getUsername() : null);
        int page = request != null && request.getPage() != null && request.getPage() >= 0 ? request.getPage() : 0;
        int size = request != null && request.getSize() != null && request.getSize() > 0 ? request.getSize() : 10;

        List<MeetingBooking> bookings = filteredBookings(username, request != null ? request.getSearch() : null)
                .stream()
                .sorted(resolveComparator(request != null ? request.getSortColumn() : null, request != null ? request.getSortDirection() : null))
                .toList();
        List<MeetingStatusReportListItemDto> rows = bookings.stream().map(this::toListItem).toList();
        int from = Math.min(page * size, rows.size());
        int to = Math.min(from + size, rows.size());
        List<MeetingStatusReportListItemDto> content = rows.subList(from, to);
        int totalPages = rows.isEmpty() ? 0 : (int) Math.ceil((double) rows.size() / size);

        return MessageResponseDTO.<MeetingStatusReportFilterResultDto>builder()
                .success(true)
                .message("Meeting status report filtered successfully")
                .data(MeetingStatusReportFilterResultDto.builder()
                        .summary(toSummary(bookings))
                        .statusSummary(toStatusSummary(bookings))
                        .approvalAnalysis(toApprovalAnalysis(bookings))
                        .approverPerformance(toApproverPerformance(bookings))
                        .cancellationAnalysis(toCancellationAnalysis(bookings))
                        .monthlyStatusTrend(toMonthlyTrend(bookings))
                        .content(content)
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
    public MessageResponseDTO<MeetingStatusReportDetailDto> view(MeetingStatusReportViewRequest request) {
        if (request == null || request.getId() == null) {
            throw new BadRequestException("Invalid meeting status report view request");
        }
        String username = requireUsername(request.getUsername());
        MeetingBooking booking = findVisibleBooking(request.getId(), username);
        return MessageResponseDTO.<MeetingStatusReportDetailDto>builder()
                .success(true)
                .message("Meeting status report detail loaded successfully")
                .data(toDetail(booking))
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public MessageResponseDTO<MeetingStatusReportExportDto> export(MeetingStatusReportExportRequest request) {
        String username = requireUsername(request != null ? request.getUsername() : null);
        String exportType = StringUtils.hasText(request != null ? request.getExportType() : null)
                ? request.getExportType().trim().toUpperCase(Locale.ENGLISH)
                : "CSV";
        if (!List.of("EXCEL", "PDF", "CSV", "PRINT").contains(exportType)) {
            throw new BadRequestException("Invalid export type");
        }
        List<MeetingBooking> bookings = exportBookings(request, username);
        List<MeetingStatusReportListItemDto> rows = bookings.stream().map(this::toListItem).toList();

        return MessageResponseDTO.<MeetingStatusReportExportDto>builder()
                .success(true)
                .message("Meeting status report export data loaded successfully")
                .data(MeetingStatusReportExportDto.builder()
                        .exportType(exportType)
                        .generatedDate(LocalDateTime.now())
                        .summary(toSummary(bookings))
                        .rows(rows)
                        .totalRecords(rows.size())
                        .build())
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportExcelFile(MeetingStatusReportExportRequest request) {
        String username = requireUsername(request != null ? request.getUsername() : null);
        List<MeetingBooking> bookings = exportBookings(request, username);
        List<MeetingStatusReportListItemDto> rows = bookings.stream().map(this::toListItem).toList();
        MeetingStatusReportSummaryDto summary = toSummary(bookings);
        MeetingStatusApprovalAnalysisDto approvalAnalysis = toApprovalAnalysis(bookings);

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Meeting Status");
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            int rowIndex = 0;
            sheet.createRow(rowIndex++).createCell(0).setCellValue("Meeting Status Report");
            rowIndex++;
            rowIndex = writeSummaryRows(sheet, rowIndex, summary, approvalAnalysis);
            rowIndex++;

            Row headerRow = sheet.createRow(rowIndex++);
            String[] headers = reportHeaders();
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            for (MeetingStatusReportListItemDto row : rows) {
                Row dataRow = sheet.createRow(rowIndex++);
                writeRowCells(dataRow, row);
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
    public byte[] exportPdfFile(MeetingStatusReportExportRequest request) {
        String username = requireUsername(request != null ? request.getUsername() : null);
        List<MeetingBooking> bookings = exportBookings(request, username);
        List<MeetingStatusReportListItemDto> rows = bookings.stream().map(this::toListItem).toList();
        MeetingStatusReportSummaryDto summary = toSummary(bookings);
        MeetingStatusApprovalAnalysisDto approvalAnalysis = toApprovalAnalysis(bookings);

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, outputStream);
            document.open();
            document.add(new Paragraph("Meeting Status Report"));
            document.add(new Paragraph("Total Requests: " + summary.getTotalRequests()
                    + " | Pending: " + summary.getPendingApprovals()
                    + " | Approved: " + summary.getApproved()
                    + " | Rejected: " + summary.getRejected()
                    + " | Completed: " + summary.getCompleted()
                    + " | Cancelled: " + summary.getCancelled()));
            document.add(new Paragraph("Approval Rate: " + approvalAnalysis.getApprovalRate()
                    + "% | Rejection Rate: " + approvalAnalysis.getRejectionRate()
                    + "% | Avg Approval Time: " + approvalAnalysis.getAverageApprovalTime()));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(reportHeaders().length);
            table.setWidthPercentage(100);
            for (String header : reportHeaders()) {
                table.addCell(new PdfPCell(new Phrase(header)));
            }
            for (MeetingStatusReportListItemDto row : rows) {
                for (String value : rowValues(row)) {
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
    public byte[] exportCsvFile(MeetingStatusReportExportRequest request) {
        String username = requireUsername(request != null ? request.getUsername() : null);
        List<MeetingBooking> bookings = exportBookings(request, username);
        List<MeetingStatusReportListItemDto> rows = bookings.stream().map(this::toListItem).toList();
        MeetingStatusReportSummaryDto summary = toSummary(bookings);
        MeetingStatusApprovalAnalysisDto approvalAnalysis = toApprovalAnalysis(bookings);

        StringBuilder builder = new StringBuilder();
        builder.append("Meeting Status Report").append(System.lineSeparator());
        builder.append("Total Requests,").append(summary.getTotalRequests()).append(System.lineSeparator());
        builder.append("Pending Approvals,").append(summary.getPendingApprovals()).append(System.lineSeparator());
        builder.append("Approved,").append(summary.getApproved()).append(System.lineSeparator());
        builder.append("Rejected,").append(summary.getRejected()).append(System.lineSeparator());
        builder.append("Ongoing,").append(summary.getOngoing()).append(System.lineSeparator());
        builder.append("Completed,").append(summary.getCompleted()).append(System.lineSeparator());
        builder.append("Cancelled,").append(summary.getCancelled()).append(System.lineSeparator());
        builder.append("Approval Rate,").append(approvalAnalysis.getApprovalRate()).append("%").append(System.lineSeparator());
        builder.append("Rejection Rate,").append(approvalAnalysis.getRejectionRate()).append("%").append(System.lineSeparator());
        builder.append("Average Approval Time,").append(csv(approvalAnalysis.getAverageApprovalTime())).append(System.lineSeparator());
        builder.append(System.lineSeparator());
        builder.append(String.join(",", reportHeaders())).append(System.lineSeparator());
        for (MeetingStatusReportListItemDto row : rows) {
            builder.append(String.join(",", List.of(rowValues(row)).stream().map(this::csv).toList()))
                    .append(System.lineSeparator());
        }
        return builder.toString().getBytes(StandardCharsets.UTF_8);
    }

    private List<MeetingBooking> exportBookings(MeetingStatusReportExportRequest request, String username) {
        return filteredBookings(username, request != null ? request.getSearch() : null)
                .stream()
                .sorted(resolveComparator(request != null ? request.getSortColumn() : null, request != null ? request.getSortDirection() : null))
                .toList();
    }

    private List<MeetingBooking> filteredBookings(String username, MeetingStatusReportFilterSearch search) {
        Long companyId = resolveCompanyId();
        PageTaskPrivileges privileges = pagePrivilegeResolver.resolve(username, PAGE_CODE);
        boolean canSeeAll = canSeeAll(username, privileges);

        return meetingBookingRepository.findAll().stream()
                .filter(booking -> companyId.equals(booking.getCompanyId()))
                .filter(booking -> canSeeAll || username.equalsIgnoreCase(nullToEmpty(booking.getCreatedBy())))
                .filter(booking -> matches(booking, search))
                .toList();
    }

    private MeetingBooking findVisibleBooking(Long id, String username) {
        Long companyId = resolveCompanyId();
        PageTaskPrivileges privileges = pagePrivilegeResolver.resolve(username, PAGE_CODE);
        boolean canSeeAll = canSeeAll(username, privileges);
        MeetingBooking booking = meetingBookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Meeting status report record not found"));
        if (!companyId.equals(booking.getCompanyId()) || (!canSeeAll && !username.equalsIgnoreCase(nullToEmpty(booking.getCreatedBy())))) {
            throw new ResourceNotFoundException("Meeting status report record not found");
        }
        return booking;
    }

    private boolean matches(MeetingBooking booking, MeetingStatusReportFilterSearch search) {
        if (search == null) {
            return true;
        }
        return (search.getDateFrom() == null || !booking.getMeetingDate().isBefore(search.getDateFrom()))
                && (search.getDateTo() == null || !booking.getMeetingDate().isAfter(search.getDateTo()))
                && contains(booking.getStatus() != null ? booking.getStatus().name() : null, search.getStatus())
                && (search.getMeetingRoomId() == null || search.getMeetingRoomId().equals(booking.getMeetingRoomId()))
                && contains(booking.getMeetingRoomName(), search.getMeetingRoomName())
                && contains(booking.getMeetingType() != null ? booking.getMeetingType().name() : null, search.getMeetingType())
                && contains(booking.getCreatedBy(), search.getRequestedBy())
                && contains(resolveApprover(booking), search.getApprover())
                && contains(booking.getRequestNo(), search.getRequestNo());
    }

    private Comparator<MeetingBooking> resolveComparator(String sortColumn, String sortDirection) {
        String column = StringUtils.hasText(sortColumn) ? sortColumn.trim() : "meetingDate";
        boolean desc = "DESC".equalsIgnoreCase(sortDirection);
        Comparator<MeetingBooking> comparator = switch (column) {
            case "requestNo" -> Comparator.comparing(MeetingBooking::getRequestNo, Comparator.nullsLast(String::compareToIgnoreCase));
            case "meetingName" -> Comparator.comparing(MeetingBooking::getMeetingName, Comparator.nullsLast(String::compareToIgnoreCase));
            case "requestedBy" -> Comparator.comparing(MeetingBooking::getCreatedBy, Comparator.nullsLast(String::compareToIgnoreCase));
            case "meetingRoom", "meetingRoomName" -> Comparator.comparing(MeetingBooking::getMeetingRoomName, Comparator.nullsLast(String::compareToIgnoreCase));
            case "meetingType" -> Comparator.comparing(booking -> booking.getMeetingType() != null ? booking.getMeetingType().name() : null,
                    Comparator.nullsLast(String::compareToIgnoreCase));
            case "status" -> Comparator.comparing(booking -> booking.getStatus() != null ? booking.getStatus().name() : null,
                    Comparator.nullsLast(String::compareToIgnoreCase));
            case "approver" -> Comparator.comparing(this::resolveApprover, Comparator.nullsLast(String::compareToIgnoreCase));
            case "lastModifiedDate" -> Comparator.comparing(MeetingBooking::getLastModifiedDate, Comparator.nullsLast(LocalDateTime::compareTo));
            default -> Comparator.comparing(MeetingBooking::getMeetingDate, Comparator.nullsLast(LocalDate::compareTo));
        };
        return desc ? comparator.reversed() : comparator;
    }

    private MeetingStatusReportSummaryDto toSummary(List<MeetingBooking> bookings) {
        return MeetingStatusReportSummaryDto.builder()
                .totalRequests(bookings.size())
                .pendingApprovals(countStatus(bookings, MeetingBookingStatus.PENDING_APPROVAL))
                .approved(countStatus(bookings, MeetingBookingStatus.APPROVED))
                .rejected(countStatus(bookings, MeetingBookingStatus.REJECTED))
                .ongoing(countStatus(bookings, MeetingBookingStatus.ONGOING))
                .completed(countStatus(bookings, MeetingBookingStatus.COMPLETED))
                .cancelled(countStatus(bookings, MeetingBookingStatus.CANCELLED))
                .build();
    }

    private List<MeetingStatusSummaryItemDto> toStatusSummary(List<MeetingBooking> bookings) {
        long total = bookings.size();
        return List.of(MeetingBookingStatus.values()).stream()
                .map(status -> {
                    long count = countStatus(bookings, status);
                    BigDecimal percentage = total > 0
                            ? BigDecimal.valueOf(count).multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP)
                            : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
                    return MeetingStatusSummaryItemDto.builder()
                            .status(status)
                            .statusDescription(toTitleCase(status.name()))
                            .count(count)
                            .percentage(percentage)
                            .build();
                })
                .toList();
    }

    private MeetingStatusApprovalAnalysisDto toApprovalAnalysis(List<MeetingBooking> bookings) {
        long totalSubmitted = bookings.stream().filter(booking -> booking.getSubmittedDate() != null).count();
        long totalApproved = bookings.stream().filter(booking -> booking.getApprovedDate() != null).count();
        long totalRejected = bookings.stream().filter(booking -> booking.getRejectedDate() != null || booking.getStatus() == MeetingBookingStatus.REJECTED).count();
        long decisions = totalApproved + totalRejected;
        BigDecimal approvalRate = decisions > 0
                ? BigDecimal.valueOf(totalApproved).multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(decisions), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        BigDecimal rejectionRate = decisions > 0
                ? BigDecimal.valueOf(totalRejected).multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(decisions), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        return MeetingStatusApprovalAnalysisDto.builder()
                .totalSubmitted(totalSubmitted)
                .totalApproved(totalApproved)
                .totalRejected(totalRejected)
                .approvalRate(approvalRate)
                .rejectionRate(rejectionRate)
                .averageApprovalTime(formatAverageDuration(approvalDurations(bookings)))
                .build();
    }

    private List<MeetingStatusApproverPerformanceDto> toApproverPerformance(List<MeetingBooking> bookings) {
        Map<String, List<MeetingBooking>> byApprover = bookings.stream()
                .filter(booking -> StringUtils.hasText(resolveApprover(booking)))
                .collect(Collectors.groupingBy(this::resolveApprover));
        return byApprover.entrySet().stream()
                .map(entry -> {
                    List<MeetingBooking> approverBookings = entry.getValue();
                    return MeetingStatusApproverPerformanceDto.builder()
                            .approver(entry.getKey())
                            .requests(approverBookings.size())
                            .approved(approverBookings.stream().filter(booking -> booking.getApprovedDate() != null).count())
                            .rejected(approverBookings.stream().filter(booking -> booking.getRejectedDate() != null || booking.getStatus() == MeetingBookingStatus.REJECTED).count())
                            .averageApprovalTime(formatAverageDuration(approvalDurations(approverBookings)))
                            .build();
                })
                .sorted(Comparator.comparing(MeetingStatusApproverPerformanceDto::getRequests).reversed())
                .toList();
    }

    private List<MeetingStatusCancellationAnalysisDto> toCancellationAnalysis(List<MeetingBooking> bookings) {
        return bookings.stream()
                .filter(booking -> booking.getStatus() == MeetingBookingStatus.CANCELLED || booking.getCancelledDate() != null)
                .collect(Collectors.groupingBy(this::cancellationReason, Collectors.counting()))
                .entrySet().stream()
                .map(entry -> MeetingStatusCancellationAnalysisDto.builder()
                        .reason(entry.getKey())
                        .count(entry.getValue())
                        .build())
                .sorted(Comparator.comparing(MeetingStatusCancellationAnalysisDto::getCount).reversed())
                .toList();
    }

    private List<MeetingStatusMonthlyTrendDto> toMonthlyTrend(List<MeetingBooking> bookings) {
        Map<YearMonth, List<MeetingBooking>> byMonth = bookings.stream()
                .filter(booking -> booking.getMeetingDate() != null)
                .collect(Collectors.groupingBy(booking -> YearMonth.from(booking.getMeetingDate())));
        return byMonth.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    List<MeetingBooking> monthBookings = entry.getValue();
                    return MeetingStatusMonthlyTrendDto.builder()
                            .month(entry.getKey().toString())
                            .approved(countStatus(monthBookings, MeetingBookingStatus.APPROVED)
                                    + countStatus(monthBookings, MeetingBookingStatus.ONGOING)
                                    + countStatus(monthBookings, MeetingBookingStatus.COMPLETED))
                            .rejected(countStatus(monthBookings, MeetingBookingStatus.REJECTED))
                            .cancelled(countStatus(monthBookings, MeetingBookingStatus.CANCELLED))
                            .build();
                })
                .toList();
    }

    private List<Duration> approvalDurations(List<MeetingBooking> bookings) {
        return bookings.stream()
                .filter(booking -> booking.getSubmittedDate() != null && booking.getApprovedDate() != null)
                .map(booking -> Duration.between(booking.getSubmittedDate(), booking.getApprovedDate()))
                .filter(duration -> !duration.isNegative())
                .toList();
    }

    private String formatAverageDuration(List<Duration> durations) {
        if (durations == null || durations.isEmpty()) {
            return "0h 0m";
        }
        long averageMinutes = Math.round(durations.stream().mapToLong(Duration::toMinutes).average().orElse(0));
        return (averageMinutes / 60) + "h " + (averageMinutes % 60) + "m";
    }

    private long countStatus(List<MeetingBooking> bookings, MeetingBookingStatus status) {
        return bookings.stream().filter(booking -> booking.getStatus() == status).count();
    }

    private MeetingStatusReportListItemDto toListItem(MeetingBooking booking) {
        return MeetingStatusReportListItemDto.builder()
                .id(booking.getId())
                .requestNo(booking.getRequestNo())
                .meetingName(booking.getMeetingName())
                .requestedBy(booking.getCreatedBy())
                .meetingRoomId(booking.getMeetingRoomId())
                .meetingRoomName(booking.getMeetingRoomName())
                .meetingDate(booking.getMeetingDate())
                .meetingType(booking.getMeetingType())
                .meetingTypeDescription(booking.getMeetingType() != null ? toTitleCase(booking.getMeetingType().name()) : null)
                .status(booking.getStatus())
                .statusDescription(booking.getStatus() != null ? toTitleCase(booking.getStatus().name()) : null)
                .approver(resolveApprover(booking))
                .build();
    }

    private MeetingStatusReportDetailDto toDetail(MeetingBooking booking) {
        return MeetingStatusReportDetailDto.builder()
                .id(booking.getId())
                .requestNo(booking.getRequestNo())
                .meetingName(booking.getMeetingName())
                .meetingType(booking.getMeetingType())
                .meetingTypeDescription(booking.getMeetingType() != null ? toTitleCase(booking.getMeetingType().name()) : null)
                .meetingRoomId(booking.getMeetingRoomId())
                .meetingRoomName(booking.getMeetingRoomName())
                .meetingDate(booking.getMeetingDate())
                .startTime(booking.getStartTime())
                .endTime(booking.getEndTime())
                .attendees(booking.getNumberOfAttendees())
                .status(booking.getStatus())
                .statusDescription(booking.getStatus() != null ? toTitleCase(booking.getStatus().name()) : null)
                .requestedBy(booking.getCreatedBy())
                .submittedBy(booking.getSubmittedBy())
                .submittedDate(booking.getSubmittedDate())
                .approvedBy(booking.getApprovedBy())
                .approvedDate(booking.getApprovedDate())
                .approvalRemark(booking.getApprovalRemark())
                .rejectedBy(booking.getRejectedBy())
                .rejectedDate(booking.getRejectedDate())
                .rejectionRemark(booking.getRejectionRemark())
                .cancelledBy(booking.getCancelledBy())
                .cancelledDate(booking.getCancelledDate())
                .cancellationReason(booking.getCancellationReason())
                .statusHistory(statusHistory(booking))
                .refreshments(booking.getRefreshments().stream().map(this::toRefreshmentDto).toList())
                .beverages(booking.getBeverages().stream().map(this::toBeverageDto).toList())
                .supportServices(booking.getSupportServices().stream().map(this::toSupportDto).toList())
                .build();
    }

    private List<MeetingStatusHistoryDto> statusHistory(MeetingBooking booking) {
        List<MeetingStatusHistoryDto> history = new ArrayList<>();
        addHistory(history, MeetingBookingStatus.DRAFT, booking.getCreatedBy(), booking.getCreatedDate(), "Booking created");
        addHistory(history, MeetingBookingStatus.PENDING_APPROVAL, booking.getSubmittedBy(), booking.getSubmittedDate(), "Submitted for approval");
        addHistory(history, MeetingBookingStatus.APPROVED, booking.getApprovedBy(), booking.getApprovedDate(), booking.getApprovalRemark());
        addHistory(history, MeetingBookingStatus.REJECTED, booking.getRejectedBy(), booking.getRejectedDate(), booking.getRejectionRemark());
        addHistory(history, MeetingBookingStatus.CANCELLED, booking.getCancelledBy(), booking.getCancelledDate(), booking.getCancellationReason());
        if (booking.getOngoingUpdatedDate() != null) {
            addHistory(history, MeetingBookingStatus.ONGOING, booking.getOngoingUpdatedBy(), booking.getOngoingUpdatedDate(), booking.getOngoingUpdate());
        }
        return history.stream()
                .sorted(Comparator.comparing(MeetingStatusHistoryDto::getActionDate, Comparator.nullsLast(LocalDateTime::compareTo)))
                .toList();
    }

    private void addHistory(List<MeetingStatusHistoryDto> history,
                            MeetingBookingStatus status,
                            String actionBy,
                            LocalDateTime actionDate,
                            String remark) {
        if (actionDate == null) {
            return;
        }
        history.add(MeetingStatusHistoryDto.builder()
                .status(status)
                .statusDescription(toTitleCase(status.name()))
                .actionBy(actionBy)
                .actionDate(actionDate)
                .remark(remark)
                .build());
    }

    private MeetingBookingRefreshmentDto toRefreshmentDto(MeetingBookingRefreshment line) {
        return MeetingBookingRefreshmentDto.builder()
                .id(line.getId())
                .refreshmentId(line.getRefreshmentId())
                .refreshmentCode(line.getRefreshmentCode())
                .itemName(line.getItemName())
                .category(line.getCategory())
                .categoryDescription(line.getCategory() != null ? toTitleCase(line.getCategory().name()) : null)
                .vendorId(line.getVendorId())
                .vendorCode(line.getVendorCode())
                .vendorName(line.getVendorName())
                .quantity(line.getQuantity())
                .unitPrice(line.getUnitPrice())
                .totalAmount(line.getTotalAmount())
                .build();
    }

    private MeetingBookingBeverageDto toBeverageDto(MeetingBookingBeverage line) {
        return MeetingBookingBeverageDto.builder()
                .id(line.getId())
                .beverageId(line.getBeverageId())
                .beverageCode(line.getBeverageCode())
                .beverageName(line.getBeverageName())
                .vendorId(line.getVendorId())
                .vendorCode(line.getVendorCode())
                .vendorName(line.getVendorName())
                .quantity(line.getQuantity())
                .unitPrice(line.getUnitPrice())
                .totalAmount(line.getTotalAmount())
                .build();
    }

    private MeetingBookingSupportServiceDto toSupportDto(MeetingBookingSupportService line) {
        return MeetingBookingSupportServiceDto.builder()
                .id(line.getId())
                .serviceId(line.getServiceId())
                .serviceCode(line.getServiceCode())
                .serviceName(line.getServiceName())
                .serviceCategory(line.getServiceCategory())
                .serviceCategoryDescription(line.getServiceCategory() != null ? toTitleCase(line.getServiceCategory().name()) : null)
                .assignedTeam(line.getAssignedTeam())
                .assignedTeamDescription(line.getAssignedTeam() != null ? toTitleCase(line.getAssignedTeam().name()) : null)
                .chargeable(line.getChargeable())
                .estimatedAmount(line.getEstimatedAmount())
                .remarks(line.getRemarks())
                .build();
    }

    private int writeSummaryRows(Sheet sheet, int rowIndex, MeetingStatusReportSummaryDto summary, MeetingStatusApprovalAnalysisDto approvalAnalysis) {
        rowIndex = writeSummaryRow(sheet, rowIndex, "Total Requests", summary.getTotalRequests());
        rowIndex = writeSummaryRow(sheet, rowIndex, "Pending Approvals", summary.getPendingApprovals());
        rowIndex = writeSummaryRow(sheet, rowIndex, "Approved", summary.getApproved());
        rowIndex = writeSummaryRow(sheet, rowIndex, "Rejected", summary.getRejected());
        rowIndex = writeSummaryRow(sheet, rowIndex, "Ongoing", summary.getOngoing());
        rowIndex = writeSummaryRow(sheet, rowIndex, "Completed", summary.getCompleted());
        rowIndex = writeSummaryRow(sheet, rowIndex, "Cancelled", summary.getCancelled());
        rowIndex = writeSummaryRow(sheet, rowIndex, "Approval Rate", approvalAnalysis.getApprovalRate() + "%");
        rowIndex = writeSummaryRow(sheet, rowIndex, "Rejection Rate", approvalAnalysis.getRejectionRate() + "%");
        return writeSummaryRow(sheet, rowIndex, "Average Approval Time", approvalAnalysis.getAverageApprovalTime());
    }

    private int writeSummaryRow(Sheet sheet, int rowIndex, String label, Object value) {
        Row row = sheet.createRow(rowIndex++);
        row.createCell(0).setCellValue(label);
        row.createCell(1).setCellValue(text(value));
        return rowIndex;
    }

    private String[] reportHeaders() {
        return new String[]{
                "Request No",
                "Meeting Name",
                "Requested By",
                "Meeting Room",
                "Date",
                "Meeting Type",
                "Status",
                "Approver"
        };
    }

    private void writeRowCells(Row dataRow, MeetingStatusReportListItemDto row) {
        String[] values = rowValues(row);
        for (int i = 0; i < values.length; i++) {
            dataRow.createCell(i).setCellValue(values[i]);
        }
    }

    private String[] rowValues(MeetingStatusReportListItemDto row) {
        return new String[]{
                text(row.getRequestNo()),
                text(row.getMeetingName()),
                text(row.getRequestedBy()),
                text(row.getMeetingRoomName()),
                text(row.getMeetingDate()),
                text(row.getMeetingTypeDescription()),
                text(row.getStatusDescription()),
                text(row.getApprover())
        };
    }

    private MeetingStatusReportPrivilegesDto toPrivileges(PageTaskPrivileges privileges) {
        boolean export = privileges.isExport();
        return MeetingStatusReportPrivilegesDto.builder()
                .search(privileges.isSearch())
                .resetFilters(privileges.isSearch())
                .viewDetails(privileges.isView())
                .viewApprovalStatistics(privileges.isView() || privileges.isSearch())
                .viewApprovalAnalytics(privileges.isView() || privileges.isSearch())
                .exportExcel(export)
                .exportPdf(export)
                .exportCsv(export)
                .print(privileges.isPrint() || export)
                .build();
    }

    private boolean canSeeAll(String username, PageTaskPrivileges privileges) {
        if (privileges.isApprove() || privileges.isReject() || privileges.isExport() || privileges.isPrint()) {
            return true;
        }
        UserLookup user = userLookupRepository.findByUsername(username).orElse(null);
        String roleCode = user != null && user.getRole() != null ? user.getRole().getCode() : null;
        if (!StringUtils.hasText(roleCode)) {
            return false;
        }
        String normalizedRole = roleCode.trim().toUpperCase(Locale.ENGLISH);
        return normalizedRole.contains("ADMIN") || normalizedRole.contains("SUPER");
    }

    private String resolveApprover(MeetingBooking booking) {
        if (StringUtils.hasText(booking.getApprovedBy())) {
            return booking.getApprovedBy();
        }
        if (StringUtils.hasText(booking.getRejectedBy())) {
            return booking.getRejectedBy();
        }
        return null;
    }

    private String cancellationReason(MeetingBooking booking) {
        if (StringUtils.hasText(booking.getCancellationReasonCode())) {
            return toTitleCase(booking.getCancellationReasonCode());
        }
        if (StringUtils.hasText(booking.getCancellationReason())) {
            return booking.getCancellationReason().trim();
        }
        return "Not Specified";
    }

    private List<MeetingRoom> companyRooms(Long companyId) {
        return meetingRoomRepository.findAll().stream()
                .filter(room -> companyId.equals(room.getCompanyId()))
                .sorted(Comparator.comparing(MeetingRoom::getRoomName, Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList();
    }

    private MeetingRoomDto toRoomDto(MeetingRoom room) {
        return MeetingRoomDto.builder()
                .id(room.getId())
                .companyId(room.getCompanyId())
                .companyCode(room.getCompanyCode())
                .companyName(room.getCompanyName())
                .roomCode(room.getRoomCode())
                .roomName(room.getRoomName())
                .capacity(room.getCapacity())
                .location(room.getLocation())
                .floor(room.getFloor())
                .availabilityStatus(room.getAvailabilityStatus())
                .availabilityStatusDescription(room.getAvailabilityStatus() != null ? toTitleCase(room.getAvailabilityStatus().name()) : null)
                .description(room.getDescription())
                .active(room.getActive())
                .activeStatusDescription(Boolean.TRUE.equals(room.getActive()) ? "Active" : "Inactive")
                .bookable(Boolean.TRUE.equals(room.getActive()) && room.getAvailabilityStatus() == RoomAvailabilityStatus.AVAILABLE)
                .build();
    }

    private List<ReferenceOptionDto> exportTypes() {
        return List.of(
                option("EXCEL", "Export Excel"),
                option("PDF", "Export PDF"),
                option("CSV", "Export CSV"),
                option("PRINT", "Print")
        );
    }

    private List<ReferenceOptionDto> toOptions(Enum<?>[] values) {
        return List.of(values).stream().map(value -> option(value.name(), toTitleCase(value.name()))).toList();
    }

    private ReferenceOptionDto option(String code, String description) {
        return ReferenceOptionDto.builder().code(code).description(description).build();
    }

    private String requireUsername(String username) {
        if (!StringUtils.hasText(username)) {
            throw new BadRequestException("Username is required");
        }
        return username.trim();
    }

    private Long resolveCompanyId() {
        if (CompanyContext.getCompanyId() != null) {
            return CompanyContext.getCompanyId();
        }
        return defaultCompanyId != null ? defaultCompanyId : 1L;
    }

    private CompanyLookup resolveCompany(Long companyId) {
        if (companyId == null) {
            return null;
        }
        return companyLookupRepository.findById(companyId).orElse(null);
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

    private String csv(Object value) {
        String text = text(value);
        if (text.contains(",") || text.contains("\"") || text.contains("\n") || text.contains("\r")) {
            return "\"" + text.replace("\"", "\"\"") + "\"";
        }
        return text;
    }

    private String text(Object value) {
        return value == null ? "" : value.toString();
    }

    private boolean contains(String source, String expected) {
        if (!StringUtils.hasText(expected)) {
            return true;
        }
        return source != null && source.toLowerCase(Locale.ENGLISH).contains(expected.trim().toLowerCase(Locale.ENGLISH));
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
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
