<script setup>
import { ref, onMounted } from 'vue'
import { useAuthStore } from '../stores/auth'

const authStore = useAuthStore()

const permissions = ref([])
const loading = ref(false)
const showModal = ref(false)
const editingPermission = ref(null)

const form = ref({
  name: '',
  code: '',
  type: 'BUTTON',
  resourceId: null,
  description: ''
})

const resourceOptions = ref([])

async function loadPermissions() {
  loading.value = true
  try {
    const response = await fetch('/api/system/permissions', {
      credentials: 'include'
    })
    if (response.ok) {
      permissions.value = await response.json()
      // 从现有权限中提取资源选项
      const resources = new Map()
      permissions.value.forEach(perm => {
        if (perm.resourceName) {
          resources.set(perm.resourceId, perm.resourceName)
        }
      })
      resourceOptions.value = [
        { id: null, name: '不关联资源' },
        ...Array.from(resources.entries()).map(([id, name]) => ({ id, name }))
      ]
    }
  } catch (error) {
    console.error('加载权限失败:', error)
  } finally {
    loading.value = false
  }
}

function handleAdd() {
  editingPermission.value = null
  form.value = {
    name: '',
    code: '',
    type: 'BUTTON',
    resourceId: null,
    description: ''
  }
  showModal.value = true
}

function handleEdit(permission) {
  editingPermission.value = permission
  form.value = { ...permission }
  showModal.value = true
}

async function handleSubmit() {
  try {
    const url = editingPermission.value 
      ? `/api/system/permissions/${editingPermission.value.id}`
      : '/api/system/permissions'
    const method = editingPermission.value ? 'PUT' : 'POST'
    
    const response = await fetch(url, {
      method,
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(form.value),
      credentials: 'include'
    })
    
    if (response.ok) {
      showModal.value = false
      await loadPermissions()
    } else {
      const error = await response.text()
      throw new Error(error)
    }
  } catch (error) {
    console.error('保存失败:', error)
  }
}

async function handleDelete(permission) {
  if (!confirm(`确定要删除权限 "${permission.name}" 吗？`)) return
  
  try {
    const response = await fetch(`/api/system/permissions/${permission.id}`, {
      method: 'DELETE',
      credentials: 'include'
    })
    
    if (response.ok) {
      await loadPermissions()
    } else {
      const error = await response.text()
      throw new Error(error)
    }
  } catch (error) {
    console.error('删除失败:', error)
  }
}

onMounted(() => {
  loadPermissions()
})
</script>

<template>
  <div class="management-page">
    <div class="page-header">
      <h2>权限管理</h2>
      <button class="btn-primary" @click="handleAdd">新增权限</button>
    </div>
    
    <div class="table-container">
      <table v-if="permissions.length > 0" class="data-table">
        <thead>
          <tr>
            <th>权限名称</th>
            <th>权限编码</th>
            <th>类型</th>
            <th>关联资源</th>
            <th>描述</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="permission in permissions" :key="permission.id">
            <td>{{ permission.name }}</td>
            <td><code>{{ permission.code }}</code></td>
            <td>
              <span class="type-tag" :class="permission.type.toLowerCase()">
                {{ permission.type }}
              </span>
            </td>
            <td>{{ permission.resourceName || '-' }}</td>
            <td>{{ permission.description || '-' }}</td>
            <td class="actions">
              <button class="btn-text" @click="handleEdit(permission)">编辑</button>
              <button class="btn-text btn-danger" @click="handleDelete(permission)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
      
      <div v-else class="empty-state">
        <p>暂无权限数据</p>
      </div>
    </div>
    
    <!-- 新增/编辑弹窗 -->
    <div v-if="showModal" class="modal-overlay" @click.self="showModal = false">
      <div class="modal">
        <div class="modal-header">
          <h3>{{ editingPermission ? '编辑权限' : '新增权限' }}</h3>
          <button class="close-btn" @click="showModal = false">&times;</button>
        </div>
        
        <div class="modal-body">
          <div class="form-item">
            <label>权限名称 *</label>
            <input v-model="form.name" type="text" placeholder="请输入权限名称" />
          </div>
          
          <div class="form-item">
            <label>权限编码 *</label>
            <input v-model="form.code" type="text" placeholder="请输入权限编码（如：system:user:create）" />
          </div>
          
          <div class="form-item">
            <label>权限类型 *</label>
            <select v-model="form.type">
              <option value="BUTTON">按钮</option>
              <option value="API">API</option>
              <option value="DATA">数据</option>
            </select>
          </div>
          
          <div class="form-item">
            <label>关联资源</label>
            <select v-model="form.resourceId">
              <option :value="null">不关联资源</option>
              <option v-for="resource in resourceOptions" :key="resource.id" :value="resource.id">
                {{ resource.name }}
              </option>
            </select>
            <small class="help-text">可选，关联菜单或其他资源</small>
          </div>
          
          <div class="form-item">
            <label>描述</label>
            <textarea 
              v-model="form.description" 
              placeholder="请输入权限描述"
              rows="3"
            ></textarea>
          </div>
        </div>
        
        <div class="modal-footer">
          <button class="btn-secondary" @click="showModal = false">取消</button>
          <button class="btn-primary" @click="handleSubmit" :disabled="!form.name || !form.code">
            {{ editingPermission ? '保存' : '创建' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.management-page {
  padding: 24px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-header h2 {
  font-size: 20px;
  color: #333;
}

.table-container {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th,
.data-table td {
  padding: 12px 16px;
  text-align: left;
  border-bottom: 1px solid #e8e8e8;
}

.data-table th {
  background: #fafafa;
  font-weight: 500;
  color: #333;
  font-size: 14px;
}

.data-table td {
  font-size: 14px;
  color: #666;
}

.data-table tr:hover {
  background: #f5f5f5;
}

.type-tag {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.type-tag.button {
  background: #f6ffed;
  color: #52c41a;
}

.type-tag.api {
  background: #fff7e6;
  color: #fa8c16;
}

.type-tag.data {
  background: #f9f0ff;
  color: #722ed1;
}

.empty-state {
  padding: 60px 20px;
  text-align: center;
  color: #999;
}

.modal-overlay {
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

.modal {
  background: #fff;
  border-radius: 8px;
  width: 500px;
  max-width: 90%;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  border-bottom: 1px solid #e8e8e8;
}

.modal-header h3 {
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
}

.close-btn:hover {
  color: #333;
}

.modal-body {
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

.form-item input:focus,
.form-item select:focus,
.form-item textarea:focus {
  outline: none;
  border-color: #40a9ff;
}

.help-text {
  font-size: 12px;
  color: #999;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  padding: 16px 24px;
  border-top: 1px solid #e8e8e8;
}

.btn-primary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>
