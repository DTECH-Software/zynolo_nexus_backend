package com.zynolo_nexus.auth_service.repository;

import com.zynolo_nexus.auth_service.enums.CompanyModuleSubscriptionStatus;
import com.zynolo_nexus.auth_service.model.CompanyModuleSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompanyModuleSubscriptionRepository extends JpaRepository<CompanyModuleSubscription, Long> {

    List<CompanyModuleSubscription> findByCompany_IdAndStatus(Long companyId, CompanyModuleSubscriptionStatus status);

    List<CompanyModuleSubscription> findByCompany_Id(Long companyId);
}
