package com.zynolo_nexus.setting_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.zynolo_nexus.setting_service.filter.AuditLogFilter;
import com.zynolo_nexus.setting_service.filter.CompanyContextFilter;

@Configuration
public class SecurityConfig {

    private final AuditLogFilter auditLogFilter;
    private final CompanyContextFilter companyContextFilter;

    public SecurityConfig(AuditLogFilter auditLogFilter, CompanyContextFilter companyContextFilter) {
        this.auditLogFilter = auditLogFilter;
        this.companyContextFilter = companyContextFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .addFilterBefore(companyContextFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(auditLogFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
