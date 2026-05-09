package com.example.authsystem.controller;

import com.example.authsystem.annotation.RequirePermission;
import com.example.authsystem.entity.Permission;
import com.example.authsystem.entity.Role;
import com.example.authsystem.entity.Menu;
import com.example.authsystem.service.RbacService;
import java.time.Instant;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/system/roles")
public class RoleController {

    private final RbacService rbacService;

    public RoleController(RbacService rbacService) {
        this.rbacService = rbacService;
    }

    @GetMapping
    @RequirePermission({"system:role:view"})
    public List<Map<String, Object>> listRoles() {
        return rbacService.getAllRoles().stream()
            .map(this::convertRoleToMap)
            .toList();
    }

    @GetMapping("/{id}")
    @RequirePermission({"system:role:view"})
    public Map<String, Object> getRole(@PathVariable Long id) {
        Role role = rbacService.getRoleById(id);
        if (role == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "角色不存在");
        }
        return convertRoleToMap(role);
    }

    @PostMapping
    @RequirePermission({"system:role:create"})
    public Map<String, Object> createRole(@RequestBody Map<String, String> body) {
        String code = body.get("code");
        String name = body.get("name");
        String description = body.get("description");

        if (code == null || code.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "角色编码不能为空");
        }
        if (rbacService.getRoleByCode(code) != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "角色编码已存在");
        }

        Role role = rbacService.createRole(code, name, description);
        Map<String, Object> result = convertRoleToMap(role);
        result.put("message", "角色创建成功");
        return result;
    }

    @PutMapping("/{id}")
    @RequirePermission({"system:role:edit"})
    public Map<String, Object> updateRole(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Role role = rbacService.getRoleById(id);
        if (role == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "角色不存在");
        }

        String name = body.get("name");
        String description = body.get("description");
        String status = body.get("status");

        rbacService.updateRole(id, name, description, status);
        Map<String, Object> result = convertRoleToMap(rbacService.getRoleById(id));
        result.put("message", "角色更新成功");
        return result;
    }

    @DeleteMapping("/{id}")
    @RequirePermission({"system:role:delete"})
    public Map<String, Object> deleteRole(@PathVariable Long id) {
        Role role = rbacService.getRoleById(id);
        if (role == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "角色不存在");
        }
        rbacService.deleteRole(id);
        Map<String, Object> result = new HashMap<>();
        result.put("message", "角色删除成功");
        return result;
    }

    @GetMapping("/{id}/permissions")
    @RequirePermission({"system:role:view"})
    public List<Map<String, Object>> getRolePermissions(@PathVariable Long id) {
        Role role = rbacService.getRoleById(id);
        if (role == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "角色不存在");
        }
        return rbacService.getRolePermissions(role).stream()
            .map(this::convertPermissionToMap)
            .toList();
    }

    @PostMapping("/{id}/permissions")
    @RequirePermission({"system:role:assign"})
    public Map<String, Object> assignRolePermissions(@PathVariable Long id, @RequestBody List<String> permissionCodes) {
        Role role = rbacService.getRoleById(id);
        if (role == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "角色不存在");
        }

        List<Permission> permissions = permissionCodes.stream()
            .map(code -> rbacService.getAllPermissions().stream()
                .filter(p -> p.getCode().equals(code))
                .findFirst()
                .orElse(null))
            .filter(Objects::nonNull)
            .toList();

        rbacService.assignPermissions(role, permissions);
        Map<String, Object> result = new HashMap<>();
        result.put("message", "权限分配成功");
        return result;
    }

    @GetMapping("/{id}/menus")
    @RequirePermission({"system:role:view"})
    public List<Map<String, Object>> getRoleMenus(@PathVariable Long id) {
        Role role = rbacService.getRoleById(id);
        if (role == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "角色不存在");
        }
        return rbacService.getRoleMenusFlat(role).stream()
            .map(this::convertMenuToMap)
            .toList();
    }

    @PostMapping("/{id}/menus")
    @RequirePermission({"system:role:assign"})
    public Map<String, Object> assignRoleMenus(@PathVariable Long id, @RequestBody List<Long> menuIds) {
        Role role = rbacService.getRoleById(id);
        if (role == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "角色不存在");
        }

        List<Menu> menus = menuIds.stream()
            .map(rbacService::getMenuById)
            .filter(Objects::nonNull)
            .toList();

        rbacService.assignMenus(role, menus);
        Map<String, Object> result = new HashMap<>();
        result.put("message", "菜单分配成功");
        return result;
    }

    private Map<String, Object> convertRoleToMap(Role role) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", role.getId());
        map.put("code", role.getCode());
        map.put("name", role.getName());
        map.put("description", role.getDescription());
        map.put("status", role.getStatus());
        map.put("createdAt", role.getCreatedAt());
        List<String> permCodes = rbacService.getRolePermissions(role).stream()
            .map(Permission::getCode)
            .toList();
        map.put("permissions", permCodes);
        return map;
    }

    private Map<String, Object> convertPermissionToMap(Permission perm) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", perm.getId());
        map.put("code", perm.getCode());
        map.put("name", perm.getName());
        map.put("type", perm.getType());
        map.put("description", perm.getDescription());
        return map;
    }

    private Map<String, Object> convertMenuToMap(Menu menu) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", menu.getId());
        map.put("name", menu.getName());
        map.put("path", menu.getPath());
        map.put("component", menu.getComponent());
        map.put("parentId", menu.getParentId());
        map.put("type", menu.getType());
        map.put("perms", menu.getPerms());
        map.put("icon", menu.getIcon());
        map.put("orderNum", menu.getOrderNum());
        map.put("status", menu.getStatus());
        return map;
    }
}
