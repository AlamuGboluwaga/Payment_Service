package com.elroi.payment_services.payment.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class PaystackConfig {

    @Bean
    RestClient paystackRestClient(@Value("${paystack.secret-key}") String secretKey) {

        return RestClient.builder().baseUrl("https://api.paystack.co").defaultHeader("Authorization", "Bearer " + secretKey).defaultHeader("Content-Type", "application/json").build();
    }
}
