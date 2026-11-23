package com.zynolo_nexus.meeting_room_booking_service.repository;

import com.zynolo_nexus.meeting_room_booking_service.model.MeetingRefreshment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeetingRefreshmentRepository extends JpaRepository<MeetingRefreshment, Long> {

    boolean existsByCompanyIdAndRefreshmentCodeIgnoreCase(Long companyId, String refreshmentCode);

    boolean existsByCompanyIdAndRefreshmentCodeIgnoreCaseAndIdNot(Long companyId, String refreshmentCode, Long id);
}
