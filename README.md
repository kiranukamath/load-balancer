# load-balancer


1. explain HttpRequestParser class parseRequest in detail. I want to understand what is happening in everyline. 
2. what happens when nextBackend.getAndIncrement() reaches max integer value? how will it work?
3. System.arraycopy mean? why do we use this?
4. backendChannel.write(ByteBuffer.wrap(fullRequest)) what does this do? are we not using virtual thread to make external call?
5. ByteArrayOutputStream ByteBuffer, understand these two in detail.


Spring Boot HTTP Load Balancer
A production-grade HTTP load balancer built with Spring Boot and Java 21, leveraging virtual threads, structured concurrency, and a custom HTTP parser. Follows clean code practices, SOLID principles, and design patterns for scalability and extensibility.
Features

Distributes HTTP requests using a pluggable load balancing strategy (default: round-robin).
Parses HTTP request lines, headers, and bodies with robust error handling.
Supports custom API endpoints (e.g., /api/status) via a route handler factory.
Handles large data with buffered socket I/O.
Uses Java 21 virtual threads for thousands of concurrent connections.
Employs structured concurrency for reliable backend communication.
Configurable via application.yml.

Prerequisites

Java 21
Gradle
Backend servers (e.g., Python http.server on ports 8081 and 8082)

Setup

Start backend servers:python3 -m http.server 8081
python3 -m http.server 8082


Build the project:./gradlew build


Run the application:./gradlew bootRun



Testing

Load balancing:curl http://localhost:8080


Status endpoint:curl http://localhost:8080/api/status


Load test:ab -n 1000 -c 100 http://localhost:8080/



Future Enhancements

Health checks for backend servers.
Metrics with Micrometer and Prometheus.
Advanced load balancing (e.g., least connections).
HTTPS support with TLS/SSL.
Additional API endpoints.

Technologies

Java 21 (Virtual Threads, Structured Concurrency)
Spring Boot 3.x
Gradle
SLF4J/Logback
Java NIO


