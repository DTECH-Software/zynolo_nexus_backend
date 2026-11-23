package com.zynolo_nexus.auth_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;

@Configuration
public class GoogleOidcConfig {

    @Value("${oauth.google.issuer:https://accounts.google.com}")
    private String issuer;

    @Bean
    public JwtDecoder googleJwtDecoder() {
        return JwtDecoders.fromIssuerLocation(issuer);
    }
}
