<template>
  <el-drawer v-model="visible" title="主题设置" direction="rtl" size="420px">
    <div class="space-y-5">
      <div>
        <div class="mb-2">全局尺寸</div>
        <el-radio-group v-model="custom.size" @change="applySize">
          <el-radio-button label="small">紧凑</el-radio-button>
          <el-radio-button label="default">适中</el-radio-button>
          <el-radio-button label="large">舒适</el-radio-button>
        </el-radio-group>
      </div>
      <div>
        <div class="mb-2">主色</div>
        <el-color-picker v-model="custom.colorPrimary" show-alpha />
      </div>
      <div>
        <div class="mb-2">色板</div>
        <div class="grid grid-cols-2 gap-3">
          <div class="flex items-center gap-2"><el-tag type="success">成功</el-tag><el-color-picker v-model="custom.colorSuccess" /></div>
          <div class="flex items-center gap-2"><el-tag type="warning">警告</el-tag><el-color-picker v-model="custom.colorWarning" /></div>
          <div class="flex items-center gap-2"><el-tag type="danger">危险</el-tag><el-color-picker v-model="custom.colorDanger" /></div>
          <div class="flex items-center gap-2"><el-tag type="info">信息</el-tag><el-color-picker v-model="custom.colorInfo" /></div>
        </div>
      </div>
      <div>
        <div class="mb-2">圆角（base/small/round）</div>
        <div class="flex items-center gap-3">
          <el-input-number v-model="custom.radiusBase" :min="0" :max="32" />
          <el-input-number v-model="custom.radiusSmall" :min="0" :max="24" />
          <el-input-number v-model="custom.radiusRound" :min="0" :max="64" />
        </div>
      </div>
      <div>
        <div class="mb-2">按钮风格</div>
        <el-segmented v-model="custom.buttonShape" :options="[{label:'方形',value:'square'},{label:'圆角',value:'rounded'},{label:'圆形',value:'pill'}]" />
      </div>
      <div>
        <div class="mb-2">字体大小</div>
        <el-slider v-model="custom.fontSizeBase" :min="12" :max="18" :step="1" show-input />
      </div>
      <div>
        <div class="mb-2">控件间距</div>
        <div class="flex items-center gap-3">
          <div class="flex-1">
            <div class="text-xs mb-1">表单项间距</div>
            <el-slider v-model="custom.formGap" :min="8" :max="24" :step="1" />
          </div>
          <div class="flex-1">
            <div class="text-xs mb-1">卡片内边距</div>
            <el-slider v-model="custom.cardPadding" :min="8" :max="32" :step="1" />
          </div>
        </div>
      </div>
      <div>
        <div class="mb-2">卡片阴影强度</div>
        <el-segmented v-model="shadowPreset" :options="shadowOptions" />
      </div>
      <div>
        <div class="mb-2">组件示例</div>
        <div class="preview">
          <div class="ep-card p-4 space-y-3">
            <div class="text-sm text-[var(--el-text-color-regular)]">预览卡片</div>
            <el-input placeholder="示例输入" />
            <div class="flex gap-2">
              <el-button type="primary">主要按钮</el-button>
              <el-button>默认按钮</el-button>
            </div>
          </div>
        </div>
      </div>
      <div class="flex justify-between">
        <el-button @click="reset">恢复默认</el-button>
        <div class="flex gap-2">
          <el-button @click="save">保存</el-button>
          <el-button type="primary" @click="applyAll">应用</el-button>
        </div>
      </div>
    </div>
  </el-drawer>
</template>

<script setup lang="ts">
import { reactive, watch, onMounted, computed, ref } from 'vue'

const props = defineProps<{ modelValue: boolean }>()
const emit = defineEmits(['update:modelValue'])
const visible = computed<boolean>({ get: () => props.modelValue, set: (v: boolean) => emit('update:modelValue', v) })

type CustomTheme = {
  colorPrimary: string
  size: 'small' | 'default' | 'large'
  radiusBase: number
  radiusSmall: number
  radiusRound: number
  boxShadow: string
  boxShadowLight: string
  boxShadowDark: string
  fontSizeBase: number
  formGap: number
  cardPadding: number
  buttonShape: 'square' | 'rounded' | 'pill'
  colorSuccess: string
  colorWarning: string
  colorDanger: string
  colorInfo: string
}

const def: CustomTheme = {
  colorPrimary: '#409EFF',
  size: 'default',
  radiusBase: 8,
  radiusSmall: 6,
  radiusRound: 20,
  boxShadow: '0 10px 20px rgba(0,0,0,.08)',
  boxShadowLight: '0 1px 4px rgba(0,0,0,.06)',
  boxShadowDark: '0 14px 28px rgba(0,0,0,.18)',
  fontSizeBase: 14,
  formGap: 16,
  cardPadding: 16,
  buttonShape: 'rounded',
  colorSuccess: '#67C23A',
  colorWarning: '#E6A23C',
  colorDanger: '#F56C6C',
  colorInfo: '#909399'
}
const custom = reactive<CustomTheme>({ ...def })
const shadowPreset = ref<'light'|'normal'|'dark'>('normal')
const shadowOptions = [
  { label: '浅', value: 'light' },
  { label: '标准', value: 'normal' },
  { label: '深', value: 'dark' }
]

function apply(custom: CustomTheme) {
  const root = document.documentElement
  root.style.setProperty('--el-color-primary', custom.colorPrimary)
  root.style.setProperty('--el-color-success', custom.colorSuccess)
  root.style.setProperty('--el-color-warning', custom.colorWarning)
  root.style.setProperty('--el-color-danger', custom.colorDanger)
  root.style.setProperty('--el-color-info', custom.colorInfo)
  root.style.setProperty('--el-border-radius-base', custom.radiusBase + 'px')
  root.style.setProperty('--el-border-radius-small', custom.radiusSmall + 'px')
  root.style.setProperty('--el-border-radius-round', custom.radiusRound + 'px')
  root.style.setProperty('--el-box-shadow', custom.boxShadow)
  root.style.setProperty('--el-box-shadow-light', custom.boxShadowLight)
  root.style.setProperty('--el-box-shadow-dark', custom.boxShadowDark)
  root.style.setProperty('--el-font-size-base', custom.fontSizeBase + 'px')
  root.style.setProperty('--app-form-item-gap', custom.formGap + 'px')
  root.style.setProperty('--app-card-padding', custom.cardPadding + 'px')
  const br = custom.buttonShape === 'square' ? '0px' : custom.buttonShape === 'pill' ? '9999px' : custom.radiusBase + 'px'
  root.style.setProperty('--app-button-radius', br)
}
function applyAll() { apply(custom) }
function save() { localStorage.setItem('app-theme-custom', JSON.stringify(custom)) }
function reset() {
  Object.assign(custom, def)
  const root = document.documentElement
  ;['--el-color-primary','--el-border-radius-base','--el-border-radius-small','--el-border-radius-round','--el-box-shadow','--el-box-shadow-light','--el-box-shadow-dark'].forEach(k => root.style.removeProperty(k))
  localStorage.removeItem('app-theme-custom')
}
function applySize() {
  localStorage.setItem('app-size', custom.size)
  window.dispatchEvent(new CustomEvent('size-change'))
}
onMounted(() => {
  try {
    const saved = localStorage.getItem('app-theme-custom')
    if (saved) Object.assign(custom, JSON.parse(saved))
    apply(custom)
    const savedSize = localStorage.getItem('app-size') as CustomTheme['size'] | null
    if (savedSize) custom.size = savedSize
  } catch {}
})
watch(custom, () => apply(custom), { deep: true })
watch(shadowPreset, (p) => {
  if (p === 'light') {
    custom.boxShadow = '0 6px 12px rgba(0,0,0,.06)'
    custom.boxShadowLight = '0 1px 3px rgba(0,0,0,.05)'
    custom.boxShadowDark = '0 10px 20px rgba(0,0,0,.12)'
  } else if (p === 'dark') {
    custom.boxShadow = '0 12px 24px rgba(0,0,0,.22)'
    custom.boxShadowLight = '0 2px 5px rgba(0,0,0,.14)'
    custom.boxShadowDark = '0 18px 36px rgba(0,0,0,.35)'
  } else {
    custom.boxShadow = def.boxShadow
    custom.boxShadowLight = def.boxShadowLight
    custom.boxShadowDark = def.boxShadowDark
  }
})
</script>

<style scoped lang="scss">
.preview { .ep-card { border:1px solid var(--el-border-color); border-radius:var(--el-border-radius-base); box-shadow:var(--el-box-shadow-light); } }
</style>