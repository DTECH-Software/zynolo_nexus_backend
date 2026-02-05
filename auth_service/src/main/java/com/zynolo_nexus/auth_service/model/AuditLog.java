package com.zynolo_nexus.auth_service.model;

import com.zynolo_nexus.auth_service.enums.LoginStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50)
    private String username;

    @Column(length = 255)
    private String endpoint;

    @Column(length = 50)
    private String event;

    @Column(length = 20)
    private String channel;

    @Column(length = 45)
    private String ip;

    @Column(length = 255)
    private String userAgent;

    @Column(length = 50)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private LoginStatus status;

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
