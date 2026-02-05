package com.zynolo_nexus.auth_service.filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zynolo_nexus.auth_service.model.AuditLog;
import com.zynolo_nexus.auth_service.repository.AuditLogRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
public class AuditLogFilter extends OncePerRequestFilter {

    private static final Map<String, String> POST_EVENTS = Map.ofEntries(
            Map.entry("/api/v1/auth/login", "AUTH_LOGIN"),
            Map.entry("/api/v1/auth/logout", "AUTH_LOGOUT"),
            Map.entry("/api/v1/auth/forgot-password", "AUTH_FORGOT_PASSWORD"),
            Map.entry("/api/v1/auth/verify-reset-otp", "AUTH_VERIFY_RESET_OTP"),
            Map.entry("/api/v1/auth/reset-password", "AUTH_RESET_PASSWORD"),
            Map.entry("/api/auth/sso/google", "AUTH_SSO_GOOGLE"),
            Map.entry("/api/v1/modules", "MODULE_CREATE"),
            Map.entry("/api/v1/sections", "SECTION_CREATE"),
            Map.entry("/api/v1/pages", "PAGE_CREATE"),
            Map.entry("/api/v1/page-tasks", "PAGE_TASK_CREATE"),
            Map.entry("/internal/modules", "INTERNAL_MODULE_CREATE"),
            Map.entry("/internal/sections", "INTERNAL_SECTION_CREATE"),
            Map.entry("/internal/pages", "INTERNAL_PAGE_CREATE"),
            Map.entry("/internal/page-tasks", "INTERNAL_PAGE_TASK_CREATE")
    );

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    public AuditLogFilter(AuditLogRepository auditLogRepository, ObjectMapper objectMapper) {
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        ContentCachingRequestWrapper wrapped = new ContentCachingRequestWrapper(request);
        filterChain.doFilter(wrapped, response);

        String body = new String(wrapped.getContentAsByteArray(), StandardCharsets.UTF_8);
        JsonNode node = parseJson(body);

        String endpoint = request.getServletPath();
        String event = POST_EVENTS.getOrDefault(endpoint, "POST_" + sanitize(endpoint));

        AuditLog log = AuditLog.builder()
                .username(readText(node, "username"))
                .channel(readText(node, "channel"))
                .ip(readText(node, "ip"))
                .userAgent(readText(node, "userAgent"))
                .message(readText(node, "message"))
                .endpoint(endpoint)
                .event(event)
                .build();
        auditLogRepository.save(log);
    }

    private JsonNode parseJson(String body) {
        if (body == null || body.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readTree(body);
        } catch (IOException ex) {
            return null;
        }
    }

    private String readText(JsonNode node, String field) {
        if (node == null || !node.hasNonNull(field)) {
            return null;
        }
        return node.get(field).asText();
    }

    private String sanitize(String path) {
        return path == null ? "UNKNOWN" : path.replace("/", "_");
    }
}
