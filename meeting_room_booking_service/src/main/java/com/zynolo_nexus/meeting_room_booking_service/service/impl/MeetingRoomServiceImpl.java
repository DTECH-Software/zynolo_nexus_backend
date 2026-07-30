package com.zynolo_nexus.meeting_room_booking_service.service.impl;

import com.zynolo_nexus.meeting_room_booking_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingRoomActiveStatusRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingRoomCreateRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingRoomFilterRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingRoomFilterSearch;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingRoomReferenceDataRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingRoomUpdateRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingRoomDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingRoomFilterResultDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingRoomPrivilegesDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingRoomReferenceDataDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.ReferenceOptionDto;
import com.zynolo_nexus.meeting_room_booking_service.enums.RoomAvailabilityStatus;
import com.zynolo_nexus.meeting_room_booking_service.exception.BadRequestException;
import com.zynolo_nexus.meeting_room_booking_service.exception.ResourceNotFoundException;
import com.zynolo_nexus.meeting_room_booking_service.model.CompanyLookup;
import com.zynolo_nexus.meeting_room_booking_service.model.MeetingRoom;
import com.zynolo_nexus.meeting_room_booking_service.repository.CompanyLookupRepository;
import com.zynolo_nexus.meeting_room_booking_service.repository.MeetingRoomRepository;
import com.zynolo_nexus.meeting_room_booking_service.service.MeetingRoomService;
import com.zynolo_nexus.meeting_room_booking_service.context.CompanyContext;
import com.zynolo_nexus.meeting_room_booking_service.service.support.PagePrivilegeResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class MeetingRoomServiceImpl implements MeetingRoomService {

    private static final String PAGE_CODE = "MBM_SYSC_MERM";

    private final MeetingRoomRepository meetingRoomRepository;
    private final CompanyLookupRepository companyLookupRepository;
    private final PagePrivilegeResolver pagePrivilegeResolver;

    @Value("${app.default.company-id:1}")
    private Long defaultCompanyId;

    @Override
    @Transactional
    public MessageResponseDTO<MeetingRoomDto> create(MeetingRoomCreateRequest request) {
        if (request == null) {
            throw new BadRequestException("Invalid meeting room request");
        }
        Long companyId = resolveCompanyId();

        validateRequired(request.getRoomCode(), request.getRoomName(), request.getCapacity(), request.getAvailabilityStatus(), request.getActive());

        String roomCode = normalizeCode(request.getRoomCode());
        String roomName = request.getRoomName().trim();
        if (meetingRoomRepository.existsByCompanyIdAndRoomCodeIgnoreCase(companyId, roomCode)) {
            throw new BadRequestException("Room code already exists");
        }
        if (meetingRoomRepository.existsByCompanyIdAndRoomNameIgnoreCase(companyId, roomName)) {
            throw new BadRequestException("Room name already exists");
        }

        CompanyLookup company = resolveCompany(companyId);

        MeetingRoom room = MeetingRoom.builder()
                .companyId(companyId)
                .companyCode(resolveCompanyCode(company))
                .companyName(resolveCompanyName(company))
                .roomCode(roomCode)
                .roomName(roomName)
                .capacity(request.getCapacity())
                .location(trimToNull(request.getLocation()))
                .floor(trimToNull(request.getFloor()))
                .availabilityStatus(request.getAvailabilityStatus())
                .description(trimToNull(request.getDescription()))
                .active(request.getActive())
                .createdBy(trimToNull(request.getUsername()))
                .lastModifiedBy(trimToNull(request.getUsername()))
                .build();

        try {
            return success("Meeting room created successfully", toDto(meetingRoomRepository.saveAndFlush(room)));
        } catch (DataIntegrityViolationException ex) {
            throw duplicateRoomException(ex);
        }
    }

    @Override
    @Transactional
    public MessageResponseDTO<MeetingRoomDto> update(MeetingRoomUpdateRequest request) {
        if (request == null || request.getId() == null) {
            throw new BadRequestException("Invalid meeting room update request");
        }
        MeetingRoom room = findRoom(request.getId());
        Long companyId = room.getCompanyId();

        if (StringUtils.hasText(request.getRoomCode())) {
            String roomCode = normalizeCode(request.getRoomCode());
            if (!roomCode.equalsIgnoreCase(room.getRoomCode())
                    && meetingRoomRepository.existsByCompanyIdAndRoomCodeIgnoreCaseAndIdNot(companyId, roomCode, room.getId())) {
                throw new BadRequestException("Room code already exists");
            }
            room.setRoomCode(roomCode);
        }
        if (StringUtils.hasText(request.getRoomName())) {
            String roomName = request.getRoomName().trim();
            if (!roomName.equalsIgnoreCase(room.getRoomName())
                    && meetingRoomRepository.existsByCompanyIdAndRoomNameIgnoreCaseAndIdNot(companyId, roomName, room.getId())) {
                throw new BadRequestException("Room name already exists");
            }
            room.setRoomName(roomName);
        }
        if (request.getCapacity() != null) {
            if (request.getCapacity() <= 0) {
                throw new BadRequestException("Capacity must be greater than 0");
            }
            room.setCapacity(request.getCapacity());
        }
        if (request.getLocation() != null) {
            room.setLocation(trimToNull(request.getLocation()));
        }
        if (request.getFloor() != null) {
            room.setFloor(trimToNull(request.getFloor()));
        }
        if (request.getAvailabilityStatus() != null) {
            room.setAvailabilityStatus(request.getAvailabilityStatus());
        }
        if (request.getDescription() != null) {
            room.setDescription(trimToNull(request.getDescription()));
        }
        if (request.getActive() != null) {
            room.setActive(request.getActive());
        }
        if (StringUtils.hasText(request.getUsername())) {
            room.setLastModifiedBy(request.getUsername().trim());
        }

        try {
            return success("Meeting room updated successfully", toDto(meetingRoomRepository.saveAndFlush(room)));
        } catch (DataIntegrityViolationException ex) {
            throw duplicateRoomException(ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public MessageResponseDTO<MeetingRoomDto> view(Long id) {
        if (id == null) {
            throw new BadRequestException("Invalid meeting room view request");
        }
        return success("Meeting room retrieved successfully", toDto(findRoom(id)));
    }

    @Override
    @Transactional
    public MessageResponseDTO<MeetingRoomDto> updateActiveStatus(MeetingRoomActiveStatusRequest request) {
        if (request == null || request.getId() == null || request.getActive() == null) {
            throw new BadRequestException("Invalid meeting room active status request");
        }
        MeetingRoom room = findRoom(request.getId());
        room.setActive(request.getActive());
        if (StringUtils.hasText(request.getUsername())) {
            room.setLastModifiedBy(request.getUsername().trim());
        }
        String message = request.getActive() ? "Meeting room activated successfully" : "Meeting room deactivated successfully";
        return success(message, toDto(meetingRoomRepository.save(room)));
    }

    @Override
    @Transactional(readOnly = true)
    public MessageResponseDTO<MeetingRoomFilterResultDto> filterList(MeetingRoomFilterRequest request) {
        int page = request != null && request.getPage() != null && request.getPage() >= 0 ? request.getPage() : 0;
        int size = request != null && request.getSize() != null && request.getSize() > 0 ? request.getSize() : 10;
        MeetingRoomFilterSearch search = request != null ? request.getSearch() : null;
        Long companyId = resolveCompanyId();

        List<MeetingRoomDto> filtered = meetingRoomRepository.findAll().stream()
                .filter(room -> companyId.equals(room.getCompanyId()))
                .filter(room -> matches(room, search))
                .sorted(resolveComparator(request))
                .map(this::toDto)
                .toList();

        int from = Math.min(page * size, filtered.size());
        int to = Math.min(from + size, filtered.size());
        List<MeetingRoomDto> content = filtered.subList(from, to);
        int totalPages = filtered.isEmpty() ? 0 : (int) Math.ceil((double) filtered.size() / size);

        return MessageResponseDTO.<MeetingRoomFilterResultDto>builder()
                .success(true)
                .message("Meeting rooms filtered successfully")
                .data(MeetingRoomFilterResultDto.builder()
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
    public MessageResponseDTO<MeetingRoomReferenceDataDto> referenceData(MeetingRoomReferenceDataRequest request) {
        List<ReferenceOptionDto> statuses = List.of(RoomAvailabilityStatus.values()).stream()
                .map(status -> ReferenceOptionDto.builder()
                        .code(status.name())
                        .description(toDescription(status.name()))
                        .build())
                .toList();
        Long companyId = resolveCompanyId();
        CompanyLookup company = resolveCompany(companyId);
        var pagePrivileges = pagePrivilegeResolver.resolve(request != null ? request.getUsername() : null, PAGE_CODE);

        return MessageResponseDTO.<MeetingRoomReferenceDataDto>builder()
                .success(true)
                .message("Meeting room reference data loaded successfully")
                .data(MeetingRoomReferenceDataDto.builder()
                        .companyId(companyId)
                        .companyCode(resolveCompanyCode(company))
                        .companyName(resolveCompanyName(company))
                        .availabilityStatuses(statuses)
                        .maxRoomCount(null)
                        .currentRoomCount(meetingRoomRepository.countByCompanyId(companyId))
                        .privileges(MeetingRoomPrivilegesDto.builder()
                                .add(pagePrivileges.isAdd())
                                .update(pagePrivileges.isUpdate())
                                .view(pagePrivileges.isView())
                                .search(pagePrivileges.isSearch())
                                .activate(pagePrivileges.isActivate())
                                .deactivate(pagePrivileges.isDeactivate())
                                .build())
                        .build())
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    private MeetingRoom findRoom(Long id) {
        Long companyId = resolveCompanyId();
        MeetingRoom room = meetingRoomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Meeting room not found"));
        if (!companyId.equals(room.getCompanyId())) {
            throw new ResourceNotFoundException("Meeting room not found");
        }
        return room;
    }

    private void validateRequired(
            String roomCode,
            String roomName,
            Integer capacity,
            RoomAvailabilityStatus availabilityStatus,
            Boolean active) {
        if (!StringUtils.hasText(roomCode) || !StringUtils.hasText(roomName)
                || capacity == null || availabilityStatus == null || active == null) {
            throw new BadRequestException("Invalid meeting room request");
        }
        if (capacity <= 0) {
            throw new BadRequestException("Capacity must be greater than 0");
        }
    }

    private BadRequestException duplicateRoomException(DataIntegrityViolationException ex) {
        String message = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage();
        String normalized = message != null ? message.toLowerCase(Locale.ENGLISH) : "";
        if (normalized.contains("idx_meeting_rooms_company_code") || normalized.contains("room_code")) {
            return new BadRequestException("Room code already exists");
        }
        if (normalized.contains("idx_meeting_rooms_company_name") || normalized.contains("room_name")) {
            return new BadRequestException("Room name already exists");
        }
        return new BadRequestException("Room code or room name already exists");
    }

    private boolean matches(MeetingRoom room, MeetingRoomFilterSearch search) {
        if (search == null) {
            return true;
        }
        return contains(room.getRoomCode(), search.getRoomCode())
                && contains(room.getRoomName(), search.getRoomName())
                && contains(room.getLocation(), search.getLocation())
                && contains(room.getFloor(), search.getFloor())
                && matchesAvailabilityStatus(room.getAvailabilityStatus(), search.getAvailabilityStatus())
                && matchesActive(search.getActive(), search.getStatus(), room.getActive());
    }

    private boolean matchesAvailabilityStatus(RoomAvailabilityStatus actualStatus, String expectedStatus) {
        if (!StringUtils.hasText(expectedStatus)) {
            return true;
        }
        if (actualStatus == null) {
            return false;
        }
        String normalized = expectedStatus.trim()
                .toUpperCase(Locale.ENGLISH)
                .replace('-', '_')
                .replace(' ', '_');
        return actualStatus.name().equals(normalized);
    }


    private boolean matchesActive(Boolean activeFilter, String statusFilter, Boolean actualActive) {
        if (StringUtils.hasText(statusFilter)) {
            String normalized = statusFilter.trim().toUpperCase(Locale.ENGLISH);
            if ("ACTIVE".equals(normalized)) {
                return Boolean.TRUE.equals(actualActive);
            }
            if ("INACTIVE".equals(normalized) || "DEACTIVE".equals(normalized) || "DEACTIVATED".equals(normalized)) {
                return !Boolean.TRUE.equals(actualActive);
            }
        }
        if (Boolean.FALSE.equals(activeFilter)) {
            return !Boolean.TRUE.equals(actualActive);
        }
        return true;
    }
    private Comparator<MeetingRoom> resolveComparator(MeetingRoomFilterRequest request) {
        String column = request != null && StringUtils.hasText(request.getSortColumn())
                ? request.getSortColumn().trim()
                : "lastModifiedDate";
        boolean desc = request == null || !"ASC".equalsIgnoreCase(request.getSortDirection());
        Comparator<MeetingRoom> comparator = switch (column) {
            case "roomCode" -> Comparator.comparing(MeetingRoom::getRoomCode, Comparator.nullsLast(String::compareToIgnoreCase));
            case "roomName" -> Comparator.comparing(MeetingRoom::getRoomName, Comparator.nullsLast(String::compareToIgnoreCase));
            case "capacity" -> Comparator.comparing(MeetingRoom::getCapacity, Comparator.nullsLast(Integer::compareTo));
            case "availabilityStatus" -> Comparator.comparing(room -> room.getAvailabilityStatus() != null ? room.getAvailabilityStatus().name() : null,
                    Comparator.nullsLast(String::compareToIgnoreCase));
            default -> Comparator.comparing(MeetingRoom::getLastModifiedDate, Comparator.nullsLast(LocalDateTime::compareTo));
        };
        return desc ? comparator.reversed() : comparator;
    }

    private MeetingRoomDto toDto(MeetingRoom room) {
        boolean bookable = Boolean.TRUE.equals(room.getActive()) && room.getAvailabilityStatus() == RoomAvailabilityStatus.AVAILABLE;
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
                .availabilityStatusDescription(room.getAvailabilityStatus() != null ? toDescription(room.getAvailabilityStatus().name()) : null)
                .description(room.getDescription())
                .active(room.getActive())
                .activeStatusDescription(Boolean.TRUE.equals(room.getActive()) ? "Active" : "Inactive")
                .bookable(bookable)
                .createdDate(room.getCreatedDate())
                .lastModifiedDate(room.getLastModifiedDate())
                .createdBy(room.getCreatedBy())
                .lastModifiedBy(room.getLastModifiedBy())
                .build();
    }

    private MessageResponseDTO<MeetingRoomDto> success(String message, MeetingRoomDto data) {
        return MessageResponseDTO.<MeetingRoomDto>builder()
                .success(true)
                .message(message)
                .data(data)
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    private String normalizeCode(String value) {
        return value.trim().toUpperCase(Locale.ENGLISH);
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

    private String toDescription(String value) {
        String lower = value.toLowerCase(Locale.ENGLISH).replace('_', ' ');
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }
}



