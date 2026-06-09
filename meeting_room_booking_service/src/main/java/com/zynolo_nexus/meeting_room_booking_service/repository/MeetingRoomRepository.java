package com.zynolo_nexus.meeting_room_booking_service.repository;

import com.zynolo_nexus.meeting_room_booking_service.model.MeetingRoom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeetingRoomRepository extends JpaRepository<MeetingRoom, Long> {

    long countByCompanyId(Long companyId);

    boolean existsByCompanyIdAndRoomCodeIgnoreCase(Long companyId, String roomCode);

    boolean existsByCompanyIdAndRoomCodeIgnoreCaseAndIdNot(Long companyId, String roomCode, Long id);

    boolean existsByCompanyIdAndRoomNameIgnoreCase(Long companyId, String roomName);

    boolean existsByCompanyIdAndRoomNameIgnoreCaseAndIdNot(Long companyId, String roomName, Long id);
}
