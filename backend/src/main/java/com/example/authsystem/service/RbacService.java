package com.example.authsystem.service;

import com.example.authsystem.entity.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class RbacService {

    private final Map<Long, User> users = new ConcurrentHashMap<>();
    private final Map<Long, Role> roles = new ConcurrentHashMap<>();
    private final Map<Long, Permission> permissions = new ConcurrentHashMap<>();
    private final Map<Long, Menu> menus = new ConcurrentHashMap<>();
    private final Map<Long, Set<Long>> userRoles = new ConcurrentHashMap<>();
    private final Map<Long, Set<Long>> rolePermissions = new ConcurrentHashMap<>();
    private final Map<Long, Set<Long>> roleMenus = new ConcurrentHashMap<>();

    private final AtomicLong userIdGenerator = new AtomicLong(1);
    private final AtomicLong roleIdGenerator = new AtomicLong(1);
    private final AtomicLong permissionIdGenerator = new AtomicLong(1);
    private final AtomicLong menuIdGenerator = new AtomicLong(1);

    private final PasswordEncoder passwordEncoder;

    public RbacService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
        initDefaultData();
    }

    private void initDefaultData() {
        Permission viewUser = createPermission("system:user:view", "查看用户", "menu", null);
        Permission createUser = createPermission("system:user:create", "创建用户", "button", null);
        Permission editUser = createPermission("system:user:edit", "编辑用户", "button", null);
        Permission deleteUser = createPermission("system:user:delete", "删除用户", "button", null);
        Permission viewRole = createPermission("system:role:view", "查看角色", "menu", null);
        Permission editRole = createPermission("system:role:edit", "编辑角色", "button", null);
        Permission assignRole = createPermission("system:role:assign", "分配权限", "button", null);
        Permission viewMenu = createPermission("system:menu:view", "查看菜单", "menu", null);
        Permission editMenu = createPermission("system:menu:edit", "编辑菜单", "button", null);
        Permission viewPermission = createPermission("system:permission:view", "查看权限", "menu", null);

        Menu dashboard = createMenu("首页", "/dashboard", "Dashboard", 0L, "directory", null, "🏠", 1);
        Menu system = createMenu("系统管理", "/system", null, 0L, "directory", null, "⚙️", 2);
        Menu userMenu = createMenu("用户管理", "/system/user", "UserManagement", system.getId(), "menu", "system:user:view", "👤", 1);
        Menu roleMenu = createMenu("角色管理", "/system/role", "RoleManagement", system.getId(), "menu", "system:role:view", "🎭", 2);
        Menu menuMenu = createMenu("菜单管理", "/system/menu", "MenuManagement", system.getId(), "menu", "system:menu:view", "📋", 3);
        Menu permissionMenu = createMenu("权限配置", "/system/permission", "PermissionManagement", system.getId(), "menu", "system:permission:view", "🔐", 4);

        Role admin = createRole("ADMIN", "系统管理员", "拥有所有权限");
        assignPermissions(admin, Arrays.asList(viewUser, createUser, editUser, deleteUser, viewRole, editRole, assignRole, viewMenu, editMenu, viewPermission));
        assignMenus(admin, Arrays.asList(dashboard, system, userMenu, roleMenu, menuMenu, permissionMenu));

        Role editor = createRole("USER", "普通用户", "可以编辑但不能删除");
        assignPermissions(editor, Arrays.asList(viewUser, createUser, editUser, viewRole, viewMenu, viewPermission));
        assignMenus(editor, Arrays.asList(dashboard, system, userMenu, roleMenu, menuMenu, permissionMenu));

        Role viewer = createRole("VIEWER", "访客", "只能查看");
        assignPermissions(viewer, Arrays.asList(viewUser, viewRole, viewMenu, viewPermission));
        assignMenus(viewer, Arrays.asList(dashboard, system, userMenu, roleMenu, menuMenu, permissionMenu));

        User adminUser = createUser("admin", passwordEncoder.encode("admin123"), "系统管理员", "admin@example.com", "13800138000");
        assignRoles(adminUser, Arrays.asList(admin));

        User demoUser = createUser("demo", passwordEncoder.encode("demo123"), "演示用户", "demo@example.com", "13800138001");
        assignRoles(demoUser, Arrays.asList(editor));
    }

    public User createUser(String username, String password, String displayName, String email, String phone) {
        User user = new User(userIdGenerator.getAndIncrement(), username, password, displayName, email, phone, "1", Instant.now(), Instant.now());
        users.put(user.getId(), user);
        userRoles.put(user.getId(), ConcurrentHashMap.newKeySet());
        return user;
    }

    public User getUserById(Long id) {
        return users.get(id);
    }

    public User getUserByUsername(String username) {
        return users.values().stream()
            .filter(u -> u.getUsername().equals(username))
            .findFirst()
            .orElse(null);
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    public User updateUser(Long id, String displayName, String email, String phone, String status) {
        User user = users.get(id);
        if (user != null) {
            if (displayName != null) user.setDisplayName(displayName);
            if (email != null) user.setEmail(email);
            if (phone != null) user.setPhone(phone);
            if (status != null) user.setStatus(status);
            user.setUpdatedAt(Instant.now());
        }
        return user;
    }

    public void deleteUser(Long id) {
        users.remove(id);
        userRoles.remove(id);
    }

    public void assignRoles(User user, List<Role> roles) {
        Set<Long> roleIds = userRoles.computeIfAbsent(user.getId(), k -> ConcurrentHashMap.newKeySet());
        roleIds.clear();
        for (Role role : roles) {
            roleIds.add(role.getId());
        }
    }

    public List<Role> getUserRoles(User user) {
        Set<Long> roleIds = userRoles.getOrDefault(user.getId(), new HashSet<>());
        return roles.values().stream()
            .filter(r -> roleIds.contains(r.getId()))
            .collect(Collectors.toList());
    }

    public Role createRole(String code, String name, String description) {
        Role role = new Role(roleIdGenerator.getAndIncrement(), code, name, description, "1", new ArrayList<>(), new ArrayList<>(), Instant.now(), Instant.now());
        roles.put(role.getId(), role);
        rolePermissions.put(role.getId(), ConcurrentHashMap.newKeySet());
        roleMenus.put(role.getId(), ConcurrentHashMap.newKeySet());
        return role;
    }

    public Role getRoleById(Long id) {
        return roles.get(id);
    }

    public Role getRoleByCode(String code) {
        return roles.values().stream()
            .filter(r -> r.getCode().equals(code))
            .findFirst()
            .orElse(null);
    }

    public List<Role> getAllRoles() {
        return new ArrayList<>(roles.values());
    }

    public Role updateRole(Long id, String name, String description, String status) {
        Role role = roles.get(id);
        if (role != null) {
            if (name != null) role.setName(name);
            if (description != null) role.setDescription(description);
            if (status != null) role.setStatus(status);
            role.setUpdatedAt(Instant.now());
        }
        return role;
    }

    public void deleteRole(Long id) {
        roles.remove(id);
        rolePermissions.remove(id);
        roleMenus.remove(id);
    }

    public void assignPermissions(Role role, List<Permission> perms) {
        Set<Long> permIds = rolePermissions.computeIfAbsent(role.getId(), k -> ConcurrentHashMap.newKeySet());
        permIds.clear();
        for (Permission p : perms) {
            permIds.add(p.getId());
        }
        role.setPermissions(perms.stream().map(Permission::getCode).collect(Collectors.toList()));
    }

    public List<Permission> getRolePermissions(Role role) {
        Set<Long> permIds = rolePermissions.getOrDefault(role.getId(), new HashSet<>());
        return permissions.values().stream()
            .filter(p -> permIds.contains(p.getId()))
            .collect(Collectors.toList());
    }

    public void assignMenus(Role role, List<Menu> menus) {
        Set<Long> menuIds = roleMenus.computeIfAbsent(role.getId(), k -> ConcurrentHashMap.newKeySet());
        menuIds.clear();
        for (Menu m : menus) {
            menuIds.add(m.getId());
        }
    }

    public List<Menu> getRoleMenus(Role role) {
        Set<Long> menuIds = roleMenus.getOrDefault(role.getId(), new HashSet<>());
        return menus.values().stream()
            .filter(m -> menuIds.contains(m.getId()))
            .collect(Collectors.toList());
    }

    public Permission createPermission(String code, String name, String type, String description) {
        Permission permission = new Permission(permissionIdGenerator.getAndIncrement(), code, name, type, null, null, description, null, 1, null, "1", Instant.now(), Instant.now());
        permissions.put(permission.getId(), permission);
        return permission;
    }

    public Permission getPermissionById(Long id) {
        return permissions.get(id);
    }

    public List<Permission> getAllPermissions() {
        return new ArrayList<>(permissions.values());
    }

    public Permission updatePermission(Long id, String name, String type, String description, String status) {
        Permission permission = permissions.get(id);
        if (permission != null) {
            if (name != null) permission.setName(name);
            if (type != null) permission.setType(type);
            if (description != null) permission.setDescription(description);
            if (status != null) permission.setStatus(status);
            permission.setUpdatedAt(Instant.now());
        }
        return permission;
    }

    public void deletePermission(Long id) {
        permissions.remove(id);
    }

    public Menu createMenu(String name, String path, String component, Long parentId, String type, String perms, String icon, Integer orderNum) {
        Menu menu = new Menu(menuIdGenerator.getAndIncrement(), name, path, component, parentId, type, perms, icon, orderNum, "1", Instant.now(), Instant.now());
        menus.put(menu.getId(), menu);
        return menu;
    }

    public Menu getMenuById(Long id) {
        return menus.get(id);
    }

    public List<Menu> getAllMenus() {
        return new ArrayList<>(menus.values());
    }

    public List<Menu> getMenuTree() {
        return buildMenuTree(getAllMenus(), 0L);
    }

    private List<Menu> buildMenuTree(List<Menu> allMenus, Long parentId) {
        return allMenus.stream()
            .filter(m -> Objects.equals(m.getParentId(), parentId))
            .peek(m -> {
                List<Menu> children = buildMenuTree(allMenus, m.getId());
                if (!children.isEmpty()) {
                }
            })
            .sorted(Comparator.comparing(Menu::getOrderNum))
            .collect(Collectors.toList());
    }

    public Menu updateMenu(Long id, String name, String path, String component, String type, String perms, String icon, Integer orderNum, String status) {
        Menu menu = menus.get(id);
        if (menu != null) {
            if (name != null) menu.setName(name);
            if (path != null) menu.setPath(path);
            if (component != null) menu.setComponent(component);
            if (type != null) menu.setType(type);
            if (perms != null) menu.setPerms(perms);
            if (icon != null) menu.setIcon(icon);
            if (orderNum != null) menu.setOrderNum(orderNum);
            if (status != null) menu.setStatus(status);
            menu.setUpdatedAt(Instant.now());
        }
        return menu;
    }

    public void deleteMenu(Long id) {
        menus.remove(id);
    }

    public List<String> getPermissionsForRoles(List<Role> userRoles) {
        return userRoles.stream()
            .flatMap(role -> getRolePermissions(role).stream())
            .map(Permission::getCode)
            .distinct()
            .collect(Collectors.toList());
    }

    public List<Menu> getMenusForRoles(List<Role> userRoles) {
        Set<Long> menuIds = userRoles.stream()
            .flatMap(role -> getRoleMenus(role).stream())
            .map(Menu::getId)
            .collect(Collectors.toSet());
        return menus.values().stream()
            .filter(m -> menuIds.contains(m.getId()))
            .sorted(Comparator.comparing(Menu::getOrderNum))
            .collect(Collectors.toList());
    }
}
