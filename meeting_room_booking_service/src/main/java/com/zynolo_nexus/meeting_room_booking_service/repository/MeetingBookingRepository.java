package com.zynolo_nexus.meeting_room_booking_service.repository;

import com.zynolo_nexus.meeting_room_booking_service.model.MeetingBooking;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeetingBookingRepository extends JpaRepository<MeetingBooking, Long> {

    boolean existsByCompanyIdAndRequestNoIgnoreCase(Long companyId, String requestNo);
}
