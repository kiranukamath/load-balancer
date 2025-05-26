package com.kiran.load_balancer.service;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Service;

@Service
public class RoundRobinStrategy implements LoadBalancingStrategy{

    private final List<String> backends;
    private final AtomicInteger nextBackend = new AtomicInteger(0);

    public RoundRobinStrategy(List<String> backends) {
        // Validation moved to LoadBalancerConfig to catch earlier
        this.backends = List.copyOf(backends); // Defensive copy for immutability
    }

    @Override
    public String selectedBackend() {
        return backends.get(nextBackend.getAndIncrement()%backends.size());
    }
    
}
