<script setup>
import { ref, onMounted } from 'vue'
import { useAuthStore } from './stores/auth'
import { useMenuStore } from './stores/menu'
import Sidebar from './components/Sidebar.vue'
import Navbar from './components/Navbar.vue'
import UserManagement from './components/UserManagement.vue'
import RoleManagement from './components/RoleManagement.vue'
import MenuManagement from './components/MenuManagement.vue'
import PermissionManagement from './components/PermissionManagement.vue'

const authStore = useAuthStore()
const menuStore = useMenuStore()

const currentView = ref('dashboard')

const loginForm = ref({
  username: 'admin',
  password: 'admin123'
})

async function handleLogin() {
  await authStore.login(loginForm.value.username, loginForm.value.password)
  await menuStore.loadMenus()
}

function handleNavigate({ path, menu }) {
  currentView.value = path.replace('/', '') || 'dashboard'
}

onMounted(() => {
  authStore.loadUser()
  menuStore.loadMenus()
})
</script>

<template>
  <div v-if="!authStore.isAuthenticated" class="login-container">
    <div class="login-box">
      <h1 class="login-title">认证管理系统</h1>
      <p class="login-subtitle">Spring Boot 3 + Vue 3 + RBAC</p>
      
      <form @submit.prevent="handleLogin" class="login-form">
        <div class="form-item">
          <label>用户名</label>
          <input v-model="loginForm.username" type="text" placeholder="请输入用户名" />
        </div>
        
        <div class="form-item">
          <label>密码</label>
          <input v-model="loginForm.password" type="password" placeholder="请输入密码" />
        </div>
        
        <button type="submit" class="login-btn" :disabled="authStore.loading">
          {{ authStore.loading ? '登录中...' : '登录' }}
        </button>
        
        <p v-if="authStore.error" class="error-text">{{ authStore.error }}</p>
        
        <div class="login-tips">
          <p>默认账号：</p>
          <p>admin / admin123（管理员）</p>
          <p>demo / demo123（普通用户）</p>
        </div>
      </form>
    </div>
  </div>
  
  <div v-else class="layout">
    <Sidebar @navigate="handleNavigate" />
    
    <div class="main-container">
      <Navbar />
      
      <main class="content">
        <div v-if="currentView === 'dashboard'" class="page-content">
          <h2>欢迎使用认证管理系统</h2>
          
          <div class="info-cards">
            <div class="info-card">
              <div class="card-icon">👤</div>
              <div class="card-content">
                <div class="card-label">当前用户</div>
                <div class="card-value">{{ authStore.user?.displayName }}</div>
              </div>
            </div>
            
            <div class="info-card">
              <div class="card-icon">🎭</div>
              <div class="card-content">
                <div class="card-label">角色</div>
                <div class="card-value">{{ authStore.roles.join(', ') || '无' }}</div>
              </div>
            </div>
            
            <div class="info-card">
              <div class="card-icon">🔐</div>
              <div class="card-content">
                <div class="card-label">权限数量</div>
                <div class="card-value">{{ authStore.permissions.length }}</div>
              </div>
            </div>
          </div>
          
          <div class="permissions-section">
            <h3>我的权限</h3>
            <div class="permission-tags">
              <span v-for="perm in authStore.permissions" :key="perm" class="perm-tag">
                {{ perm }}
              </span>
              <span v-if="!authStore.permissions.length" class="no-data">暂无权限</span>
            </div>
          </div>
        </div>
        
        <div v-if="currentView === 'systemuser'" class="page-content">
          <UserManagement />
        </div>
        
        <div v-if="currentView === 'systemrole'" class="page-content">
          <RoleManagement />
        </div>
        
        <div v-if="currentView === 'systemmenu'" class="page-content">
          <MenuManagement />
        </div>
        
        <div v-if="currentView === 'systempermission'" class="page-content">
          <PermissionManagement />
        </div>
      </main>
    </div>
  </div>
</template>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', sans-serif;
  background: #f0f2f5;
}

.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-box {
  background: #fff;
  padding: 40px;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  width: 400px;
}

.login-title {
  text-align: center;
  color: #333;
  margin-bottom: 8px;
  font-size: 24px;
}

.login-subtitle {
  text-align: center;
  color: #666;
  margin-bottom: 32px;
  font-size: 14px;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-item label {
  color: #333;
  font-size: 14px;
  font-weight: 500;
}

.form-item input {
  padding: 10px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  font-size: 14px;
  transition: all 0.3s;
}

.form-item input:focus {
  border-color: #1890ff;
  outline: none;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
}

.login-btn {
  padding: 12px;
  background: #1890ff;
  color: #fff;
  border: none;
  border-radius: 4px;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.3s;
}

.login-btn:hover {
  background: #40a9ff;
}

.login-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.error-text {
  color: #ff4d4f;
  font-size: 14px;
  text-align: center;
}

.login-tips {
  margin-top: 16px;
  padding: 12px;
  background: #f5f5f5;
  border-radius: 4px;
  font-size: 13px;
  color: #666;
}

.login-tips p {
  margin: 4px 0;
}

.layout {
  display: flex;
  height: 100vh;
  overflow: hidden;
}

.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.content {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
}

.page-content {
  background: #fff;
  padding: 24px;
  border-radius: 8px;
  min-height: calc(100vh - 144px);
}

.page-content h2 {
  margin-bottom: 24px;
  color: #333;
  font-size: 20px;
}

.info-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 32px;
}

.info-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 8px;
  color: #fff;
}

.card-icon {
  font-size: 36px;
}

.card-content {
  flex: 1;
}

.card-label {
  font-size: 13px;
  opacity: 0.9;
  margin-bottom: 4px;
}

.card-value {
  font-size: 20px;
  font-weight: 600;
}

.permissions-section {
  margin-top: 32px;
}

.permissions-section h3 {
  margin-bottom: 16px;
  color: #333;
  font-size: 16px;
}

.permission-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.perm-tag {
  padding: 6px 12px;
  background: #e6f7ff;
  color: #1890ff;
  border-radius: 4px;
  font-size: 13px;
  font-family: 'Courier New', monospace;
}

.no-data {
  color: #999;
  font-size: 14px;
}
</style>
