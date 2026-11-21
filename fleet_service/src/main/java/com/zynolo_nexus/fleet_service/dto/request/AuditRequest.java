package com.zynolo_nexus.fleet_service.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Audit fields required on every fleet API request body")
public class AuditRequest {

    @NotBlank(message = "channel is required")
    @Schema(example = "WEB", description = "Client channel code")
    private String channel;

    @NotBlank(message = "ip is required")
    @Schema(example = "127.0.0.1")
    private String ip;

    @NotBlank(message = "message is required")
    @Schema(example = "FLVM reference-data")
    private String message;

    @NotBlank(message = "userAgent is required")
    @Schema(example = "Swagger-UI")
    private String userAgent;

    @NotBlank(message = "username is required")
    @Schema(example = "admin", description = "Must match the JWT subject when called via gateway")
    private String username;
}
