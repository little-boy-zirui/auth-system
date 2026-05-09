package com.example.authsystem.controller;

import com.example.authsystem.annotation.RequirePermission;
import com.example.authsystem.model.AuthProvider;
import com.example.authsystem.model.SessionUser;
import com.example.authsystem.payload.LoginRequest;
import com.example.authsystem.payload.UserResponse;
import com.example.authsystem.service.LocalUserDetailsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final String SESSION_USER_KEY = "SESSION_AUTH_USER";

    private final AuthenticationManager authenticationManager;
    private final LocalUserDetailsService localUserDetailsService;

    public AuthController(AuthenticationManager authenticationManager, LocalUserDetailsService localUserDetailsService) {
        this.authenticationManager = authenticationManager;
        this.localUserDetailsService = localUserDetailsService;
    }

    @PostMapping("/login")
    public UserResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpServletRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(request.username(), request.password()));
            SessionUser user = localUserDetailsService.findSessionUser(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
            httpServletRequest.getSession(true).setAttribute(SESSION_USER_KEY, user);
            return UserResponse.from(user);
        } catch (BadCredentialsException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
        }
    }

    @PostMapping("/logout")
    public Map<String, Object> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return Map.of("success", true);
    }

    @GetMapping("/me")
    public UserResponse me(HttpServletRequest request, Authentication authentication) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            Object sessionUser = session.getAttribute(SESSION_USER_KEY);
            if (sessionUser instanceof SessionUser user) {
                return UserResponse.from(user);
            }
        }
        if (authentication == null || !authentication.isAuthenticated()) {
            return UserResponse.anonymous();
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof OidcUser oidcUser) {
            return new UserResponse(true,
                oidcUser.getPreferredUsername(),
                oidcUser.getFullName(),
                oidcUser.getEmail(),
                AuthProvider.OAUTH2,
                authorities(authentication),
                List.of("user:view", "user:create"));
        }
        if (principal instanceof OAuth2User oauth2User) {
            String username = oauth2User.getAttribute("login");
            if (username == null) {
                username = oauth2User.getAttribute("name");
            }
            String displayName = oauth2User.getAttribute("name");
            String email = oauth2User.getAttribute("email");
            return new UserResponse(true, username, displayName, email, AuthProvider.OAUTH2, authorities(authentication), List.of("user:view"));
        }
        return UserResponse.anonymous();
    }

    @GetMapping("/users")
    @RequirePermission({"user:view"})
    public List<Map<String, String>> listUsers() {
        return List.of(
            Map.of("username", "admin", "role", "ADMIN"),
            Map.of("username", "demo", "role", "USER"),
            Map.of("username", "guest", "role", "VIEWER")
        );
    }

    @PostMapping("/users")
    @RequirePermission({"user:create"})
    public Map<String, Object> createUser(@RequestBody Map<String, String> body) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "User created: " + body.get("username"));
        return result;
    }

    @DeleteMapping("/users/{username}")
    @RequirePermission({"user:delete"})
    public Map<String, Object> deleteUser(@PathVariable String username) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "User deleted: " + username);
        return result;
    }

    private List<String> authorities(Authentication authentication) {
        return authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toList());
    }
}
