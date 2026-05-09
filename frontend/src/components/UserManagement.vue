<script setup>
import { ref, onMounted, computed } from 'vue'
import { useAuthStore } from '../stores/auth'

const authStore = useAuthStore()

const users = ref([])
const roles = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const editingUser = ref(null)
const roleDialogVisible = ref(false)
const assigningUser = ref(null)
const selectedRoles = ref([])

const form = ref({
  username: '',
  password: '',
  displayName: '',
  email: '',
  phone: '',
  status: '1'
})

const columns = [
  { key: 'id', label: 'ID' },
  { key: 'username', label: '用户名' },
  { key: 'displayName', label: '显示名称' },
  { key: 'email', label: '邮箱' },
  { key: 'phone', label: '手机' },
  { key: 'roles', label: '角色' },
  { key: 'status', label: '状态' },
  { key: 'actions', label: '操作' }
]

async function loadUsers() {
  if (!authStore.hasPermission('system:user:view')) {
    return
  }
  
  loading.value = true
  try {
    const response = await fetch('/api/system/users', {
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

async function loadRoles() {
  try {
    const response = await fetch('/api/system/roles', {
      credentials: 'include'
    })
    if (response.ok) {
      roles.value = await response.json()
    }
  } catch (error) {
    console.error('加载角色失败:', error)
  }
}

function handleAdd() {
  editingUser.value = null
  form.value = {
    username: '',
    password: '',
    displayName: '',
    email: '',
    phone: '',
    status: '1'
  }
  dialogVisible.value = true
}

function handleEdit(user) {
  editingUser.value = user
  form.value = {
    username: user.username,
    password: '',
    displayName: user.displayName || '',
    email: user.email || '',
    phone: user.phone || '',
    status: user.status || '1'
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
    const response = await fetch(`/api/system/users/${user.id}`, {
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
    let response
    if (editingUser.value) {
      response = await fetch(`/api/system/users/${editingUser.value.id}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify({
          displayName: form.value.displayName,
          email: form.value.email,
          phone: form.value.phone,
          status: form.value.status
        })
      })
    } else {
      response = await fetch('/api/system/users', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify(form.value)
      })
    }
    
    if (response.ok) {
      dialogVisible.value = false
      await loadUsers()
    }
  } catch (error) {
    console.error('保存用户失败:', error)
  }
}

function handleAssignRoles(user) {
  assigningUser.value = user
  selectedRoles.value = user.roles || []
  roleDialogVisible.value = true
}

async function handleAssignRolesSubmit() {
  try {
    const response = await fetch(`/api/system/users/${assigningUser.value.id}/roles`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include',
      body: JSON.stringify(selectedRoles.value)
    })
    
    if (response.ok) {
      roleDialogVisible.value = false
      await loadUsers()
    }
  } catch (error) {
    console.error('分配角色失败:', error)
  }
}

onMounted(() => {
  loadUsers()
  loadRoles()
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
          <tr v-for="user in users" :key="user.id">
            <td>{{ user.id }}</td>
            <td>{{ user.username }}</td>
            <td>{{ user.displayName || '-' }}</td>
            <td>{{ user.email || '-' }}</td>
            <td>{{ user.phone || '-' }}</td>
            <td>
              <span v-for="role in user.roles" :key="role" class="role-tag">
                {{ role }}
              </span>
            </td>
            <td>
              <span :class="['status-tag', user.status === '1' ? 'success' : 'danger']">
                {{ user.status === '1' ? '正常' : '停用' }}
              </span>
            </td>
            <td>
              <button v-permission="'system:user:edit'" @click="handleEdit(user)" class="btn-link">编辑</button>
              <button v-permission="'system:user:delete'" @click="handleDelete(user)" class="btn-link danger">删除</button>
              <button v-permission="'system:role:assign'" @click="handleAssignRoles(user)" class="btn-link primary">分配角色</button>
            </td>
          </tr>
        </tbody>
      </table>
      
      <p v-else class="no-data">暂无用户数据</p>
    </div>
    
    <!-- 用户编辑对话框 -->
    <div v-if="dialogVisible" class="dialog-overlay" @click.self="dialogVisible = false">
      <div class="dialog">
        <div class="dialog-header">
          <h3>{{ editingUser ? '编辑用户' : '新增用户' }}</h3>
          <button @click="dialogVisible = false" class="close-btn">×</button>
        </div>
        
        <div class="dialog-body">
          <div v-if="!editingUser" class="form-item">
            <label>用户名 <span class="required">*</span></label>
            <input v-model="form.username" type="text" placeholder="请输入用户名" />
          </div>
          
          <div v-if="!editingUser" class="form-item">
            <label>密码 <span class="required">*</span></label>
            <input v-model="form.password" type="password" placeholder="请输入密码" />
          </div>
          
          <div class="form-item">
            <label>显示名称</label>
            <input v-model="form.displayName" type="text" placeholder="请输入显示名称" />
          </div>
          
          <div class="form-item">
            <label>邮箱</label>
            <input v-model="form.email" type="email" placeholder="请输入邮箱" />
          </div>
          
          <div class="form-item">
            <label>手机</label>
            <input v-model="form.phone" type="text" placeholder="请输入手机号" />
          </div>
          
          <div class="form-item">
            <label>状态</label>
            <select v-model="form.status" class="select">
              <option value="1">正常</option>
              <option value="0">停用</option>
            </select>
          </div>
        </div>
        
        <div class="dialog-footer">
          <button @click="dialogVisible = false" class="btn-default">取消</button>
          <button @click="handleSubmit" class="btn-primary">确定</button>
        </div>
      </div>
    </div>
    
    <!-- 角色分配对话框 -->
    <div v-if="roleDialogVisible" class="dialog-overlay" @click.self="roleDialogVisible = false">
      <div class="dialog">
        <div class="dialog-header">
          <h3>分配角色 - {{ assigningUser?.username }}</h3>
          <button @click="roleDialogVisible = false" class="close-btn">×</button>
        </div>
        
        <div class="dialog-body">
          <div class="checkbox-group">
            <label v-for="role in roles" :key="role.id" class="checkbox-label">
              <input 
                type="checkbox" 
                :value="role.code" 
                v-model="selectedRoles"
              />
              <span class="checkbox-text">
                <strong>{{ role.name }}</strong>
                <small>{{ role.description || role.code }}</small>
              </span>
            </label>
          </div>
        </div>
        
        <div class="dialog-footer">
          <button @click="roleDialogVisible = false" class="btn-default">取消</button>
          <button @click="handleAssignRolesSubmit" class="btn-primary">确定</button>
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

.status-tag {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.status-tag.success {
  background: #f6ffed;
  color: #52c41a;
}

.status-tag.danger {
  background: #fff1f0;
  color: #ff4d4f;
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

.btn-link.primary {
  color: #52c41a;
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

.form-item input,
.form-item select {
  padding: 8px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  font-size: 14px;
}

.form-item .select {
  cursor: pointer;
}

.required {
  color: #ff4d4f;
}

.checkbox-group {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.checkbox-label {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  cursor: pointer;
  padding: 12px;
  border: 1px solid #e8e8e8;
  border-radius: 4px;
  transition: all 0.3s;
}

.checkbox-label:hover {
  border-color: #1890ff;
  background: #f0f5ff;
}

.checkbox-label input[type="checkbox"] {
  margin-top: 2px;
}

.checkbox-text {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.checkbox-text strong {
  color: #333;
  font-size: 14px;
}

.checkbox-text small {
  color: #999;
  font-size: 12px;
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
