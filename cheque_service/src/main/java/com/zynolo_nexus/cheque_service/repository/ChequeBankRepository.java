package com.zynolo_nexus.cheque_service.repository;

import com.zynolo_nexus.cheque_service.enums.ChequeBankStatus;
import com.zynolo_nexus.cheque_service.model.ChequeBank;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChequeBankRepository extends JpaRepository<ChequeBank, Long> {

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);

    Optional<ChequeBank> findByCodeIgnoreCase(String code);

    List<ChequeBank> findAllByStatusOrderByCodeAsc(ChequeBankStatus status);
}
