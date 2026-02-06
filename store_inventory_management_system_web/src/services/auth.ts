import http from './http'

type LoginUser = {
  id: number
  userAccount: string
  userName: string
  phone: string
  email: string
  status: number
}

type PageItem = {
  id: number
  parentId: number | null
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

const state = {
  user: null as LoginUser | null,
  pages: [] as PageItem[],
  permissionNames: [] as string[],
  routesInjected: false,
  inited: false
}

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
  const modules = import.meta.glob('../pages/**/*.vue')
  state.pages.forEach(p => {
    if (!p.path || !p.component) return
    const key1 = normalizeComponentKey(p.component)
    const normalizedComponent = p.component.replace(/\\/g, '/')
    const key2 = normalizedComponent.endsWith('.vue') ? `../${normalizedComponent}` : `../${normalizedComponent}.vue`
    const comp = (modules as any)[key1] || (modules as any)[key2]
    if (!comp) return
    const path = p.path.startsWith('/') ? p.path : `/${p.path}`
    const exists = r.getRoutes().some((rt: any) => rt.path === path)
    if (!exists) r.addRoute({ path, component: comp })
  })
  state.routesInjected = true
}

async function ensureInited(r: any) {
  if (state.inited) return
  await refreshAccess()
  await ensureDynamicRoutes(r)
  state.inited = true
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
    .filter(p => p.visible === 1)
    .sort((a, b) => (b.orderNum || 0) - (a.orderNum || 0))
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
  hasPermission
}
