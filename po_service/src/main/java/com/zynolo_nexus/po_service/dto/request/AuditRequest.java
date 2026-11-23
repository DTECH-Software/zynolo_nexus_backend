package com.zynolo_nexus.po_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuditRequest {

    @NotBlank(message = "channel is required")
    private String channel;

    @NotBlank(message = "ip is required")
    private String ip;

    @NotBlank(message = "message is required")
    private String message;

    @NotBlank(message = "userAgent is required")
    private String userAgent;

    @NotBlank(message = "username is required")
    private String username;
}
