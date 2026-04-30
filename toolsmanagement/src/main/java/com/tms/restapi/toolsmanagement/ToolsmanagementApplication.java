package com.tms.restapi.toolsmanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main application class for the Tools Management System REST API.
 *
 * This Spring Boot application provides a comprehensive system for managing tools, kits,
 * and their issuance in a training/manufacturing environment. It includes features like:
 * - User authentication and authorization (Admin, Trainer, Security roles)
 * - Tool and kit inventory management
 * - Issuance and return tracking
 * - Real-time notifications via WebSockets
 * - Reporting and analytics
 * - Chatbot for user queries
 *
 * The application uses MSSQL database with Flyway migrations, JWT authentication,
 * and follows a layered architecture with controllers, services, repositories, and models.
 */
@SpringBootApplication
@EnableScheduling
public class ToolsmanagementApplication {

	/**
	 * Main method that starts the Spring Boot application.
	 *
	 * This method bootstraps the application by running the SpringApplication
	 * with the ToolsmanagementApplication class as the main configuration class.
	 * It enables auto-configuration, component scanning, and starts the embedded
	 * web server (Tomcat by default).
	 *
	 * @param args command line arguments passed to the application
	 */
	public static void main(String[] args) {
		SpringApplication.run(ToolsmanagementApplication.class, args);
	}

}
