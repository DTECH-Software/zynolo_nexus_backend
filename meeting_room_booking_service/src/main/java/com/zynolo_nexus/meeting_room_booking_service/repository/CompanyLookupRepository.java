package com.zynolo_nexus.meeting_room_booking_service.repository;

import com.zynolo_nexus.meeting_room_booking_service.model.CompanyLookup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyLookupRepository extends JpaRepository<CompanyLookup, Long> {
}
