package com.zynolo_nexus.meeting_room_booking_service.scheduler;

import com.zynolo_nexus.meeting_room_booking_service.enums.MeetingBookingStatus;
import com.zynolo_nexus.meeting_room_booking_service.model.MeetingBooking;
import com.zynolo_nexus.meeting_room_booking_service.repository.MeetingBookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class MeetingBookingStatusScheduler {

    private static final String SYSTEM_USER = "system";

    private final MeetingBookingRepository meetingBookingRepository;

    @Value("${meeting.booking.status-zone:Asia/Colombo}")
    private String statusZone;

    @Scheduled(fixedDelayString = "${meeting.booking.status-sync-delay-ms:60000}")
    @Transactional
    public void syncBookingStatuses() {
        LocalDateTime now = LocalDateTime.now(ZoneId.of(statusZone));
        List<MeetingBooking> changedBookings = meetingBookingRepository.findAll().stream()
                .filter(this::isAutoStatusCandidate)
                .filter(booking -> syncStatus(booking, now))
                .toList();

        if (!changedBookings.isEmpty()) {
            meetingBookingRepository.saveAll(changedBookings);
            log.info("Auto-updated {} meeting booking statuses", changedBookings.size());
        }
    }

    private boolean isAutoStatusCandidate(MeetingBooking booking) {
        return booking != null
                && booking.getMeetingDate() != null
                && booking.getStartTime() != null
                && booking.getEndTime() != null
                && (booking.getStatus() == MeetingBookingStatus.APPROVED
                || booking.getStatus() == MeetingBookingStatus.ONGOING);
    }

    private boolean syncStatus(MeetingBooking booking, LocalDateTime now) {
        LocalDateTime startDateTime = LocalDateTime.of(booking.getMeetingDate(), booking.getStartTime());
        LocalDateTime endDateTime = LocalDateTime.of(booking.getMeetingDate(), booking.getEndTime());

        if (booking.getStatus() == MeetingBookingStatus.APPROVED) {
            if (!now.isBefore(endDateTime)) {
                booking.setStatus(MeetingBookingStatus.COMPLETED);
                booking.setLastModifiedBy(SYSTEM_USER);
                return true;
            }
            if (!now.isBefore(startDateTime)) {
                booking.setStatus(MeetingBookingStatus.ONGOING);
                booking.setLastModifiedBy(SYSTEM_USER);
                return true;
            }
        }

        if (booking.getStatus() == MeetingBookingStatus.ONGOING && !now.isBefore(endDateTime)) {
            booking.setStatus(MeetingBookingStatus.COMPLETED);
            booking.setLastModifiedBy(SYSTEM_USER);
            return true;
        }

        return false;
    }
}
