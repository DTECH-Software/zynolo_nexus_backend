package com.zynolo_nexus.cheque_service.controller;

import com.zynolo_nexus.cheque_service.dto.api.MessageResponseDTO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/cheque")
public class ChequeHealthController {

    @PostMapping("/health")
    public MessageResponseDTO<Map<String, String>> health() {
        return MessageResponseDTO.<Map<String, String>>builder()
                .success(true)
                .message("Cheque service is up")
                .data(Map.of("service", "cheque_service", "status", "UP"))
                .errors(null)
                .errorCode(0)
                .responseTime(LocalDateTime.now())
                .build();
    }
}

