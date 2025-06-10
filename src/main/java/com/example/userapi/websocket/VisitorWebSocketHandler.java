package com.example.userapi.websocket;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.model.CityResponse;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class VisitorWebSocketHandler extends TextWebSocketHandler {

    private static final Set<WebSocketSession> sessions = Collections.synchronizedSet(new HashSet<>());
    private static final Map<String, VisitorInfo> visitorsInfo = new ConcurrentHashMap<>();
    private DatabaseReader dbReader;

    public VisitorWebSocketHandler() {
        try {
            File database = new File("geoip/GeoLite2-City.mmdb");
            dbReader = new DatabaseReader.Builder(database).build();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);

        String ip = extractClientIp(session);
        VisitorInfo info = lookupVisitorInfo(ip);

        visitorsInfo.put(session.getId(), info);

        broadcastVisitors();
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
        visitorsInfo.remove(session.getId());
        broadcastVisitors();
    }

    private void broadcastVisitors() throws Exception {
        Map<String, Object> messageMap = new HashMap<>();
        messageMap.put("count", sessions.size());
        messageMap.put("visitors", visitorsInfo.values());
        String message = new ObjectMapper().writeValueAsString(messageMap);

        synchronized (sessions) {
            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(message));
                }
            }
        }
    }

    private String extractClientIp(WebSocketSession session) {
        try {
            Object forwarded = session.getAttributes().get("X-Forwarded-For");
            if (forwarded instanceof String fwd && !fwd.isEmpty()) {
                return fwd.split(",")[0].trim(); // prendre la première IP si plusieurs
            }

            InetSocketAddress remoteAddress = session.getRemoteAddress();
            if (remoteAddress != null) {
                return remoteAddress.getAddress().getHostAddress();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Unknown";
    }

    private VisitorInfo lookupVisitorInfo(String ip) {
        if (dbReader == null || ip.equals("Unknown")) {
            return new VisitorInfo(ip, "Inconnu", "Inconnu");
        }

        try {
            CityResponse response = dbReader.city(java.net.InetAddress.getByName(ip));
            String city = response.getCity().getName();
            String country = response.getCountry().getName();
            return new VisitorInfo(ip, city != null ? city : "Inconnu", country != null ? country : "Inconnu");
        } catch (Exception e) {
            return new VisitorInfo(ip, "Inconnu", "Inconnu");
        }
    }

    public static class VisitorInfo {
        private String ip;
        private String city;
        private String country;

        public VisitorInfo(String ip, String city, String country) {
            this.ip = ip;
            this.city = city;
            this.country = country;
        }

        public String getIp() {
            return ip;
        }

        public String getCity() {
            return city;
        }

        public String getCountry() {
            return country;
        }
    }
}
