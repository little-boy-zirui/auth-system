package com.example.authsystem.payload;

import com.example.authsystem.model.AuthProvider;
import com.example.authsystem.model.SessionUser;
import java.util.List;

public record UserResponse(
    boolean authenticated,
    String username,
    String displayName,
    String email,
    AuthProvider provider,
    List<String> roles,
    List<String> permissions
) {
    public static UserResponse anonymous() {
        return new UserResponse(false, null, null, null, null, List.of(), List.of());
    }

    public static UserResponse from(SessionUser user) {
        return new UserResponse(true, user.username(), user.displayName(), user.email(), user.provider(), user.roles(), user.permissions());
    }
}
