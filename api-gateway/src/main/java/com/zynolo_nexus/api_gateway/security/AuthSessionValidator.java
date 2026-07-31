package com.zynolo_nexus.api_gateway.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Map;

@Component
public class AuthSessionValidator {

    private static final String INTERNAL_HEADER = "X-Internal-Token";

    private final RestTemplate restTemplate;
    private final String validateUrl;
    private final String internalToken;

    public AuthSessionValidator(@Value("${auth.service.url}") String authServiceUrl,
                                @Value("${internal.api.token}") String internalToken) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(2));
        factory.setReadTimeout(Duration.ofSeconds(2));
        this.restTemplate = new RestTemplate(factory);
        this.validateUrl = authServiceUrl.replaceAll("/+$", "") + "/internal/auth-sessions/validate";
        this.internalToken = internalToken;
    }

    public boolean isActive(String username, String sessionId) {
        if (!StringUtils.hasText(username) || !StringUtils.hasText(sessionId)) {
            return false;
        }
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set(INTERNAL_HEADER, internalToken);
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(
                    Map.of("username", username, "sessionId", sessionId),
                    headers
            );
            ResponseEntity<Map> response = restTemplate.postForEntity(validateUrl, entity, Map.class);
            Object active = response.getBody() != null ? response.getBody().get("active") : null;
            return Boolean.TRUE.equals(active);
        } catch (RestClientException ex) {
            return false;
        }
    }
}
