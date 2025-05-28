package com.example.cdrservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CdrServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CdrServiceApplication.class, args);
	}

}
