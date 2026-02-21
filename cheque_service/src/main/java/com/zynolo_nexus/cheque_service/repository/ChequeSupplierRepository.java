package com.zynolo_nexus.cheque_service.repository;

import com.zynolo_nexus.cheque_service.model.ChequeSupplier;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChequeSupplierRepository extends JpaRepository<ChequeSupplier, Long> {

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);
}
