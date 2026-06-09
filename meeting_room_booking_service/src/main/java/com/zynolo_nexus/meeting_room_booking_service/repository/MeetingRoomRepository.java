package com.zynolo_nexus.meeting_room_booking_service.repository;

import com.zynolo_nexus.meeting_room_booking_service.model.MeetingRoom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeetingRoomRepository extends JpaRepository<MeetingRoom, Long> {

    boolean existsByRoomCodeIgnoreCase(String roomCode);

    boolean existsByRoomCodeIgnoreCaseAndIdNot(String roomCode, Long id);

    boolean existsByRoomNameIgnoreCase(String roomName);

    boolean existsByRoomNameIgnoreCaseAndIdNot(String roomName, Long id);
}
