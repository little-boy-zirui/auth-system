<script setup>
import { computed, ref } from 'vue'
import { useAuthStore } from '../stores/auth'
import { useMenuStore } from '../stores/menu'

const authStore = useAuthStore()
const menuStore = useMenuStore()

const sidebarCollapse = ref(false)

const visibleMenus = computed(() => {
  if (!authStore.isAuthenticated) {
    return []
  }
  if (authStore.hasPermission('*:*:*')) {
    return menuStore.menus
  }
  return filterMenusByPermission(menuStore.menus)
})

function filterMenusByPermission(menus) {
  return menus.map(menu => {
    if (menu.children && menu.children.length > 0) {
      const filteredChildren = filterMenusByPermission(menu.children)
      if (filteredChildren.length > 0) {
        return { ...menu, children: filteredChildren }
      }
    }
    if (!menu.perms || authStore.hasPermission(menu.perms)) {
      return menu
    }
    return null
  }).filter(m => m !== null)
}

function navigateTo(path, menu) {
  menuStore.setActiveMenu(path)
  emit('navigate', { path, menu })
}

const emit = defineEmits(['navigate'])
</script>

<template>
  <aside :class="['sidebar', { collapsed: sidebarCollapse }]">
    <div class="sidebar-header">
      <h1 v-if="!sidebarCollapse">认证管理系统</h1>
      <span v-else class="logo">认证</span>
      <button class="toggle-btn" @click="sidebarCollapse = !sidebarCollapse">
        {{ sidebarCollapse ? '→' : '←' }}
      </button>
    </div>
    
    <nav class="sidebar-nav">
      <div v-for="menu in visibleMenus" :key="menu.id" class="menu-item">
        <div 
          v-if="menu.children && menu.children.length > 0"
          class="menu-title"
        >
          <span class="menu-icon">{{ menu.icon }}</span>
          <span v-if="!sidebarCollapse" class="menu-text">{{ menu.title }}</span>
        </div>
        
        <div v-if="menu.children && menu.children.length > 0" class="submenu">
          <div 
            v-for="child in menu.children" 
            :key="child.id"
            :class="['submenu-item', { active: menuStore.activeMenu === child.path }]"
            @click="navigateTo(child.path, child)"
          >
            <span class="menu-icon">{{ child.icon }}</span>
            <span v-if="!sidebarCollapse" class="menu-text">{{ child.title }}</span>
          </div>
        </div>
        
        <div 
          v-else
          :class="['menu-link', { active: menuStore.activeMenu === menu.path }]"
          @click="navigateTo(menu.path, menu)"
        >
          <span class="menu-icon">{{ menu.icon }}</span>
          <span v-if="!sidebarCollapse" class="menu-text">{{ menu.title }}</span>
        </div>
      </div>
    </nav>
  </aside>
</template>

<style scoped>
.sidebar {
  width: 240px;
  background: linear-gradient(180deg, #001529 0%, #002140 100%);
  height: 100vh;
  transition: width 0.3s ease;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.sidebar.collapsed {
  width: 64px;
}

.sidebar-header {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.sidebar-header h1 {
  color: #fff;
  font-size: 18px;
  margin: 0;
  white-space: nowrap;
}

.logo {
  color: #fff;
  font-size: 16px;
  font-weight: bold;
}

.toggle-btn {
  background: none;
  border: none;
  color: #fff;
  cursor: pointer;
  font-size: 18px;
  padding: 4px 8px;
  border-radius: 4px;
}

.toggle-btn:hover {
  background: rgba(255, 255, 255, 0.1);
}

.sidebar-nav {
  flex: 1;
  overflow-y: auto;
  padding: 8px 0;
}

.menu-item {
  margin-bottom: 4px;
}

.menu-title {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  color: rgba(255, 255, 255, 0.65);
  font-size: 14px;
  cursor: default;
}

.menu-icon {
  font-size: 16px;
  margin-right: 10px;
  width: 20px;
  text-align: center;
}

.menu-text {
  white-space: nowrap;
}

.submenu {
  background: rgba(255, 255, 255, 0.05);
  margin: 0 8px;
  border-radius: 4px;
  overflow: hidden;
}

.submenu-item {
  display: flex;
  align-items: center;
  padding: 10px 16px 10px 36px;
  color: rgba(255, 255, 255, 0.65);
  cursor: pointer;
  transition: all 0.3s;
}

.submenu-item:hover {
  background: rgba(255, 255, 255, 0.08);
  color: #fff;
}

.submenu-item.active {
  background: #1890ff;
  color: #fff;
}

.menu-link {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  color: rgba(255, 255, 255, 0.65);
  cursor: pointer;
  transition: all 0.3s;
}

.menu-link:hover {
  background: rgba(255, 255, 255, 0.08);
  color: #fff;
}

.menu-link.active {
  background: #1890ff;
  color: #fff;
}

.sidebar-nav::-webkit-scrollbar {
  width: 6px;
}

.sidebar-nav::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.2);
  border-radius: 3px;
}

.sidebar-nav::-webkit-scrollbar-track {
  background: transparent;
}
</style>
