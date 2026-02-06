<template>
  <el-config-provider :size="globalSize">
    <el-container v-if="!isAuth" class="min-h-screen">
      <el-header height="64px" class="flex items-center justify-between px-6">
        <div class="flex items-center gap-4 min-w-0">
          <el-button text @click="toggleCollapse" class="shrink-0">
            <el-icon>
              <component :is="isCollapsed ? Expand : Fold" />
            </el-icon>
          </el-button>
          <div class="text-xl font-semibold flex items-center gap-2 truncate">
            <svg t="1763476865240" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="17401" width="30" height="30" style="color: var(--el-color-primary)"><path d="M960.507 367.104c-0.046-0.512-0.174-0.957-0.22-1.331-0.446-1.89-0.973-3.733-1.69-5.51l-76.913-190.98c-13.322-39.614-51.517-63.903-96.276-63.995H245.949c-45.342 0-80.537 24.069-93.28 62.126L70.046 362.28c-0.4 1.157-0.799 2.842-1.173 4.577-6.016 18.386-9.016 37.279-9.016 56.218 0.066 66.657 36.68 127.452 95.544 158.807 0 0 0 0.046 0.02 0.046 0.042 0 0.042 0 0.042 0.046 0 0 0.02 0 0.02 0.041 24.248 12.923 52.736 19.41 84.844 19.41 53.826-0.133 103.628-23.716 137.62-64.123 33.777 40.054 83.22 63.498 136.96 63.857 53.29-0.44 102.584-23.936 136.243-63.99 33.977 40.366 83.892 63.903 137.933 63.903 32.686-0.18 61.768-7.02 86.288-20.43 57.953-31.62 93.926-92.256 93.88-158.228 0.092-18.964-3.016-38.016-8.745-55.311zM843.935 531.482c-14.833 8.125-33.393 12.298-54.933 12.39-39.926 0-76.293-20.204-97.566-54.554-1.51-3.266-4.086-8.617-8.75-13.788-5.458-6.062-15.274-13.23-31.442-13.23-13.103 0-24.914 5.33-31.576 13.502-4.439 5.017-6.84 9.948-8.57 13.522-21.008 33.904-57.068 54.333-96.19 54.645-39.634-0.225-75.827-20.603-96.947-54.758-1.464-3.02-3.932-8.105-7.772-12.39-15.411-18.36-49.915-17.409-63.35-1.419-4.951 5.417-7.552 10.86-9.216 14.52-21.16 33.926-57.6 54.314-97.367 54.4-21.161 0-39.327-3.993-53.914-11.77h-0.112c-37.504-19.917-69.187-67.057-69.212-109.466 0-12.523 2.063-25.175 6.287-37.478 0.312-0.886 0.553-1.864 0.799-2.868l81.382-200.35c1.664-4.926 6.707-19.85 32-19.85h556.396c10.434 0.599 28.334 2.442 34.682 21.135l76.207 197.775a30.75 30.75 0 0 0 0.972 3.753c4.265 12.345 6.303 24.894 6.303 37.325 0.097 41.897-31.206 88.812-68.111 108.954z m28.774 96.716c-18.12 0-28.6 14.654-28.6 32.733l0.092 182.743c0 10.168-8.305 18.432-18.473 18.467l-633.646 0.4c-10.168 0-18.427-8.213-18.427-18.294l-0.246-179.722c-0.02-18.028-10.526-32.64-28.6-32.64h-0.067c-18.073 0.046-28.554 14.7-28.528 32.814l0.22 175.416c0 46.142 37.683 83.712 83.999 83.712l617.036-0.4c46.275-0.091 83.938-37.744 83.938-83.931l-0.092-178.57c-0.006-18.11-10.486-32.728-28.606-32.728z m-101.606-237.49H246.482c-18.12 0-32.799-10.502-32.799-28.555 0-18.028 14.7-28.554 32.799-28.554h524.62c18.115 0 32.769 10.526 32.769 28.554 0.005 18.048-14.649 28.554-32.768 28.554z" p-id="17402" fill="currentColor"></path></svg>
            门店库存管理系统
          </div>
        </div>
        <div class="flex items-center gap-3">
          <ThemeSwitcher @open-customizer="panelVisible = true" />
          <el-dropdown trigger="click">
            <el-button text class="min-w-0">
              <span class="truncate max-w-[180px]">{{ displayName }}</span>
              <el-icon class="ml-1"><ArrowDown /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-container>
        <el-aside :width="isCollapsed ? '64px' : '220px'" class="p-3 border-r border-[var(--el-border-color)] h-[calc(100vh-64px)] overflow-auto">
          <el-menu :default-active="route.path" router :collapse="isCollapsed" :collapse-transition="false">
            <template v-for="r in menuTree" :key="r.id">
              <el-sub-menu v-if="r.children.length" :index="'group-'+r.id">
                <template #title>{{ r.name }}</template>
                <el-menu-item v-for="c in r.children" :key="c.id" v-show="!!c.pathNorm" :index="c.pathNorm">{{ c.name }}</el-menu-item>
              </el-sub-menu>
              <el-menu-item v-else-if="!!r.pathNorm" :index="r.pathNorm">{{ r.name }}</el-menu-item>
            </template>
          </el-menu>
        </el-aside>
        <el-main class="p-6">
          <router-view v-slot="{ Component }">
            <transition name="route-fade" mode="out-in">
              <component :is="Component" />
            </transition>
          </router-view>
        </el-main>
      </el-container>
      <ThemeCustomizer v-model="panelVisible" />
    </el-container>
    <div v-else>
      <router-view />
    </div>
  </el-config-provider>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { gsap } from 'gsap'
import ThemeSwitcher from './components/ThemeSwitcher.vue'
import ThemeCustomizer from './components/ThemeCustomizer.vue'
import { ElMessageBox } from 'element-plus'
import { Fold, Expand, ArrowDown } from '@element-plus/icons-vue'
import auth from './services/auth'

const route = useRoute()
const router = useRouter()
onMounted(() => { gsap.from('.el-header', { duration: 0.6, opacity: 0, y: -20 }) })
const globalSize = ref<'small' | 'default' | 'large'>('default')
function syncSize() {
  const s = (localStorage.getItem('app-size') as 'small' | 'default' | 'large' | null)
  globalSize.value = s ?? 'default'
}
onMounted(() => { syncSize(); window.addEventListener('size-change', syncSize) })
const panelVisible = ref(false)
const isCollapsed = ref(localStorage.getItem('app-aside-collapsed') === '1')
function toggleCollapse() {
  isCollapsed.value = !isCollapsed.value
  localStorage.setItem('app-aside-collapsed', isCollapsed.value ? '1' : '0')
}
const menus = computed(() => auth.getMenuPages())
function normalizeMenuPath(path: string) {
  if (!path) return ''
  return path.startsWith('/') ? path : `/${path}`
}
function descOrder(a: any, b: any) {
  return (b?.orderNum || 0) - (a?.orderNum || 0)
}
const menuTree = computed(() => {
  const all = (menus.value || []).map(p => ({ ...p, pathNorm: p?.path ? normalizeMenuPath(p.path) : '' }))
  const roots = all
    .filter(p => p.parentId === null || p.parentId === 0)
    .sort(descOrder)
  return roots.map(r => ({
    ...r,
    children: all
      .filter(p => p.parentId === r.id)
      .sort(descOrder)
  }))
})
const isAuth = computed(() => route.path.startsWith('/login') || route.path.startsWith('/register'))
const displayName = computed(() => auth.state.user?.userName || auth.state.user?.userAccount || '当前用户')

async function handleLogout() {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', { type: 'warning' })
  } catch {
    return
  }
  await auth.logout()
  router.replace('/login')
}
</script>

<style scoped lang="scss">
@media (prefers-color-scheme: dark) {
  :host {
    color: var(--el-text-color-primary)
  }
}

.route-fade-enter-active,
.route-fade-leave-active {
  transition: opacity .2s ease, transform .2s ease
}

.route-fade-enter-from {
  opacity: 0;
  transform: translateY(6px)
}

.route-fade-leave-to {
  opacity: 0;
  transform: translateY(-6px)
}
</style>
