package com.zynolo_nexus.meeting_room_booking_service.service.impl;

import com.zynolo_nexus.meeting_room_booking_service.context.CompanyContext;
import com.zynolo_nexus.meeting_room_booking_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.CancellationFilterRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.CancellationFilterSearch;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.CancellationReferenceDataRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.CancellationSubmitRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.CancellationViewRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.CancellationActionsDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.CancellationFilterResultDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.CancellationListItemDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.CancellationPrivilegesDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.CancellationReferenceDataDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingBookingDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingRoomDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.ReferenceOptionDto;
import com.zynolo_nexus.meeting_room_booking_service.enums.MeetingBookingStatus;
import com.zynolo_nexus.meeting_room_booking_service.enums.RoomAvailabilityStatus;
import com.zynolo_nexus.meeting_room_booking_service.exception.BadRequestException;
import com.zynolo_nexus.meeting_room_booking_service.exception.ResourceNotFoundException;
import com.zynolo_nexus.meeting_room_booking_service.model.CompanyLookup;
import com.zynolo_nexus.meeting_room_booking_service.model.MeetingBooking;
import com.zynolo_nexus.meeting_room_booking_service.model.MeetingRoom;
import com.zynolo_nexus.meeting_room_booking_service.repository.CompanyLookupRepository;
import com.zynolo_nexus.meeting_room_booking_service.repository.MeetingBookingRepository;
import com.zynolo_nexus.meeting_room_booking_service.repository.MeetingRoomRepository;
import com.zynolo_nexus.meeting_room_booking_service.service.CancellationService;
import com.zynolo_nexus.meeting_room_booking_service.service.MeetingBookingService;
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
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CancellationServiceImpl implements CancellationService {

    private static final String PAGE_CODE = "MBM_TRNS_CNEL";
    private static final Set<MeetingBookingStatus> CANCELLABLE_STATUSES = Set.of(
            MeetingBookingStatus.APPROVED,
            MeetingBookingStatus.ONGOING
    );
    private static final Set<MeetingBookingStatus> VISIBLE_CANCELLATION_STATUSES = Set.of(
            MeetingBookingStatus.APPROVED,
            MeetingBookingStatus.ONGOING,
            MeetingBookingStatus.CANCELLED
    );

    private final MeetingBookingRepository meetingBookingRepository;
    private final MeetingRoomRepository meetingRoomRepository;
    private final CompanyLookupRepository companyLookupRepository;
    private final PagePrivilegeResolver pagePrivilegeResolver;
    private final MeetingBookingService meetingBookingService;

    @Value("${app.default.company-id:1}")
    private Long defaultCompanyId;

    @Override
    @Transactional(readOnly = true)
    public MessageResponseDTO<CancellationReferenceDataDto> referenceData(CancellationReferenceDataRequest request) {
        Long companyId = resolveCompanyId();
        CompanyLookup company = resolveCompany(companyId);
        PageTaskPrivileges privileges = pagePrivilegeResolver.resolve(request != null ? request.getUsername() : null, PAGE_CODE);

        return MessageResponseDTO.<CancellationReferenceDataDto>builder()
                .success(true)
                .message("Cancellation reference data loaded successfully")
                .data(CancellationReferenceDataDto.builder()
                        .companyId(companyId)
                        .companyCode(resolveCompanyCode(company))
                        .companyName(resolveCompanyName(company))
                        .statuses(List.of(toOption(MeetingBookingStatus.APPROVED), toOption(MeetingBookingStatus.ONGOING), toOption(MeetingBookingStatus.CANCELLED)))
                        .cancellationReasons(cancellationReasons())
                        .meetingRooms(meetingRoomRepository.findAll().stream()
                                .filter(room -> companyId.equals(room.getCompanyId()))
                                .sorted(Comparator.comparing(MeetingRoom::getRoomName, Comparator.nullsLast(String::compareToIgnoreCase)))
                                .map(this::toRoomDto)
                                .toList())
                        .privileges(CancellationPrivilegesDto.builder()
                                .view(privileges.isView())
                                .search(privileges.isSearch())
                                .cancel(privileges.isCancel())
                                .build())
                        .build())
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public MessageResponseDTO<CancellationFilterResultDto> filterList(CancellationFilterRequest request) {
        String username = requireUsername(request != null ? request.getUsername() : null);
        int page = request != null && request.getPage() != null && request.getPage() >= 0 ? request.getPage() : 0;
        int size = request != null && request.getSize() != null && request.getSize() > 0 ? request.getSize() : 10;
        CancellationFilterSearch search = request != null ? request.getSearch() : null;
        Long companyId = resolveCompanyId();
        PageTaskPrivileges privileges = pagePrivilegeResolver.resolve(username, PAGE_CODE);

        List<CancellationListItemDto> filtered = meetingBookingRepository.findAll().stream()
                .filter(booking -> companyId.equals(booking.getCompanyId()))
                .filter(booking -> username.equalsIgnoreCase(nullToEmpty(booking.getCreatedBy())))
                .filter(this::isVisibleInCancellationPage)
                .filter(booking -> matches(booking, search))
                .sorted(resolveComparator(request))
                .map(booking -> toListItem(booking, privileges))
                .toList();

        int from = Math.min(page * size, filtered.size());
        int to = Math.min(from + size, filtered.size());
        List<CancellationListItemDto> content = filtered.subList(from, to);
        int totalPages = filtered.isEmpty() ? 0 : (int) Math.ceil((double) filtered.size() / size);

        return MessageResponseDTO.<CancellationFilterResultDto>builder()
                .success(true)
                .message("Cancellation requests filtered successfully")
                .data(CancellationFilterResultDto.builder()
                        .content(content)
                        .size(content.size())
                        .totalRecords(filtered.size())
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
    public MessageResponseDTO<MeetingBookingDto> view(CancellationViewRequest request) {
        MeetingBooking booking = findOwnCancellableBooking(request);
        return success("Cancellation request retrieved successfully", meetingBookingService.view(booking.getId()).getData());
    }

    @Override
    @Transactional
    public MessageResponseDTO<MeetingBookingDto> cancel(CancellationSubmitRequest request) {
        if (request == null || request.getId() == null) {
            throw new BadRequestException("Invalid cancellation request");
        }
        String username = requireUsername(request.getUsername());
        if (!StringUtils.hasText(request.getCancellationReason())) {
            throw new BadRequestException("Cancellation reason is required");
        }

        MeetingBooking booking = findOwnCancellableBooking(request.getId(), username);
        booking.setStatus(MeetingBookingStatus.CANCELLED);
        booking.setCancellationReason(request.getCancellationReason().trim());
        booking.setCancellationReasonCode(trimToNull(request.getCancellationReasonCode()));
        booking.setCancelledBy(username);
        booking.setCancelledDate(LocalDateTime.now());
        booking.setLastModifiedBy(username);
        meetingBookingRepository.save(booking);

        return success("Meeting booking cancelled successfully", meetingBookingService.view(booking.getId()).getData());
    }

    private MeetingBooking findOwnCancellableBooking(CancellationViewRequest request) {
        if (request == null || request.getId() == null) {
            throw new BadRequestException("Invalid cancellation view request");
        }
        return findOwnCancellableBooking(request.getId(), requireUsername(request.getUsername()));
    }

    private MeetingBooking findOwnCancellableBooking(Long id, String username) {
        Long companyId = resolveCompanyId();
        MeetingBooking booking = meetingBookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cancellable meeting booking not found"));
        if (!companyId.equals(booking.getCompanyId())
                || !username.equalsIgnoreCase(nullToEmpty(booking.getCreatedBy()))
                || !isCancellable(booking)) {
            throw new ResourceNotFoundException("Cancellable meeting booking not found");
        }
        return booking;
    }

    private boolean isCancellable(MeetingBooking booking) {
        return booking != null && CANCELLABLE_STATUSES.contains(booking.getStatus());
    }

    private boolean isVisibleInCancellationPage(MeetingBooking booking) {
        return booking != null && VISIBLE_CANCELLATION_STATUSES.contains(booking.getStatus());
    }


    private CancellationListItemDto toListItem(MeetingBooking booking, PageTaskPrivileges privileges) {
        return CancellationListItemDto.builder()
                .id(booking.getId())
                .requestNo(booking.getRequestNo())
                .meetingName(booking.getMeetingName())
                .meetingRoomId(booking.getMeetingRoomId())
                .meetingRoomName(booking.getMeetingRoomName())
                .meetingDate(booking.getMeetingDate())
                .startTime(booking.getStartTime())
                .endTime(booking.getEndTime())
                .status(booking.getStatus())
                .statusDescription(booking.getStatus() != null ? toTitleCase(booking.getStatus().name()) : null)
                .actions(CancellationActionsDto.builder()
                        .view(privileges.isView())
                        .cancel(privileges.isCancel() && isCancellable(booking))
                        .build())
                .build();
    }

    private boolean matches(MeetingBooking booking, CancellationFilterSearch search) {
        if (search == null) {
            return true;
        }
        return contains(booking.getRequestNo(), search.getRequestNo())
                && contains(booking.getMeetingName(), search.getMeetingName())
                && (search.getMeetingRoomId() == null || search.getMeetingRoomId().equals(booking.getMeetingRoomId()))
                && contains(booking.getMeetingRoomName(), search.getMeetingRoomName())
                && contains(booking.getStatus() != null ? booking.getStatus().name() : null, search.getStatus())
                && (search.getDateFrom() == null || !booking.getMeetingDate().isBefore(search.getDateFrom()))
                && (search.getDateTo() == null || !booking.getMeetingDate().isAfter(search.getDateTo()));
    }

    private Comparator<MeetingBooking> resolveComparator(CancellationFilterRequest request) {
        String column = request != null && StringUtils.hasText(request.getSortColumn())
                ? request.getSortColumn().trim()
                : "lastModifiedDate";
        boolean desc = request == null || !"ASC".equalsIgnoreCase(request.getSortDirection());
        Comparator<MeetingBooking> comparator = switch (column) {
            case "requestNo" -> Comparator.comparing(MeetingBooking::getRequestNo, Comparator.nullsLast(String::compareToIgnoreCase));
            case "meetingName" -> Comparator.comparing(MeetingBooking::getMeetingName, Comparator.nullsLast(String::compareToIgnoreCase));
            case "meetingRoomName" -> Comparator.comparing(MeetingBooking::getMeetingRoomName, Comparator.nullsLast(String::compareToIgnoreCase));
            case "meetingDate" -> Comparator.comparing(MeetingBooking::getMeetingDate, Comparator.nullsLast(LocalDate::compareTo));
            case "startTime" -> Comparator.comparing(MeetingBooking::getStartTime, Comparator.nullsLast(LocalTime::compareTo));
            case "status" -> Comparator.comparing(booking -> booking.getStatus() != null ? booking.getStatus().name() : null,
                    Comparator.nullsLast(String::compareToIgnoreCase));
            default -> Comparator.comparing(MeetingBooking::getLastModifiedDate, Comparator.nullsLast(LocalDateTime::compareTo));
        };
        return desc ? comparator.reversed() : comparator;
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

    private List<ReferenceOptionDto> cancellationReasons() {
        return List.of(
                ReferenceOptionDto.builder().code("MEETING_NO_LONGER_REQUIRED").description("Meeting No Longer Required").build(),
                ReferenceOptionDto.builder().code("SCHEDULE_CHANGED").description("Schedule Changed").build(),
                ReferenceOptionDto.builder().code("ROOM_NOT_REQUIRED").description("Room Not Required").build(),
                ReferenceOptionDto.builder().code("MEETING_POSTPONED").description("Meeting Postponed").build(),
                ReferenceOptionDto.builder().code("DUPLICATE_BOOKING").description("Duplicate Booking").build(),
                ReferenceOptionDto.builder().code("OTHER").description("Other").build()
        );
    }

    private ReferenceOptionDto toOption(Enum<?> value) {
        return ReferenceOptionDto.builder()
                .code(value.name())
                .description(toTitleCase(value.name()))
                .build();
    }

    private MessageResponseDTO<MeetingBookingDto> success(String message, MeetingBookingDto data) {
        return MessageResponseDTO.<MeetingBookingDto>builder()
                .success(true)
                .message(message)
                .data(data)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
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




