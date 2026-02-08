package com.zynolo_nexus.setting_service.repository;

import com.zynolo_nexus.setting_service.model.UsernamePolicy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsernamePolicyRepository extends JpaRepository<UsernamePolicy, Long> {

    Optional<UsernamePolicy> findTopByOrderByIdAsc();
}
