import type { Directive } from 'vue'
import auth from '../services/auth'
import router from '../router'

const perm: Directive = {
  async mounted(el, binding) {
    const name = String(binding.value || '')
    const ok = auth.hasPermission(name)
    if (!ok) el.style.display = 'none'
  },
  async updated(el, binding) {
    const name = String(binding.value || '')
    const ok = auth.hasPermission(name)
    el.style.display = ok ? '' : 'none'
  }
}

export default perm
