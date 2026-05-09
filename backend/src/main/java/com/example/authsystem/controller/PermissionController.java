package com.example.authsystem.controller;

import com.example.authsystem.annotation.RequirePermission;
import com.example.authsystem.entity.Permission;
import com.example.authsystem.service.RbacService;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/system/permissions")
public class PermissionController {

    private final RbacService rbacService;

    public PermissionController(RbacService rbacService) {
        this.rbacService = rbacService;
    }

    @GetMapping
    @RequirePermission({"system:permission:view"})
    public List<Map<String, Object>> listPermissions() {
        return rbacService.getAllPermissions().stream()
            .map(this::convertPermissionToMap)
            .toList();
    }

    @GetMapping("/{id}")
    @RequirePermission({"system:permission:view"})
    public Map<String, Object> getPermission(@PathVariable Long id) {
        Permission permission = rbacService.getPermissionById(id);
        if (permission == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "权限不存在");
        }
        return convertPermissionToMap(permission);
    }

    @PostMapping
    @RequirePermission({"system:role:edit"})
    public Map<String, Object> createPermission(@RequestBody Map<String, String> body) {
        String code = body.get("code");
        String name = body.get("name");
        String type = body.get("type");
        String description = body.get("description");

        if (code == null || code.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "权限编码不能为空");
        }

        Permission permission = rbacService.createPermission(code, name, type, description);
        Map<String, Object> result = convertPermissionToMap(permission);
        result.put("message", "权限创建成功");
        return result;
    }

    @PutMapping("/{id}")
    @RequirePermission({"system:role:edit"})
    public Map<String, Object> updatePermission(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Permission permission = rbacService.getPermissionById(id);
        if (permission == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "权限不存在");
        }

        String name = body.get("name");
        String type = body.get("type");
        String description = body.get("description");
        String status = body.get("status");

        rbacService.updatePermission(id, name, type, description, status);
        Map<String, Object> result = convertPermissionToMap(rbacService.getPermissionById(id));
        result.put("message", "权限更新成功");
        return result;
    }

    @DeleteMapping("/{id}")
    @RequirePermission({"system:role:edit"})
    public Map<String, Object> deletePermission(@PathVariable Long id) {
        Permission permission = rbacService.getPermissionById(id);
        if (permission == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "权限不存在");
        }
        rbacService.deletePermission(id);
        Map<String, Object> result = new HashMap<>();
        result.put("message", "权限删除成功");
        return result;
    }

    private Map<String, Object> convertPermissionToMap(Permission perm) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", perm.getId());
        map.put("code", perm.getCode());
        map.put("name", perm.getName());
        map.put("type", perm.getType());
        map.put("description", perm.getDescription());
        map.put("status", perm.getStatus());
        map.put("createdAt", perm.getCreatedAt());
        return map;
    }
}
