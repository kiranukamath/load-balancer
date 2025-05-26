package com.kiran.load_balancer.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.kiran.load_balancer.service.LoadBalancingStrategy;
import com.kiran.load_balancer.service.RoundRobinStrategy;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Configuration
@ConfigurationProperties(prefix = "loadbalancer")
@Data
@Slf4j
public class LoadBalancerConfig {
    private List<String> backends = new ArrayList<>(); // Default to empty list
    private int maxBufferSize = 8192; // Default value
    private String strategy = "round-robin"; // Default strategy
    
    @PostConstruct
    public void validateConfig() {
        if (backends.isEmpty()) {
            log.warn("No backends configured in application.yml. Load balancer will not forward requests.");
        } else {
            log.info("Configured backends: {}", backends);
        }
        log.info("Load balancing strategy: {}", strategy);
        log.info("Max buffer size: {}", maxBufferSize);
    }

    @Bean
    public LoadBalancingStrategy loadBalancingStrategy() {
        if (backends.isEmpty()) {
            log.error("Cannot create load balancing strategy: backends list is empty");
            throw new IllegalStateException("Backends list cannot be empty");
        }
        if ("round-robin".equalsIgnoreCase(strategy)) {
            return new RoundRobinStrategy(backends);
        }
        throw new IllegalArgumentException("Unknown load balancing strategy: " + strategy);
    }
}
