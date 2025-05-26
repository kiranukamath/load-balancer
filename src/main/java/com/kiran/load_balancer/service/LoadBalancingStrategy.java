package com.kiran.load_balancer.service;

public interface LoadBalancingStrategy {
    String selectedBackend();
}