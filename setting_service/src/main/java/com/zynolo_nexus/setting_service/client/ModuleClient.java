package com.zynolo_nexus.setting_service.client;

import com.zynolo_nexus.contracts.modules.ModuleDto;
import com.zynolo_nexus.contracts.modules.ModuleRequest;
import com.zynolo_nexus.setting_service.dto.api.MessageResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "auth-service-modules", url = "${auth.service.url}")
public interface ModuleClient {

    @PostMapping("/api/v1/modules")
    MessageResponseDTO<ModuleDto> create(@RequestBody ModuleRequest request);

    @PostMapping("/api/v1/modules/{code}/update")
    MessageResponseDTO<ModuleDto> update(@PathVariable("code") String code, @RequestBody ModuleRequest request);

    @PostMapping("/api/v1/modules/list")
    MessageResponseDTO<List<ModuleDto>> getAll();

    @PostMapping("/api/v1/modules/{code}/deactivate")
    MessageResponseDTO<String> deactivate(@PathVariable("code") String code);
}
