package com.zynolo_nexus.setting_service.repository;

import com.zynolo_nexus.setting_service.model.PasswordPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordPolicyRepository extends JpaRepository<PasswordPolicy, Long> {

    Optional<PasswordPolicy> findTopByOrderByIdAsc();
}
