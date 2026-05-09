package com.example.authsystem.service;

import com.example.authsystem.config.AppProperties;
import com.example.authsystem.model.AuthProvider;
import com.example.authsystem.model.SessionUser;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

public class LocalUserDetailsService implements UserDetailsService {

    private final Map<String, LocalUserRecord> users;
    private final RbacService rbacService;

    public LocalUserDetailsService(AppProperties appProperties, PasswordEncoder passwordEncoder, RbacService rbacService) {
        this.rbacService = rbacService;
        this.users = new LinkedHashMap<>();
        for (AppProperties.LocalUserProperties user : appProperties.getUsers()) {
            users.put(user.getUsername(), new LocalUserRecord(
                user.getUsername(),
                passwordEncoder.encode(user.getPassword()),
                user.getDisplayName(),
                user.getRoles()));
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        LocalUserRecord user = users.get(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }
        return User.withUsername(user.username())
            .password(user.password())
            .authorities(toAuthorities(user.roles()))
            .build();
    }

    public Optional<SessionUser> findSessionUser(String username) {
        LocalUserRecord user = users.get(username);
        if (user == null) {
            return Optional.empty();
        }
        List<String> permissions = rbacService.getPermissionsForRoles(user.roles());
        return Optional.of(new SessionUser(user.username(), user.displayName(), null, AuthProvider.LOCAL, user.roles(), permissions));
    }

    private Collection<? extends GrantedAuthority> toAuthorities(List<String> roles) {
        return roles.stream()
            .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());
    }

    private record LocalUserRecord(String username, String password, String displayName, List<String> roles) {
    }
}
