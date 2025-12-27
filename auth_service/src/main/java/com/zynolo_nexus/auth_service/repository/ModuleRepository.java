package com.zynolo_nexus.auth_service.repository;

import com.zynolo_nexus.auth_service.model.Module;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ModuleRepository extends JpaRepository<Module, Long> {

    Optional<Module> findByCode(String code);

    List<Module> findAllByActiveTrueOrderBySortOrderAsc();
}
