package com.zynolo_nexus.cheque_service.repository;

import com.zynolo_nexus.cheque_service.model.ChequeCustomer;
import com.zynolo_nexus.cheque_service.enums.ChequeCustomerStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChequeCustomerRepository extends JpaRepository<ChequeCustomer, Long> {

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);

    Optional<ChequeCustomer> findByCodeIgnoreCase(String code);

    List<ChequeCustomer> findAllByStatusOrderByCodeAsc(ChequeCustomerStatus status);
}
