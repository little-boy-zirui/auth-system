<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useAuthStore } from './stores/auth'

const authStore = useAuthStore()

const state = reactive({
  loginForm: {
    username: 'admin',
    password: 'admin123'
  },
  ssoForm: {
    username: 'demo',
    service: 'http://localhost:5173/sso-client'
  },
  ssoResult: null,
  activeTab: 'login'
})

const authProviders = computed(() => {
  if (!authStore.isAuthenticated) {
    return []
  }
  return authStore.roles ?? []
})

async function loginLocal() {
  try {
    await authStore.login(state.loginForm.username, state.loginForm.password)
  } catch (error) {
    console.error('登录失败:', error)
  }
}

function loginOAuth2() {
  window.location.href = '/oauth2/authorization/github?redirect_uri=http://localhost:5173/'
}

async function logout() {
  await authStore.logout()
  state.ssoResult = null
}

async function createSsoTicket() {
  state.loading = true
  state.error = ''
  try {
    const response = await fetch('/api/sso/login', {
      method: 'POST',
      credentials: 'include',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(state.ssoForm)
    })
    state.ssoResult = await response.json()
  } catch (error) {
    console.error('SSO Ticket 创建失败:', error)
  }
}

async function consumeSsoTicket() {
  if (!state.ssoResult?.ticket) {
    return
  }
  try {
    const response = await fetch(`/api/sso/validate?ticket=${encodeURIComponent(state.ssoResult.ticket)}&service=${encodeURIComponent(state.ssoResult.service)}`, {
      credentials: 'include'
    })
    authStore.user = await response.json()
  } catch (error) {
    console.error('SSO Ticket 验证失败:', error)
  }
}

async function loadUsers() {
  try {
    const response = await fetch('/api/auth/users', {
      credentials: 'include'
    })
    if (response.ok) {
      return await response.json()
    }
  } catch (error) {
    console.error('加载用户失败:', error)
  }
  return []
}

const users = ref([])
async function refreshUsers() {
  users.value = await loadUsers()
}

onMounted(() => {
  authStore.loadUser()
})
</script>

<template>
  <main class="page-shell">
    <section class="hero-card">
      <div>
        <p class="eyebrow">Spring Boot 3 + Vue 3 + RBAC</p>
        <h1>统一认证与权限管理系统</h1>
        <p class="hero-copy">
          包含本地账号登录、OAuth2.1 第三方登录、简化版 SSO，以及完整的 RBAC 权限控制（含按钮级权限）。
        </p>
      </div>
      <div class="status-card">
        <p class="status-label">当前认证状态</p>
        <strong>{{ authStore.isAuthenticated ? '已登录' : '未登录' }}</strong>
        <p>{{ authStore.user?.displayName || '匿名访问' }}</p>
        <p v-if="authStore.user?.provider">认证方式：{{ authStore.user.provider }}</p>
      </div>
    </section>

    <section class="panel-grid" v-if="!authStore.isAuthenticated">
      <article class="panel">
        <h2>本地登录</h2>
        <p class="muted">内置演示账号：`admin/admin123`（ADMIN 角色）、`demo/demo123`（USER 角色）</p>
        <label>
          用户名
          <input v-model="state.loginForm.username" placeholder="请输入用户名" />
        </label>
        <label>
          密码
          <input v-model="state.loginForm.password" type="password" placeholder="请输入密码" />
        </label>
        <button :disabled="authStore.loading" @click="loginLocal">本地登录</button>
      </article>

      <article class="panel accent-panel">
        <h2>OAuth2.1 登录</h2>
        <p class="muted">默认接入 GitHub。配置 `GITHUB_CLIENT_ID` 与 `GITHUB_CLIENT_SECRET` 后可直接使用。</p>
        <button class="secondary" @click="loginOAuth2">使用 GitHub 登录</button>
      </article>

      <article class="panel">
        <h2>SSO 单点登录</h2>
        <p class="muted">演示 CAS 风格 ticket 流程：登录认证中心后换取一次性票据。</p>
        <label>
          SSO 用户
          <input v-model="state.ssoForm.username" placeholder="请输入 SSO 用户名" />
        </label>
        <label>
          Service URL
          <input v-model="state.ssoForm.service" placeholder="请输入回调服务地址" />
        </label>
        <div class="button-row">
          <button :disabled="authStore.loading" @click="createSsoTicket">生成 Ticket</button>
          <button class="secondary" :disabled="!state.ssoResult" @click="consumeSsoTicket">验证 Ticket</button>
        </div>
        <p v-if="state.ssoResult" class="ticket-box">{{ state.ssoResult.ticket }}</p>
      </article>
    </section>

    <section v-else class="dashboard">
      <div class="dashboard-header">
        <h2>欢迎，{{ authStore.user?.displayName }}</h2>
        <div class="user-actions">
          <button class="secondary" @click="authStore.loadUser">刷新状态</button>
          <button class="ghost" @click="logout">退出登录</button>
        </div>
      </div>

      <div class="dashboard-grid">
        <article class="dash-panel">
          <h3>我的角色</h3>
          <div class="tag-row">
            <span v-for="role in authProviders" :key="role" class="tag">{{ role.replace('ROLE_', '') }}</span>
            <span v-if="!authProviders.length" class="tag muted-tag">暂无角色</span>
          </div>
        </article>

        <article class="dash-panel">
          <h3>我的权限</h3>
          <div class="permission-list">
            <span v-for="perm in authStore.permissions" :key="perm" class="perm-tag">{{ perm }}</span>
            <span v-if="!authStore.permissions.length" class="muted-tag">暂无权限</span>
          </div>
        </article>
      </div>

      <article class="dash-panel full-width">
        <div class="panel-tabs">
          <button :class="{ active: state.activeTab === 'users' }" @click="state.activeTab = 'users'; refreshUsers()">用户管理</button>
          <button :class="{ active: state.activeTab === 'roles' }" @click="state.activeTab = 'roles'">角色管理</button>
        </div>

        <div v-if="state.activeTab === 'users'" class="tab-content">
          <h3>用户列表</h3>
          <p v-if="!authStore.hasPermission('user:view')" class="no-permission">你没有查看用户的权限</p>
          <div v-else class="table-container">
            <table class="data-table" v-if="users.length > 0">
              <thead>
                <tr>
                  <th>用户名</th>
                  <th>角色</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="u in users" :key="u.username">
                  <td>{{ u.username }}</td>
                  <td>{{ u.role }}</td>
                </tr>
              </tbody>
            </table>
            <p v-else class="muted">暂无用户数据</p>
          </div>
          <div class="button-row" v-permission="'user:create'">
            <button>创建用户</button>
          </div>
        </div>

        <div v-if="state.activeTab === 'roles'" class="tab-content">
          <h3>角色列表</h3>
          <p v-if="!authStore.hasPermission('role:view')" class="no-permission">你没有查看角色的权限</p>
          <div v-else class="role-cards">
            <div class="role-card">
              <h4>ADMIN - 系统管理员</h4>
              <p class="muted">拥有所有权限</p>
              <div class="perm-list">
                <span class="perm-tag">user:view</span>
                <span class="perm-tag">user:create</span>
                <span class="perm-tag">user:edit</span>
                <span class="perm-tag">user:delete</span>
                <span class="perm-tag">role:view</span>
                <span class="perm-tag">role:assign</span>
              </div>
            </div>
            <div class="role-card">
              <h4>EDITOR - 编辑员</h4>
              <p class="muted">可以编辑但不能删除</p>
              <div class="perm-list">
                <span class="perm-tag">user:view</span>
                <span class="perm-tag">user:create</span>
                <span class="perm-tag">user:edit</span>
                <span class="perm-tag">role:view</span>
              </div>
            </div>
            <div class="role-card">
              <h4>VIEWER - 访客</h4>
              <p class="muted">只能查看</p>
              <div class="perm-list">
                <span class="perm-tag">user:view</span>
                <span class="perm-tag">role:view</span>
              </div>
            </div>
          </div>
        </div>
      </article>
    </section>
  </main>
</template>

<style>
.dashboard {
  margin-top: 24px;
}

.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.dashboard-header h2 {
  margin: 0;
  color: #f4f7ff;
}

.user-actions {
  display: flex;
  gap: 12px;
}

.dashboard-grid {
  display: grid;
  gap: 20px;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  margin-bottom: 24px;
}

.dash-panel {
  border: 1px solid rgba(255, 255, 255, 0.08);
  background: rgba(6, 12, 24, 0.68);
  backdrop-filter: blur(18px);
  border-radius: 24px;
  padding: 24px;
}

.dash-panel.full-width {
  grid-column: 1 / -1;
}

.dash-panel h3 {
  margin: 0 0 16px;
  color: #8eb2ff;
  font-size: 16px;
}

.permission-list,
.perm-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.perm-tag {
  padding: 8px 12px;
  border-radius: 999px;
  background: rgba(127, 180, 255, 0.14);
  color: #d8e8ff;
  font-size: 13px;
  font-family: monospace;
}

.panel-tabs {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  padding-bottom: 16px;
}

.panel-tabs button {
  border: none;
  border-radius: 12px;
  padding: 10px 18px;
  cursor: pointer;
  color: #aeb9cd;
  background: rgba(255, 255, 255, 0.04);
  font-weight: 600;
}

.panel-tabs button.active {
  color: #04101f;
  background: linear-gradient(135deg, #7ca6ff, #80ffd4);
}

.tab-content h3 {
  margin: 0 0 16px;
  color: #8eb2ff;
  font-size: 16px;
}

.no-permission {
  padding: 24px;
  text-align: center;
  color: #ff8d98;
  background: rgba(255, 92, 122, 0.08);
  border-radius: 16px;
}

.table-container {
  overflow-x: auto;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th,
.data-table td {
  padding: 14px 16px;
  text-align: left;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}

.data-table th {
  color: #8eb2ff;
  font-weight: 600;
  font-size: 13px;
  text-transform: uppercase;
}

.data-table tr:hover {
  background: rgba(255, 255, 255, 0.02);
}

.role-cards {
  display: grid;
  gap: 16px;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
}

.role-card {
  padding: 20px;
  background: rgba(255, 255, 255, 0.03);
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.06);
}

.role-card h4 {
  margin: 0 0 8px;
  color: #dbe4f6;
}

.role-card .muted {
  font-size: 13px;
  margin-bottom: 16px;
}

@media (max-width: 960px) {
  .dashboard-grid {
    grid-template-columns: 1fr;
  }

  .dashboard-header {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }

  .role-cards {
    grid-template-columns: 1fr;
  }
}
</style>
