package com.kiran.load_balancer.handler;

import java.io.IOException;

import org.springframework.stereotype.Component;

import com.kiran.load_balancer.model.HttpRequest;
import com.kiran.load_balancer.service.RequestForwarder;
import com.kiran.load_balancer.service.RouteHandler;

@Component
public class DefaultRouteHandler implements RouteHandler {
    private final RequestForwarder requestForwarder;

    public DefaultRouteHandler(RequestForwarder requestForwarder) {
        this.requestForwarder = requestForwarder;
    }

    @Override
    public boolean canHandle(HttpRequest request) {
        return true; // Fallback handler
    }

    @Override
    public String handle(HttpRequest request) throws IOException {
        return requestForwarder.handle(request);
    }
}