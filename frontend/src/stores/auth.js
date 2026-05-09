import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useAuthStore = defineStore('auth', () => {
  const user = ref(null)
  const loading = ref(false)
  const error = ref('')

  const isAuthenticated = computed(() => user.value?.authenticated)
  const roles = computed(() => user.value?.roles || [])
  const permissions = computed(() => user.value?.permissions || [])

  function hasPermission(permissionCode) {
    if (!user.value?.authenticated) {
      return false
    }
    const userPermissions = user.value.permissions || []
    return userPermissions.includes(permissionCode)
  }

  function hasRole(roleCode) {
    if (!user.value?.authenticated) {
      return false
    }
    const userRoles = user.value.roles || []
    return userRoles.includes(roleCode) || userRoles.includes(`ROLE_${roleCode}`)
  }

  async function loadUser() {
    loading.value = true
    error.value = ''
    try {
      const response = await fetch('/api/auth/me', {
        credentials: 'include'
      })
      user.value = await response.json()
    } catch (e) {
      error.value = e.message
    } finally {
      loading.value = false
    }
  }

  async function login(username, password) {
    loading.value = true
    error.value = ''
    try {
      const response = await fetch('/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify({ username, password })
      })
      
      if (!response.ok) {
        const text = await response.text()
        throw new Error(text || '登录失败')
      }
      
      user.value = await response.json()
      return user.value
    } catch (e) {
      error.value = e.message
      throw e
    } finally {
      loading.value = false
    }
  }

  async function logout() {
    loading.value = true
    error.value = ''
    try {
      await fetch('/api/auth/logout', {
        method: 'POST',
        credentials: 'include'
      })
      user.value = null
    } catch (e) {
      error.value = e.message
    } finally {
      loading.value = false
    }
  }

  return {
    user,
    loading,
    error,
    isAuthenticated,
    roles,
    permissions,
    hasPermission,
    hasRole,
    loadUser,
    login,
    logout
  }
})
