package com.zynolo_nexus.meeting_room_booking_service.repository;

import com.zynolo_nexus.meeting_room_booking_service.model.MeetingBeverage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeetingBeverageRepository extends JpaRepository<MeetingBeverage, Long> {

    boolean existsByCompanyIdAndBeverageCodeIgnoreCase(Long companyId, String beverageCode);

    boolean existsByCompanyIdAndBeverageCodeIgnoreCaseAndIdNot(Long companyId, String beverageCode, Long id);

    boolean existsByCompanyIdAndBeverageNameIgnoreCase(Long companyId, String beverageName);

    boolean existsByCompanyIdAndBeverageNameIgnoreCaseAndIdNot(Long companyId, String beverageName, Long id);
}
