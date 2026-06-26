package com.zynolo_nexus.po_service.repository;

import com.zynolo_nexus.po_service.model.MeetingVendorLookup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MeetingVendorLookupRepository extends JpaRepository<MeetingVendorLookup, Long> {

    List<MeetingVendorLookup> findAllByActiveOrderByVendorCodeAsc(Boolean active);
}
