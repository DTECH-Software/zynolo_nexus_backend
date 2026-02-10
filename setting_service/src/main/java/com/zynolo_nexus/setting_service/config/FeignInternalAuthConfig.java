package com.zynolo_nexus.setting_service.config;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.zynolo_nexus.setting_service.context.CompanyContext;

@Configuration
public class FeignInternalAuthConfig {

    @Value("${internal.api.token}")
    private String internalToken;

    @Bean
    public RequestInterceptor internalAuthInterceptor() {
        return template -> {
            if (internalToken != null) {
                template.header("X-Internal-Token", internalToken);
            }
            Long companyId = CompanyContext.getCompanyId();
            if (companyId != null) {
                template.header("X-Company-Id", String.valueOf(companyId));
            }
        };
    }
}
