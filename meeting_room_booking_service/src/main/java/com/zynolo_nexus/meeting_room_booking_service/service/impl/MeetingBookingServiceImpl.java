package com.zynolo_nexus.meeting_room_booking_service.service.impl;

import com.zynolo_nexus.meeting_room_booking_service.context.CompanyContext;
import com.zynolo_nexus.meeting_room_booking_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingBookingBeverageRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingBookingFilterRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingBookingFilterSearch;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingBookingIdRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingBookingParticipantRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingBookingReferenceDataRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingBookingRefreshmentRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingBookingSaveDraftRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingBookingSupportServiceRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingBookingUpdateRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingBeverageDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingBookingBeverageDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingBookingDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingBookingFilterResultDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingBookingParticipantDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingBookingPrivilegesDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingBookingReferenceDataDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingBookingRefreshmentDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingBookingSupportServiceDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingRefreshmentDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingRoomDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingSupportServiceDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingVendorDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.ReferenceOptionDto;
import com.zynolo_nexus.meeting_room_booking_service.enums.MeetingBookingStatus;
import com.zynolo_nexus.meeting_room_booking_service.enums.MeetingBookingType;
import com.zynolo_nexus.meeting_room_booking_service.enums.MeetingParticipantType;
import com.zynolo_nexus.meeting_room_booking_service.enums.RoomAvailabilityStatus;
import com.zynolo_nexus.meeting_room_booking_service.exception.BadRequestException;
import com.zynolo_nexus.meeting_room_booking_service.exception.ResourceNotFoundException;
import com.zynolo_nexus.meeting_room_booking_service.model.CompanyLookup;
import com.zynolo_nexus.meeting_room_booking_service.model.MeetingBeverage;
import com.zynolo_nexus.meeting_room_booking_service.model.MeetingBooking;
import com.zynolo_nexus.meeting_room_booking_service.model.MeetingBookingBeverage;
import com.zynolo_nexus.meeting_room_booking_service.model.MeetingBookingParticipant;
import com.zynolo_nexus.meeting_room_booking_service.model.MeetingBookingRefreshment;
import com.zynolo_nexus.meeting_room_booking_service.model.MeetingBookingSupportService;
import com.zynolo_nexus.meeting_room_booking_service.model.MeetingRefreshment;
import com.zynolo_nexus.meeting_room_booking_service.model.MeetingRoom;
import com.zynolo_nexus.meeting_room_booking_service.model.MeetingSupportService;
import com.zynolo_nexus.meeting_room_booking_service.model.MeetingVendor;
import com.zynolo_nexus.meeting_room_booking_service.repository.CompanyLookupRepository;
import com.zynolo_nexus.meeting_room_booking_service.repository.MeetingBeverageRepository;
import com.zynolo_nexus.meeting_room_booking_service.repository.MeetingBookingRepository;
import com.zynolo_nexus.meeting_room_booking_service.repository.MeetingRefreshmentRepository;
import com.zynolo_nexus.meeting_room_booking_service.repository.MeetingRoomRepository;
import com.zynolo_nexus.meeting_room_booking_service.repository.MeetingSupportServiceRepository;
import com.zynolo_nexus.meeting_room_booking_service.repository.MeetingVendorRepository;
import com.zynolo_nexus.meeting_room_booking_service.service.MeetingBookingService;
import com.zynolo_nexus.meeting_room_booking_service.service.support.PagePrivilegeResolver;
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
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class MeetingBookingServiceImpl implements MeetingBookingService {

    private static final String PAGE_CODE = "MBM_TRNS_CREB";
    private static final BigDecimal EXTERNAL_ROOM_RATE = new BigDecimal("3600.00");

    private final MeetingBookingRepository meetingBookingRepository;
    private final MeetingRoomRepository meetingRoomRepository;
    private final MeetingVendorRepository meetingVendorRepository;
    private final MeetingRefreshmentRepository meetingRefreshmentRepository;
    private final MeetingBeverageRepository meetingBeverageRepository;
    private final MeetingSupportServiceRepository meetingSupportServiceRepository;
    private final CompanyLookupRepository companyLookupRepository;
    private final PagePrivilegeResolver pagePrivilegeResolver;

    @Value("${app.default.company-id:1}")
    private Long defaultCompanyId;

    @Override
    @Transactional(readOnly = true)
    public MessageResponseDTO<MeetingBookingReferenceDataDto> referenceData(MeetingBookingReferenceDataRequest request) {
        Long companyId = resolveCompanyId();
        CompanyLookup company = resolveCompany(companyId);
        var pagePrivileges = pagePrivilegeResolver.resolve(request != null ? request.getUsername() : null, PAGE_CODE);

        return MessageResponseDTO.<MeetingBookingReferenceDataDto>builder()
                .success(true)
                .message("Meeting booking reference data loaded successfully")
                .data(MeetingBookingReferenceDataDto.builder()
                        .companyId(companyId)
                        .companyCode(resolveCompanyCode(company))
                        .companyName(resolveCompanyName(company))
                        .meetingTypes(toOptions(MeetingBookingType.values()))
                        .statuses(toOptions(MeetingBookingStatus.values()))
                        .externalMeetingRoomHourlyRate(EXTERNAL_ROOM_RATE)
                        .meetingRooms(meetingRoomRepository.findAll().stream()
                                .filter(room -> companyId.equals(room.getCompanyId()))
                                .filter(room -> Boolean.TRUE.equals(room.getActive()))
                                .filter(room -> room.getAvailabilityStatus() == RoomAvailabilityStatus.AVAILABLE)
                                .sorted(Comparator.comparing(MeetingRoom::getRoomName, Comparator.nullsLast(String::compareToIgnoreCase)))
                                .map(this::toRoomDto)
                                .toList())
                        .activeVendors(meetingVendorRepository.findAll().stream()
                                .filter(vendor -> companyId.equals(vendor.getCompanyId()))
                                .filter(vendor -> Boolean.TRUE.equals(vendor.getActive()))
                                .sorted(Comparator.comparing(MeetingVendor::getVendorName, Comparator.nullsLast(String::compareToIgnoreCase)))
                                .map(this::toVendorDto)
                                .toList())
                        .refreshments(meetingRefreshmentRepository.findAll().stream()
                                .filter(refreshment -> companyId.equals(refreshment.getCompanyId()))
                                .filter(refreshment -> Boolean.TRUE.equals(refreshment.getActive()))
                                .sorted(Comparator.comparing(MeetingRefreshment::getItemName, Comparator.nullsLast(String::compareToIgnoreCase)))
                                .map(this::toRefreshmentMasterDto)
                                .toList())
                        .beverages(meetingBeverageRepository.findAll().stream()
                                .filter(beverage -> companyId.equals(beverage.getCompanyId()))
                                .filter(beverage -> Boolean.TRUE.equals(beverage.getActive()))
                                .sorted(Comparator.comparing(MeetingBeverage::getBeverageName, Comparator.nullsLast(String::compareToIgnoreCase)))
                                .map(this::toBeverageMasterDto)
                                .toList())
                        .supportServices(meetingSupportServiceRepository.findAll().stream()
                                .filter(service -> companyId.equals(service.getCompanyId()))
                                .filter(service -> Boolean.TRUE.equals(service.getActive()))
                                .sorted(Comparator.comparing(MeetingSupportService::getServiceName, Comparator.nullsLast(String::compareToIgnoreCase)))
                                .map(this::toSupportMasterDto)
                                .toList())
                        .privileges(MeetingBookingPrivilegesDto.builder()
                                .saveDraft(pagePrivileges.isSaveDraft() || pagePrivileges.isAdd())
                                .update(pagePrivileges.isUpdate())
                                .view(pagePrivileges.isView())
                                .search(pagePrivileges.isSearch())
                                .submit(pagePrivileges.isSubmit())
                                .cancel(pagePrivileges.isCancel())
                                .build())
                        .build())
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    @Transactional
    public MessageResponseDTO<MeetingBookingDto> saveDraft(MeetingBookingSaveDraftRequest request) {
        if (request == null) {
            throw new BadRequestException("Invalid booking request");
        }
        Long companyId = resolveCompanyId();
        CompanyLookup company = resolveCompany(companyId);

        MeetingBooking booking = MeetingBooking.builder()
                .companyId(companyId)
                .companyCode(resolveCompanyCode(company))
                .companyName(resolveCompanyName(company))
                .requestNo(generateRequestNo(companyId))
                .status(MeetingBookingStatus.DRAFT)
                .createdBy(trimToNull(request.getUsername()))
                .lastModifiedBy(trimToNull(request.getUsername()))
                .build();

        applyHeader(
                booking,
                request.getMeetingName(),
                request.getMeetingType(),
                request.getMeetingRoomId(),
                request.getMeetingDate(),
                request.getStartTime(),
                request.getEndTime(),
                request.getNumberOfAttendees(),
                request.getPurposeRemarks(),
                false
        );
        replaceDetails(booking, request.getRefreshments(), request.getBeverages(), request.getSupportServices(), request.getParticipants());
        calculateSummary(booking);

        return success("Meeting booking draft saved successfully", toDto(meetingBookingRepository.save(booking)));
    }

    @Override
    @Transactional
    public MessageResponseDTO<MeetingBookingDto> update(MeetingBookingUpdateRequest request) {
        if (request == null || request.getId() == null) {
            throw new BadRequestException("Invalid booking update request");
        }
        MeetingBooking booking = findBooking(request.getId());
        if (booking.getStatus() != MeetingBookingStatus.DRAFT) {
            throw new BadRequestException("Booking cannot be edited after submission");
        }

        applyHeader(
                booking,
                request.getMeetingName(),
                request.getMeetingType(),
                request.getMeetingRoomId(),
                request.getMeetingDate(),
                request.getStartTime(),
                request.getEndTime(),
                request.getNumberOfAttendees(),
                request.getPurposeRemarks(),
                true
        );
        if (request.getRefreshments() != null || request.getBeverages() != null
                || request.getSupportServices() != null || request.getParticipants() != null) {
            replaceDetails(
                    booking,
                    request.getRefreshments() != null ? request.getRefreshments() : toRefreshmentRequests(booking),
                    request.getBeverages() != null ? request.getBeverages() : toBeverageRequests(booking),
                    request.getSupportServices() != null ? request.getSupportServices() : toSupportRequests(booking),
                    request.getParticipants() != null ? request.getParticipants() : toParticipantRequests(booking)
            );
        }
        calculateSummary(booking);
        if (StringUtils.hasText(request.getUsername())) {
            booking.setLastModifiedBy(request.getUsername().trim());
        }

        return success("Meeting booking updated successfully", toDto(meetingBookingRepository.save(booking)));
    }

    @Override
    @Transactional
    public MessageResponseDTO<MeetingBookingDto> editPendingApproval(MeetingBookingUpdateRequest request) {
        if (request == null || request.getId() == null) {
            throw new BadRequestException("Invalid pending approval edit request");
        }
        MeetingBooking booking = findBooking(request.getId());
        if (booking.getStatus() != MeetingBookingStatus.PENDING_APPROVAL) {
            throw new BadRequestException("Only pending approval bookings can be edited by approver");
        }

        applyHeader(
                booking,
                request.getMeetingName(),
                request.getMeetingType(),
                request.getMeetingRoomId(),
                request.getMeetingDate(),
                request.getStartTime(),
                request.getEndTime(),
                request.getNumberOfAttendees(),
                request.getPurposeRemarks(),
                true
        );
        if (request.getRefreshments() != null || request.getBeverages() != null
                || request.getSupportServices() != null || request.getParticipants() != null) {
            replaceDetails(
                    booking,
                    request.getRefreshments() != null ? request.getRefreshments() : toRefreshmentRequests(booking),
                    request.getBeverages() != null ? request.getBeverages() : toBeverageRequests(booking),
                    request.getSupportServices() != null ? request.getSupportServices() : toSupportRequests(booking),
                    request.getParticipants() != null ? request.getParticipants() : toParticipantRequests(booking)
            );
        }
        calculateSummary(booking);
        if (StringUtils.hasText(request.getUsername())) {
            booking.setLastModifiedBy(request.getUsername().trim());
        }

        return success("Pending approval meeting request edited successfully", toDto(meetingBookingRepository.save(booking)));
    }

    @Override
    @Transactional
    public MessageResponseDTO<MeetingBookingDto> submit(MeetingBookingIdRequest request) {
        if (request == null || request.getId() == null) {
            throw new BadRequestException("Invalid booking submit request");
        }
        MeetingBooking booking = findBooking(request.getId());
        if (booking.getStatus() != MeetingBookingStatus.DRAFT) {
            throw new BadRequestException("Only draft bookings can be submitted");
        }
        validateHeader(
                booking.getMeetingName(),
                booking.getMeetingType(),
                booking.getMeetingRoomId(),
                booking.getMeetingDate(),
                booking.getStartTime(),
                booking.getEndTime(),
                booking.getNumberOfAttendees(),
                booking.getId()
        );
        validateRequiredDetails(booking);
        booking.setStatus(MeetingBookingStatus.PENDING_APPROVAL);
        booking.setSubmittedBy(trimToNull(request.getUsername()));
        booking.setSubmittedDate(LocalDateTime.now());
        if (StringUtils.hasText(request.getUsername())) {
            booking.setLastModifiedBy(request.getUsername().trim());
        }
        return success("Meeting booking submitted for approval successfully", toDto(meetingBookingRepository.save(booking)));
    }

    @Override
    @Transactional
    public MessageResponseDTO<MeetingBookingDto> cancel(MeetingBookingIdRequest request) {
        if (request == null || request.getId() == null) {
            throw new BadRequestException("Invalid booking cancel request");
        }
        MeetingBooking booking = findBooking(request.getId());
        if (booking.getStatus() != MeetingBookingStatus.DRAFT && booking.getStatus() != MeetingBookingStatus.PENDING_APPROVAL) {
            throw new BadRequestException("Only draft or pending approval bookings can be cancelled");
        }
        booking.setStatus(MeetingBookingStatus.CANCELLED);
        if (StringUtils.hasText(request.getUsername())) {
            booking.setLastModifiedBy(request.getUsername().trim());
        }
        return success("Meeting booking cancelled successfully", toDto(meetingBookingRepository.save(booking)));
    }

    @Override
    @Transactional(readOnly = true)
    public MessageResponseDTO<MeetingBookingDto> view(Long id) {
        if (id == null) {
            throw new BadRequestException("Invalid booking view request");
        }
        return success("Meeting booking retrieved successfully", toDto(findBooking(id)));
    }

    @Override
    @Transactional(readOnly = true)
    public MessageResponseDTO<MeetingBookingFilterResultDto> filterList(MeetingBookingFilterRequest request) {
        int page = request != null && request.getPage() != null && request.getPage() >= 0 ? request.getPage() : 0;
        int size = request != null && request.getSize() != null && request.getSize() > 0 ? request.getSize() : 10;
        MeetingBookingFilterSearch search = request != null ? request.getSearch() : null;
        Long companyId = resolveCompanyId();

        List<MeetingBookingDto> filtered = meetingBookingRepository.findAll().stream()
                .filter(booking -> companyId.equals(booking.getCompanyId()))
                .filter(booking -> matches(booking, search))
                .sorted(resolveComparator(request))
                .map(this::toDto)
                .toList();

        int from = Math.min(page * size, filtered.size());
        int to = Math.min(from + size, filtered.size());
        List<MeetingBookingDto> content = filtered.subList(from, to);
        int totalPages = filtered.isEmpty() ? 0 : (int) Math.ceil((double) filtered.size() / size);

        return MessageResponseDTO.<MeetingBookingFilterResultDto>builder()
                .success(true)
                .message("Meeting bookings filtered successfully")
                .data(MeetingBookingFilterResultDto.builder()
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

    private void applyHeader(MeetingBooking booking,
                             String meetingName,
                             MeetingBookingType meetingType,
                             Long meetingRoomId,
                             LocalDate meetingDate,
                             LocalTime startTime,
                             LocalTime endTime,
                             Integer numberOfAttendees,
                             String purposeRemarks,
                             boolean partial) {
        String nextMeetingName = partial && !StringUtils.hasText(meetingName) ? booking.getMeetingName() : trimToNull(meetingName);
        MeetingBookingType nextMeetingType = partial && meetingType == null ? booking.getMeetingType() : meetingType;
        Long nextRoomId = partial && meetingRoomId == null ? booking.getMeetingRoomId() : meetingRoomId;
        LocalDate nextDate = partial && meetingDate == null ? booking.getMeetingDate() : meetingDate;
        LocalTime nextStart = partial && startTime == null ? booking.getStartTime() : startTime;
        LocalTime nextEnd = partial && endTime == null ? booking.getEndTime() : endTime;
        Integer nextAttendees = partial && numberOfAttendees == null ? booking.getNumberOfAttendees() : numberOfAttendees;

        MeetingRoom room = validateHeader(nextMeetingName, nextMeetingType, nextRoomId, nextDate, nextStart, nextEnd, nextAttendees, booking.getId());
        booking.setMeetingName(nextMeetingName);
        booking.setMeetingType(nextMeetingType);
        booking.setMeetingRoomId(room.getId());
        booking.setMeetingRoomCode(room.getRoomCode());
        booking.setMeetingRoomName(room.getRoomName());
        booking.setRoomCapacity(room.getCapacity());
        booking.setMeetingDate(nextDate);
        booking.setStartTime(nextStart);
        booking.setEndTime(nextEnd);
        booking.setNumberOfAttendees(nextAttendees);
        if (!partial || purposeRemarks != null) {
            booking.setPurposeRemarks(trimToNull(purposeRemarks));
        }
    }

    private MeetingRoom validateHeader(String meetingName,
                                       MeetingBookingType meetingType,
                                       Long meetingRoomId,
                                       LocalDate meetingDate,
                                       LocalTime startTime,
                                       LocalTime endTime,
                                       Integer numberOfAttendees,
                                       Long excludeBookingId) {
        if (!StringUtils.hasText(meetingName) || meetingType == null || meetingRoomId == null
                || meetingDate == null || startTime == null || endTime == null || numberOfAttendees == null) {
            throw new BadRequestException("Invalid booking request");
        }
        if (!startTime.isBefore(endTime)) {
            throw new BadRequestException("Start time must be before end time");
        }
        if (numberOfAttendees <= 0) {
            throw new BadRequestException("Number of attendees must be greater than 0");
        }

        Long companyId = resolveCompanyId();
        MeetingRoom room = meetingRoomRepository.findById(meetingRoomId)
                .orElseThrow(() -> new BadRequestException("Meeting room not found"));
        if (!companyId.equals(room.getCompanyId()) || !Boolean.TRUE.equals(room.getActive())
                || room.getAvailabilityStatus() != RoomAvailabilityStatus.AVAILABLE) {
            throw new BadRequestException("Meeting room is not available for booking");
        }
        if (numberOfAttendees > room.getCapacity()) {
            throw new BadRequestException("Attendee count cannot exceed room capacity");
        }
        validateNoOverlap(companyId, meetingRoomId, meetingDate, startTime, endTime, excludeBookingId);
        return room;
    }

    private void validateNoOverlap(Long companyId,
                                   Long roomId,
                                   LocalDate meetingDate,
                                   LocalTime startTime,
                                   LocalTime endTime,
                                   Long excludeBookingId) {
        boolean overlaps = meetingBookingRepository.findAll().stream()
                .filter(booking -> companyId.equals(booking.getCompanyId()))
                .filter(booking -> roomId.equals(booking.getMeetingRoomId()))
                .filter(booking -> meetingDate.equals(booking.getMeetingDate()))
                .filter(booking -> excludeBookingId == null || !excludeBookingId.equals(booking.getId()))
                .filter(booking -> booking.getStatus() != MeetingBookingStatus.DRAFT)
                .filter(booking -> booking.getStatus() != MeetingBookingStatus.REJECTED)
                .filter(booking -> booking.getStatus() != MeetingBookingStatus.CANCELLED)
                .filter(booking -> booking.getStatus() != MeetingBookingStatus.COMPLETED)
                .anyMatch(booking -> startTime.isBefore(booking.getEndTime()) && endTime.isAfter(booking.getStartTime()));
        if (overlaps) {
            throw new BadRequestException("Meeting room is already booked for the selected time");
        }
    }

    private void replaceDetails(MeetingBooking booking,
                                List<MeetingBookingRefreshmentRequest> refreshmentRequests,
                                List<MeetingBookingBeverageRequest> beverageRequests,
                                List<MeetingBookingSupportServiceRequest> supportRequests,
                                List<MeetingBookingParticipantRequest> participantRequests) {
        Long companyId = resolveCompanyId();

        booking.getRefreshments().clear();
        if (refreshmentRequests != null) {
            refreshmentRequests.forEach(line -> booking.getRefreshments().add(buildRefreshmentLine(booking, line, companyId)));
        }

        booking.getBeverages().clear();
        if (beverageRequests != null) {
            beverageRequests.forEach(line -> booking.getBeverages().add(buildBeverageLine(booking, line, companyId)));
        }

        booking.getSupportServices().clear();
        if (supportRequests != null) {
            supportRequests.forEach(line -> booking.getSupportServices().add(buildSupportLine(booking, line, companyId)));
        }

        booking.getParticipants().clear();
        if (participantRequests != null) {
            participantRequests.forEach(line -> booking.getParticipants().add(buildParticipantLine(booking, line)));
        }
    }

    private MeetingBookingRefreshment buildRefreshmentLine(MeetingBooking booking, MeetingBookingRefreshmentRequest line, Long companyId) {
        if (line == null || line.getRefreshmentId() == null || line.getVendorId() == null || line.getQuantity() == null || line.getQuantity() <= 0) {
            throw new BadRequestException("Invalid refreshment line");
        }
        MeetingRefreshment refreshment = meetingRefreshmentRepository.findById(line.getRefreshmentId())
                .orElseThrow(() -> new BadRequestException("Refreshment not found"));
        if (!companyId.equals(refreshment.getCompanyId()) || !Boolean.TRUE.equals(refreshment.getActive())) {
            throw new BadRequestException("Refreshment is not selectable");
        }
        MeetingVendor vendor = resolveActiveVendor(line.getVendorId(), companyId);
        BigDecimal total = refreshment.getUnitPrice().multiply(BigDecimal.valueOf(line.getQuantity()));
        return MeetingBookingRefreshment.builder()
                .booking(booking)
                .refreshmentId(refreshment.getId())
                .refreshmentCode(refreshment.getRefreshmentCode())
                .itemName(refreshment.getItemName())
                .category(refreshment.getCategory())
                .vendorId(vendor.getId())
                .vendorCode(vendor.getVendorCode())
                .vendorName(vendor.getVendorName())
                .quantity(line.getQuantity())
                .unitPrice(refreshment.getUnitPrice())
                .totalAmount(total)
                .build();
    }

    private MeetingBookingBeverage buildBeverageLine(MeetingBooking booking, MeetingBookingBeverageRequest line, Long companyId) {
        if (line == null || line.getBeverageId() == null || line.getVendorId() == null || line.getQuantity() == null || line.getQuantity() <= 0) {
            throw new BadRequestException("Invalid beverage line");
        }
        MeetingBeverage beverage = meetingBeverageRepository.findById(line.getBeverageId())
                .orElseThrow(() -> new BadRequestException("Beverage not found"));
        if (!companyId.equals(beverage.getCompanyId()) || !Boolean.TRUE.equals(beverage.getActive())) {
            throw new BadRequestException("Beverage is not selectable");
        }
        MeetingVendor vendor = resolveActiveVendor(line.getVendorId(), companyId);
        BigDecimal total = beverage.getUnitPrice().multiply(BigDecimal.valueOf(line.getQuantity()));
        return MeetingBookingBeverage.builder()
                .booking(booking)
                .beverageId(beverage.getId())
                .beverageCode(beverage.getBeverageCode())
                .beverageName(beverage.getBeverageName())
                .vendorId(vendor.getId())
                .vendorCode(vendor.getVendorCode())
                .vendorName(vendor.getVendorName())
                .quantity(line.getQuantity())
                .unitPrice(beverage.getUnitPrice())
                .totalAmount(total)
                .build();
    }

    private MeetingBookingSupportService buildSupportLine(MeetingBooking booking, MeetingBookingSupportServiceRequest line, Long companyId) {
        if (line == null || line.getServiceId() == null) {
            throw new BadRequestException("Invalid support service line");
        }
        MeetingSupportService service = meetingSupportServiceRepository.findById(line.getServiceId())
                .orElseThrow(() -> new BadRequestException("Support service not found"));
        if (!companyId.equals(service.getCompanyId()) || !Boolean.TRUE.equals(service.getActive())) {
            throw new BadRequestException("Support service is not selectable");
        }
        BigDecimal amount = Boolean.TRUE.equals(service.getChargeable())
                ? defaultZero(service.getDefaultCharge())
                : BigDecimal.ZERO;
        return MeetingBookingSupportService.builder()
                .booking(booking)
                .serviceId(service.getId())
                .serviceCode(service.getServiceCode())
                .serviceName(service.getServiceName())
                .serviceCategory(service.getServiceCategory())
                .assignedTeam(service.getAssignedTeam())
                .chargeable(service.getChargeable())
                .estimatedAmount(amount)
                .remarks(trimToNull(line.getRemarks()))
                .build();
    }

    private MeetingBookingParticipant buildParticipantLine(MeetingBooking booking, MeetingBookingParticipantRequest line) {
        if (line == null || line.getParticipantType() == null) {
            throw new BadRequestException("Invalid participant line");
        }
        if (line.getParticipantType() == MeetingParticipantType.INTERNAL
                && !StringUtils.hasText(line.getEmployeeCode())
                && !StringUtils.hasText(line.getEmployeeName())) {
            throw new BadRequestException("Internal participant requires employee details");
        }
        if (line.getParticipantType() == MeetingParticipantType.EXTERNAL && !StringUtils.hasText(line.getName())) {
            throw new BadRequestException("External participant requires name");
        }
        return MeetingBookingParticipant.builder()
                .booking(booking)
                .participantType(line.getParticipantType())
                .employeeCode(trimToNull(line.getEmployeeCode()))
                .employeeName(trimToNull(line.getEmployeeName()))
                .name(trimToNull(line.getName()))
                .company(trimToNull(line.getCompany()))
                .contactNo(trimToNull(line.getContactNo()))
                .email(trimToNull(line.getEmail()))
                .build();
    }

    private MeetingVendor resolveActiveVendor(Long vendorId, Long companyId) {
        MeetingVendor vendor = meetingVendorRepository.findById(vendorId)
                .orElseThrow(() -> new BadRequestException("Vendor not found"));
        if (!companyId.equals(vendor.getCompanyId()) || !Boolean.TRUE.equals(vendor.getActive())) {
            throw new BadRequestException("Vendor must be active");
        }
        return vendor;
    }

    private void calculateSummary(MeetingBooking booking) {
        BigDecimal refreshmentCost = booking.getRefreshments().stream()
                .map(MeetingBookingRefreshment::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal beverageCost = booking.getBeverages().stream()
                .map(MeetingBookingBeverage::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal supportCost = booking.getSupportServices().stream()
                .map(MeetingBookingSupportService::getEstimatedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long minutes = Duration.between(booking.getStartTime(), booking.getEndTime()).toMinutes();
        BigDecimal durationHours = BigDecimal.valueOf(minutes)
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
        BigDecimal roomCharge = booking.getMeetingType() == MeetingBookingType.EXTERNAL_MEETING
                ? EXTERNAL_ROOM_RATE.multiply(durationHours)
                : BigDecimal.ZERO;

        booking.setDurationHours(durationHours);
        booking.setEstimatedRefreshmentCost(refreshmentCost);
        booking.setEstimatedBeverageCost(beverageCost);
        booking.setEstimatedSupportCost(supportCost);
        booking.setRoomCharge(roomCharge);
        booking.setTotalEstimatedCost(refreshmentCost.add(beverageCost).add(supportCost).add(roomCharge));
    }

    private void validateRequiredDetails(MeetingBooking booking) {
        if (booking.getRefreshments().stream().anyMatch(line -> line.getVendorId() == null || line.getQuantity() == null || line.getQuantity() <= 0)) {
            throw new BadRequestException("Invalid refreshment requirements");
        }
        if (booking.getBeverages().stream().anyMatch(line -> line.getVendorId() == null || line.getQuantity() == null || line.getQuantity() <= 0)) {
            throw new BadRequestException("Invalid beverage requirements");
        }
    }

    private MeetingBooking findBooking(Long id) {
        Long companyId = resolveCompanyId();
        MeetingBooking booking = meetingBookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Meeting booking not found"));
        if (!companyId.equals(booking.getCompanyId())) {
            throw new ResourceNotFoundException("Meeting booking not found");
        }
        return booking;
    }

    private boolean matches(MeetingBooking booking, MeetingBookingFilterSearch search) {
        if (search == null) {
            return true;
        }
        return contains(booking.getRequestNo(), search.getRequestNo())
                && contains(booking.getMeetingName(), search.getMeetingName())
                && contains(booking.getMeetingType() != null ? booking.getMeetingType().name() : null, search.getMeetingType())
                && contains(booking.getMeetingRoomName(), search.getMeetingRoomName())
                && contains(booking.getMeetingDate() != null ? booking.getMeetingDate().toString() : null, search.getMeetingDate())
                && contains(booking.getStatus() != null ? booking.getStatus().name() : null, search.getStatus())
                && contains(booking.getCreatedBy(), search.getCreatedBy());
    }

    private Comparator<MeetingBooking> resolveComparator(MeetingBookingFilterRequest request) {
        String column = request != null && StringUtils.hasText(request.getSortColumn())
                ? request.getSortColumn().trim()
                : "lastModifiedDate";
        boolean desc = request == null || !"ASC".equalsIgnoreCase(request.getSortDirection());
        Comparator<MeetingBooking> comparator = switch (column) {
            case "requestNo" -> Comparator.comparing(MeetingBooking::getRequestNo, Comparator.nullsLast(String::compareToIgnoreCase));
            case "meetingName" -> Comparator.comparing(MeetingBooking::getMeetingName, Comparator.nullsLast(String::compareToIgnoreCase));
            case "meetingDate" -> Comparator.comparing(MeetingBooking::getMeetingDate, Comparator.nullsLast(LocalDate::compareTo));
            case "startTime" -> Comparator.comparing(MeetingBooking::getStartTime, Comparator.nullsLast(LocalTime::compareTo));
            case "status" -> Comparator.comparing(booking -> booking.getStatus() != null ? booking.getStatus().name() : null,
                    Comparator.nullsLast(String::compareToIgnoreCase));
            default -> Comparator.comparing(MeetingBooking::getLastModifiedDate, Comparator.nullsLast(LocalDateTime::compareTo));
        };
        return desc ? comparator.reversed() : comparator;
    }

    private MeetingBookingDto toDto(MeetingBooking booking) {
        return MeetingBookingDto.builder()
                .id(booking.getId())
                .companyId(booking.getCompanyId())
                .companyCode(booking.getCompanyCode())
                .companyName(booking.getCompanyName())
                .requestNo(booking.getRequestNo())
                .meetingName(booking.getMeetingName())
                .meetingType(booking.getMeetingType())
                .meetingTypeDescription(booking.getMeetingType() != null ? toTitleCase(booking.getMeetingType().name()) : null)
                .meetingRoomId(booking.getMeetingRoomId())
                .meetingRoomCode(booking.getMeetingRoomCode())
                .meetingRoomName(booking.getMeetingRoomName())
                .roomCapacity(booking.getRoomCapacity())
                .meetingDate(booking.getMeetingDate())
                .startTime(booking.getStartTime())
                .endTime(booking.getEndTime())
                .numberOfAttendees(booking.getNumberOfAttendees())
                .purposeRemarks(booking.getPurposeRemarks())
                .status(booking.getStatus())
                .statusDescription(booking.getStatus() != null ? toTitleCase(booking.getStatus().name()) : null)
                .durationHours(booking.getDurationHours())
                .estimatedRefreshmentCost(booking.getEstimatedRefreshmentCost())
                .estimatedBeverageCost(booking.getEstimatedBeverageCost())
                .estimatedSupportCost(booking.getEstimatedSupportCost())
                .roomCharge(booking.getRoomCharge())
                .totalEstimatedCost(booking.getTotalEstimatedCost())
                .refreshments(booking.getRefreshments().stream().map(this::toRefreshmentDto).toList())
                .beverages(booking.getBeverages().stream().map(this::toBeverageDto).toList())
                .supportServices(booking.getSupportServices().stream().map(this::toSupportDto).toList())
                .participants(booking.getParticipants().stream().map(this::toParticipantDto).toList())
                .submittedBy(booking.getSubmittedBy())
                .submittedDate(booking.getSubmittedDate())
                .approvedBy(booking.getApprovedBy())
                .approvedDate(booking.getApprovedDate())
                .approvalRemark(booking.getApprovalRemark())
                .rejectedBy(booking.getRejectedBy())
                .rejectedDate(booking.getRejectedDate())
                .rejectionRemark(booking.getRejectionRemark())
                .cancellationReason(booking.getCancellationReason())
                .cancellationReasonCode(booking.getCancellationReasonCode())
                .cancelledBy(booking.getCancelledBy())
                .cancelledDate(booking.getCancelledDate())
                .ongoingUpdate(booking.getOngoingUpdate())
                .ongoingAdditionalAttendees(booking.getOngoingAdditionalAttendees())
                .ongoingUpdatedTotalAttendees(booking.getOngoingUpdatedTotalAttendees())
                .ongoingUpdatedBy(booking.getOngoingUpdatedBy())
                .ongoingUpdatedDate(booking.getOngoingUpdatedDate())
                .invoiceNo(booking.getInvoiceNo())
                .createdDate(booking.getCreatedDate())
                .lastModifiedDate(booking.getLastModifiedDate())
                .createdBy(booking.getCreatedBy())
                .lastModifiedBy(booking.getLastModifiedBy())
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

    private MeetingBookingParticipantDto toParticipantDto(MeetingBookingParticipant line) {
        return MeetingBookingParticipantDto.builder()
                .id(line.getId())
                .participantType(line.getParticipantType())
                .employeeCode(line.getEmployeeCode())
                .employeeName(line.getEmployeeName())
                .name(line.getName())
                .company(line.getCompany())
                .contactNo(line.getContactNo())
                .email(line.getEmail())
                .build();
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

    private MeetingVendorDto toVendorDto(MeetingVendor vendor) {
        return MeetingVendorDto.builder()
                .id(vendor.getId())
                .companyId(vendor.getCompanyId())
                .companyCode(vendor.getCompanyCode())
                .companyName(vendor.getCompanyName())
                .vendorCode(vendor.getVendorCode())
                .vendorName(vendor.getVendorName())
                .vendorType(vendor.getVendorType())
                .vendorTypeDescription(vendor.getVendorType() != null ? toTitleCase(vendor.getVendorType().name()) : null)
                .contactPerson(vendor.getContactPerson())
                .contactNumber(vendor.getContactNumber())
                .emailAddress(vendor.getEmailAddress())
                .active(vendor.getActive())
                .activeStatusDescription(Boolean.TRUE.equals(vendor.getActive()) ? "Active" : "Inactive")
                .selectable(Boolean.TRUE.equals(vendor.getActive()))
                .build();
    }

    private MeetingRefreshmentDto toRefreshmentMasterDto(MeetingRefreshment refreshment) {
        return MeetingRefreshmentDto.builder()
                .id(refreshment.getId())
                .companyId(refreshment.getCompanyId())
                .companyCode(refreshment.getCompanyCode())
                .companyName(refreshment.getCompanyName())
                .refreshmentCode(refreshment.getRefreshmentCode())
                .category(refreshment.getCategory())
                .categoryDescription(refreshment.getCategory() != null ? toTitleCase(refreshment.getCategory().name()) : null)
                .itemName(refreshment.getItemName())
                .defaultVendorId(refreshment.getDefaultVendorId())
                .defaultVendorCode(refreshment.getDefaultVendorCode())
                .defaultVendorName(refreshment.getDefaultVendorName())
                .unitPrice(refreshment.getUnitPrice())
                .description(refreshment.getDescription())
                .active(refreshment.getActive())
                .activeStatusDescription(Boolean.TRUE.equals(refreshment.getActive()) ? "Active" : "Inactive")
                .selectable(Boolean.TRUE.equals(refreshment.getActive()))
                .build();
    }

    private MeetingBeverageDto toBeverageMasterDto(MeetingBeverage beverage) {
        return MeetingBeverageDto.builder()
                .id(beverage.getId())
                .companyId(beverage.getCompanyId())
                .companyCode(beverage.getCompanyCode())
                .companyName(beverage.getCompanyName())
                .beverageCode(beverage.getBeverageCode())
                .beverageName(beverage.getBeverageName())
                .defaultVendorId(beverage.getDefaultVendorId())
                .defaultVendorCode(beverage.getDefaultVendorCode())
                .defaultVendorName(beverage.getDefaultVendorName())
                .unitPrice(beverage.getUnitPrice())
                .description(beverage.getDescription())
                .active(beverage.getActive())
                .activeStatusDescription(Boolean.TRUE.equals(beverage.getActive()) ? "Active" : "Inactive")
                .selectable(Boolean.TRUE.equals(beverage.getActive()))
                .build();
    }

    private MeetingSupportServiceDto toSupportMasterDto(MeetingSupportService service) {
        return MeetingSupportServiceDto.builder()
                .id(service.getId())
                .companyId(service.getCompanyId())
                .companyCode(service.getCompanyCode())
                .companyName(service.getCompanyName())
                .serviceCode(service.getServiceCode())
                .serviceName(service.getServiceName())
                .serviceCategory(service.getServiceCategory())
                .serviceCategoryDescription(service.getServiceCategory() != null ? toTitleCase(service.getServiceCategory().name()) : null)
                .assignedTeam(service.getAssignedTeam())
                .assignedTeamDescription(service.getAssignedTeam() != null ? toTitleCase(service.getAssignedTeam().name()) : null)
                .chargeable(service.getChargeable())
                .defaultCharge(service.getDefaultCharge())
                .description(service.getDescription())
                .active(service.getActive())
                .activeStatusDescription(Boolean.TRUE.equals(service.getActive()) ? "Active" : "Inactive")
                .selectable(Boolean.TRUE.equals(service.getActive()))
                .build();
    }

    private List<MeetingBookingRefreshmentRequest> toRefreshmentRequests(MeetingBooking booking) {
        return booking.getRefreshments().stream()
                .map(line -> {
                    MeetingBookingRefreshmentRequest request = new MeetingBookingRefreshmentRequest();
                    request.setRefreshmentId(line.getRefreshmentId());
                    request.setVendorId(line.getVendorId());
                    request.setQuantity(line.getQuantity());
                    return request;
                })
                .toList();
    }

    private List<MeetingBookingBeverageRequest> toBeverageRequests(MeetingBooking booking) {
        return booking.getBeverages().stream()
                .map(line -> {
                    MeetingBookingBeverageRequest request = new MeetingBookingBeverageRequest();
                    request.setBeverageId(line.getBeverageId());
                    request.setVendorId(line.getVendorId());
                    request.setQuantity(line.getQuantity());
                    return request;
                })
                .toList();
    }

    private List<MeetingBookingSupportServiceRequest> toSupportRequests(MeetingBooking booking) {
        return booking.getSupportServices().stream()
                .map(line -> {
                    MeetingBookingSupportServiceRequest request = new MeetingBookingSupportServiceRequest();
                    request.setServiceId(line.getServiceId());
                    request.setRemarks(line.getRemarks());
                    return request;
                })
                .toList();
    }

    private List<MeetingBookingParticipantRequest> toParticipantRequests(MeetingBooking booking) {
        return booking.getParticipants().stream()
                .map(line -> {
                    MeetingBookingParticipantRequest request = new MeetingBookingParticipantRequest();
                    request.setParticipantType(line.getParticipantType());
                    request.setEmployeeCode(line.getEmployeeCode());
                    request.setEmployeeName(line.getEmployeeName());
                    request.setName(line.getName());
                    request.setCompany(line.getCompany());
                    request.setContactNo(line.getContactNo());
                    request.setEmail(line.getEmail());
                    return request;
                })
                .toList();
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
        return List.of(values).stream()
                .map(value -> ReferenceOptionDto.builder()
                        .code(value.name())
                        .description(toTitleCase(value.name()))
                        .build())
                .toList();
    }

    private String generateRequestNo(Long companyId) {
        String requestNo;
        do {
            requestNo = "MBR-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                    + "-" + ThreadLocalRandom.current().nextInt(1000, 10000);
        } while (meetingBookingRepository.existsByCompanyIdAndRequestNoIgnoreCase(companyId, requestNo));
        return requestNo;
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

    private BigDecimal defaultZero(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
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
