package com.zynolo_nexus.auth_service.filter;

import com.zynolo_nexus.auth_service.context.CompanyContext;
import com.zynolo_nexus.auth_service.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class CompanyContextFilter extends OncePerRequestFilter {

    private static final String COMPANY_HEADER = "X-Company-Id";
    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtUtil jwtUtil;

    public CompanyContextFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String header = request.getHeader(COMPANY_HEADER);
        if (StringUtils.hasText(header)) {
            try {
                CompanyContext.setCompanyId(Long.parseLong(header.trim()));
            } catch (NumberFormatException ex) {
                CompanyContext.setCompanyId(null);
            }
        } else {
            String authHeader = request.getHeader(AUTH_HEADER);
            if (StringUtils.hasText(authHeader) && authHeader.startsWith(BEARER_PREFIX)) {
                String token = authHeader.substring(BEARER_PREFIX.length());
                if (jwtUtil.validateAccessToken(token)) {
                    CompanyContext.setCompanyId(jwtUtil.getCompanyId(token));
                }
            }
        }
        try {
            filterChain.doFilter(request, response);
        } finally {
            CompanyContext.clear();
        }
    }
}
