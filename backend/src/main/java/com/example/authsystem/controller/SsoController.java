package com.example.authsystem.controller;

import com.example.authsystem.model.AuthProvider;
import com.example.authsystem.model.SessionUser;
import com.example.authsystem.payload.SsoLoginRequest;
import com.example.authsystem.payload.UserResponse;
import com.example.authsystem.service.LocalUserDetailsService;
import com.example.authsystem.service.SsoService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/sso")
public class SsoController {

    private static final String SESSION_USER_KEY = "SESSION_AUTH_USER";

    private final LocalUserDetailsService localUserDetailsService;
    private final SsoService ssoService;

    public SsoController(LocalUserDetailsService localUserDetailsService, SsoService ssoService) {
        this.localUserDetailsService = localUserDetailsService;
        this.ssoService = ssoService;
    }

    @PostMapping("/login")
    public Map<String, String> login(@Valid @RequestBody SsoLoginRequest request, HttpServletRequest httpServletRequest) {
        SessionUser sessionUser = localUserDetailsService.findSessionUser(request.username())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "SSO user not found"));
        httpServletRequest.getSession(true).setAttribute(SESSION_USER_KEY, sessionUser);
        String ticket = ssoService.createTicket(sessionUser, request.service());
        return Map.of("ticket", ticket, "service", request.service());
    }

    @GetMapping("/validate")
    public UserResponse validate(@RequestParam String ticket, @RequestParam String service, HttpServletRequest request) {
        try {
            SessionUser user = ssoService.consumeTicket(ticket, service);
            request.getSession(true).setAttribute(SESSION_USER_KEY, user);
            return UserResponse.from(user);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.getMessage());
        }
    }

    @PostMapping("/logout")
    public Map<String, Object> logout(HttpServletRequest request) {
        if (request.getSession(false) != null) {
            request.getSession(false).invalidate();
        }
        ssoService.clear();
        return Map.of("success", true);
    }
}
