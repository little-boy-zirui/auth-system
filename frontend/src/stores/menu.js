import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useMenuStore = defineStore('menu', () => {
  const menus = ref([])
  const activeMenu = ref('/dashboard')

  const defaultMenus = [
    {
      id: 1,
      title: '首页',
      path: '/dashboard',
      icon: '🏠',
      type: 'menu',
      children: []
    },
    {
      id: 2,
      title: '系统管理',
      path: '/system',
      icon: '⚙️',
      type: 'directory',
      children: [
        {
          id: 21,
          title: '用户管理',
          path: '/system/user',
          icon: '👤',
          type: 'menu',
          perms: 'system:user:view'
        },
        {
          id: 22,
          title: '角色管理',
          path: '/system/role',
          icon: '🎭',
          type: 'menu',
          perms: 'system:role:view'
        },
        {
          id: 23,
          title: '菜单管理',
          path: '/system/menu',
          icon: '📋',
          type: 'menu',
          perms: 'system:menu:view'
        },
        {
          id: 24,
          title: '权限配置',
          path: '/system/permission',
          icon: '🔐',
          type: 'menu',
          perms: 'system:permission:view'
        }
      ]
    }
  ]

  function setMenus(menuList) {
    menus.value = menuList || defaultMenus
  }

  function normalizeMenu(menu) {
    return {
      ...menu,
      title: menu.title || menu.name,
      children: Array.isArray(menu.children) ? menu.children.map(normalizeMenu) : []
    }
  }

  async function loadMenus() {
    try {
      const response = await fetch('/api/system/menus/tree', {
        credentials: 'include'
      })
      if (!response.ok) {
        setMenus(defaultMenus)
        return
      }
      const data = await response.json()
      setMenus(data.map(normalizeMenu))
    } catch {
      setMenus(defaultMenus)
    }
  }

  function setActiveMenu(path) {
    activeMenu.value = path
  }

  return {
    menus,
    activeMenu,
    setMenus,
    setActiveMenu,
    loadMenus
  }
})
