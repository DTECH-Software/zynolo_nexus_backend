package com.zynolo_nexus.meeting_room_booking_service.repository;

import com.zynolo_nexus.meeting_room_booking_service.enums.BookingStatus;
import com.zynolo_nexus.meeting_room_booking_service.model.MeetingRoomBooking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MeetingRoomBookingRepository extends JpaRepository<MeetingRoomBooking, Long> {

    Optional<MeetingRoomBooking> findByBookingNoIgnoreCase(String bookingNo);

    List<MeetingRoomBooking> findByRoomIdAndStatusInAndStartTimeLessThanAndEndTimeGreaterThan(
            Long roomId,
            Collection<BookingStatus> statuses,
            LocalDateTime endTime,
            LocalDateTime startTime);
}
