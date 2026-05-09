<script setup>
import { useAuthStore } from '../stores/auth'

const authStore = useAuthStore()

const allPermissions = [
  { code: 'system:user:view', name: '查看用户', type: '菜单权限' },
  { code: 'system:user:create', name: '创建用户', type: '按钮权限' },
  { code: 'system:user:edit', name: '编辑用户', type: '按钮权限' },
  { code: 'system:user:delete', name: '删除用户', type: '按钮权限' },
  { code: 'system:role:view', name: '查看角色', type: '菜单权限' },
  { code: 'system:role:edit', name: '编辑角色', type: '按钮权限' },
  { code: 'system:menu:view', name: '查看菜单', type: '菜单权限' },
  { code: 'system:permission:view', name: '查看权限', type: '菜单权限' }
]
</script>

<template>
  <div class="permission-management">
    <div class="page-header">
      <h2>权限配置</h2>
    </div>
    
    <div class="permission-sections">
      <div class="permission-section">
        <h3>系统权限列表</h3>
        <table>
          <thead>
            <tr>
              <th>权限标识</th>
              <th>权限名称</th>
              <th>类型</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="perm in allPermissions" :key="perm.code">
              <td class="perm-code">{{ perm.code }}</td>
              <td>{{ perm.name }}</td>
              <td>
                <span :class="['perm-type', perm.type === '按钮权限' ? 'button' : 'menu']">
                  {{ perm.type }}
                </span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      
      <div class="permission-section">
        <h3>权限使用说明</h3>
        <div class="usage-cards">
          <div class="usage-card">
            <h4>菜单权限</h4>
            <p>控制用户能否看到某个菜单项，无权限时菜单不会显示</p>
          </div>
          <div class="usage-card">
            <h4>按钮权限</h4>
            <p>控制页面内按钮的显示，无权限时按钮自动隐藏</p>
          </div>
          <div class="usage-card">
            <h4>API 权限</h4>
            <p>后端接口级别权限控制，无权限时返回 403 错误</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.permission-management {
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

.permission-sections {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.permission-section {
  background: #fafafa;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  padding: 20px;
}

.permission-section h3 {
  color: #333;
  font-size: 16px;
  margin: 0 0 16px 0;
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

.perm-code {
  font-family: 'Courier New', monospace;
  color: #1890ff;
}

.perm-type {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.perm-type.menu {
  background: #e6f7ff;
  color: #1890ff;
}

.perm-type.button {
  background: #f6ffed;
  color: #52c41a;
}

.usage-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 16px;
}

.usage-card {
  background: #fff;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  padding: 16px;
}

.usage-card h4 {
  color: #333;
  font-size: 14px;
  margin: 0 0 8px 0;
}

.usage-card p {
  color: #666;
  font-size: 13px;
  margin: 0;
  line-height: 1.6;
}
</style>
