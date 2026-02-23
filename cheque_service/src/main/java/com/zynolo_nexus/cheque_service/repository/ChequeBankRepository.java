package com.zynolo_nexus.cheque_service.repository;

import com.zynolo_nexus.cheque_service.model.ChequeBank;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChequeBankRepository extends JpaRepository<ChequeBank, Long> {

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);
}
