package com.zynolo_nexus.fleet_service.controller;

import com.zynolo_nexus.fleet_service.dto.api.MessageResponseDTO;
import com.zynolo_nexus.fleet_service.service.support.ResponseMessageHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/fleet")
public class FleetHealthController {

    private final ResponseMessageHelper messages;

    @PostMapping("/health")
    public MessageResponseDTO<Map<String, String>> health() {
        return MessageResponseDTO.<Map<String, String>>builder()
                .success(true)
                .message(messages.get("fleet.health.success"))
                .data(Map.of("service", "fleet_service", "status", "UP"))
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }
}