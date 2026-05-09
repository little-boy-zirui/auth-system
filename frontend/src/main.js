import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import { createPermissionDirective } from './directives/permission'
import './style.css'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.directive('permission', createPermissionDirective())

app.mount('#app')
