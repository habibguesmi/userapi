package com.example.userapi.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new VisitorWebSocketHandler(), "/ws/visitors")
                .addInterceptors(new HttpSessionHandshakeInterceptor())
                .setAllowedOrigins("https://habibguesmi.github.io", "http://localhost:4200")
                .withSockJS(); // Active SockJS
    }
}
