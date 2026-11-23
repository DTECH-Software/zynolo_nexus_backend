package com.zynolo_nexus.auth_service.repository;

import com.zynolo_nexus.auth_service.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {

    Optional<Task> findByCode(String code);

    List<Task> findAllByActiveTrueOrderBySortOrderAsc();

    List<Task> findAllByOrderBySortOrderAsc();
}
