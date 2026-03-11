package com.zynolo_nexus.po_service.repository;

import com.zynolo_nexus.po_service.enums.MasterStatus;
import com.zynolo_nexus.po_service.model.Currency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface CurrencyRepository extends JpaRepository<Currency, Long>, JpaSpecificationExecutor<Currency> {

    boolean existsByCodeIgnoreCase(String code);

    List<Currency> findAllByStatusOrderByCodeAsc(MasterStatus status);

    Optional<Currency> findByCodeIgnoreCaseAndStatus(String code, MasterStatus status);
}
