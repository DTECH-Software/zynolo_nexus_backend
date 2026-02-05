package com.zynolo_nexus.auth_service.repository;

import com.zynolo_nexus.auth_service.model.Module;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ModuleRepository extends JpaRepository<Module, Long> {

    Optional<Module> findByCode(String code);

    @Query("""
            select m from Module m
            where m.status is null or m.status = :status
            order by m.sortOrder asc
            """)
    List<Module> findAllActiveOrderBySortOrderAsc(@Param("status") com.zynolo_nexus.auth_service.enums.ModuleStatus status);
}
