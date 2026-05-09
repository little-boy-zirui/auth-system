<script setup>
import { ref, onMounted } from 'vue'
import { useAuthStore } from '../stores/auth'

const authStore = useAuthStore()

const roles = ref([])
const permissions = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const editingRole = ref(null)
const permDialogVisible = false
const assigningRole = ref(null)
const selectedPermissions = ref([])

const form = ref({
  code: '',
  name: '',
  description: '',
  status: '1'
})

const columns = [
  { key: 'id', label: 'ID' },
  { key: 'code', label: '角色编码' },
  { key: 'name', label: '角色名称' },
  { key: 'description', label: '描述' },
  { key: 'permissions', label: '权限' },
  { key: 'status', label: '状态' },
  { key: 'actions', label: '操作' }
]

async function loadRoles() {
  if (!authStore.hasPermission('system:role:view')) {
    return
  }
  
  loading.value = true
  try {
    const response = await fetch('/api/system/roles', {
      credentials: 'include'
    })
    if (response.ok) {
      roles.value = await response.json()
    }
  } catch (error) {
    console.error('加载角色失败:', error)
  } finally {
    loading.value = false
  }
}

async function loadPermissions() {
  try {
    const response = await fetch('/api/system/permissions', {
      credentials: 'include'
    })
    if (response.ok) {
      permissions.value = await response.json()
    }
  } catch (error) {
    console.error('加载权限失败:', error)
  }
}

function handleAdd() {
  editingRole.value = null
  form.value = {
    code: '',
    name: '',
    description: '',
    status: '1'
  }
  dialogVisible.value = true
}

function handleEdit(role) {
  editingRole.value = role
  form.value = {
    code: role.code,
    name: role.name,
    description: role.description || '',
    status: role.status || '1'
  }
  dialogVisible.value = true
}

async function handleDelete(role) {
  if (!confirm(`确定要删除角色 "${role.name}" 吗？`)) {
    return
  }
  
  if (!authStore.hasPermission('system:role:edit')) {
    alert('没有删除权限')
    return
  }
  
  try {
    const response = await fetch(`/api/system/roles/${role.id}`, {
      method: 'DELETE',
      credentials: 'include'
    })
    if (response.ok) {
      await loadRoles()
    }
  } catch (error) {
    console.error('删除角色失败:', error)
  }
}

async function handleSubmit() {
  try {
    let response
    if (editingRole.value) {
      response = await fetch(`/api/system/roles/${editingRole.value.id}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify({
          name: form.value.name,
          description: form.value.description,
          status: form.value.status
        })
      })
    } else {
      response = await fetch('/api/system/roles', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify(form.value)
      })
    }
    
    if (response.ok) {
      dialogVisible.value = false
      await loadRoles()
    }
  } catch (error) {
    console.error('保存角色失败:', error)
  }
}

function handleAssignPermissions(role) {
  assigningRole.value = role
  selectedPermissions.value = role.permissions || []
  permDialogVisible.value = true
}

async function handleAssignPermissionsSubmit() {
  try {
    const response = await fetch(`/api/system/roles/${assigningRole.value.id}/permissions`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include',
      body: JSON.stringify(selectedPermissions.value)
    })
    
    if (response.ok) {
      permDialogVisible.value = false
      await loadRoles()
    }
  } catch (error) {
    console.error('分配权限失败:', error)
  }
}

onMounted(() => {
  loadRoles()
  loadPermissions()
})
</script>

<template>
  <div class="role-management">
    <div class="page-header">
      <h2>角色管理</h2>
      <button v-permission="'system:role:edit'" @click="handleAdd" class="btn-primary">
        + 新增角色
      </button>
    </div>
    
    <div v-if="!authStore.hasPermission('system:role:view')" class="no-permission">
      <p>你没有访问角色管理的权限</p>
    </div>
    
    <div v-else class="table-container">
      <table v-if="roles.length > 0">
        <thead>
          <tr>
            <th v-for="col in columns" :key="col.key">{{ col.label }}</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="role in roles" :key="role.id">
            <td>{{ role.id }}</td>
            <td><span class="code-tag">{{ role.code }}</span></td>
            <td>{{ role.name }}</td>
            <td>{{ role.description || '-' }}</td>
            <td>
              <div class="perm-preview">
                <span v-for="perm in role.permissions?.slice(0, 3)" :key="perm" class="perm-tag">
                  {{ perm }}
                </span>
                <span v-if="role.permissions?.length > 3" class="perm-more">+{{ role.permissions.length - 3 }}</span>
              </div>
            </td>
            <td>
              <span :class="['status-tag', role.status === '1' ? 'success' : 'danger']">
                {{ role.status === '1' ? '正常' : '停用' }}
              </span>
            </td>
            <td>
              <button v-permission="'system:role:edit'" @click="handleEdit(role)" class="btn-link">编辑</button>
              <button v-permission="'system:role:assign'" @click="handleAssignPermissions(role)" class="btn-link primary">分配权限</button>
              <button v-permission="'system:role:edit'" @click="handleDelete(role)" class="btn-link danger">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
      
      <p v-else class="no-data">暂无角色数据</p>
    </div>
    
    <!-- 角色编辑对话框 -->
    <div v-if="dialogVisible" class="dialog-overlay" @click.self="dialogVisible = false">
      <div class="dialog">
        <div class="dialog-header">
          <h3>{{ editingRole ? '编辑角色' : '新增角色' }}</h3>
          <button @click="dialogVisible = false" class="close-btn">×</button>
        </div>
        
        <div class="dialog-body">
          <div class="form-item">
            <label>角色编码 <span class="required">*</span></label>
            <input 
              v-model="form.code" 
              type="text" 
              placeholder="请输入角色编码"
              :disabled="!!editingRole"
              :class="{ disabled: !!editingRole }"
            />
          </div>
          
          <div class="form-item">
            <label>角色名称 <span class="required">*</span></label>
            <input v-model="form.name" type="text" placeholder="请输入角色名称" />
          </div>
          
          <div class="form-item">
            <label>描述</label>
            <textarea v-model="form.description" rows="3" placeholder="请输入角色描述" />
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
    
    <!-- 权限分配对话框 -->
    <div v-if="permDialogVisible" class="dialog-overlay" @click.self="permDialogVisible = false">
      <div class="dialog dialog-large">
        <div class="dialog-header">
          <h3>分配权限 - {{ assigningRole?.name }}</h3>
          <button @click="permDialogVisible = false" class="close-btn">×</button>
        </div>
        
        <div class="dialog-body">
          <div class="perm-categories">
            <div class="perm-category">
              <h4>菜单权限</h4>
              <div class="checkbox-group">
                <label v-for="perm in permissions.filter(p => p.type === 'menu')" :key="perm.id" class="checkbox-label">
                  <input 
                    type="checkbox" 
                    :value="perm.code" 
                    v-model="selectedPermissions"
                  />
                  <span class="checkbox-text">
                    <strong>{{ perm.name }}</strong>
                    <small>{{ perm.code }}</small>
                  </span>
                </label>
              </div>
            </div>
            
            <div class="perm-category">
              <h4>按钮权限</h4>
              <div class="checkbox-group">
                <label v-for="perm in permissions.filter(p => p.type === 'button')" :key="perm.id" class="checkbox-label">
                  <input 
                    type="checkbox" 
                    :value="perm.code" 
                    v-model="selectedPermissions"
                  />
                  <span class="checkbox-text">
                    <strong>{{ perm.name }}</strong>
                    <small>{{ perm.code }}</small>
                  </span>
                </label>
              </div>
            </div>
          </div>
        </div>
        
        <div class="dialog-footer">
          <button @click="permDialogVisible = false" class="btn-default">取消</button>
          <button @click="handleAssignPermissionsSubmit" class="btn-primary">确定</button>
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

.code-tag {
  padding: 2px 8px;
  background: #f5f5f5;
  color: #666;
  border-radius: 4px;
  font-family: 'Courier New', monospace;
  font-size: 12px;
}

.perm-preview {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  align-items: center;
}

.perm-tag {
  padding: 2px 6px;
  background: #e6f7ff;
  color: #1890ff;
  border-radius: 4px;
  font-size: 11px;
  font-family: 'Courier New', monospace;
}

.perm-more {
  color: #999;
  font-size: 11px;
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

.dialog.dialog-large {
  width: 700px;
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
.form-item select,
.form-item textarea {
  padding: 8px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  font-size: 14px;
  font-family: inherit;
}

.form-item .select {
  cursor: pointer;
}

.form-item input.disabled {
  background: #f5f5f5;
  cursor: not-allowed;
}

.required {
  color: #ff4d4f;
}

.perm-categories {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.perm-category h4 {
  color: #333;
  font-size: 14px;
  margin: 0 0 12px 0;
  padding-bottom: 8px;
  border-bottom: 1px solid #f0f0f0;
}

.checkbox-group {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
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
