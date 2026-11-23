package com.zynolo_nexus.meeting_room_booking_service.client;

import com.zynolo_nexus.meeting_room_booking_service.context.CompanyContext;
import com.zynolo_nexus.meeting_room_booking_service.dto.client.RolePageTaskAccessDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
public class AuthModuleClient {

    private static final String INTERNAL_HEADER = "X-Internal-Token";
    private static final String COMPANY_HEADER = "X-Company-Id";

    private final RestTemplateBuilder restTemplateBuilder;

    @Value("${auth.service.url:http://localhost:8091}")
    private String authServiceUrl;

    @Value("${internal.api.token:change-me-in-prod}")
    private String internalToken;

    public RolePageTaskAccessDto getRolePageTaskAccess(String roleCode) {
        String url = UriComponentsBuilder.fromHttpUrl(authServiceUrl)
                .path("/internal/role-page-tasks/{roleCode}/get")
                .buildAndExpand(roleCode)
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.set(INTERNAL_HEADER, internalToken);
        Long companyId = CompanyContext.getCompanyId();
        if (companyId != null) {
            headers.set(COMPANY_HEADER, String.valueOf(companyId));
        }

        RestTemplate restTemplate = restTemplateBuilder.build();
        return restTemplate.exchange(
                url,
                HttpMethod.POST,
                new HttpEntity<>(headers),
                RolePageTaskAccessDto.class
        ).getBody();
    }
}
