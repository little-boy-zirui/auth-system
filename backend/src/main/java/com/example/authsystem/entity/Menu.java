package com.example.authsystem.entity;

import java.time.Instant;

public class Menu {

    private Long id;
    private String name;
    private String path;
    private String component;
    private Long parentId;
    private String type;
    private String perms;
    private String icon;
    private Integer orderNum;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;

    public Menu() {
    }

    public Menu(Long id, String name, String path, String component, Long parentId, String type, String perms, String icon, Integer orderNum, String status, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.path = path;
        this.component = component;
        this.parentId = parentId;
        this.type = type;
        this.perms = perms;
        this.icon = icon;
        this.orderNum = orderNum;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getComponent() {
        return component;
    }

    public void setComponent(String component) {
        this.component = component;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPerms() {
        return perms;
    }

    public void setPerms(String perms) {
        this.perms = perms;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Integer getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(Integer orderNum) {
        this.orderNum = orderNum;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
