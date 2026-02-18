package com.zynolo_nexus.cheque_service.repository;

import com.zynolo_nexus.cheque_service.model.ChequeCompany;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChequeCompanyRepository extends JpaRepository<ChequeCompany, Long> {

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);

    Optional<ChequeCompany> findByCodeIgnoreCase(String code);
}

