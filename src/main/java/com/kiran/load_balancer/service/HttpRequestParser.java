package com.kiran.load_balancer.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kiran.load_balancer.config.LoadBalancerConfig;
import com.kiran.load_balancer.model.HttpRequest;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class HttpRequestParser {
    @Autowired
    LoadBalancerConfig config;

    private static final String HEADER_DELIMITER = "\r\n\r\n";
    private static final String LINE_DELIMITER = "\r\n";

    public HttpRequest parseRequest(SocketChannel client) throws IOException{
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        StringBuilder headerLines = new StringBuilder();
        boolean headerComplete = false;
        int contentLength =0;

        ByteBuffer byteBuffer = ByteBuffer.allocate(config.getMaxBufferSize());

        while(!headerComplete){
            int byteRead = client.read(byteBuffer);
            if(byteRead == -1){
                throw new IOException("Client connection closed");
            }
            byteBuffer.flip();
            buffer.write(byteBuffer.array(),0,byteBuffer.limit());
            byteBuffer.clear();

            String currentData = buffer.toString();
            int headerEndIndex = currentData.indexOf(HEADER_DELIMITER);
            if(headerEndIndex != -1){
                headerComplete = true;
                headerLines.append(currentData,0, headerEndIndex);
                buffer.reset();
                buffer.write(currentData.substring(headerEndIndex+4).getBytes());
            }else{
                headerLines.append(currentData);
                buffer.reset();
            }

        }

        String[] lines = headerLines.toString().split(LINE_DELIMITER);
        if (lines.length == 0 || lines[0].trim().isEmpty()) {
            throw new IOException("Invalid HTTP request");
        }

        String[] requestLineParts = lines[0].split(" ", 3);
        if (requestLineParts.length != 3) {
            throw new IOException("Invalid request line: " + lines[0]);
        }

        String method = requestLineParts[0];
        String path = requestLineParts[1];
        String version = requestLineParts[2];

        Map<String,String> headers = new HashMap<>();
        for (int i = 1; i < lines.length; i++) {
            if(!lines[i].isEmpty()){
                String[] headerParts = lines[i].split(": ",2);
                if(headerParts.length ==2){
                    headers.put(headerParts[0], headerParts[1]);
                    if ("Content-Length".equalsIgnoreCase(headerParts[0])) {
                        contentLength = Integer.parseInt(headerParts[1]);
                    }
                }
            }
        }

        byte[] body = readBody(client,buffer,contentLength);
        log.info("Parsed HTTP request: {} {}", method, path);
        return new HttpRequest(method, path, version, headers, body);
    }

    private byte[] readBody(SocketChannel client, ByteArrayOutputStream buffer, int contentLength) throws IOException {
        if(contentLength<0){
            return new byte[0];
        }

        ByteBuffer byteBuffer = ByteBuffer.allocate(config.getMaxBufferSize());
        while(buffer.size()<contentLength){
            int byteRead = client.read(byteBuffer);
            if(byteRead==-1){
                throw new IOException("Client connection closed while reading body");
            }
            byteBuffer.flip();
            buffer.write(byteBuffer.array(),0,byteBuffer.limit());
            byteBuffer.clear();
        }
        
        byte[] body = buffer.toByteArray();
        if(body.length > contentLength){
            byte[] truncated = new byte[contentLength];
            System.arraycopy(body, 0, truncated, 0, contentLength);
            return truncated;
        }
        return body;
    }
}
