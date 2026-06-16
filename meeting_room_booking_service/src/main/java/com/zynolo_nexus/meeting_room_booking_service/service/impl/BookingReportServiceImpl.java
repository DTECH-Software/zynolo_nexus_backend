package com.zynolo_nexus.meeting_room_booking_service.service.impl;

import com.zynolo_nexus.meeting_room_booking_service.context.CompanyContext;
import com.zynolo_nexus.meeting_room_booking_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.BookingReportExportRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.BookingReportFilterRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.BookingReportFilterSearch;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.BookingReportReferenceDataRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.BookingReportViewRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.BookingReportDetailDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.BookingReportExportDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.BookingReportFilterResultDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.BookingReportListItemDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.BookingReportPrivilegesDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.BookingReportReferenceDataDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.BookingReportSummaryDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingBookingBeverageDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingBookingRefreshmentDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingBookingSupportServiceDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingRoomDto;
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
import com.zynolo_nexus.meeting_room_booking_service.service.BookingReportService;
import com.zynolo_nexus.meeting_room_booking_service.service.support.PagePrivilegeResolver;
import com.zynolo_nexus.meeting_room_booking_service.service.support.PageTaskPrivileges;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class BookingReportServiceImpl implements BookingReportService {

    private static final String PAGE_CODE = "MBM_RPRT_BOKR";

    private final MeetingBookingRepository meetingBookingRepository;
    private final MeetingRoomRepository meetingRoomRepository;
    private final CompanyLookupRepository companyLookupRepository;
    private final UserLookupRepository userLookupRepository;
    private final PagePrivilegeResolver pagePrivilegeResolver;

    @Value("${app.default.company-id:1}")
    private Long defaultCompanyId;

    @Override
    @Transactional(readOnly = true)
    public MessageResponseDTO<BookingReportReferenceDataDto> referenceData(BookingReportReferenceDataRequest request) {
        Long companyId = resolveCompanyId();
        CompanyLookup company = resolveCompany(companyId);
        PageTaskPrivileges privileges = pagePrivilegeResolver.resolve(request != null ? request.getUsername() : null, PAGE_CODE);

        return MessageResponseDTO.<BookingReportReferenceDataDto>builder()
                .success(true)
                .message("Meeting booking report reference data loaded successfully")
                .data(BookingReportReferenceDataDto.builder()
                        .companyId(companyId)
                        .companyCode(resolveCompanyCode(company))
                        .companyName(resolveCompanyName(company))
                        .meetingTypes(toOptions(MeetingBookingType.values()))
                        .statuses(toOptions(MeetingBookingStatus.values()))
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
    public MessageResponseDTO<BookingReportFilterResultDto> filterList(BookingReportFilterRequest request) {
        String username = requireUsername(request != null ? request.getUsername() : null);
        int page = request != null && request.getPage() != null && request.getPage() >= 0 ? request.getPage() : 0;
        int size = request != null && request.getSize() != null && request.getSize() > 0 ? request.getSize() : 10;
        BookingReportFilterSearch search = request != null ? request.getSearch() : null;

        List<MeetingBooking> filteredBookings = filteredBookings(username, search)
                .stream()
                .sorted(resolveComparator(request != null ? request.getSortColumn() : null, request != null ? request.getSortDirection() : null))
                .toList();

        List<BookingReportListItemDto> rows = filteredBookings.stream().map(this::toListItem).toList();
        int from = Math.min(page * size, rows.size());
        int to = Math.min(from + size, rows.size());
        List<BookingReportListItemDto> content = rows.subList(from, to);
        int totalPages = rows.isEmpty() ? 0 : (int) Math.ceil((double) rows.size() / size);

        return MessageResponseDTO.<BookingReportFilterResultDto>builder()
                .success(true)
                .message("Meeting booking report filtered successfully")
                .data(BookingReportFilterResultDto.builder()
                        .summary(toSummary(filteredBookings))
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
    public MessageResponseDTO<BookingReportDetailDto> view(BookingReportViewRequest request) {
        if (request == null || request.getId() == null) {
            throw new BadRequestException("Invalid report view request");
        }
        String username = requireUsername(request.getUsername());
        MeetingBooking booking = findVisibleBooking(request.getId(), username);
        return MessageResponseDTO.<BookingReportDetailDto>builder()
                .success(true)
                .message("Meeting booking report detail loaded successfully")
                .data(toDetail(booking))
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public MessageResponseDTO<BookingReportExportDto> export(BookingReportExportRequest request) {
        String username = requireUsername(request != null ? request.getUsername() : null);
        String exportType = StringUtils.hasText(request != null ? request.getExportType() : null)
                ? request.getExportType().trim().toUpperCase(Locale.ENGLISH)
                : "CSV";
        if (!List.of("EXCEL", "PDF", "CSV", "PRINT").contains(exportType)) {
            throw new BadRequestException("Invalid export type");
        }

        List<MeetingBooking> filteredBookings = filteredBookings(username, request != null ? request.getSearch() : null)
                .stream()
                .sorted(resolveComparator(request != null ? request.getSortColumn() : null, request != null ? request.getSortDirection() : null))
                .toList();
        List<BookingReportListItemDto> rows = filteredBookings.stream().map(this::toListItem).toList();

        return MessageResponseDTO.<BookingReportExportDto>builder()
                .success(true)
                .message("Meeting booking report export data loaded successfully")
                .data(BookingReportExportDto.builder()
                        .exportType(exportType)
                        .generatedDate(LocalDateTime.now())
                        .summary(toSummary(filteredBookings))
                        .rows(rows)
                        .totalRecords(rows.size())
                        .build())
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    private List<MeetingBooking> filteredBookings(String username, BookingReportFilterSearch search) {
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
                .orElseThrow(() -> new ResourceNotFoundException("Meeting booking report record not found"));
        if (!companyId.equals(booking.getCompanyId()) || (!canSeeAll && !username.equalsIgnoreCase(nullToEmpty(booking.getCreatedBy())))) {
            throw new ResourceNotFoundException("Meeting booking report record not found");
        }
        return booking;
    }

    private boolean matches(MeetingBooking booking, BookingReportFilterSearch search) {
        if (search == null) {
            return true;
        }
        return (search.getDateFrom() == null || !booking.getMeetingDate().isBefore(search.getDateFrom()))
                && (search.getDateTo() == null || !booking.getMeetingDate().isAfter(search.getDateTo()))
                && contains(booking.getRequestNo(), search.getRequestNo())
                && contains(booking.getMeetingName(), search.getMeetingName())
                && (search.getMeetingRoomId() == null || search.getMeetingRoomId().equals(booking.getMeetingRoomId()))
                && contains(booking.getMeetingRoomName(), search.getMeetingRoomName())
                && contains(booking.getMeetingType() != null ? booking.getMeetingType().name() : null, search.getMeetingType())
                && contains(booking.getStatus() != null ? booking.getStatus().name() : null, search.getStatus())
                && contains(booking.getCreatedBy(), search.getRequestedBy())
                && contains(booking.getApprovedBy(), search.getApprover())
                && (search.getAttendeeCountMin() == null || defaultInt(booking.getNumberOfAttendees()) >= search.getAttendeeCountMin())
                && (search.getAttendeeCountMax() == null || defaultInt(booking.getNumberOfAttendees()) <= search.getAttendeeCountMax());
    }

    private Comparator<MeetingBooking> resolveComparator(String sortColumn, String sortDirection) {
        String column = StringUtils.hasText(sortColumn) ? sortColumn.trim() : "meetingDate";
        boolean desc = "DESC".equalsIgnoreCase(sortDirection);
        Comparator<MeetingBooking> comparator = switch (column) {
            case "requestNo" -> Comparator.comparing(MeetingBooking::getRequestNo, Comparator.nullsLast(String::compareToIgnoreCase));
            case "meetingName" -> Comparator.comparing(MeetingBooking::getMeetingName, Comparator.nullsLast(String::compareToIgnoreCase));
            case "meetingRoom", "meetingRoomName" -> Comparator.comparing(MeetingBooking::getMeetingRoomName, Comparator.nullsLast(String::compareToIgnoreCase));
            case "requestedBy" -> Comparator.comparing(MeetingBooking::getCreatedBy, Comparator.nullsLast(String::compareToIgnoreCase));
            case "status" -> Comparator.comparing(booking -> booking.getStatus() != null ? booking.getStatus().name() : null,
                    Comparator.nullsLast(String::compareToIgnoreCase));
            case "lastModifiedDate" -> Comparator.comparing(MeetingBooking::getLastModifiedDate, Comparator.nullsLast(LocalDateTime::compareTo));
            default -> Comparator.comparing(MeetingBooking::getMeetingDate, Comparator.nullsLast(LocalDate::compareTo))
                    .thenComparing(MeetingBooking::getStartTime, Comparator.nullsLast(LocalTime::compareTo));
        };
        return desc ? comparator.reversed() : comparator;
    }

    private BookingReportSummaryDto toSummary(List<MeetingBooking> bookings) {
        return BookingReportSummaryDto.builder()
                .totalBookings(bookings.size())
                .internalMeetings(countType(bookings, MeetingBookingType.INTERNAL_MEETING))
                .externalMeetings(countType(bookings, MeetingBookingType.EXTERNAL_MEETING))
                .approvedMeetings(countStatus(bookings, MeetingBookingStatus.APPROVED))
                .cancelledMeetings(countStatus(bookings, MeetingBookingStatus.CANCELLED))
                .pendingApprovals(countStatus(bookings, MeetingBookingStatus.PENDING_APPROVAL))
                .build();
    }

    private long countType(List<MeetingBooking> bookings, MeetingBookingType type) {
        return bookings.stream().filter(booking -> booking.getMeetingType() == type).count();
    }

    private long countStatus(List<MeetingBooking> bookings, MeetingBookingStatus status) {
        return bookings.stream().filter(booking -> booking.getStatus() == status).count();
    }

    private BookingReportListItemDto toListItem(MeetingBooking booking) {
        return BookingReportListItemDto.builder()
                .id(booking.getId())
                .requestNo(booking.getRequestNo())
                .meetingName(booking.getMeetingName())
                .meetingRoomId(booking.getMeetingRoomId())
                .meetingRoomName(booking.getMeetingRoomName())
                .meetingDate(booking.getMeetingDate())
                .startTime(booking.getStartTime())
                .endTime(booking.getEndTime())
                .meetingType(booking.getMeetingType())
                .meetingTypeDescription(booking.getMeetingType() != null ? toTitleCase(booking.getMeetingType().name()) : null)
                .attendees(booking.getNumberOfAttendees())
                .requestedBy(booking.getCreatedBy())
                .status(booking.getStatus())
                .statusDescription(booking.getStatus() != null ? toTitleCase(booking.getStatus().name()) : null)
                .build();
    }

    private BookingReportDetailDto toDetail(MeetingBooking booking) {
        return BookingReportDetailDto.builder()
                .id(booking.getId())
                .requestNo(booking.getRequestNo())
                .meetingName(booking.getMeetingName())
                .meetingType(booking.getMeetingType())
                .meetingTypeDescription(booking.getMeetingType() != null ? toTitleCase(booking.getMeetingType().name()) : null)
                .meetingRoomId(booking.getMeetingRoomId())
                .meetingRoomCode(booking.getMeetingRoomCode())
                .meetingRoomName(booking.getMeetingRoomName())
                .meetingDate(booking.getMeetingDate())
                .startTime(booking.getStartTime())
                .endTime(booking.getEndTime())
                .attendees(booking.getNumberOfAttendees())
                .remarks(booking.getPurposeRemarks())
                .status(booking.getStatus())
                .statusDescription(booking.getStatus() != null ? toTitleCase(booking.getStatus().name()) : null)
                .refreshments(booking.getRefreshments().stream().map(this::toRefreshmentDto).toList())
                .beverages(booking.getBeverages().stream().map(this::toBeverageDto).toList())
                .supportServices(booking.getSupportServices().stream().map(this::toSupportDto).toList())
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
                .createdDate(booking.getCreatedDate())
                .createdBy(booking.getCreatedBy())
                .build();
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

    private BookingReportPrivilegesDto toPrivileges(PageTaskPrivileges privileges) {
        boolean export = privileges.isExport();
        return BookingReportPrivilegesDto.builder()
                .search(privileges.isSearch())
                .resetFilters(privileges.isSearch())
                .viewDetails(privileges.isView())
                .exportExcel(export)
                .exportPdf(export)
                .exportCsv(export)
                .print(privileges.isPrint() || export)
                .build();
    }

    private boolean canSeeAll(String username, PageTaskPrivileges privileges) {
        if (privileges.isApprove() || privileges.isReject()) {
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

    private int defaultInt(Integer value) {
        return value != null ? value : 0;
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
