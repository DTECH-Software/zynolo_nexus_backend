package com.zynolo_nexus.auth_service.repository;

import com.zynolo_nexus.auth_service.model.Role;
import com.zynolo_nexus.auth_service.model.RolePageTaskAccess;
import com.zynolo_nexus.auth_service.model.PageTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RolePageTaskAccessRepository extends JpaRepository<RolePageTaskAccess, Long> {

    List<RolePageTaskAccess> findByRole(Role role);

    List<RolePageTaskAccess> findByRoleAndCompanyId(Role role, Long companyId);

    Optional<RolePageTaskAccess> findByRoleAndPageTaskAndCompanyId(Role role, PageTask pageTask, Long companyId);

    void deleteByRoleAndPageTaskAndCompanyId(Role role, PageTask pageTask, Long companyId);

    void deleteByPageTask(PageTask pageTask);
}
