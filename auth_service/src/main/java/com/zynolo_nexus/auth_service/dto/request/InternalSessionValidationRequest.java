package com.zynolo_nexus.auth_service.dto.request;

import lombok.Data;

@Data
public class InternalSessionValidationRequest {
    private String username;
    private String sessionId;
}
