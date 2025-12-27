package com.zynolo_nexus.auth_service.repository;

import com.zynolo_nexus.auth_service.model.Page;
import com.zynolo_nexus.auth_service.model.Section;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PageRepository extends JpaRepository<Page, Long> {

    Optional<Page> findByCode(String code);

    List<Page> findAllByActiveTrueOrderBySortOrderAsc();

    List<Page> findAllBySectionAndActiveTrueOrderBySortOrderAsc(Section section);
}
