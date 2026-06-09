import { useAuthStore } from '@/stores/auth'

export default {
  mounted(el, binding) {
    const auth = useAuthStore()
    const code = binding.value
    if (code && !auth.permissions.includes(code)) {
      el.parentNode?.removeChild(el)
    }
  }
}
