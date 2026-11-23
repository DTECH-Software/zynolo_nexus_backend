package com.zynolo_nexus.cheque_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.zynolo_nexus.cheque_service.client")
public class ChequeServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChequeServiceApplication.class, args);
    }
}

