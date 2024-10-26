package com.example.history_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@EnableDiscoveryClient
@Slf4j
public class HistoryServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(HistoryServiceApplication.class, args);
		log.info("History Service is running on port 8082");
	}
}
