
package com.elroi.payment_services;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

@Slf4j
@SpringBootApplication
public class PaymentServicesApplication {

    public static void main(String[] args) {

        // Load .env if present
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();

        dotenv.entries().forEach(entry ->
                System.setProperty(entry.getKey(), entry.getValue())
        );

        // Start Spring Boot
        ConfigurableApplicationContext context =
                SpringApplication.run(PaymentServicesApplication.class, args);

        // Read the configured port
        Environment environment = context.getEnvironment();
        String port = environment.getProperty("server.port", "8080");



        log.info(
                "Payment Services Application started successfully on port: {}",
                port
        );
    }
}
