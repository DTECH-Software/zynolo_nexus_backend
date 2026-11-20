package com.zynolo_nexus.fleet_service.repository;

import com.zynolo_nexus.fleet_service.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);
}