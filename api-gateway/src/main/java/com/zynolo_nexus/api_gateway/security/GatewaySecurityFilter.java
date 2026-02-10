package com.zynolo_nexus.api_gateway.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class GatewaySecurityFilter extends OncePerRequestFilter {

    private static final List<String> BLOCKED_PREFIXES = List.of("/internal/", "/actuator/");
    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/v1/auth/login",
            "/api/v1/auth/logout",
            "/api/v1/auth/forgot-password",
            "/api/v1/auth/verify-reset-otp",
            "/api/v1/auth/reset-password",
            "/api/auth/sso/google"
    );
    private static final String INTERNAL_HEADER = "x-internal-token";
    private static final String COMPANY_HEADER = "x-company-id";
    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtValidator jwtValidator;
    private final ObjectMapper objectMapper;

    public GatewaySecurityFilter(JwtValidator jwtValidator, ObjectMapper objectMapper) {
        this.jwtValidator = jwtValidator;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getServletPath();
        if (isBlocked(path)) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String companyHeaderValue = null;

        if (!isPublic(path)) {
            String authHeader = request.getHeader(AUTH_HEADER);
            if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
            String token = authHeader.substring(BEARER_PREFIX.length());
            var claims = jwtValidator.validateAndGetClaims(token);
            if (claims == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
            Object companyClaim = claims.get("companyId");
            if (companyClaim != null) {
                companyHeaderValue = String.valueOf(companyClaim);
            }

            if ("POST".equalsIgnoreCase(request.getMethod())) {
                CachedBodyHttpServletRequest cached = new CachedBodyHttpServletRequest(request);
                String payloadUsername = extractUsername(cached.getCachedBody());
                String tokenUsername = claims.getSubject();
                if (payloadUsername != null && tokenUsername != null && !payloadUsername.equals(tokenUsername)) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"success\":false,\"message\":\"Invalid username\",\"errorCode\":403}");
                    return;
                }
                request = cached;
            }
        }

        Map<String, String> extraHeaders = companyHeaderValue != null
                ? Map.of(COMPANY_HEADER, companyHeaderValue)
                : Collections.emptyMap();
        HttpServletRequest sanitized = new HeaderRewriteRequestWrapper(
                request,
                extraHeaders,
                INTERNAL_HEADER
        );
        filterChain.doFilter(sanitized, response);
    }

    private String extractUsername(byte[] body) {
        if (body == null || body.length == 0) {
            return null;
        }
        try {
            JsonNode node = objectMapper.readTree(body);
            if (node != null && node.hasNonNull("username")) {
                return node.get("username").asText();
            }
        } catch (IOException ex) {
            return null;
        }
        return null;
    }

    private boolean isBlocked(String path) {
        if (path == null) {
            return false;
        }
        for (String prefix : BLOCKED_PREFIXES) {
            if (path.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    private boolean isPublic(String path) {
        if (path == null) {
            return false;
        }
        for (String p : PUBLIC_PATHS) {
            if (path.startsWith(p)) {
                return true;
            }
        }
        return false;
    }

    private static class HeaderRewriteRequestWrapper extends HttpServletRequestWrapper {

        private final Set<String> blockedHeaders;
        private final Map<String, String> extraHeaders;

        HeaderRewriteRequestWrapper(HttpServletRequest request,
                                    Map<String, String> extraHeaders,
                                    String... headersToStrip) {
            super(request);
            Set<String> headers = new HashSet<>();
            for (String header : headersToStrip) {
                headers.add(header.toLowerCase());
            }
            this.blockedHeaders = Collections.unmodifiableSet(headers);
            Map<String, String> extras = new java.util.HashMap<>();
            if (extraHeaders != null) {
                extraHeaders.forEach((key, value) -> {
                    if (key != null && value != null) {
                        extras.put(key.toLowerCase(), value);
                    }
                });
            }
            this.extraHeaders = Collections.unmodifiableMap(extras);
        }

        @Override
        public String getHeader(String name) {
            if (name != null && blockedHeaders.contains(name.toLowerCase())) {
                return null;
            }
            if (name != null) {
                String value = extraHeaders.get(name.toLowerCase());
                if (value != null) {
                    return value;
                }
            }
            return super.getHeader(name);
        }

        @Override
        public Enumeration<String> getHeaders(String name) {
            if (name != null && blockedHeaders.contains(name.toLowerCase())) {
                return Collections.emptyEnumeration();
            }
            if (name != null) {
                String value = extraHeaders.get(name.toLowerCase());
                if (value != null) {
                    return Collections.enumeration(List.of(value));
                }
            }
            return super.getHeaders(name);
        }

        @Override
        public Enumeration<String> getHeaderNames() {
            Enumeration<String> names = super.getHeaderNames();
            List<String> filtered = Collections.list(names).stream()
                    .filter(n -> !blockedHeaders.contains(n.toLowerCase()))
                    .toList();
            List<String> all = new java.util.ArrayList<>(filtered);
            extraHeaders.keySet().forEach(key -> {
                if (all.stream().noneMatch(existing -> existing.equalsIgnoreCase(key))) {
                    all.add(key);
                }
            });
            return Collections.enumeration(all);
        }
    }
}
