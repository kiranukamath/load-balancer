package com.kiran.load_balancer.handler;

import org.springframework.stereotype.Component;

import com.kiran.load_balancer.model.HttpRequest;
import com.kiran.load_balancer.service.RouteHandler;

@Component
public class StatusRouteHandler implements RouteHandler {
    private static final String STATUS_PATH = "/api/status";

    @Override
    public boolean canHandle(HttpRequest request) {
        return STATUS_PATH.equals(request.path());
    }

    @Override
    public String handle(HttpRequest request) {
        return """
                HTTP/1.1 200 OK\r
                Content-Type: application/json\r
                Content-Length: 30\r
                \r
                {"status":"Load Balancer OK"}\r
                """;
    }

}