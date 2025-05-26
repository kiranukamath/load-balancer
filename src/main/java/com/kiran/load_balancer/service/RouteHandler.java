package com.kiran.load_balancer.service;

import java.io.IOException;

import com.kiran.load_balancer.model.HttpRequest;

public interface RouteHandler {
    boolean canHandle(HttpRequest request);
    String handle(HttpRequest request) throws IOException;
}
