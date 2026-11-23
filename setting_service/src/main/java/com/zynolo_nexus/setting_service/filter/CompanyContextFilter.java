package com.zynolo_nexus.setting_service.filter;

import com.zynolo_nexus.setting_service.context.CompanyContext;
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
        }
        try {
            filterChain.doFilter(request, response);
        } finally {
            CompanyContext.clear();
        }
    }
}
