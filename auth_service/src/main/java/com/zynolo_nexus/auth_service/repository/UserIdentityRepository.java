package com.zynolo_nexus.auth_service.repository;

import com.zynolo_nexus.auth_service.model.UserIdentity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserIdentityRepository extends JpaRepository<UserIdentity, Long> {

    Optional<UserIdentity> findByProviderAndProviderSubject(String provider, String providerSubject);
}
