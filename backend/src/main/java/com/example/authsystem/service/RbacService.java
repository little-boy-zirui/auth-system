package com.example.authsystem.service;

import com.example.authsystem.entity.Permission;
import com.example.authsystem.entity.Role;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Service;

@Service
public class RbacService {

    private final Map<Long, Role> roles = new ConcurrentHashMap<>();
    private final Map<Long, Permission> permissions = new ConcurrentHashMap<>();
    private final AtomicLong roleIdGenerator = new AtomicLong(1);
    private final AtomicLong permissionIdGenerator = new AtomicLong(1);

    public RbacService() {
        initDefaultData();
    }

    private void initDefaultData() {
        Permission viewUser = createPermission("user:view", "查看用户", "BUTTON", "/api/users", "GET", "查看用户列表");
        Permission createUser = createPermission("user:create", "创建用户", "BUTTON", "/api/users", "POST", "创建新用户");
        Permission editUser = createPermission("user:edit", "编辑用户", "BUTTON", "/api/users/*", "PUT", "编辑用户");
        Permission deleteUser = createPermission("user:delete", "删除用户", "BUTTON", "/api/users/*", "DELETE", "删除用户");
        Permission viewRole = createPermission("role:view", "查看角色", "BUTTON", "/api/roles", "GET", "查看角色列表");
        Permission assignRole = createPermission("role:assign", "分配角色", "BUTTON", "/api/users/*/roles", "POST", "分配角色给用户");

        Role admin = createRole("ADMIN", "系统管理员", "拥有所有权限");
        assignPermissions(admin, viewUser, createUser, editUser, deleteUser, viewRole, assignRole);

        Role editor = createRole("EDITOR", "编辑员", "可以编辑但不能删除");
        assignPermissions(editor, viewUser, createUser, editUser, viewRole);

        Role viewer = createRole("VIEWER", "访客", "只能查看");
        assignPermissions(viewer, viewUser, viewRole);
    }

    public Permission createPermission(String code, String name, String type, String path, String method, String description) {
        Permission permission = new Permission(permissionIdGenerator.getAndIncrement(), code, name, type, path, method, description, Instant.now());
        permissions.put(permission.getId(), permission);
        return permission;
    }

    public Role createRole(String code, String name, String description) {
        Role role = new Role(roleIdGenerator.getAndIncrement(), code, name, description, new ArrayList<>(), Instant.now());
        roles.put(role.getId(), role);
        return role;
    }

    public void assignPermissions(Role role, Permission... permissions) {
        List<String> permissionCodes = new ArrayList<>(role.getPermissions());
        for (Permission p : permissions) {
            if (!permissionCodes.contains(p.getCode())) {
                permissionCodes.add(p.getCode());
            }
        }
        role.setPermissions(permissionCodes);
    }

    public List<Role> getAllRoles() {
        return new ArrayList<>(roles.values());
    }

    public List<Permission> getAllPermissions() {
        return new ArrayList<>(permissions.values());
    }

    public Role getRoleByCode(String code) {
        return roles.values().stream()
            .filter(r -> r.getCode().equals(code))
            .findFirst()
            .orElse(null);
    }

    public List<String> getPermissionsForRoles(List<String> roleCodes) {
        return roles.values().stream()
            .filter(r -> roleCodes.contains(r.getCode()))
            .flatMap(r -> r.getPermissions().stream())
            .distinct()
            .toList();
    }

    public void updateRolePermissions(String roleCode, List<String> permissionCodes) {
        Role role = getRoleByCode(roleCode);
        if (role != null) {
            role.setPermissions(permissionCodes);
        }
    }
}
