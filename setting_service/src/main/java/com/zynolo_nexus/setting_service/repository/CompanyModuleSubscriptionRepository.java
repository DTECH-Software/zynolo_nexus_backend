package com.zynolo_nexus.setting_service.repository;

import com.zynolo_nexus.setting_service.enums.CompanyModuleSubscriptionStatus;
import com.zynolo_nexus.setting_service.model.Company;
import com.zynolo_nexus.setting_service.model.CompanyModuleSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CompanyModuleSubscriptionRepository extends JpaRepository<CompanyModuleSubscription, Long> {

    Optional<CompanyModuleSubscription> findByCompanyAndModuleCodeIgnoreCase(Company company, String moduleCode);

    boolean existsByCompanyAndModuleCodeIgnoreCaseAndIdNot(Company company, String moduleCode, Long id);

    List<CompanyModuleSubscription> findByCompany_IdAndStatus(Long companyId, CompanyModuleSubscriptionStatus status);

    List<CompanyModuleSubscription> findByCompany_Id(Long companyId);
}
