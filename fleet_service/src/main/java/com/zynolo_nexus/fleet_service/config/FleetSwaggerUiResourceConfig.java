package com.zynolo_nexus.fleet_service.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Serves Swagger UI static assets under the same prefix as fleet APIs so the gateway can proxy everything on 8099.
 */
@Configuration
public class FleetSwaggerUiResourceConfig implements WebMvcConfigurer {

    public static final String FLEET_DOCS_UI_PATTERN = OpenApiConfig.FLEET_API_PREFIX + "/swagger-ui/**";

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(FLEET_DOCS_UI_PATTERN)
                .addResourceLocations("classpath:/META-INF/resources/webjars/swagger-ui/")
                .resourceChain(false);
    }
}