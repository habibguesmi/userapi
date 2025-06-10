package com.example.userapi.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;
import org.springframework.http.server.ServletServerHttpRequest;

import java.util.Map;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new VisitorWebSocketHandler(), "/ws/visitors")
                .addInterceptors(new HttpSessionHandshakeInterceptor() {
                    @Override
                    public boolean beforeHandshake(ServerHttpRequest request,
                                                   ServerHttpResponse response,
                                                   WebSocketHandler wsHandler,
                                                   Map<String, Object> attributes) throws Exception {
                        if (request instanceof ServletServerHttpRequest servletRequest) {
                            String ip = servletRequest.getServletRequest().getHeader("X-Forwarded-For");
                            if (ip != null && !ip.isEmpty()) {
                                attributes.put("X-Forwarded-For", ip);
                            }
                        }
                        return super.beforeHandshake(request, response, wsHandler, attributes);
                    }
                })
                .setAllowedOrigins(
                        "https://habibguesmi.github.io",
                        "http://localhost:4200",
                        "http://localhost:5173",
                        "https://user-app-angular.onrender.com"
                )
                .withSockJS(); // important pour fallback HTTP
    }
}
