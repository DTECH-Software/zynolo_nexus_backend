package com.zynolo_nexus.meeting_room_booking_service.repository;

import com.zynolo_nexus.meeting_room_booking_service.model.ZynoloSpaceCustomer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ZynoloSpaceCustomerRepository extends JpaRepository<ZynoloSpaceCustomer, Long> {

    boolean existsByCompanyIdAndCustomerCodeIgnoreCase(Long companyId, String customerCode);

    boolean existsByCompanyIdAndCustomerCodeIgnoreCaseAndIdNot(Long companyId, String customerCode, Long id);

    boolean existsByCompanyIdAndCustomerCompanyNameIgnoreCase(Long companyId, String customerCompanyName);

    boolean existsByCompanyIdAndCustomerCompanyNameIgnoreCaseAndIdNot(Long companyId, String customerCompanyName, Long id);

    Optional<ZynoloSpaceCustomer> findByCompanyIdAndCustomerCodeIgnoreCase(Long companyId, String customerCode);

    Optional<ZynoloSpaceCustomer> findByCompanyIdAndCustomerCompanyNameIgnoreCase(Long companyId, String customerCompanyName);
}
