package com.zynolo_nexus.cheque_service.client;

import com.zynolo_nexus.cheque_service.config.FeignInternalAuthConfig;
import com.zynolo_nexus.cheque_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.cheque_service.dto.response.SettingCompanyLookupDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(
        name = "setting-service-for-cheque",
        url = "${setting.service.url}",
        configuration = FeignInternalAuthConfig.class
)
public interface SettingCompanyClient {

    @PostMapping("/api/v1/setting/companies/view")
    MessageResponseDTO<SettingCompanyLookupDto> viewCompany(@RequestBody Map<String, Object> request);
}
