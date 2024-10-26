package com.example.history_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@EnableDiscoveryClient
@Slf4j
public class HistoryServiceApplication {
	public static void main(String[] args) {
		// Load environment variables from .env.properties
		Dotenv dotenv = Dotenv.configure()
				.filename(".env.properties") // Specify the .env properties filename
				.load();

		// Set environment variables for the application
		System.setProperty("DB_USERNAME", dotenv.get("DB_USERNAME"));
		System.setProperty("DB_PASSWORD", dotenv.get("DB_PASSWORD"));
		System.setProperty("JWT_SECRET", dotenv.get("JWT_SECRET"));

		SpringApplication.run(HistoryServiceApplication.class, args);
		log.info("History Service is running on port 8082");
	}
}
