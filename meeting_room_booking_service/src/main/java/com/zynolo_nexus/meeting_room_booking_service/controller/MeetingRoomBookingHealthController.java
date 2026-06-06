package com.zynolo_nexus.meeting_room_booking_service.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/meeting-room/health")
public class MeetingRoomBookingHealthController {

    @GetMapping
    public Map<String, String> health() {
        return Map.of(
                "status", "UP",
                "service", "meeting-room-booking-service"
        );
    }
}
