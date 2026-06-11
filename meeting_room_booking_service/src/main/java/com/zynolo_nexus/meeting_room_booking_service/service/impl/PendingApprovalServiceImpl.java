package com.zynolo_nexus.meeting_room_booking_service.service.impl;

import com.zynolo_nexus.meeting_room_booking_service.context.CompanyContext;
import com.zynolo_nexus.meeting_room_booking_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingBookingUpdateRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.PendingApprovalActionRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.PendingApprovalFilterRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.PendingApprovalFilterSearch;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.PendingApprovalReferenceDataRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingBookingDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingRoomDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.PendingApprovalActionsDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.PendingApprovalFilterResultDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.PendingApprovalListItemDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.PendingApprovalPrivilegesDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.PendingApprovalReferenceDataDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.ReferenceOptionDto;
import com.zynolo_nexus.meeting_room_booking_service.enums.MeetingBookingStatus;
import com.zynolo_nexus.meeting_room_booking_service.enums.MeetingBookingType;
import com.zynolo_nexus.meeting_room_booking_service.enums.RoomAvailabilityStatus;
import com.zynolo_nexus.meeting_room_booking_service.exception.BadRequestException;
import com.zynolo_nexus.meeting_room_booking_service.exception.ResourceNotFoundException;
import com.zynolo_nexus.meeting_room_booking_service.model.CompanyLookup;
import com.zynolo_nexus.meeting_room_booking_service.model.MeetingBooking;
import com.zynolo_nexus.meeting_room_booking_service.model.MeetingRoom;
import com.zynolo_nexus.meeting_room_booking_service.repository.CompanyLookupRepository;
import com.zynolo_nexus.meeting_room_booking_service.repository.MeetingBookingRepository;
import com.zynolo_nexus.meeting_room_booking_service.repository.MeetingRoomRepository;
import com.zynolo_nexus.meeting_room_booking_service.service.MeetingBookingService;
import com.zynolo_nexus.meeting_room_booking_service.service.PendingApprovalService;
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
public class PendingApprovalServiceImpl implements PendingApprovalService {

    private static final String PAGE_CODE = "MBM_TRNS_PENA";

    private final MeetingBookingRepository meetingBookingRepository;
    private final MeetingRoomRepository meetingRoomRepository;
    private final CompanyLookupRepository companyLookupRepository;
    private final PagePrivilegeResolver pagePrivilegeResolver;
    private final MeetingBookingService meetingBookingService;

    @Value("${app.default.company-id:1}")
    private Long defaultCompanyId;

    @Override
    @Transactional(readOnly = true)
    public MessageResponseDTO<PendingApprovalReferenceDataDto> referenceData(PendingApprovalReferenceDataRequest request) {
        Long companyId = resolveCompanyId();
        CompanyLookup company = resolveCompany(companyId);
        PageTaskPrivileges privileges = pagePrivilegeResolver.resolve(request != null ? request.getUsername() : null, PAGE_CODE);

        return MessageResponseDTO.<PendingApprovalReferenceDataDto>builder()
                .success(true)
                .message("Pending approvals reference data loaded successfully")
                .data(PendingApprovalReferenceDataDto.builder()
                        .companyId(companyId)
                        .companyCode(resolveCompanyCode(company))
                        .companyName(resolveCompanyName(company))
                        .meetingTypes(toOptions(MeetingBookingType.values()))
                        .statuses(List.of(toOption(MeetingBookingStatus.PENDING_APPROVAL)))
                        .meetingRooms(meetingRoomRepository.findAll().stream()
                                .filter(room -> companyId.equals(room.getCompanyId()))
                                .sorted(Comparator.comparing(MeetingRoom::getRoomName, Comparator.nullsLast(String::compareToIgnoreCase)))
                                .map(this::toRoomDto)
                                .toList())
                        .privileges(PendingApprovalPrivilegesDto.builder()
                                .view(privileges.isView())
                                .search(privileges.isSearch())
                                .approve(privileges.isApprove())
                                .reject(privileges.isReject())
                                .edit(privileges.isUpdate())
                                .build())
                        .build())
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public MessageResponseDTO<PendingApprovalFilterResultDto> filterList(PendingApprovalFilterRequest request) {
        int page = request != null && request.getPage() != null && request.getPage() >= 0 ? request.getPage() : 0;
        int size = request != null && request.getSize() != null && request.getSize() > 0 ? request.getSize() : 10;
        PendingApprovalFilterSearch search = request != null ? request.getSearch() : null;
        Long companyId = resolveCompanyId();
        PageTaskPrivileges privileges = pagePrivilegeResolver.resolve(request != null ? request.getUsername() : null, PAGE_CODE);

        List<PendingApprovalListItemDto> filtered = meetingBookingRepository.findAll().stream()
                .filter(booking -> companyId.equals(booking.getCompanyId()))
                .filter(booking -> booking.getStatus() == MeetingBookingStatus.PENDING_APPROVAL)
                .filter(booking -> matches(booking, search))
                .sorted(resolveComparator(request))
                .map(booking -> toListItem(booking, privileges))
                .toList();

        int from = Math.min(page * size, filtered.size());
        int to = Math.min(from + size, filtered.size());
        List<PendingApprovalListItemDto> content = filtered.subList(from, to);
        int totalPages = filtered.isEmpty() ? 0 : (int) Math.ceil((double) filtered.size() / size);

        return MessageResponseDTO.<PendingApprovalFilterResultDto>builder()
                .success(true)
                .message("Pending approvals filtered successfully")
                .data(PendingApprovalFilterResultDto.builder()
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
    public MessageResponseDTO<MeetingBookingDto> view(PendingApprovalActionRequest request) {
        MeetingBooking booking = findPendingBooking(request);
        return success("Pending approval request retrieved successfully", meetingBookingService.view(booking.getId()).getData());
    }

    @Override
    @Transactional
    public MessageResponseDTO<MeetingBookingDto> approve(PendingApprovalActionRequest request) {
        MeetingBooking booking = findPendingBooking(request);
        String username = requireUsername(request.getUsername());

        booking.setStatus(MeetingBookingStatus.APPROVED);
        booking.setApprovedBy(username);
        booking.setApprovedDate(LocalDateTime.now());
        booking.setApprovalRemark(trimToNull(request.getApprovalRemark()));
        booking.setRejectedBy(null);
        booking.setRejectedDate(null);
        booking.setRejectionRemark(null);
        booking.setLastModifiedBy(username);
        meetingBookingRepository.save(booking);

        return success("Meeting booking approved successfully", meetingBookingService.view(booking.getId()).getData());
    }

    @Override
    @Transactional
    public MessageResponseDTO<MeetingBookingDto> reject(PendingApprovalActionRequest request) {
        MeetingBooking booking = findPendingBooking(request);
        String username = requireUsername(request.getUsername());
        if (!StringUtils.hasText(request.getRejectionRemark())) {
            throw new BadRequestException("Rejection remark is required");
        }

        booking.setStatus(MeetingBookingStatus.REJECTED);
        booking.setRejectedBy(username);
        booking.setRejectedDate(LocalDateTime.now());
        booking.setRejectionRemark(request.getRejectionRemark().trim());
        booking.setApprovedBy(null);
        booking.setApprovedDate(null);
        booking.setApprovalRemark(null);
        booking.setLastModifiedBy(username);
        meetingBookingRepository.save(booking);

        return success("Meeting booking rejected successfully", meetingBookingService.view(booking.getId()).getData());
    }

    @Override
    @Transactional
    public MessageResponseDTO<MeetingBookingDto> edit(MeetingBookingUpdateRequest request) {
        if (request == null || request.getId() == null) {
            throw new BadRequestException("Invalid pending approval edit request");
        }
        requireUsername(request.getUsername());
        findPendingBooking(request.getId());
        return meetingBookingService.editPendingApproval(request);
    }

    private MeetingBooking findPendingBooking(PendingApprovalActionRequest request) {
        if (request == null || request.getId() == null) {
            throw new BadRequestException("Invalid pending approval action request");
        }
        return findPendingBooking(request.getId());
    }

    private MeetingBooking findPendingBooking(Long id) {
        Long companyId = resolveCompanyId();
        MeetingBooking booking = meetingBookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pending approval request not found"));
        if (!companyId.equals(booking.getCompanyId()) || booking.getStatus() != MeetingBookingStatus.PENDING_APPROVAL) {
            throw new ResourceNotFoundException("Pending approval request not found");
        }
        return booking;
    }

    private PendingApprovalListItemDto toListItem(MeetingBooking booking, PageTaskPrivileges privileges) {
        return PendingApprovalListItemDto.builder()
                .id(booking.getId())
                .requestNo(booking.getRequestNo())
                .requestedBy(booking.getCreatedBy())
                .submittedBy(booking.getSubmittedBy())
                .submittedDate(booking.getSubmittedDate())
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
                .actions(PendingApprovalActionsDto.builder()
                        .view(privileges.isView())
                        .approve(privileges.isApprove())
                        .reject(privileges.isReject())
                        .edit(privileges.isUpdate())
                        .build())
                .build();
    }

    private boolean matches(MeetingBooking booking, PendingApprovalFilterSearch search) {
        if (search == null) {
            return true;
        }
        return contains(booking.getRequestNo(), search.getRequestNo())
                && contains(booking.getMeetingName(), search.getMeetingName())
                && contains(booking.getCreatedBy(), search.getRequestedBy())
                && (search.getMeetingRoomId() == null || search.getMeetingRoomId().equals(booking.getMeetingRoomId()))
                && contains(booking.getMeetingRoomName(), search.getMeetingRoomName())
                && contains(booking.getMeetingType() != null ? booking.getMeetingType().name() : null, search.getMeetingType())
                && contains(booking.getStatus() != null ? booking.getStatus().name() : null, search.getStatus())
                && (search.getDateFrom() == null || !booking.getMeetingDate().isBefore(search.getDateFrom()))
                && (search.getDateTo() == null || !booking.getMeetingDate().isAfter(search.getDateTo()));
    }

    private Comparator<MeetingBooking> resolveComparator(PendingApprovalFilterRequest request) {
        String column = request != null && StringUtils.hasText(request.getSortColumn())
                ? request.getSortColumn().trim()
                : "lastModifiedDate";
        boolean desc = request == null || !"ASC".equalsIgnoreCase(request.getSortDirection());
        Comparator<MeetingBooking> comparator = switch (column) {
            case "requestNo" -> Comparator.comparing(MeetingBooking::getRequestNo, Comparator.nullsLast(String::compareToIgnoreCase));
            case "requestedBy" -> Comparator.comparing(MeetingBooking::getCreatedBy, Comparator.nullsLast(String::compareToIgnoreCase));
            case "meetingName" -> Comparator.comparing(MeetingBooking::getMeetingName, Comparator.nullsLast(String::compareToIgnoreCase));
            case "meetingRoomName" -> Comparator.comparing(MeetingBooking::getMeetingRoomName, Comparator.nullsLast(String::compareToIgnoreCase));
            case "meetingDate" -> Comparator.comparing(MeetingBooking::getMeetingDate, Comparator.nullsLast(LocalDate::compareTo));
            case "startTime" -> Comparator.comparing(MeetingBooking::getStartTime, Comparator.nullsLast(LocalTime::compareTo));
            case "submittedDate" -> Comparator.comparing(MeetingBooking::getSubmittedDate, Comparator.nullsLast(LocalDateTime::compareTo));
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
