<script setup>
import { computed } from 'vue'
import { useAuthStore } from '../stores/auth'

const authStore = useAuthStore()

const userInfo = computed(() => authStore.user)
</script>

<template>
  <header class="navbar">
    <div class="navbar-left">
      <slot name="breadcrumb">
        <div class="breadcrumb">
          <span>首页</span>
          <span class="separator">/</span>
          <span>控制台</span>
        </div>
      </slot>
    </div>
    
    <div class="navbar-right">
      <div class="user-dropdown">
        <div class="user-info">
          <span class="username">{{ userInfo?.displayName || userInfo?.username || '用户' }}</span>
          <span class="role-tag" v-if="userInfo?.roles && userInfo.roles.length > 0">
            {{ userInfo.roles[0].replace('ROLE_', '') }}
          </span>
        </div>
        <div class="dropdown-menu">
          <button @click="authStore.logout" class="dropdown-item">
            退出登录
          </button>
        </div>
      </div>
    </div>
  </header>
</template>

<style scoped>
.navbar {
  height: 60px;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
}

.navbar-left {
  display: flex;
  align-items: center;
}

.breadcrumb {
  display: flex;
  align-items: center;
  color: rgba(0, 0, 0, 0.45);
  font-size: 14px;
}

.breadcrumb .separator {
  margin: 0 8px;
}

.navbar-right {
  display: flex;
  align-items: center;
}

.user-dropdown {
  position: relative;
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 8px 16px;
  border-radius: 4px;
  transition: all 0.3s;
}

.user-dropdown:hover {
  background: #f5f5f5;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.username {
  color: rgba(0, 0, 0, 0.85);
  font-size: 14px;
}

.role-tag {
  background: #e6f7ff;
  color: #1890ff;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.dropdown-menu {
  position: absolute;
  top: 100%;
  right: 0;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  border-radius: 4px;
  padding: 8px 0;
  min-width: 120px;
  display: none;
  z-index: 1000;
}

.user-dropdown:hover .dropdown-menu {
  display: block;
}

.dropdown-item {
  width: 100%;
  padding: 8px 16px;
  text-align: left;
  background: none;
  border: none;
  cursor: pointer;
  color: rgba(0, 0, 0, 0.85);
  font-size: 14px;
  transition: all 0.3s;
}

.dropdown-item:hover {
  background: #f5f5f5;
  color: #ff4d4f;
}
</style>
