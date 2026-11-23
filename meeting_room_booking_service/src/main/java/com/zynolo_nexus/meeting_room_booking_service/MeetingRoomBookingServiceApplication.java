package com.zynolo_nexus.meeting_room_booking_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MeetingRoomBookingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MeetingRoomBookingServiceApplication.class, args);
    }
}
