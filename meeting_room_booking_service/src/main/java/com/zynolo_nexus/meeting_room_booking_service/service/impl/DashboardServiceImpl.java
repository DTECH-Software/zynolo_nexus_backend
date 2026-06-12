package com.zynolo_nexus.meeting_room_booking_service.service.impl;

import com.zynolo_nexus.meeting_room_booking_service.context.CompanyContext;
import com.zynolo_nexus.meeting_room_booking_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.DashboardFilterSearch;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.DashboardOverviewRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.DashboardReferenceDataRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.DashboardActivityDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.DashboardApprovalActionsDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.DashboardKpiDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.DashboardMeetingListItemDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.DashboardOngoingActionsDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.DashboardOngoingMeetingDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.DashboardOverviewDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.DashboardPendingApprovalDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.DashboardPrivilegesDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.DashboardQuickActionsDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.DashboardReferenceDataDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.DashboardRoomUtilizationDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.DashboardUtilizationSummaryDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingRoomDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.ReferenceOptionDto;
import com.zynolo_nexus.meeting_room_booking_service.enums.MeetingBookingStatus;
import com.zynolo_nexus.meeting_room_booking_service.enums.MeetingBookingType;
import com.zynolo_nexus.meeting_room_booking_service.enums.RoomAvailabilityStatus;
import com.zynolo_nexus.meeting_room_booking_service.exception.BadRequestException;
import com.zynolo_nexus.meeting_room_booking_service.model.CompanyLookup;
import com.zynolo_nexus.meeting_room_booking_service.model.MeetingBooking;
import com.zynolo_nexus.meeting_room_booking_service.model.MeetingRoom;
import com.zynolo_nexus.meeting_room_booking_service.model.UserLookup;
import com.zynolo_nexus.meeting_room_booking_service.repository.CompanyLookupRepository;
import com.zynolo_nexus.meeting_room_booking_service.repository.MeetingBookingRepository;
import com.zynolo_nexus.meeting_room_booking_service.repository.MeetingRoomRepository;
import com.zynolo_nexus.meeting_room_booking_service.repository.UserLookupRepository;
import com.zynolo_nexus.meeting_room_booking_service.service.DashboardService;
import com.zynolo_nexus.meeting_room_booking_service.service.support.PagePrivilegeResolver;
import com.zynolo_nexus.meeting_room_booking_service.service.support.PageTaskPrivileges;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private static final String PAGE_CODE = "MBM_CNDB_DASH";
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
    public MessageResponseDTO<DashboardReferenceDataDto> referenceData(DashboardReferenceDataRequest request) {
        Long companyId = resolveCompanyId();
        CompanyLookup company = resolveCompany(companyId);
        PageTaskPrivileges privileges = pagePrivilegeResolver.resolve(request != null ? request.getUsername() : null, PAGE_CODE);

        return MessageResponseDTO.<DashboardReferenceDataDto>builder()
                .success(true)
                .message("Dashboard reference data loaded successfully")
                .data(DashboardReferenceDataDto.builder()
                        .companyId(companyId)
                        .companyCode(resolveCompanyCode(company))
                        .companyName(resolveCompanyName(company))
                        .meetingTypes(toOptions(MeetingBookingType.values()))
                        .statuses(toOptions(MeetingBookingStatus.values()))
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
    public MessageResponseDTO<DashboardOverviewDto> overview(DashboardOverviewRequest request) {
        String username = requireUsername(request != null ? request.getUsername() : null);
        Long companyId = resolveCompanyId();
        CompanyLookup company = resolveCompany(companyId);
        DashboardFilterSearch search = request != null ? request.getSearch() : null;
        LocalDate today = LocalDate.now();
        LocalDate periodFrom = search != null && search.getDateFrom() != null ? search.getDateFrom() : today.withDayOfMonth(1);
        LocalDate periodTo = search != null && search.getDateTo() != null ? search.getDateTo() : today.withDayOfMonth(today.lengthOfMonth());
        if (periodTo.isBefore(periodFrom)) {
            throw new BadRequestException("Invalid dashboard date range");
        }

        PageTaskPrivileges privileges = pagePrivilegeResolver.resolve(username, PAGE_CODE);
        boolean canSeeAll = canSeeAll(username, privileges);
        List<MeetingBooking> scopedBookings = meetingBookingRepository.findAll().stream()
                .filter(booking -> companyId.equals(booking.getCompanyId()))
                .filter(booking -> canSeeAll || username.equalsIgnoreCase(nullToEmpty(booking.getCreatedBy())))
                .toList();

        List<MeetingBooking> periodBookings = scopedBookings.stream()
                .filter(booking -> matchesDashboardFilters(booking, search))
                .filter(booking -> !booking.getMeetingDate().isBefore(periodFrom) && !booking.getMeetingDate().isAfter(periodTo))
                .toList();

        List<MeetingBooking> quickBookings = scopedBookings.stream()
                .filter(booking -> matchesCommonFilters(booking, search))
                .toList();

        DashboardPrivilegesDto dashboardPrivileges = toPrivileges(privileges);
        List<MeetingRoom> rooms = companyRooms(companyId);
        List<DashboardRoomUtilizationDto> utilization = roomUtilization(rooms, periodBookings, periodFrom, periodTo);

        DashboardOverviewDto data = DashboardOverviewDto.builder()
                .companyId(companyId)
                .companyCode(resolveCompanyCode(company))
                .companyName(resolveCompanyName(company))
                .periodFrom(periodFrom)
                .periodTo(periodTo)
                .visibilityScope(canSeeAll ? "ALL_COMPANY_MEETINGS" : "MY_MEETINGS")
                .kpis(toKpis(periodBookings))
                .roomUtilization(utilization)
                .utilizationSummary(toUtilizationSummary(utilization))
                .todayMeetings(todayMeetings(quickBookings, today))
                .upcomingMeetings(upcomingMeetings(quickBookings, today))
                .pendingApprovals(pendingApprovals(periodBookings, dashboardPrivileges))
                .ongoingMeetings(ongoingMeetings(periodBookings, dashboardPrivileges))
                .recentActivities(recentActivities(periodBookings))
                .quickActions(toQuickActions(dashboardPrivileges))
                .privileges(dashboardPrivileges)
                .build();

        return MessageResponseDTO.<DashboardOverviewDto>builder()
                .success(true)
                .message("Dashboard overview loaded successfully")
                .data(data)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    private DashboardKpiDto toKpis(List<MeetingBooking> bookings) {
        return DashboardKpiDto.builder()
                .totalMeetings(bookings.size())
                .pendingApprovals(countStatus(bookings, MeetingBookingStatus.PENDING_APPROVAL))
                .approvedMeetings(countStatus(bookings, MeetingBookingStatus.APPROVED))
                .ongoingMeetings(countStatus(bookings, MeetingBookingStatus.ONGOING))
                .completedMeetings(countStatus(bookings, MeetingBookingStatus.COMPLETED))
                .cancelledMeetings(countStatus(bookings, MeetingBookingStatus.CANCELLED))
                .build();
    }

    private long countStatus(List<MeetingBooking> bookings, MeetingBookingStatus status) {
        return bookings.stream().filter(booking -> booking.getStatus() == status).count();
    }

    private List<DashboardRoomUtilizationDto> roomUtilization(List<MeetingRoom> rooms,
                                                              List<MeetingBooking> periodBookings,
                                                              LocalDate periodFrom,
                                                              LocalDate periodTo) {
        BigDecimal periodCapacityHours = BigDecimal.valueOf(ChronoUnit.DAYS.between(periodFrom, periodTo) + 1)
                .multiply(WORKING_HOURS_PER_DAY)
                .setScale(2, RoundingMode.HALF_UP);

        return rooms.stream()
                .map(room -> {
                    List<MeetingBooking> roomBookings = periodBookings.stream()
                            .filter(booking -> UTILIZATION_STATUSES.contains(booking.getStatus()))
                            .filter(booking -> room.getId().equals(booking.getMeetingRoomId()))
                            .toList();
                    BigDecimal totalHours = roomBookings.stream()
                            .map(this::durationHours)
                            .reduce(BigDecimal.ZERO, BigDecimal::add)
                            .setScale(2, RoundingMode.HALF_UP);
                    BigDecimal utilization = periodCapacityHours.compareTo(BigDecimal.ZERO) > 0
                            ? totalHours.multiply(BigDecimal.valueOf(100)).divide(periodCapacityHours, 2, RoundingMode.HALF_UP)
                            : BigDecimal.ZERO;
                    if (utilization.compareTo(BigDecimal.valueOf(100)) > 0) {
                        utilization = BigDecimal.valueOf(100).setScale(2, RoundingMode.HALF_UP);
                    }
                    return DashboardRoomUtilizationDto.builder()
                            .roomId(room.getId())
                            .roomCode(room.getRoomCode())
                            .roomName(room.getRoomName())
                            .bookingCount(roomBookings.size())
                            .totalBookingHours(totalHours)
                            .periodCapacityHours(periodCapacityHours)
                            .utilizationPercentage(utilization)
                            .build();
                })
                .sorted(Comparator.comparing(DashboardRoomUtilizationDto::getUtilizationPercentage).reversed())
                .toList();
    }

    private DashboardUtilizationSummaryDto toUtilizationSummary(List<DashboardRoomUtilizationDto> utilization) {
        BigDecimal totalHours = utilization.stream()
                .map(DashboardRoomUtilizationDto::getTotalBookingHours)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        long bookingCount = utilization.stream().mapToLong(DashboardRoomUtilizationDto::getBookingCount).sum();
        BigDecimal averageDuration = bookingCount > 0
                ? totalHours.divide(BigDecimal.valueOf(bookingCount), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        String mostUsed = utilization.stream().max(Comparator.comparing(DashboardRoomUtilizationDto::getTotalBookingHours))
                .map(DashboardRoomUtilizationDto::getRoomName).orElse(null);
        String leastUsed = utilization.stream().min(Comparator.comparing(DashboardRoomUtilizationDto::getTotalBookingHours))
                .map(DashboardRoomUtilizationDto::getRoomName).orElse(null);
        return DashboardUtilizationSummaryDto.builder()
                .totalBookingHours(totalHours)
                .averageMeetingDuration(averageDuration)
                .mostUsedRoom(mostUsed)
                .leastUsedRoom(leastUsed)
                .build();
    }

    private List<DashboardMeetingListItemDto> todayMeetings(List<MeetingBooking> bookings, LocalDate today) {
        return bookings.stream()
                .filter(booking -> today.equals(booking.getMeetingDate()))
                .sorted(Comparator.comparing(MeetingBooking::getStartTime, Comparator.nullsLast(LocalTime::compareTo)))
                .map(this::toMeetingItem)
                .toList();
    }

    private List<DashboardMeetingListItemDto> upcomingMeetings(List<MeetingBooking> bookings, LocalDate today) {
        LocalDate from = today.plusDays(1);
        LocalDate to = today.plusDays(7);
        return bookings.stream()
                .filter(booking -> !booking.getMeetingDate().isBefore(from) && !booking.getMeetingDate().isAfter(to))
                .sorted(Comparator.comparing(MeetingBooking::getMeetingDate).thenComparing(MeetingBooking::getStartTime))
                .limit(10)
                .map(this::toMeetingItem)
                .toList();
    }

    private List<DashboardPendingApprovalDto> pendingApprovals(List<MeetingBooking> bookings, DashboardPrivilegesDto privileges) {
        if (!privileges.isPendingApprovals()) {
            return List.of();
        }
        return bookings.stream()
                .filter(booking -> booking.getStatus() == MeetingBookingStatus.PENDING_APPROVAL)
                .sorted(Comparator.comparing(MeetingBooking::getSubmittedDate, Comparator.nullsLast(LocalDateTime::compareTo)).reversed())
                .limit(10)
                .map(booking -> DashboardPendingApprovalDto.builder()
                        .id(booking.getId())
                        .requestNo(booking.getRequestNo())
                        .meetingName(booking.getMeetingName())
                        .requestedBy(booking.getCreatedBy())
                        .meetingDate(booking.getMeetingDate())
                        .meetingRoomName(booking.getMeetingRoomName())
                        .actions(DashboardApprovalActionsDto.builder()
                                .view(privileges.isView())
                                .approve(privileges.isApprove())
                                .reject(privileges.isReject())
                                .build())
                        .build())
                .toList();
    }

    private List<DashboardOngoingMeetingDto> ongoingMeetings(List<MeetingBooking> bookings, DashboardPrivilegesDto privileges) {
        return bookings.stream()
                .filter(booking -> booking.getStatus() == MeetingBookingStatus.ONGOING)
                .sorted(Comparator.comparing(MeetingBooking::getMeetingDate).thenComparing(MeetingBooking::getStartTime))
                .limit(10)
                .map(booking -> DashboardOngoingMeetingDto.builder()
                        .id(booking.getId())
                        .requestNo(booking.getRequestNo())
                        .meetingName(booking.getMeetingName())
                        .meetingRoomName(booking.getMeetingRoomName())
                        .startedAt(booking.getStartTime())
                        .durationHours(durationHours(booking))
                        .actions(DashboardOngoingActionsDto.builder()
                                .view(privileges.isView())
                                .update(privileges.isOngoingUpdate() && !StringUtils.hasText(booking.getOngoingUpdate()))
                                .build())
                        .build())
                .toList();
    }

    private List<DashboardActivityDto> recentActivities(List<MeetingBooking> bookings) {
        List<DashboardActivityDto> activities = new ArrayList<>();
        bookings.forEach(booking -> {
            addActivity(activities, booking, "BOOKING_CREATED", "Booking Created", booking.getCreatedBy(), booking.getCreatedDate());
            addActivity(activities, booking, "BOOKING_SUBMITTED", "Booking Submitted", booking.getSubmittedBy(), booking.getSubmittedDate());
            addActivity(activities, booking, "BOOKING_APPROVED", "Booking Approved", booking.getApprovedBy(), booking.getApprovedDate());
            addActivity(activities, booking, "BOOKING_REJECTED", "Booking Rejected", booking.getRejectedBy(), booking.getRejectedDate());
            addActivity(activities, booking, "MEETING_UPDATED", "Meeting Updated", booking.getOngoingUpdatedBy(), booking.getOngoingUpdatedDate());
            addActivity(activities, booking, "BOOKING_CANCELLED", "Booking Cancelled", booking.getCancelledBy(), booking.getCancelledDate());
            if (StringUtils.hasText(booking.getInvoiceNo())) {
                addActivity(activities, booking, "INVOICE_GENERATED", "Invoice Generated", booking.getLastModifiedBy(), booking.getLastModifiedDate());
            }
        });
        return activities.stream()
                .sorted(Comparator.comparing(DashboardActivityDto::getActivityDate, Comparator.nullsLast(LocalDateTime::compareTo)).reversed())
                .limit(10)
                .toList();
    }

    private void addActivity(List<DashboardActivityDto> activities,
                             MeetingBooking booking,
                             String type,
                             String description,
                             String by,
                             LocalDateTime date) {
        if (date == null) {
            return;
        }
        activities.add(DashboardActivityDto.builder()
                .activityType(type)
                .description(description)
                .requestNo(booking.getRequestNo())
                .meetingName(booking.getMeetingName())
                .activityBy(by)
                .activityDate(date)
                .build());
    }

    private DashboardMeetingListItemDto toMeetingItem(MeetingBooking booking) {
        return DashboardMeetingListItemDto.builder()
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
                .numberOfAttendees(booking.getNumberOfAttendees())
                .status(booking.getStatus())
                .statusDescription(booking.getStatus() != null ? toTitleCase(booking.getStatus().name()) : null)
                .requestedBy(booking.getCreatedBy())
                .build();
    }

    private boolean matchesDashboardFilters(MeetingBooking booking, DashboardFilterSearch search) {
        return matchesCommonFilters(booking, search)
                && (search == null || !StringUtils.hasText(search.getStatus())
                || contains(booking.getStatus() != null ? booking.getStatus().name() : null, search.getStatus()));
    }

    private boolean matchesCommonFilters(MeetingBooking booking, DashboardFilterSearch search) {
        if (search == null) {
            return true;
        }
        return (search.getMeetingRoomId() == null || search.getMeetingRoomId().equals(booking.getMeetingRoomId()))
                && contains(booking.getMeetingRoomName(), search.getMeetingRoomName())
                && contains(booking.getMeetingType() != null ? booking.getMeetingType().name() : null, search.getMeetingType());
    }

    private DashboardPrivilegesDto toPrivileges(PageTaskPrivileges privileges) {
        return DashboardPrivilegesDto.builder()
                .view(privileges.isView())
                .search(privileges.isSearch())
                .createBooking(privileges.isAdd())
                .viewCalendar(privileges.isView() || privileges.isSearch())
                .myBookings(privileges.isView())
                .pendingApprovals(privileges.isApprove() || privileges.isReject())
                .approve(privileges.isApprove())
                .reject(privileges.isReject())
                .ongoingUpdate(privileges.isSaveUpdate() || privileges.isOngoingUpdate() || privileges.isUpdate())
                .build();
    }

    private DashboardQuickActionsDto toQuickActions(DashboardPrivilegesDto privileges) {
        return DashboardQuickActionsDto.builder()
                .createBooking(privileges.isCreateBooking())
                .viewCalendar(privileges.isViewCalendar())
                .myBookings(privileges.isMyBookings())
                .pendingApprovals(privileges.isPendingApprovals())
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

    private BigDecimal durationHours(MeetingBooking booking) {
        if (booking.getDurationHours() != null) {
            return booking.getDurationHours().setScale(2, RoundingMode.HALF_UP);
        }
        if (booking.getStartTime() == null || booking.getEndTime() == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(Duration.between(booking.getStartTime(), booking.getEndTime()).toMinutes())
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
    }

    private List<ReferenceOptionDto> toOptions(Enum<?>[] values) {
        return List.of(values).stream().map(this::toOption).toList();
    }

    private ReferenceOptionDto toOption(Enum<?> value) {
        return ReferenceOptionDto.builder()
                .code(value.name())
                .description(toTitleCase(value.name()))
                .build();
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
