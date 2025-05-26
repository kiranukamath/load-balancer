package com.kiran.load_balancer.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public record HttpRequest (
    String method,
    String path,
    String version,
    Map<String,String> headers,
    byte[] body
){
    public HttpRequest{
        if (method == null || path == null || version == null) {
            throw new IllegalArgumentException("Method, path, and version cannot be null");
        }
        headers = headers !=null ? Collections.unmodifiableMap(new HashMap<>(headers)) : Collections.emptyMap();
        body = body != null ? body.clone(): new byte[0];
    }
    
    public byte[] getBody() {
        return body.clone(); // Defensive copy to ensure immutability
    }
}
