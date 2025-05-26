package com.kiran.load_balancer.handler;

import java.util.List;

import org.springframework.stereotype.Component;

import com.kiran.load_balancer.model.HttpRequest;
import com.kiran.load_balancer.service.RouteHandler;

@Component
public class RouteHandlerFactory {
    private final List<RouteHandler> handlers;

    public RouteHandlerFactory(List<RouteHandler> handlers) {
        this.handlers = handlers;
    }

    public RouteHandler getHandler(HttpRequest request) {
        return handlers.stream()
                .filter(handler -> handler.canHandle(request))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No handler found for request: " + request.path()));
    }
}