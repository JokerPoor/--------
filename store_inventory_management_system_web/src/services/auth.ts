import { reactive } from 'vue'
import http from './http'

type Role = {
  id: number
  roleName: string
  roleKey?: string
  status?: number
}

type LoginUser = {
  id: number
  userAccount: string
  userName: string
  phone: string
  email: string
  status: number
  roles?: Role[]
}

type PageItem = {
  id: number | string
  parentId: number | string | null
  name: string
  path: string
  component: string
  icon?: string
  orderNum: number
  visible: number
  meta?: string
}

type PermissionItem = {
  id: number
  name: string
  description?: string
}

const state = reactive({
  user: null as LoginUser | null,
  pages: [] as PageItem[],
  permissionNames: [] as string[],
  routesInjected: false,
  inited: false
})

async function refreshAccess() {
  const [userRes, pagesRes, permsRes] = await Promise.all([
    http.get('/user/getLoginUser'),
    http.post('/page/user'),
    http.post('/permission/user')
  ])
  state.user = userRes.data || null
  state.pages = pagesRes.data || []
  const permissions = (permsRes.data || []) as PermissionItem[]
  state.permissionNames = permissions.map(p => p.name)
}

function normalizeComponentKey(raw: string) {
  const rawNorm = (raw || '').replace(/\\/g, '/').replace(/^\//g, '')
  const c = rawNorm.endsWith('.vue') ? rawNorm.slice(0, -4) : rawNorm
  if (c.startsWith('src/')) return `../${c.slice(4)}.vue`
  if (c.startsWith('pages/')) return `../${c}.vue`
  return `../pages/${c}.vue`
}

async function ensureDynamicRoutes(r: any) {
  if (state.routesInjected) return
  // 使用 eager: true 直接导入所有组件，不使用懒加载
  const modules = import.meta.glob('../pages/**/*.vue', { eager: true })
  console.log('[Auth] Available modules (eager):', Object.keys(modules))
  console.log('[Auth] Pages from backend:', state.pages)
  
  state.pages.forEach(p => {
    if (!p.path || !p.component) {
      console.warn('[Auth] Skipping page (no path or component):', p)
      return
    }
    const key1 = normalizeComponentKey(p.component)
    const normalizedComponent = p.component.replace(/\\/g, '/')
    const key2 = normalizedComponent.endsWith('.vue') ? `../${normalizedComponent}` : `../${normalizedComponent}.vue`
    const key3 = `../pages/${normalizedComponent}.vue`
    const key4 = `../pages/${normalizedComponent}`
    
    const moduleObj = (modules as any)[key1] || (modules as any)[key2] || (modules as any)[key3] || (modules as any)[key4]
    
    if (!moduleObj) {
      console.error('[Auth] Component not found for page:', p.name, 'tried keys:', [key1, key2, key3, key4])
      return
    }
    
    // eager 模式返回的是模块对象，需要取 .default
    const comp = moduleObj.default || moduleObj
    console.log('[Auth] Component for', p.name, ':', typeof comp, comp)
    
    const path = p.path.startsWith('/') ? p.path : `/${p.path}`
    const exists = r.getRoutes().some((rt: any) => rt.path === path)
    if (!exists) {
      console.log('[Auth] Adding route:', path, '→', p.component)
      r.addRoute({ 
        path, 
        component: comp,
        name: p.name
      })
    } else {
      console.log('[Auth] Route already exists:', path)
    }
  })
  state.routesInjected = true
  console.log('[Auth] All routes after injection:', r.getRoutes().map((rt: any) => ({ path: rt.path, name: rt.name })))
}

async function ensureInited(r: any) {
  if (state.inited) return false
  await refreshAccess()
  await ensureDynamicRoutes(r)
  state.inited = true
  return true
}

async function login(payload: { userAccount: string; userPassword: string }) {
  await http.post('/user/login', payload)
}

async function register(payload: {
  userAccount: string
  userPassword: string
  userName: string
  email: string
  phone: string
  roleId: number
}) {
  await http.post('/user/register', payload)
}

async function logout() {
  try {
    await http.post('/user/logout')
  } finally {
    state.user = null
    state.pages = []
    state.permissionNames = []
    state.routesInjected = false
    state.inited = false
  }
}

function hasPermission(name: string) {
  if (!name) return false
  return state.permissionNames.includes(name)
}

function getMenuPages() {
  return state.pages
    .filter(p => p.visible == 1)
    .sort((a, b) => (b.orderNum || 0) - (a.orderNum || 0))
}

function getUser() {
  return state.user
}

export default {
  state,
  login,
  register,
  logout,
  refreshAccess,
  ensureDynamicRoutes,
  ensureInited,
  getMenuPages,
  hasPermission,
  getUser
}
