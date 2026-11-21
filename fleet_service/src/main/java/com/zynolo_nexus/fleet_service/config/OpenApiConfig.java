package com.zynolo_nexus.fleet_service.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    public static final String FLEET_API_PREFIX = "/api/v1/fleet";
    public static final String BEARER_SCHEME = "bearerAuth";
    public static final String COMPANY_HEADER_SCHEME = "companyIdHeader";

    @Bean
    public OpenAPI fleetOpenApi() {
        return new OpenAPI()
                .servers(List.of(new Server()
                        .url("/")
                        .description("API gateway (port 8099) — Try it out hits the same host you opened Swagger on")))
                .info(new Info()
                        .title("Zynolo Fleet Service")
                        .description("""
                                Live API contract for Fleet Master pages and related endpoints.
                                Open Swagger via api-gateway: `/api/v1/fleet/swagger-ui.html`.
                                All business APIs use POST with `MessageResponseDTO` envelopes.
                                Request bodies extend `AuditRequest` (channel, ip, message, userAgent, username).
                                Authorize with JWT; gateway injects `X-Company-Id` from the token on API calls.
                                """)
                        .version("1.0.0")
                        .contact(new Contact().name("Zynolo Nexus")))
                .components(new Components()
                        .addSecuritySchemes(BEARER_SCHEME, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT from auth login (gateway validates; fleet trusts gateway in deploy)"))
                        .addSecuritySchemes(COMPANY_HEADER_SCHEME, new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name("X-Company-Id")
                                .description("Tenant company id; falls back to app.default.company-id when omitted locally")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME))
                .addSecurityItem(new SecurityRequirement().addList(COMPANY_HEADER_SCHEME));
    }
}