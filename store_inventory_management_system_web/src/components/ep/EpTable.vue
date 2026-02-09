<template>
  <div class="ep-card" v-loading="loading" element-loading-text="加载中..." element-loading-background="rgba(255,255,255,0.6)">
    <div class="flex justify-between mb-3">
      <div class="flex gap-2">
        <el-input v-model="keyword" placeholder="搜索" clearable @input="onSearch" style="width:220px" />
        <slot name="toolbar"></slot>
      </div>
      <el-button type="primary" @click="$emit('refresh')"><el-icon><Refresh /></el-icon><span>刷新</span></el-button>
    </div>
    <vxe-table ref="tableRef" :data="rows" border stripe @checkbox-change="onCheckboxChange" @checkbox-all="onCheckboxChange">
      <vxe-column type="checkbox" width="48" />
      <vxe-column v-for="c in columns" :key="c.prop" :field="c.prop" :title="c.label" :width="c.width">
        <template #default="{ row }">
          <slot v-if="c.slot" :name="c.slot" :row="row"></slot>
          <span v-else>{{ row[c.prop] }}</span>
        </template>
      </vxe-column>
      <vxe-column title="操作" width="200">
        <template #default="scope">
          <el-button link type="primary" @click="$emit('edit', scope.row)" v-perm="editPerm"><el-icon><Edit /></el-icon>编辑</el-button>
          <el-button link type="danger" @click="$emit('remove', scope.row)" v-perm="removePerm"><el-icon><Delete /></el-icon>删除</el-button>
          <el-button link type="warning" @click="$emit('reset', scope.row)" v-perm="resetPerm"><el-icon><Key /></el-icon>重置密码</el-button>
      </template>
      </vxe-column>
    </vxe-table>
    <div class="mt-3 flex justify-end">
      <vxe-pager :current-page="pagination.current" :page-size="pagination.size" :total="pagination.total" @page-change="onPageChange" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { debounce } from 'lodash-es'
import type { VxeTableInstance } from 'vxe-table'
import { ElIcon } from 'element-plus'
import { Refresh, Edit, Delete, Key } from '@element-plus/icons-vue'

const props = defineProps<{
  rows: any[],
  columns: { prop: string, label: string, width?: number, slot?: string }[],
  loading: boolean,
  pagination: { total: number, current: number, size: number },
  editPerm?: string,
  removePerm?: string,
  resetPerm?: string
}>()
const emit = defineEmits(['refresh','edit','remove','reset','search','update:current','update:size','selection-change'])
const keyword = ref('')
const onSearch = debounce(() => { emit('search', keyword.value) }, 300)
const tableRef = ref<VxeTableInstance>()
function onCheckboxChange() {
  const records = tableRef.value?.getCheckboxRecords() || []
  emit('selection-change', records)
}
function onPageChange(e: any) {
  emit('update:current', e.currentPage)
  emit('update:size', e.pageSize)
}
</script>

<style scoped lang="scss">
.ep-card {
  background-color: var(--el-bg-color);
  border: 1px solid var(--el-border-color);
  border-radius: var(--el-border-radius-base);
  box-shadow: var(--el-box-shadow-light);
  padding: var(--app-card-padding);
}
</style>