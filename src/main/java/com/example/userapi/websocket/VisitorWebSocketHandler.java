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
import java.io.InputStream;

public class VisitorWebSocketHandler extends TextWebSocketHandler {

    private static final Set<WebSocketSession> sessions = Collections.synchronizedSet(new HashSet<>());
    private static final Map<String, VisitorInfo> visitorsInfo = new ConcurrentHashMap<>();
    private DatabaseReader dbReader;

    public VisitorWebSocketHandler() {
        try {
            InputStream databaseStream = getClass().getClassLoader().getResourceAsStream("geoip/GeoLite2-City.mmdb");
            if (databaseStream != null) {
                dbReader = new DatabaseReader.Builder(databaseStream).build();
                System.out.println("📦 Base GeoIP chargée depuis les ressources.");
            } else {
                System.err.println("❌ Fichier GeoLite2-City.mmdb introuvable dans les ressources.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("✅ Nouvelle connexion WebSocket");

        String ip = extractClientIp(session);
        VisitorInfo info = lookupVisitorInfo(ip);

        System.out.println("👤 IP détectée : " + ip + " | Ville: " + info.getCity() + ", Pays: " + info.getCountry());

        sessions.add(session);
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
        List<VisitorInfo> filteredVisitors = new ArrayList<>();

        for (VisitorInfo info : visitorsInfo.values()) {
            // Filtrer : garder uniquement IPv4 valides
            boolean isIPv4 = info.getIp().matches("\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b");

            // Filtrer les "Inconnu" (ville ET pays)
            boolean knownLocation = !(info.getCity().equalsIgnoreCase("Inconnu") && info.getCountry().equalsIgnoreCase("Inconnu"));

            if (isIPv4 && knownLocation) {
                filteredVisitors.add(info);
            }
        }

        Map<String, Object> messageMap = new HashMap<>();
        messageMap.put("count", filteredVisitors.size());
        messageMap.put("visitors", filteredVisitors);
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
        List<String> xfHeaders = session.getHandshakeHeaders().get("X-Forwarded-For");
        if (xfHeaders != null && !xfHeaders.isEmpty()) {
            String forwardedIp = xfHeaders.get(0).split(",")[0].trim();
            System.out.println("📡 IP via X-Forwarded-For: " + forwardedIp);
            return forwardedIp;
        }

        InetSocketAddress remoteAddress = session.getRemoteAddress();
        if (remoteAddress != null) {
            String ip = remoteAddress.getAddress().getHostAddress();
            System.out.println("📡 IP via RemoteAddress: " + ip);
            return ip;
        }

        System.out.println("⚠️ Impossible de déterminer l'IP");
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
