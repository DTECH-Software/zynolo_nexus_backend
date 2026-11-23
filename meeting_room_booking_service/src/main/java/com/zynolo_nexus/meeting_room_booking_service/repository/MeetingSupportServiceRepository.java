package com.zynolo_nexus.meeting_room_booking_service.repository;

import com.zynolo_nexus.meeting_room_booking_service.model.MeetingSupportService;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeetingSupportServiceRepository extends JpaRepository<MeetingSupportService, Long> {

    boolean existsByCompanyIdAndServiceCodeIgnoreCase(Long companyId, String serviceCode);

    boolean existsByCompanyIdAndServiceCodeIgnoreCaseAndIdNot(Long companyId, String serviceCode, Long id);

    boolean existsByCompanyIdAndServiceNameIgnoreCase(Long companyId, String serviceName);

    boolean existsByCompanyIdAndServiceNameIgnoreCaseAndIdNot(Long companyId, String serviceName, Long id);
}
