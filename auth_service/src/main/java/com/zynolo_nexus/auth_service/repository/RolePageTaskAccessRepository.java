package com.zynolo_nexus.auth_service.repository;

import com.zynolo_nexus.auth_service.model.Role;
import com.zynolo_nexus.auth_service.model.RolePageTaskAccess;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RolePageTaskAccessRepository extends JpaRepository<RolePageTaskAccess, Long> {

    List<RolePageTaskAccess> findByRole(Role role);
}
