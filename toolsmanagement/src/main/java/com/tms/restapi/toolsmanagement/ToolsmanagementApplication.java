package com.tms.restapi.toolsmanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ToolsmanagementApplication {

	public static void main(String[] args) {
		SpringApplication.run(ToolsmanagementApplication.class, args);
	}

}
