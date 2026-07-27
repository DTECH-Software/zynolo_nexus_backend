package com.zynolo_nexus.meeting_room_booking_service.repository;

import com.zynolo_nexus.meeting_room_booking_service.model.ZynoloSpaceCustomer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ZynoloSpaceCustomerRepository extends JpaRepository<ZynoloSpaceCustomer, Long> {

    boolean existsByCompanyIdAndCustomerCodeIgnoreCase(Long companyId, String customerCode);

    boolean existsByCompanyIdAndCustomerCodeIgnoreCaseAndIdNot(Long companyId, String customerCode, Long id);

    boolean existsByCompanyIdAndCustomerCompanyNameIgnoreCase(Long companyId, String customerCompanyName);

    boolean existsByCompanyIdAndCustomerCompanyNameIgnoreCaseAndIdNot(Long companyId, String customerCompanyName, Long id);
}
