<script setup>
import { computed, onMounted, reactive, ref } from 'vue'

const state = reactive({
  me: null,
  loading: false,
  error: '',
  loginForm: {
    username: 'admin',
    password: 'admin123'
  },
  ssoForm: {
    username: 'demo',
    service: 'http://localhost:5173/sso-client'
  },
  ssoResult: null
})

const authProviders = computed(() => {
  if (!state.me?.authenticated) {
    return []
  }
  return state.me.roles ?? []
})

async function request(url, options = {}) {
  const response = await fetch(url, {
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json',
      ...(options.headers ?? {})
    },
    ...options
  })

  if (!response.ok) {
    const text = await response.text()
    throw new Error(text || '请求失败')
  }

  return response.json()
}

async function loadCurrentUser() {
  state.loading = true
  state.error = ''
  try {
    state.me = await request('/api/auth/me', { method: 'GET', headers: {} })
  } catch (error) {
    state.error = error.message
  } finally {
    state.loading = false
  }
}

async function loginLocal() {
  state.loading = true
  state.error = ''
  try {
    state.me = await request('/api/auth/login', {
      method: 'POST',
      body: JSON.stringify(state.loginForm)
    })
  } catch (error) {
    state.error = error.message
  } finally {
    state.loading = false
  }
}

function loginOAuth2() {
  window.location.href = '/oauth2/authorization/github?redirect_uri=http://localhost:5173/'
}

async function logout() {
  state.loading = true
  state.error = ''
  try {
    await request('/api/auth/logout', { method: 'POST', body: '{}' })
    state.me = await request('/api/auth/me', { method: 'GET', headers: {} })
    state.ssoResult = null
  } catch (error) {
    state.error = error.message
  } finally {
    state.loading = false
  }
}

async function createSsoTicket() {
  state.loading = true
  state.error = ''
  try {
    state.ssoResult = await request('/api/sso/login', {
      method: 'POST',
      body: JSON.stringify(state.ssoForm)
    })
  } catch (error) {
    state.error = error.message
  } finally {
    state.loading = false
  }
}

async function consumeSsoTicket() {
  if (!state.ssoResult?.ticket) {
    return
  }
  state.loading = true
  state.error = ''
  try {
    state.me = await request(`/api/sso/validate?ticket=${encodeURIComponent(state.ssoResult.ticket)}&service=${encodeURIComponent(state.ssoResult.service)}`, {
      method: 'GET',
      headers: {}
    })
  } catch (error) {
    state.error = error.message
  } finally {
    state.loading = false
  }
}

onMounted(loadCurrentUser)
</script>

<template>
  <main class="page-shell">
    <section class="hero-card">
      <div>
        <p class="eyebrow">Spring Boot 3 + Vue 3</p>
        <h1>统一登录认证系统</h1>
        <p class="hero-copy">
          包含本地账号登录、OAuth2.1 第三方登录，以及简化版 SSO 单点登录演示。
        </p>
      </div>
      <div class="status-card">
        <p class="status-label">当前认证状态</p>
        <strong>{{ state.me?.authenticated ? '已登录' : '未登录' }}</strong>
        <p>{{ state.me?.displayName || '匿名访问' }}</p>
        <p v-if="state.me?.provider">认证方式: {{ state.me.provider }}</p>
      </div>
    </section>

    <section class="panel-grid">
      <article class="panel">
        <h2>本地登录</h2>
        <p class="muted">内置演示账号：`admin/admin123`、`demo/demo123`</p>
        <label>
          用户名
          <input v-model="state.loginForm.username" placeholder="请输入用户名" />
        </label>
        <label>
          密码
          <input v-model="state.loginForm.password" type="password" placeholder="请输入密码" />
        </label>
        <button :disabled="state.loading" @click="loginLocal">本地登录</button>
      </article>

      <article class="panel accent-panel">
        <h2>OAuth2.1 登录</h2>
        <p class="muted">默认接入 GitHub。配置 `GITHUB_CLIENT_ID` 与 `GITHUB_CLIENT_SECRET` 后可直接使用。</p>
        <button class="secondary" :disabled="state.loading" @click="loginOAuth2">使用 GitHub 登录</button>
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
          <button :disabled="state.loading" @click="createSsoTicket">生成 Ticket</button>
          <button class="secondary" :disabled="state.loading || !state.ssoResult" @click="consumeSsoTicket">验证 Ticket</button>
        </div>
        <p v-if="state.ssoResult" class="ticket-box">{{ state.ssoResult.ticket }}</p>
      </article>
    </section>

    <section class="panel-grid bottom-grid">
      <article class="panel">
        <h2>登录用户信息</h2>
        <pre>{{ JSON.stringify(state.me, null, 2) }}</pre>
        <div class="button-row">
          <button class="secondary" :disabled="state.loading" @click="loadCurrentUser">刷新用户态</button>
          <button class="ghost" :disabled="state.loading" @click="logout">退出登录</button>
        </div>
      </article>

      <article class="panel">
        <h2>授权能力</h2>
        <div class="tag-row">
          <span v-for="role in authProviders" :key="role" class="tag">{{ role }}</span>
          <span v-if="!authProviders.length" class="tag muted-tag">暂无角色</span>
        </div>
        <p v-if="state.error" class="error-text">{{ state.error }}</p>
      </article>
    </section>
  </main>
</template>
