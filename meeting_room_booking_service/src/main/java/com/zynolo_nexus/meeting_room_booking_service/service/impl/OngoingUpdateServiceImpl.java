package com.zynolo_nexus.meeting_room_booking_service.service.impl;

import com.zynolo_nexus.meeting_room_booking_service.context.CompanyContext;
import com.zynolo_nexus.meeting_room_booking_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingBookingRefreshmentRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.MeetingBookingSupportServiceRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.OngoingUpdateFilterRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.OngoingUpdateFilterSearch;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.OngoingUpdateReferenceDataRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.OngoingUpdateSaveRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.OngoingUpdateViewRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingBookingDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingRefreshmentDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingRoomDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingSupportServiceDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.MeetingVendorDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.OngoingUpdateActionsDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.OngoingUpdateFilterResultDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.OngoingUpdateListItemDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.OngoingUpdatePrivilegesDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.OngoingUpdateReferenceDataDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.ReferenceOptionDto;
import com.zynolo_nexus.meeting_room_booking_service.enums.MeetingBookingStatus;
import com.zynolo_nexus.meeting_room_booking_service.enums.MeetingBookingType;
import com.zynolo_nexus.meeting_room_booking_service.enums.RoomAvailabilityStatus;
import com.zynolo_nexus.meeting_room_booking_service.exception.BadRequestException;
import com.zynolo_nexus.meeting_room_booking_service.exception.ResourceNotFoundException;
import com.zynolo_nexus.meeting_room_booking_service.model.CompanyLookup;
import com.zynolo_nexus.meeting_room_booking_service.model.MeetingBooking;
import com.zynolo_nexus.meeting_room_booking_service.model.MeetingBookingRefreshment;
import com.zynolo_nexus.meeting_room_booking_service.model.MeetingBookingSupportService;
import com.zynolo_nexus.meeting_room_booking_service.model.MeetingRefreshment;
import com.zynolo_nexus.meeting_room_booking_service.model.MeetingRoom;
import com.zynolo_nexus.meeting_room_booking_service.model.MeetingSupportService;
import com.zynolo_nexus.meeting_room_booking_service.model.MeetingVendor;
import com.zynolo_nexus.meeting_room_booking_service.repository.CompanyLookupRepository;
import com.zynolo_nexus.meeting_room_booking_service.repository.MeetingBookingRepository;
import com.zynolo_nexus.meeting_room_booking_service.repository.MeetingRefreshmentRepository;
import com.zynolo_nexus.meeting_room_booking_service.repository.MeetingRoomRepository;
import com.zynolo_nexus.meeting_room_booking_service.repository.MeetingSupportServiceRepository;
import com.zynolo_nexus.meeting_room_booking_service.repository.MeetingVendorRepository;
import com.zynolo_nexus.meeting_room_booking_service.service.MeetingBookingService;
import com.zynolo_nexus.meeting_room_booking_service.service.OngoingUpdateService;
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
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class OngoingUpdateServiceImpl implements OngoingUpdateService {

    private static final String PAGE_CODE = "MBM_TRNS_ONGU";
    private static final BigDecimal EXTERNAL_ROOM_RATE = new BigDecimal("3600.00");

    private final MeetingBookingRepository meetingBookingRepository;
    private final MeetingRoomRepository meetingRoomRepository;
    private final MeetingVendorRepository meetingVendorRepository;
    private final MeetingRefreshmentRepository meetingRefreshmentRepository;
    private final MeetingSupportServiceRepository meetingSupportServiceRepository;
    private final CompanyLookupRepository companyLookupRepository;
    private final PagePrivilegeResolver pagePrivilegeResolver;
    private final MeetingBookingService meetingBookingService;

    @Value("${app.default.company-id:1}")
    private Long defaultCompanyId;

    @Override
    @Transactional(readOnly = true)
    public MessageResponseDTO<OngoingUpdateReferenceDataDto> referenceData(OngoingUpdateReferenceDataRequest request) {
        Long companyId = resolveCompanyId();
        CompanyLookup company = resolveCompany(companyId);
        PageTaskPrivileges privileges = pagePrivilegeResolver.resolve(request != null ? request.getUsername() : null, PAGE_CODE);

        return MessageResponseDTO.<OngoingUpdateReferenceDataDto>builder()
                .success(true)
                .message("Ongoing updates reference data loaded successfully")
                .data(OngoingUpdateReferenceDataDto.builder()
                        .companyId(companyId)
                        .companyCode(resolveCompanyCode(company))
                        .companyName(resolveCompanyName(company))
                        .meetingTypes(toOptions(MeetingBookingType.values()))
                        .statuses(List.of(toOption(MeetingBookingStatus.ONGOING)))
                        .meetingRooms(meetingRoomRepository.findAll().stream()
                                .filter(room -> companyId.equals(room.getCompanyId()))
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
                                .map(this::toRefreshmentDto)
                                .toList())
                        .supportServices(meetingSupportServiceRepository.findAll().stream()
                                .filter(service -> companyId.equals(service.getCompanyId()))
                                .filter(service -> Boolean.TRUE.equals(service.getActive()))
                                .sorted(Comparator.comparing(MeetingSupportService::getServiceName, Comparator.nullsLast(String::compareToIgnoreCase)))
                                .map(this::toSupportServiceDto)
                                .toList())
                        .privileges(OngoingUpdatePrivilegesDto.builder()
                                .view(privileges.isView())
                                .search(privileges.isSearch())
                                .saveUpdate(privileges.isSaveUpdate() || privileges.isUpdate())
                                .build())
                        .build())
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public MessageResponseDTO<OngoingUpdateFilterResultDto> filterList(OngoingUpdateFilterRequest request) {
        String username = requireUsername(request != null ? request.getUsername() : null);
        int page = request != null && request.getPage() != null && request.getPage() >= 0 ? request.getPage() : 0;
        int size = request != null && request.getSize() != null && request.getSize() > 0 ? request.getSize() : 10;
        OngoingUpdateFilterSearch search = request != null ? request.getSearch() : null;
        Long companyId = resolveCompanyId();
        PageTaskPrivileges privileges = pagePrivilegeResolver.resolve(username, PAGE_CODE);

        List<OngoingUpdateListItemDto> filtered = meetingBookingRepository.findAll().stream()
                .filter(booking -> companyId.equals(booking.getCompanyId()))
                .filter(booking -> username.equalsIgnoreCase(nullToEmpty(booking.getCreatedBy())))
                .filter(booking -> booking.getStatus() == MeetingBookingStatus.ONGOING)
                .filter(booking -> matches(booking, search))
                .sorted(resolveComparator(request))
                .map(booking -> toListItem(booking, privileges))
                .toList();

        int from = Math.min(page * size, filtered.size());
        int to = Math.min(from + size, filtered.size());
        List<OngoingUpdateListItemDto> content = filtered.subList(from, to);
        int totalPages = filtered.isEmpty() ? 0 : (int) Math.ceil((double) filtered.size() / size);

        return MessageResponseDTO.<OngoingUpdateFilterResultDto>builder()
                .success(true)
                .message("Ongoing updates filtered successfully")
                .data(OngoingUpdateFilterResultDto.builder()
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
    public MessageResponseDTO<MeetingBookingDto> view(OngoingUpdateViewRequest request) {
        MeetingBooking booking = findOwnOngoingBooking(request);
        return success("Ongoing meeting update retrieved successfully", meetingBookingService.view(booking.getId()).getData());
    }

    @Override
    @Transactional
    public MessageResponseDTO<MeetingBookingDto> saveUpdate(OngoingUpdateSaveRequest request) {
        if (request == null || request.getId() == null) {
            throw new BadRequestException("Invalid ongoing update request");
        }
        String username = requireUsername(request.getUsername());
        MeetingBooking booking = findOwnOngoingBooking(request.getId(), username);
        if (StringUtils.hasText(booking.getOngoingUpdate()) || booking.getOngoingUpdatedDate() != null) {
            throw new BadRequestException("Ongoing update is already submitted for this booking");
        }
        if (!StringUtils.hasText(request.getUpdateReason())) {
            throw new BadRequestException("Update reason is required");
        }

        int additionalAttendees = request.getAdditionalAttendees() != null ? request.getAdditionalAttendees() : 0;
        if (additionalAttendees < 0) {
            throw new BadRequestException("Additional attendees cannot be negative");
        }
        boolean hasRefreshments = request.getAdditionalRefreshments() != null && !request.getAdditionalRefreshments().isEmpty();
        boolean hasSupportServices = request.getAdditionalSupportServices() != null && !request.getAdditionalSupportServices().isEmpty();
        if (additionalAttendees == 0 && !hasRefreshments && !hasSupportServices) {
            throw new BadRequestException("At least one ongoing update change is required");
        }

        int updatedTotalAttendees = defaultInt(booking.getNumberOfAttendees()) + additionalAttendees;
        if (updatedTotalAttendees > defaultInt(booking.getRoomCapacity())) {
            throw new BadRequestException("Updated attendees cannot exceed room capacity");
        }
        if (additionalAttendees > 0) {
            booking.setNumberOfAttendees(updatedTotalAttendees);
        }
        booking.setOngoingAdditionalAttendees(additionalAttendees);
        booking.setOngoingUpdatedTotalAttendees(updatedTotalAttendees);

        Long companyId = resolveCompanyId();
        if (hasRefreshments) {
            request.getAdditionalRefreshments().forEach(line -> booking.getRefreshments().add(buildRefreshmentLine(booking, line, companyId)));
        }
        if (hasSupportServices) {
            request.getAdditionalSupportServices().forEach(line -> booking.getSupportServices().add(buildSupportLine(booking, line, companyId)));
        }

        calculateSummary(booking);
        booking.setOngoingUpdate(request.getUpdateReason().trim());
        booking.setOngoingUpdatedBy(username);
        booking.setOngoingUpdatedDate(LocalDateTime.now());
        booking.setLastModifiedBy(username);
        meetingBookingRepository.save(booking);

        return success("Ongoing meeting update saved successfully", meetingBookingService.view(booking.getId()).getData());
    }

    private MeetingBookingRefreshment buildRefreshmentLine(MeetingBooking booking, MeetingBookingRefreshmentRequest line, Long companyId) {
        if (line == null || line.getRefreshmentId() == null || line.getVendorId() == null || line.getQuantity() == null || line.getQuantity() <= 0) {
            throw new BadRequestException("Invalid additional refreshment line");
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

    private MeetingBookingSupportService buildSupportLine(MeetingBooking booking, MeetingBookingSupportServiceRequest line, Long companyId) {
        if (line == null || line.getServiceId() == null) {
            throw new BadRequestException("Invalid additional support service line");
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
                .map(line -> line.getTotalAmount() != null ? line.getTotalAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal supportCost = booking.getSupportServices().stream()
                .map(line -> line.getEstimatedAmount() != null ? line.getEstimatedAmount() : BigDecimal.ZERO)
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

    private MeetingBooking findOwnOngoingBooking(OngoingUpdateViewRequest request) {
        if (request == null || request.getId() == null) {
            throw new BadRequestException("Invalid ongoing update view request");
        }
        return findOwnOngoingBooking(request.getId(), requireUsername(request.getUsername()));
    }

    private MeetingBooking findOwnOngoingBooking(Long id, String username) {
        Long companyId = resolveCompanyId();
        MeetingBooking booking = meetingBookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ongoing meeting booking not found"));
        if (!companyId.equals(booking.getCompanyId())
                || !username.equalsIgnoreCase(nullToEmpty(booking.getCreatedBy()))
                || booking.getStatus() != MeetingBookingStatus.ONGOING) {
            throw new ResourceNotFoundException("Ongoing meeting booking not found");
        }
        return booking;
    }

    private OngoingUpdateListItemDto toListItem(MeetingBooking booking, PageTaskPrivileges privileges) {
        boolean updateSubmitted = StringUtils.hasText(booking.getOngoingUpdate()) || booking.getOngoingUpdatedDate() != null;
        return OngoingUpdateListItemDto.builder()
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
                .currentAttendees(booking.getNumberOfAttendees())
                .roomCapacity(booking.getRoomCapacity())
                .status(booking.getStatus())
                .statusDescription(booking.getStatus() != null ? toTitleCase(booking.getStatus().name()) : null)
                .updateSubmitted(updateSubmitted)
                .ongoingUpdatedDate(booking.getOngoingUpdatedDate())
                .actions(OngoingUpdateActionsDto.builder()
                        .view(privileges.isView())
                        .saveUpdate((privileges.isSaveUpdate() || privileges.isUpdate()) && !updateSubmitted)
                        .build())
                .build();
    }

    private boolean matches(MeetingBooking booking, OngoingUpdateFilterSearch search) {
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

    private Comparator<MeetingBooking> resolveComparator(OngoingUpdateFilterRequest request) {
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

    private MeetingRefreshmentDto toRefreshmentDto(MeetingRefreshment refreshment) {
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

    private MeetingSupportServiceDto toSupportServiceDto(MeetingSupportService service) {
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

    private BigDecimal defaultZero(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
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
