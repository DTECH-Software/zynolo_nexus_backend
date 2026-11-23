package com.zynolo_nexus.meeting_room_booking_service.repository;

import com.zynolo_nexus.meeting_room_booking_service.model.MeetingVendor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeetingVendorRepository extends JpaRepository<MeetingVendor, Long> {

    boolean existsByCompanyIdAndVendorCodeIgnoreCase(Long companyId, String vendorCode);

    boolean existsByCompanyIdAndVendorCodeIgnoreCaseAndIdNot(Long companyId, String vendorCode, Long id);

    boolean existsByCompanyIdAndVendorNameIgnoreCase(Long companyId, String vendorName);

    boolean existsByCompanyIdAndVendorNameIgnoreCaseAndIdNot(Long companyId, String vendorName, Long id);
}
