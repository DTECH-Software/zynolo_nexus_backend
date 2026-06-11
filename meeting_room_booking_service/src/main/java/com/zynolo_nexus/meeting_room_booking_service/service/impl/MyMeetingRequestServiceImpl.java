package com.zynolo_nexus.meeting_room_booking_service.service.impl;

import com.zynolo_nexus.meeting_room_booking_service.context.CompanyContext;
import com.zynolo_nexus.meeting_room_booking_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingBookingBeverageRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingBookingParticipantRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingBookingRefreshmentRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingBookingSupportServiceRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingBookingUpdateRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MyMeetingRequestActionRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MyMeetingRequestFilterRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MyMeetingRequestFilterSearch;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MyMeetingRequestReferenceDataRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingBookingBeverageDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingBookingDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingBookingParticipantDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingBookingRefreshmentDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingBookingSupportServiceDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingRoomDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MyMeetingRequestActionsDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MyMeetingRequestFilterResultDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MyMeetingRequestListItemDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MyMeetingRequestPrivilegesDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MyMeetingRequestReferenceDataDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.ReferenceOptionDto;
import com.zynolo_nexus.meeting_room_booking_service.enums.MeetingBookingStatus;
import com.zynolo_nexus.meeting_room_booking_service.enums.MeetingBookingType;
import com.zynolo_nexus.meeting_room_booking_service.enums.MeetingParticipantType;
import com.zynolo_nexus.meeting_room_booking_service.enums.RoomAvailabilityStatus;
import com.zynolo_nexus.meeting_room_booking_service.exception.BadRequestException;
import com.zynolo_nexus.meeting_room_booking_service.exception.ResourceNotFoundException;
import com.zynolo_nexus.meeting_room_booking_service.model.CompanyLookup;
import com.zynolo_nexus.meeting_room_booking_service.model.MeetingBooking;
import com.zynolo_nexus.meeting_room_booking_service.model.MeetingBookingBeverage;
import com.zynolo_nexus.meeting_room_booking_service.model.MeetingBookingParticipant;
import com.zynolo_nexus.meeting_room_booking_service.model.MeetingBookingRefreshment;
import com.zynolo_nexus.meeting_room_booking_service.model.MeetingBookingSupportService;
import com.zynolo_nexus.meeting_room_booking_service.model.MeetingRoom;
import com.zynolo_nexus.meeting_room_booking_service.repository.CompanyLookupRepository;
import com.zynolo_nexus.meeting_room_booking_service.repository.MeetingBookingRepository;
import com.zynolo_nexus.meeting_room_booking_service.repository.MeetingRoomRepository;
import com.zynolo_nexus.meeting_room_booking_service.service.MeetingBookingService;
import com.zynolo_nexus.meeting_room_booking_service.service.MyMeetingRequestService;
import com.zynolo_nexus.meeting_room_booking_service.service.support.PagePrivilegeResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
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
public class MyMeetingRequestServiceImpl implements MyMeetingRequestService {

    private static final String PAGE_CODE = "MBM_TRNS_MYRQ";

    private final MeetingBookingRepository meetingBookingRepository;
    private final MeetingRoomRepository meetingRoomRepository;
    private final CompanyLookupRepository companyLookupRepository;
    private final PagePrivilegeResolver pagePrivilegeResolver;
    private final MeetingBookingService meetingBookingService;

    @Value("${app.default.company-id:1}")
    private Long defaultCompanyId;

    @Override
    @Transactional(readOnly = true)
    public MessageResponseDTO<MyMeetingRequestReferenceDataDto> referenceData(MyMeetingRequestReferenceDataRequest request) {
        Long companyId = resolveCompanyId();
        CompanyLookup company = resolveCompany(companyId);
        var privileges = pagePrivilegeResolver.resolve(request != null ? request.getUsername() : null, PAGE_CODE);

        return MessageResponseDTO.<MyMeetingRequestReferenceDataDto>builder()
                .success(true)
                .message("My requests reference data loaded successfully")
                .data(MyMeetingRequestReferenceDataDto.builder()
                        .companyId(companyId)
                        .companyCode(resolveCompanyCode(company))
                        .companyName(resolveCompanyName(company))
                        .meetingTypes(toOptions(MeetingBookingType.values()))
                        .statuses(toOptions(MeetingBookingStatus.values()))
                        .meetingRooms(meetingRoomRepository.findAll().stream()
                                .filter(room -> companyId.equals(room.getCompanyId()))
                                .sorted(Comparator.comparing(MeetingRoom::getRoomName, Comparator.nullsLast(String::compareToIgnoreCase)))
                                .map(this::toRoomDto)
                                .toList())
                        .privileges(MyMeetingRequestPrivilegesDto.builder()
                                .createNewBooking(privileges.isAdd())
                                .view(privileges.isView())
                                .edit(privileges.isUpdate())
                                .search(privileges.isSearch())
                                .submit(privileges.isSubmit())
                                .delete(privileges.isDelete())
                                .cancel(privileges.isCancel())
                                .copyAsNew(privileges.isCopyAsNew())
                                .addOngoingUpdate(privileges.isOngoingUpdate())
                                .viewInvoice(privileges.isViewInvoice())
                                .export(privileges.isExport())
                                .build())
                        .build())
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public MessageResponseDTO<MyMeetingRequestFilterResultDto> filterList(MyMeetingRequestFilterRequest request) {
        String username = requireUsername(request != null ? request.getUsername() : null);
        int page = request != null && request.getPage() != null && request.getPage() >= 0 ? request.getPage() : 0;
        int size = request != null && request.getSize() != null && request.getSize() > 0 ? request.getSize() : 10;
        MyMeetingRequestFilterSearch search = request != null ? request.getSearch() : null;
        Long companyId = resolveCompanyId();
        var privileges = pagePrivilegeResolver.resolve(username, PAGE_CODE);

        List<MyMeetingRequestListItemDto> filtered = meetingBookingRepository.findAll().stream()
                .filter(booking -> companyId.equals(booking.getCompanyId()))
                .filter(booking -> username.equalsIgnoreCase(nullToEmpty(booking.getCreatedBy())))
                .filter(booking -> matches(booking, search))
                .sorted(resolveComparator(request))
                .map(booking -> toListItem(booking, privileges))
                .toList();

        int from = Math.min(page * size, filtered.size());
        int to = Math.min(from + size, filtered.size());
        List<MyMeetingRequestListItemDto> content = filtered.subList(from, to);
        int totalPages = filtered.isEmpty() ? 0 : (int) Math.ceil((double) filtered.size() / size);

        return MessageResponseDTO.<MyMeetingRequestFilterResultDto>builder()
                .success(true)
                .message("My meeting requests filtered successfully")
                .data(MyMeetingRequestFilterResultDto.builder()
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
    public MessageResponseDTO<MeetingBookingDto> view(MyMeetingRequestActionRequest request) {
        MeetingBooking booking = findOwnBooking(request);
        return success("My meeting request retrieved successfully", toDto(booking));
    }

    @Override
    @Transactional
    public MessageResponseDTO<MeetingBookingDto> update(MeetingBookingUpdateRequest request) {
        if (request == null || request.getId() == null) {
            throw new BadRequestException("Invalid my request update request");
        }
        MeetingBooking booking = findOwnBooking(request.getId(), requireUsername(request.getUsername()));
        if (booking.getStatus() != MeetingBookingStatus.DRAFT) {
            throw new BadRequestException("Creator cannot edit after submission");
        }
        return meetingBookingService.update(request);
    }

    @Override
    @Transactional
    public MessageResponseDTO<MeetingBookingDto> submit(MyMeetingRequestActionRequest request) {
        MeetingBooking booking = findOwnBooking(request);
        if (booking.getStatus() != MeetingBookingStatus.DRAFT) {
            throw new BadRequestException("Only draft requests can be submitted");
        }
        return meetingBookingService.submit(toBookingIdRequest(request));
    }

    @Override
    @Transactional
    public MessageResponseDTO<MeetingBookingDto> cancel(MyMeetingRequestActionRequest request) {
        MeetingBooking booking = findOwnBooking(request);
        if (!StringUtils.hasText(request.getCancellationReason())) {
            throw new BadRequestException("Cancellation reason is required");
        }
        if (booking.getStatus() != MeetingBookingStatus.APPROVED) {
            throw new BadRequestException("This request cannot be cancelled");
        }
        booking.setStatus(MeetingBookingStatus.CANCELLED);
        booking.setCancellationReason(request.getCancellationReason().trim());
        booking.setCancelledBy(request.getUsername().trim());
        booking.setCancelledDate(LocalDateTime.now());
        booking.setLastModifiedBy(request.getUsername().trim());
        return success("My meeting request cancelled successfully", toDto(meetingBookingRepository.save(booking)));
    }

    @Override
    @Transactional
    public MessageResponseDTO<MeetingBookingDto> deleteDraft(MyMeetingRequestActionRequest request) {
        MeetingBooking booking = findOwnBooking(request);
        if (booking.getStatus() != MeetingBookingStatus.DRAFT) {
            throw new BadRequestException("Only draft requests can be deleted");
        }
        MeetingBookingDto data = toDto(booking);
        meetingBookingRepository.delete(booking);
        return success("Draft meeting request deleted successfully", data);
    }

    @Override
    @Transactional
    public MessageResponseDTO<MeetingBookingDto> copyAsNew(MyMeetingRequestActionRequest request) {
        MeetingBooking source = findOwnBooking(request);
        if (source.getStatus() != MeetingBookingStatus.REJECTED) {
            throw new BadRequestException("Only rejected requests can be copied as new");
        }

        MeetingBooking copy = copyBooking(source, request.getUsername().trim());
        return success("Meeting request copied as new draft successfully", toDto(meetingBookingRepository.save(copy)));
    }

    @Override
    @Transactional
    public MessageResponseDTO<MeetingBookingDto> addOngoingUpdate(MyMeetingRequestActionRequest request) {
        MeetingBooking booking = findOwnBooking(request);
        if (booking.getStatus() != MeetingBookingStatus.ONGOING) {
            throw new BadRequestException("Ongoing update is allowed only for ongoing requests");
        }
        if (StringUtils.hasText(booking.getOngoingUpdate())) {
            throw new BadRequestException("Ongoing update is already added");
        }
        if (!StringUtils.hasText(request.getOngoingUpdate())) {
            throw new BadRequestException("Ongoing update is required");
        }
        booking.setOngoingUpdate(request.getOngoingUpdate().trim());
        booking.setOngoingUpdatedBy(request.getUsername().trim());
        booking.setOngoingUpdatedDate(LocalDateTime.now());
        booking.setLastModifiedBy(request.getUsername().trim());
        return success("Ongoing update added successfully", toDto(meetingBookingRepository.save(booking)));
    }

    private MeetingBooking findOwnBooking(MyMeetingRequestActionRequest request) {
        if (request == null || request.getId() == null) {
            throw new BadRequestException("Invalid my request action request");
        }
        return findOwnBooking(request.getId(), requireUsername(request.getUsername()));
    }

    private MeetingBooking findOwnBooking(Long id, String username) {
        Long companyId = resolveCompanyId();
        MeetingBooking booking = meetingBookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Meeting request not found"));
        if (!companyId.equals(booking.getCompanyId()) || !username.equalsIgnoreCase(nullToEmpty(booking.getCreatedBy()))) {
            throw new ResourceNotFoundException("Meeting request not found");
        }
        return booking;
    }

    private MeetingBooking copyBooking(MeetingBooking source, String username) {
        MeetingBooking copy = MeetingBooking.builder()
                .companyId(source.getCompanyId())
                .companyCode(source.getCompanyCode())
                .companyName(source.getCompanyName())
                .requestNo(generateRequestNo(source.getCompanyId()))
                .meetingName(source.getMeetingName())
                .meetingType(source.getMeetingType())
                .meetingRoomId(source.getMeetingRoomId())
                .meetingRoomCode(source.getMeetingRoomCode())
                .meetingRoomName(source.getMeetingRoomName())
                .roomCapacity(source.getRoomCapacity())
                .meetingDate(source.getMeetingDate())
                .startTime(source.getStartTime())
                .endTime(source.getEndTime())
                .numberOfAttendees(source.getNumberOfAttendees())
                .purposeRemarks(source.getPurposeRemarks())
                .status(MeetingBookingStatus.DRAFT)
                .durationHours(source.getDurationHours())
                .estimatedRefreshmentCost(source.getEstimatedRefreshmentCost())
                .estimatedBeverageCost(source.getEstimatedBeverageCost())
                .estimatedSupportCost(source.getEstimatedSupportCost())
                .roomCharge(source.getRoomCharge())
                .totalEstimatedCost(source.getTotalEstimatedCost())
                .createdBy(username)
                .lastModifiedBy(username)
                .build();

        source.getRefreshments().forEach(line -> copy.getRefreshments().add(MeetingBookingRefreshment.builder()
                .booking(copy)
                .refreshmentId(line.getRefreshmentId())
                .refreshmentCode(line.getRefreshmentCode())
                .itemName(line.getItemName())
                .category(line.getCategory())
                .vendorId(line.getVendorId())
                .vendorCode(line.getVendorCode())
                .vendorName(line.getVendorName())
                .quantity(line.getQuantity())
                .unitPrice(line.getUnitPrice())
                .totalAmount(line.getTotalAmount())
                .build()));
        source.getBeverages().forEach(line -> copy.getBeverages().add(MeetingBookingBeverage.builder()
                .booking(copy)
                .beverageId(line.getBeverageId())
                .beverageCode(line.getBeverageCode())
                .beverageName(line.getBeverageName())
                .vendorId(line.getVendorId())
                .vendorCode(line.getVendorCode())
                .vendorName(line.getVendorName())
                .quantity(line.getQuantity())
                .unitPrice(line.getUnitPrice())
                .totalAmount(line.getTotalAmount())
                .build()));
        source.getSupportServices().forEach(line -> copy.getSupportServices().add(MeetingBookingSupportService.builder()
                .booking(copy)
                .serviceId(line.getServiceId())
                .serviceCode(line.getServiceCode())
                .serviceName(line.getServiceName())
                .serviceCategory(line.getServiceCategory())
                .assignedTeam(line.getAssignedTeam())
                .chargeable(line.getChargeable())
                .estimatedAmount(line.getEstimatedAmount())
                .remarks(line.getRemarks())
                .build()));
        source.getParticipants().forEach(line -> copy.getParticipants().add(MeetingBookingParticipant.builder()
                .booking(copy)
                .participantType(line.getParticipantType())
                .employeeCode(line.getEmployeeCode())
                .employeeName(line.getEmployeeName())
                .name(line.getName())
                .company(line.getCompany())
                .contactNo(line.getContactNo())
                .email(line.getEmail())
                .build()));
        return copy;
    }

    private MyMeetingRequestListItemDto toListItem(MeetingBooking booking, com.zynolo_nexus.meeting_room_booking_service.service.support.PageTaskPrivileges privileges) {
        return MyMeetingRequestListItemDto.builder()
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
                .actions(resolveActions(booking, privileges))
                .build();
    }

    private MyMeetingRequestActionsDto resolveActions(MeetingBooking booking, com.zynolo_nexus.meeting_room_booking_service.service.support.PageTaskPrivileges privileges) {
        MeetingBookingStatus status = booking.getStatus();
        return MyMeetingRequestActionsDto.builder()
                .view(privileges.isView())
                .edit(privileges.isUpdate() && status == MeetingBookingStatus.DRAFT)
                .submit(privileges.isSubmit() && status == MeetingBookingStatus.DRAFT)
                .delete(privileges.isDelete() && status == MeetingBookingStatus.DRAFT)
                .cancel(privileges.isCancel() && (status == MeetingBookingStatus.APPROVED))
                .copyAsNew(privileges.isCopyAsNew() && status == MeetingBookingStatus.REJECTED)
                .addOngoingUpdate(privileges.isOngoingUpdate() && status == MeetingBookingStatus.ONGOING && !StringUtils.hasText(booking.getOngoingUpdate()))
                .viewInvoice(privileges.isViewInvoice() && status == MeetingBookingStatus.COMPLETED)
                .build();
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
                .cancelledBy(booking.getCancelledBy())
                .cancelledDate(booking.getCancelledDate())
                .ongoingUpdate(booking.getOngoingUpdate())
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

    private boolean matches(MeetingBooking booking, MyMeetingRequestFilterSearch search) {
        if (search == null) {
            return true;
        }
        return contains(booking.getRequestNo(), search.getRequestNo())
                && contains(booking.getMeetingName(), search.getMeetingName())
                && (search.getMeetingRoomId() == null || search.getMeetingRoomId().equals(booking.getMeetingRoomId()))
                && contains(booking.getMeetingRoomName(), search.getMeetingRoomName())
                && contains(booking.getMeetingType() != null ? booking.getMeetingType().name() : null, search.getMeetingType())
                && contains(booking.getStatus() != null ? booking.getStatus().name() : null, search.getStatus())
                && (search.getDateFrom() == null || !booking.getMeetingDate().isBefore(search.getDateFrom()))
                && (search.getDateTo() == null || !booking.getMeetingDate().isAfter(search.getDateTo()));
    }

    private Comparator<MeetingBooking> resolveComparator(MyMeetingRequestFilterRequest request) {
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

    private com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingBookingIdRequest toBookingIdRequest(MyMeetingRequestActionRequest request) {
        com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingBookingIdRequest idRequest =
                new com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingBookingIdRequest();
        idRequest.setChannel(request.getChannel());
        idRequest.setIp(request.getIp());
        idRequest.setMessage(request.getMessage());
        idRequest.setUserAgent(request.getUserAgent());
        idRequest.setUsername(request.getUsername());
        idRequest.setId(request.getId());
        return idRequest;
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

    private String requireUsername(String username) {
        if (!StringUtils.hasText(username)) {
            throw new BadRequestException("Username is required");
        }
        return username.trim();
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
