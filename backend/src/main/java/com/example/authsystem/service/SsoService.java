package com.example.authsystem.service;

import com.example.authsystem.model.AuthProvider;
import com.example.authsystem.model.SessionUser;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class SsoService {

    private final Map<String, SsoTicket> tickets = new ConcurrentHashMap<>();

    public String createTicket(SessionUser user, String service) {
        String ticket = "ST-" + UUID.randomUUID();
        tickets.put(ticket, new SsoTicket(ticket, service, new SessionUser(
            user.username(),
            user.displayName(),
            user.email(),
            AuthProvider.SSO,
            user.roles()), Instant.now().plusSeconds(120)));
        return ticket;
    }

    public SessionUser consumeTicket(String ticket, String service) {
        SsoTicket stored = tickets.remove(ticket);
        if (stored == null) {
            throw new IllegalArgumentException("SSO ticket does not exist");
        }
        if (!stored.service().equals(service)) {
            throw new IllegalArgumentException("SSO service does not match");
        }
        if (stored.expiresAt().isBefore(Instant.now())) {
            throw new IllegalArgumentException("SSO ticket expired");
        }
        return stored.user();
    }

    public void clear() {
        tickets.clear();
    }

    private record SsoTicket(String ticket, String service, SessionUser user, Instant expiresAt) {
    }
}
