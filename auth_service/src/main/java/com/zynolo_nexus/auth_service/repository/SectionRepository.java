package com.zynolo_nexus.auth_service.repository;

import com.zynolo_nexus.auth_service.model.Module;
import com.zynolo_nexus.auth_service.model.Section;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SectionRepository extends JpaRepository<Section, Long> {

    Optional<Section> findByCode(String code);

    List<Section> findAllByActiveTrueOrderBySortOrderAsc();

    List<Section> findAllByModuleAndActiveTrueOrderBySortOrderAsc(Module module);
}
