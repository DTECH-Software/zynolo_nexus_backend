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
import com.zynolo_nexus.meeting_room_booking_service.dto.request.RoomUtilizationReportExportRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.RoomUtilizationReportFilterRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.RoomUtilizationReportFilterSearch;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.RoomUtilizationReportReferenceDataRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.RoomUtilizationReportViewRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingRoomDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.ReferenceOptionDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.RoomBookingDistributionDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.RoomUtilizationBookingHistoryDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.RoomUtilizationReportDetailDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.RoomUtilizationReportExportDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.RoomUtilizationReportFilterResultDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.RoomUtilizationReportListItemDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.RoomUtilizationReportPrivilegesDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.RoomUtilizationReportReferenceDataDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.RoomUtilizationSummaryDto;
import com.zynolo_nexus.meeting_room_booking_service.enums.MeetingBookingStatus;
import com.zynolo_nexus.meeting_room_booking_service.enums.MeetingBookingType;
import com.zynolo_nexus.meeting_room_booking_service.enums.RoomAvailabilityStatus;
import com.zynolo_nexus.meeting_room_booking_service.exception.BadRequestException;
import com.zynolo_nexus.meeting_room_booking_service.exception.ResourceNotFoundException;
import com.zynolo_nexus.meeting_room_booking_service.model.CompanyLookup;
import com.zynolo_nexus.meeting_room_booking_service.model.MeetingBooking;
import com.zynolo_nexus.meeting_room_booking_service.model.MeetingRoom;
import com.zynolo_nexus.meeting_room_booking_service.model.UserLookup;
import com.zynolo_nexus.meeting_room_booking_service.repository.CompanyLookupRepository;
import com.zynolo_nexus.meeting_room_booking_service.repository.MeetingBookingRepository;
import com.zynolo_nexus.meeting_room_booking_service.repository.MeetingRoomRepository;
import com.zynolo_nexus.meeting_room_booking_service.repository.UserLookupRepository;
import com.zynolo_nexus.meeting_room_booking_service.service.RoomUtilizationReportService;
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
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomUtilizationReportServiceImpl implements RoomUtilizationReportService {

    private static final String PAGE_CODE = "MBM_RPRT_ROUR";
    private static final BigDecimal WORKING_HOURS_PER_DAY = new BigDecimal("8.00");
    private static final Set<MeetingBookingStatus> UTILIZATION_STATUSES = Set.of(
            MeetingBookingStatus.APPROVED,
            MeetingBookingStatus.ONGOING,
            MeetingBookingStatus.COMPLETED
    );

    private final MeetingBookingRepository meetingBookingRepository;
    private final MeetingRoomRepository meetingRoomRepository;
    private final CompanyLookupRepository companyLookupRepository;
    private final UserLookupRepository userLookupRepository;
    private final PagePrivilegeResolver pagePrivilegeResolver;

    @Value("${app.default.company-id:1}")
    private Long defaultCompanyId;

    @Override
    @Transactional(readOnly = true)
    public MessageResponseDTO<RoomUtilizationReportReferenceDataDto> referenceData(RoomUtilizationReportReferenceDataRequest request) {
        Long companyId = resolveCompanyId();
        CompanyLookup company = resolveCompany(companyId);
        PageTaskPrivileges privileges = pagePrivilegeResolver.resolve(request != null ? request.getUsername() : null, PAGE_CODE);

        return MessageResponseDTO.<RoomUtilizationReportReferenceDataDto>builder()
                .success(true)
                .message("Room utilization report reference data loaded successfully")
                .data(RoomUtilizationReportReferenceDataDto.builder()
                        .companyId(companyId)
                        .companyCode(resolveCompanyCode(company))
                        .companyName(resolveCompanyName(company))
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
    public MessageResponseDTO<RoomUtilizationReportFilterResultDto> filterList(RoomUtilizationReportFilterRequest request) {
        String username = requireUsername(request != null ? request.getUsername() : null);
        int page = request != null && request.getPage() != null && request.getPage() >= 0 ? request.getPage() : 0;
        int size = request != null && request.getSize() != null && request.getSize() > 0 ? request.getSize() : 10;
        RoomUtilizationReportFilterSearch search = request != null ? request.getSearch() : null;
        ReportPeriod period = resolvePeriod(search);

        ReportDataset dataset = dataset(username, search, period);
        List<RoomUtilizationReportListItemDto> rows = dataset.rows().stream()
                .sorted(resolveComparator(request != null ? request.getSortColumn() : null, request != null ? request.getSortDirection() : null))
                .toList();
        int from = Math.min(page * size, rows.size());
        int to = Math.min(from + size, rows.size());
        List<RoomUtilizationReportListItemDto> content = rows.subList(from, to);
        int totalPages = rows.isEmpty() ? 0 : (int) Math.ceil((double) rows.size() / size);

        return MessageResponseDTO.<RoomUtilizationReportFilterResultDto>builder()
                .success(true)
                .message("Room utilization report filtered successfully")
                .data(RoomUtilizationReportFilterResultDto.builder()
                        .periodFrom(period.from())
                        .periodTo(period.to())
                        .summary(toSummary(rows, dataset.bookings()))
                        .content(content)
                        .roomUtilizationChart(rows)
                        .bookingDistributionChart(toBookingDistribution(rows))
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
    public MessageResponseDTO<RoomUtilizationReportDetailDto> view(RoomUtilizationReportViewRequest request) {
        if (request == null || request.getRoomId() == null) {
            throw new BadRequestException("Invalid room utilization report view request");
        }
        String username = requireUsername(request.getUsername());
        Long companyId = resolveCompanyId();
        MeetingRoom room = meetingRoomRepository.findById(request.getRoomId())
                .filter(value -> companyId.equals(value.getCompanyId()))
                .orElseThrow(() -> new ResourceNotFoundException("Meeting room utilization record not found"));
        ReportPeriod period = resolvePeriod(request.getSearch());
        List<MeetingBooking> roomBookings = utilizationBookings(username, request.getSearch(), period).stream()
                .filter(booking -> room.getId().equals(booking.getMeetingRoomId()))
                .sorted(Comparator.comparing(MeetingBooking::getMeetingDate).thenComparing(MeetingBooking::getStartTime))
                .toList();
        RoomUtilizationReportListItemDto utilization = toRoomUtilization(room, roomBookings, availableHours(period));

        return MessageResponseDTO.<RoomUtilizationReportDetailDto>builder()
                .success(true)
                .message("Room utilization report detail loaded successfully")
                .data(RoomUtilizationReportDetailDto.builder()
                        .roomId(room.getId())
                        .roomCode(room.getRoomCode())
                        .roomName(room.getRoomName())
                        .capacity(room.getCapacity())
                        .periodFrom(period.from())
                        .periodTo(period.to())
                        .utilization(utilization)
                        .bookingHistory(roomBookings.stream().map(this::toHistory).toList())
                        .build())
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public MessageResponseDTO<RoomUtilizationReportExportDto> export(RoomUtilizationReportExportRequest request) {
        String username = requireUsername(request != null ? request.getUsername() : null);
        String exportType = StringUtils.hasText(request != null ? request.getExportType() : null)
                ? request.getExportType().trim().toUpperCase(Locale.ENGLISH)
                : "CSV";
        if (!List.of("EXCEL", "PDF", "CSV", "PRINT").contains(exportType)) {
            throw new BadRequestException("Invalid export type");
        }
        ReportPeriod period = resolvePeriod(request != null ? request.getSearch() : null);
        List<RoomUtilizationReportListItemDto> rows = exportRows(request, username, period);
        List<MeetingBooking> bookings = utilizationBookings(username, request != null ? request.getSearch() : null, period);

        return MessageResponseDTO.<RoomUtilizationReportExportDto>builder()
                .success(true)
                .message("Room utilization report export data loaded successfully")
                .data(RoomUtilizationReportExportDto.builder()
                        .exportType(exportType)
                        .generatedDate(LocalDateTime.now())
                        .periodFrom(period.from())
                        .periodTo(period.to())
                        .summary(toSummary(rows, bookings))
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
    public byte[] exportExcelFile(RoomUtilizationReportExportRequest request) {
        String username = requireUsername(request != null ? request.getUsername() : null);
        ReportPeriod period = resolvePeriod(request != null ? request.getSearch() : null);
        List<RoomUtilizationReportListItemDto> rows = exportRows(request, username, period);
        RoomUtilizationSummaryDto summary = toSummary(rows, utilizationBookings(username, request != null ? request.getSearch() : null, period));

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Room Utilization");
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            int rowIndex = 0;
            sheet.createRow(rowIndex++).createCell(0).setCellValue("Room Utilization Report");
            Row periodRow = sheet.createRow(rowIndex++);
            periodRow.createCell(0).setCellValue("Period");
            periodRow.createCell(1).setCellValue(period.from() + " to " + period.to());
            rowIndex++;
            rowIndex = writeSummaryRows(sheet, rowIndex, summary);
            rowIndex++;

            Row headerRow = sheet.createRow(rowIndex++);
            String[] headers = reportHeaders();
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            for (RoomUtilizationReportListItemDto row : rows) {
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
    public byte[] exportPdfFile(RoomUtilizationReportExportRequest request) {
        String username = requireUsername(request != null ? request.getUsername() : null);
        ReportPeriod period = resolvePeriod(request != null ? request.getSearch() : null);
        List<RoomUtilizationReportListItemDto> rows = exportRows(request, username, period);
        RoomUtilizationSummaryDto summary = toSummary(rows, utilizationBookings(username, request != null ? request.getSearch() : null, period));

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, outputStream);
            document.open();
            document.add(new Paragraph("Room Utilization Report"));
            document.add(new Paragraph("Period: " + period.from() + " to " + period.to()));
            document.add(new Paragraph("Total Rooms: " + summary.getTotalMeetingRooms()
                    + " | Bookings: " + summary.getTotalBookings()
                    + " | Hours: " + summary.getTotalBookingHours()
                    + " | Avg Utilization: " + summary.getAverageUtilization() + "%"));
            document.add(new Paragraph("Most Utilized: " + text(summary.getMostUtilizedRoom())
                    + " | Least Utilized: " + text(summary.getLeastUtilizedRoom())));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(reportHeaders().length);
            table.setWidthPercentage(100);
            for (String header : reportHeaders()) {
                table.addCell(new PdfPCell(new Phrase(header)));
            }
            for (RoomUtilizationReportListItemDto row : rows) {
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
    public byte[] exportCsvFile(RoomUtilizationReportExportRequest request) {
        String username = requireUsername(request != null ? request.getUsername() : null);
        ReportPeriod period = resolvePeriod(request != null ? request.getSearch() : null);
        List<RoomUtilizationReportListItemDto> rows = exportRows(request, username, period);
        RoomUtilizationSummaryDto summary = toSummary(rows, utilizationBookings(username, request != null ? request.getSearch() : null, period));

        StringBuilder builder = new StringBuilder();
        builder.append("Room Utilization Report").append(System.lineSeparator());
        builder.append("Period,").append(csv(period.from() + " to " + period.to())).append(System.lineSeparator());
        builder.append("Total Meeting Rooms,").append(summary.getTotalMeetingRooms()).append(System.lineSeparator());
        builder.append("Total Bookings,").append(summary.getTotalBookings()).append(System.lineSeparator());
        builder.append("Total Booking Hours,").append(summary.getTotalBookingHours()).append(System.lineSeparator());
        builder.append("Average Utilization,").append(summary.getAverageUtilization()).append("%").append(System.lineSeparator());
        builder.append("Most Utilized Room,").append(csv(summary.getMostUtilizedRoom())).append(System.lineSeparator());
        builder.append("Least Utilized Room,").append(csv(summary.getLeastUtilizedRoom())).append(System.lineSeparator());
        builder.append("Most Active Day,").append(csv(summary.getMostActiveDay())).append(System.lineSeparator());
        builder.append("Most Active Time Slot,").append(csv(summary.getMostActiveTimeSlot())).append(System.lineSeparator());
        builder.append("Highest Booked Room,").append(csv(summary.getHighestBookedRoom())).append(System.lineSeparator());
        builder.append(System.lineSeparator());
        builder.append(String.join(",", reportHeaders())).append(System.lineSeparator());
        for (RoomUtilizationReportListItemDto row : rows) {
            builder.append(String.join(",", List.of(rowValues(row)).stream().map(this::csv).toList()))
                    .append(System.lineSeparator());
        }
        return builder.toString().getBytes(StandardCharsets.UTF_8);
    }

    private ReportDataset dataset(String username, RoomUtilizationReportFilterSearch search, ReportPeriod period) {
        Long companyId = resolveCompanyId();
        List<MeetingRoom> rooms = companyRooms(companyId).stream()
                .filter(room -> search == null || search.getMeetingRoomId() == null || search.getMeetingRoomId().equals(room.getId()))
                .filter(room -> search == null || contains(room.getRoomName(), search.getMeetingRoomName()))
                .toList();
        List<MeetingBooking> bookings = utilizationBookings(username, search, period).stream()
                .filter(booking -> rooms.stream().anyMatch(room -> room.getId().equals(booking.getMeetingRoomId())))
                .toList();
        BigDecimal availableHours = availableHours(period);
        List<RoomUtilizationReportListItemDto> rows = rooms.stream()
                .map(room -> toRoomUtilization(room, bookings.stream()
                        .filter(booking -> room.getId().equals(booking.getMeetingRoomId()))
                        .toList(), availableHours))
                .toList();
        return new ReportDataset(rows, bookings);
    }

    private List<MeetingBooking> utilizationBookings(String username, RoomUtilizationReportFilterSearch search, ReportPeriod period) {
        Long companyId = resolveCompanyId();
        PageTaskPrivileges privileges = pagePrivilegeResolver.resolve(username, PAGE_CODE);
        boolean canSeeAll = canSeeAll(username, privileges);

        return meetingBookingRepository.findAll().stream()
                .filter(booking -> companyId.equals(booking.getCompanyId()))
                .filter(booking -> canSeeAll || username.equalsIgnoreCase(nullToEmpty(booking.getCreatedBy())))
                .filter(booking -> UTILIZATION_STATUSES.contains(booking.getStatus()))
                .filter(booking -> booking.getMeetingDate() != null
                        && !booking.getMeetingDate().isBefore(period.from())
                        && !booking.getMeetingDate().isAfter(period.to()))
                .filter(booking -> matchesBookingFilters(booking, search))
                .toList();
    }

    private boolean matchesBookingFilters(MeetingBooking booking, RoomUtilizationReportFilterSearch search) {
        if (search == null) {
            return true;
        }
        return (search.getMeetingRoomId() == null || search.getMeetingRoomId().equals(booking.getMeetingRoomId()))
                && contains(booking.getMeetingRoomName(), search.getMeetingRoomName())
                && contains(booking.getMeetingType() != null ? booking.getMeetingType().name() : null, search.getMeetingType());
    }

    private List<RoomUtilizationReportListItemDto> exportRows(RoomUtilizationReportExportRequest request, String username, ReportPeriod period) {
        return dataset(username, request != null ? request.getSearch() : null, period).rows().stream()
                .sorted(resolveComparator(request != null ? request.getSortColumn() : null, request != null ? request.getSortDirection() : null))
                .toList();
    }

    private RoomUtilizationReportListItemDto toRoomUtilization(MeetingRoom room, List<MeetingBooking> roomBookings, BigDecimal availableHours) {
        BigDecimal totalHours = roomBookings.stream()
                .map(this::durationHours)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal utilization = availableHours.compareTo(BigDecimal.ZERO) > 0
                ? totalHours.multiply(BigDecimal.valueOf(100)).divide(availableHours, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        if (utilization.compareTo(BigDecimal.valueOf(100)) > 0) {
            utilization = BigDecimal.valueOf(100).setScale(2, RoundingMode.HALF_UP);
        }
        return RoomUtilizationReportListItemDto.builder()
                .roomId(room.getId())
                .roomCode(room.getRoomCode())
                .roomName(room.getRoomName())
                .capacity(room.getCapacity())
                .totalBookings(roomBookings.size())
                .totalHours(totalHours)
                .availableHours(availableHours.setScale(2, RoundingMode.HALF_UP))
                .utilizationPercentage(utilization)
                .internalMeetings(countType(roomBookings, MeetingBookingType.INTERNAL_MEETING))
                .externalMeetings(countType(roomBookings, MeetingBookingType.EXTERNAL_MEETING))
                .build();
    }

    private RoomUtilizationSummaryDto toSummary(List<RoomUtilizationReportListItemDto> rows, List<MeetingBooking> bookings) {
        BigDecimal totalHours = rows.stream()
                .map(RoomUtilizationReportListItemDto::getTotalHours)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalAvailableHours = rows.stream()
                .map(RoomUtilizationReportListItemDto::getAvailableHours)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal averageUtilization = totalAvailableHours.compareTo(BigDecimal.ZERO) > 0
                ? totalHours.multiply(BigDecimal.valueOf(100)).divide(totalAvailableHours, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        return RoomUtilizationSummaryDto.builder()
                .totalMeetingRooms(rows.size())
                .totalBookings(rows.stream().mapToLong(RoomUtilizationReportListItemDto::getTotalBookings).sum())
                .totalBookingHours(totalHours)
                .averageUtilization(averageUtilization)
                .mostUtilizedRoom(rows.stream().max(Comparator.comparing(RoomUtilizationReportListItemDto::getUtilizationPercentage))
                        .map(RoomUtilizationReportListItemDto::getRoomName).orElse(null))
                .leastUtilizedRoom(rows.stream().min(Comparator.comparing(RoomUtilizationReportListItemDto::getUtilizationPercentage))
                        .map(RoomUtilizationReportListItemDto::getRoomName).orElse(null))
                .mostActiveDay(mostActiveDay(bookings))
                .mostActiveTimeSlot(mostActiveTimeSlot(bookings))
                .highestBookedRoom(rows.stream().max(Comparator.comparing(RoomUtilizationReportListItemDto::getTotalBookings))
                        .map(RoomUtilizationReportListItemDto::getRoomName).orElse(null))
                .build();
    }

    private List<RoomBookingDistributionDto> toBookingDistribution(List<RoomUtilizationReportListItemDto> rows) {
        long totalBookings = rows.stream().mapToLong(RoomUtilizationReportListItemDto::getTotalBookings).sum();
        return rows.stream()
                .map(row -> RoomBookingDistributionDto.builder()
                        .roomId(row.getRoomId())
                        .roomName(row.getRoomName())
                        .bookingCount(row.getTotalBookings())
                        .percentage(totalBookings > 0
                                ? BigDecimal.valueOf(row.getTotalBookings()).multiply(BigDecimal.valueOf(100))
                                .divide(BigDecimal.valueOf(totalBookings), 2, RoundingMode.HALF_UP)
                                : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                        .build())
                .toList();
    }

    private String mostActiveDay(List<MeetingBooking> bookings) {
        return bookings.stream()
                .filter(booking -> booking.getMeetingDate() != null)
                .collect(Collectors.groupingBy(booking -> booking.getMeetingDate().getDayOfWeek(), Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(entry -> toTitleCase(entry.getKey().name()))
                .orElse(null);
    }

    private String mostActiveTimeSlot(List<MeetingBooking> bookings) {
        return bookings.stream()
                .filter(booking -> booking.getStartTime() != null)
                .map(booking -> timeSlot(booking.getStartTime()))
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    private String timeSlot(LocalTime time) {
        int startHour = (time.getHour() / 2) * 2;
        int endHour = Math.min(startHour + 2, 24);
        return "%02d:00 - %02d:00".formatted(startHour, endHour);
    }

    private long countType(List<MeetingBooking> bookings, MeetingBookingType type) {
        return bookings.stream().filter(booking -> booking.getMeetingType() == type).count();
    }

    private BigDecimal availableHours(ReportPeriod period) {
        return BigDecimal.valueOf(ChronoUnit.DAYS.between(period.from(), period.to()) + 1)
                .multiply(WORKING_HOURS_PER_DAY)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal durationHours(MeetingBooking booking) {
        if (booking.getDurationHours() != null) {
            return booking.getDurationHours().setScale(2, RoundingMode.HALF_UP);
        }
        if (booking.getStartTime() == null || booking.getEndTime() == null || !booking.getEndTime().isAfter(booking.getStartTime())) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(Duration.between(booking.getStartTime(), booking.getEndTime()).toMinutes())
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
    }

    private ReportPeriod resolvePeriod(RoomUtilizationReportFilterSearch search) {
        LocalDate today = LocalDate.now();
        LocalDate from = search != null && search.getDateFrom() != null ? search.getDateFrom() : today.withDayOfMonth(1);
        LocalDate to = search != null && search.getDateTo() != null ? search.getDateTo() : today.withDayOfMonth(today.lengthOfMonth());
        if (to.isBefore(from)) {
            throw new BadRequestException("Invalid room utilization report date range");
        }
        return new ReportPeriod(from, to);
    }

    private Comparator<RoomUtilizationReportListItemDto> resolveComparator(String sortColumn, String sortDirection) {
        String column = StringUtils.hasText(sortColumn) ? sortColumn.trim() : "utilizationPercentage";
        boolean desc = !"ASC".equalsIgnoreCase(sortDirection);
        Comparator<RoomUtilizationReportListItemDto> comparator = switch (column) {
            case "roomName", "meetingRoomName" -> Comparator.comparing(RoomUtilizationReportListItemDto::getRoomName,
                    Comparator.nullsLast(String::compareToIgnoreCase));
            case "capacity" -> Comparator.comparing(RoomUtilizationReportListItemDto::getCapacity, Comparator.nullsLast(Integer::compareTo));
            case "totalBookings", "bookings" -> Comparator.comparing(RoomUtilizationReportListItemDto::getTotalBookings);
            case "totalHours", "hours" -> Comparator.comparing(RoomUtilizationReportListItemDto::getTotalHours);
            case "internalMeetings" -> Comparator.comparing(RoomUtilizationReportListItemDto::getInternalMeetings);
            case "externalMeetings" -> Comparator.comparing(RoomUtilizationReportListItemDto::getExternalMeetings);
            default -> Comparator.comparing(RoomUtilizationReportListItemDto::getUtilizationPercentage);
        };
        return desc ? comparator.reversed() : comparator;
    }

    private RoomUtilizationBookingHistoryDto toHistory(MeetingBooking booking) {
        return RoomUtilizationBookingHistoryDto.builder()
                .id(booking.getId())
                .requestNo(booking.getRequestNo())
                .meetingName(booking.getMeetingName())
                .meetingDate(booking.getMeetingDate())
                .startTime(booking.getStartTime())
                .endTime(booking.getEndTime())
                .durationHours(durationHours(booking))
                .status(booking.getStatus())
                .statusDescription(booking.getStatus() != null ? toTitleCase(booking.getStatus().name()) : null)
                .build();
    }

    private int writeSummaryRows(Sheet sheet, int rowIndex, RoomUtilizationSummaryDto summary) {
        rowIndex = writeSummaryRow(sheet, rowIndex, "Total Meeting Rooms", summary.getTotalMeetingRooms());
        rowIndex = writeSummaryRow(sheet, rowIndex, "Total Bookings", summary.getTotalBookings());
        rowIndex = writeSummaryRow(sheet, rowIndex, "Total Booking Hours", summary.getTotalBookingHours());
        rowIndex = writeSummaryRow(sheet, rowIndex, "Average Utilization", summary.getAverageUtilization() + "%");
        rowIndex = writeSummaryRow(sheet, rowIndex, "Most Utilized Room", summary.getMostUtilizedRoom());
        rowIndex = writeSummaryRow(sheet, rowIndex, "Least Utilized Room", summary.getLeastUtilizedRoom());
        rowIndex = writeSummaryRow(sheet, rowIndex, "Most Active Day", summary.getMostActiveDay());
        rowIndex = writeSummaryRow(sheet, rowIndex, "Most Active Time Slot", summary.getMostActiveTimeSlot());
        return writeSummaryRow(sheet, rowIndex, "Highest Booked Room", summary.getHighestBookedRoom());
    }

    private int writeSummaryRow(Sheet sheet, int rowIndex, String label, Object value) {
        Row row = sheet.createRow(rowIndex++);
        row.createCell(0).setCellValue(label);
        row.createCell(1).setCellValue(text(value));
        return rowIndex;
    }

    private String[] reportHeaders() {
        return new String[]{
                "Room Name",
                "Capacity",
                "Total Bookings",
                "Total Hours",
                "Available Hours",
                "Utilization %",
                "Internal Meetings",
                "External Meetings"
        };
    }

    private void writeRowCells(Row dataRow, RoomUtilizationReportListItemDto row) {
        String[] values = rowValues(row);
        for (int i = 0; i < values.length; i++) {
            dataRow.createCell(i).setCellValue(values[i]);
        }
    }

    private String[] rowValues(RoomUtilizationReportListItemDto row) {
        return new String[]{
                text(row.getRoomName()),
                text(row.getCapacity()),
                text(row.getTotalBookings()),
                text(row.getTotalHours()),
                text(row.getAvailableHours()),
                text(row.getUtilizationPercentage()),
                text(row.getInternalMeetings()),
                text(row.getExternalMeetings())
        };
    }

    private RoomUtilizationReportPrivilegesDto toPrivileges(PageTaskPrivileges privileges) {
        boolean export = privileges.isExport();
        return RoomUtilizationReportPrivilegesDto.builder()
                .search(privileges.isSearch())
                .resetFilters(privileges.isSearch())
                .viewDetails(privileges.isView())
                .viewAnalytics(privileges.isView() || privileges.isSearch())
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

    private record ReportPeriod(LocalDate from, LocalDate to) {
    }

    private record ReportDataset(List<RoomUtilizationReportListItemDto> rows, List<MeetingBooking> bookings) {
    }
}
