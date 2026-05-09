package com.example.authsystem.entity;

import java.time.Instant;

public class Permission {

    private Long id;
    private String code;
    private String name;
    private String type;
    private String path;
    private String method;
    private String description;
    private Instant createdAt;

    public Permission() {
    }

    public Permission(Long id, String code, String name, String type, String path, String method, String description, Instant createdAt) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.type = type;
        this.path = path;
        this.method = method;
        this.description = description;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
