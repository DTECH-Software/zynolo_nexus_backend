package com.zynolo_nexus.cheque_service.repository;

import com.zynolo_nexus.cheque_service.model.ChequeCustomer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChequeCustomerRepository extends JpaRepository<ChequeCustomer, Long> {

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);
}
