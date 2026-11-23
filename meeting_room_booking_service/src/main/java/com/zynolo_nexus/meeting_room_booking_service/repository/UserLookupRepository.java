package com.zynolo_nexus.meeting_room_booking_service.repository;

import com.zynolo_nexus.meeting_room_booking_service.model.UserLookup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserLookupRepository extends JpaRepository<UserLookup, Long> {

    Optional<UserLookup> findByUsername(String username);
}
