package com.vms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@SpringBootApplication
public class VmsApplication {

	public static void main(String[] args) {
		SpringApplication.run(VmsApplication.class, args);
	}

	@RestController
	@RequestMapping("/api")
	static class HealthController {
		@GetMapping("/health")
		public ResponseEntity<Map<String, String>> health() {
			return ResponseEntity.ok(
			Map.of(
				"status", "UP",
				"message", "server is running"
			)
			);
		}
	}
}
