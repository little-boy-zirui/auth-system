import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import { createPermissionDirective } from './directives/permission'
import UserManagement from './components/UserManagement.vue'
import RoleManagement from './components/RoleManagement.vue'
import MenuManagement from './components/MenuManagement.vue'
import PermissionManagement from './components/PermissionManagement.vue'
import './style.css'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.directive('permission', createPermissionDirective())
app.component('UserManagement', UserManagement)
app.component('RoleManagement', RoleManagement)
app.component('MenuManagement', MenuManagement)
app.component('PermissionManagement', PermissionManagement)

app.mount('#app')
