package com.zynolo_nexus.auth_service.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class InternalApiAuthFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(InternalApiAuthFilter.class);

    @Value("${internal.api.token}")
    private String expectedToken;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getServletPath();
        if (!path.startsWith("/internal/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String tokenInProp = expectedToken != null ? expectedToken.trim() : null;
        String header = request.getHeader("X-Internal-Token");
        boolean match = tokenInProp != null && tokenInProp.equals(header != null ? header.trim() : null);
        log.debug("Internal token check: path={}, headerToken={}, expectedToken={}", path, header, tokenInProp);
        if (match) {
            var auth = new UsernamePasswordAuthenticationToken(
                    "internal-spadmin",
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_SPADMIN"))
            );
            SecurityContextHolder.getContext().setAuthentication(auth);
            filterChain.doFilter(request, response);
        } else {
            log.warn("Internal call rejected: path={}, headerToken={}, expectedTokenSet={}",
                    path, header, tokenInProp != null);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("Forbidden");
        }
    }
}
