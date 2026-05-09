package com.example.authsystem.controller;

import com.example.authsystem.annotation.RequirePermission;
import com.example.authsystem.entity.Menu;
import com.example.authsystem.service.RbacService;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/system/menus")
public class MenuController {

    private final RbacService rbacService;

    public MenuController(RbacService rbacService) {
        this.rbacService = rbacService;
    }

    @GetMapping
    @RequirePermission({"system:menu:view"})
    public List<Map<String, Object>> listMenus() {
        return rbacService.getAllMenus().stream()
            .sorted(Comparator.comparing(Menu::getOrderNum))
            .map(this::convertMenuToMap)
            .toList();
    }

    @GetMapping("/tree")
    @RequirePermission({"system:menu:view"})
    public List<Map<String, Object>> getMenuTree() {
        return rbacService.getMenuTree().stream()
            .map(this::convertMenuToMap)
            .toList();
    }

    @GetMapping("/{id}")
    @RequirePermission({"system:menu:view"})
    public Map<String, Object> getMenu(@PathVariable Long id) {
        Menu menu = rbacService.getMenuById(id);
        if (menu == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "菜单不存在");
        }
        return convertMenuToMap(menu);
    }

    @PostMapping
    @RequirePermission({"system:menu:create"})
    public Map<String, Object> createMenu(@RequestBody Map<String, Object> body) {
        String name = (String) body.get("name");
        String path = (String) body.get("path");
        String component = (String) body.get("component");
        Long parentId = body.get("parentId") != null ? Long.valueOf(body.get("parentId").toString()) : 0L;
        String type = (String) body.get("type");
        String perms = (String) body.get("perms");
        String icon = (String) body.get("icon");
        Integer orderNum = body.get("orderNum") != null ? Integer.valueOf(body.get("orderNum").toString()) : 1;

        if (name == null || name.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "菜单名称不能为空");
        }

        Menu menu = rbacService.createMenu(name, path, component, parentId, type, perms, icon, orderNum);
        Map<String, Object> result = convertMenuToMap(menu);
        result.put("message", "菜单创建成功");
        return result;
    }

    @PutMapping("/{id}")
    @RequirePermission({"system:menu:edit"})
    public Map<String, Object> updateMenu(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Menu menu = rbacService.getMenuById(id);
        if (menu == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "菜单不存在");
        }

        String name = (String) body.get("name");
        String path = (String) body.get("path");
        String component = (String) body.get("component");
        String type = (String) body.get("type");
        String perms = (String) body.get("perms");
        String icon = (String) body.get("icon");
        Integer orderNum = body.get("orderNum") != null ? Integer.valueOf(body.get("orderNum").toString()) : null;
        String status = (String) body.get("status");

        rbacService.updateMenu(id, name, path, component, type, perms, icon, orderNum, status);
        Map<String, Object> result = convertMenuToMap(rbacService.getMenuById(id));
        result.put("message", "菜单更新成功");
        return result;
    }

    @DeleteMapping("/{id}")
    @RequirePermission({"system:menu:delete"})
    public Map<String, Object> deleteMenu(@PathVariable Long id) {
        Menu menu = rbacService.getMenuById(id);
        if (menu == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "菜单不存在");
        }
        rbacService.deleteMenu(id);
        Map<String, Object> result = new HashMap<>();
        result.put("message", "菜单删除成功");
        return result;
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
        map.put("createdAt", menu.getCreatedAt());
        return map;
    }
}
