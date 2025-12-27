package com.zynolo_nexus.setting_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.zynolo_nexus.setting_service.client")
public class SettingServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(SettingServiceApplication.class, args);
	}

}
