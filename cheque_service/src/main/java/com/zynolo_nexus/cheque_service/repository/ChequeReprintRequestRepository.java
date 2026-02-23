package com.zynolo_nexus.cheque_service.repository;

import com.zynolo_nexus.cheque_service.enums.ChequeReprintStatus;
import com.zynolo_nexus.cheque_service.model.ChequeReprintRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChequeReprintRequestRepository extends JpaRepository<ChequeReprintRequest, Long> {

    boolean existsByVoucherIdAndStatus(Long voucherId, ChequeReprintStatus status);

    Optional<ChequeReprintRequest> findFirstByVoucherIdAndStatusAndUsedForPrintFalseOrderByApprovedDateDesc(
            Long voucherId,
            ChequeReprintStatus status
    );

    List<ChequeReprintRequest> findAllByStatusOrderByRequestedDateDesc(ChequeReprintStatus status);
}
