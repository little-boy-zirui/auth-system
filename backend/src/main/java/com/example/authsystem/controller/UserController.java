package com.example.authsystem.controller;

import com.example.authsystem.annotation.RequirePermission;
import com.example.authsystem.entity.Role;
import com.example.authsystem.entity.User;
import com.example.authsystem.service.RbacService;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/system/users")
public class UserController {

    private final RbacService rbacService;
    private final PasswordEncoder passwordEncoder;

    public UserController(RbacService rbacService, PasswordEncoder passwordEncoder) {
        this.rbacService = rbacService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    @RequirePermission({"system:user:view"})
    public List<Map<String, Object>> listUsers() {
        return rbacService.getAllUsers().stream()
            .map(this::convertUserToMap)
            .toList();
    }

    @GetMapping("/{id}")
    @RequirePermission({"system:user:view"})
    public Map<String, Object> getUser(@PathVariable Long id) {
        User user = rbacService.getUserById(id);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "用户不存在");
        }
        return convertUserToMap(user);
    }

    @PostMapping
    @RequirePermission({"system:user:create"})
    public Map<String, Object> createUser(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        String displayName = body.get("displayName");
        String email = body.get("email");
        String phone = body.get("phone");

        if (username == null || username.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "用户名不能为空");
        }
        if (rbacService.getUserByUsername(username) != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "用户名已存在");
        }

        User user = rbacService.createUser(username, passwordEncoder.encode(password), displayName, email, phone);
        Map<String, Object> result = convertUserToMap(user);
        result.put("message", "用户创建成功");
        return result;
    }

    @PutMapping("/{id}")
    @RequirePermission({"system:user:edit"})
    public Map<String, Object> updateUser(@PathVariable Long id, @RequestBody Map<String, String> body) {
        User user = rbacService.getUserById(id);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "用户不存在");
        }

        String displayName = body.get("displayName");
        String email = body.get("email");
        String phone = body.get("phone");
        String status = body.get("status");

        rbacService.updateUser(id, displayName, email, phone, status);
        Map<String, Object> result = convertUserToMap(rbacService.getUserById(id));
        result.put("message", "用户更新成功");
        return result;
    }

    @DeleteMapping("/{id}")
    @RequirePermission({"system:user:delete"})
    public Map<String, Object> deleteUser(@PathVariable Long id) {
        User user = rbacService.getUserById(id);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "用户不存在");
        }
        rbacService.deleteUser(id);
        Map<String, Object> result = new HashMap<>();
        result.put("message", "用户删除成功");
        return result;
    }

    @GetMapping("/{id}/roles")
    @RequirePermission({"system:user:view"})
    public List<Map<String, Object>> getUserRoles(@PathVariable Long id) {
        User user = rbacService.getUserById(id);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "用户不存在");
        }
        return rbacService.getUserRoles(user).stream()
            .map(this::convertRoleToMap)
            .toList();
    }

    @PostMapping("/{id}/roles")
    @RequirePermission({"system:role:assign"})
    public Map<String, Object> assignUserRoles(@PathVariable Long id, @RequestBody List<String> roleCodes) {
        User user = rbacService.getUserById(id);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "用户不存在");
        }

        List<Role> roles = roleCodes.stream()
            .map(rbacService::getRoleByCode)
            .filter(Objects::nonNull)
            .toList();

        rbacService.assignRoles(user, roles);
        Map<String, Object> result = new HashMap<>();
        result.put("message", "角色分配成功");
        return result;
    }

    private Map<String, Object> convertUserToMap(User user) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", user.getId());
        map.put("username", user.getUsername());
        map.put("displayName", user.getDisplayName());
        map.put("email", user.getEmail());
        map.put("phone", user.getPhone());
        map.put("status", user.getStatus());
        map.put("createdAt", user.getCreatedAt());
        List<String> roleCodes = rbacService.getUserRoles(user).stream()
            .map(Role::getCode)
            .toList();
        map.put("roles", roleCodes);
        return map;
    }

    private Map<String, Object> convertRoleToMap(Role role) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", role.getId());
        map.put("code", role.getCode());
        map.put("name", role.getName());
        map.put("description", role.getDescription());
        return map;
    }
}
