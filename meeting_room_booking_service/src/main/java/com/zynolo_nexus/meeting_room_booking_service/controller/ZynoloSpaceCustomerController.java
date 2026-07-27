package com.zynolo_nexus.meeting_room_booking_service.controller;

import com.zynolo_nexus.meeting_room_booking_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.ZynoloSpaceCustomerActiveStatusRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.ZynoloSpaceCustomerCreateRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.ZynoloSpaceCustomerFilterRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.ZynoloSpaceCustomerReferenceDataRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.ZynoloSpaceCustomerUpdateRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.request.ZynoloSpaceCustomerViewRequest;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.ZynoloSpaceCustomerDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.ZynoloSpaceCustomerFilterResultDto;
import com.zynolo_nexus.meeting_room_booking_service.dto.response.ZynoloSpaceCustomerReferenceDataDto;
import com.zynolo_nexus.meeting_room_booking_service.service.ZynoloSpaceCustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/meeting-room/space-customers")
@RequiredArgsConstructor
public class ZynoloSpaceCustomerController {

    private final ZynoloSpaceCustomerService zynoloSpaceCustomerService;

    @PostMapping
    public MessageResponseDTO<ZynoloSpaceCustomerDto> create(@Valid @RequestBody ZynoloSpaceCustomerCreateRequest request) {
        return zynoloSpaceCustomerService.create(request);
    }

    @PostMapping("/update")
    public MessageResponseDTO<ZynoloSpaceCustomerDto> update(@Valid @RequestBody ZynoloSpaceCustomerUpdateRequest request) {
        return zynoloSpaceCustomerService.update(request);
    }

    @PostMapping("/view")
    public MessageResponseDTO<ZynoloSpaceCustomerDto> view(@RequestBody ZynoloSpaceCustomerViewRequest request) {
        return zynoloSpaceCustomerService.view(request != null ? request.getId() : null);
    }

    @PostMapping("/active-status")
    public MessageResponseDTO<ZynoloSpaceCustomerDto> activeStatus(@RequestBody ZynoloSpaceCustomerActiveStatusRequest request) {
        return zynoloSpaceCustomerService.updateActiveStatus(request);
    }

    @PostMapping("/filter-list")
    public MessageResponseDTO<ZynoloSpaceCustomerFilterResultDto> filterList(@RequestBody ZynoloSpaceCustomerFilterRequest request) {
        return zynoloSpaceCustomerService.filterList(request);
    }

    @PostMapping("/reference-data")
    public MessageResponseDTO<ZynoloSpaceCustomerReferenceDataDto> referenceData(
            @RequestBody(required = false) ZynoloSpaceCustomerReferenceDataRequest request) {
        return zynoloSpaceCustomerService.referenceData(request);
    }
}
