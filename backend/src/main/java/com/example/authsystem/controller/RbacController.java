package com.example.authsystem.controller;

import com.example.authsystem.entity.Permission;
import com.example.authsystem.entity.Role;
import com.example.authsystem.service.RbacService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rbac")
public class RbacController {

    private final RbacService rbacService;

    public RbacController(RbacService rbacService) {
        this.rbacService = rbacService;
    }

    @GetMapping("/roles")
    public List<Role> getAllRoles() {
        return rbacService.getAllRoles();
    }

    @GetMapping("/permissions")
    public List<Permission> getAllPermissions() {
        return rbacService.getAllPermissions();
    }

    @PostMapping("/roles/{roleCode}/permissions")
    public Map<String, Object> updateRolePermissions(
            @PathVariable String roleCode,
            @RequestBody List<String> permissionCodes) {
        rbacService.updateRolePermissions(roleCode, permissionCodes);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("roleCode", roleCode);
        return result;
    }
}
