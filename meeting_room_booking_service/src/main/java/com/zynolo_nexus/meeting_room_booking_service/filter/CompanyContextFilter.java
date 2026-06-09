package com.zynolo_nexus.meeting_room_booking_service.filter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zynolo_nexus.meeting_room_booking_service.context.CompanyContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@Component
public class CompanyContextFilter extends OncePerRequestFilter {

    private static final String COMPANY_ID_HEADER = "X-Company-Id";
    private static final String COMPANY_CODE_HEADER = "X-Company-Code";
    private static final String COMPANY_NAME_HEADER = "X-Company-Name";
    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final ObjectMapper objectMapper;

    @Value("${app.default.company-id:1}")
    private Long defaultCompanyId;

    public CompanyContextFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        applyTokenContext(request);
        applyHeaderContext(request);
        applyDefaultCompany();

        try {
            filterChain.doFilter(request, response);
        } finally {
            CompanyContext.clear();
        }
    }

    private void applyTokenContext(HttpServletRequest request) {
        String authHeader = request.getHeader(AUTH_HEADER);
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith(BEARER_PREFIX)) {
            return;
        }
        String token = authHeader.substring(BEARER_PREFIX.length()).trim();
        String[] parts = token.split("\\.");
        if (parts.length < 2) {
            return;
        }
        try {
            byte[] decoded = Base64.getUrlDecoder().decode(addBase64Padding(parts[1]));
            Map<String, Object> claims = objectMapper.readValue(
                    new String(decoded, StandardCharsets.UTF_8),
                    new TypeReference<>() {
                    }
            );
            setCompanyFromClaims(claims);
        } catch (Exception ignored) {
            // Gateway validates the token. Here we only extract company context when available.
        }
    }

    private void applyHeaderContext(HttpServletRequest request) {
        String companyId = request.getHeader(COMPANY_ID_HEADER);
        if (CompanyContext.getCompanyId() == null && StringUtils.hasText(companyId)) {
            try {
                CompanyContext.setCompanyId(Long.parseLong(companyId.trim()));
            } catch (NumberFormatException ex) {
                CompanyContext.setCompanyId(null);
            }
        }
        String companyCode = request.getHeader(COMPANY_CODE_HEADER);
        if (!StringUtils.hasText(CompanyContext.getCompanyCode()) && StringUtils.hasText(companyCode)) {
            CompanyContext.setCompanyCode(companyCode.trim());
        }
        String companyName = request.getHeader(COMPANY_NAME_HEADER);
        if (!StringUtils.hasText(CompanyContext.getCompanyName()) && StringUtils.hasText(companyName)) {
            CompanyContext.setCompanyName(companyName.trim());
        }
    }

    private void applyDefaultCompany() {
        if (CompanyContext.getCompanyId() == null && defaultCompanyId != null) {
            CompanyContext.setCompanyId(defaultCompanyId);
        }
    }

    private void setCompanyFromClaims(Map<String, Object> claims) {
        if (claims == null || claims.isEmpty()) {
            return;
        }

        Long companyId = asLong(firstPresent(
                claims,
                "companyId",
                "company_id",
                "selectedCompanyId",
                "selected_company_id",
                "currentCompanyId",
                "current_company_id",
                "cid"
        ));
        if (companyId == null) {
            companyId = asLong(firstPresentFromNestedCompany(claims, "id", "companyId"));
        }
        if (companyId != null) {
            CompanyContext.setCompanyId(companyId);
        }

        String companyCode = asString(firstPresent(
                claims,
                "companyCode",
                "company_code",
                "selectedCompanyCode",
                "selected_company_code",
                "currentCompanyCode",
                "current_company_code"
        ));
        if (!StringUtils.hasText(companyCode)) {
            companyCode = asString(firstPresentFromNestedCompany(claims, "code", "companyCode"));
        }
        if (StringUtils.hasText(companyCode)) {
            CompanyContext.setCompanyCode(companyCode);
        }

        String companyName = asString(firstPresent(
                claims,
                "companyName",
                "company_name",
                "selectedCompanyName",
                "selected_company_name",
                "currentCompanyName",
                "current_company_name"
        ));
        if (!StringUtils.hasText(companyName)) {
            companyName = asString(firstPresentFromNestedCompany(claims, "name", "description", "companyName"));
        }
        if (StringUtils.hasText(companyName)) {
            CompanyContext.setCompanyName(companyName);
        }
    }

    private Object firstPresent(Map<String, Object> claims, String... keys) {
        for (String key : keys) {
            if (claims.containsKey(key)) {
                return claims.get(key);
            }
        }
        return null;
    }

    private Object firstPresentFromNestedCompany(Map<String, Object> claims, String... keys) {
        Object nested = firstPresent(claims, "company", "selectedCompany", "currentCompany");
        if (!(nested instanceof Map<?, ?> nestedMap)) {
            return null;
        }
        for (String key : keys) {
            if (nestedMap.containsKey(key)) {
                return nestedMap.get(key);
            }
        }
        return null;
    }

    private Long asLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(String.valueOf(value).trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String asString(Object value) {
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value).trim();
        return StringUtils.hasText(text) ? text : null;
    }

    private String addBase64Padding(String value) {
        int padding = (4 - value.length() % 4) % 4;
        return value + "=".repeat(padding);
    }
}
