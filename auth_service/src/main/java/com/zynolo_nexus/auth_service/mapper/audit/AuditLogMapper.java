package com.zynolo_nexus.auth_service.mapper.audit;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Component;

import com.zynolo_nexus.auth_service.dto.audit.AuditLogDto;
import com.zynolo_nexus.auth_service.enums.LoginStatus;

@Component
public class AuditLogMapper {

    public AuditLogDto buildLoginAudit(String actor, LoginStatus status, Map<String, Object> metadata) {
        AuditLogDto dto = new AuditLogDto();
        dto.setAction(status != null ? status.name() : "LOGIN");
        dto.setPerformedBy(actor);
        dto.setPerformedAt(LocalDateTime.now());
        String ip = metadata != null ? Objects.toString(metadata.get("ip"), null) : null;
        dto.setIp(ip);
        return dto;
    }
}
