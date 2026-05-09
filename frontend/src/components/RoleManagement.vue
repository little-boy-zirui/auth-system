<script setup>
import { ref, onMounted } from 'vue'
import { useAuthStore } from '../stores/auth'

const authStore = useAuthStore()

const roles = ref([
  { code: 'ADMIN', name: '管理员', description: '拥有所有权限', permissions: ['system:user:view', 'system:user:create', 'system:user:edit', 'system:user:delete', 'system:role:view', 'system:role:edit', 'system:menu:view'] },
  { code: 'USER', name: '普通用户', description: '可以编辑但不能删除', permissions: ['system:user:view', 'system:user:create', 'system:role:view'] },
  { code: 'VIEWER', name: '访客', description: '只能查看', permissions: ['system:user:view', 'system:role:view'] }
])

const permissions = ref([
  { code: 'system:user:view', name: '查看用户', type: 'menu' },
  { code: 'system:user:create', name: '创建用户', type: 'button' },
  { code: 'system:user:edit', name: '编辑用户', type: 'button' },
  { code: 'system:user:delete', name: '删除用户', type: 'button' },
  { code: 'system:role:view', name: '查看角色', type: 'menu' },
  { code: 'system:role:edit', name: '编辑角色', type: 'button' },
  { code: 'system:menu:view', name: '查看菜单', type: 'menu' }
])

async function loadRoles() {
  if (!authStore.hasPermission('system:role:view')) {
    return
  }
  
  try {
    const response = await fetch('/api/rbac/roles', {
      credentials: 'include'
    })
    if (response.ok) {
      roles.value = await response.json()
    }
  } catch (error) {
    console.error('加载角色失败:', error)
  }
}

onMounted(() => {
  loadRoles()
})
</script>

<template>
  <div class="role-management">
    <div class="page-header">
      <h2>角色管理</h2>
    </div>
    
    <div v-if="!authStore.hasPermission('system:role:view')" class="no-permission">
      <p>你没有访问角色管理的权限</p>
    </div>
    
    <div v-else class="role-cards">
      <div v-for="role in roles" :key="role.code" class="role-card">
        <div class="role-header">
          <h3>{{ role.name }}</h3>
          <span class="role-code">{{ role.code }}</span>
        </div>
        
        <p class="role-description">{{ role.description || '暂无描述' }}</p>
        
        <div class="role-permissions">
          <h4>权限列表</h4>
          <div class="permission-tags">
            <span v-for="perm in role.permissions" :key="perm" class="perm-tag">
              {{ perm }}
            </span>
            <span v-if="!role.permissions || role.permissions.length === 0" class="no-data">暂无权限</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.role-management {
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

.no-permission {
  padding: 40px;
  text-align: center;
  color: #999;
  background: #f5f5f5;
  border-radius: 8px;
}

.role-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.role-card {
  background: #fff;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  padding: 20px;
  transition: all 0.3s;
}

.role-card:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.role-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.role-header h3 {
  color: #333;
  font-size: 16px;
  margin: 0;
}

.role-code {
  padding: 4px 8px;
  background: #e6f7ff;
  color: #1890ff;
  border-radius: 4px;
  font-size: 12px;
}

.role-description {
  color: #666;
  font-size: 14px;
  margin-bottom: 16px;
}

.role-permissions h4 {
  color: #333;
  font-size: 14px;
  margin-bottom: 12px;
}

.permission-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.perm-tag {
  padding: 4px 8px;
  background: #f5f5f5;
  color: #666;
  border-radius: 4px;
  font-size: 12px;
  font-family: 'Courier New', monospace;
}

.no-data {
  color: #999;
  font-size: 13px;
}
</style>
