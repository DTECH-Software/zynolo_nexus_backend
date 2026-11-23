package com.zynolo_nexus.meeting_room_booking_service.service.impl;

import com.zynolo_nexus.meeting_room_booking_service.context.CompanyContext;
import com.zynolo_nexus.meeting_room_booking_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.CalendarEventDetailRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.CalendarEventsRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.CalendarFilterSearch;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.CalendarReferenceDataRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.CalendarEventDetailDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.CalendarEventDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.CalendarEventsResultDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.CalendarPrivilegesDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.CalendarQuickActionsDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.CalendarReferenceDataDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.CalendarRoomAvailabilityDto;
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
import com.zynolo_nexus.meeting_room_booking_service.repository.CompanyLookupRepository;
import com.zynolo_nexus.meeting_room_booking_service.repository.MeetingBookingRepository;
import com.zynolo_nexus.meeting_room_booking_service.repository.MeetingRoomRepository;
import com.zynolo_nexus.meeting_room_booking_service.service.CalendarService;
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
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class CalendarServiceImpl implements CalendarService {

    private static final String PAGE_CODE = "MBM_CNDB_CALN";
    private static final Set<MeetingBookingStatus> BLOCKING_STATUSES = Set.of(
            MeetingBookingStatus.APPROVED,
            MeetingBookingStatus.ONGOING,
            MeetingBookingStatus.COMPLETED
    );
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH);

    private final MeetingBookingRepository meetingBookingRepository;
    private final MeetingRoomRepository meetingRoomRepository;
    private final CompanyLookupRepository companyLookupRepository;
    private final PagePrivilegeResolver pagePrivilegeResolver;

    @Value("${app.default.company-id:1}")
    private Long defaultCompanyId;

    @Override
    @Transactional(readOnly = true)
    public MessageResponseDTO<CalendarReferenceDataDto> referenceData(CalendarReferenceDataRequest request) {
        Long companyId = resolveCompanyId();
        CompanyLookup company = resolveCompany(companyId);
        PageTaskPrivileges privileges = pagePrivilegeResolver.resolve(request != null ? request.getUsername() : null, PAGE_CODE);

        CalendarPrivilegesDto calendarPrivileges = toPrivileges(privileges);
        return MessageResponseDTO.<CalendarReferenceDataDto>builder()
                .success(true)
                .message("Calendar reference data loaded successfully")
                .data(CalendarReferenceDataDto.builder()
                        .companyId(companyId)
                        .companyCode(resolveCompanyCode(company))
                        .companyName(resolveCompanyName(company))
                        .viewTypes(viewTypes())
                        .meetingTypes(toOptions(MeetingBookingType.values()))
                        .statuses(toOptions(MeetingBookingStatus.values()))
                        .statusColors(statusColors())
                        .meetingRooms(companyRooms(companyId).stream().map(this::toRoomDto).toList())
                        .privileges(calendarPrivileges)
                        .quickActions(toQuickActions(calendarPrivileges, null))
                        .build())
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public MessageResponseDTO<CalendarEventsResultDto> events(CalendarEventsRequest request) {
        CalendarFilterSearch search = request != null ? request.getSearch() : null;
        Long companyId = resolveCompanyId();
        LocalDate today = LocalDate.now();
        LocalDate dateFrom = search != null && search.getDateFrom() != null ? search.getDateFrom() : today.withDayOfMonth(1);
        LocalDate dateTo = search != null && search.getDateTo() != null ? search.getDateTo() : today.withDayOfMonth(today.lengthOfMonth());
        if (dateTo.isBefore(dateFrom)) {
            throw new BadRequestException("Invalid calendar date range");
        }
        PageTaskPrivileges privileges = pagePrivilegeResolver.resolve(request != null ? request.getUsername() : null, PAGE_CODE);
        CalendarPrivilegesDto calendarPrivileges = toPrivileges(privileges);

        List<MeetingBooking> bookings = meetingBookingRepository.findAll().stream()
                .filter(booking -> companyId.equals(booking.getCompanyId()))
                .filter(booking -> !booking.getMeetingDate().isBefore(dateFrom) && !booking.getMeetingDate().isAfter(dateTo))
                .filter(booking -> matches(booking, search))
                .sorted(Comparator.comparing(MeetingBooking::getMeetingDate).thenComparing(MeetingBooking::getStartTime))
                .toList();

        List<CalendarEventDto> events = bookings.stream()
                .map(booking -> toEvent(booking, calendarPrivileges))
                .toList();

        List<CalendarRoomAvailabilityDto> availability = buildAvailability(companyRooms(companyId), bookings, dateFrom, dateTo, search);

        return MessageResponseDTO.<CalendarEventsResultDto>builder()
                .success(true)
                .message("Calendar events loaded successfully")
                .data(CalendarEventsResultDto.builder()
                        .events(events)
                        .roomAvailability(availability)
                        .totalEvents(events.size())
                        .build())
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public MessageResponseDTO<CalendarEventDetailDto> detail(CalendarEventDetailRequest request) {
        if (request == null || request.getId() == null) {
            throw new BadRequestException("Invalid calendar detail request");
        }
        Long companyId = resolveCompanyId();
        MeetingBooking booking = meetingBookingRepository.findById(request.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Calendar event not found"));
        if (!companyId.equals(booking.getCompanyId())) {
            throw new ResourceNotFoundException("Calendar event not found");
        }
        PageTaskPrivileges privileges = pagePrivilegeResolver.resolve(request.getUsername(), PAGE_CODE);
        CalendarPrivilegesDto calendarPrivileges = toPrivileges(privileges);

        return MessageResponseDTO.<CalendarEventDetailDto>builder()
                .success(true)
                .message("Calendar event detail loaded successfully")
                .data(toDetail(booking, calendarPrivileges))
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    private List<CalendarRoomAvailabilityDto> buildAvailability(List<MeetingRoom> rooms,
                                                                List<MeetingBooking> bookings,
                                                                LocalDate dateFrom,
                                                                LocalDate dateTo,
                                                                CalendarFilterSearch search) {
        List<LocalDate> dates = dateFrom.datesUntil(dateTo.plusDays(1)).toList();
        return rooms.stream()
                .filter(room -> search == null || search.getMeetingRoomId() == null || search.getMeetingRoomId().equals(room.getId()))
                .flatMap(room -> dates.stream().map(date -> {
                    long blockingCount = bookings.stream()
                            .filter(booking -> room.getId().equals(booking.getMeetingRoomId()))
                            .filter(booking -> date.equals(booking.getMeetingDate()))
                            .filter(this::blocksAvailability)
                            .count();
                    return CalendarRoomAvailabilityDto.builder()
                            .roomId(room.getId())
                            .roomCode(room.getRoomCode())
                            .roomName(room.getRoomName())
                            .date(date)
                            .blockingBookingCount(blockingCount)
                            .available(blockingCount == 0 && Boolean.TRUE.equals(room.getActive())
                                    && room.getAvailabilityStatus() == RoomAvailabilityStatus.AVAILABLE)
                            .build();
                }))
                .toList();
    }

    private CalendarEventDto toEvent(MeetingBooking booking, CalendarPrivilegesDto privileges) {
        return CalendarEventDto.builder()
                .id(booking.getId())
                .requestNo(booking.getRequestNo())
                .title(booking.getMeetingName())
                .meetingName(booking.getMeetingName())
                .meetingType(booking.getMeetingType())
                .meetingTypeDescription(booking.getMeetingType() != null ? toTitleCase(booking.getMeetingType().name()) : null)
                .meetingRoomId(booking.getMeetingRoomId())
                .meetingRoomName(booking.getMeetingRoomName())
                .meetingDate(booking.getMeetingDate())
                .startTime(booking.getStartTime())
                .endTime(booking.getEndTime())
                .timeLabel(timeLabel(booking.getStartTime(), booking.getEndTime()))
                .status(booking.getStatus())
                .statusDescription(booking.getStatus() != null ? toTitleCase(booking.getStatus().name()) : null)
                .statusColor(statusColor(booking.getStatus()))
                .tentative(booking.getStatus() == MeetingBookingStatus.PENDING_APPROVAL)
                .blocksAvailability(blocksAvailability(booking))
                .requestedBy(booking.getCreatedBy())
                .actions(toQuickActions(privileges, booking))
                .build();
    }

    private CalendarEventDetailDto toDetail(MeetingBooking booking, CalendarPrivilegesDto privileges) {
        return CalendarEventDetailDto.builder()
                .id(booking.getId())
                .requestNo(booking.getRequestNo())
                .meetingName(booking.getMeetingName())
                .meetingRoomId(booking.getMeetingRoomId())
                .meetingRoomCode(booking.getMeetingRoomCode())
                .meetingRoomName(booking.getMeetingRoomName())
                .meetingDate(booking.getMeetingDate())
                .startTime(booking.getStartTime())
                .endTime(booking.getEndTime())
                .numberOfAttendees(booking.getNumberOfAttendees())
                .meetingType(booking.getMeetingType())
                .meetingTypeDescription(booking.getMeetingType() != null ? toTitleCase(booking.getMeetingType().name()) : null)
                .status(booking.getStatus())
                .statusDescription(booking.getStatus() != null ? toTitleCase(booking.getStatus().name()) : null)
                .statusColor(statusColor(booking.getStatus()))
                .tentative(booking.getStatus() == MeetingBookingStatus.PENDING_APPROVAL)
                .blocksAvailability(blocksAvailability(booking))
                .purposeRemarks(booking.getPurposeRemarks())
                .refreshments(booking.getRefreshments().stream().map(this::toRefreshmentDto).toList())
                .beverages(booking.getBeverages().stream().map(this::toBeverageDto).toList())
                .supportServices(booking.getSupportServices().stream().map(this::toSupportDto).toList())
                .actions(toQuickActions(privileges, booking))
                .build();
    }

    private boolean matches(MeetingBooking booking, CalendarFilterSearch search) {
        if (search == null) {
            return true;
        }
        return (search.getMeetingRoomId() == null || search.getMeetingRoomId().equals(booking.getMeetingRoomId()))
                && contains(booking.getMeetingRoomName(), search.getMeetingRoomName())
                && contains(booking.getMeetingType() != null ? booking.getMeetingType().name() : null, search.getMeetingType())
                && contains(booking.getStatus() != null ? booking.getStatus().name() : null, search.getStatus())
                && contains(booking.getCreatedBy(), search.getRequestedBy());
    }

    private boolean blocksAvailability(MeetingBooking booking) {
        return booking != null && BLOCKING_STATUSES.contains(booking.getStatus());
    }

    private CalendarPrivilegesDto toPrivileges(PageTaskPrivileges privileges) {
        return CalendarPrivilegesDto.builder()
                .view(privileges.isView())
                .search(privileges.isSearch())
                .createBooking(privileges.isAdd() || privileges.isSaveDraft())
                .cancelBooking(privileges.isCancel())
                .myBookings(privileges.isView())
                .build();
    }

    private CalendarQuickActionsDto toQuickActions(CalendarPrivilegesDto privileges, MeetingBooking booking) {
        boolean cancellable = booking != null && (booking.getStatus() == MeetingBookingStatus.APPROVED
                || booking.getStatus() == MeetingBookingStatus.ONGOING);
        return CalendarQuickActionsDto.builder()
                .createBooking(privileges.isCreateBooking())
                .viewBooking(privileges.isView())
                .cancelBooking(privileges.isCancelBooking() && cancellable)
                .goToMyBookings(privileges.isMyBookings())
                .build();
    }

    private List<ReferenceOptionDto> viewTypes() {
        return List.of(
                option("DAY", "Day View"),
                option("WEEK", "Week View"),
                option("MONTH", "Month View"),
                option("ROOM_WISE", "Room Wise View")
        );
    }

    private List<ReferenceOptionDto> statusColors() {
        return Stream.of(MeetingBookingStatus.values())
                .map(status -> option(status.name(), statusColor(status)))
                .toList();
    }

    private String statusColor(MeetingBookingStatus status) {
        if (status == null) {
            return "#6B7280";
        }
        Map<MeetingBookingStatus, String> colors = Map.of(
                MeetingBookingStatus.DRAFT, "#9CA3AF",
                MeetingBookingStatus.PENDING_APPROVAL, "#F59E0B",
                MeetingBookingStatus.APPROVED, "#2563EB",
                MeetingBookingStatus.ONGOING, "#16A34A",
                MeetingBookingStatus.COMPLETED, "#166534",
                MeetingBookingStatus.REJECTED, "#DC2626",
                MeetingBookingStatus.CANCELLED, "#374151"
        );
        return colors.getOrDefault(status, "#6B7280");
    }

    private String timeLabel(LocalTime start, LocalTime end) {
        if (start == null || end == null) {
            return null;
        }
        return start.format(TIME_FORMAT) + " - " + end.format(TIME_FORMAT);
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
                .perHourCharge(room.getPerHourCharge())
                .availabilityStatus(room.getAvailabilityStatus())
                .availabilityStatusDescription(room.getAvailabilityStatus() != null ? toTitleCase(room.getAvailabilityStatus().name()) : null)
                .description(room.getDescription())
                .active(room.getActive())
                .activeStatusDescription(Boolean.TRUE.equals(room.getActive()) ? "Active" : "Inactive")
                .bookable(Boolean.TRUE.equals(room.getActive()) && room.getAvailabilityStatus() == RoomAvailabilityStatus.AVAILABLE)
                .build();
    }

    private List<ReferenceOptionDto> toOptions(Enum<?>[] values) {
        return List.of(values).stream().map(value -> option(value.name(), toTitleCase(value.name()))).toList();
    }

    private ReferenceOptionDto option(String code, String description) {
        return ReferenceOptionDto.builder().code(code).description(description).build();
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

    private boolean contains(String source, String expected) {
        if (!StringUtils.hasText(expected)) {
            return true;
        }
        return source != null && source.toLowerCase(Locale.ENGLISH).contains(expected.trim().toLowerCase(Locale.ENGLISH));
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
