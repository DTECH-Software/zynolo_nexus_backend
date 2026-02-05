package com.zynolo_nexus.api_gateway.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class GatewaySecurityFilter extends OncePerRequestFilter {

    private static final List<String> BLOCKED_PREFIXES = List.of("/internal/", "/actuator/");
    private static final String INTERNAL_HEADER = "x-internal-token";

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

        HttpServletRequest sanitized = new HeaderStripRequestWrapper(request, INTERNAL_HEADER);
        filterChain.doFilter(sanitized, response);
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

    private static class HeaderStripRequestWrapper extends HttpServletRequestWrapper {

        private final Set<String> blockedHeaders;

        HeaderStripRequestWrapper(HttpServletRequest request, String... headersToStrip) {
            super(request);
            Set<String> headers = new HashSet<>();
            for (String header : headersToStrip) {
                headers.add(header.toLowerCase());
            }
            this.blockedHeaders = Collections.unmodifiableSet(headers);
        }

        @Override
        public String getHeader(String name) {
            if (name != null && blockedHeaders.contains(name.toLowerCase())) {
                return null;
            }
            return super.getHeader(name);
        }

        @Override
        public Enumeration<String> getHeaders(String name) {
            if (name != null && blockedHeaders.contains(name.toLowerCase())) {
                return Collections.emptyEnumeration();
            }
            return super.getHeaders(name);
        }

        @Override
        public Enumeration<String> getHeaderNames() {
            Enumeration<String> names = super.getHeaderNames();
            List<String> filtered = Collections.list(names).stream()
                    .filter(n -> !blockedHeaders.contains(n.toLowerCase()))
                    .toList();
            return Collections.enumeration(filtered);
        }
    }
}
