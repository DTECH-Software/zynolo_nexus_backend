package com.zynolo_nexus.auth_service.repository;

import com.zynolo_nexus.auth_service.model.Page;
import com.zynolo_nexus.auth_service.model.PageTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PageTaskRepository extends JpaRepository<PageTask, Long> {

    Optional<PageTask> findByPageAndCode(Page page, String code);

    List<PageTask> findAllByActiveTrueOrderBySortOrderAsc();

    List<PageTask> findAllByPageAndActiveTrueOrderBySortOrderAsc(Page page);
}
