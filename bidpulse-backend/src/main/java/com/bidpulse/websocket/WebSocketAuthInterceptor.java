package com.bidpulse.websocket;

import org.springframework.http.server.*;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

public class WebSocketAuthInterceptor implements HandshakeInterceptor {
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   org.springframework.web.socket.WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) {
        // copy Authorization header to attributes (frontend may send token)
        var headers = request.getHeaders();
        if (headers.containsKey("Authorization")) {
            attributes.put("Authorization", headers.getFirst("Authorization"));
        }
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               org.springframework.web.socket.WebSocketHandler wsHandler,
                               Exception exception) { }
}