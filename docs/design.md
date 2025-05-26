Design Documentation
Overview
A scalable HTTP load balancer using Spring Boot and Java 21, designed with clean code, SOLID principles, and design patterns. Supports HTTP parsing, custom routing, and high concurrency with virtual threads.
Architecture

LoadBalancerServer: Manages TCP connections using ServerSocketChannel and virtual threads.
HttpRequestParser: Parses HTTP requests (method, path, headers, body).
LoadBalancingStrategy: Strategy pattern for pluggable load balancing (e.g., RoundRobinStrategy).
RouteHandlerFactory: Factory pattern for selecting route handlers (e.g., StatusRouteHandler, DefaultRouteHandler).
RequestForwarder: Forwards requests to backends with structured concurrency.
LoadBalancerConfig: Configures backends and settings via application.yml.

Design Decisions

Clean Code: Small methods, meaningful names, and consistent error handling.
SOLID Principles:
Single Responsibility: Each class has one purpose (e.g., parsing, forwarding).
Open/Closed: Extensible load balancing and routing via interfaces.
Dependency Inversion: Depend on abstractions like LoadBalancingStrategy.


Design Patterns:
Strategy: For load balancing algorithms.
Factory: For route handlers.
Template Method: Common handling logic in RouteHandler.


Optimizations: Buffered I/O, virtual threads, and structured concurrency for scalability.

Future Extensions

Backend health checks.
Metrics integration with Micrometer.
Advanced load balancing algorithms.
HTTPS support.
Management API endpoints.

