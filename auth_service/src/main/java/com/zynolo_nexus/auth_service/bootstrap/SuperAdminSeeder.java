package com.zynolo_nexus.auth_service.bootstrap;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.zynolo_nexus.auth_service.enums.LoginStatus;
import com.zynolo_nexus.auth_service.enums.RoleCode;
import com.zynolo_nexus.auth_service.enums.UserStatus;
import com.zynolo_nexus.auth_service.model.Role;
import com.zynolo_nexus.auth_service.model.User;
import com.zynolo_nexus.auth_service.repository.RoleRepository;
import com.zynolo_nexus.auth_service.repository.UserRepository;

@Component
public class SuperAdminSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${seed.superadmin.enabled:true}")
    private boolean enabled;

    @Value("${seed.superadmin.username:superadmin}")
    private String username;

    @Value("${seed.superadmin.password:Super@123}")
    private String password;

    @Value("${seed.superadmin.email:superadmin@example.com}")
    private String email;

    @Value("${seed.superadmin.mobile:0763366802}")
    private String mobile;

    public SuperAdminSeeder(UserRepository userRepository,
                            RoleRepository roleRepository,
                            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (!enabled) {
            return;
        }

        if (userRepository.existsByUsername(username)) {
            return;
        }

        Role role = roleRepository.findByCode(RoleCode.SPADMIN)
                .orElseThrow(() -> new IllegalStateException("SPADMIN role not found. Seed user cannot be created."));

        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .email(email)
                .mobile(mobile)
                .firstName("Super")
                .lastName("Admin")
                .status(UserStatus.ACTIVE)
                .loginStatus(LoginStatus.ACTIVE)
                .passwordExpiredDate(LocalDate.now().plusDays(30))
                .expectingFirstTimeLogging(true)
                .reset(false)
                .role(role)
                .build();

        user.setCreatedDate(LocalDateTime.now());
        user.setLastModifiedDate(LocalDateTime.now());
        user.setCreatedBy("SYSTEM");
        user.setLastModifiedBy("SYSTEM");

        userRepository.save(user);
    }
}
