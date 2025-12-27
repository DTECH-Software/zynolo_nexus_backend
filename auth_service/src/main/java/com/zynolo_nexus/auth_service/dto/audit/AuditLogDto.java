package com.zynolo_nexus.auth_service.dto.audit;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class AuditLogDto {
    private String action;
    private String performedBy;
    private LocalDateTime performedAt;
    private String ip;
}
