package com.zynolo_nexus.auth_service.repository;

import com.zynolo_nexus.auth_service.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Long> {

    Optional<Company> findByCode(String code);
}
