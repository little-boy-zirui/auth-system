<script setup>
import { useAuthStore } from '../stores/auth'

const authStore = useAuthStore()

const menus = [
  {
    id: 1,
    title: '首页',
    type: 'directory',
    path: '/dashboard',
    icon: '🏠',
    children: []
  },
  {
    id: 2,
    title: '系统管理',
    type: 'directory',
    path: '/system',
    icon: '⚙️',
    children: [
      { id: 21, title: '用户管理', type: 'menu', path: '/system/user', perms: 'system:user:view' },
      { id: 22, title: '角色管理', type: 'menu', path: '/system/role', perms: 'system:role:view' },
      { id: 23, title: '菜单管理', type: 'menu', path: '/system/menu', perms: 'system:menu:view' },
      { id: 24, title: '权限配置', type: 'menu', path: '/system/permission', perms: 'system:permission:view' }
    ]
  }
]
</script>

<template>
  <div class="menu-management">
    <div class="page-header">
      <h2>菜单管理</h2>
    </div>
    
    <div class="menu-tree">
      <div v-for="menu in menus" :key="menu.id" class="menu-node">
        <div class="menu-item">
          <span class="menu-icon">{{ menu.icon }}</span>
          <span class="menu-title">{{ menu.title }}</span>
          <span class="menu-type">{{ menu.type === 'directory' ? '目录' : '菜单' }}</span>
          <span v-if="menu.perms" class="menu-perms">{{ menu.perms }}</span>
        </div>
        
        <div v-if="menu.children && menu.children.length > 0" class="submenu">
          <div v-for="child in menu.children" :key="child.id" class="menu-item child">
            <span class="menu-icon">{{ child.icon || '📄' }}</span>
            <span class="menu-title">{{ child.title }}</span>
            <span class="menu-type">菜单</span>
            <span v-if="child.perms" class="menu-perms">{{ child.perms }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.menu-management {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-header h2 {
  color: #333;
  font-size: 18px;
  margin: 0;
}

.menu-tree {
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  padding: 16px;
  background: #fafafa;
}

.menu-node {
  margin-bottom: 8px;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #fff;
  border: 1px solid #e8e8e8;
  border-radius: 4px;
  margin-bottom: 4px;
}

.menu-item.child {
  margin-left: 32px;
  background: #f9f9f9;
}

.menu-icon {
  font-size: 18px;
}

.menu-title {
  color: #333;
  font-weight: 500;
  flex: 1;
}

.menu-type {
  padding: 2px 8px;
  background: #f0f0f0;
  color: #666;
  border-radius: 4px;
  font-size: 12px;
}

.menu-perms {
  padding: 2px 8px;
  background: #e6f7ff;
  color: #1890ff;
  border-radius: 4px;
  font-size: 12px;
  font-family: 'Courier New', monospace;
}
</style>
