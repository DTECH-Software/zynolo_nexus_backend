package com.zynolo_nexus.auth_service.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.zynolo_nexus.auth_service.model.PasswordResetToken;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findTopByUsernameAndOtpAndUsedFalseOrderByCreatedAtDesc(String username, String otp);
}
