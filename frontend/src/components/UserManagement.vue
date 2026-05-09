<script setup>
import { ref, onMounted } from 'vue'
import { useAuthStore } from '../stores/auth'

const authStore = useAuthStore()

const users = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const editingUser = ref(null)

const form = ref({
  username: '',
  password: '',
  displayName: '',
  roles: []
})

const availableRoles = [
  { code: 'ADMIN', name: '管理员' },
  { code: 'USER', name: '普通用户' },
  { code: 'VIEWER', name: '访客' }
]

const columns = [
  { key: 'username', label: '用户名' },
  { key: 'displayName', label: '显示名称' },
  { key: 'roles', label: '角色' },
  { key: 'actions', label: '操作' }
]

async function loadUsers() {
  if (!authStore.hasPermission('system:user:view')) {
    return
  }
  
  loading.value = true
  try {
    const response = await fetch('/api/auth/users', {
      credentials: 'include'
    })
    if (response.ok) {
      users.value = await response.json()
    }
  } catch (error) {
    console.error('加载用户失败:', error)
  } finally {
    loading.value = false
  }
}

function handleAdd() {
  editingUser.value = null
  form.value = {
    username: '',
    password: '',
    displayName: '',
    roles: []
  }
  dialogVisible.value = true
}

function handleEdit(user) {
  editingUser.value = user
  form.value = {
    username: user.username,
    password: '',
    displayName: user.displayName,
    roles: user.roles || []
  }
  dialogVisible.value = true
}

async function handleDelete(user) {
  if (!confirm(`确定要删除用户 "${user.username}" 吗？`)) {
    return
  }
  
  if (!authStore.hasPermission('system:user:delete')) {
    alert('没有删除权限')
    return
  }
  
  try {
    const response = await fetch(`/api/auth/users/${user.username}`, {
      method: 'DELETE',
      credentials: 'include'
    })
    if (response.ok) {
      await loadUsers()
    }
  } catch (error) {
    console.error('删除用户失败:', error)
  }
}

async function handleSubmit() {
  try {
    const method = editingUser.value ? 'PUT' : 'POST'
    const response = await fetch('/api/auth/users', {
      method,
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include',
      body: JSON.stringify(form.value)
    })
    
    if (response.ok) {
      dialogVisible.value = false
      await loadUsers()
    }
  } catch (error) {
    console.error('保存用户失败:', error)
  }
}

onMounted(() => {
  loadUsers()
})
</script>

<template>
  <div class="user-management">
    <div class="page-header">
      <h2>用户管理</h2>
      <button v-permission="'system:user:create'" @click="handleAdd" class="btn-primary">
        + 新增用户
      </button>
    </div>
    
    <div v-if="!authStore.hasPermission('system:user:view')" class="no-permission">
      <p>你没有访问用户管理的权限</p>
    </div>
    
    <div v-else class="table-container">
      <table v-if="users.length > 0">
        <thead>
          <tr>
            <th v-for="col in columns" :key="col.key">{{ col.label }}</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="user in users" :key="user.username">
            <td>{{ user.username }}</td>
            <td>{{ user.displayName }}</td>
            <td>
              <span v-for="role in user.roles" :key="role" class="role-tag">
                {{ role.replace('ROLE_', '') }}
              </span>
            </td>
            <td>
              <button v-permission="'system:user:edit'" @click="handleEdit(user)" class="btn-link">编辑</button>
              <button v-permission="'system:user:delete'" @click="handleDelete(user)" class="btn-link danger">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
      
      <p v-else class="no-data">暂无用户数据</p>
    </div>
    
    <div v-if="dialogVisible" class="dialog-overlay" @click.self="dialogVisible = false">
      <div class="dialog">
        <div class="dialog-header">
          <h3>{{ editingUser ? '编辑用户' : '新增用户' }}</h3>
          <button @click="dialogVisible = false" class="close-btn">×</button>
        </div>
        
        <div class="dialog-body">
          <div class="form-item">
            <label>用户名</label>
            <input v-model="form.username" type="text" placeholder="请输入用户名" />
          </div>
          
          <div class="form-item">
            <label>密码</label>
            <input v-model="form.password" type="password" :placeholder="editingUser ? '不修改请留空' : '请输入密码'" />
          </div>
          
          <div class="form-item">
            <label>显示名称</label>
            <input v-model="form.displayName" type="text" placeholder="请输入显示名称" />
          </div>
          
          <div class="form-item">
            <label>角色</label>
            <div class="checkbox-group">
              <label v-for="role in availableRoles" :key="role.code" class="checkbox-label">
                <input 
                  type="checkbox" 
                  :value="role.code" 
                  v-model="form.roles"
                />
                {{ role.name }}
              </label>
            </div>
          </div>
        </div>
        
        <div class="dialog-footer">
          <button @click="dialogVisible = false" class="btn-default">取消</button>
          <button @click="handleSubmit" class="btn-primary">确定</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.user-management {
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

.btn-primary {
  padding: 8px 16px;
  background: #1890ff;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.btn-primary:hover {
  background: #40a9ff;
}

.no-permission {
  padding: 40px;
  text-align: center;
  color: #999;
  background: #f5f5f5;
  border-radius: 8px;
}

.table-container {
  overflow-x: auto;
}

table {
  width: 100%;
  border-collapse: collapse;
}

th, td {
  padding: 12px 16px;
  text-align: left;
  border-bottom: 1px solid #f0f0f0;
}

th {
  background: #fafafa;
  color: #333;
  font-weight: 500;
  font-size: 14px;
}

td {
  color: #666;
  font-size: 14px;
}

.role-tag {
  display: inline-block;
  padding: 2px 8px;
  background: #e6f7ff;
  color: #1890ff;
  border-radius: 4px;
  font-size: 12px;
  margin-right: 4px;
}

.btn-link {
  background: none;
  border: none;
  color: #1890ff;
  cursor: pointer;
  font-size: 14px;
  padding: 4px 8px;
  margin-right: 8px;
}

.btn-link:hover {
  text-decoration: underline;
}

.btn-link.danger {
  color: #ff4d4f;
}

.no-data {
  text-align: center;
  color: #999;
  padding: 40px;
}

.dialog-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.dialog {
  background: #fff;
  border-radius: 8px;
  width: 500px;
  max-width: 90vw;
  max-height: 90vh;
  overflow: auto;
}

.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  border-bottom: 1px solid #f0f0f0;
}

.dialog-header h3 {
  margin: 0;
  font-size: 16px;
  color: #333;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  color: #999;
  cursor: pointer;
  padding: 0;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
}

.close-btn:hover {
  background: #f5f5f5;
  color: #333;
}

.dialog-body {
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
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

.form-item input[type="text"],
.form-item input[type="password"] {
  padding: 8px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  font-size: 14px;
}

.checkbox-group {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #666;
  font-size: 14px;
  cursor: pointer;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 24px;
  border-top: 1px solid #f0f0f0;
}

.btn-default {
  padding: 8px 16px;
  background: #fff;
  color: #666;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.btn-default:hover {
  border-color: #1890ff;
  color: #1890ff;
}
</style>
