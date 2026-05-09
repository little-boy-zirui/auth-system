import { computed } from 'vue'
import { useAuthStore } from './auth'

export function createPermissionDirective() {
  return {
    mounted(el, binding) {
      const { hasPermission } = useAuthStore()
      const { value } = binding
      
      if (!checkPermission(value, hasPermission)) {
        if (el.parentNode) {
          el.parentNode.removeChild(el)
        }
      }
    }
  }
}

function checkPermission(value, hasPermission) {
  if (typeof value === 'string') {
    return hasPermission(value)
  }
  
  if (Array.isArray(value)) {
    const all = value.every(v => hasPermission(v))
    const any = value.some(v => hasPermission(v))
    return all || any
  }
  
  return false
}
