package com.zynolo_nexus.meeting_room_booking_service.repository;

import com.zynolo_nexus.meeting_room_booking_service.model.MeetingRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MeetingRoomRepository extends JpaRepository<MeetingRoom, Long> {

    Optional<MeetingRoom> findByRoomCodeIgnoreCase(String roomCode);

    boolean existsByRoomCodeIgnoreCase(String roomCode);

    boolean existsByRoomCodeIgnoreCaseAndIdNot(String roomCode, Long id);
}
