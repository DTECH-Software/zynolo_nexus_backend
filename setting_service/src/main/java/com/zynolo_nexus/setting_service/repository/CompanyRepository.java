package com.zynolo_nexus.setting_service.repository;

import com.zynolo_nexus.setting_service.enums.CompanyStatus;
import com.zynolo_nexus.setting_service.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Long> {

    Optional<Company> findByCode(String code);

    boolean existsByCode(String code);

    List<Company> findAllByStatusOrderByCodeAsc(CompanyStatus status);
}
