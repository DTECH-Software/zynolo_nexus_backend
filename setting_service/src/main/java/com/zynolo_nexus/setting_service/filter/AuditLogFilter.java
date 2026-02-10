package com.zynolo_nexus.setting_service.filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zynolo_nexus.setting_service.model.AuditLog;
import com.zynolo_nexus.setting_service.repository.AuditLogRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.util.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
public class AuditLogFilter extends OncePerRequestFilter {

    private static final Map<String, String> POST_EVENTS = Map.ofEntries(
            Map.entry("/api/v1/setting/users", "USER_CREATE"),
            Map.entry("/api/v1/setting/companies", "COMPANY_CREATE"),
            Map.entry("/api/v1/setting/modules", "MODULE_CREATE"),
            Map.entry("/api/v1/setting/modules/update", "MODULE_UPDATE"),
            Map.entry("/api/v1/setting/modules/view", "MODULE_VIEW"),
            Map.entry("/api/v1/setting/modules/status", "MODULE_STATUS_UPDATE"),
            Map.entry("/api/v1/setting/modules/filter-list", "MODULE_FILTER_LIST"),
            Map.entry("/api/v1/setting/modules/reference-data", "MODULE_REFERENCE_DATA"),
            Map.entry("/api/v1/setting/sections", "SECTION_CREATE"),
            Map.entry("/api/v1/setting/sections/update", "SECTION_UPDATE"),
            Map.entry("/api/v1/setting/sections/view", "SECTION_VIEW"),
            Map.entry("/api/v1/setting/sections/status", "SECTION_STATUS_UPDATE"),
            Map.entry("/api/v1/setting/sections/filter-list", "SECTION_FILTER_LIST"),
            Map.entry("/api/v1/setting/sections/reference-data", "SECTION_REFERENCE_DATA"),
            Map.entry("/api/v1/setting/pages", "PAGE_CREATE"),
            Map.entry("/api/v1/setting/pages/update", "PAGE_UPDATE"),
            Map.entry("/api/v1/setting/pages/view", "PAGE_VIEW"),
            Map.entry("/api/v1/setting/pages/status", "PAGE_STATUS_UPDATE"),
            Map.entry("/api/v1/setting/pages/filter-list", "PAGE_FILTER_LIST"),
            Map.entry("/api/v1/setting/pages/reference-data", "PAGE_REFERENCE_DATA"),
            Map.entry("/api/v1/setting/password-policy/reference-data", "PASSWORD_POLICY_REFERENCE_DATA"),
            Map.entry("/api/v1/setting/password-policy/view", "PASSWORD_POLICY_VIEW"),
            Map.entry("/api/v1/setting/password-policy/update", "PASSWORD_POLICY_UPDATE"),
            Map.entry("/api/v1/setting/password-policy/reset", "PASSWORD_POLICY_RESET"),
            Map.entry("/api/v1/setting/username-policy/reference-data", "USERNAME_POLICY_REFERENCE_DATA"),
            Map.entry("/api/v1/setting/username-policy/view", "USERNAME_POLICY_VIEW"),
            Map.entry("/api/v1/setting/username-policy/update", "USERNAME_POLICY_UPDATE"),
            Map.entry("/api/v1/setting/username-policy/reset", "USERNAME_POLICY_RESET"),
            Map.entry("/api/v1/setting/roles", "ROLE_CREATE"),
            Map.entry("/api/v1/setting/roles/update", "ROLE_UPDATE"),
            Map.entry("/api/v1/setting/roles/view", "ROLE_VIEW"),
            Map.entry("/api/v1/setting/roles/status", "ROLE_STATUS_UPDATE"),
            Map.entry("/api/v1/setting/roles/filter-list", "ROLE_FILTER_LIST"),
            Map.entry("/api/v1/setting/roles/reference-data", "ROLE_REFERENCE_DATA"),
            Map.entry("/api/v1/setting/roles/delete", "ROLE_DELETE"),
            Map.entry("/api/v1/setting/tasks", "TASK_CREATE"),
            Map.entry("/api/v1/setting/tasks/update", "TASK_UPDATE"),
            Map.entry("/api/v1/setting/tasks/view", "TASK_VIEW"),
            Map.entry("/api/v1/setting/tasks/status", "TASK_STATUS_UPDATE"),
            Map.entry("/api/v1/setting/tasks/filter-list", "TASK_FILTER_LIST"),
            Map.entry("/api/v1/setting/tasks/reference-data", "TASK_REFERENCE_DATA"),
            Map.entry("/api/v1/setting/page-tasks", "PAGE_TASK_CREATE"),
            Map.entry("/api/v1/setting/role-modules", "ROLE_MODULE_ACCESS_UPDATE"),
            Map.entry("/api/v1/setting/role-page-tasks", "ROLE_PAGE_TASK_ACCESS_UPDATE")
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

        String authenticatedUsername = getAuthenticatedUsername();
        String payloadUsername = readText(node, "username");
        String channel = readText(node, "channel");
        String ip = readText(node, "ip");
        String userAgent = readText(node, "userAgent");
        String message = readText(node, "message");

        if (!StringUtils.hasText(channel)) {
            channel = getHeaderValue(request, "X-Channel", "Channel");
        }
        if (!StringUtils.hasText(ip)) {
            ip = resolveClientIp(request);
        }
        if (!StringUtils.hasText(userAgent)) {
            userAgent = request.getHeader("User-Agent");
        }
        if (!StringUtils.hasText(message)) {
            message = getHeaderValue(request, "X-Message", "Message");
        }
        if (!StringUtils.hasText(message)) {
            message = event;
        }

        AuditLog log = AuditLog.builder()
                .username(authenticatedUsername != null ? authenticatedUsername : payloadUsername)
                .channel(channel)
                .ip(ip)
                .userAgent(userAgent)
                .message(message)
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

    private String getAuthenticatedUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal == null || "anonymousUser".equals(principal)) {
            return null;
        }
        return authentication.getName();
    }

    private String getHeaderValue(HttpServletRequest request, String... names) {
        if (request == null || names == null) {
            return null;
        }
        for (String name : names) {
            String value = request.getHeader(name);
            if (StringUtils.hasText(value)) {
                return value.trim();
            }
        }
        return null;
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = getHeaderValue(request, "X-Forwarded-For");
        if (StringUtils.hasText(forwarded)) {
            String[] parts = forwarded.split(",");
            if (parts.length > 0 && StringUtils.hasText(parts[0])) {
                return parts[0].trim();
            }
        }
        String realIp = getHeaderValue(request, "X-Real-IP");
        if (StringUtils.hasText(realIp)) {
            return realIp;
        }
        return request != null ? request.getRemoteAddr() : null;
    }
}
