<script setup>
import { ref, onMounted } from 'vue'
import { useAuthStore } from '../stores/auth'

const authStore = useAuthStore()

const menus = ref([])
const loading = ref(false)
const showModal = ref(false)
const editingMenu = ref(null)

const form = ref({
  name: '',
  code: '',
  type: 'MENU',
  parentId: null,
  path: '',
  icon: '',
  sortOrder: 0
})

const menuOptions = ref([])

async function loadMenus() {
  loading.value = true
  try {
    const response = await fetch('/api/system/menus', {
      credentials: 'include'
    })
    if (response.ok) {
      menus.value = await response.json()
      menuOptions.value = [{ id: null, name: '顶级菜单', children: [] }, ...menus.value]
    }
  } catch (error) {
    console.error('加载菜单失败:', error)
  } finally {
    loading.value = false
  }
}

function handleAdd() {
  editingMenu.value = null
  form.value = {
    name: '',
    code: '',
    type: 'MENU',
    parentId: null,
    path: '',
    icon: '',
    sortOrder: 0
  }
  showModal.value = true
}

function handleEdit(menu) {
  editingMenu.value = menu
  form.value = { ...menu }
  showModal.value = true
}

async function handleSubmit() {
  try {
    const url = editingMenu.value 
      ? `/api/system/menus/${editingMenu.value.id}`
      : '/api/system/menus'
    const method = editingMenu.value ? 'PUT' : 'POST'
    
    const response = await fetch(url, {
      method,
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(form.value),
      credentials: 'include'
    })
    
    if (response.ok) {
      showModal.value = false
      await loadMenus()
    } else {
      const error = await response.text()
      throw new Error(error)
    }
  } catch (error) {
    console.error('保存失败:', error)
  }
}

async function handleDelete(menu) {
  if (!confirm(`确定要删除菜单 "${menu.name}" 吗？`)) return
  
  try {
    const response = await fetch(`/api/system/menus/${menu.id}`, {
      method: 'DELETE',
      credentials: 'include'
    })
    
    if (response.ok) {
      await loadMenus()
    } else {
      const error = await response.text()
      throw new Error(error)
    }
  } catch (error) {
    console.error('删除失败:', error)
  }
}

function getTypeLabel(type) {
  const labels = {
    MENU: '菜单',
    BUTTON: '按钮'
  }
  return labels[type] || type
}

function getParentName(parentId) {
  if (!parentId) return '-'
  const parent = menus.value.find(m => m.id === parentId)
  return parent ? parent.name : '-'
}

onMounted(() => {
  loadMenus()
})
</script>

<template>
  <div class="management-page">
    <div class="page-header">
      <h2>菜单管理</h2>
      <button class="btn-primary" @click="handleAdd">新增菜单</button>
    </div>
    
    <div class="table-container">
      <table v-if="menus.length > 0" class="data-table">
        <thead>
          <tr>
            <th>名称</th>
            <th>编码</th>
            <th>类型</th>
            <th>父级菜单</th>
            <th>路径</th>
            <th>图标</th>
            <th>排序</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="menu in menus" :key="menu.id">
            <td>{{ menu.name }}</td>
            <td><code>{{ menu.code }}</code></td>
            <td>
              <span class="type-tag" :class="menu.type.toLowerCase()">
                {{ getTypeLabel(menu.type) }}
              </span>
            </td>
            <td>{{ getParentName(menu.parentId) }}</td>
            <td><code>{{ menu.path || '-' }}</code></td>
            <td>{{ menu.icon || '-' }}</td>
            <td>{{ menu.sortOrder }}</td>
            <td class="actions">
              <button class="btn-text" @click="handleEdit(menu)">编辑</button>
              <button class="btn-text btn-danger" @click="handleDelete(menu)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
      
      <div v-else class="empty-state">
        <p>暂无菜单数据</p>
      </div>
    </div>
    
    <!-- 新增/编辑弹窗 -->
    <div v-if="showModal" class="modal-overlay" @click.self="showModal = false">
      <div class="modal">
        <div class="modal-header">
          <h3>{{ editingMenu ? '编辑菜单' : '新增菜单' }}</h3>
          <button class="close-btn" @click="showModal = false">&times;</button>
        </div>
        
        <div class="modal-body">
          <div class="form-item">
            <label>菜单名称 *</label>
            <input v-model="form.name" type="text" placeholder="请输入菜单名称" />
          </div>
          
          <div class="form-item">
            <label>菜单编码 *</label>
            <input v-model="form.code" type="text" placeholder="请输入菜单编码（如：system:user）" />
          </div>
          
          <div class="form-item">
            <label>菜单类型 *</label>
            <select v-model="form.type">
              <option value="MENU">菜单</option>
              <option value="BUTTON">按钮</option>
            </select>
          </div>
          
          <div class="form-item">
            <label>父级菜单</label>
            <select v-model="form.parentId">
              <option :value="null">顶级菜单</option>
              <option v-for="menu in menuOptions" :key="menu.id" :value="menu.id" :disabled="menu.children && menu.children.length > 0">
                {{ menu.name }}
              </option>
            </select>
          </div>
          
          <div class="form-item">
            <label>菜单路径</label>
            <input v-model="form.path" type="text" placeholder="如：/system/user" />
          </div>
          
          <div class="form-item">
            <label>图标</label>
            <input v-model="form.icon" type="text" placeholder="如：user" />
          </div>
          
          <div class="form-item">
            <label>排序</label>
            <input v-model="form.sortOrder" type="number" placeholder="数字越小越靠前" />
          </div>
        </div>
        
        <div class="modal-footer">
          <button class="btn-secondary" @click="showModal = false">取消</button>
          <button class="btn-primary" @click="handleSubmit" :disabled="!form.name || !form.code">
            {{ editingMenu ? '保存' : '创建' }}
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

.type-tag.menu {
  background: #e6f7ff;
  color: #1890ff;
}

.type-tag.button {
  background: #f6ffed;
  color: #52c41a;
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
.form-item select {
  padding: 8px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  font-size: 14px;
}

.form-item input:focus,
.form-item select:focus {
  outline: none;
  border-color: #40a9ff;
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
