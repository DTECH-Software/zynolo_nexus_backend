package com.zynolo_nexus.auth_service.repository;

import com.zynolo_nexus.auth_service.model.Role;
import com.zynolo_nexus.auth_service.model.RoleModuleAccess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface RoleModuleAccessRepository extends JpaRepository<RoleModuleAccess, Long> {

    List<RoleModuleAccess> findByRole(Role role);

    @Transactional
    void deleteByRole(Role role);
}
