package com.zynolo_nexus.po_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.zynolo_nexus.po_service.client")
public class PoServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PoServiceApplication.class, args);
    }
}
