package com.kiran.load_balancer.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kiran.load_balancer.model.HttpRequest;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RequestForwarder implements RouteHandler {
    @Autowired
    LoadBalancingStrategy loadBalancingStrategy;

    @Override
    public boolean canHandle(HttpRequest request) {
        return true;
    }

    @Override
    public String handle(HttpRequest request) throws IOException {
        String backend = loadBalancingStrategy.selectedBackend();
        return forwardToBackend(backend, request);
    }

    private String forwardToBackend(String backend, HttpRequest request) throws IOException {
        String[] parts = backend.split(":");
        String host = parts[0];
        int port = Integer.parseInt(parts[1]);

        try (SocketChannel backendChannel = SocketChannel.open(new InetSocketAddress(host, port))) {
            StringBuilder requestBuilder = new StringBuilder().append(request.method()).append(" ")
                    .append(request.path()).append(" ")
                    .append(request.version()).append("\r\n");
            for (Entry<String, String> header : request.headers().entrySet()) {
                requestBuilder.append(header.getKey()).append(": ").append(header.getValue()).append("\r\n");
            }
            requestBuilder.append("\r\n");
            byte[] headerBytes = requestBuilder.toString().getBytes();
            byte[] body = request.getBody();
            byte[] fullRequest = new byte[headerBytes.length + body.length];

            System.arraycopy(headerBytes, 0, fullRequest, 0, headerBytes.length);
            System.arraycopy(body, 0, fullRequest, headerBytes.length, body.length);

            //send request
            backendChannel.write(ByteBuffer.wrap(fullRequest));
            log.info("Forwarded request to backend: {}", backend);

            //read response
            ByteArrayOutputStream responseBuffer = new ByteArrayOutputStream();
            ByteBuffer buffer = ByteBuffer.allocate(8192);
            while(backendChannel.read(buffer) > 0){
                buffer.flip();
                responseBuffer.write(buffer.array(), 0, buffer.limit());
                buffer.clear();
            }

            String response = responseBuffer.toString();
            log.info("Received response from backend: {}", response.substring(0, Math.min(response.length(), 50)));
            return response;
        }
    }

}
