package com.zynolo_nexus.po_service.repository;

import com.zynolo_nexus.po_service.enums.MasterStatus;
import com.zynolo_nexus.po_service.model.Product;
import com.zynolo_nexus.po_service.model.Vendor;
import com.zynolo_nexus.po_service.model.VendorProductMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface VendorProductMappingRepository extends JpaRepository<VendorProductMapping, Long>, JpaSpecificationExecutor<VendorProductMapping> {

    Optional<VendorProductMapping> findByVendorAndProduct(Vendor vendor, Product product);

    Optional<VendorProductMapping> findByVendorAndProductAndStatus(Vendor vendor, Product product, MasterStatus status);

    List<VendorProductMapping> findAllByVendorAndStatusOrderByIdAsc(Vendor vendor, MasterStatus status);
}
