package com.example.authsystem.model;

import java.io.Serializable;
import java.util.List;

public record SessionUser(
    String username,
    String displayName,
    String email,
    AuthProvider provider,
    List<String> roles
) implements Serializable {
}
