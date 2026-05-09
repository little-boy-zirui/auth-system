import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useAuthStore = defineStore('auth', () => {
  const user = ref(null)
  const loading = ref(false)
  const error = ref('')
  const permissions = ref([])
  const roles = ref([])

  const isAuthenticated = computed(() => user.value?.authenticated)

  function hasPermission(permissionCode) {
    if (!user.value?.authenticated) {
      return false
    }
    const userPermissions = user.value.permissions || []
    return userPermissions.includes(permissionCode) || userPermissions.includes('*:*:*')
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
      if (user.value?.permissions) {
        permissions.value = user.value.permissions
      }
      if (user.value?.roles) {
        roles.value = user.value.roles
      }
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
      if (user.value?.permissions) {
        permissions.value = user.value.permissions
      }
      if (user.value?.roles) {
        roles.value = user.value.roles
      }
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
      permissions.value = []
      roles.value = []
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
    permissions,
    roles,
    isAuthenticated,
    hasPermission,
    hasRole,
    loadUser,
    login,
    logout
  }
})
