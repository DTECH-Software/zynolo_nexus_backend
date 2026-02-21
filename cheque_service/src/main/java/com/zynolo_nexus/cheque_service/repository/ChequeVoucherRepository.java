package com.zynolo_nexus.cheque_service.repository;

import com.zynolo_nexus.cheque_service.model.ChequeVoucher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface ChequeVoucherRepository extends JpaRepository<ChequeVoucher, Long> {

    boolean existsByVoucherNo(String voucherNo);

    long countByCreatedDateBetween(LocalDateTime from, LocalDateTime to);
}
