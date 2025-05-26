package com.kiran.load_balancer.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Executors;
import java.util.concurrent.StructuredTaskScope;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import com.kiran.load_balancer.config.LoadBalancerConfig;
import com.kiran.load_balancer.handler.RouteHandlerFactory;
import com.kiran.load_balancer.model.HttpRequest;
import com.kiran.load_balancer.service.HttpRequestParser;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class LoadBalancerServer implements CommandLineRunner{

    private final HttpRequestParser requestParser;
    private final RouteHandlerFactory routeHandlerFactory;
    private final int port;
    private final LoadBalancerConfig config;

    public LoadBalancerServer(HttpRequestParser requestParser, RouteHandlerFactory routeHandlerFactory,
                             @Value("${server.port:8080}") int port,
                             LoadBalancerConfig config) {
        this.requestParser = requestParser;
        this.routeHandlerFactory = routeHandlerFactory;
        this.port = port;
        this.config = config;
    }
    
    @Override
    public void run(String... args) throws Exception {
        try(ServerSocketChannel serverSocket = ServerSocketChannel.open()){
            serverSocket.bind(new InetSocketAddress(port));
            log.info("Load balancer started on port {}", port);
            try(var executor = Executors.newVirtualThreadPerTaskExecutor()){
                while(true){
                    SocketChannel client = serverSocket.accept();
                    log.info("Accepted connection from {}", client.getRemoteAddress());
                    executor.submit(() -> handleClient(client));
                }
            }
        }
    }

    private void handleClient(SocketChannel client) {
        try (client) {
            HttpRequest request = requestParser.parseRequest(client);
            String response;

            // Check if backends are available for non-status routes
            if (!"/api/status".equals(request.path()) && config.getBackends().isEmpty()) {
                log.warn("No backends available to handle request for path: {}", request.path());
                sendErrorResponse(client, 503, "Service Unavailable");
                return;
            }

            try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
                var handler = routeHandlerFactory.getHandler(request);
                var future = scope.fork(() -> handler.handle(request));
                scope.join();
                response = future.get(); // Use get() instead of resultNow()
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Interrupted while handling client: {}", e.getMessage());
                sendErrorResponse(client, 500, "Internal Server Error");
                return;
            } catch (Exception e) {
                log.error("Error processing request: {}", e.getMessage());
                sendErrorResponse(client, 500, "Internal Server Error");
                return;
            }

            client.write(ByteBuffer.wrap(response.getBytes()));
            log.info("Sent response for path: {}", request.path());
        } catch (IOException e) {
            log.error("Error handling client: {}", e.getMessage());
            sendErrorResponse(client, 400, "Bad Request");
        }
    }

    private void sendErrorResponse(SocketChannel client, int statusCode, String reason) {
        try {
            String errorResponse = String.format("""
                    HTTP/1.1 %d %s\r
                    Content-Length: 0\r
                    \r
                    """, statusCode, reason);
            client.write(ByteBuffer.wrap(errorResponse.getBytes()));
        } catch (IOException e) {
            log.error("Failed to send error response: {}", e.getMessage());
        }
    }
    
}
