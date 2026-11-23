package com.zynolo_nexus.po_service.repository;

import com.zynolo_nexus.po_service.model.PoRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PoRequestRepository extends JpaRepository<PoRequest, Long>, JpaSpecificationExecutor<PoRequest> {
}
